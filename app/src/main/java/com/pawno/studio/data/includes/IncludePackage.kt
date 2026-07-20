package com.pawno.studio.data.includes

data class IncludePackage(
    val id: String,
    val name: String,
    val version: String,
    val author: String,
    val description: String,
    val isBundled: Boolean = true,
    val fileCount: Int = 0,
    val relativePath: String = ""
) {
    val folderName: String get() = relativePath
}
