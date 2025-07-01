package com.example.shuigongrizhi.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.shuigongrizhi.data.entity.LocationRecord
import com.example.shuigongrizhi.data.dao.LocationRecordDao
import com.example.shuigongrizhi.utils.LocationUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

enum class MapProvider { GOOGLE, GAODE, BAIDU }

enum class LocationSource { 
    SYSTEM,     // 系统定位
    IP,         // IP定位
    DEFAULT     // 默认位置
}

data class LocationUiState(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val address: String? = null,
    val locationSource: LocationSource? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val history: List<LocationRecord> = emptyList(),
    val mapProvider: MapProvider = MapProvider.GOOGLE
)

class LocationViewModel(
    application: Application,
    private val locationRecordDao: LocationRecordDao
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(LocationUiState())
    val uiState: StateFlow<LocationUiState> = _uiState

    fun setMapProvider(provider: MapProvider) {
        _uiState.value = _uiState.value.copy(mapProvider = provider)
    }

    fun fetchCurrentLocation() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val context = getApplication<Application>()
                
                // 使用新的三级定位策略
                val locationInfo = LocationUtils.getLocationInfo(context)
                val (lat, lon) = locationInfo
                val address = locationInfo.address
                
                // 判断定位来源
                val locationSource = when {
                    LocationUtils.isDefaultLocation(lat, lon) -> LocationSource.DEFAULT
                    else -> {
                        // 这里可以根据实际情况判断是系统定位还是IP定位
                        // 由于 LocationUtils 内部已经处理了优先级，这里简化处理
                        LocationSource.SYSTEM
                    }
                }
                
                val provider = _uiState.value.mapProvider.name
                val record = LocationRecord(
                    latitude = lat,
                    longitude = lon,
                    address = address,
                    timestamp = Date(),
                    provider = provider
                )
                
                // 保存到数据库
                locationRecordDao.insert(record)
                
                _uiState.value = _uiState.value.copy(
                    latitude = lat,
                    longitude = lon,
                    address = address,
                    locationSource = locationSource,
                    isLoading = false,
                    error = null
                )
                
                // 加载历史记录
                loadHistory()
                
                android.util.Log.d("LocationViewModel", "位置获取成功: $address (来源: $locationSource)")
                
            } catch (e: Exception) {
                android.util.Log.e("LocationViewModel", "位置获取失败", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "位置获取失败: ${e.message}"
                )
            }
        }
    }

    fun loadHistory() {
        viewModelScope.launch {
            try {
                locationRecordDao.getAll().collect { list ->
                    _uiState.value = _uiState.value.copy(history = list)
                }
            } catch (e: Exception) {
                android.util.Log.e("LocationViewModel", "加载历史记录失败", e)
            }
        }
    }

    fun associateWithLog(logId: Long) {
        viewModelScope.launch {
            try {
                // 获取最新的位置记录并关联到日志
                val latestRecord = locationRecordDao.getLatestRecord()
                latestRecord?.let { record ->
                    val updatedRecord = record.copy(logId = logId)
                    locationRecordDao.update(updatedRecord)
                    android.util.Log.d("LocationViewModel", "位置记录已关联到日志: $logId")
                }
            } catch (e: Exception) {
                android.util.Log.e("LocationViewModel", "关联日志失败", e)
            }
        }
    }
    
    fun getLocationSourceText(source: LocationSource?): String {
        return when (source) {
            LocationSource.SYSTEM -> "系统定位"
            LocationSource.IP -> "IP定位"
            LocationSource.DEFAULT -> "默认位置"
            null -> "未知"
        }
    }
    
    fun getLocationSourceColor(source: LocationSource?): androidx.compose.ui.graphics.Color {
        return when (source) {
            LocationSource.SYSTEM -> androidx.compose.ui.graphics.Color(0xFF4CAF50) // 绿色
            LocationSource.IP -> androidx.compose.ui.graphics.Color(0xFFFF9800) // 橙色
            LocationSource.DEFAULT -> androidx.compose.ui.graphics.Color(0xFFF44336) // 红色
            null -> androidx.compose.ui.graphics.Color.Gray
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun refreshLocation() {
        fetchCurrentLocation()
    }
} 