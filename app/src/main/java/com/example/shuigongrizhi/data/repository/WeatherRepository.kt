package com.example.shuigongrizhi.data.repository

import com.example.shuigongrizhi.config.ApiConfig
import com.example.shuigongrizhi.core.AppConfig
import com.example.shuigongrizhi.network.CaiyunWeatherResponse
import com.example.shuigongrizhi.network.RealtimeWeather
import com.example.shuigongrizhi.network.WeatherResponse
import com.example.shuigongrizhi.network.WeatherResult
import com.example.shuigongrizhi.network.WeatherService
import com.example.shuigongrizhi.network.WindData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
// import javax.inject.Singleton
import kotlin.random.Random

// @Singleton
class WeatherRepository @Inject constructor(
    private val weatherService: WeatherService,
    private val appConfig: AppConfig
) {
    
    /**
     * 获取当前使用的API Token
     */
    private fun getCurrentToken(): String {
        return appConfig?.weatherApiToken?.takeIf { it.isNotEmpty() } ?: ApiConfig.CAIYUN_API_TOKEN
    }
    
    /**
     * 检查是否启用无API模式
     */
    private fun isNoApiMode(): Boolean {
        return (appConfig?.weatherApiToken?.isEmpty() ?: true) && ApiConfig.CAIYUN_API_TOKEN.isEmpty()
    }
    
    /**
     * 生成模拟天气数据
     */
    private fun generateMockWeatherData(
        latitude: Double = ApiConfig.DEFAULT_LATITUDE,
        longitude: Double = ApiConfig.DEFAULT_LONGITUDE
    ): WeatherResponse {
        return CaiyunWeatherResponse(
            status = "ok",
            api_version = "v2.5",
            api_status = "active",
            lang = "zh_CN",
            unit = "metric",
            tzshift = 28800,
            timezone = "Asia/Shanghai",
            server_time = System.currentTimeMillis() / 1000,
            location = listOf(longitude, latitude),
            result = WeatherResult(
                realtime = RealtimeWeather(
                    status = "ok",
                    temperature = Random.nextDouble(15.0, 25.0),
                    humidity = Random.nextDouble(0.4, 0.8),
                    cloudrate = Random.nextDouble(0.1, 0.9),
                    skycon = listOf("CLEAR_DAY", "PARTLY_CLOUDY_DAY", "CLOUDY", "LIGHT_RAIN").random(),
                    visibility = Random.nextDouble(5.0, 15.0),
                    dswrf = Random.nextDouble(100.0, 800.0),
                    wind = WindData(
                        speed = Random.nextDouble(1.0, 10.0),
                        direction = Random.nextDouble(0.0, 360.0)
                    ),
                    pressure = Random.nextDouble(1000.0, 1020.0),
                    apparent_temperature = Random.nextDouble(15.0, 25.0)
                )
            )
        )
    }
    
    /**
     * 获取当前天气信息（通过经纬度）
     * 支持API模式和无API模式
     */
    suspend fun getCurrentWeather(
        latitude: Double = ApiConfig.DEFAULT_LATITUDE,
        longitude: Double = ApiConfig.DEFAULT_LONGITUDE
    ): Result<WeatherResponse> = withContext(Dispatchers.IO) {
        if (isNoApiMode()) {
            // 无API模式，返回模拟数据
            delay(500) // 模拟网络延迟
            Result.success(generateMockWeatherData(latitude, longitude))
        } else {
            // API模式，调用真实API
            executeWithRetry {
                weatherService?.getCurrentWeather(
                    token = getCurrentToken(),
                    longitude = longitude,
                    latitude = latitude,
                    lang = ApiConfig.DEFAULT_LANG,
                    unit = ApiConfig.DEFAULT_UNIT,
                    granu = ApiConfig.DEFAULT_GRANU
                ) ?: throw Exception("WeatherService not available")
            }
        }
    }
    
    /**
     * 获取当前天气信息（通过城市名）
     * 支持API模式和无API模式
     */
    suspend fun getCurrentWeatherByCity(
        cityName: String = ApiConfig.DEFAULT_CITY
    ): Result<WeatherResponse> = withContext(Dispatchers.IO) {
        if (isNoApiMode()) {
            // 无API模式，返回模拟数据
            delay(500) // 模拟网络延迟
            Result.success(generateMockWeatherData())
        } else {
            // API模式，调用真实API
            executeWithRetry {
                // 使用默认坐标，实际应用中可以先通过地理编码API获取坐标
                weatherService?.getCurrentWeather(
                    token = getCurrentToken(),
                    longitude = ApiConfig.DEFAULT_LONGITUDE,
                    latitude = ApiConfig.DEFAULT_LATITUDE,
                    lang = ApiConfig.DEFAULT_LANG,
                    unit = ApiConfig.DEFAULT_UNIT,
                    granu = ApiConfig.DEFAULT_GRANU
                ) ?: throw Exception("WeatherService not available")
            }
        }
    }
    
    /**
     * 获取实时天气信息
     */
    suspend fun getRealtimeWeather(
        latitude: Double = ApiConfig.DEFAULT_LATITUDE,
        longitude: Double = ApiConfig.DEFAULT_LONGITUDE
    ): Result<WeatherResponse> = withContext(Dispatchers.IO) {
        if (isNoApiMode()) {
            // 无API模式，返回模拟数据
            delay(500) // 模拟网络延迟
            Result.success(generateMockWeatherData(latitude, longitude))
        } else {
            // API模式，调用真实API
            executeWithRetry {
                weatherService?.getCurrentWeather(
                    token = getCurrentToken(),
                    longitude = longitude,
                    latitude = latitude,
                    lang = ApiConfig.DEFAULT_LANG,
                    unit = ApiConfig.DEFAULT_UNIT,
                    granu = "realtime"
                ) ?: throw Exception("WeatherService not available")
            }
        }
    }
    
    /**
     * 获取小时级天气预报
     */
    suspend fun getHourlyWeather(
        latitude: Double = ApiConfig.DEFAULT_LATITUDE,
        longitude: Double = ApiConfig.DEFAULT_LONGITUDE
    ): Result<WeatherResponse> = withContext(Dispatchers.IO) {
        if (isNoApiMode()) {
            // 无API模式，返回模拟数据
            delay(500) // 模拟网络延迟
            Result.success(generateMockWeatherData(latitude, longitude))
        } else {
            // API模式，调用真实API
            executeWithRetry {
                weatherService?.getCurrentWeather(
                    token = getCurrentToken(),
                    longitude = longitude,
                    latitude = latitude,
                    lang = ApiConfig.DEFAULT_LANG,
                    unit = ApiConfig.DEFAULT_UNIT,
                    granu = "hourly"
                ) ?: throw Exception("WeatherService not available")
            }
        }
    }
    
    /**
     * 获取天级天气预报
     */
    suspend fun getDailyWeather(
        latitude: Double = ApiConfig.DEFAULT_LATITUDE,
        longitude: Double = ApiConfig.DEFAULT_LONGITUDE
    ): Result<WeatherResponse> = withContext(Dispatchers.IO) {
        if (isNoApiMode()) {
            // 无API模式，返回模拟数据
            delay(500) // 模拟网络延迟
            Result.success(generateMockWeatherData(latitude, longitude))
        } else {
            // API模式，调用真实API
            executeWithRetry {
                weatherService?.getCurrentWeather(
                    token = getCurrentToken(),
                    longitude = longitude,
                    latitude = latitude,
                    lang = ApiConfig.DEFAULT_LANG,
                    unit = ApiConfig.DEFAULT_UNIT,
                    granu = "daily"
                ) ?: throw Exception("WeatherService not available")
            }
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
                return Result.failure(Exception("获取天气信息失败: ${e.message}"))
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
    
    /**
     * 测试API Token是否有效
     */
    suspend fun testToken(token: String): Result<String> = withContext(Dispatchers.IO) {
        if (token.isEmpty()) {
            // 无API模式
            delay(300) // 模拟验证延迟
            Result.success("无API模式已启用，将使用模拟天气数据")
        } else {
            // API模式，测试真实Token
            try {
                val response = weatherService?.getCurrentWeather(
                    token = token,
                    longitude = ApiConfig.DEFAULT_LONGITUDE,
                    latitude = ApiConfig.DEFAULT_LATITUDE,
                    lang = ApiConfig.DEFAULT_LANG,
                    unit = ApiConfig.DEFAULT_UNIT,
                    granu = ApiConfig.DEFAULT_GRANU
                ) ?: throw Exception("WeatherService not available")
                
                // 如果请求成功，返回成功信息
                Result.success("Token验证成功，可以正常获取天气数据")
            } catch (e: HttpException) {
                val errorMessage = when (e.code()) {
                    401 -> "Token无效，请检查API密钥"
                    403 -> "Token权限不足，请检查账户状态"
                    429 -> "API调用频率超限，Token有效但需要等待"
                    else -> "Token测试失败: HTTP ${e.code()}"
                }
                Result.failure(Exception(errorMessage))
            } catch (e: Exception) {
                Result.failure(Exception("Token测试失败: ${e.message}"))
            }
        }
    }
}