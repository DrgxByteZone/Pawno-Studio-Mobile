/**
 * AMX Binary Inspector & Opcode Header Extractor
 * Pawno Studio Mobile
 * Author: By M.B.A & AXEL - Blackpanther Company
 */

#include <jni.h>
#include <string>
#include <vector>
#include <fstream>
#include <sstream>
#include <cstdint>
#include <cstring>
#include <android/log.h>

#define LOG_TAG "PawnoAMX"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

#pragma pack(push, 1)
struct AMX_HEADER {
    int32_t size;          /* size of the "file" */
    uint16_t magic;        /* signature 0xf1e0 */
    char file_version;     /* file format version */
    char amx_version;      /* required version of the AMX */
    int16_t flags;
    int16_t defsize;       /* size of a definition record */
    int32_t cod;           /* initial value of COD - code offset */
    int32_t dat;           /* initial value of DAT - data offset */
    int32_t hea;           /* initial value of HEA - start of the heap */
    int32_t stp;           /* initial value of STP - stack top */
    int32_t cip;           /* initial value of CIP - the instruction pointer */
    int32_t publics;       /* offset to the "public functions" table */
    int32_t natives;       /* offset to the "native functions" table */
    int32_t libraries;     /* offset to the table of libraries */
    int32_t pubvars;       /* offset to the "public variables" table */
    int32_t tags;          /* offset to the "public tagnames" table */
    int32_t nametable;     /* offset to the name table */
};

struct AMX_FUNCSTUBNT {
    uint32_t address;
    uint32_t nameoffs;
};
#pragma pack(pop)

#define AMX_MAGIC 0xF1E0

extern "C" {

JNIEXPORT jstring JNICALL
Java_com_pawno_studio_data_amx_AmxInspector_inspectNative(
    JNIEnv* env, jobject thiz, jstring amxFilePath
) {
    if (!amxFilePath) {
        return env->NewStringUTF("{\"error\":\"File path is null\"}");
    }

    const char* path = env->GetStringUTFChars(amxFilePath, nullptr);
    std::ifstream file(path, std::ios::binary | std::ios::ate);
    if (!file.is_open()) {
        std::string err = std::string("{\"error\":\"Cannot open file: ") + path + "\"}";
        env->ReleaseStringUTFChars(amxFilePath, path);
        return env->NewStringUTF(err.c_str());
    }

    std::streamsize fileSize = file.tellg();
    if (fileSize < (std::streamsize)sizeof(AMX_HEADER)) {
        env->ReleaseStringUTFChars(amxFilePath, path);
        return env->NewStringUTF("{\"error\":\"File is smaller than AMX header size\"}");
    }

    file.seekg(0, std::ios::beg);
    std::vector<uint8_t> buffer(fileSize);
    if (!file.read(reinterpret_cast<char*>(buffer.data()), fileSize)) {
        env->ReleaseStringUTFChars(amxFilePath, path);
        return env->NewStringUTF("{\"error\":\"Failed to read AMX bytes\"}");
    }
    env->ReleaseStringUTFChars(amxFilePath, path);

    const AMX_HEADER* hdr = reinterpret_cast<const AMX_HEADER*>(buffer.data());
    if (hdr->magic != AMX_MAGIC) {
        return env->NewStringUTF("{\"error\":\"Invalid AMX magic signature (Not a valid AMX binary)\"}");
    }

    int32_t codeSize = hdr->dat - hdr->cod;
    int32_t dataSize = hdr->hea - hdr->dat;
    int32_t stackSize = hdr->stp - hdr->hea;
    int32_t totalSize = hdr->size;

    // Helper lambda to read a null-terminated string from nametable
    auto readName = [&](uint32_t nameoffs) -> std::string {
        if (nameoffs >= (uint32_t)fileSize) return "";
        const char* str = reinterpret_cast<const char*>(buffer.data() + nameoffs);
        size_t maxLen = fileSize - nameoffs;
        size_t len = strnlen(str, maxLen);
        return std::string(str, len);
    };

    // Public functions extraction
    int numPublics = 0;
    if (hdr->natives > hdr->publics && hdr->defsize > 0) {
        numPublics = (hdr->natives - hdr->publics) / hdr->defsize;
    }
    std::stringstream publicsJson;
    publicsJson << "[";
    for (int i = 0; i < numPublics && i < 200; ++i) {
        size_t offset = hdr->publics + i * hdr->defsize;
        if (offset + sizeof(AMX_FUNCSTUBNT) <= (size_t)fileSize) {
            const AMX_FUNCSTUBNT* stub = reinterpret_cast<const AMX_FUNCSTUBNT*>(buffer.data() + offset);
            std::string name = readName(stub->nameoffs);
            if (i > 0) publicsJson << ",";
            publicsJson << "{\"name\":\"" << name << "\",\"address\":" << stub->address << "}";
        }
    }
    publicsJson << "]";

    // Native functions extraction
    int numNatives = 0;
    if (hdr->libraries > hdr->natives && hdr->defsize > 0) {
        numNatives = (hdr->libraries - hdr->natives) / hdr->defsize;
    }
    std::stringstream nativesJson;
    nativesJson << "[";
    for (int i = 0; i < numNatives && i < 200; ++i) {
        size_t offset = hdr->natives + i * hdr->defsize;
        if (offset + sizeof(AMX_FUNCSTUBNT) <= (size_t)fileSize) {
            const AMX_FUNCSTUBNT* stub = reinterpret_cast<const AMX_FUNCSTUBNT*>(buffer.data() + offset);
            std::string name = readName(stub->nameoffs);
            if (i > 0) nativesJson << ",";
            nativesJson << "{\"name\":\"" << name << "\",\"address\":" << stub->address << "}";
        }
    }
    nativesJson << "]";

    std::stringstream json;
    json << "{"
         << "\"isValid\":true,"
         << "\"magic\":" << hdr->magic << ","
         << "\"fileVersion\":" << (int)hdr->file_version << ","
         << "\"amxVersion\":" << (int)hdr->amx_version << ","
         << "\"flags\":" << hdr->flags << ","
         << "\"codeSize\":" << codeSize << ","
         << "\"dataSize\":" << dataSize << ","
         << "\"stackSize\":" << stackSize << ","
         << "\"totalSize\":" << totalSize << ","
         << "\"cip\":" << hdr->cip << ","
         << "\"numPublics\":" << numPublics << ","
         << "\"numNatives\":" << numNatives << ","
         << "\"publics\":" << publicsJson.str() << ","
         << "\"natives\":" << nativesJson.str()
         << "}";

    return env->NewStringUTF(json.str().c_str());
}

} // extern "C"
