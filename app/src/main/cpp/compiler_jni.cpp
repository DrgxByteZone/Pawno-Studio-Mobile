/**
 * Advanced JNI Wrapper for Pawn Compiler Engine
 * Pawno Studio Mobile
 * Author: By M.B.A & AXEL - Blackpanther Company
 * License: Apache License 2.0
 */

#include <jni.h>
#include <string>
#include <vector>
#include <mutex>
#include <sstream>
#include <cstring>
#include <cstdarg>
#include <cstdio>
#include <cstdlib>
#include <android/log.h>
#include <unistd.h>
#include <libgen.h>
#include <pthread.h>
#include <map>
#include <unordered_map>

#include <dirent.h>
#include <strings.h>
#include <sys/stat.h>

#define LOG_TAG "PawnoCompilerEngine"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

// 16MB Stack size to safely handle large gamemodes (50,000 - 150,000+ lines)
#define COMPILE_THREAD_STACK_SIZE (16 * 1024 * 1024)

extern "C" {
    int pc_compile(int argc, char *argv[]);
    int pc_geterrorwarnings(void);
}

extern "C" {
    typedef unsigned char MEMFILE;
    MEMFILE *mfcreate(const char *filename);
    void     mfclose(MEMFILE *mf);
    int      mfdump(MEMFILE *mf);
    long     mfseek(MEMFILE *mf, long offset, int whence);
    int      mfputs(MEMFILE *mf, const char *string);
    char    *mfgets(MEMFILE *mf, char *string, unsigned int size);
}

namespace {
    std::mutex g_outputMutex;
    std::stringstream g_outputBuffer;
    std::stringstream g_errorBuffer;
    std::stringstream g_structuredDiagnostics;

    int g_warningCount = 0;
    int g_errorCount = 0;
    const int MAX_DIAGNOSTICS_CAPTURE = 500;
    const size_t MAX_BUFFER_SIZE = 2 * 1024 * 1024; // 2MB dynamic buffer

    struct CachedFile {
        const std::string* data;
        size_t pos;
        size_t savedPos;
        CachedFile(const std::string* d) : data(d), pos(0), savedPos(0) {}
    };

    std::unordered_map<std::string, std::string> g_srcCache;
    std::string g_cacheDir = "/tmp";

    void clearBuffers() {
        std::lock_guard<std::mutex> lock(g_outputMutex);
        g_outputBuffer.str("");
        g_outputBuffer.clear();
        g_errorBuffer.str("");
        g_errorBuffer.clear();
        g_structuredDiagnostics.str("");
        g_structuredDiagnostics.clear();
        g_warningCount = 0;
        g_errorCount = 0;
    }

    // Helper to escape json string
    std::string escapeJson(const std::string& s) {
        std::ostringstream o;
        for (auto c = s.cbegin(); c != s.cend(); ++c) {
            switch (*c) {
            case '"': o << "\\\""; break;
            case '\\': o << "\\\\"; break;
            case '\b': o << "\\b"; break;
            case '\f': o << "\\f"; break;
            case '\n': o << "\\n"; break;
            case '\r': o << "\\r"; break;
            case '\t': o << "\\t"; break;
            default:
                if ('\x00' <= *c && *c <= '\x1f') {
                    o << "\\u" << std::hex << (int)*c;
                } else {
                    o << *c;
                }
            }
        }
        return o.str();
    }
}

// Intercept compiler printf
extern "C" int pc_printf(const char* message, ...) {
    char buf[4096];
    if (!message) return 0;

    va_list argptr;
    va_start(argptr, message);
    va_list argcopy;
    va_copy(argcopy, argptr);
    int n = vsnprintf(buf, sizeof(buf), message, argcopy);
    va_end(argcopy);

    char* buffer = buf;
    char* heapBuffer = nullptr;
    if (n >= (int)sizeof(buf)) {
        heapBuffer = static_cast<char*>(malloc(n + 1));
        if (heapBuffer) {
            vsnprintf(heapBuffer, n + 1, message, argptr);
            buffer = heapBuffer;
        }
    }
    va_end(argptr);

    if (n > 0) {
        std::lock_guard<std::mutex> lock(g_outputMutex);
        if (g_outputBuffer.tellp() < (std::streampos)MAX_BUFFER_SIZE) {
            g_outputBuffer << buffer;
        }
    }

    if (heapBuffer) free(heapBuffer);
    return n;
}

