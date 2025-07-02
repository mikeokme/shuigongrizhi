package com.example.shuigongrizhi.core

import android.content.Context
import com.example.shuigongrizhi.BuildConfig
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.io.PrintWriter
import java.io.StringWriter
// import javax.inject.Inject
// import javax.inject.Singleton

/**
 * 全局异常处理器
 * 提供统一的异常处理和错误报告功能
 */
// @Singleton
class ExceptionHandler /* @Inject constructor(
    private val context: Context
) */ {
    private val context: Context? = null
    
    private val _errorEvents = MutableSharedFlow<ErrorEvent>()
    val errorEvents: SharedFlow<ErrorEvent> = _errorEvents.asSharedFlow()
    
    /**
     * 协程异常处理器
     */
    val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        handleException(exception, "协程执行异常")
    }
    
    /**
     * 处理异常
     */
    fun handleException(
        throwable: Throwable,
        context: String = "未知上下文",
        isCritical: Boolean = false
    ) {
        val errorEvent = createErrorEvent(throwable, context, isCritical)
        
        // 记录日志
        logException(errorEvent)
        
        // 发送错误事件
        _errorEvents.tryEmit(errorEvent)
        
        // 如果是严重错误，可以考虑额外处理
        if (isCritical) {
            handleCriticalError(errorEvent)
        }
    }
    
    /**
     * 创建错误事件
     */
    private fun createErrorEvent(
        throwable: Throwable,
        context: String,
        isCritical: Boolean
    ): ErrorEvent {
        return ErrorEvent(
            exception = throwable,
            context = context,
            timestamp = System.currentTimeMillis(),
            isCritical = isCritical,
            stackTrace = getStackTrace(throwable),
            errorType = classifyException(throwable),
            userMessage = generateUserMessage(throwable)
        )
    }
    
    /**
     * 记录异常日志
     */
    private fun logException(errorEvent: ErrorEvent) {
        val logLevel = if (errorEvent.isCritical) "CRITICAL" else "ERROR"
        val message = "[$logLevel] ${errorEvent.context}: ${errorEvent.exception.message}"
        
        Logger.exception(errorEvent.exception, message)
        
        // 详细堆栈信息
        Logger.d("异常堆栈: ${errorEvent.stackTrace}", "ExceptionHandler")
    }
    
    /**
     * 处理严重错误
     */
    private fun handleCriticalError(errorEvent: ErrorEvent) {
        Logger.e("检测到严重错误: ${errorEvent.context}", "ExceptionHandler")
        
        // 可以在这里添加额外的处理逻辑
        // 例如：发送错误报告、显示错误对话框等
    }
    
    /**
     * 获取堆栈跟踪字符串
     */
    private fun getStackTrace(throwable: Throwable): String {
        val stringWriter = StringWriter()
        val printWriter = PrintWriter(stringWriter)
        throwable.printStackTrace(printWriter)
        return stringWriter.toString()
    }
    
    /**
     * 分类异常类型
     */
    private fun classifyException(throwable: Throwable): ErrorType {
        return when (throwable) {
            is NetworkException -> ErrorType.NETWORK
            is DatabaseException -> ErrorType.DATABASE
            is ValidationException -> ErrorType.VALIDATION
            is BusinessException -> ErrorType.BUSINESS
            is SecurityException -> ErrorType.SECURITY
            is OutOfMemoryError -> ErrorType.MEMORY
            is IllegalArgumentException,
            is IllegalStateException -> ErrorType.LOGIC
            is java.io.IOException -> ErrorType.IO
            is java.net.SocketTimeoutException,
            is java.net.ConnectException -> ErrorType.NETWORK
            else -> ErrorType.UNKNOWN
        }
    }
    
    /**
     * 生成用户友好的错误消息
     */
    private fun generateUserMessage(throwable: Throwable): String {
        return when (throwable) {
            is NetworkException -> "网络连接异常，请检查网络设置"
            is DatabaseException -> "数据存储异常，请稍后重试"
            is ValidationException -> throwable.message ?: "数据验证失败"
            is BusinessException -> throwable.message ?: "业务处理异常"
            is SecurityException -> "权限不足，请检查应用权限设置"
            is OutOfMemoryError -> "内存不足，请关闭其他应用后重试"
            is java.io.IOException -> "文件操作失败，请检查存储空间"
            is java.net.SocketTimeoutException -> "网络请求超时，请检查网络连接"
            is java.net.ConnectException -> "无法连接到服务器，请检查网络"
            else -> "发生未知错误，请稍后重试"
        }
    }
    
    /**
     * 获取错误统计信息
     */
    fun getErrorStatistics(): ErrorStatistics {
        // 这里可以实现错误统计逻辑
        // 例如：从本地存储读取错误记录，统计各类错误的发生次数
        return ErrorStatistics(
            totalErrors = 0,
            criticalErrors = 0,
            networkErrors = 0,
            databaseErrors = 0,
            lastErrorTime = 0L
        )
    }
    
    /**
     * 清理错误记录
     */
    fun clearErrorHistory() {
        Logger.business("清理错误历史记录")
        // 实现清理逻辑
    }
    
    /**
     * 生成错误报告
     */
    fun generateErrorReport(): String {
        val statistics = getErrorStatistics()
        val deviceInfo = getDeviceInfo()
        
        return buildString {
            appendLine("=== 错误报告 ===")
            appendLine("生成时间: ${Utils.DateTime.formatTimestamp(System.currentTimeMillis())}")
            appendLine("应用版本: ${BuildConfig.VERSION_NAME}")
            appendLine("构建版本: ${BuildConfig.VERSION_CODE}")
            appendLine()
            appendLine("=== 设备信息 ===")
            deviceInfo.forEach { (key, value) ->
                appendLine("$key: $value")
            }
            appendLine()
            appendLine("=== 错误统计 ===")
            appendLine("总错误数: ${statistics.totalErrors}")
            appendLine("严重错误数: ${statistics.criticalErrors}")
            appendLine("网络错误数: ${statistics.networkErrors}")
            appendLine("数据库错误数: ${statistics.databaseErrors}")
            if (statistics.lastErrorTime > 0) {
                appendLine("最后错误时间: ${Utils.DateTime.formatTimestamp(statistics.lastErrorTime)}")
            }
        }
    }
    
    /**
     * 获取设备信息
     */
    private fun getDeviceInfo(): Map<String, String> {
        return mapOf(
            "设备型号" to android.os.Build.MODEL,
            "系统版本" to "Android ${android.os.Build.VERSION.RELEASE}",
            "API级别" to android.os.Build.VERSION.SDK_INT.toString(),
            "制造商" to android.os.Build.MANUFACTURER,
            "设备品牌" to android.os.Build.BRAND,
            "CPU架构" to android.os.Build.SUPPORTED_ABIS.joinToString(", ")
        )
    }
}

