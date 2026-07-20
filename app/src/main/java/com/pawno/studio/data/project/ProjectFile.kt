package com.pawno.studio.data.project

import java.io.File

data class ProjectFile(
    val file: File,
    val name: String = file.name,
    val isDirectory: Boolean = file.isDirectory,
    val extension: String = file.extension.lowercase(),
    val sizeBytes: Long = if (file.isFile) file.length() else 0L,
    val isPawnSource: Boolean = file.extension.equals("pwn", ignoreCase = true),
    val isPawnInclude: Boolean = file.extension.equals("inc", ignoreCase = true),
    val isAmxBinary: Boolean = file.extension.equals("amx", ignoreCase = true),
    val isConfigFile: Boolean = file.extension.equals("cfg", ignoreCase = true) || file.extension.equals("ini", ignoreCase = true) || file.extension.equals("json", ignoreCase = true),
    val children: List<ProjectFile> = emptyList(),
    var isDirty: Boolean = false,
    val isMainEntrypoint: Boolean = false,
    val relativePath: String = file.name,
    val absolutePath: String = file.absolutePath,
    val isGamemode: Boolean = file.extension.equals("pwn", ignoreCase = true),
    val isInclude: Boolean = file.extension.equals("inc", ignoreCase = true)
)
