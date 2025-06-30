package com.example.shuigongrizhi.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shuigongrizhi.data.entity.MediaFile
import com.example.shuigongrizhi.data.repository.MediaFileRepository
import com.example.shuigongrizhi.utils.CameraManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class MediaGalleryViewModel @Inject constructor(
    private val mediaFileRepository: MediaFileRepository
) : ViewModel() {
    
    private lateinit var cameraManager: CameraManager
    private var currentProjectId: Long = 0
    
    // 媒体文件列表
    private val _mediaFiles = MutableStateFlow<List<MediaFile>>(emptyList())
    val mediaFiles: StateFlow<List<MediaFile>> = _mediaFiles.asStateFlow()
    
    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // 错误信息
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // 存储信息
    private val _storageInfo = MutableStateFlow<Map<String, Any>>(emptyMap())
    val storageInfo: StateFlow<Map<String, Any>> = _storageInfo.asStateFlow()
    
    /**
     * 初始化项目
     */
    fun initializeProject(projectId: Long, context: Context) {
        currentProjectId = projectId
        cameraManager = CameraManager(context)
        updateStorageInfo()
    }
    
    /**
     * 加载项目媒体文件
     */
    fun loadProjectMediaFiles() {
        if (!::cameraManager.isInitialized) {
            _error.value = "相机管理器未初始化"
            return
        }
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                // 获取物理文件列表
                val physicalFiles = cameraManager.getProjectMediaFiles(currentProjectId)
                
                // 获取数据库中的媒体文件记录
                val dbMediaFiles = mutableListOf<MediaFile>()
                
                // 为每个物理文件查找或创建数据库记录
                physicalFiles.forEach { file ->
                    val existingMediaFile = findMediaFileByPath(file.absolutePath)
                    if (existingMediaFile != null) {
                        dbMediaFiles.add(existingMediaFile)
                    } else {
                        // 如果数据库中没有记录，创建一个新的记录
                        val newMediaFile = createMediaFileFromPhysicalFile(file)
                        val insertedId = mediaFileRepository.insertMediaFile(newMediaFile)
                        dbMediaFiles.add(newMediaFile.copy(id = insertedId))
                    }
                }
                
                _mediaFiles.value = dbMediaFiles.sortedByDescending { it.createdAt }
                updateStorageInfo()
                
            } catch (e: Exception) {
                _error.value = "加载媒体文件失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 根据文件路径查找媒体文件记录
     */
    private suspend fun findMediaFileByPath(filePath: String): MediaFile? {
        return try {
            // 这里需要在MediaFileDao中添加按路径查询的方法
            // 暂时返回null，后续需要扩展DAO
            null
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 从物理文件创建MediaFile对象
     */
    private fun createMediaFileFromPhysicalFile(file: File): MediaFile {
        val fileType = when (file.extension.lowercase()) {
            "jpg", "jpeg", "png" -> com.example.shuigongrizhi.data.entity.MediaType.PHOTO
            "mp4", "avi", "mov" -> com.example.shuigongrizhi.data.entity.MediaType.VIDEO
            else -> com.example.shuigongrizhi.data.entity.MediaType.PHOTO
        }
        
        return MediaFile(
            logId = currentProjectId, // 使用项目ID作为logId
            filePath = file.absolutePath,
            fileName = file.name,
            fileType = fileType,
            fileSize = file.length(),
            description = "",
            createdAt = java.util.Date(file.lastModified())
        )
    }
    
    /**
     * 删除媒体文件
     */
    fun deleteMediaFile(mediaFile: MediaFile) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // 从数据库删除
                mediaFileRepository.deleteMediaFile(mediaFile)
                
                // 删除物理文件
                if (::cameraManager.isInitialized) {
                    cameraManager.deleteProjectMediaFile(mediaFile.filePath)
                }
                
                // 重新加载媒体文件列表
                loadProjectMediaFiles()
                
            } catch (e: Exception) {
                _error.value = "删除媒体文件失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 更新存储信息
     */
    private fun updateStorageInfo() {
        if (::cameraManager.isInitialized) {
            val info = cameraManager.getProjectMediaStorageInfo(currentProjectId)
            _storageInfo.value = info
        }
    }
    
    /**
     * 清除错误信息
     */
    fun clearError() {
        _error.value = null
    }
    
    /**
     * 刷新媒体文件列表
     */
    fun refreshMediaFiles() {
        loadProjectMediaFiles()
    }
    
    /**
     * 获取媒体文件总数
     */
    fun getMediaFileCount(): Int {
        return _mediaFiles.value.size
    }
    
    /**
     * 按类型获取媒体文件
     */
    fun getMediaFilesByType(type: com.example.shuigongrizhi.data.entity.MediaType): List<MediaFile> {
        return _mediaFiles.value.filter { it.fileType == type }
    }
    
    /**
     * 获取最新的媒体文件
     */
    fun getLatestMediaFiles(count: Int = 10): List<MediaFile> {
        return _mediaFiles.value.take(count)
    }
    
    /**
     * 搜索媒体文件
     */
    fun searchMediaFiles(query: String): List<MediaFile> {
        if (query.isBlank()) return _mediaFiles.value
        
        return _mediaFiles.value.filter { mediaFile ->
            mediaFile.fileName.contains(query, ignoreCase = true) ||
            mediaFile.description.contains(query, ignoreCase = true)
        }
    }
    
    /**
     * 按日期范围获取媒体文件
     */
    fun getMediaFilesByDateRange(
        startDate: java.util.Date,
        endDate: java.util.Date
    ): List<MediaFile> {
        return _mediaFiles.value.filter { mediaFile ->
            mediaFile.createdAt.after(startDate) && mediaFile.createdAt.before(endDate)
        }
    }
}