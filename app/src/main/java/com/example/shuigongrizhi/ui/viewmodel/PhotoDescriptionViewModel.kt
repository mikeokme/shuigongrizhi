package com.example.shuigongrizhi.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shuigongrizhi.data.entity.MediaFile
import com.example.shuigongrizhi.data.entity.MediaType
import com.example.shuigongrizhi.data.repository.MediaFileRepository
import com.example.shuigongrizhi.utils.CameraManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class PhotoDescriptionState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false
)

@HiltViewModel
class PhotoDescriptionViewModel @Inject constructor(
    private val mediaFileRepository: MediaFileRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private lateinit var cameraManager: CameraManager
    
    private val _state = MutableStateFlow(PhotoDescriptionState())
    val state: StateFlow<PhotoDescriptionState> = _state.asStateFlow()
    
    private fun initializeCameraManager() {
        if (!::cameraManager.isInitialized) {
            cameraManager = CameraManager(context)
        }
    }
    
    /**
     * 保存照片到项目文件夹并添加说明
     */
    fun savePhotoWithDescription(
        photoUri: Uri,
        projectId: Long,
        description: String,
        location: String,
        notes: String
    ) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                
                initializeCameraManager()
                
                // 创建项目媒体目录
                cameraManager.createProjectMediaDirectories(projectId)
                
                // 生成文件名（按日期命名）
                val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                val timeFormat = SimpleDateFormat("HHmmss", Locale.getDefault())
                val currentDate = Date()
                val fileName = "IMG_${dateFormat.format(currentDate)}_${timeFormat.format(currentDate)}.jpg"
                
                // 移动文件到项目目录
                val savedFile = movePhotoToProjectDirectory(
                    photoUri, 
                    projectId, 
                    fileName
                )
                
                if (savedFile != null) {
                    // 创建MediaFile记录
                    val mediaFile = MediaFile(
                        logId = 0L, // 暂时设为0，后续关联到具体日志
                        filePath = savedFile.absolutePath,
                        fileName = fileName,
                        fileType = MediaType.PHOTO,
                        fileSize = savedFile.length(),
                        createdAt = currentDate,
                        description = buildPhotoDescription(description, location, notes),
                        projectId = projectId
                    )
                    
                    // 保存到数据库
                    mediaFileRepository.insertMediaFile(mediaFile)
                    
                    _state.value = _state.value.copy(
                        isLoading = false,
                        saveSuccess = true
                    )
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "保存照片失败"
                    )
                }
                
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "保存照片时发生错误: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 移动照片到项目目录
     */
    private suspend fun movePhotoToProjectDirectory(
        sourceUri: Uri,
        projectId: Long,
        fileName: String
    ): File? {
        return try {
            // 获取项目媒体目录
            val projectMediaDir = getProjectMediaDirectory(projectId)
            
            // 按日期创建子目录
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateDir = File(projectMediaDir, dateFormat.format(Date()))
            if (!dateDir.exists()) {
                dateDir.mkdirs()
            }
            
            // 目标文件
            val targetFile = File(dateDir, fileName)
            
            // 复制文件
            context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                targetFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            
            targetFile
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 获取项目媒体目录
     */
    private fun getProjectMediaDirectory(projectId: Long): File {
        val appDir = File(context.getExternalFilesDir(null), "ShuigongRizhi")
        val projectDir = File(appDir, "Project_$projectId")
        val mediaDir = File(projectDir, "media")
        
        if (!mediaDir.exists()) {
            mediaDir.mkdirs()
        }
        
        return mediaDir
    }
    
    /**
     * 构建照片描述信息
     */
    private fun buildPhotoDescription(
        description: String,
        location: String,
        notes: String
    ): String {
        val builder = StringBuilder()
        builder.append("📷 照片描述: $description")
        
        if (location.isNotBlank()) {
            builder.append("\n📍 拍摄位置: $location")
        }
        
        if (notes.isNotBlank()) {
            builder.append("\n📝 备注说明: $notes")
        }
        
        return builder.toString()
    }
    
    /**
     * 清除错误状态
     */
    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
    
    /**
     * 清除保存成功状态
     */
    fun clearSaveSuccess() {
        _state.value = _state.value.copy(saveSuccess = false)
    }
}