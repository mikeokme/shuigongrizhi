package com.example.shuigongrizhi.network

import retrofit2.http.GET
import retrofit2.http.Query

// OpenWeatherMap API接口
interface WeatherService {
    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "zh_cn"
    ): WeatherResponse
}

// OpenWeatherMap API响应数据模型
// 只保留常用字段，实际可根据需要扩展
data class WeatherResponse(
    val weather: List<WeatherDescription>,
    val main: MainInfo,
    val wind: WindInfo,
    val name: String,
    val sys: SysInfo,
    val coord: CoordInfo,
    val visibility: Int
)

data class WeatherDescription(
    val main: String,
    val description: String,
    val icon: String
)

data class MainInfo(
    val temp: Float,
    val humidity: Int,
    val pressure: Int,
    val feels_like: Float
)

data class WindInfo(
    val speed: Float,
    val deg: Int
)

data class SysInfo(
    val country: String
)

data class CoordInfo(
    val lon: Double,
    val lat: Double
)