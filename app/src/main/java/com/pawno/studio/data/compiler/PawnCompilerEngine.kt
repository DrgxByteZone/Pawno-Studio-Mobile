package com.pawno.studio.data.compiler

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class PawnCompilerEngine(private val context: Context) {

    private val loadedLibraries = mutableSetOf<String>()

    @Synchronized
    fun ensureLibraryLoaded(version: CompilerVersion) {
        val targetLib = when (version) {
            CompilerVersion.AUTO -> CompilerVersion.ZEEX_3_10_11.libName
            else -> version.libName
        }

        if (!loadedLibraries.contains(targetLib)) {
            try {
                System.loadLibrary(targetLib)
                loadedLibraries.add(targetLib)
                Log.i(TAG, "Successfully loaded native library: $targetLib")
            } catch (e: UnsatisfiedLinkError) {
                Log.e(TAG, "Failed to load native library: $targetLib", e)
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
        val workingDir = sourceFile.parentFile ?: context.filesDir
        val projectRoot = workingDir.parentFile ?: workingDir

        if (version == CompilerVersion.AUTO) {
            return@withContext compileWithAutoDetection(sourceFile, workingDir, projectRoot, options, additionalIncludes)
        }

        compileInternal(sourceFile, version, options, additionalIncludes, isAutoFallback = false)
    }

    private fun compileWithAutoDetection(
        sourceFile: File,
        workingDir: File,
        projectRoot: File,
        options: CompileOptions,
        additionalIncludes: List<String>
    ): CompileResult {
        val detection = CompilerDetector.detect(projectRoot, sourceFile)
        Log.i(TAG, "AUTO Mode: ${detection.details} (Confidence: ${detection.confidence})")

        val targetVersion = detection.recommendedVersion
        val initialOptions = if (detection.recommendedFlags.isNotEmpty()) {
            val flagsStr = detection.recommendedFlags.joinToString(" ")
            val mergedFlags = if (options.customFlags.isBlank()) flagsStr else "${options.customFlags} $flagsStr"
            options.copy(customFlags = mergedFlags)
        } else {
            options
        }

        val primaryResult = compileInternal(sourceFile, targetVersion, initialOptions, additionalIncludes, isAutoFallback = false)
        if (primaryResult.isSuccess) {
            return primaryResult
        }

        // Smart Fallback:
        // Only attempt fallback if the primary compiler was 3.10 and we encounter errors typical of legacy Pawn 3.2 code
        val couldBeLegacy = targetVersion != CompilerVersion.COMPUPHASE_3_2 &&
                primaryResult.errorCount > 0 &&
                isLikelyLegacyGamemode(primaryResult.rawErrors)

        if (!couldBeLegacy) {
            return primaryResult
        }

        Log.w(TAG, "AUTO Mode: Detected possible legacy syntax. Testing Pawn 3.2 fallback...")
        val fallbackVersion = CompilerVersion.COMPUPHASE_3_2
        val fallbackResult = compileInternal(sourceFile, fallbackVersion, initialOptions, additionalIncludes, isAutoFallback = true)

        // Only adopt fallback if it actually SUCCEEDED with 0 errors and created an AMX file
        return if (fallbackResult.isSuccess && fallbackResult.outputAmxFile != null) {
            Log.i(TAG, "AUTO Mode Auto-Fallback to ${fallbackVersion.displayName} SUCCEEDED!")
            fallbackResult.copy(
                isAutoRecovered = true,
                autoRecoveryReason = "Gamemode menggunakan sintaks CompuPhase legacy. Otomatis di-recover menggunakan engine ${fallbackVersion.displayName}!"
            )
        } else {
            // Keep primary result so user sees actual errors for their target version
            Log.i(TAG, "AUTO Mode: Fallback did not succeed. Returning primary compiler result.")
            primaryResult
        }
    }

    private fun isLikelyLegacyGamemode(rawErrors: String): Boolean {
        return rawErrors.contains("error 010") ||
               rawErrors.contains("error 001") ||
               rawErrors.contains("warning 208") ||
               rawErrors.contains("error 029")
    }

    private fun compileInternal(
        sourceFile: File,
        version: CompilerVersion,
        options: CompileOptions,
        additionalIncludes: List<String>,
        isAutoFallback: Boolean
    ): CompileResult {
        ensureLibraryLoaded(version)

        val workingDir = sourceFile.parentFile ?: context.filesDir
        val projectRoot = workingDir.parentFile ?: workingDir
        val cacheDir = context.cacheDir.absolutePath

        val mergedIncludes = mutableListOf<String>()

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

        mergedIncludes.addAll(additionalIncludes)
        mergedIncludes.addAll(options.includePaths)

        val expectedAmx = if (options.outputAmxPath != null) {
            File(options.outputAmxPath)
        } else {
            File(workingDir, sourceFile.nameWithoutExtension + ".amx")
        }

        if (expectedAmx.exists()) {
            expectedAmx.delete()
        }

        val finalOptions = options.copy(
            includePaths = mergedIncludes.distinct(),
            outputAmxPath = expectedAmx.absolutePath
        )
        val cliArgs = finalOptions.toCliArgs(sourceFile.absolutePath).toMutableList()

        // Prepend binary command name as argv[0]
        val fullArgv = mutableListOf("pawncc")
        fullArgv.add("-D${workingDir.absolutePath}")

        // For Pawn 3.2, ensure Pawno default flags are added if not specified
        if (version == CompilerVersion.COMPUPHASE_3_2) {
            if (!cliArgs.any { it.startsWith("-;") }) fullArgv.add("-;+")
            if (!cliArgs.any { it.startsWith("-(") }) fullArgv.add("-(+")
        }

        // Sanitize arguments: Ensure options like "-w" followed by "203" are merged into "-w203"
        // This is critical because in Pawn 3.2, a standalone number is treated as an input source file (e.g. 203.p)
        var argIdx = 0
        while (argIdx < cliArgs.size) {
            val arg = cliArgs[argIdx]
            if ((arg == "-w" || arg == "-d") && argIdx + 1 < cliArgs.size && !cliArgs[argIdx + 1].startsWith("-")) {
                fullArgv.add("$arg${cliArgs[argIdx + 1]}")
                argIdx += 2
            } else {
                fullArgv.add(arg)
                argIdx++
            }
        }

        Log.i(TAG, "Invoking pc_compile (${version.displayName}) with args: ${fullArgv.joinToString(" ")}")

        val jsonResult = try {
            when (version) {
                CompilerVersion.COMPUPHASE_3_2 -> compileNative32(fullArgv.toTypedArray(), cacheDir)
                CompilerVersion.ZEEX_3_10_7 -> compileNative3107(fullArgv.toTypedArray(), cacheDir)
                CompilerVersion.ZEEX_3_10_11 -> compileNative31011(fullArgv.toTypedArray(), cacheDir)
                CompilerVersion.AUTO -> compileNative31011(fullArgv.toTypedArray(), cacheDir)
            }
        } catch (e: UnsatisfiedLinkError) {
            Log.w(TAG, "Versioned native method not found, falling back to compileNative", e)
            compileNative(fullArgv.toTypedArray(), cacheDir)
        }

        val result = CompilerOutputParser.parseJsonResult(jsonResult, sourceFile.name)
        val amxSize = if (expectedAmx.exists()) expectedAmx.length() else 0L

        return result.copy(
            outputAmxFile = if (expectedAmx.exists()) expectedAmx.absolutePath else null,
            amxSizeBytes = amxSize,
            compilerVersion = version,
            isAutoRecovered = isAutoFallback
        )
    }

    private external fun compileNative32(args: Array<String>, cacheDir: String): String
    private external fun compileNative3107(args: Array<String>, cacheDir: String): String
    private external fun compileNative31011(args: Array<String>, cacheDir: String): String
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
