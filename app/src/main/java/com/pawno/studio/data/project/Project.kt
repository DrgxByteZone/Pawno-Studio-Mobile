package com.pawno.studio.data.project

import java.io.File

data class Project(
    val id: String,
    val name: String,
    val rootDir: File,
    val mainGamemodeFile: File? = null,
    val lastOpenedTimestamp: Long = System.currentTimeMillis(),
    val totalFiles: Int = 0
)