// Intercept compiler error & warning reporting
extern "C" int pc_error(int number, char* message, char* filename,
                        int firstline, int lastline, va_list argptr) {
    char buf[4096];
    bool warnAsError = (number >= 200 && pc_geterrorwarnings());
    bool isWarning   = (number >= 200 && !warnAsError);
    bool isError     = (number > 0 && number < 200) || warnAsError;

    static const char* prefix[3] = {"error", "fatal error", "warning"};

    va_list argcopy;
    va_copy(argcopy, argptr);
    int n = vsnprintf(buf, sizeof(buf), message, argcopy);
    va_end(argcopy);

    char* msgBuffer = buf;
    char* heapBuffer = nullptr;
    if (n >= (int)sizeof(buf)) {
        heapBuffer = static_cast<char*>(malloc(n + 1));
        if (heapBuffer) {
            vsnprintf(heapBuffer, n + 1, message, argptr);
            msgBuffer = heapBuffer;
        }
    }

    std::stringstream textStream;
    if (number != 0) {
        const char* pre = prefix[number / 100];
        if (warnAsError) pre = prefix[0];

        if (number == 111 || number == 237) {
            textStream << filename << "(" << lastline << ") : ";
        } else if (firstline >= 0) {
            textStream << filename << "(" << firstline << " -- " << lastline << ") : "
                       << pre << " " << number << ": ";
        } else {
            textStream << filename << "(" << lastline << ") : "
                       << pre << " " << number << ": ";
        }
    }
    textStream << msgBuffer << "\n";

    {
        std::lock_guard<std::mutex> lock(g_outputMutex);
        if (isWarning) ++g_warningCount;
        if (isError) ++g_errorCount;

        if (g_errorBuffer.tellp() < (std::streampos)MAX_BUFFER_SIZE) {
            g_errorBuffer << textStream.str();
        }

        // Capture structured JSON diagnostic item
        if (g_warningCount + g_errorCount <= MAX_DIAGNOSTICS_CAPTURE) {
            std::stringstream diagJson;
            diagJson << "{"
                     << "\"code\":" << number << ","
                     << "\"severity\":\"" << (isWarning ? "WARNING" : "ERROR") << "\","
                     << "\"file\":\"" << escapeJson(filename ? filename : "") << "\","
                     << "\"line\":" << lastline << ","
                     << "\"firstLine\":" << firstline << ","
                     << "\"message\":\"" << escapeJson(msgBuffer) << "\""
                     << "},";
            g_structuredDiagnostics << diagJson.str();
        }
    }

    if (heapBuffer) free(heapBuffer);
    return 0;
}

// Case-insensitive path resolver for Windows-ported SA-MP gamemodes on Android (Linux ext4 / FUSE)
static std::string resolve_case_insensitive_path(const std::string& inputPath) {
    if (inputPath.empty()) return inputPath;

    // Fast path: if path already exists directly, return it
    if (access(inputPath.c_str(), F_OK) == 0) {
        return inputPath;
    }

    // Normalize Windows backslashes
    std::string normalizedPath = inputPath;
    for (char& c : normalizedPath) {
        if (c == '\\') c = '/';
    }

    bool isAbsolute = (!normalizedPath.empty() && normalizedPath[0] == '/');
    std::vector<std::string> rawParts;
    std::stringstream ss(normalizedPath);
    std::string item;
    while (std::getline(ss, item, '/')) {
        if (!item.empty()) {
            rawParts.push_back(item);
        }
    }

    // Collapse '.' and '..' components to avoid FUSE opendir/stat failures on Android
    std::vector<std::string> parts;
    for (const auto& part : rawParts) {
        if (part == ".") {
            continue;
        } else if (part == "..") {
            if (!parts.empty() && parts.back() != "..") {
                parts.pop_back();
            } else if (!isAbsolute) {
                parts.push_back("..");
            }
        } else {
            parts.push_back(part);
        }
    }

    // Reconstruct canonical path
    std::string canonical;
    if (isAbsolute) {
        canonical = "/";
    }
    for (size_t i = 0; i < parts.size(); ++i) {
        if (i > 0 || !isAbsolute) {
            if (!canonical.empty() && canonical.back() != '/') canonical += "/";
        }
        canonical += parts[i];
    }

    // Check if canonical path exists directly
    if (!canonical.empty() && access(canonical.c_str(), F_OK) == 0) {
        return canonical;
    }

    // Walk components case-insensitively
    std::string current = isAbsolute ? "/" : ".";
    for (size_t i = 0; i < parts.size(); ++i) {
        const std::string& part = parts[i];
        if (part == "..") {
            current = (current == "/" ? "/" : current + "/") + part;
            continue;
        }

        std::string candidate = (current == "/" ? "/" : current + "/") + part;
        if (access(candidate.c_str(), F_OK) == 0) {
            current = candidate;
            continue;
        }

        DIR* dir = opendir(current.c_str());
        if (!dir) {
            return canonical.empty() ? inputPath : canonical;
        }

        struct dirent* entry;
        bool found = false;
        while ((entry = readdir(dir)) != nullptr) {
            if (strcasecmp(entry->d_name, part.c_str()) == 0) {
                current = (current == "/" ? "/" : current + "/") + std::string(entry->d_name);
                found = true;
                break;
            }
        }
        closedir(dir);
        if (!found) {
            return canonical.empty() ? inputPath : canonical;
        }
    }
    return current;
}

