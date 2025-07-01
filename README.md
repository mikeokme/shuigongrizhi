# 淮工集团施工日志系统 (HuaiGong Group Construction Log System)

[![Android](https://img.shields.io/badge/Android-API%2024+-green.svg)](https://developer.android.com/about/versions/14)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-blue.svg)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-1.5+-orange.svg)](https://developer.android.com/jetpack/compose)
[![Room Database](https://img.shields.io/badge/Room%20Database-2.6+-purple.svg)](https://developer.android.com/training/data-storage/room)

> 🏗️ 专业的现代化施工日志管理系统，专为水利工程施工管理而设计

## 📋 项目概述

淮工施工日志系统是一款基于 Android 平台的现代化施工日志管理应用，采用 Kotlin + Jetpack Compose 开发，专为水利工程施工管理场景设计。系统集成了项目管理、日志记录、天气信息、多媒体管理、位置服务等核心功能，为施工管理人员提供全方位的数字化解决方案。

## ✨ 核心功能

### 🏢 项目管理
- **项目创建与编辑**：支持创建多种类型的水利工程项目
- **项目类型管理**：水库、河道、灌区、水电站、泵站等
- **项目信息管理**：项目名称、类型、描述、开始/结束日期、负责人
- **项目列表与详情**：直观的项目展示和管理界面
- **数据备份与恢复**：自动备份项目数据，支持数据恢复

### 📝 施工日志
- **智能日志记录**：支持按项目、按日期的日志管理
- **天气信息集成**：自动获取实时天气，支持手动修改
- **施工信息记录**：
  - 施工部位和主要内容
  - 人员、机械、材料进场和使用情况
  - 质量验收管理
  - 安全管理记录
- **媒体附件**：支持图片、视频等多媒体文件关联
- **日志导出**：支持 PDF 格式导出和分享

### 🌤️ 天气服务
- **实时天气获取**：集成 OpenWeatherMap API
- **自动定位**：优先使用系统定位，失败时使用 IP 定位
- **天气详情展示**：温度、湿度、风力、气压等详细信息
- **多城市支持**：支持手动切换城市天气查询

### 📸 多媒体管理
- **拍照录像**：集成系统相机，支持拍照和录像功能
- **媒体库管理**：按项目分类管理图片和视频文件
- **媒体预览**：支持图片和视频的预览播放
- **文件管理**：支持媒体文件的删除和管理

### 📍 位置服务
- **位置记录**：记录施工位置和历史轨迹
- **地图集成**：支持谷歌/高德/百度地图切换
- **轨迹管理**：查看和管理历史位置记录
- **日志关联**：位置信息与施工日志关联

### 📊 数据管理
- **本地数据库**：使用 Room 数据库进行本地存储
- **数据导出**：支持施工日志的 PDF 导出
- **数据备份**：自动备份项目数据到本地存储
- **数据恢复**：支持从备份文件恢复数据

## 🏗️ 技术架构

### 前端技术栈
- **UI 框架**：Jetpack Compose
- **状态管理**：StateFlow + ViewModel
- **导航**：Navigation Compose
- **主题**：Material 3 Design System

### 后端技术栈
- **数据库**：Room Database
- **网络请求**：Retrofit + OkHttp
- **依赖注入**：Hilt
- **异步处理**：Kotlin Coroutines

### 核心组件
- **数据层**：Entity → DAO → Repository → ViewModel
- **UI 层**：Screen → ViewModel → Repository
- **网络层**：Retrofit Service → Repository
- **工具层**：Utils → Manager

## 🚀 快速开始

### 环境要求
- Android Studio Hedgehog | 2023.1.1 或更高版本
- Android SDK API 34 (Android 14)
- Kotlin 1.9.0 或更高版本
- JDK 17 或更高版本

### 安装步骤

1. **克隆项目**
   ```bash
   git clone https://github.com/your-username/shuigongrizhi.git
   cd shuigongrizhi
   ```

2. **配置 API Key**
   - 在 `app/src/main/java/com/example/shuigongrizhi/config/ApiConfig.kt` 中配置 OpenWeatherMap API Key
   ```kotlin
   const val WEATHER_API_KEY = "your_openweathermap_api_key_here"
   ```

3. **构建项目**
   ```bash
   ./gradlew build
   ```

4. **运行应用**
   - 在 Android Studio 中打开项目
   - 连接 Android 设备或启动模拟器
   - 点击运行按钮

### 权限配置

应用需要以下权限：
- **相机权限**：用于拍照和录像功能
- **存储权限**：用于保存媒体文件和导出 PDF
- **位置权限**：用于获取当前位置和记录轨迹
- **网络权限**：用于获取天气信息和数据同步

## 📱 使用指南

### 项目管理
1. 点击主界面"项目管理"卡片
2. 点击"+"按钮创建新项目
3. 填写项目信息（支持日期选择器或 YYMMDD 格式输入）
4. 保存项目

### 施工日志
1. 选择项目进入项目详情页
2. 点击"新建日志"按钮
3. 选择日期并填写施工信息
4. 天气信息会自动获取，可手动修改
5. 添加相关媒体文件
6. 保存日志

### 天气查询
1. 点击主界面天气卡片
2. 查看当前城市天气详情
3. 支持手动切换城市

### 多媒体管理
1. 点击主界面"拍照录像"或"媒体管理"
2. 选择项目后进入媒体界面
3. 拍照、录像或查看历史媒体文件

### 位置服务
1. 点击主界面"位置服务"卡片
2. 查看当前位置和历史轨迹
3. 支持地图类型切换

## 🔧 开发指南

### 项目结构
```
app/src/main/java/com/example/shuigongrizhi/
├── config/           # 配置文件
├── data/            # 数据层
│   ├── converter/   # 类型转换器
│   ├── dao/         # 数据访问对象
│   ├── database/    # 数据库配置
│   ├── entity/      # 数据实体
│   ├── preferences/ # 偏好设置
│   └── repository/  # 数据仓库
├── di/              # 依赖注入
├── network/         # 网络层
├── ui/              # 用户界面
│   ├── components/  # 可复用组件
│   ├── screen/      # 页面
│   ├── theme/       # 主题样式
│   └── viewmodel/   # 视图模型
├── utils/           # 工具类
└── navigation/      # 导航配置
```

### 添加新功能
1. 在 `data/entity/` 中定义数据实体
2. 在 `data/dao/` 中创建数据访问对象
3. 在 `data/repository/` 中实现数据仓库
4. 在 `ui/viewmodel/` 中创建视图模型
5. 在 `ui/screen/` 中实现用户界面
6. 在 `navigation/Navigation.kt` 中添加路由

### 代码规范
- 使用 Kotlin 编码规范
- 遵循 MVVM 架构模式
- 使用 StateFlow 进行状态管理
- 添加适当的注释和文档

## 🐛 常见问题

### 构建问题
如果在构建过程中遇到 `R.jar` 文件被占用的问题：

**方案一：使用清理脚本**
```bash
# Windows
clean_build.bat

# Linux/Mac
./clean_build.sh
```

**方案二：手动清理**
1. 关闭 Android Studio
2. 删除 `app/build`、`build`、`.gradle` 目录
3. 重新打开项目

**方案三：停止 Gradle 守护进程**
```bash
./gradlew --stop
```

### 权限问题
确保在 AndroidManifest.xml 中正确配置了所需权限，并在运行时请求用户授权。

### API 配置
确保正确配置了 OpenWeatherMap API Key，否则天气功能将无法使用。

## 🤝 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 👨‍💻 开发者

- **开发者**：醉生梦死
- **版本**：beta 1.0
- **联系方式**：[your-email@example.com]

## 🙏 致谢

- [Jetpack Compose](https://developer.android.com/jetpack/compose) - 现代化 Android UI 工具包
- [Room Database](https://developer.android.com/training/data-storage/room) - 本地数据库解决方案
- [OpenWeatherMap](https://openweathermap.org/) - 天气数据 API
- [Material Design](https://material.io/) - 设计系统

---

⭐ 如果这个项目对你有帮助，请给它一个星标！