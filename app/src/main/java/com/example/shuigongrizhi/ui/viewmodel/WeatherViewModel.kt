package com.example.shuigongrizhi.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shuigongrizhi.data.entity.WeatherCondition
import com.example.shuigongrizhi.data.repository.WeatherRepository
import com.example.shuigongrizhi.network.WeatherResponse
import com.example.shuigongrizhi.config.ApiConfig
import com.example.shuigongrizhi.utils.LocationUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class WeatherState {
    object Idle : WeatherState()
    object Loading : WeatherState()
    data class Success(val weather: WeatherResponse) : WeatherState()
    data class Error(val message: String) : WeatherState()
}

data class WeatherData(
    val isLoading: Boolean = false,
    val weatherCondition: String = "",
    val temperature: String = "",
    val humidity: String = "",
    val windSpeed: String = "",
    val windLevel: String = "",
    val windDirection: String = "",
    val pressure: String = "",
    val visibility: String = "",
    val cityName: String = "",
    val description: String = "",
    val feelsLike: String = "",
    val uvIndex: String = "",
    val sunrise: String = "",
    val sunset: String = "",
    val lastUpdated: String = "",
    val error: String? = null
)

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository
) : ViewModel() {
    
    private val _weatherState = MutableStateFlow<WeatherState>(WeatherState.Idle)
    val weatherState: StateFlow<WeatherState> = _weatherState.asStateFlow()
    
    private val _weatherData = MutableStateFlow(WeatherData())
    val weatherData: StateFlow<WeatherData> = _weatherData.asStateFlow()
    
    fun getCurrentWeather(lat: Double = 39.9042, lon: Double = 116.4074) {
        viewModelScope.launch {
            _weatherState.value = WeatherState.Loading
            
            weatherRepository.getCurrentWeather(lat, lon)
                .onSuccess { response ->
                    _weatherState.value = WeatherState.Success(response)
                    updateWeatherData(response)
                }
                .onFailure { exception ->
                    _weatherState.value = WeatherState.Error("获取天气信息失败: ${exception.message}")
                }
        }
    }
    
    fun getWeatherByCity(cityName: String) {
        viewModelScope.launch {
            _weatherState.value = WeatherState.Loading
            
            weatherRepository.getCurrentWeatherByCity(cityName)
                .onSuccess { response ->
                    _weatherState.value = WeatherState.Success(response)
                    updateWeatherData(response)
                }
                .onFailure { exception ->
                    _weatherState.value = WeatherState.Error("获取天气信息失败: ${exception.message}")
                }
        }
    }
    
    fun getCurrentWeatherAuto(context: Context) {
        viewModelScope.launch {
            _weatherState.value = WeatherState.Loading
            val loc = LocationUtils.getBestLocation(context)
            val (lat, lon) = loc ?: Pair(ApiConfig.DEFAULT_LATITUDE, ApiConfig.DEFAULT_LONGITUDE)
            weatherRepository.getCurrentWeather(lat, lon)
                .onSuccess { response ->
                    _weatherState.value = WeatherState.Success(response)
                    updateWeatherData(response)
                }
                .onFailure { exception ->
                    _weatherState.value = WeatherState.Error("获取天气信息失败: ${exception.message}")
                }
        }
    }
    
    private fun updateWeatherData(response: WeatherResponse) {
        val realtime = response.result.realtime
        
        // 根据彩云天气的skycon字段映射天气状况
        val condition = when (realtime.skycon.lowercase()) {
            "clear_day", "clear_night" -> WeatherCondition.SUNNY.displayName
            "partly_cloudy_day", "partly_cloudy_night" -> "多云"
            "cloudy" -> WeatherCondition.CLOUDY.displayName
            "light_rain" -> "小雨"
            "moderate_rain" -> "中雨"
            "heavy_rain" -> "大雨"
            "storm_rain" -> "暴雨"
            "light_snow" -> "小雪"
            "moderate_snow" -> "中雪"
            "heavy_snow" -> "大雪"
            "storm_snow" -> "暴雪"
            "fog" -> "雾"
            "dust" -> "浮尘"
            "sand" -> "沙尘"
            "wind" -> "大风"
            else -> WeatherCondition.CLOUDY.displayName
        }
        
        val windDirection = getWindDirection(realtime.wind.direction.toInt())
        val windLevel = getWindLevel(realtime.wind.speed)
        val currentTime = java.text.SimpleDateFormat(
            "yyyy-MM-dd HH:mm", 
            java.util.Locale.getDefault()
        ).format(java.util.Date())
        
        // 根据经纬度获取城市名（简化处理）
        val cityName = getCityNameFromLocation(response.location[1], response.location[0])
        
        _weatherData.value = WeatherData(
            isLoading = false,
            weatherCondition = condition,
            temperature = "${realtime.temperature.toInt()}°C",
            humidity = "${(realtime.humidity * 100).toInt()}%",
            windSpeed = "${realtime.wind.speed} m/s",
            windLevel = windLevel,
            windDirection = windDirection,
            pressure = "${realtime.pressure.toInt()} hPa",
            visibility = "${realtime.visibility.toInt()} km",
            cityName = cityName,
            description = getSkyconDescription(realtime.skycon),
            feelsLike = "${realtime.apparent_temperature.toInt()}°C",
            lastUpdated = currentTime,
            error = null
        )
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
    
    private fun getSkyconDescription(skycon: String): String {
        return when (skycon.lowercase()) {
            "clear_day" -> "晴天"
            "clear_night" -> "晴夜"
            "partly_cloudy_day" -> "白天多云"
            "partly_cloudy_night" -> "夜间多云"
            "cloudy" -> "阴天"
            "light_rain" -> "小雨"
            "moderate_rain" -> "中雨"
            "heavy_rain" -> "大雨"
            "storm_rain" -> "暴雨"
            "light_snow" -> "小雪"
            "moderate_snow" -> "中雪"
            "heavy_snow" -> "大雪"
            "storm_snow" -> "暴雪"
            "fog" -> "雾"
            "dust" -> "浮尘"
            "sand" -> "沙尘"
            "wind" -> "大风"
            else -> "未知"
        }
    }
    
    private fun getCityNameFromLocation(lat: Double, lon: Double): String {
        // 简化处理，根据经纬度返回城市名
        // 实际应用中可以使用地理编码API
        return when {
            lat > 39.8 && lat < 40.0 && lon > 116.3 && lon < 116.5 -> "北京"
            lat > 31.1 && lat < 31.3 && lon > 121.4 && lon < 121.6 -> "上海"
            lat > 22.4 && lat < 22.7 && lon > 113.9 && lon < 114.4 -> "深圳"
            lat > 23.0 && lat < 23.3 && lon > 113.1 && lon < 113.5 -> "广州"
            else -> "未知城市"
        }
    }

    private fun getWindLevel(speed: Double): String {
        return when {
            speed < 0.3 -> "0级"
            speed < 1.6 -> "1级"
            speed < 3.4 -> "2级"
            speed < 5.5 -> "3级"
            speed < 8.0 -> "4级"
            speed < 10.8 -> "5级"
            speed < 13.9 -> "6级"
            speed < 17.2 -> "7级"
            speed < 20.8 -> "8级"
            speed < 24.5 -> "9级"
            speed < 28.5 -> "10级"
            speed < 32.7 -> "11级"
            else -> "12级以上"
        }
    }
    
    fun clearError() {
        _weatherState.value = WeatherState.Idle
    }
    
    fun refreshWeather(context: Context) {
        getCurrentWeatherAuto(context)
    }
}