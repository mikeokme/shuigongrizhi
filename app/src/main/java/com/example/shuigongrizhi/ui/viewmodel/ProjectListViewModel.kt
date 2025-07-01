package com.example.shuigongrizhi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.example.shuigongrizhi.data.entity.Project
import com.example.shuigongrizhi.data.entity.ProjectType
import com.example.shuigongrizhi.data.entity.ConstructionLog
import com.example.shuigongrizhi.data.repository.ProjectRepository
import com.example.shuigongrizhi.data.repository.ConstructionLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Calendar

@HiltViewModel
class ProjectListViewModel @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val constructionLogRepository: ConstructionLogRepository
) : ViewModel() {
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
                projectRepository.getAllProjects().collect { projectList ->
                    android.util.Log.d("ProjectList", "收到项目列表更新，数量: ${projectList.size}")
                    
                    // 如果没有项目，创建默认项目
                    if (projectList.isEmpty()) {
                        createDefaultProject()
                    } else {
                        _projects.value = projectList
                        _isLoading.value = false
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }

    fun deleteProject(project: Project) {
        viewModelScope.launch {
            try {
                // 检查是否为默认项目，如果是则不允许删除
                if (project.name == "淮工自营水利工程项目" && project.manager == "自营") {
                    _error.value = "默认项目不能删除"
                    return@launch
                }
                
                projectRepository.deleteProject(project)
                android.util.Log.d("ProjectList", "项目删除成功: ${project.name}")
            } catch (e: Exception) {
                _error.value = e.message
                android.util.Log.e("ProjectList", "删除项目失败", e)
            }
        }
    }

    fun refreshProjects() {
        android.util.Log.d("ProjectList", "开始刷新项目列表")
        loadProjects()
    }

    fun clearError() {
        _error.value = null
    }

    private suspend fun createDefaultProject() {
        try {
            android.util.Log.d("ProjectList", "创建默认项目")
            
            // 检查是否已存在默认项目
            val existingProjects = projectRepository.getAllProjects().first()
            val defaultProjectExists = existingProjects.any { project -> 
                project.name == "淮工自营水利工程项目" && project.manager == "自营" 
            }
            
            if (defaultProjectExists) {
                android.util.Log.d("ProjectList", "默认项目已存在，跳过创建")
                _isLoading.value = false
                return
            }
            
            // 创建默认项目的开始日期（当前日期）
            val startDate = Date()
            
            // 创建默认项目的结束日期（2099年12月31日）
            val endCalendar = Calendar.getInstance()
            endCalendar.set(2099, Calendar.DECEMBER, 31, 23, 59, 59)
            val endDate = endCalendar.time
            
            val defaultProject = Project(
                name = "淮工自营水利工程项目",
                type = ProjectType.水利,
                description = "淮工集团自营水利工程项目，用于日常施工日志记录和管理。",
                startDate = startDate,
                endDate = endDate,
                manager = "自营"
            )
            
            val projectId = projectRepository.insertProject(defaultProject)
            android.util.Log.d("ProjectList", "默认项目创建成功，ID: $projectId")
            
            // 创建默认施工日志
            createDefaultConstructionLog(projectId)
            
        } catch (e: Exception) {
            android.util.Log.e("ProjectList", "创建默认项目失败: ${e.message}")
            _error.value = "创建默认项目失败: ${e.message}"
            _isLoading.value = false
        }
    }

    private suspend fun createDefaultConstructionLog(projectId: Long) {
        try {
            android.util.Log.d("ProjectList", "为项目 $projectId 创建默认施工日志")
            
            val currentDate = Date()
            
            val defaultLog = ConstructionLog(
                projectId = projectId,
                date = currentDate,
                weatherCondition = "晴",
                temperature = "适宜",
                wind = "微风",
                constructionSite = "淮工自营水利工程施工现场",
                mainContent = "项目初始化，开始施工准备工作。检查施工设备、材料到位情况，确保施工安全。",
                personnelEquipment = "施工人员到位，设备检查完毕，材料准备充足。",
                qualityManagement = "严格按照水利工程质量标准执行，做好质量控制和检查记录。",
                safetyManagement = "落实安全生产责任制，确保施工现场安全，做好安全防护措施。",
                mediaFiles = emptyList(),
                createdAt = currentDate,
                updatedAt = currentDate
            )
            
            val logId = constructionLogRepository.insertLog(defaultLog)
            android.util.Log.d("ProjectList", "默认施工日志创建成功，ID: $logId")
            
        } catch (e: Exception) {
            android.util.Log.e("ProjectList", "创建默认施工日志失败: ${e.message}")
            // 不设置错误状态，因为项目已经创建成功
        }
    }
    
    /**
     * 检查是否为默认项目
     */
    fun isDefaultProject(project: Project): Boolean {
        return project.name == "淮工自营水利工程项目" && project.manager == "自营"
    }
}