/**
 * 错误事件数据类
 */
data class ErrorEvent(
    val exception: Throwable,
    val context: String,
    val timestamp: Long,
    val isCritical: Boolean,
    val stackTrace: String,
    val errorType: ErrorType,
    val userMessage: String
) {
    val id: String = Utils.Crypto.generateUUID()
    
    fun getDisplayTime(): String {
        return Utils.DateTime.formatTimestamp(timestamp)
    }
}

/**
 * 错误类型枚举
 */
enum class ErrorType(val displayName: String) {
    NETWORK("网络错误"),
    DATABASE("数据库错误"),
    VALIDATION("验证错误"),
    BUSINESS("业务错误"),
    SECURITY("安全错误"),
    MEMORY("内存错误"),
    LOGIC("逻辑错误"),
    IO("IO错误"),
    UNKNOWN("未知错误")
}

/**
 * 错误统计数据类
 */
data class ErrorStatistics(
    val totalErrors: Int,
    val criticalErrors: Int,
    val networkErrors: Int,
    val databaseErrors: Int,
    val lastErrorTime: Long
) {
    val errorRate: Double
        get() = if (totalErrors > 0) criticalErrors.toDouble() / totalErrors else 0.0
    
    fun getHealthStatus(): String {
        return when {
            criticalErrors == 0 && totalErrors < 5 -> "良好"
            criticalErrors < 2 && totalErrors < 20 -> "正常"
            criticalErrors < 5 && totalErrors < 50 -> "警告"
            else -> "严重"
        }
    }
}