package com.example.shuigongrizhi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.example.shuigongrizhi.data.entity.ConstructionLog
import com.example.shuigongrizhi.data.entity.Project
import com.example.shuigongrizhi.data.repository.ConstructionLogRepository
import com.example.shuigongrizhi.data.repository.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class DashboardState(
    val project: Project? = null,
    val logs: List<ConstructionLog> = emptyList(),
    val selectedDate: Date = Date(),
    val selectedLog: ConstructionLog? = null,
    val currentMonth: Date = Date(),
    val logDates: Set<String> = emptySet()
)

@HiltViewModel
class ProjectDashboardViewModel @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val constructionLogRepository: ConstructionLogRepository
) : ViewModel() {
    
    private val _dashboardState = MutableStateFlow(DashboardState())
    val dashboardState: StateFlow<DashboardState> = _dashboardState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private var projectId: Long = 0

    fun loadProject(id: Long) {
        projectId = id
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val projectResult = projectRepository.getProjectById(id)
                val project = (projectResult as? com.example.shuigongrizhi.core.Result.Success)?.data
                project?.let {
                    _dashboardState.value = _dashboardState.value.copy(project = it)
                    loadLogs()
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadLogs() {
        viewModelScope.launch {
            try {
                constructionLogRepository.getLogsByProjectId(projectId).collect { logs ->
                    val logDates = logs.map { dateFormat.format(it.date) }.toSet()
                    val selectedLog = logs.find { 
                        dateFormat.format(it.date) == dateFormat.format(_dashboardState.value.selectedDate) 
                    }
                    
                    _dashboardState.value = _dashboardState.value.copy(
                        logs = logs,
                        logDates = logDates,
                        selectedLog = selectedLog
                    )
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun selectDate(date: Date) {
        val selectedLog = _dashboardState.value.logs.find { 
            dateFormat.format(it.date) == dateFormat.format(date) 
        }
        
        _dashboardState.value = _dashboardState.value.copy(
            selectedDate = date,
            selectedLog = selectedLog
        )
    }

    fun changeMonth(month: Date) {
        _dashboardState.value = _dashboardState.value.copy(currentMonth = month)
    }

    fun hasLogForDate(date: Date): Boolean {
        return _dashboardState.value.logDates.contains(dateFormat.format(date))
    }

    fun getSelectedDateString(): String {
        return SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
            .format(_dashboardState.value.selectedDate)
    }

    fun clearError() {
        _error.value = null
    }
}