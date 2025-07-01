package com.example.shuigongrizhi.core

import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.core.content.ContextCompat
import java.io.File
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * 工具类集合
 * 提供各种常用的工具方法
 */
object Utils {
    
    /**
     * 网络工具类
     */
    object Network {
        
        /**
         * 检查网络连接状态
         */
        fun isNetworkAvailable(context: Context): Boolean {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val network = connectivityManager.activeNetwork ?: return false
                val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            } else {
                @Suppress("DEPRECATION")
                val networkInfo = connectivityManager.activeNetworkInfo
                networkInfo?.isConnected == true
            }
        }
        
        /**
         * 获取网络类型
         */
        fun getNetworkType(context: Context): String {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val network = connectivityManager.activeNetwork ?: return "无网络"
                val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return "无网络"
                
                when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "移动网络"
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "以太网"
                    else -> "其他"
                }
            } else {
                @Suppress("DEPRECATION")
                val networkInfo = connectivityManager.activeNetworkInfo
                networkInfo?.typeName ?: "无网络"
            }
        }
    }
    
    /**
     * 文件工具类
     */
    object File {
        
        /**
         * 创建应用目录结构
         */
        fun createAppDirectories(context: Context): Boolean {
            return try {
                val externalDir = context.getExternalFilesDir(null) ?: return false
                val appDir = java.io.File(externalDir, Constants.Storage.APP_FOLDER_NAME)
                
                val directories = listOf(
                    java.io.File(appDir, Constants.Storage.PROJECTS_FOLDER),
                    java.io.File(appDir, Constants.Storage.MEDIA_FOLDER),
                    java.io.File(appDir, Constants.Storage.EXPORTS_FOLDER),
                    java.io.File(appDir, Constants.Storage.TEMP_FOLDER)
                )
                
                directories.all { it.mkdirs() || it.exists() }
            } catch (e: Exception) {
                false
            }
        }
        
        /**
         * 获取文件大小（可读格式）
         */
        fun getReadableFileSize(file: java.io.File): String {
            return if (file.exists()) {
                file.length().toReadableFileSize()
            } else {
                "0 B"
            }
        }
        
        /**
         * 清理临时文件
         */
        fun cleanTempFiles(context: Context): Boolean {
            return try {
                val externalDir = context.getExternalFilesDir(null) ?: return false
                val tempDir = java.io.File(externalDir, "${Constants.Storage.APP_FOLDER_NAME}/${Constants.Storage.TEMP_FOLDER}")
                
                if (tempDir.exists() && tempDir.isDirectory) {
                    tempDir.listFiles()?.forEach { file ->
                        if (file.isFile) {
                            file.delete()
                        }
                    }
                }
                true
            } catch (e: Exception) {
                false
            }
        }
        
        /**
         * 生成唯一文件名
         */
        fun generateUniqueFileName(prefix: String = "", extension: String = ""): String {
            val timestamp = SimpleDateFormat(Constants.DateFormat.FILE_DATETIME, Locale.getDefault()).format(Date())
            val uuid = UUID.randomUUID().toString().take(8)
            return "${prefix}${timestamp}_${uuid}${extension}"
        }
    }
    
    /**
     * 权限工具类
     */
    object Permission {
        
        /**
         * 检查权限是否已授予
         */
        fun isPermissionGranted(context: Context, permission: String): Boolean {
            return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
        
        /**
         * 检查多个权限是否都已授予
         */
        fun arePermissionsGranted(context: Context, permissions: Array<String>): Boolean {
            return permissions.all { isPermissionGranted(context, it) }
        }
        
        /**
         * 获取未授予的权限列表
         */
        fun getDeniedPermissions(context: Context, permissions: Array<String>): List<String> {
            return permissions.filter { !isPermissionGranted(context, it) }
        }
    }
    
    /**
     * 加密工具类
     */
    object Crypto {
        
        /**
         * 计算字符串的MD5哈希值
         */
        fun md5(input: String): String {
            val md = MessageDigest.getInstance("MD5")
            val digest = md.digest(input.toByteArray())
            return digest.joinToString("") { "%02x".format(it) }
        }
        
        /**
         * 计算字符串的SHA256哈希值
         */
        fun sha256(input: String): String {
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(input.toByteArray())
            return digest.joinToString("") { "%02x".format(it) }
        }
        
        /**
         * 生成随机UUID
         */
        fun generateUUID(): String {
            return UUID.randomUUID().toString()
        }
    }
    
    /**
     * 日期时间工具类
     */
    object DateTime {
        
        /**
         * 获取当前时间戳
         */
        fun getCurrentTimestamp(): Long {
            return System.currentTimeMillis()
        }
        
        /**
         * 格式化时间戳为可读字符串
         */
        fun formatTimestamp(timestamp: Long, pattern: String = Constants.DateFormat.DISPLAY_DATETIME): String {
            val formatter = SimpleDateFormat(pattern, Locale.getDefault())
            return formatter.format(Date(timestamp))
        }
        
        /**
         * 解析日期字符串
         */
        fun parseDate(dateString: String, pattern: String = Constants.DateFormat.ISO_DATE): Date? {
            return try {
                val formatter = SimpleDateFormat(pattern, Locale.getDefault())
                formatter.parse(dateString)
            } catch (e: Exception) {
                null
            }
        }
        
        /**
         * 计算两个日期之间的天数差
         */
        fun daysBetween(startDate: Date, endDate: Date): Long {
            val diffInMillis = endDate.time - startDate.time
            return diffInMillis / (24 * 60 * 60 * 1000)
        }
    }
    
    /**
     * 验证工具类
     */
    object Validation {
        
        /**
         * 验证项目名称
         */
        fun validateProjectName(name: String): ValidationResult {
            val trimmedName = name.trim()
            return when {
                trimmedName.isEmpty() -> ValidationResult.Error("项目名称不能为空")
                trimmedName.length < Constants.Validation.MIN_PROJECT_NAME_LENGTH -> 
                    ValidationResult.Error("项目名称至少需要${Constants.Validation.MIN_PROJECT_NAME_LENGTH}个字符")
                trimmedName.length > Constants.Validation.MAX_PROJECT_NAME_LENGTH -> 
                    ValidationResult.Error("项目名称不能超过${Constants.Validation.MAX_PROJECT_NAME_LENGTH}个字符")
                else -> ValidationResult.Success
            }
        }
        
        /**
         * 验证描述内容
         */
        fun validateDescription(description: String): ValidationResult {
            return when {
                description.length > Constants.Validation.MAX_DESCRIPTION_LENGTH -> 
                    ValidationResult.Error("描述不能超过${Constants.Validation.MAX_DESCRIPTION_LENGTH}个字符")
                else -> ValidationResult.Success
            }
        }
        
        /**
         * 验证内容长度
         */
        fun validateContent(content: String): ValidationResult {
            return when {
                content.length > Constants.Validation.MAX_CONTENT_LENGTH -> 
                    ValidationResult.Error("内容不能超过${Constants.Validation.MAX_CONTENT_LENGTH}个字符")
                else -> ValidationResult.Success
            }
        }
    }
}

/**
 * 验证结果密封类
 */
sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
    
    val isSuccess: Boolean
        get() = this is Success
        
    val isError: Boolean
        get() = this is Error
        
    fun getErrorMessage(): String? {
        return if (this is Error) message else null
    }
}