// Pawn file I/O hooks
extern "C" void* pc_opensrc(char* filename) {
    if (!filename) return nullptr;

    // Normalize Windows backslashes (\ -> /) for cross-platform compatibility
    std::string fname(filename);
    for (char& c : fname) {
        if (c == '\\') c = '/';
    }

    auto it = g_srcCache.find(fname);
    if (it == g_srcCache.end()) {
        FILE* f = fopen(fname.c_str(), "rb");
        if (!f && fname != filename) {
            f = fopen(filename, "rb");
        }
        if (!f) {
            std::string resolved = resolve_case_insensitive_path(fname);
            if (!resolved.empty() && resolved != fname) {
                f = fopen(resolved.c_str(), "rb");
                if (f) {
                    LOGD("Case-insensitive resolved: %s -> %s", fname.c_str(), resolved.c_str());
                }
            }
        }
        if (!f) return nullptr;

        fseek(f, 0, SEEK_END);
        long fsize = ftell(f);
        fseek(f, 0, SEEK_SET);

        if (fsize <= 0) {
            fclose(f);
            g_srcCache[fname] = "";
        } else {
            std::string content(fsize, '\0');
            size_t totalBytesRead = 0;
            while (totalBytesRead < static_cast<size_t>(fsize)) {
                size_t n = fread(&content[totalBytesRead], 1, static_cast<size_t>(fsize) - totalBytesRead, f);
                if (n == 0) {
                    if (feof(f)) break;
                    if (ferror(f)) {
                        LOGE("Error reading file %s at offset %zu: %s", fname.c_str(), totalBytesRead, strerror(errno));
                        break;
                    }
                }
                totalBytesRead += n;
            }
            content.resize(totalBytesRead);
            fclose(f);
            g_srcCache[fname] = std::move(content);
        }
        it = g_srcCache.find(fname);
    }
    return new CachedFile(&it->second);
}

extern "C" void* pc_createsrc(char* filename) {
    return fopen(filename, "wt");
}

extern "C" void* pc_createtmpsrc(char** filename) {
    char* tname = nullptr;
    FILE* ftmp = nullptr;
    std::string tmpl = g_cacheDir + "/pawnXXXXXX";
    if ((tname = static_cast<char*>(malloc(tmpl.size() + 1))) != nullptr) {
        int fdtmp = -1;
        memcpy(tname, tmpl.c_str(), tmpl.size() + 1);
        if ((fdtmp = mkstemp(tname)) >= 0) {
            ftmp = fdopen(fdtmp, "wt");
        }
        if (fdtmp < 0 || filename == nullptr) {
            free(tname);
            tname = nullptr;
        }
    }
    if (filename != nullptr) *filename = tname;
    return ftmp;
}

extern "C" void pc_closesrc(void* handle) {
    if (handle != nullptr) {
        delete static_cast<CachedFile*>(handle);
    }
}

extern "C" void pc_resetsrc(void* handle, void* position) {
    if (handle != nullptr) {
        static_cast<CachedFile*>(handle)->pos = *static_cast<size_t*>(position);
    }
}

