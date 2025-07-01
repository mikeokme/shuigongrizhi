package com.example.shuigongrizhi.core

/**
 * 应用常量定义
 * 遵循单一职责原则，集中管理所有常量
 */
object Constants {
    
    /**
     * 数据库相关常量
     */
    object Database {
        const val NAME = "shuigong_database"
        const val VERSION = 4
        
        // 表名
        const val TABLE_PROJECTS = "projects"
        const val TABLE_CONSTRUCTION_LOGS = "construction_logs"
        const val TABLE_MEDIA_FILES = "media_files"
        const val TABLE_LOCATION_RECORDS = "location_records"
    }
    
    /**
     * 网络相关常量
     */
    object Network {
        const val CONNECT_TIMEOUT = 30L
        const val READ_TIMEOUT = 30L
        const val WRITE_TIMEOUT = 30L
        
        // API端点
        const val WEATHER_ENDPOINT = "weather"
        const val FORECAST_ENDPOINT = "forecast"
    }
    
    /**
     * 文件和存储相关常量
     */
    object Storage {
        const val APP_FOLDER_NAME = "ShuigongRizhi"
        const val PROJECTS_FOLDER = "Projects"
        const val MEDIA_FOLDER = "Media"
        const val EXPORTS_FOLDER = "Exports"
        const val TEMP_FOLDER = "Temp"
        
        // 文件扩展名
        const val PDF_EXTENSION = ".pdf"
        const val IMAGE_EXTENSION = ".jpg"
        const val VIDEO_EXTENSION = ".mp4"
    }
    
    /**
     * UI相关常量
     */
    object UI {
        const val ANIMATION_DURATION_SHORT = 300
        const val ANIMATION_DURATION_MEDIUM = 500
        const val ANIMATION_DURATION_LONG = 1000
        
        // 分页相关
        const val PAGE_SIZE = 20
        const val PREFETCH_DISTANCE = 5
    }
    
    /**
     * 权限相关常量
     */
    object Permissions {
        const val CAMERA_REQUEST_CODE = 1001
        const val STORAGE_REQUEST_CODE = 1002
        const val LOCATION_REQUEST_CODE = 1003
    }
    
    /**
     * 默认值常量
     */
    object Defaults {
        const val PROJECT_NAME = "淮工自营水利工程项目"
        const val PROJECT_MANAGER = "自营"
        const val PROJECT_DESCRIPTION = "淮工集团自营水利工程项目，用于日常施工日志记录和管理。"
        
        // 默认天气信息
        const val DEFAULT_WEATHER = "晴"
        const val DEFAULT_TEMPERATURE = "适宜"
        const val DEFAULT_WIND = "微风"
    }
    
    /**
     * 日期格式常量
     */
    object DateFormat {
        const val DISPLAY_DATE = "yyyy年MM月dd日"
        const val DISPLAY_DATETIME = "yyyy年MM月dd日 HH:mm"
        const val FILE_DATE = "yyyyMMdd"
        const val FILE_DATETIME = "yyyyMMdd_HHmmss"
        const val ISO_DATE = "yyyy-MM-dd"
        const val ISO_DATETIME = "yyyy-MM-dd'T'HH:mm:ss"
    }
    
    /**
     * 验证规则常量
     */
    object Validation {
        const val MIN_PROJECT_NAME_LENGTH = 2
        const val MAX_PROJECT_NAME_LENGTH = 50
        const val MIN_DESCRIPTION_LENGTH = 0
        const val MAX_DESCRIPTION_LENGTH = 500
        const val MAX_CONTENT_LENGTH = 2000
    }
    
    /**
     * 错误消息常量
     */
    object ErrorMessages {
        const val NETWORK_ERROR = "网络连接失败，请检查网络设置"
        const val DATABASE_ERROR = "数据库操作失败"
        const val VALIDATION_ERROR = "数据验证失败"
        const val PERMISSION_DENIED = "权限被拒绝"
        const val FILE_NOT_FOUND = "文件未找到"
        const val UNKNOWN_ERROR = "发生未知错误"
    }
}