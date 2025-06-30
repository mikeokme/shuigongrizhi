package com.example.shuigongrizhi.data.dao

import androidx.room.*
import com.example.shuigongrizhi.data.entity.MediaFile
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaFileDao {
    @Query("SELECT * FROM media_files WHERE logId = :logId ORDER BY createdAt ASC")
    fun getMediaFilesByLogId(logId: Long): Flow<List<MediaFile>>

    @Query("SELECT * FROM media_files WHERE id = :id")
    suspend fun getMediaFileById(id: Long): MediaFile?

    @Insert
    suspend fun insertMediaFile(mediaFile: MediaFile): Long

    @Update
    suspend fun updateMediaFile(mediaFile: MediaFile)

    @Delete
    suspend fun deleteMediaFile(mediaFile: MediaFile)

    @Query("DELETE FROM media_files WHERE id = :id")
    suspend fun deleteMediaFileById(id: Long)

    @Query("DELETE FROM media_files WHERE logId = :logId")
    suspend fun deleteMediaFilesByLogId(logId: Long)
}