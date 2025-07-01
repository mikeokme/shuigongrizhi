package com.example.shuigongrizhi.network

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// 彩云天气 API接口
interface WeatherService {
    @GET("{token}/{longitude},{latitude}/weather.json")
    suspend fun getCurrentWeather(
        @Path("token") token: String,
        @Path("longitude") longitude: Double,
        @Path("latitude") latitude: Double,
        @Query("lang") lang: String = "zh_CN",
        @Query("unit") unit: String = "metric",
        @Query("granu") granu: String = "realtime"
    ): CaiyunWeatherResponse
}

// 彩云天气 API响应数据模型
data class CaiyunWeatherResponse(
    val status: String,
    val api_version: String,
    val api_status: String,
    val lang: String,
    val unit: String,
    val tzshift: Int,
    val timezone: String,
    val server_time: Long,
    val location: List<Double>,
    val result: WeatherResult
)

data class WeatherResult(
    val realtime: RealtimeWeather,
    val minutely: MinutelyWeather? = null,
    val hourly: HourlyWeather? = null,
    val daily: DailyWeather? = null
)

// 实时天气数据
data class RealtimeWeather(
    val status: String,
    val temperature: Double,
    val humidity: Double,
    val cloudrate: Double,
    val skycon: String,
    val visibility: Double,
    val dswrf: Double,
    val wind: WindData,
    val pressure: Double,
    val apparent_temperature: Double
)

data class WindData(
    val speed: Double,
    val direction: Double
)

// 分钟级降水预报
data class MinutelyWeather(
    val status: String,
    val description: String,
    val probability: List<Double>,
    val datasource: String,
    val precipitation_2h: List<Double>,
    val precipitation: List<Double>
)

// 小时级预报
data class HourlyWeather(
    val status: String,
    val description: String,
    val precipitation: List<HourlyData>,
    val temperature: List<HourlyData>,
    val wind: List<HourlyWindData>,
    val humidity: List<HourlyData>,
    val cloudrate: List<HourlyData>,
    val skycon: List<HourlySkycon>,
    val pressure: List<HourlyData>,
    val visibility: List<HourlyData>
)

data class HourlyData(
    val datetime: String,
    val value: Double
)

data class HourlyWindData(
    val datetime: String,
    val speed: Double,
    val direction: Double
)

data class HourlySkycon(
    val datetime: String,
    val value: String
)

// 天级预报
data class DailyWeather(
    val status: String,
    val astro: List<AstroData>,
    val precipitation_08h_20h: List<DailyData>,
    val precipitation_20h_32h: List<DailyData>,
    val precipitation: List<DailyData>,
    val temperature: List<TemperatureData>,
    val temperature_08h_20h: List<TemperatureData>,
    val temperature_20h_32h: List<TemperatureData>,
    val wind: List<DailyWindData>,
    val wind_08h_20h: List<DailyWindData>,
    val wind_20h_32h: List<DailyWindData>,
    val humidity: List<DailyData>,
    val cloudrate: List<DailyData>,
    val pressure: List<DailyData>,
    val visibility: List<DailyData>,
    val dswrf: List<DailyData>,
    val skycon: List<DailySkycon>,
    val skycon_08h_20h: List<DailySkycon>,
    val skycon_20h_32h: List<DailySkycon>,
    val life_index: LifeIndex
)

data class AstroData(
    val date: String,
    val sunrise: SunTime,
    val sunset: SunTime
)

data class SunTime(
    val time: String
)

data class DailyData(
    val date: String,
    val max: Double,
    val min: Double,
    val avg: Double
)

data class TemperatureData(
    val date: String,
    val max: Double,
    val min: Double,
    val avg: Double
)

data class DailyWindData(
    val date: String,
    val max: WindDirection,
    val min: WindDirection,
    val avg: WindDirection
)

data class WindDirection(
    val speed: Double,
    val direction: Double
)

data class DailySkycon(
    val date: String,
    val value: String
)

data class LifeIndex(
    val ultraviolet: List<IndexData>,
    val carWashing: List<IndexData>,
    val dressing: List<IndexData>,
    val comfort: List<IndexData>,
    val coldRisk: List<IndexData>
)

data class IndexData(
    val date: String,
    val index: String,
    val desc: String
)

// 为了兼容现有代码，保留WeatherResponse别名
typealias WeatherResponse = CaiyunWeatherResponse