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
        val condition = when (response.weather.firstOrNull()?.main?.lowercase()) {
            "clear" -> WeatherCondition.SUNNY.displayName
            "clouds" -> WeatherCondition.CLOUDY.displayName
            "rain" -> WeatherCondition.RAINY.displayName
            "snow" -> WeatherCondition.SNOWY.displayName
            "thunderstorm" -> "雷雨"
            "drizzle" -> "小雨"
            "mist", "fog" -> "雾"
            else -> WeatherCondition.CLOUDY.displayName
        }
        
        val windDirection = getWindDirection(response.wind.deg)
        val currentTime = java.text.SimpleDateFormat(
            "yyyy-MM-dd HH:mm", 
            java.util.Locale.getDefault()
        ).format(java.util.Date())
        
        _weatherData.value = WeatherData(
            isLoading = false,
            weatherCondition = condition,
            temperature = "${response.main.temp.toInt()}°C",
            humidity = "${response.main.humidity}%",
            windSpeed = "${response.wind.speed} m/s",
            windDirection = windDirection,
            pressure = "${response.main.pressure} hPa",
            visibility = "${response.visibility / 1000} km",
            cityName = response.name,
            description = response.weather.firstOrNull()?.description ?: "",
            feelsLike = "${response.main.feels_like.toInt()}°C",
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
    
    fun clearError() {
        _weatherState.value = WeatherState.Idle
    }
    
    fun refreshWeather() {
        getCurrentWeather()
    }
}