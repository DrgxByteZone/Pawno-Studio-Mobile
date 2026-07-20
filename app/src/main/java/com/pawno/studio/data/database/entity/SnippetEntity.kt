package com.pawno.studio.data.database.entity

data class SnippetEntity(
    val prefix: String,
    val title: String,
    val codeBody: String,
    val category: String = "general"
)
