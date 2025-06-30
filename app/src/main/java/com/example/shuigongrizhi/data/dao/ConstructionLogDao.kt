package com.example.shuigongrizhi.data.dao

import androidx.room.*
import com.example.shuigongrizhi.data.entity.ConstructionLog
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface ConstructionLogDao {
    @Query("SELECT * FROM construction_logs WHERE projectId = :projectId ORDER BY date DESC")
    fun getLogsByProjectId(projectId: Long): Flow<List<ConstructionLog>>

    @Query("SELECT * FROM construction_logs WHERE projectId = :projectId AND date = :date")
    suspend fun getLogByProjectAndDate(projectId: Long, date: Date): ConstructionLog?

    @Query("SELECT * FROM construction_logs WHERE id = :id")
    suspend fun getLogById(id: Long): ConstructionLog?

    @Query("SELECT * FROM construction_logs WHERE projectId = :projectId AND date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    suspend fun getLogsByDateRange(projectId: Long, startDate: Date, endDate: Date): List<ConstructionLog>

    @Insert
    suspend fun insertLog(log: ConstructionLog): Long

    @Update
    suspend fun updateLog(log: ConstructionLog)

    @Delete
    suspend fun deleteLog(log: ConstructionLog)

    @Query("DELETE FROM construction_logs WHERE id = :id")
    suspend fun deleteLogById(id: Long)

    @Query("SELECT COUNT(*) FROM construction_logs WHERE projectId = :projectId")
    suspend fun getLogCountByProject(projectId: Long): Int
}