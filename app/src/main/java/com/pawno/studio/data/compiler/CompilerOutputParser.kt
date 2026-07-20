package com.pawno.studio.data.compiler

import org.json.JSONObject

object CompilerOutputParser {

    private val DIAGNOSTIC_REGEX = """^(.*)\((\d+)(?:\s*--\s*(\d+))?\)\s*:\s*(error|fatal error|warning)\s+(\d+)\s*:\s*(.*)$""".toRegex()

    fun parseJsonResult(jsonString: String, targetFile: String): CompileResult {
        return try {
            val json = JSONObject(jsonString)
            val exitCode = json.optInt("exitCode", -1)
            val errorCount = json.optInt("errorCount", 0)
            val warningCount = json.optInt("warningCount", 0)
            val elapsedMs = json.optDouble("elapsedMs", 0.0)
            val rawErrors = json.optString("rawErrors", "")
            val rawOutput = json.optString("rawOutput", "")

            val diagnosticsList = mutableListOf<DiagnosticItem>()
            val diagArray = json.optJSONArray("diagnostics")
            if (diagArray != null) {
                for (i in 0 until diagArray.length()) {
                    val dObj = diagArray.getJSONObject(i)
                    val code = dObj.optInt("code", 0)
                    val sevStr = dObj.optString("severity", "ERROR")
                    val file = dObj.optString("file", targetFile)
                    val line = dObj.optInt("line", 1)
                    val firstLine = dObj.optInt("firstLine", line)
                    val msg = dObj.optString("message", "")

                    val severity = when (sevStr.uppercase()) {
                        "FATAL_ERROR", "FATAL" -> DiagnosticSeverity.FATAL_ERROR
                        "WARNING" -> DiagnosticSeverity.WARNING
                        else -> DiagnosticSeverity.ERROR
                    }

                    diagnosticsList.add(
                        DiagnosticItem(
                            code = code,
                            severity = severity,
                            file = file,
                            line = line,
                            firstLine = firstLine,
                            message = msg
                        )
                    )
                }
            }

            // Fallback: If structured diagnostics is empty but rawErrors has content, parse via regex
            if (diagnosticsList.isEmpty() && rawErrors.isNotBlank()) {
                diagnosticsList.addAll(parseRawLines(rawErrors, targetFile))
            }

            CompileResult(
                exitCode = exitCode,
                errorCount = if (errorCount > 0) errorCount else diagnosticsList.count { it.severity == DiagnosticSeverity.ERROR || it.severity == DiagnosticSeverity.FATAL_ERROR },
                warningCount = if (warningCount > 0) warningCount else diagnosticsList.count { it.severity == DiagnosticSeverity.WARNING },
                elapsedMs = elapsedMs,
                rawErrors = rawErrors,
                rawOutput = rawOutput,
                diagnostics = diagnosticsList,
                targetFile = targetFile
            )
        } catch (e: Exception) {
            CompileResult(
                exitCode = -1,
                errorCount = 1,
                warningCount = 0,
                elapsedMs = 0.0,
                rawErrors = jsonString,
                rawOutput = "",
                diagnostics = parseRawLines(jsonString, targetFile),
                targetFile = targetFile
            )
        }
    }

    fun parseRawLines(rawText: String, defaultFile: String): List<DiagnosticItem> {
        val items = mutableListOf<DiagnosticItem>()
        rawText.lines().forEach { line ->
            val match = DIAGNOSTIC_REGEX.matchEntire(line.trim())
            if (match != null) {
                val (filePath, lineStr, endLineStr, typeStr, codeStr, message) = match.destructured
                val lineNum = lineStr.toIntOrNull() ?: 1
                val firstLineNum = if (endLineStr.isNotEmpty()) lineNum else -1
                val actualLine = if (endLineStr.isNotEmpty()) (endLineStr.toIntOrNull() ?: lineNum) else lineNum
                val code = codeStr.toIntOrNull() ?: 0

                val severity = when (typeStr.lowercase()) {
                    "fatal error" -> DiagnosticSeverity.FATAL_ERROR
                    "warning" -> DiagnosticSeverity.WARNING
                    else -> DiagnosticSeverity.ERROR
                }

                items.add(
                    DiagnosticItem(
                        code = code,
                        severity = severity,
                        file = filePath.ifBlank { defaultFile },
                        line = actualLine,
                        firstLine = firstLineNum,
                        message = message.trim(),
                        rawText = line
                    )
                )
            }
        }
        return items
    }
}
