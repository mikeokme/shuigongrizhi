package com.example.shuigongrizhi.core

/**
 * UI状态管理类，遵循Google推荐的单一数据源原则
 * 用于在ViewModel和UI之间传递状态信息
 */
data class UiState<out T>(
    val isLoading: Boolean = false,
    val data: T? = null,
    val error: String? = null,
    val isRefreshing: Boolean = false
) {
    /**
     * 检查是否有数据
     */
    fun hasData(): Boolean = data != null
    
    /**
     * 检查是否有错误
     */
    fun hasError(): Boolean = error != null
    
    /**
     * 检查是否为空状态（无数据且无加载）
     */
    fun isEmpty(): Boolean = !isLoading && !hasData() && !hasError()
    
    /**
     * 检查是否为成功状态（有数据且无错误）
     */
    fun isSuccess(): Boolean = hasData() && !hasError()
    
    companion object {
        /**
         * 创建加载状态
         */
        fun <T> loading(): UiState<T> = UiState(isLoading = true)
        
        /**
         * 创建成功状态
         */
        fun <T> success(data: T): UiState<T> = UiState(data = data)
        
        /**
         * 创建错误状态
         */
        fun <T> error(message: String): UiState<T> = UiState(error = message)
        
        /**
         * 创建刷新状态
         */
        fun <T> refreshing(currentData: T? = null): UiState<T> = 
            UiState(isRefreshing = true, data = currentData)
        
        /**
         * 创建空状态
         */
        fun <T> empty(): UiState<T> = UiState()
    }
}

/**
 * 扩展函数：从Result转换为UiState
 */
fun <T> Result<T>.toUiState(): UiState<T> {
    return when (this) {
        is Result.Loading -> UiState.loading()
        is Result.Success -> UiState.success(data)
        is Result.Error -> UiState.error(exception.message ?: "未知错误")
    }
}

/**
 * 扩展函数：映射UiState的数据
 */
inline fun <T, R> UiState<T>.mapData(transform: (T) -> R): UiState<R> {
    return UiState(
        isLoading = isLoading,
        data = data?.let(transform),
        error = error,
        isRefreshing = isRefreshing
    )
}