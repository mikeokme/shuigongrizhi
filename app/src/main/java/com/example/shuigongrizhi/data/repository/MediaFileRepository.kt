package com.example.shuigongrizhi.data.repository

import com.example.shuigongrizhi.data.dao.MediaFileDao
import com.example.shuigongrizhi.data.entity.MediaFile
import kotlinx.coroutines.flow.Flow

class MediaFileRepository(private val mediaFileDao: MediaFileDao) {
    fun getMediaFilesByLogId(logId: Long): Flow<List<MediaFile>> = 
        mediaFileDao.getMediaFilesByLogId(logId)

    suspend fun getMediaFileById(id: Long): MediaFile? = mediaFileDao.getMediaFileById(id)

    suspend fun insertMediaFile(mediaFile: MediaFile): Long = mediaFileDao.insertMediaFile(mediaFile)

    suspend fun updateMediaFile(mediaFile: MediaFile) = mediaFileDao.updateMediaFile(mediaFile)

    suspend fun deleteMediaFile(mediaFile: MediaFile) = mediaFileDao.deleteMediaFile(mediaFile)

    suspend fun deleteMediaFileById(id: Long) = mediaFileDao.deleteMediaFileById(id)

    suspend fun deleteMediaFilesByLogId(logId: Long) = mediaFileDao.deleteMediaFilesByLogId(logId)
}