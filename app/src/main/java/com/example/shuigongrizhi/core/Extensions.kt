package com.example.shuigongrizhi.core

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 通用扩展函数集合
 * 提供常用的工具方法，提高代码复用性
 */

// ==================== Context扩展 ====================

/**
 * 显示Toast消息
 */
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

/**
 * 显示长时间Toast消息
 */
fun Context.showLongToast(message: String) {
    showToast(message, Toast.LENGTH_LONG)
}

// ==================== Date扩展 ====================

/**
 * 格式化日期为显示字符串
 */
fun Date.toDisplayString(): String {
    val formatter = SimpleDateFormat(Constants.DateFormat.DISPLAY_DATE, Locale.getDefault())
    return formatter.format(this)
}

/**
 * 格式化日期时间为显示字符串
 */
fun Date.toDisplayDateTimeString(): String {
    val formatter = SimpleDateFormat(Constants.DateFormat.DISPLAY_DATETIME, Locale.getDefault())
    return formatter.format(this)
}

/**
 * 格式化日期为文件名字符串
 */
fun Date.toFileString(): String {
    val formatter = SimpleDateFormat(Constants.DateFormat.FILE_DATETIME, Locale.getDefault())
    return formatter.format(this)
}

/**
 * 格式化日期为ISO字符串
 */
fun Date.toIsoString(): String {
    val formatter = SimpleDateFormat(Constants.DateFormat.ISO_DATETIME, Locale.getDefault())
    return formatter.format(this)
}

// ==================== String扩展 ====================

/**
 * 检查字符串是否为有效的项目名称
 */
fun String.isValidProjectName(): Boolean {
    return this.trim().length in Constants.Validation.MIN_PROJECT_NAME_LENGTH..Constants.Validation.MAX_PROJECT_NAME_LENGTH
}

/**
 * 检查字符串是否为有效的描述
 */
fun String.isValidDescription(): Boolean {
    return this.length <= Constants.Validation.MAX_DESCRIPTION_LENGTH
}

/**
 * 安全地截取字符串
 */
fun String.safeTruncate(maxLength: Int): String {
    return if (this.length <= maxLength) this else this.take(maxLength - 3) + "..."
}

/**
 * 移除多余的空白字符
 */
fun String.cleanWhitespace(): String {
    return this.trim().replace(Regex("\\s+"), " ")
}

// ==================== Flow扩展 ====================

/**
 * 将Flow转换为Result包装的Flow
 */
fun <T> Flow<T>.asResult(): Flow<Result<T>> {
    return this
        .map<T, Result<T>> { Result.Success(it) }
        .catch { emit(Result.Error(it)) }
}

/**
 * 在Flow中处理错误
 */
fun <T> Flow<T>.handleErrors(): Flow<T> {
    return this.catch { throwable ->
        // 记录错误日志
        android.util.Log.e("FlowError", "Flow error occurred", throwable)
        // 可以选择重新抛出或发出默认值
        throw throwable
    }
}

// ==================== Compose扩展 ====================

/**
 * 在Compose中显示Toast
 */
@Composable
fun ShowToast(message: String) {
    val context = LocalContext.current
    LaunchedEffect(message) {
        if (message.isNotEmpty()) {
            context.showToast(message)
        }
    }
}

/**
 * 在Compose中处理错误事件
 */
@Composable
fun HandleErrorEvents(
    errorFlow: Flow<String>,
    onError: (String) -> Unit = {}
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        errorFlow.collect { error ->
            context.showToast(error)
            onError(error)
        }
    }
}

// ==================== 数字扩展 ====================

/**
 * 将毫秒转换为秒
 */
fun Long.toSeconds(): Long = this / 1000

/**
 * 将字节转换为可读的文件大小
 */
fun Long.toReadableFileSize(): String {
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    var size = this.toDouble()
    var unitIndex = 0
    
    while (size >= 1024 && unitIndex < units.size - 1) {
        size /= 1024
        unitIndex++
    }
    
    return String.format("%.1f %s", size, units[unitIndex])
}

// ==================== 集合扩展 ====================

/**
 * 安全地获取列表中的元素
 */
fun <T> List<T>.safeGet(index: Int): T? {
    return if (index in 0 until size) this[index] else null
}

/**
 * 检查列表是否不为空且不为null
 */
fun <T> List<T>?.isNotNullOrEmpty(): Boolean {
    return this != null && this.isNotEmpty()
}

/**
 * 将列表分组为指定大小的块
 */
fun <T> List<T>.chunkedSafely(size: Int): List<List<T>> {
    return if (size <= 0) listOf(this) else this.chunked(size)
}