extern "C" char* pc_readsrc(void* handle, unsigned char* target, int maxchars) {
    if (!handle) return nullptr;
    CachedFile* cf = static_cast<CachedFile*>(handle);
    const std::string& data = *cf->data;
    if (cf->pos >= data.size() || maxchars <= 1) return nullptr;

    size_t avail = data.size() - cf->pos;
    size_t maxread = (avail < (size_t)(maxchars - 1)) ? avail : (size_t)(maxchars - 1);
    const char* src = data.c_str() + cf->pos;
    const char* nl = (const char*)memchr(src, '\n', maxread);
    size_t copylen = nl ? (size_t)(nl - src + 1) : maxread;

    memcpy(target, src, copylen);
    target[copylen] = '\0';
    cf->pos += copylen;
    return reinterpret_cast<char*>(target);
}

extern "C" int pc_writesrc(void* handle, unsigned char* source) {
    return fputs(reinterpret_cast<char*>(source), static_cast<FILE*>(handle)) >= 0;
}

extern "C" void* pc_getpossrc(void* handle) {
    CachedFile* cf = static_cast<CachedFile*>(handle);
    cf->savedPos = cf->pos;
    return &cf->savedPos;
}

extern "C" int pc_eofsrc(void* handle) {
    CachedFile* cf = static_cast<CachedFile*>(handle);
    return (cf->pos >= cf->data->size()) ? 1 : 0;
}

extern "C" void* pc_openasm(char* filename) {
    return mfcreate(filename);
}

extern "C" void pc_closeasm(void* handle, int deletefile) {
    if (handle != nullptr) {
        if (!deletefile) mfdump(static_cast<MEMFILE*>(handle));
        mfclose(static_cast<MEMFILE*>(handle));
    }
}

extern "C" void pc_resetasm(void* handle) {
    if (handle != nullptr) {
        mfseek(static_cast<MEMFILE*>(handle), 0, SEEK_SET);
    }
}

extern "C" int pc_writeasm(void* handle, char* string) {
    return mfputs(static_cast<MEMFILE*>(handle), string);
}

extern "C" char* pc_readasm(void* handle, char* string, int maxchars) {
    return mfgets(static_cast<MEMFILE*>(handle), string, static_cast<unsigned int>(maxchars));
}

extern "C" void* pc_openbin(char* filename) {
    FILE* fbin = fopen(filename, "wb");
    if (fbin != nullptr) {
        setvbuf(fbin, nullptr, _IOFBF, 2UL << 20); // 2MB I/O buffer
    }
    return fbin;
}

extern "C" void pc_closebin(void* handle, int deletefile) {
    if (handle != nullptr) {
        fclose(static_cast<FILE*>(handle));
        if (deletefile) {
            extern char binfname[];
            remove(binfname);
        }
    }
}

extern "C" void pc_resetbin(void* handle, long offset) {
    if (handle != nullptr) {
        fflush(static_cast<FILE*>(handle));
        fseek(static_cast<FILE*>(handle), offset, SEEK_SET);
    }
}

extern "C" int pc_writebin(void* handle, void* buffer, int size) {
    return static_cast<int>(fwrite(buffer, 1, size, static_cast<FILE*>(handle))) == size;
}

extern "C" long pc_lengthbin(void* handle) {
    return ftell(static_cast<FILE*>(handle));
}

// High-capacity compilation thread
struct CompileArgs {
    int argc;
    char** argv;
    int result;
    double elapsedMs;
};

static void* compiler_worker_thread(void* arg) {
    CompileArgs* cargs = static_cast<CompileArgs*>(arg);
    struct timespec start, end;
    clock_gettime(CLOCK_MONOTONIC, &start);

    cargs->result = pc_compile(cargs->argc, cargs->argv);

    clock_gettime(CLOCK_MONOTONIC, &end);
    cargs->elapsedMs = (end.tv_sec - start.tv_sec) * 1000.0
                     + (end.tv_nsec - start.tv_nsec) / 1000000.0;
    return nullptr;
}

extern "C" {

JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    LOGI("Pawno Studio Native Engine loaded successfully");
    return JNI_VERSION_1_6;
}

