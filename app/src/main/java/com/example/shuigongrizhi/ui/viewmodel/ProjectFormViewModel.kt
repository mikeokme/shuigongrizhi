package com.example.shuigongrizhi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.example.shuigongrizhi.data.entity.Project
import com.example.shuigongrizhi.data.entity.ProjectStatus
import com.example.shuigongrizhi.data.repository.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

data class ProjectFormState(
    val name: String = "",
    val number: String = "",
    val constructionUnit: String = "",
    val supervisionUnit: String = "",
    val location: String = "",
    val projectType: String = "水库", // 默认为水库类型
    val startDate: Date? = null,
    val plannedCompletionDate: Date? = null,
    val status: ProjectStatus = ProjectStatus.ONGOING,
    val nameError: String? = null,
    val numberError: String? = null
)

@HiltViewModel
class ProjectFormViewModel @Inject constructor(private val projectRepository: ProjectRepository) : ViewModel() {
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
                val project = projectRepository.getProjectById(projectId)
                project?.let {
                    editingProjectId = it.id
                    _formState.value = ProjectFormState(
                        name = it.name,
                        number = it.number,
                        constructionUnit = it.constructionUnit,
                        supervisionUnit = it.supervisionUnit,
                        location = it.location,
                        projectType = it.projectType,
                        startDate = it.startDate,
                        plannedCompletionDate = it.plannedCompletionDate,
                        status = it.status
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

    fun updateNumber(number: String) {
        _formState.value = _formState.value.copy(
            number = number,
            numberError = if (number.isBlank()) "项目编号不能为空" else null
        )
    }

    fun updateConstructionUnit(unit: String) {
        _formState.value = _formState.value.copy(constructionUnit = unit)
    }

    fun updateSupervisionUnit(unit: String) {
        _formState.value = _formState.value.copy(supervisionUnit = unit)
    }

    fun updateLocation(location: String) {
        _formState.value = _formState.value.copy(location = location)
    }

    fun updateStartDate(date: Date?) {
        _formState.value = _formState.value.copy(startDate = date)
    }

    fun updatePlannedCompletionDate(date: Date?) {
        _formState.value = _formState.value.copy(plannedCompletionDate = date)
    }

    fun updateStatus(status: ProjectStatus) {
        _formState.value = _formState.value.copy(status = status)
    }
    
    fun updateProjectType(type: String) {
        _formState.value = _formState.value.copy(projectType = type)
    }

    fun saveProject() {
        val state = _formState.value
        
        // 验证必填字段
        val nameError = if (state.name.isBlank()) "项目名称不能为空" else null
        val numberError = if (state.number.isBlank()) "项目编号不能为空" else null
        
        if (nameError != null || numberError != null) {
            _formState.value = state.copy(
                nameError = nameError,
                numberError = numberError
            )
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val project = if (editingProjectId != null) {
                    // 更新现有项目
                    Project(
                        id = editingProjectId!!,
                        name = state.name,
                        number = state.number,
                        constructionUnit = state.constructionUnit,
                        supervisionUnit = state.supervisionUnit,
                        location = state.location,
                        projectType = state.projectType,
                        startDate = state.startDate,
                        plannedCompletionDate = state.plannedCompletionDate,
                        status = state.status,
                        updatedAt = Date()
                    )
                } else {
                    // 创建新项目
                    Project(
                        name = state.name,
                        number = state.number,
                        constructionUnit = state.constructionUnit,
                        supervisionUnit = state.supervisionUnit,
                        location = state.location,
                        projectType = state.projectType,
                        startDate = state.startDate,
                        plannedCompletionDate = state.plannedCompletionDate,
                        status = state.status
                    )
                }

                if (editingProjectId != null) {
                    projectRepository.updateProject(project)
                } else {
                    projectRepository.insertProject(project)
                }
                
                _saveResult.value = true
            } catch (e: Exception) {
                _error.value = e.message
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