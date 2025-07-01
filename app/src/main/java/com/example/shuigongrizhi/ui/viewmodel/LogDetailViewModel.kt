package com.example.shuigongrizhi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shuigongrizhi.data.entity.ConstructionLog
import com.example.shuigongrizhi.data.repository.ConstructionLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

data class LogDetailState(
    val log: ConstructionLog? = null
)

@HiltViewModel
class LogDetailViewModel @Inject constructor(
    private val constructionLogRepository: ConstructionLogRepository
) : ViewModel() {
    
    private val _logDetailState = MutableStateFlow(LogDetailState())
    val logDetailState: StateFlow<LogDetailState> = _logDetailState.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    /**
     * 加载日志详情
     */
    fun loadLogDetail(logId: Long) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val log = constructionLogRepository.getLogById(logId)
                if (log != null) {
                    _logDetailState.value = LogDetailState(log = log)
                } else {
                    _error.value = "未找到指定的日志记录"
                }
            } catch (e: Exception) {
                _error.value = "加载日志详情失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 刷新日志详情
     */
    fun refreshLogDetail(logId: Long) {
        loadLogDetail(logId)
    }
    
    /**
     * 清除错误状态
     */
    fun clearError() {
        _error.value = null
    }
    
    /**
     * 删除当前日志
     */
    fun deleteCurrentLog(): Boolean {
        val currentLog = _logDetailState.value.log
        return if (currentLog != null) {
            viewModelScope.launch {
                try {
                    constructionLogRepository.deleteLogById(currentLog.id)
                    _logDetailState.value = LogDetailState(log = null)
                } catch (e: Exception) {
                    _error.value = "删除日志失败: ${e.message}"
                }
            }
            true
        } else {
            false
        }
    }
    
    /**
     * 获取日志的媒体文件统计
     */
    fun getMediaStatistics(): MediaStatistics? {
        val log = _logDetailState.value.log ?: return null
        
        val mediaFiles = log.mediaFiles
        val imageCount = mediaFiles.count { it.endsWith(".jpg", true) || it.endsWith(".jpeg", true) || it.endsWith(".png", true) }
        val videoCount = mediaFiles.count { it.endsWith(".mp4", true) || it.endsWith(".avi", true) || it.endsWith(".mov", true) }
        
        return MediaStatistics(
            totalFiles = mediaFiles.size,
            imageCount = imageCount,
            videoCount = videoCount
        )
    }
    
    /**
     * 导出日志为文本格式
     */
    fun exportLogAsText(): String? {
        val log = _logDetailState.value.log ?: return null
        
        return buildString {
            appendLine("施工日志详情")
            appendLine("====================")
            appendLine()
            
            appendLine("日期: ${java.text.SimpleDateFormat("yyyy年MM月dd日", java.util.Locale.getDefault()).format(log.date)}")
            
            if (log.weatherCondition.isNotEmpty()) {
                appendLine("天气: ${log.weatherCondition}")
            }
            
            if (log.temperature.isNotEmpty()) {
                appendLine("温度: ${log.temperature}")
            }
            
            if (log.wind.isNotEmpty()) {
                appendLine("风力: ${log.wind}")
            }
            
            if (log.constructionSite.isNotEmpty()) {
                appendLine()
                appendLine("施工地点:")
                appendLine(log.constructionSite)
            }
            
            if (log.mainContent.isNotEmpty()) {
                appendLine()
                appendLine("主要施工内容:")
                appendLine(log.mainContent)
            }
            
            if (log.personnelEquipment.isNotEmpty()) {
                appendLine()
                appendLine("人员设备情况:")
                appendLine(log.personnelEquipment)
            }
            
            if (log.qualityManagement.isNotEmpty()) {
                appendLine()
                appendLine("质量管理:")
                appendLine(log.qualityManagement)
            }
            
            if (log.safetyManagement.isNotEmpty()) {
                appendLine()
                appendLine("安全管理:")
                appendLine(log.safetyManagement)
            }
            
            if (log.mediaFiles.isNotEmpty()) {
                appendLine()
                appendLine("媒体文件 (${log.mediaFiles.size}个):")
                log.mediaFiles.forEach { mediaFile ->
                    appendLine("- $mediaFile")
                }
            }
            
            appendLine()
            appendLine("创建时间: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(log.createdAt)}")
            
            if (log.updatedAt != log.createdAt) {
                appendLine("更新时间: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(log.updatedAt)}")
            }
        }
    }
    
    /**
     * 获取日志内容摘要
     */
    fun getLogSummary(): LogSummary? {
        val log = _logDetailState.value.log ?: return null
        
        return LogSummary(
            date = log.date.time,
            site = log.constructionSite,
            contentPreview = if (log.mainContent.length > 100) {
                log.mainContent.take(100) + "..."
            } else {
                log.mainContent
            },
            mediaCount = log.mediaFiles.size,
            hasWeatherInfo = log.weatherCondition.isNotEmpty() || log.temperature.isNotEmpty() || log.wind.isNotEmpty(),
            completeness = calculateCompleteness(log)
        )
    }
    
    /**
     * 计算日志完整度
     */
    private fun calculateCompleteness(log: ConstructionLog): Float {
        var totalFields = 0
        var filledFields = 0
        
        // 基本字段
        totalFields += 6
        if (log.constructionSite.isNotEmpty()) filledFields++
        if (log.mainContent.isNotEmpty()) filledFields++
        if (log.personnelEquipment.isNotEmpty()) filledFields++
        if (log.qualityManagement.isNotEmpty()) filledFields++
        if (log.safetyManagement.isNotEmpty()) filledFields++
        if (log.mediaFiles.isNotEmpty()) filledFields++
        
        // 天气字段
        totalFields += 3
        if (log.weatherCondition.isNotEmpty()) filledFields++
        if (log.temperature.isNotEmpty()) filledFields++
        if (log.wind.isNotEmpty()) filledFields++
        
        return if (totalFields > 0) filledFields.toFloat() / totalFields else 0f
    }
}

/**
 * 媒体文件统计数据类
 */
data class MediaStatistics(
    val totalFiles: Int,
    val imageCount: Int,
    val videoCount: Int
)

/**
 * 日志摘要数据类
 */
data class LogSummary(
    val date: Long,
    val site: String,
    val contentPreview: String,
    val mediaCount: Int,
    val hasWeatherInfo: Boolean,
    val completeness: Float
)