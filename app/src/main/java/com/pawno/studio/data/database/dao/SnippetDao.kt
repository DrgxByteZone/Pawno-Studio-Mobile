package com.pawno.studio.data.database.dao

import com.pawno.studio.data.database.entity.SnippetEntity
import kotlinx.coroutines.flow.Flow

interface SnippetDao {
    fun getAllSnippets(): Flow<List<SnippetEntity>>
    suspend fun insert(snippet: SnippetEntity)
    suspend fun insertAll(snippets: List<SnippetEntity>)
    suspend fun delete(prefix: String)
}
