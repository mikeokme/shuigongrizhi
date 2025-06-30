package com.example.shuigongrizhi.config

object ApiConfig {
    // OpenWeatherMap API密钥
    // 注意：在生产环境中，应该将API密钥存储在更安全的地方，如BuildConfig或环境变量
    const val OPENWEATHER_API_KEY = "ad01985ba99a733396e2b6c25e55806f"
    
    // 默认城市坐标（江苏徐州）
    const val DEFAULT_LATITUDE = 34.2610
    const val DEFAULT_LONGITUDE = 117.1859
    
    // 默认城市名称
    const val DEFAULT_CITY = "Xuzhou"
}