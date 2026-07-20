package com.pawno.studio.data.compiler

data class CompileOptions(
    val sourcePath: String = "",
    val outputAmxPath: String? = null,
    val compilerVersion: CompilerVersion = CompilerVersion.ZEEX_3_10_11,
    val includePaths: List<String> = emptyList(),
    val optimizationLevel: Int = 2,
    val debugLevel: Int = 2,
    val stackReserveBytes: Int = 16384,
    val compactEncoding: Boolean = false,
    val compatibilityMode: Boolean = false,
    val treatWarningsAsErrors: Boolean = false,
    val requireSemicolons: Boolean = true,
    val requireParentheses: Boolean = true,
    val memoryThreshold: Int = 16384,
    val customFlags: String = ""
) {
    fun toCliArgs(sourceFilePath: String = sourcePath): List<String> {
        val args = mutableListOf<String>()

        // Debug level
        args.add("-d$debugLevel")

        // Optimization level
        if (optimizationLevel in 0..2) {
            args.add("-O$optimizationLevel")
        }

        // Stack/Heap reserve in cells (-S). Do NOT pass -X as -X is the abstract machine total size limit
        // which triggers Error 106 if the AMX binary exceeds that size.
        if (stackReserveBytes > 16384) {
            args.add("-S${stackReserveBytes / 4}")
        }

        // Compact bytecode
        if (compactEncoding) {
            args.add("-C")
        }



        // Treat warnings as errors
        if (treatWarningsAsErrors) {
            args.add("-w200")
        }

        // Syntax rules
        if (requireSemicolons) args.add("-;+")
        if (requireParentheses) args.add("-(+")

        // Output path if customized
        outputAmxPath?.let {
            if (it.isNotBlank()) args.add("-o$it")
        }

        // Include paths
        for (path in includePaths) {
            if (path.isNotBlank()) {
                args.add("-i$path")
            }
        }

        // Custom flags
        if (customFlags.isNotBlank()) {
            val tokens = customFlags.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
            args.addAll(tokens)
        }

        // Source file MUST be the last argument
        val actualSource = if (sourceFilePath.isNotBlank()) sourceFilePath else sourcePath
        args.add(actualSource)

        return args
    }
}
