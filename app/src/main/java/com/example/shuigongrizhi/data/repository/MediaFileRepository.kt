package com.example.shuigongrizhi.data.repository

import com.example.shuigongrizhi.data.dao.MediaFileDao
import com.example.shuigongrizhi.data.entity.MediaFile
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaFileRepository @Inject constructor(private val mediaFileDao: MediaFileDao) {
    fun getMediaFilesByLogId(logId: Long): Flow<List<MediaFile>> = 
        mediaFileDao.getMediaFilesByLogId(logId)

    suspend fun getMediaFileById(id: Long): MediaFile? = mediaFileDao.getMediaFileById(id)

    suspend fun insertMediaFile(mediaFile: MediaFile): Long = mediaFileDao.insertMediaFile(mediaFile)

    suspend fun updateMediaFile(mediaFile: MediaFile) = mediaFileDao.updateMediaFile(mediaFile)

    suspend fun deleteMediaFile(mediaFile: MediaFile) = mediaFileDao.deleteMediaFile(mediaFile)

    suspend fun deleteMediaFileById(id: Long) = mediaFileDao.deleteMediaFileById(id)

    suspend fun deleteMediaFilesByLogId(logId: Long) = mediaFileDao.deleteMediaFilesByLogId(logId)

    // 媒体浏览相关方法
    fun getMediaFilesByProjectId(projectId: Long): Flow<List<MediaFile>> = 
        mediaFileDao.getMediaFilesByProjectId(projectId)

    suspend fun getMediaFileCountByProjectId(projectId: Long): Int = 
        mediaFileDao.getMediaFileCountByProjectId(projectId)

    fun getMediaFilesByProjectIdAndType(projectId: Long, fileType: String): Flow<List<MediaFile>> = 
        mediaFileDao.getMediaFilesByProjectIdAndType(projectId, fileType)

    suspend fun getLatestMediaFileByProjectId(projectId: Long): MediaFile? = 
        mediaFileDao.getLatestMediaFileByProjectId(projectId)

    fun searchMediaFilesByProjectId(projectId: Long, searchQuery: String): Flow<List<MediaFile>> = 
        mediaFileDao.searchMediaFilesByProjectId(projectId, searchQuery)

    fun getMediaFilesByProjectIdAndDateRange(projectId: Long, startDate: Long, endDate: Long): Flow<List<MediaFile>> = 
        mediaFileDao.getMediaFilesByProjectIdAndDateRange(projectId, startDate, endDate)
}