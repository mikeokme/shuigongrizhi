package com.example.shuigongrizhi.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shuigongrizhi.data.entity.MediaFile
import com.example.shuigongrizhi.data.entity.MediaType
import com.example.shuigongrizhi.data.repository.MediaFileRepository
import com.example.shuigongrizhi.utils.CameraManager
// import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
// import javax.inject.Inject

// @HiltViewModel
class CameraViewModel /* @Inject constructor(
    private val mediaFileRepository: MediaFileRepository
) */ : ViewModel() {
    private val mediaFileRepository: MediaFileRepository? = null
    
    private lateinit var cameraManager: CameraManager
    private var currentProjectId: Long = 0
    
    // 当前拍摄的文件URI
    var currentImageUri: Uri? = null
        private set
    var currentVideoUri: Uri? = null
        private set
    
    // 存储信息状态
    private val _storageInfo = MutableStateFlow<Map<String, Any>>(emptyMap())
    val storageInfo: StateFlow<Map<String, Any>> = _storageInfo.asStateFlow()
    
    // 错误状态
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    /**
     * 初始化项目媒体管理
     */
    fun initializeProjectMedia(projectId: Long) {
        currentProjectId = projectId
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // 创建项目媒体目录
                val success = cameraManager.createProjectMediaDirectories(projectId)
                if (!success) {
                    _error.value = "创建项目媒体目录失败"
                    return@launch
                }
                
                // 更新存储信息
                updateStorageInfo()
                
            } catch (e: Exception) {
                _error.value = "初始化项目媒体失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 初始化相机管理器
     */
    fun initializeCameraManager(context: Context) {
        cameraManager = CameraManager(context)
    }
    
    /**
     * 创建图片文件
     */
    fun createImageFile(context: Context, projectId: Long): Uri? {
        if (!::cameraManager.isInitialized) {
            initializeCameraManager(context)
        }
        
        return try {
            val uri = cameraManager.createProjectImageFile(projectId)
            currentImageUri = uri
            uri
        } catch (e: Exception) {
            _error.value = "创建图片文件失败: ${e.message}"
            null
        }
    }
    
    /**
     * 创建视频文件
     */
    fun createVideoFile(context: Context, projectId: Long): Uri? {
        if (!::cameraManager.isInitialized) {
            initializeCameraManager(context)
        }
        
        return try {
            val uri = cameraManager.createProjectVideoFile(projectId)
            currentVideoUri = uri
            uri
        } catch (e: Exception) {
            _error.value = "创建视频文件失败: ${e.message}"
            null
        }
    }
    
    /**
     * 保存媒体文件到数据库
     */
    fun saveMediaFile(
        uri: Uri,
        mediaType: MediaType,
        logId: Long,
        description: String = ""
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                if (!::cameraManager.isInitialized) {
                    _error.value = "相机管理器未初始化"
                    return@launch
                }
                
                // 移动文件到项目目录并创建MediaFile对象
                val mediaFile = cameraManager.moveToProjectMediaDir(
                    uri, currentProjectId, mediaType, description
                )
                
                if (mediaFile != null) {
                    // 设置正确的logId并保存到数据库
                    val finalMediaFile = mediaFile.copy(logId = logId)
                    mediaFileRepository.insertMediaFile(finalMediaFile)
                    
                    // 更新存储信息
                    updateStorageInfo()
                    
                    android.util.Log.d("CameraViewModel", "媒体文件保存成功: ${finalMediaFile.fileName}")
                } else {
                    _error.value = "移动媒体文件失败"
                }
                
            } catch (e: Exception) {
                _error.value = "保存媒体文件失败: ${e.message}"
                android.util.Log.e("CameraViewModel", "保存媒体文件失败", e)
            } finally {
                _isLoading.value = false
            }
        }
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
                
                // 更新存储信息
                updateStorageInfo()
                
                android.util.Log.d("CameraViewModel", "媒体文件删除成功: ${mediaFile.fileName}")
                
            } catch (e: Exception) {
                _error.value = "删除媒体文件失败: ${e.message}"
                android.util.Log.e("CameraViewModel", "删除媒体文件失败", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 获取项目媒体文件列表
     */
    fun getProjectMediaFiles(logId: Long) = mediaFileRepository.getMediaFilesByLogId(logId)
    
    /**
     * 更新存储信息
     */
    private fun updateStorageInfo() {
        if (::cameraManager.isInitialized && currentProjectId > 0) {
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
     * 获取项目媒体目录路径
     */
    fun getProjectMediaPath(): String {
        return if (::cameraManager.isInitialized) {
            val info = cameraManager.getProjectMediaStorageInfo(currentProjectId)
            info["mediaDir"] as? String ?: ""
        } else {
            ""
        }
    }
}