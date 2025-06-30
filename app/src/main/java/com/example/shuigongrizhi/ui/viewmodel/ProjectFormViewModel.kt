package com.example.shuigongrizhi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.example.shuigongrizhi.data.entity.Project
import com.example.shuigongrizhi.data.repository.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
            try {
                val project = if (editingProjectId != null) {
                    Project(
                        id = editingProjectId!!,
                        name = state.name,
                        type = state.type,
                        description = state.description.takeIf { it.isNotBlank() },
                        startDate = state.startDate!!,
                        endDate = state.endDate,
                        manager = state.manager.takeIf { it.isNotBlank() }
                    )
                } else {
                    Project(
                        name = state.name,
                        type = state.type,
                        description = state.description.takeIf { it.isNotBlank() },
                        startDate = state.startDate!!,
                        endDate = state.endDate,
                        manager = state.manager.takeIf { it.isNotBlank() }
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