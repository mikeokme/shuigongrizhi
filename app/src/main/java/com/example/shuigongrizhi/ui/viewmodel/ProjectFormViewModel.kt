package com.example.shuigongrizhi.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
                val project = (projectResult as? com.example.shuigongrizhi.core.Result.Success)?.data
                project?.let {
                    editingProjectId = it.id
                    _formState.value = ProjectFormState(
                        name = it.name,
                        type = it.type,
                        description = it.description ?: "",
                        startDate = it.startDate,
                        endDate = it.endDate,
                        manager = it.manager ?: ""
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
                    android.util.Log.d("ProjectForm", "Project updated successfully: ${project.name}")
                    "项目更新成功"
                } else {
                    val projectId = projectRepository.insertProject(project)
                    android.util.Log.d("ProjectForm", "Project created with ID: $projectId, name: ${project.name}")
                    "项目创建成功，ID: $projectId"
                }
                
                // 验证保存是否成功
                kotlinx.coroutines.delay(100) // 给数据库操作一些时间
                android.util.Log.d("ProjectForm", "Save operation completed: $result")
                
                // 自动备份项目数据
                try {
                    val savedProject = if (editingProjectId != null) {
                        val result = projectRepository.getProjectById(editingProjectId!!)
                        (result as? com.example.shuigongrizhi.core.Result.Success)?.data
                    } else {
                        // 获取刚创建的项目
                        val allProjectsResult = projectRepository.getAllProjects().first()
                        (allProjectsResult as? com.example.shuigongrizhi.core.Result.Success)?.data
                            ?.find { it.name == project.name && it.type == project.type }
                    }
                    
                    savedProject?.let { proj ->
                        // 获取项目相关的日志
                        val logs = constructionLogRepository.getLogsByProjectId(proj.id).first()
                        
                        // 执行自动备份
                        val backupSuccess = projectDataManager.autoBackupProject(proj, logs)
                        if (backupSuccess) {
                            android.util.Log.d("ProjectForm", "项目数据自动备份成功")
                        } else {
                            android.util.Log.w("ProjectForm", "项目数据自动备份失败")
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("ProjectForm", "自动备份过程中发生错误", e)
                }
                
                // 保存成功
                android.util.Log.d("ProjectForm", "项目保存成功，ID: $result")
                _saveResult.value = true
                
            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("UNIQUE constraint failed") == true -> "项目名称已存在，请使用其他名称"
                    e.message?.contains("database") == true -> "数据库操作失败，请重试"
                    else -> e.message ?: "保存项目时发生未知错误"
                }
                _error.value = errorMessage
                _saveResult.value = false
            } finally {
                _isLoading.value = false
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