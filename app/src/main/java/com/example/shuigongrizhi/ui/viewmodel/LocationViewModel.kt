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

data class LocationUiState(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val address: String? = null,
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
            val context = getApplication<Application>()
            val loc = LocationUtils.getBestLocation(context)
            if (loc != null) {
                val (lat, lon) = loc
                val address = LocationUtils.getAddressFromLatLng(context, lat, lon)
                val provider = _uiState.value.mapProvider.name
                val record = LocationRecord(
                    latitude = lat,
                    longitude = lon,
                    address = address,
                    timestamp = Date(),
                    provider = provider
                )
                locationRecordDao.insert(record)
                _uiState.value = _uiState.value.copy(
                    latitude = lat,
                    longitude = lon,
                    address = address,
                    isLoading = false
                )
                loadHistory()
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "无法获取当前位置"
                )
            }
        }
    }

    fun loadHistory() {
        viewModelScope.launch {
            locationRecordDao.getAll().collect { list ->
                _uiState.value = _uiState.value.copy(history = list)
            }
        }
    }

    fun associateWithLog(logId: Long) {
        // 可实现：将最新位置记录的 logId 字段更新为 logId
    }
} 