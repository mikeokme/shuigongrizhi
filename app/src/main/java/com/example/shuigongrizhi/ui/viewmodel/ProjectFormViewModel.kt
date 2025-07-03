package com.example.shuigongrizhi.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shuigongrizhi.core.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import com.example.shuigongrizhi.data.entity.Project
import com.example.shuigongrizhi.data.repository.ProjectRepository
import com.example.shuigongrizhi.data.repository.ConstructionLogRepository
import com.example.shuigongrizhi.utils.ProjectDataManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Date
import com.example.shuigongrizhi.data.entity.ProjectType
import com.example.shuigongrizhi.data.entity.ConstructionLog

data class ProjectFormState(
    val name: String = "",
    val type: ProjectType = ProjectType.水利,
    val description: String = "",
    val startDate: Date? = null,
    val endDate: Date? = null,
    val manager: String = "",
    val nameError: String? = null,
    val startDateError: String? = null
)

@HiltViewModel
class ProjectFormViewModel @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val constructionLogRepository: ConstructionLogRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private val projectDataManager = ProjectDataManager(context)
    private val _formState = MutableStateFlow(ProjectFormState())
    val formState: StateFlow<ProjectFormState> = _formState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _saveResult = MutableStateFlow<Boolean?>(null)
    val saveResult: StateFlow<Boolean?> = _saveResult.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var editingProjectId: Long? = null

    fun loadProject(projectId: Long) {
        viewModelScope.launch {
            try {
                val projectResult = projectRepository.getProjectById(projectId)
                if (projectResult is Result.Success<*> && projectResult.data is Project) {
                    val project = projectResult.data as Project
                    editingProjectId = project.id
                    _formState.value = ProjectFormState(
                        name = project.name,
                        type = project.type,
                        description = project.description ?: "",
                        startDate = project.startDate,
                        endDate = project.endDate,
                        manager = project.manager ?: ""
                    )
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun updateName(name: String) {
        _formState.value = _formState.value.copy(
            name = name,
            nameError = if (name.isBlank()) "项目名称不能为空" else null
        )
    }

    fun updateType(type: ProjectType) {
        _formState.value = _formState.value.copy(type = type)
    }

    fun updateDescription(desc: String) {
        _formState.value = _formState.value.copy(description = desc)
    }

    fun updateStartDate(date: Date?) {
        _formState.value = _formState.value.copy(startDate = date)
    }

    fun updateEndDate(date: Date?) {
        _formState.value = _formState.value.copy(endDate = date)
    }

    fun updateManager(manager: String) {
        _formState.value = _formState.value.copy(manager = manager)
    }

    fun saveProject() {
        val state = _formState.value
        
        // 清除之前的错误
        _formState.value = state.copy(
            nameError = null,
            startDateError = null
        )
        
        // 校验必填字段
        val nameError = if (state.name.isBlank()) "项目名称不能为空" else null
        val startDateError = if (state.startDate == null) "请选择开始日期" else null
        
        if (nameError != null || startDateError != null) {
            _formState.value = state.copy(
                nameError = nameError,
                startDateError = startDateError
            )
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val project = if (editingProjectId != null) {
                    Project(
                        id = editingProjectId!!,
                        name = state.name.trim(),
                        type = state.type,
                        description = state.description.trim().takeIf { it.isNotBlank() },
                        startDate = state.startDate!!,
                        endDate = state.endDate,
                        manager = state.manager.trim().takeIf { it.isNotBlank() }
                    )
                } else {
                    Project(
                        name = state.name.trim(),
                        type = state.type,
                        description = state.description.trim().takeIf { it.isNotBlank() },
                        startDate = state.startDate!!,
                        endDate = state.endDate,
                        manager = state.manager.trim().takeIf { it.isNotBlank() }
                    )
                }
                
                val result = if (editingProjectId != null) {
                    projectRepository.updateProject(project)
                } else {
                    projectRepository.insertProject(project)
                }

                if (result is Result.Success<*> && result.data is Long) {
                    android.util.Log.d("ProjectForm", "Save operation completed successfully")
                    triggerAutoBackup(project)
                    _saveResult.value = true
                } else {
                    val errorMessage = when (result) {
                        is Result.Error -> result.exception.message ?: "未知错误"
                        else -> "未知错误"
                    }
                    android.util.Log.e("ProjectForm", "Save failed: $errorMessage")
                    _error.value = "保存失败: $errorMessage"
                    _saveResult.value = false
                }
            } catch (e: Exception) {
                _error.value = e.message
                _saveResult.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun triggerAutoBackup(project: Project) {
        viewModelScope.launch {
            try {
                val projectToBackup = if (project.id == 0L) {
                    // For new projects, we need to fetch it again to get the generated ID
                    val result = projectRepository.getAllProjects().first()
                    if (result is Result.Success<*> && result.data is List<*> && (result.data as List<*>).all { it is Project }) {
                        (result.data as List<Project>).find { it.name == project.name && it.startDate == project.startDate }
                    } else {
                        null
                    }
                } else {
                    project
                }

                projectToBackup?.let { proj ->
                    val logsResult = constructionLogRepository.getLogsByProjectId(proj.id).first()
                    if (logsResult is Result.Success<*> && logsResult.data is List<*> && (logsResult.data as List<*>).all { it is ConstructionLog }) {
                        val backupSuccess = projectDataManager.autoBackupProject(proj, logsResult.data as List<ConstructionLog>)
                        if (backupSuccess) {
                            android.util.Log.d("ProjectForm", "项目数据自动备份成功")
                        } else {
                            android.util.Log.w("ProjectForm", "项目数据自动备份失败")
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ProjectForm", "自动备份过程中发生错误", e)
            }
        }
    }

    fun clearSaveResult() {
        _saveResult.value = null
    }

    fun clearError() {
        _error.value = null
    }
}