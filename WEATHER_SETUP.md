# 天气功能配置指南

## 概述

水利施工日志应用已集成天气信息模块，可以显示实时天气数据，帮助记录施工环境条件。

## 功能特性

### 主界面天气卡片
- 显示当前温度
- 显示天气状况描述
- 显示城市名称
- 支持加载状态和错误处理
- 点击可进入详细天气页面

### 天气详情页面
- 完整的天气信息展示
- 温度、湿度、气压、风速等详细数据
- 天气图标和描述
- 位置信息
- 刷新功能

## API配置

### 1. 获取OpenWeatherMap API密钥

1. 访问 [OpenWeatherMap](https://openweathermap.org/api)
2. 注册账户并登录
3. 在API Keys页面生成新的API密钥
4. 复制API密钥

### 2. 配置API密钥

打开文件：`app/src/main/java/com/example/shuigongrizhi/config/ApiConfig.kt`

将 `your_api_key_here` 替换为你的实际API密钥：

```kotlin
object ApiConfig {
    // 将下面的字符串替换为你的OpenWeatherMap API密钥
    const val OPENWEATHER_API_KEY = "你的API密钥"
    
    // 可以修改默认城市坐标
    const val DEFAULT_LATITUDE = 39.9042  // 北京纬度
    const val DEFAULT_LONGITUDE = 116.4074 // 北京经度
    
    const val DEFAULT_CITY = "Beijing"
}
```

### 3. 自定义默认位置

如果需要修改默认显示的城市，可以更新 `ApiConfig.kt` 中的坐标：

- `DEFAULT_LATITUDE`: 纬度
- `DEFAULT_LONGITUDE`: 经度
- `DEFAULT_CITY`: 城市名称

## 使用说明

### 主界面
1. 启动应用后，主界面会自动加载天气信息
2. 天气卡片显示当前温度和天气状况
3. 点击天气卡片进入详细页面

### 天气详情页面
1. 显示完整的天气信息
2. 点击刷新按钮更新数据
3. 点击返回按钮回到主界面

## 网络权限

确保应用已添加网络权限（已在AndroidManifest.xml中配置）：

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

## 故障排除

### 天气信息无法加载
1. 检查网络连接
2. 确认API密钥配置正确
3. 检查API密钥是否有效且未过期
4. 确认OpenWeatherMap服务可用

### API密钥相关问题
- 免费API密钥有调用次数限制
- 新注册的API密钥可能需要几小时才能生效
- 确保API密钥字符串没有多余的空格或字符

## 技术架构

### 组件结构
- `WeatherRepository`: 数据仓库，处理API调用
- `WeatherViewModel`: 视图模型，管理UI状态
- `WeatherService`: Retrofit接口，定义API端点
- `WeatherDetailScreen`: 天气详情页面UI
- `WeatherCard`: 主界面天气卡片组件

### 数据流
1. ViewModel调用Repository获取数据
2. Repository通过WeatherService调用API
3. API响应转换为UI状态
4. UI根据状态显示相应内容

## 扩展功能

可以考虑添加的功能：
- 位置服务集成，自动获取当前位置天气
- 天气预报（需要升级API计划）
- 天气历史记录
- 多城市天气对比
- 天气预警通知