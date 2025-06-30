package com.example.shuigongrizhi.config

object ApiConfig {
    // OpenWeatherMap API密钥
    // 注意：在生产环境中，应该将API密钥存储在更安全的地方，如BuildConfig或环境变量
    const val OPENWEATHER_API_KEY = "your_api_key_here"
    
    // 默认城市坐标（北京）
    const val DEFAULT_LATITUDE = 39.9042
    const val DEFAULT_LONGITUDE = 116.4074
    
    // 默认城市名称
    const val DEFAULT_CITY = "Beijing"
}