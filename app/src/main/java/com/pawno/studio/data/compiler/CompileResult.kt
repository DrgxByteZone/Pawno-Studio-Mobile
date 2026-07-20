package com.pawno.studio.data.compiler

data class CompileResult(
    val exitCode: Int,
    val errorCount: Int,
    val warningCount: Int,
    val elapsedMs: Double,
    val rawErrors: String,
    val rawOutput: String,
    val diagnostics: List<DiagnosticItem>,
    val targetFile: String = "",
    val outputAmxFile: String? = null,
    val amxSizeBytes: Long = 0L,
    val compilerVersion: CompilerVersion = CompilerVersion.ZEEX_3_10_11,
    val headerSizeBytes: Long = 0L
) {
    val isSuccess: Boolean
        get() = exitCode == 0 && errorCount == 0

    val success: Boolean
        get() = isSuccess

    val hasErrors: Boolean
        get() = !isSuccess

    val outputAmxPath: String?
        get() = outputAmxFile

    val summaryText: String
        get() = if (isSuccess) {
            "Build Succeeded (0 Errors, $warningCount Warnings)"
        } else {
            "Build Failed ($errorCount Errors, $warningCount Warnings)"
        }
}
