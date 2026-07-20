package com.pawno.studio.data.compiler

data class DiagnosticItem(
    val code: Int,
    val severity: DiagnosticSeverity,
    val file: String,
    val line: Int,
    val firstLine: Int = -1,
    val message: String,
    val rawText: String = "",
    val sourceSnippet: String? = null,
    val suggestion: String? = null
) {
    val errorCode: Int get() = code
    val lineNumber: Int get() = line
    val fileName: String get() = file

    val displayTitle: String
        get() = when (severity) {
            DiagnosticSeverity.FATAL_ERROR -> "Fatal Error $code"
            DiagnosticSeverity.ERROR -> "Error $code"
            DiagnosticSeverity.WARNING -> "Warning $code"
            DiagnosticSeverity.NOTE -> "Note $code"
        }

    val locationText: String
        get() = if (firstLine > 0 && firstLine != line) {
            "lines $firstLine -- $line"
        } else {
            "line $line"
        }
}
