package com.pawno.studio.data.database.entity

data class ProjectEntity(
    val projectPath: String,
    val projectName: String,
    val mainFilePath: String?,
    val lastModifiedTimestamp: Long = System.currentTimeMillis()
)
