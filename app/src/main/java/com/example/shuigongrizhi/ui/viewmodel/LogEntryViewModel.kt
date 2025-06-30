package com.example.shuigongrizhi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.example.shuigongrizhi.data.entity.ConstructionLog
import com.example.shuigongrizhi.data.entity.MediaFile
import com.example.shuigongrizhi.data.entity.MediaType
import com.example.shuigongrizhi.data.entity.WeatherCondition
import com.example.shuigongrizhi.data.repository.ConstructionLogRepository
import com.example.shuigongrizhi.data.repository.MediaFileRepository
import com.example.shuigongrizhi.network.NetworkModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

data class LogEntryState(
    val date: Date = Date(),
    val weatherCondition: String = "",
    val temperature: String = "",
    val wind: String = "",
    val windInfo: String = "",
    val constructionLocation: String = "",
    val constructionSite: String = "",
    val mainWorkContent: String = "",
    val constructionPersonnel: String = "",
    val machineryUsed: String = "",
    val machinery: String = "",
    val safetyNotes: String = "",
    val mediaFiles: List<MediaFile> = emptyList(),
    val isLoadingWeather: Boolean = false
)

@HiltViewModel
class LogEntryViewModel @Inject constructor(
    private val constructionLogRepository: ConstructionLogRepository,
    private val mediaFileRepository: MediaFileRepository
) : ViewModel() {
    
    private val _logState = MutableStateFlow(LogEntryState())
    val logState: StateFlow<LogEntryState> = _logState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _saveResult = MutableStateFlow<Boolean?>(null)
    val saveResult: StateFlow<Boolean?> = _saveResult.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var projectId: Long = 0
    private var editingLogId: Long? = null

    fun initializeLog(projectId: Long, date: Date, logId: Long? = null) {
        this.projectId = projectId
        this.editingLogId = logId
        
        _logState.value = _logState.value.copy(date = date)
        
        if (logId != null) {
            loadExistingLog(logId)
        }
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
                        constructionLocation = it.constructionLocation,
                        mainWorkContent = it.mainWorkContent,
                        constructionPersonnel = it.constructionPersonnel,
                        machineryUsed = it.machineryUsed,
                        safetyNotes = it.safetyNotes
                    )
                    
                    // 加载媒体文件
                    mediaFileRepository.getMediaFilesByLogId(logId).collect { mediaFiles ->
                        _logState.value = _logState.value.copy(mediaFiles = mediaFiles)
                    }
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

    fun updateWindInfo(windInfo: String) {
        _logState.value = _logState.value.copy(windInfo = windInfo)
    }

    fun updateConstructionSite(site: String) {
        _logState.value = _logState.value.copy(constructionSite = site)
    }

    fun updateMachinery(machinery: String) {
        _logState.value = _logState.value.copy(machinery = machinery)
    }

    fun getCurrentWeather() {
        fetchWeatherData()
    }

    fun createTempImageFile(): android.net.Uri? {
        // This should be implemented to create a temporary file for camera capture
        // For now, returning null as a placeholder
        return null
    }

    fun updateConstructionLocation(location: String) {
        _logState.value = _logState.value.copy(constructionLocation = location)
    }

    fun updateMainWorkContent(content: String) {
        _logState.value = _logState.value.copy(mainWorkContent = content)
    }

    fun updateConstructionPersonnel(personnel: String) {
        _logState.value = _logState.value.copy(constructionPersonnel = personnel)
    }

    fun updateMachineryUsed(machinery: String) {
        _logState.value = _logState.value.copy(machineryUsed = machinery)
    }

    fun updateSafetyNotes(notes: String) {
        _logState.value = _logState.value.copy(safetyNotes = notes)
    }

    fun fetchWeatherData(location: String = "北京") {
        viewModelScope.launch {
            _logState.value = _logState.value.copy(isLoadingWeather = true)
            try {
                // 注意：这里需要实际的API密钥
                val response = NetworkModule.weatherService.getCurrentWeather(
                    location = location,
                    apiKey = "YOUR_API_KEY_HERE" // 需要替换为实际的API密钥
                )
                
                if (response.isSuccessful) {
                    response.body()?.let { weather ->
                        val condition = when (weather.weather.firstOrNull()?.main?.lowercase()) {
                            "clear" -> WeatherCondition.SUNNY.displayName
                            "clouds" -> WeatherCondition.CLOUDY.displayName
                            "rain" -> WeatherCondition.RAINY.displayName
                            "snow" -> WeatherCondition.SNOWY.displayName
                            else -> WeatherCondition.CLOUDY.displayName
                        }
                        
                        val temp = "${weather.main.temp_min.toInt()} ~ ${weather.main.temp_max.toInt()} °C"
                        val windDirection = getWindDirection(weather.wind.deg)
                        val windSpeed = "${(weather.wind.speed * 3.6).toInt()}级/$windDirection"
                        
                        _logState.value = _logState.value.copy(
                            weatherCondition = condition,
                            temperature = temp,
                            wind = windSpeed
                        )
                    }
                } else {
                    _error.value = "获取天气信息失败"
                }
            } catch (e: Exception) {
                _error.value = "网络连接失败：${e.message}"
            } finally {
                _logState.value = _logState.value.copy(isLoadingWeather = false)
            }
        }
    }

    private fun getWindDirection(degrees: Int): String {
        return when (degrees) {
            in 0..22, in 338..360 -> "北风"
            in 23..67 -> "东北风"
            in 68..112 -> "东风"
            in 113..157 -> "东南风"
            in 158..202 -> "南风"
            in 203..247 -> "西南风"
            in 248..292 -> "西风"
            in 293..337 -> "西北风"
            else -> "无风"
        }
    }

    fun addMediaFile(filePath: String, fileName: String, mediaType: MediaType) {
        viewModelScope.launch {
            try {
                if (editingLogId != null) {
                    val mediaFile = MediaFile(
                        logId = editingLogId!!,
                        filePath = filePath,
                        fileName = fileName,
                        fileType = mediaType
                    )
                    mediaFileRepository.insertMediaFile(mediaFile)
                } else {
                    // 如果还没有保存日志，先临时存储
                    val tempMediaFile = MediaFile(
                        logId = 0, // 临时ID
                        filePath = filePath,
                        fileName = fileName,
                        fileType = mediaType
                    )
                    val currentFiles = _logState.value.mediaFiles.toMutableList()
                    currentFiles.add(tempMediaFile)
                    _logState.value = _logState.value.copy(mediaFiles = currentFiles)
                }
            } catch (e: Exception) {
                _error.value = "添加媒体文件失败：${e.message}"
            }
        }
    }

    fun removeMediaFile(mediaFile: MediaFile) {
        viewModelScope.launch {
            try {
                if (mediaFile.id > 0) {
                    mediaFileRepository.deleteMediaFile(mediaFile)
                } else {
                    // 移除临时文件
                    val currentFiles = _logState.value.mediaFiles.toMutableList()
                    currentFiles.remove(mediaFile)
                    _logState.value = _logState.value.copy(mediaFiles = currentFiles)
                }
            } catch (e: Exception) {
                _error.value = "删除媒体文件失败：${e.message}"
            }
        }
    }

    fun saveLog() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val state = _logState.value
                val log = if (editingLogId != null) {
                    ConstructionLog(
                        id = editingLogId!!,
                        projectId = projectId,
                        date = state.date,
                        weatherCondition = state.weatherCondition,
                        temperature = state.temperature,
                        wind = state.wind,
                        constructionLocation = state.constructionLocation,
                        mainWorkContent = state.mainWorkContent,
                        constructionPersonnel = state.constructionPersonnel,
                        machineryUsed = state.machineryUsed,
                        safetyNotes = state.safetyNotes,
                        updatedAt = Date()
                    )
                } else {
                    ConstructionLog(
                        projectId = projectId,
                        date = state.date,
                        weatherCondition = state.weatherCondition,
                        temperature = state.temperature,
                        wind = state.wind,
                        constructionLocation = state.constructionLocation,
                        mainWorkContent = state.mainWorkContent,
                        constructionPersonnel = state.constructionPersonnel,
                        machineryUsed = state.machineryUsed,
                        safetyNotes = state.safetyNotes
                    )
                }

                val logId = if (editingLogId != null) {
                    constructionLogRepository.updateLog(log)
                    editingLogId!!
                } else {
                    val newLogId = constructionLogRepository.insertLog(log)
                    editingLogId = newLogId
                    
                    // 保存临时媒体文件
                    state.mediaFiles.forEach { mediaFile ->
                        if (mediaFile.id == 0L) {
                            val newMediaFile = mediaFile.copy(logId = newLogId)
                            mediaFileRepository.insertMediaFile(newMediaFile)
                        }
                    }
                    
                    newLogId
                }
                
                _saveResult.value = true
            } catch (e: Exception) {
                _error.value = "保存失败：${e.message}"
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