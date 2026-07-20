package com.pawno.studio.data.database.dao

import com.pawno.studio.data.database.entity.RecentFileEntity
import kotlinx.coroutines.flow.Flow

interface RecentFileDao {
    fun getRecentFiles(): Flow<List<RecentFileEntity>>
    suspend fun insertOrUpdate(file: RecentFileEntity)
    suspend fun delete(filePath: String)
    suspend fun clearAll()
}
