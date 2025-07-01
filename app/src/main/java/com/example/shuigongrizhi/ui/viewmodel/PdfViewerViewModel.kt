package com.example.shuigongrizhi.ui.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shuigongrizhi.data.model.PdfFileInfo
import com.example.shuigongrizhi.utils.PdfManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.io.File
import javax.inject.Inject

@HiltViewModel
class PdfViewerViewModel @Inject constructor() : ViewModel() {
    
    private val _pdfFiles = MutableStateFlow<List<File>>(emptyList())
    val pdfFiles: StateFlow<List<File>> = _pdfFiles.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    /**
     * 加载PDF文件列表
     */
    fun loadPdfFiles() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                // 这里需要通过依赖注入获取Context，暂时使用Application Context
                // 在实际使用时，可以通过Hilt注入Application Context
                // 或者在调用时传入Context
                
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = "加载PDF文件失败：${e.message}"
            }
        }
    }
    
    /**
     * 加载PDF文件列表（带Context参数）
     */
    fun loadPdfFiles(context: Context) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                val pdfManager = PdfManager(context)
                val files = pdfManager.getAllPdfFiles()
                _pdfFiles.value = files
                
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = "加载PDF文件失败：${e.message}"
            }
        }
    }
    
    /**
     * 打开PDF文件
     */
    fun openPdfFile(file: File, context: Context) {
        viewModelScope.launch {
            try {
                val pdfManager = PdfManager(context)
                val intent = pdfManager.openPdfFile(file)
                
                if (intent != null) {
                    context.startActivity(intent)
                } else {
                    Toast.makeText(
                        context,
                        "没有找到可以打开PDF的应用，请安装PDF阅读器",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "打开PDF文件失败：${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    /**
     * 分享PDF文件
     */
    fun sharePdfFile(file: File, context: Context) {
        viewModelScope.launch {
            try {
                val pdfManager = PdfManager(context)
                val intent = pdfManager.sharePdfFile(file)
                
                if (intent != null) {
                    val chooser = android.content.Intent.createChooser(intent, "分享PDF文件")
                    context.startActivity(chooser)
                } else {
                    Toast.makeText(
                        context,
                        "分享PDF文件失败",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "分享PDF文件失败：${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    /**
     * 删除PDF文件
     */
    fun deletePdfFile(file: File) {
        viewModelScope.launch {
            try {
                val success = file.delete()
                if (success) {
                    // 从列表中移除已删除的文件
                    _pdfFiles.value = _pdfFiles.value.filter { it.absolutePath != file.absolutePath }
                } else {
                    _errorMessage.value = "删除文件失败"
                }
            } catch (e: Exception) {
                _errorMessage.value = "删除文件失败：${e.message}"
            }
        }
    }
    
    /**
     * 根据项目名称筛选PDF文件
     */
    fun filterPdfFilesByProject(projectName: String, context: Context) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                val pdfManager = PdfManager(context)
                val files = if (projectName.isBlank()) {
                    pdfManager.getAllPdfFiles()
                } else {
                    pdfManager.getPdfFilesByProject(projectName)
                }
                _pdfFiles.value = files
                
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = "筛选PDF文件失败：${e.message}"
            }
        }
    }
    
    /**
     * 清除错误信息
     */
    fun clearError() {
        _errorMessage.value = null
    }
}