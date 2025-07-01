package com.example.shuigongrizhi.config

object ApiConfig {
    // 彩云天气 API Token
    // 注意：在生产环境中，应该将API密钥存储在更安全的地方，如BuildConfig或环境变量
    // 当前使用的是演示Token，免费版本有调用频率限制
    // 如果遇到HTTP 429错误，请：
    // 1. 注册彩云天气账号获取个人Token: https://dashboard.caiyunapp.com/
    // 2. 或者减少API调用频率
    // 3. 考虑升级到付费版本以获得更高的调用限额
    const val CAIYUN_API_TOKEN = "TAkhjf8d1nlSlspN" // 演示Token，请替换为您的实际Token
    
    // 彩云天气 API 基础URL
    const val CAIYUN_BASE_URL = "https://api.caiyunapp.com/v2.5/"
    
    // 默认城市坐标（江苏徐州）
    const val DEFAULT_LATITUDE = 34.2610
    const val DEFAULT_LONGITUDE = 117.1859
    
    // 默认城市名称
    const val DEFAULT_CITY = "徐州"
    
    // API 参数配置
    const val DEFAULT_LANG = "zh_CN"
    const val DEFAULT_UNIT = "metric"
    const val DEFAULT_GRANU = "realtime" // realtime, minutely, hourly, daily
}