package com.example.shuigongrizhi.data.dao

import androidx.room.*
import com.example.shuigongrizhi.data.entity.Project
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY id DESC")
    fun getAllProjects(): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getProjectById(id: Long): Project?

    @Insert
    suspend fun insertProject(project: Project): Long

    @Update
    suspend fun updateProject(project: Project)

    @Delete
    suspend fun deleteProject(project: Project)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteProjectById(id: Long)

    @Query("SELECT COUNT(*) FROM projects WHERE name = :name")
    suspend fun countProjectByName(name: String): Int

    @Query("SELECT COUNT(*) FROM projects WHERE name = :name AND id != :excludeId")
    suspend fun countProjectByNameAndIdNot(name: String, excludeId: Long): Int
}