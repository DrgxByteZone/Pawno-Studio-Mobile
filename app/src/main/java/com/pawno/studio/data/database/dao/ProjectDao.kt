package com.pawno.studio.data.database.dao

import com.pawno.studio.data.database.entity.ProjectEntity
import kotlinx.coroutines.flow.Flow

interface ProjectDao {
    fun getAllProjects(): Flow<List<ProjectEntity>>
    suspend fun insertOrUpdate(project: ProjectEntity)
    suspend fun delete(path: String)
}
