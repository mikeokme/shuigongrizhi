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

    // 媒体浏览相关查询
    @Query("SELECT * FROM media_files WHERE logId IN (SELECT id FROM construction_logs WHERE projectId = :projectId) ORDER BY createdAt DESC")
    fun getMediaFilesByProjectId(projectId: Long): Flow<List<MediaFile>>

    @Query("SELECT COUNT(*) FROM media_files WHERE logId IN (SELECT id FROM construction_logs WHERE projectId = :projectId)")
    suspend fun getMediaFileCountByProjectId(projectId: Long): Int

    @Query("SELECT * FROM media_files WHERE logId IN (SELECT id FROM construction_logs WHERE projectId = :projectId) AND fileType = :fileType ORDER BY createdAt DESC")
    fun getMediaFilesByProjectIdAndType(projectId: Long, fileType: String): Flow<List<MediaFile>>

    @Query("SELECT * FROM media_files WHERE logId IN (SELECT id FROM construction_logs WHERE projectId = :projectId) ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestMediaFileByProjectId(projectId: Long): MediaFile?

    @Query("SELECT * FROM media_files WHERE logId IN (SELECT id FROM construction_logs WHERE projectId = :projectId) AND (fileName LIKE '%' || :searchQuery || '%' OR description LIKE '%' || :searchQuery || '%') ORDER BY createdAt DESC")
    fun searchMediaFilesByProjectId(projectId: Long, searchQuery: String): Flow<List<MediaFile>>

    @Query("SELECT * FROM media_files WHERE logId IN (SELECT id FROM construction_logs WHERE projectId = :projectId) AND createdAt BETWEEN :startDate AND :endDate ORDER BY createdAt DESC")
    fun getMediaFilesByProjectIdAndDateRange(projectId: Long, startDate: Long, endDate: Long): Flow<List<MediaFile>>
}