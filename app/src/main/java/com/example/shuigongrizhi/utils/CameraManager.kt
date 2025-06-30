package com.example.shuigongrizhi.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.example.shuigongrizhi.data.entity.MediaFile
import com.example.shuigongrizhi.data.entity.MediaType
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CameraManager(private val context: Context) {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HHmmss", Locale.getDefault())
    
    /**
     * 为指定项目创建媒体目录结构
     * 目录结构: /ProjectData/{projectId}/media/{date}/photos|videos
     */
    fun createProjectMediaDirectories(projectId: Long): Boolean {
        return try {
            val today = dateFormat.format(Date())
            
            // 创建项目媒体根目录
            val projectMediaDir = getProjectMediaDir(projectId)
            projectMediaDir?.mkdirs()
            
            // 创建今日照片目录
            val todayPhotosDir = getProjectPhotosDir(projectId, today)
            todayPhotosDir?.mkdirs()
            
            // 创建今日视频目录
            val todayVideosDir = getProjectVideosDir(projectId, today)
            todayVideosDir?.mkdirs()
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * 为项目创建临时图片文件
     */
    fun createProjectImageFile(projectId: Long): Uri? {
        return try {
            val today = dateFormat.format(Date())
            val timestamp = timeFormat.format(Date())
            
            // 确保目录存在
            createProjectMediaDirectories(projectId)
            
            val photosDir = getProjectPhotosDir(projectId, today)
            val file = File(
                photosDir,
                "IMG_${projectId}_${today}_${timestamp}.jpg"
            )
            
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * 为项目创建临时视频文件
     */
    fun createProjectVideoFile(projectId: Long): Uri? {
        return try {
            val today = dateFormat.format(Date())
            val timestamp = timeFormat.format(Date())
            
            // 确保目录存在
            createProjectMediaDirectories(projectId)
            
            val videosDir = getProjectVideosDir(projectId, today)
            val file = File(
                videosDir,
                "VID_${projectId}_${today}_${timestamp}.mp4"
            )
            
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * 将临时文件移动到项目媒体目录
     */
    fun moveToProjectMediaDir(
        tempUri: Uri,
        projectId: Long,
        mediaType: MediaType,
        description: String = ""
    ): MediaFile? {
        return try {
            val today = dateFormat.format(Date())
            val timestamp = timeFormat.format(Date())
            
            // 获取临时文件
            val tempFile = File(tempUri.path ?: return null)
            if (!tempFile.exists()) return null
            
            // 确定目标目录和文件名
            val targetDir = when (mediaType) {
                MediaType.PHOTO -> getProjectPhotosDir(projectId, today)
                MediaType.VIDEO -> getProjectVideosDir(projectId, today)
            }
            
            val extension = when (mediaType) {
                MediaType.PHOTO -> ".jpg"
                MediaType.VIDEO -> ".mp4"
            }
            
            val prefix = when (mediaType) {
                MediaType.PHOTO -> "IMG"
                MediaType.VIDEO -> "VID"
            }
            
            val fileName = "${prefix}_${projectId}_${today}_${timestamp}${extension}"
            val targetFile = File(targetDir, fileName)
            
            // 移动文件
            if (tempFile.renameTo(targetFile)) {
                MediaFile(
                    logId = 0, // 将在保存日志时设置
                    filePath = targetFile.absolutePath,
                    fileName = fileName,
                    fileType = mediaType,
                    fileSize = targetFile.length(),
                    description = description,
                    createdAt = Date()
                )
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * 获取项目媒体根目录
     */
    private fun getProjectMediaDir(projectId: Long): File? {
        val projectDataDir = context.getExternalFilesDir("ProjectData")
        return File(projectDataDir, "$projectId/media")
    }
    
    /**
     * 获取项目指定日期的照片目录
     */
    private fun getProjectPhotosDir(projectId: Long, date: String): File? {
        val projectMediaDir = getProjectMediaDir(projectId)
        return File(projectMediaDir, "$date/photos")
    }
    
    /**
     * 获取项目指定日期的视频目录
     */
    private fun getProjectVideosDir(projectId: Long, date: String): File? {
        val projectMediaDir = getProjectMediaDir(projectId)
        return File(projectMediaDir, "$date/videos")
    }
    
    /**
     * 获取项目所有媒体文件路径
     */
    fun getProjectMediaFiles(projectId: Long): List<File> {
        val mediaFiles = mutableListOf<File>()
        val projectMediaDir = getProjectMediaDir(projectId) ?: return mediaFiles
        
        if (projectMediaDir.exists() && projectMediaDir.isDirectory) {
            projectMediaDir.walkTopDown().forEach { file ->
                if (file.isFile && (file.extension == "jpg" || file.extension == "mp4")) {
                    mediaFiles.add(file)
                }
            }
        }
        
        return mediaFiles
    }
    
    /**
     * 删除项目媒体文件
     */
    fun deleteProjectMediaFile(filePath: String): Boolean {
        return try {
            val file = File(filePath)
            file.delete()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * 获取项目媒体存储信息
     */
    fun getProjectMediaStorageInfo(projectId: Long): Map<String, Any> {
        val projectMediaDir = getProjectMediaDir(projectId)
        val mediaFiles = getProjectMediaFiles(projectId)
        
        var totalSize = 0L
        var photoCount = 0
        var videoCount = 0
        
        mediaFiles.forEach { file ->
            totalSize += file.length()
            when (file.extension) {
                "jpg" -> photoCount++
                "mp4" -> videoCount++
            }
        }
        
        return mapOf(
            "projectId" to projectId,
            "mediaDir" to (projectMediaDir?.absolutePath ?: ""),
            "totalFiles" to mediaFiles.size,
            "photoCount" to photoCount,
            "videoCount" to videoCount,
            "totalSize" to totalSize,
            "totalSizeMB" to (totalSize / 1024.0 / 1024.0)
        )
    }
}