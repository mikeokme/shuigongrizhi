package com.example.shuigongrizhi.core

import android.util.Log
import com.example.shuigongrizhi.BuildConfig

/**
 * 统一日志管理工具
 * 提供分级日志记录，在发布版本中自动禁用调试日志
 */
object Logger {
    
    private const val DEFAULT_TAG = "ShuigongRizhi"
    
    // 是否启用日志（仅在调试模式下启用）
    private val isLoggingEnabled: Boolean
        get() = BuildConfig.DEBUG
    
    /**
     * 调试日志
     */
    fun d(message: String, tag: String = DEFAULT_TAG) {
        if (isLoggingEnabled) {
            Log.d(tag, message)
        }
    }
    
    fun d(message: String, throwable: Throwable?, tag: String = DEFAULT_TAG) {
        if (isLoggingEnabled) {
            Log.d(tag, message, throwable)
        }
    }
    
    /**
     * 信息日志
     */
    fun i(message: String, tag: String = DEFAULT_TAG) {
        if (isLoggingEnabled) {
            Log.i(tag, message)
        }
    }
    
    fun i(message: String, throwable: Throwable?, tag: String = DEFAULT_TAG) {
        if (isLoggingEnabled) {
            Log.i(tag, message, throwable)
        }
    }
    
    /**
     * 警告日志
     */
    fun w(message: String, tag: String = DEFAULT_TAG) {
        if (isLoggingEnabled) {
            Log.w(tag, message)
        }
    }
    
    fun w(message: String, throwable: Throwable?, tag: String = DEFAULT_TAG) {
        if (isLoggingEnabled) {
            Log.w(tag, message, throwable)
        }
    }
    
    /**
     * 错误日志（始终记录，即使在发布版本中）
     */
    fun e(message: String, tag: String = DEFAULT_TAG) {
        Log.e(tag, message)
    }
    
    fun e(message: String, throwable: Throwable?, tag: String = DEFAULT_TAG) {
        Log.e(tag, message, throwable)
    }
    
    /**
     * 详细日志
     */
    fun v(message: String, tag: String = DEFAULT_TAG) {
        if (isLoggingEnabled) {
            Log.v(tag, message)
        }
    }
    
    fun v(message: String, throwable: Throwable?, tag: String = DEFAULT_TAG) {
        if (isLoggingEnabled) {
            Log.v(tag, message, throwable)
        }
    }
    
    /**
     * 网络请求日志
     */
    fun network(message: String, tag: String = "${DEFAULT_TAG}_Network") {
        d(message, tag)
    }
    
    /**
     * 数据库操作日志
     */
    fun database(message: String, tag: String = "${DEFAULT_TAG}_Database") {
        d(message, tag)
    }
    
    /**
     * UI操作日志
     */
    fun ui(message: String, tag: String = "${DEFAULT_TAG}_UI") {
        d(message, tag)
    }
    
    /**
     * 业务逻辑日志
     */
    fun business(message: String, tag: String = "${DEFAULT_TAG}_Business") {
        d(message, tag)
    }
    
    /**
     * 性能监控日志
     */
    fun performance(message: String, tag: String = "${DEFAULT_TAG}_Performance") {
        i(message, tag)
    }
    
    /**
     * 记录方法执行时间
     */
    inline fun <T> measureTime(operation: String, block: () -> T): T {
        val startTime = System.currentTimeMillis()
        val result = block()
        val endTime = System.currentTimeMillis()
        performance("$operation 执行时间: ${endTime - startTime}ms")
        return result
    }
    
    /**
     * 记录异常信息
     */
    fun exception(throwable: Throwable, message: String = "发生异常", tag: String = "${DEFAULT_TAG}_Exception") {
        e("$message: ${throwable.message}", throwable, tag)
    }
    
    /**
     * 记录用户操作
     */
    fun userAction(action: String, details: String = "", tag: String = "${DEFAULT_TAG}_UserAction") {
        val logMessage = if (details.isNotEmpty()) "$action - $details" else action
        i(logMessage, tag)
    }
    
    /**
     * 记录生命周期事件
     */
    fun lifecycle(component: String, event: String, tag: String = "${DEFAULT_TAG}_Lifecycle") {
        d("$component - $event", tag)
    }
}