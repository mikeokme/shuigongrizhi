package com.example.shuigongrizhi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shuigongrizhi.core.AppConfig
import com.example.shuigongrizhi.data.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WeatherSettingsUiState(
    val isLoading: Boolean = false,
    val currentToken: String = "",
    val isTokenVerified: Boolean = false,
    val errorMessage: String? = null,
    val saveResult: Result<Unit>? = null,
    val testResult: Result<String>? = null
)

@HiltViewModel
class WeatherSettingsViewModel @Inject constructor(
    private val appConfig: AppConfig,
    private val weatherRepository: WeatherRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(WeatherSettingsUiState())
    val uiState: StateFlow<WeatherSettingsUiState> = _uiState.asStateFlow()
    
    // 开发者Token和验证码
    private val developerToken = "6MzHC6Wp0Fs5DhAz"
    private val verificationCode = "nzdwssm"
    
    init {
        loadCurrentSettings()
    }
    
    private fun loadCurrentSettings() {
        _uiState.value = _uiState.value.copy(
            currentToken = appConfig?.weatherApiToken ?: "",
            isTokenVerified = appConfig?.isWeatherTokenVerified ?: false
        )
    }
    
    /**
     * 获取当前Token
     */
    fun getCurrentToken(): String {
        return appConfig?.weatherApiToken ?: ""
    }
    
    /**
     * 保存自定义Token
     */
    fun saveCustomToken(token: String) {
        // 允许空token以启用无API模式
        if (token.isBlank()) {
            // 保存为无API模式
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = null
                )
                
                try {
                    // 清除Token，启用无API模式
                    appConfig?.weatherApiToken = ""
                    appConfig?.isWeatherTokenVerified = false
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentToken = "",
                        isTokenVerified = false,
                        saveResult = Result.success(Unit),
                        errorMessage = "✅ 已切换到无API模式，将使用模拟天气数据"
                    )
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "❌ 保存失败: ${e.message}"
                    )
                }
            }
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )
            
            try {
                // 测试Token是否有效
                val testResult = weatherRepository?.testToken(token) ?: Result.failure(Exception("天气仓库未初始化"))
                
                if (testResult.isSuccess) {
                    // 保存Token
                    appConfig?.weatherApiToken = token
                    appConfig?.isWeatherTokenVerified = true
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentToken = token,
                        isTokenVerified = true,
                        saveResult = Result.success(Unit),
                        errorMessage = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Token验证失败: ${testResult.exceptionOrNull()?.message ?: "未知错误"}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "保存失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 使用验证码保存开发者Token
     */
    fun saveTokenWithVerification(inputCode: String) {
        if (inputCode.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "验证码不能为空"
            )
            return
        }
        
        if (inputCode != verificationCode) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "验证码错误，请联系开发者获取正确的验证码"
            )
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )
            
            try {
                // 测试开发者Token
                val testResult = weatherRepository?.testToken(developerToken) ?: Result.failure(Exception("天气仓库未初始化"))
                
                if (testResult.isSuccess) {
                    // 保存开发者Token
                appConfig?.weatherApiToken = developerToken
                appConfig?.isWeatherTokenVerified = true
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentToken = developerToken,
                        isTokenVerified = true,
                        saveResult = Result.success(Unit),
                        errorMessage = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "开发者Token验证失败: ${testResult.exceptionOrNull()?.message ?: "未知错误"}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "保存失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 测试Token
     */
    fun testToken(token: String) {
        // 允许空token以测试无API模式
        if (token.isBlank()) {
            // 测试无API模式
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = null
                )
                
                try {
                    val result = weatherRepository?.testToken("") ?: Result.failure(Exception("天气仓库未初始化"))
                    
                    _uiState.value = if (result.isSuccess) {
                        _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "✅ ${result.getOrNull()}"
                        )
                    } else {
                        _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "❌ ${result.exceptionOrNull()?.message ?: "测试失败"}"
                        )
                    }
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "❌ 测试失败: ${e.message}"
                    )
                }
            }
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )
            
            try {
                val result = weatherRepository.testToken(token)
                
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        testResult = Result.success("Token测试成功！可以正常获取天气数据。"),
                        errorMessage = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Token测试失败: ${result.exceptionOrNull()?.message ?: "未知错误"}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "测试失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 重置为默认Token
     */
    fun resetToken() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )
            
            try {
                // 清除保存的Token
                appConfig?.weatherApiToken = ""
                appConfig?.isWeatherTokenVerified = false
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentToken = "",
                    isTokenVerified = false,
                    saveResult = Result.success(Unit),
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "重置失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 清除错误信息
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    /**
     * 清除保存结果
     */
    fun clearSaveResult() {
        _uiState.value = _uiState.value.copy(saveResult = null)
    }
    
    /**
     * 清除测试结果
     */
    fun clearTestResult() {
        _uiState.value = _uiState.value.copy(testResult = null)
    }
}