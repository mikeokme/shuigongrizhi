package com.example.shuigongrizhi.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import com.example.shuigongrizhi.data.entity.ConstructionLog
import com.example.shuigongrizhi.data.entity.Project
import com.example.shuigongrizhi.data.entity.MediaFile
import com.example.shuigongrizhi.data.repository.ConstructionLogRepository
import com.example.shuigongrizhi.data.repository.ProjectRepository
import com.example.shuigongrizhi.data.repository.MediaFileRepository
import com.example.shuigongrizhi.utils.PdfGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File

data class LogDetailState(
    val constructionLog: ConstructionLog? = null,
    val project: Project? = null,
    val mediaFiles: List<MediaFile> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val pdfFile: File? = null,
    val isGeneratingPdf: Boolean = false
)

@HiltViewModel
class LogDetailViewModel @Inject constructor(
    private val constructionLogRepo: ConstructionLogRepository,
    private val projectRepo: ProjectRepository,
    private val mediaFileRepository: MediaFileRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private val _state = MutableStateFlow(LogDetailState())
    val state: StateFlow<LogDetailState> = _state.asStateFlow()
    
    private val pdfGenerator = PdfGenerator(context)
    
    fun loadLogDetail(logId: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val logDeferred = async { constructionLogRepo.getLogById(logId) }
                val mediaFilesDeferred = async { mediaFileRepository.getMediaFilesByLogId(logId).first() }

                val log = logDeferred.await() ?: run {
                    _state.value = _state.value.copy(isLoading = false, error = "施工日志不存在")
                    return@launch
                }

                val projectResult = projectRepo.getProjectById(log.projectId)
                val project = when (projectResult) {
                    is com.example.shuigongrizhi.core.Result.Success -> projectResult.data
                    is com.example.shuigongrizhi.core.Result.Error -> {
                        _state.value = _state.value.copy(isLoading = false, error = "加载项目信息失败: ${projectResult.exception.message}")
                        return@launch
                    }
                    is com.example.shuigongrizhi.core.Result.Loading -> return@launch // Should not happen if repository is implemented correctly
                }

                if (project == null) {
                    _state.value = _state.value.copy(isLoading = false, error = "项目信息不存在")
                    return@launch
                }

                val mediaFiles = mediaFilesDeferred.await()

                _state.value = _state.value.copy(
                    constructionLog = log,
                    project = project,
                    mediaFiles = mediaFiles,
                    isLoading = false
                )
                android.util.Log.d("LogDetail", "日志详情加载成功: ${log.id}")
            } catch (e: Exception) {
                android.util.Log.e("LogDetail", "加载日志详情失败", e)
                _state.value = _state.value.copy(isLoading = false, error = "加载失败: ${e.message}")
            }
        }
    }
    
    fun generatePdf() {
        val currentState = _state.value
        val log = currentState.constructionLog
        val project = currentState.project
        
        if (log == null || project == null) {
            _state.value = currentState.copy(error = "无法生成PDF：缺少必要信息")
            return
        }
        
        viewModelScope.launch {
            _state.value = currentState.copy(isGeneratingPdf = true, error = null)
            
            try {
                val pdfFile = pdfGenerator.generateDailyConstructionLog(
                    project = project,
                    constructionLog = log,
                    mediaFiles = currentState.mediaFiles
                )
                
                _state.value = currentState.copy(
                    pdfFile = pdfFile,
                    isGeneratingPdf = false
                )

                pdfFile?.let { android.util.Log.d("LogDetail", "PDF生成成功: ${it.absolutePath}") }
                
            } catch (e: Exception) {
                android.util.Log.e("LogDetail", "PDF生成失败", e)
                _state.value = currentState.copy(
                    isGeneratingPdf = false,
                    error = "PDF生成失败: ${e.message}"
                )
            }
        }
    }
    
    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
    
    fun clearPdfFile() {
        _state.value = _state.value.copy(pdfFile = null)
    }
    
    fun refreshData(logId: Long) {
        loadLogDetail(logId)
    }
}

