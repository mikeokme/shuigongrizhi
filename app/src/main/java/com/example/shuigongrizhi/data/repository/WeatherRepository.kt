package com.example.shuigongrizhi.data.repository

import com.example.shuigongrizhi.config.ApiConfig
import com.example.shuigongrizhi.network.NetworkModule
import com.example.shuigongrizhi.network.WeatherResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor() {
    private val weatherService = NetworkModule.weatherService
    
    /**
     * 获取当前天气信息（通过经纬度）
     */
    suspend fun getCurrentWeather(
        latitude: Double = ApiConfig.DEFAULT_LATITUDE,
        longitude: Double = ApiConfig.DEFAULT_LONGITUDE
    ): Result<WeatherResponse> = withContext(Dispatchers.IO) {
        try {
            val response = weatherService.getCurrentWeather(
                lat = latitude,
                lon = longitude,
                apiKey = ApiConfig.OPENWEATHER_API_KEY
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取当前天气信息（通过城市名）
     * 注意：OpenWeatherMap的免费API不直接支持城市名查询，
     * 这里使用默认坐标作为示例
     */
    suspend fun getCurrentWeatherByCity(
        cityName: String = ApiConfig.DEFAULT_CITY
    ): Result<WeatherResponse> = withContext(Dispatchers.IO) {
        try {
            // 这里使用默认坐标，实际应用中可以先通过地理编码API获取坐标
            val response = weatherService.getCurrentWeather(
                lat = ApiConfig.DEFAULT_LATITUDE,
                lon = ApiConfig.DEFAULT_LONGITUDE,
                apiKey = ApiConfig.OPENWEATHER_API_KEY
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}