static jstring compileInternalCommon(
    JNIEnv* env, jobject thiz,
    jobjectArray args, jstring cacheDir
) {
    clearBuffers();
    g_srcCache.clear();

    if (cacheDir != nullptr) {
        const char* cDir = env->GetStringUTFChars(cacheDir, nullptr);
        if (cDir) {
            g_cacheDir = cDir;
            env->ReleaseStringUTFChars(cacheDir, cDir);
        }
    }

    int argc = env->GetArrayLength(args);
    if (argc == 0) {
        return env->NewStringUTF("{\"exitCode\":-1,\"errorCount\":1,\"warningCount\":0,\"elapsedMs\":0,\"rawOutput\":\"No arguments provided\",\"diagnostics\":[]}");
    }

    std::vector<std::string> argsStorage;
    argsStorage.reserve(argc + 2);

    for (int i = 0; i < argc; i++) {
        jstring jstr = static_cast<jstring>(env->GetObjectArrayElement(args, i));
        const char* str = env->GetStringUTFChars(jstr, nullptr);
        if (str) {
            std::string argStr(str);
            // If argument is a path (-i, -D, -o, or source file), normalize backslashes
            if (argStr.rfind("-i", 0) == 0 || argStr.rfind("-D", 0) == 0 || argStr.rfind("-o", 0) == 0 || argStr[0] != '-') {
                for (char& c : argStr) {
                    if (c == '\\') c = '/';
                }
            }
            argsStorage.emplace_back(std::move(argStr));
            env->ReleaseStringUTFChars(jstr, str);
        }
        env->DeleteLocalRef(jstr);
    }

    std::vector<char*> argv(argsStorage.size());
    for (size_t i = 0; i < argsStorage.size(); i++) {
        argv[i] = const_cast<char*>(argsStorage[i].c_str());
    }

    CompileArgs cargs = {(int)argv.size(), argv.data(), -1, 0.0};
    pthread_t thread;
    pthread_attr_t attr;

    pthread_attr_init(&attr);
    pthread_attr_setstacksize(&attr, COMPILE_THREAD_STACK_SIZE);

    if (pthread_create(&thread, &attr, compiler_worker_thread, &cargs) != 0) {
        pthread_attr_destroy(&attr);
        cargs.result = pc_compile((int)argv.size(), argv.data());
    } else {
        pthread_attr_destroy(&attr);
        pthread_join(thread, nullptr);
    }
    g_srcCache.clear();

    std::string diagList = g_structuredDiagnostics.str();
    if (!diagList.empty() && diagList.back() == ',') {
        diagList.pop_back(); // Remove trailing comma
    }

    std::stringstream resultJson;
    resultJson << "{"
               << "\"exitCode\":" << cargs.result << ","
               << "\"errorCount\":" << g_errorCount << ","
               << "\"warningCount\":" << g_warningCount << ","
               << "\"elapsedMs\":" << cargs.elapsedMs << ","
               << "\"rawErrors\":\"" << escapeJson(g_errorBuffer.str()) << "\","
               << "\"rawOutput\":\"" << escapeJson(g_outputBuffer.str()) << "\","
               << "\"diagnostics\":[" << diagList << "]"
               << "}";

    return env->NewStringUTF(resultJson.str().c_str());
}

#if defined(PAWNC_BUILD_32)
JNIEXPORT jstring JNICALL
Java_com_pawno_studio_data_compiler_PawnCompilerEngine_compileNative32(
    JNIEnv* env, jobject thiz,
    jobjectArray args, jstring cacheDir
) {
    return compileInternalCommon(env, thiz, args, cacheDir);
}
#elif defined(PAWNC_BUILD_3107)
JNIEXPORT jstring JNICALL
Java_com_pawno_studio_data_compiler_PawnCompilerEngine_compileNative3107(
    JNIEnv* env, jobject thiz,
    jobjectArray args, jstring cacheDir
) {
    return compileInternalCommon(env, thiz, args, cacheDir);
}
#else
JNIEXPORT jstring JNICALL
Java_com_pawno_studio_data_compiler_PawnCompilerEngine_compileNative31011(
    JNIEnv* env, jobject thiz,
    jobjectArray args, jstring cacheDir
) {
    return compileInternalCommon(env, thiz, args, cacheDir);
}

JNIEXPORT jstring JNICALL
Java_com_pawno_studio_data_compiler_PawnCompilerEngine_compileNative(
    JNIEnv* env, jobject thiz,
    jobjectArray args, jstring cacheDir
) {
    return compileInternalCommon(env, thiz, args, cacheDir);
}
#endif

} // extern "C"
