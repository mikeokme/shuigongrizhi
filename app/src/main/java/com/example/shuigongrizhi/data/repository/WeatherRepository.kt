package com.example.shuigongrizhi.data.repository

import com.example.shuigongrizhi.config.ApiConfig
import com.example.shuigongrizhi.network.WeatherResponse
import com.example.shuigongrizhi.network.WeatherService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor(
    private val weatherService: WeatherService
) {
    
    /**
     * 获取当前天气信息（通过经纬度）
     * 使用彩云天气API
     */
    suspend fun getCurrentWeather(
        latitude: Double = ApiConfig.DEFAULT_LATITUDE,
        longitude: Double = ApiConfig.DEFAULT_LONGITUDE
    ): Result<WeatherResponse> = withContext(Dispatchers.IO) {
        executeWithRetry {
            weatherService.getCurrentWeather(
                token = ApiConfig.CAIYUN_API_TOKEN,
                longitude = longitude,
                latitude = latitude,
                lang = ApiConfig.DEFAULT_LANG,
                unit = ApiConfig.DEFAULT_UNIT,
                granu = ApiConfig.DEFAULT_GRANU
            )
        }
    }
    
    /**
     * 获取当前天气信息（通过城市名）
     * 彩云天气API支持经纬度查询，这里使用默认坐标
     */
    suspend fun getCurrentWeatherByCity(
        cityName: String = ApiConfig.DEFAULT_CITY
    ): Result<WeatherResponse> = withContext(Dispatchers.IO) {
        executeWithRetry {
            // 使用默认坐标，实际应用中可以先通过地理编码API获取坐标
            weatherService.getCurrentWeather(
                token = ApiConfig.CAIYUN_API_TOKEN,
                longitude = ApiConfig.DEFAULT_LONGITUDE,
                latitude = ApiConfig.DEFAULT_LATITUDE,
                lang = ApiConfig.DEFAULT_LANG,
                unit = ApiConfig.DEFAULT_UNIT,
                granu = ApiConfig.DEFAULT_GRANU
            )
        }
    }
    
    /**
     * 获取实时天气信息
     */
    suspend fun getRealtimeWeather(
        latitude: Double = ApiConfig.DEFAULT_LATITUDE,
        longitude: Double = ApiConfig.DEFAULT_LONGITUDE
    ): Result<WeatherResponse> = withContext(Dispatchers.IO) {
        executeWithRetry {
            weatherService.getCurrentWeather(
                token = ApiConfig.CAIYUN_API_TOKEN,
                longitude = longitude,
                latitude = latitude,
                lang = ApiConfig.DEFAULT_LANG,
                unit = ApiConfig.DEFAULT_UNIT,
                granu = "realtime"
            )
        }
    }
    
    /**
     * 获取小时级天气预报
     */
    suspend fun getHourlyWeather(
        latitude: Double = ApiConfig.DEFAULT_LATITUDE,
        longitude: Double = ApiConfig.DEFAULT_LONGITUDE
    ): Result<WeatherResponse> = withContext(Dispatchers.IO) {
        executeWithRetry {
            weatherService.getCurrentWeather(
                token = ApiConfig.CAIYUN_API_TOKEN,
                longitude = longitude,
                latitude = latitude,
                lang = ApiConfig.DEFAULT_LANG,
                unit = ApiConfig.DEFAULT_UNIT,
                granu = "hourly"
            )
        }
    }
    
    /**
     * 获取天级天气预报
     */
    suspend fun getDailyWeather(
        latitude: Double = ApiConfig.DEFAULT_LATITUDE,
        longitude: Double = ApiConfig.DEFAULT_LONGITUDE
    ): Result<WeatherResponse> = withContext(Dispatchers.IO) {
        executeWithRetry {
            weatherService.getCurrentWeather(
                token = ApiConfig.CAIYUN_API_TOKEN,
                longitude = longitude,
                latitude = latitude,
                lang = ApiConfig.DEFAULT_LANG,
                unit = ApiConfig.DEFAULT_UNIT,
                granu = "daily"
            )
        }
    }
    
    /**
     * 执行API调用并处理重试逻辑
     */
    private suspend fun executeWithRetry(
        maxRetries: Int = 3,
        initialDelayMs: Long = 1000,
        maxDelayMs: Long = 10000,
        apiCall: suspend () -> WeatherResponse
    ): Result<WeatherResponse> {
        var currentDelay = initialDelayMs
        var lastException: Exception? = null
        
        repeat(maxRetries) { attempt ->
            try {
                val response = apiCall()
                return Result.success(response)
            } catch (e: HttpException) {
                lastException = e
                when (e.code()) {
                    429 -> {
                        // API调用频率超限，需要等待更长时间
                        if (attempt < maxRetries - 1) {
                            val retryAfter = e.response()?.headers()?.get("Retry-After")?.toLongOrNull()
                            val delayTime = if (retryAfter != null) {
                                retryAfter * 1000 // 转换为毫秒
                            } else {
                                minOf(currentDelay * 2, maxDelayMs) // 指数退避
                            }
                            delay(delayTime)
                            currentDelay = delayTime
                        }
                    }
                    401 -> {
                        // API密钥无效，不需要重试
                        return Result.failure(Exception("API密钥无效，请检查配置"))
                    }
                    403 -> {
                        // 权限不足，不需要重试
                        return Result.failure(Exception("API权限不足，请检查账户状态"))
                    }
                    404 -> {
                        // 资源不存在，不需要重试
                        return Result.failure(Exception("请求的资源不存在"))
                    }
                    500, 502, 503, 504 -> {
                        // 服务器错误，可以重试
                        if (attempt < maxRetries - 1) {
                            delay(currentDelay)
                            currentDelay = minOf(currentDelay * 2, maxDelayMs)
                        }
                    }
                    else -> {
                        // 其他HTTP错误，不重试
                        return Result.failure(Exception("网络请求失败: HTTP ${e.code()}"))
                    }
                }
            } catch (e: SocketTimeoutException) {
                lastException = e
                if (attempt < maxRetries - 1) {
                    delay(currentDelay)
                    currentDelay = minOf(currentDelay * 2, maxDelayMs)
                }
            } catch (e: UnknownHostException) {
                // 网络连接问题，不重试
                return Result.failure(Exception("网络连接失败，请检查网络设置"))
            } catch (e: Exception) {
                lastException = e
                // 其他异常，不重试
                break
            }
        }
        
        // 所有重试都失败了
        val errorMessage = when (lastException) {
            is HttpException -> {
                when (lastException.code()) {
                    429 -> "API调用频率超限，请稍后再试"
                    else -> "网络请求失败: ${lastException.message()}"
                }
            }
            is SocketTimeoutException -> "请求超时，请检查网络连接"
            else -> "获取天气信息失败: ${lastException?.message ?: "未知错误"}"
        }
        
        return Result.failure(Exception(errorMessage))
    }
}