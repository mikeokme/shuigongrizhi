package com.example.shuigongrizhi.config

object ApiConfig {
    // 彩云天气API Token
    // 设置为空字符串启用无API模式，使用模拟天气数据
    // 如需使用真实天气数据，请：
    // 1. 获取个人API Token：访问 https://caiyunapp.com/api/ 注册账号并获取个人Token
    // 2. 在应用设置中配置您的Token，或直接修改此处的CAIYUN_API_TOKEN值
    // 3. 无API模式提供随机生成的模拟天气数据，适合演示和测试
    const val CAIYUN_API_TOKEN = ""
    
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