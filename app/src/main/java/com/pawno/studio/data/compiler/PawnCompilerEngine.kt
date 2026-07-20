package com.pawno.studio.data.compiler

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class PawnCompilerEngine(private val context: Context) {

    private var currentLoadedLib: String? = null

    @Synchronized
    fun ensureLibraryLoaded(version: CompilerVersion) {
        if (currentLoadedLib != version.libName) {
            try {
                System.loadLibrary(version.libName)
                currentLoadedLib = version.libName
                Log.i(TAG, "Successfully loaded native library: ${version.libName}")
            } catch (e: UnsatisfiedLinkError) {
                Log.e(TAG, "Failed to load native library: ${version.libName}", e)
                throw e
            }
        }
    }

    suspend fun compile(options: CompileOptions): CompileResult {
        return compile(
            sourceFile = File(options.sourcePath),
            version = options.compilerVersion,
            options = options
        )
    }

    suspend fun compile(
        sourceFile: File,
        version: CompilerVersion,
        options: CompileOptions,
        additionalIncludes: List<String> = emptyList()
    ): CompileResult = withContext(Dispatchers.IO) {
        ensureLibraryLoaded(version)

        val workingDir = sourceFile.parentFile ?: context.filesDir
        val projectRoot = workingDir.parentFile ?: workingDir
        val cacheDir = context.cacheDir.absolutePath

        val mergedIncludes = mutableListOf<String>()

        // 1. Local project include directories (prioritized over bundled)
        val includeCandidates = listOf(
            File(workingDir, "pawno/include"),
            File(projectRoot, "pawno/include"),
            File(workingDir, "include"),
            File(projectRoot, "include"),
            workingDir,
            File(workingDir, "core"),
            File(workingDir, "utils"),
            projectRoot
        )

        for (dir in includeCandidates) {
            if (dir.exists() && dir.isDirectory) {
                mergedIncludes.add(dir.absolutePath)
            }
        }

        // 2. Bundled app includes & user settings includes
        mergedIncludes.addAll(additionalIncludes)
        mergedIncludes.addAll(options.includePaths)

        val finalOptions = options.copy(includePaths = mergedIncludes.distinct())
        val cliArgs = finalOptions.toCliArgs(sourceFile.absolutePath).toMutableList()



        // Prepend binary command name as argv[0]
        val fullArgv = mutableListOf("pawncc")
        // -D working directory
        fullArgv.add("-D${workingDir.absolutePath}")
        fullArgv.addAll(cliArgs)

        Log.i(TAG, "Invoking pc_compile with args: ${fullArgv.joinToString(" ")}")

        val jsonResult = compileNative(fullArgv.toTypedArray(), cacheDir)
        val result = CompilerOutputParser.parseJsonResult(jsonResult, sourceFile.name)

        // Check if output AMX was created
        val expectedAmx = if (options.outputAmxPath != null) {
            File(options.outputAmxPath)
        } else {
            File(workingDir, sourceFile.nameWithoutExtension + ".amx")
        }

        val amxSize = if (expectedAmx.exists()) expectedAmx.length() else 0L

        result.copy(
            outputAmxFile = if (expectedAmx.exists()) expectedAmx.absolutePath else null,
            amxSizeBytes = amxSize,
            compilerVersion = version
        )
    }

    private external fun compileNative(args: Array<String>, cacheDir: String): String

    companion object {
        private const val TAG = "PawnCompilerEngine"

        @Volatile
        private var INSTANCE: PawnCompilerEngine? = null

        fun getInstance(context: Context): PawnCompilerEngine {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PawnCompilerEngine(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
