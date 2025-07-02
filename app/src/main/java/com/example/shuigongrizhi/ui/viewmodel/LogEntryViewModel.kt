package com.example.shuigongrizhi.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import com.example.shuigongrizhi.data.entity.ConstructionLog
import com.example.shuigongrizhi.data.entity.MediaFile
import com.example.shuigongrizhi.data.entity.MediaType
import com.example.shuigongrizhi.data.entity.WeatherCondition
import com.example.shuigongrizhi.data.repository.ConstructionLogRepository
import com.example.shuigongrizhi.data.repository.MediaFileRepository
import com.example.shuigongrizhi.data.repository.ProjectRepository
import com.example.shuigongrizhi.network.NetworkModule
import com.example.shuigongrizhi.utils.ProjectDataManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Date

data class LogEntryState(
    val date: Date = Date(),
    val weatherCondition: String = "",
    val temperature: String = "",
    val wind: String = "",
    val constructionSite: String = "",
    val mainContent: String = "",
    val personnelEquipment: String = "",
    val qualityManagement: String = "",
    val safetyManagement: String = "",
    val mediaFiles: List<String> = emptyList(),
    val isLoadingWeather: Boolean = false
)

// @HiltViewModel // 临时禁用
class LogEntryViewModel /* @Inject constructor(
    private val constructionLogRepository: ConstructionLogRepository,
    private val projectRepository: ProjectRepository,
    private val weatherRepository: com.example.shuigongrizhi.data.repository.WeatherRepository,
    @ApplicationContext private val context: Context
) */ : ViewModel() {
    
    // 临时直接实例化依赖
    private val constructionLogRepository = ConstructionLogRepository()
    private val projectRepository = ProjectRepository()
    private val weatherRepository: com.example.shuigongrizhi.data.repository.WeatherRepository? = null
    private val context: Context? = null
    
    // private val projectDataManager = ProjectDataManager(context) // 临时禁用
    
    private val _logState = MutableStateFlow(LogEntryState())
    val logState: StateFlow<LogEntryState> = _logState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _saveResult = MutableStateFlow<Boolean?>(null)
    val saveResult: StateFlow<Boolean?> = _saveResult.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // 当前拍摄的照片URI
    var currentImageUri: Uri? = null
        private set

    private var projectId: Long = 0
    private var editingLogId: Long? = null
    
    // 新拍摄的媒体文件信息
    private val _newMediaFiles = MutableStateFlow<List<MediaFile>>(emptyList())
    val newMediaFiles: StateFlow<List<MediaFile>> = _newMediaFiles.asStateFlow()
    
    // 是否显示照片信息填写引导
    private val _showPhotoInfoGuide = MutableStateFlow(false)
    val showPhotoInfoGuide: StateFlow<Boolean> = _showPhotoInfoGuide.asStateFlow()

    fun initializeLog(projectId: Long, date: Date, logId: Long? = null) {
        this.projectId = projectId
        this.editingLogId = logId
        
        _logState.value = _logState.value.copy(date = date)
        
        if (logId != null) {
            loadExistingLog(logId)
        }
    }
    
    /**
     * 处理新拍摄的媒体文件
     * 自动引导用户填写照片信息
     */
    fun handleNewMediaFile(uri: Uri, mediaType: MediaType) {
        viewModelScope.launch {
            try {
                // 创建临时媒体文件记录
                val mediaFile = MediaFile(
                    logId = editingLogId ?: 0L, // 如果是新日志，logId暂时为0
                    filePath = uri.toString(),
                    fileName = "${if (mediaType == MediaType.PHOTO) "照片" else "视频"}_${System.currentTimeMillis()}",
                    fileType = mediaType,
                    fileSize = 0L, // 实际大小需要后续计算
                    createdAt = Date(),
                    description = ""
                )
                
                // 添加到新媒体文件列表
                val currentList = _newMediaFiles.value.toMutableList()
                currentList.add(mediaFile)
                _newMediaFiles.value = currentList
                
                // 自动在主要内容中添加照片信息注明
                val currentContent = _logState.value.mainContent
                val photoInfo = if (mediaType == MediaType.PHOTO) {
                    "\n\n📷 照片信息：\n- 拍摄时间：${java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(Date())}\n- 照片描述：[请填写照片内容描述]\n- 拍摄位置：[请填写拍摄位置]\n"
                } else {
                    "\n\n🎥 视频信息：\n- 录制时间：${java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(Date())}\n- 视频描述：[请填写视频内容描述]\n- 录制位置：[请填写录制位置]\n"
                }
                
                _logState.value = _logState.value.copy(
                    mainContent = currentContent + photoInfo
                )
                
                // 显示照片信息填写引导
                _showPhotoInfoGuide.value = true
                
            } catch (e: Exception) {
                _error.value = "处理媒体文件失败: ${e.message}"
            }
        }
    }
    
    /**
     * 关闭照片信息填写引导
     */
    fun dismissPhotoInfoGuide() {
        _showPhotoInfoGuide.value = false
    }
    
    /**
     * 自动滚动到照片信息注明位置
     */
    fun scrollToPhotoInfo(): Int {
        val content = _logState.value.mainContent
        val photoInfoIndex = content.lastIndexOf("📷 照片信息：")
        val videoInfoIndex = content.lastIndexOf("🎥 视频信息：")
        return maxOf(photoInfoIndex, videoInfoIndex)
    }

    private fun loadExistingLog(logId: Long) {
        viewModelScope.launch {
            try {
                val log = constructionLogRepository.getLogById(logId)
                log?.let {
                    _logState.value = LogEntryState(
                        date = it.date,
                        weatherCondition = it.weatherCondition,
                        temperature = it.temperature,
                        wind = it.wind,
                        constructionSite = it.constructionSite,
                        mainContent = it.mainContent,
                        personnelEquipment = it.personnelEquipment,
                        qualityManagement = it.qualityManagement,
                        safetyManagement = it.safetyManagement,
                        mediaFiles = it.mediaFiles
                    )
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun updateWeatherCondition(condition: String) {
        _logState.value = _logState.value.copy(weatherCondition = condition)
    }

    fun updateTemperature(temperature: String) {
        _logState.value = _logState.value.copy(temperature = temperature)
    }

    fun updateWind(wind: String) {
        _logState.value = _logState.value.copy(wind = wind)
    }

    fun updateConstructionSite(site: String) {
        _logState.value = _logState.value.copy(constructionSite = site)
    }

    fun updateMainContent(content: String) {
        _logState.value = _logState.value.copy(mainContent = content)
    }

    fun updatePersonnelEquipment(value: String) {
        _logState.value = _logState.value.copy(personnelEquipment = value)
    }

    fun updateQualityManagement(value: String) {
        _logState.value = _logState.value.copy(qualityManagement = value)
    }

    fun updateSafetyManagement(value: String) {
        _logState.value = _logState.value.copy(safetyManagement = value)
    }

    fun updateMediaFiles(files: List<String>) {
        _logState.value = _logState.value.copy(mediaFiles = files)
    }

    fun getCurrentWeather() {
        fetchWeatherData()
    }

    /**
     * 创建临时图片文件用于拍照
     */
    fun createTempImageFile(): Uri? {
        return try {
            val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val file = File.createTempFile(
                "IMG_${System.currentTimeMillis()}_", /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
            )
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            currentImageUri = uri
            uri
        } catch (e: Exception) {
            _error.value = "创建临时图片文件失败: ${e.message}"
            null
        }
    }

    fun createTempVideoFile(context: Context): Uri? {
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
        val file = File.createTempFile(
            "VID_${System.currentTimeMillis()}_", /* prefix */
            ".mp4", /* suffix */
            storageDir /* directory */
        )
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    fun fetchWeatherData(lat: Double = 39.9042, lon: Double = 116.4074) {
        viewModelScope.launch {
            _logState.value = _logState.value.copy(isLoadingWeather = true)
            try {
                val result = weatherRepository.getCurrentWeather(
                    latitude = lat,
                    longitude = lon
                )
                
                if (result.isSuccess) {
                    val response = result.getOrThrow()
                    val realtime = response.result.realtime
                
                    // 根据彩云天气的skycon字段映射天气状况
                    val weatherCondition = when (realtime.skycon) {
                        "CLEAR_DAY" -> "晴天"
                        "CLEAR_NIGHT" -> "晴夜"
                        "PARTLY_CLOUDY_DAY" -> "多云"
                        "PARTLY_CLOUDY_NIGHT" -> "多云"
                        "CLOUDY" -> "阴天"
                        "LIGHT_HAZE" -> "轻雾"
                        "MODERATE_HAZE" -> "中雾"
                        "HEAVY_HAZE" -> "重雾"
                        "LIGHT_RAIN" -> "小雨"
                        "MODERATE_RAIN" -> "中雨"
                        "HEAVY_RAIN" -> "大雨"
                        "STORM_RAIN" -> "暴雨"
                        "LIGHT_SNOW" -> "小雪"
                        "MODERATE_SNOW" -> "中雪"
                        "HEAVY_SNOW" -> "大雪"
                        "STORM_SNOW" -> "暴雪"
                        "DUST" -> "浮尘"
                        "SAND" -> "沙尘"
                        "WIND" -> "大风"
                        else -> "未知"
                    }
                    
                    _logState.value = _logState.value.copy(
                        weatherCondition = weatherCondition,
                        temperature = "${realtime.temperature.toInt()}°C",
                        wind = "${realtime.wind.speed} m/s"
                    )
                    _error.value = null
                } else {
                     // 处理API调用失败的情况
                     val error = result.exceptionOrNull()
                     android.util.Log.e("LogEntryViewModel", "获取天气信息失败: ${error?.message}", error)
                     _logState.value = _logState.value.copy(
                         isLoadingWeather = false
                     )
                 }
             } catch (e: Exception) {
                 android.util.Log.e("LogEntryViewModel", "获取天气信息失败", e)
                 _logState.value = _logState.value.copy(
                     isLoadingWeather = false
                 )
             }
        }
    }

    fun saveLog() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val state = _logState.value
                // 一项目一日志一日唯一性校验
                val existing = constructionLogRepository.getLogByProjectAndDate(projectId, state.date)
                if (editingLogId == null && existing != null) {
                    _error.value = "该项目当天日志已存在，自动切换为编辑模式。"
                    editingLogId = existing.id
                    loadExistingLog(existing.id)
                    _isLoading.value = false
                    return@launch
                }
                val log = ConstructionLog(
                    id = editingLogId ?: 0,
                    projectId = projectId,
                    date = state.date,
                    weatherCondition = state.weatherCondition,
                    temperature = state.temperature,
                    wind = state.wind,
                    constructionSite = state.constructionSite,
                    mainContent = state.mainContent,
                    personnelEquipment = state.personnelEquipment,
                    qualityManagement = state.qualityManagement,
                    safetyManagement = state.safetyManagement,
                    mediaFiles = state.mediaFiles,
                    updatedAt = Date()
                )
                val result = if (editingLogId != null) {
                    constructionLogRepository.updateLog(log)
                    android.util.Log.d("LogEntry", "Log updated successfully for project $projectId")
                    "updated"
                } else {
                    val logId = constructionLogRepository.insertLog(log)
                    android.util.Log.d("LogEntry", "Log inserted with ID: $logId for project $projectId")
                    logId.toString()
                }
                
                // 验证保存是否成功
                kotlinx.coroutines.delay(100) // 给数据库操作一些时间
                android.util.Log.d("LogEntry", "Save operation completed: $result")
                
                // 自动备份项目数据（包含新保存的日志）
                try {
                    val projectResult = projectRepository.getProjectById(projectId)
                    val project = (projectResult as? com.example.shuigongrizhi.core.Result.Success)?.data
                    project?.let { proj ->
                        // 获取项目的所有日志（包括刚保存的）
                        val logs = constructionLogRepository.getLogsByProjectId(projectId).first()
                        
                        // 执行自动备份
                        // val backupSuccess = projectDataManager?.autoBackupProject(proj, logs)
                        // if (backupSuccess == true) {
                        //     android.util.Log.d("LogEntry", "项目数据自动备份成功")
                        // } else {
                        //     android.util.Log.w("LogEntry", "项目数据自动备份失败")
                        // }
                        android.util.Log.d("LogEntry", "自动备份功能暂时禁用")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("LogEntry", "自动备份过程中发生错误", e)
                }
                
                _saveResult.value = true
            } catch (e: Exception) {
                _error.value = e.message
                _saveResult.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearSaveResult() {
        _saveResult.value = null
    }

    fun clearError() {
        _error.value = null
    }
}