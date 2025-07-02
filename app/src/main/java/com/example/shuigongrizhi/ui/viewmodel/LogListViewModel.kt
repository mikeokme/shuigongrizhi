package com.example.shuigongrizhi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shuigongrizhi.data.entity.ConstructionLog
import com.example.shuigongrizhi.data.repository.ConstructionLogRepository
// import dagger.hilt.android.lifecycle.HiltViewModel // 临时禁用
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
// import javax.inject.Inject // 临时禁用

data class LogListState(
    val logs: List<ConstructionLog> = emptyList(),
    val filteredLogs: List<ConstructionLog> = emptyList()
)

// @HiltViewModel // 临时禁用
class LogListViewModel /* @Inject constructor(
    private val constructionLogRepository: ConstructionLogRepository
) */ : ViewModel() {
    
    // 临时直接实例化依赖
    private val constructionLogRepository = ConstructionLogRepository()
    
    private val _logListState = MutableStateFlow(LogListState())
    val logListState: StateFlow<LogListState> = _logListState.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    init {
        // 监听搜索查询变化，自动过滤日志
        viewModelScope.launch {
            combine(
                _logListState.map { it.logs },
                _searchQuery
            ) { logs, query ->
                if (query.isBlank()) {
                    logs
                } else {
                    logs.filter { log ->
                        log.mainContent.contains(query, ignoreCase = true) ||
                        log.constructionSite.contains(query, ignoreCase = true) ||
                        log.personnelEquipment.contains(query, ignoreCase = true) ||
                        log.qualityManagement.contains(query, ignoreCase = true) ||
                        log.safetyManagement.contains(query, ignoreCase = true) ||
                        log.weatherCondition.contains(query, ignoreCase = true)
                    }
                }
            }.collect { filteredLogs ->
                _logListState.value = _logListState.value.copy(
                    filteredLogs = filteredLogs
                )
            }
        }
    }
    
    /**
     * 加载指定项目的所有日志
     */
    fun loadLogs(projectId: Long) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                constructionLogRepository.getLogsByProjectId(projectId)
                    .collect { logs ->
                        val sortedLogs = logs.sortedByDescending { it.date }
                        _logListState.value = _logListState.value.copy(
                            logs = sortedLogs,
                            filteredLogs = if (_searchQuery.value.isBlank()) {
                                sortedLogs
                            } else {
                                filterLogs(sortedLogs, _searchQuery.value)
                            }
                        )
                    }
            } catch (e: Exception) {
                _error.value = "加载日志失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 更新搜索查询
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    /**
     * 清除错误状态
     */
    fun clearError() {
        _error.value = null
    }
    
    /**
     * 刷新日志列表
     */
    fun refreshLogs(projectId: Long) {
        loadLogs(projectId)
    }
    
    /**
     * 删除日志
     */
    fun deleteLog(logId: Long) {
        viewModelScope.launch {
            try {
                constructionLogRepository.deleteLogById(logId)
                // 删除成功后，从当前列表中移除该日志
                val currentLogs = _logListState.value.logs.filter { it.id != logId }
                _logListState.value = _logListState.value.copy(
                    logs = currentLogs,
                    filteredLogs = if (_searchQuery.value.isBlank()) {
                        currentLogs
                    } else {
                        filterLogs(currentLogs, _searchQuery.value)
                    }
                )
            } catch (e: Exception) {
                _error.value = "删除日志失败: ${e.message}"
            }
        }
    }
    
    /**
     * 获取日志统计信息
     */
    fun getLogStatistics(): LogStatistics {
        val logs = _logListState.value.logs
        val now = System.currentTimeMillis()
        val sevenDaysAgo = now - (7 * 24 * 60 * 60 * 1000)
        
        return LogStatistics(
            totalLogs = logs.size,
            recentLogs = logs.count { it.createdAt.time >= sevenDaysAgo },
            mediaFilesCount = logs.sumOf { it.mediaFiles.size },
            averageContentLength = if (logs.isNotEmpty()) {
                logs.map { it.mainContent.length }.average().toInt()
            } else 0
        )
    }
    
    /**
     * 按日期范围过滤日志
     */
    fun filterLogsByDateRange(startDate: Long, endDate: Long) {
        val allLogs = _logListState.value.logs
        val filteredByDate = allLogs.filter { log ->
            log.date.time in startDate..endDate
        }
        val finalFiltered = if (_searchQuery.value.isBlank()) {
            filteredByDate
        } else {
            filterLogs(filteredByDate, _searchQuery.value)
        }
        _logListState.value = _logListState.value.copy(
            filteredLogs = finalFiltered
        )
    }
    
    /**
     * 重置过滤器
     */
    fun resetFilters() {
        _searchQuery.value = ""
        _logListState.value = _logListState.value.copy(
            filteredLogs = _logListState.value.logs
        )
    }
    
    /**
     * 按天气条件过滤
     */
    fun filterByWeatherCondition(weatherCondition: String) {
        val allLogs = _logListState.value.logs
        val filteredByWeather = if (weatherCondition.isBlank()) {
            allLogs
        } else {
            allLogs.filter { it.weatherCondition == weatherCondition }
        }
        
        val finalFiltered = if (_searchQuery.value.isBlank()) {
            filteredByWeather
        } else {
            filterLogs(filteredByWeather, _searchQuery.value)
        }
        
        _logListState.value = _logListState.value.copy(
            filteredLogs = finalFiltered
        )
    }
    
    /**
     * 私有方法：过滤日志
     */
    private fun filterLogs(logs: List<ConstructionLog>, query: String): List<ConstructionLog> {
        return logs.filter { log ->
            log.mainContent.contains(query, ignoreCase = true) ||
            log.constructionSite.contains(query, ignoreCase = true) ||
            log.personnelEquipment.contains(query, ignoreCase = true) ||
            log.qualityManagement.contains(query, ignoreCase = true) ||
            log.safetyManagement.contains(query, ignoreCase = true) ||
            log.weatherCondition.contains(query, ignoreCase = true)
        }
    }
}

/**
 * 日志统计信息数据类
 */
data class LogStatistics(
    val totalLogs: Int,
    val recentLogs: Int,
    val mediaFilesCount: Int,
    val averageContentLength: Int
)