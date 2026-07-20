package com.pawno.studio.data.compiler

enum class DiagnosticSeverity(val label: String) {
    ERROR("Error"),
    FATAL_ERROR("Fatal Error"),
    WARNING("Warning"),
    NOTE("Note");

    val isError: Boolean
        get() = this == ERROR || this == FATAL_ERROR

    companion object {
        val FATAL: DiagnosticSeverity = FATAL_ERROR
    }
}
