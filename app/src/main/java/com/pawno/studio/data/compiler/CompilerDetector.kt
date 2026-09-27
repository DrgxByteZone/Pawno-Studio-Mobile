package com.pawno.studio.data.compiler

import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.nio.charset.StandardCharsets

data class DetectionResult(
    val recommendedVersion: CompilerVersion,
    val source: DetectionSource,
    val confidence: Float,
    val details: String,
    val recommendedFlags: List<String> = emptyList()
)

enum class DetectionSource {
    BINARY_SIGNATURE, // Found pawncc.exe / pawnc.dll
    PROJECT_CONFIG,   // sampctl.json / pawn.json
    CODE_HEURISTICS,  // Scanned .pwn for open.mp or old includes
    DEFAULT_FALLBACK
}

object CompilerDetector {
    private const val TAG = "CompilerDetector"

    fun detect(rootDir: File, mainPwn: File? = null): DetectionResult {
        if (!rootDir.exists()) {
            return DetectionResult(
                recommendedVersion = CompilerVersion.ZEEX_3_10_11,
                source = DetectionSource.DEFAULT_FALLBACK,
                confidence = 0.5f,
                details = "Directory not found, default to Pawn 3.10.11"
            )
        }

        // 1. Check for settings.ini to extract extra flags (like -w203)
        val extractedFlags = mutableListOf<String>()
        val settingsIniCandidates = listOf(
            File(rootDir, "pawno/settings.ini"),
            File(rootDir, "settings.ini")
        )
        for (ini in settingsIniCandidates) {
            if (ini.exists() && ini.isFile) {
                parseSettingsIniParams(ini)?.let { flags ->
                    extractedFlags.addAll(flags)
                }
                break
            }
        }

        // 2. Binary Signature Check: Inspect pawncc.exe or pawnc.dll
        val binaryCandidates = listOf(
            File(rootDir, "pawno/pawncc.exe"),
            File(rootDir, "pawno/pawnc.dll"),
            File(rootDir, "pawno/libpawnc.dll"),
            File(rootDir, "pawncc.exe"),
            File(rootDir, "pawnc.dll")
        )

        for (bin in binaryCandidates) {
            if (bin.exists() && bin.isFile) {
                val detected = inspectBinaryStrings(bin)
                if (detected != null) {
                    val flags = if (detected == CompilerVersion.COMPUPHASE_3_2) {
                        val merged = extractedFlags.toMutableList()
                        if (!merged.contains("-;+")) merged.add(0, "-;+")
                        if (!merged.contains("-(+")) merged.add(1, "-(+")
                        if (!merged.any { it.startsWith("-w203") }) merged.add("-w203")
                        merged
                    } else {
                        extractedFlags
                    }
                    return DetectionResult(
                        recommendedVersion = detected,
                        source = DetectionSource.BINARY_SIGNATURE,
                        confidence = 1.0f,
                        details = "Detected ${detected.displayName} from ${bin.relativeTo(rootDir).path}",
                        recommendedFlags = flags
                    )
                }
            }
        }

        // 3. Project Config Check: sampctl.json / pawn.json
        val jsonCandidates = listOf(
            File(rootDir, "pawn.json"),
            File(rootDir, "pawn.yaml"),
            File(rootDir, "sampctl.json")
        )
        for (cfg in jsonCandidates) {
            if (cfg.exists() && cfg.isFile) {
                try {
                    val content = cfg.readText(StandardCharsets.UTF_8).lowercase()
                    if (content.contains("open.mp") || content.contains("3.10.11")) {
                        return DetectionResult(
                            recommendedVersion = CompilerVersion.ZEEX_3_10_11,
                            source = DetectionSource.PROJECT_CONFIG,
                            confidence = 0.95f,
                            details = "Config ${cfg.name} specifies open.mp / modern environment",
                            recommendedFlags = extractedFlags
                        )
                    } else if (content.contains("3.10.7") || content.contains("3.10.8")) {
                        return DetectionResult(
                            recommendedVersion = CompilerVersion.ZEEX_3_10_7,
                            source = DetectionSource.PROJECT_CONFIG,
                            confidence = 0.95f,
                            details = "Config ${cfg.name} specifies Pawn 3.10.7 / 3.10.8",
                            recommendedFlags = extractedFlags
                        )
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed reading project config: ${cfg.name}", e)
                }
            }
        }

        // 4. Code Heuristics: Scan main .pwn header (e.g. for open.mp)
        val pwnToScan = mainPwn ?: findMainPwn(rootDir)
        if (pwnToScan != null && pwnToScan.exists() && pwnToScan.isFile) {
            val heuristicResult = scanCodeHeuristics(pwnToScan)
            if (heuristicResult != null) {
                return DetectionResult(
                    recommendedVersion = heuristicResult,
                    source = DetectionSource.CODE_HEURISTICS,
                    confidence = 0.85f,
                    details = "Code pattern in ${pwnToScan.name} indicates ${heuristicResult.displayName}",
                    recommendedFlags = extractedFlags
                )
            }
        }

        // 5. Default Fallback -> Modern Standard (Pawn 3.10.11)
        return DetectionResult(
            recommendedVersion = CompilerVersion.ZEEX_3_10_11,
            source = DetectionSource.DEFAULT_FALLBACK,
            confidence = 0.7f,
            details = "Standard Community Default (Pawn 3.10.11)",
            recommendedFlags = extractedFlags
        )
    }

    private fun inspectBinaryStrings(file: File): CompilerVersion? {
        try {
            FileInputStream(file).use { input ->
                val buffer = ByteArray(minOf(file.length(), 512 * 1024).toInt())
                var totalRead = 0
                while (totalRead < buffer.size) {
                    val read = input.read(buffer, totalRead, buffer.size - totalRead)
                    if (read <= 0) break
                    totalRead += read
                }
                val content = String(buffer, 0, totalRead, StandardCharsets.ISO_8859_1)

                // Check for CompuPhase 3.2 signatures
                if (content.contains("3.2.3664") ||
                    content.contains("Copyright (c) 1997-2017, ITB CompuPhase") ||
                    (content.contains("ITB CompuPhase") && !content.contains("Zeex") && !content.contains("3.10"))) {
                    return CompilerVersion.COMPUPHASE_3_2
                }

                // Check for Zeex 3.10.11
                if (content.contains("3.10.11")) {
                    return CompilerVersion.ZEEX_3_10_11
                }

                // Check for Zeex 3.10.7 / 3.10.8
                if (content.contains("3.10.7") || content.contains("3.10.8")) {
                    return CompilerVersion.ZEEX_3_10_7
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error inspecting binary: ${file.name}", e)
        }
        return null
    }

    private fun parseSettingsIniParams(iniFile: File): List<String>? {
        return try {
            iniFile.useLines { lines ->
                for (line in lines) {
                    val trimmed = line.trim()
                    if (trimmed.startsWith("Params", ignoreCase = true) && trimmed.contains("=")) {
                        val value = trimmed.substringAfter("=").trim()
                        val tokens = value.split("\\s+".toRegex()).filter { it.isNotBlank() }
                        val result = mutableListOf<String>()
                        var i = 0
                        while (i < tokens.size) {
                            val token = tokens[i]
                            if ((token == "-w" || token == "-d") && i + 1 < tokens.size && !tokens[i + 1].startsWith("-")) {
                                result.add("$token${tokens[i + 1]}")
                                i += 2
                            } else {
                                result.add(token)
                                i++
                            }
                        }
                        return@useLines result
                    }
                }
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun scanCodeHeuristics(pwnFile: File): CompilerVersion? {
        return try {
            var hasOpenMp = false
            var lineCount = 0

            pwnFile.useLines { lines ->
                for (line in lines) {
                    lineCount++
                    if (lineCount > 150) break
                    val trimmed = line.trim()
                    if (trimmed.startsWith("#include")) {
                        if (trimmed.contains("open.mp", ignoreCase = true) || trimmed.contains("<omp>", ignoreCase = true)) {
                            hasOpenMp = true
                            return@useLines
                        }
                    }
                }
            }

            if (hasOpenMp) CompilerVersion.ZEEX_3_10_11 else null
        } catch (e: Exception) {
            null
        }
    }

    private fun findMainPwn(rootDir: File): File? {
        val gmDir = File(rootDir, "gamemodes")
        return when {
            File(gmDir, "main.pwn").exists() -> File(gmDir, "main.pwn")
            File(rootDir, "main.pwn").exists() -> File(rootDir, "main.pwn")
            gmDir.exists() -> gmDir.listFiles()?.firstOrNull { it.extension.equals("pwn", ignoreCase = true) }
            else -> rootDir.walkTopDown().firstOrNull { it.extension.equals("pwn", ignoreCase = true) }
        }
    }
}
