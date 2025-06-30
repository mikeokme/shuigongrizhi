package com.example.shuigongrizhi.data.repository

import com.example.shuigongrizhi.data.dao.ConstructionLogDao
import com.example.shuigongrizhi.data.entity.ConstructionLog
import kotlinx.coroutines.flow.Flow
import java.util.Date

class ConstructionLogRepository(private val constructionLogDao: ConstructionLogDao) {
    fun getLogsByProjectId(projectId: Long): Flow<List<ConstructionLog>> = 
        constructionLogDao.getLogsByProjectId(projectId)

    suspend fun getLogByProjectAndDate(projectId: Long, date: Date): ConstructionLog? = 
        constructionLogDao.getLogByProjectAndDate(projectId, date)

    suspend fun getLogById(id: Long): ConstructionLog? = constructionLogDao.getLogById(id)

    suspend fun getLogsByDateRange(projectId: Long, startDate: Date, endDate: Date): List<ConstructionLog> = 
        constructionLogDao.getLogsByDateRange(projectId, startDate, endDate)

    suspend fun insertLog(log: ConstructionLog): Long = constructionLogDao.insertLog(log)

    suspend fun updateLog(log: ConstructionLog) = constructionLogDao.updateLog(log)

    suspend fun deleteLog(log: ConstructionLog) = constructionLogDao.deleteLog(log)

    suspend fun deleteLogById(id: Long) = constructionLogDao.deleteLogById(id)

    suspend fun getLogCountByProject(projectId: Long): Int = 
        constructionLogDao.getLogCountByProject(projectId)
}