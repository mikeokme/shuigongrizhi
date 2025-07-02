package com.example.shuigongrizhi.core

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.shuigongrizhi.BuildConfig
// import javax.inject.Inject
// import javax.inject.Singleton

/**
 * 应用配置管理器
 * 提供统一的配置存储和访问
 */
// @Singleton
class AppConfig /* @Inject constructor(
    private val context: Context
) */ {
    private val context: Context? = null
    
    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    companion object {
        private const val PREFS_NAME = "shuigong_config"
        
        // 配置键名
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val KEY_APP_VERSION = "app_version"
        private const val KEY_LAST_BACKUP_TIME = "last_backup_time"
        private const val KEY_AUTO_BACKUP_ENABLED = "auto_backup_enabled"
        private const val KEY_BACKUP_INTERVAL_DAYS = "backup_interval_days"
        private const val KEY_COMPRESS_IMAGES = "compress_images"
        private const val KEY_IMAGE_QUALITY = "image_quality"
        private const val KEY_MAX_VIDEO_DURATION = "max_video_duration"
        private const val KEY_WEATHER_AUTO_FETCH = "weather_auto_fetch"
        private const val KEY_LOCATION_ENABLED = "location_enabled"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_EXPORT_FORMAT = "export_format"
        private const val KEY_SHOW_TIPS = "show_tips"
        private const val KEY_DEBUG_MODE = "debug_mode"
        private const val KEY_WEATHER_API_TOKEN = "weather_api_token"
        private const val KEY_WEATHER_TOKEN_VERIFIED = "weather_token_verified"
    }
    
    /**
     * 应用基本配置
     */
    var isFirstLaunch: Boolean
        get() = sharedPreferences.getBoolean(KEY_FIRST_LAUNCH, true)
        set(value) = sharedPreferences.edit { putBoolean(KEY_FIRST_LAUNCH, value) }
    
    var appVersion: String
        get() = sharedPreferences.getString(KEY_APP_VERSION, BuildConfig.VERSION_NAME) ?: BuildConfig.VERSION_NAME
        set(value) = sharedPreferences.edit { putString(KEY_APP_VERSION, value) }
    
    /**
     * 备份配置
     */
    var lastBackupTime: Long
        get() = sharedPreferences.getLong(KEY_LAST_BACKUP_TIME, 0L)
        set(value) = sharedPreferences.edit { putLong(KEY_LAST_BACKUP_TIME, value) }
    
    var isAutoBackupEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_AUTO_BACKUP_ENABLED, true)
        set(value) = sharedPreferences.edit { putBoolean(KEY_AUTO_BACKUP_ENABLED, value) }
    
    var backupIntervalDays: Int
        get() = sharedPreferences.getInt(KEY_BACKUP_INTERVAL_DAYS, 7)
        set(value) = sharedPreferences.edit { putInt(KEY_BACKUP_INTERVAL_DAYS, value) }
    
    /**
     * 媒体文件配置
     */
    var isCompressImages: Boolean
        get() = sharedPreferences.getBoolean(KEY_COMPRESS_IMAGES, true)
        set(value) = sharedPreferences.edit { putBoolean(KEY_COMPRESS_IMAGES, value) }
    
    var imageQuality: Int
        get() = sharedPreferences.getInt(KEY_IMAGE_QUALITY, 80)
        set(value) = sharedPreferences.edit { putInt(KEY_IMAGE_QUALITY, value.coerceIn(10, 100)) }
    
    var maxVideoDurationSeconds: Int
        get() = sharedPreferences.getInt(KEY_MAX_VIDEO_DURATION, 300) // 默认5分钟
        set(value) = sharedPreferences.edit { putInt(KEY_MAX_VIDEO_DURATION, value.coerceIn(30, 1800)) }
    
    /**
     * 功能配置
     */
    var isWeatherAutoFetch: Boolean
        get() = sharedPreferences.getBoolean(KEY_WEATHER_AUTO_FETCH, true)
        set(value) = sharedPreferences.edit { putBoolean(KEY_WEATHER_AUTO_FETCH, value) }
    
    var isLocationEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_LOCATION_ENABLED, true)
        set(value) = sharedPreferences.edit { putBoolean(KEY_LOCATION_ENABLED, value) }
    
    /**
     * UI配置
     */
    var themeMode: ThemeMode
        get() {
            val mode = sharedPreferences.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
            return try {
                ThemeMode.valueOf(mode ?: ThemeMode.SYSTEM.name)
            } catch (e: IllegalArgumentException) {
                ThemeMode.SYSTEM
            }
        }
        set(value) = sharedPreferences.edit { putString(KEY_THEME_MODE, value.name) }
    
    var language: String
        get() = sharedPreferences.getString(KEY_LANGUAGE, "zh") ?: "zh"
        set(value) = sharedPreferences.edit { putString(KEY_LANGUAGE, value) }
    
    /**
     * 导出配置
     */
    var exportFormat: ExportFormat
        get() {
            val format = sharedPreferences.getString(KEY_EXPORT_FORMAT, ExportFormat.PDF.name)
            return try {
                ExportFormat.valueOf(format ?: ExportFormat.PDF.name)
            } catch (e: IllegalArgumentException) {
                ExportFormat.PDF
            }
        }
        set(value) = sharedPreferences.edit { putString(KEY_EXPORT_FORMAT, value.name) }
    
    /**
     * 用户体验配置
     */
    var showTips: Boolean
        get() = sharedPreferences.getBoolean(KEY_SHOW_TIPS, true)
        set(value) = sharedPreferences.edit { putBoolean(KEY_SHOW_TIPS, value) }
    
    /**
     * 开发配置
     */
    var isDebugMode: Boolean
        get() = sharedPreferences.getBoolean(KEY_DEBUG_MODE, BuildConfig.DEBUG)
        set(value) = sharedPreferences.edit { putBoolean(KEY_DEBUG_MODE, value) }
    
    /**
     * 天气API配置
     */
    var weatherApiToken: String
        get() = sharedPreferences.getString(KEY_WEATHER_API_TOKEN, "") ?: ""
        set(value) = sharedPreferences.edit { putString(KEY_WEATHER_API_TOKEN, value) }
    
    var isWeatherTokenVerified: Boolean
        get() = sharedPreferences.getBoolean(KEY_WEATHER_TOKEN_VERIFIED, false)
        set(value) = sharedPreferences.edit { putBoolean(KEY_WEATHER_TOKEN_VERIFIED, value) }
    
    /**
     * 重置所有配置到默认值
     */
    fun resetToDefaults() {
        Logger.business("重置应用配置到默认值")
        sharedPreferences.edit {
            clear()
            // 保留一些重要的配置
            putString(KEY_APP_VERSION, BuildConfig.VERSION_NAME)
            putBoolean(KEY_FIRST_LAUNCH, false) // 避免重复显示引导页
        }
    }
    
    /**
     * 获取配置摘要信息
     */
    fun getConfigSummary(): Map<String, Any> {
        return mapOf(
            "应用版本" to appVersion,
            "首次启动" to isFirstLaunch,
            "自动备份" to isAutoBackupEnabled,
            "备份间隔" to "${backupIntervalDays}天",
            "图片压缩" to isCompressImages,
            "图片质量" to "${imageQuality}%",
            "自动获取天气" to isWeatherAutoFetch,
            "位置服务" to isLocationEnabled,
            "主题模式" to themeMode.displayName,
            "语言" to language,
            "导出格式" to exportFormat.displayName,
            "显示提示" to showTips,
            "调试模式" to isDebugMode
        )
    }
    
    /**
     * 检查是否需要备份
     */
    fun shouldBackup(): Boolean {
        if (!isAutoBackupEnabled) return false
        
        val currentTime = System.currentTimeMillis()
        val intervalMillis = backupIntervalDays * 24 * 60 * 60 * 1000L
        
        return (currentTime - lastBackupTime) >= intervalMillis
    }
    
    /**
     * 更新应用版本信息
     */
    fun updateAppVersion() {
        val currentVersion = BuildConfig.VERSION_NAME
        if (appVersion != currentVersion) {
            Logger.business("应用版本更新: $appVersion -> $currentVersion")
            appVersion = currentVersion
        }
    }
    
    /**
     * 标记首次启动完成
     */
    fun markFirstLaunchCompleted() {
        if (isFirstLaunch) {
            Logger.business("标记首次启动完成")
            isFirstLaunch = false
        }
    }
}

/**
 * 主题模式枚举
 */
enum class ThemeMode(val displayName: String) {
    LIGHT("浅色模式"),
    DARK("深色模式"),
    SYSTEM("跟随系统")
}

/**
 * 导出格式枚举
 */
enum class ExportFormat(val displayName: String, val extension: String) {
    PDF("PDF文档", ".pdf"),
    WORD("Word文档", ".docx"),
    EXCEL("Excel表格", ".xlsx"),
    HTML("网页文件", ".html")
}