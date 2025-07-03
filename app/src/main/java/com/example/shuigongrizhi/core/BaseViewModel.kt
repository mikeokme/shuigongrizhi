package com.example.shuigongrizhi.core

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 基础ViewModel类，提供通用的错误处理和状态管理
 * 遵循Google推荐的ViewModel最佳实践
 */
abstract class BaseViewModel : ViewModel() {
    
    // 错误事件流
    private val _errorEvents = MutableSharedFlow<String>()
    val errorEvents: SharedFlow<String> = _errorEvents.asSharedFlow()
    
    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // 全局异常处理器
    protected val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        handleError(throwable)
    }
    
    /**
     * 处理错误
     */
    protected open fun handleError(throwable: Throwable) {
        val errorMessage = ExceptionHandler().generateUserMessage(throwable)
        
        viewModelScope.launch {
            _errorEvents.emit(errorMessage)
            _isLoading.value = false
        }
    }
    
    /**
     * 设置加载状态
     */
    protected fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }
    
    /**
     * 发送错误事件
     */
    protected fun sendError(message: String) {
        viewModelScope.launch {
            _errorEvents.emit(message)
        }
    }
    
    /**
     * 安全执行协程操作
     */
    protected fun launchSafely(
        showLoading: Boolean = true,
        block: suspend () -> Unit
    ) {
        viewModelScope.launch(exceptionHandler) {
            if (showLoading) setLoading(true)
            try {
                block()
            } finally {
                if (showLoading) setLoading(false)
            }
        }
    }
}