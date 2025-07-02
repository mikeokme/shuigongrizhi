package com.example.shuigongrizhi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// import dagger.hilt.android.lifecycle.HiltViewModel // 临时禁用
// import javax.inject.Inject // 临时禁用
import com.example.shuigongrizhi.data.entity.Project
import com.example.shuigongrizhi.data.repository.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// @HiltViewModel // 临时禁用
class ProjectSelectionViewModel /* @Inject constructor(
    private val projectRepository: ProjectRepository
) */ : ViewModel() {
    
    // 临时直接实例化依赖
    private val projectRepository = ProjectRepository()
    
    private val _selectedProject = MutableStateFlow<Project?>(null)
    val selectedProject: StateFlow<Project?> = _selectedProject.asStateFlow()
    
    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        loadProjects()
    }
    
    private fun loadProjects() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                projectRepository.getAllProjects().collect { result ->
                    if (result is com.example.shuigongrizhi.core.Result.Success) {
                        _projects.value = result.data
                    } else if (result is com.example.shuigongrizhi.core.Result.Error) {
                        _error.value = result.exception.message ?: "加载项目列表失败"
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "加载项目列表失败"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun selectProject(project: Project) {
        _selectedProject.value = project
    }
    
    fun clearSelection() {
        _selectedProject.value = null
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun refreshProjects() {
        loadProjects()
    }
}