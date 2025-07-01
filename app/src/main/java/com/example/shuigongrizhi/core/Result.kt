package com.example.shuigongrizhi.core

/**
 * 通用结果包装类，用于处理成功和错误状态
 * 遵循Google推荐的错误处理模式
 */
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

/**
 * 扩展函数：检查是否成功
 */
fun <T> Result<T>.isSuccess(): Boolean = this is Result.Success

/**
 * 扩展函数：检查是否失败
 */
fun <T> Result<T>.isError(): Boolean = this is Result.Error

/**
 * 扩展函数：检查是否加载中
 */
fun <T> Result<T>.isLoading(): Boolean = this is Result.Loading

/**
 * 扩展函数：获取数据（如果成功）
 */
fun <T> Result<T>.getDataOrNull(): T? {
    return if (this is Result.Success) data else null
}

/**
 * 扩展函数：获取错误（如果失败）
 */
fun <T> Result<T>.getErrorOrNull(): Throwable? {
    return if (this is Result.Error) exception else null
}

/**
 * 扩展函数：映射成功结果
 */
inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> {
    return when (this) {
        is Result.Success -> Result.Success(transform(data))
        is Result.Error -> this
        is Result.Loading -> this
    }
}

/**
 * 扩展函数：在成功时执行操作
 */
inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) {
        action(data)
    }
    return this
}

/**
 * 扩展函数：在失败时执行操作
 */
inline fun <T> Result<T>.onError(action: (Throwable) -> Unit): Result<T> {
    if (this is Result.Error) {
        action(exception)
    }
    return this
}

/**
 * 扩展函数：在加载时执行操作
 */
inline fun <T> Result<T>.onLoading(action: () -> Unit): Result<T> {
    if (this is Result.Loading) {
        action()
    }
    return this
}