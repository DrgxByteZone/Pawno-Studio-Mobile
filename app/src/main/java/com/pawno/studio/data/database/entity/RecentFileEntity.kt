package com.pawno.studio.data.database.entity

data class RecentFileEntity(
    val filePath: String,
    val fileName: String,
    val lastOpenedTimestamp: Long = System.currentTimeMillis(),
    val cursorLine: Int = 1,
    val cursorColumn: Int = 1
)
