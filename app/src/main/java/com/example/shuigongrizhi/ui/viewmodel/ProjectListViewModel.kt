package com.example.shuigongrizhi.ui.viewmodel

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.example.shuigongrizhi.core.BaseViewModel
import com.example.shuigongrizhi.core.Constants
import com.example.shuigongrizhi.core.Logger
import com.example.shuigongrizhi.core.Result
import com.example.shuigongrizhi.core.UiState
import com.example.shuigongrizhi.core.ValidationException
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

/**
 * 项目列表ViewModel
 * 遵循MVVM架构模式，使用UiState管理UI状态
 */
@HiltViewModel
class ProjectListViewModel @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val constructionLogRepository: ConstructionLogRepository
) : BaseViewModel() {
    // UI状态管理
    private val _uiState = MutableStateFlow(UiState<List<Project>>())
    val uiState: StateFlow<UiState<List<Project>>> = _uiState.asStateFlow()
    
    // 向后兼容的属性
    val projects: StateFlow<List<Project>> = _uiState.asStateFlow().let { stateFlow ->
        MutableStateFlow(emptyList<Project>()).apply {
            viewModelScope.launch {
                stateFlow.collect { state ->
                    value = state.data ?: emptyList()
                }
            }
        }.asStateFlow()
    }

    init {
        loadProjects()
    }

    private fun loadProjects() {
        launchSafely {
            _uiState.value = UiState.loading()
            
            projectRepository.getAllProjects().collect { result ->
                when (result) {
                    is Result.Success -> {
                        val projectList = result.data
                        Logger.business("收到项目列表更新，数量: ${projectList.size}")
                        
                        // 如果没有项目，创建默认项目
                        if (projectList.isEmpty()) {
                            createDefaultProject()
                        } else {
                            _uiState.value = UiState.success(projectList)
                        }
                    }
                    is Result.Error -> {
                        _uiState.value = UiState.error(result.exception.message ?: "加载项目失败")
                        Logger.exception(result.exception, "加载项目失败")
                    }
                    is Result.Loading -> {
                        _uiState.value = UiState.loading()
                    }
                }
            }
        }
    }

    fun deleteProject(project: Project) {
        launchSafely(showLoading = false) {
            // 检查是否为默认项目，如果是则不允许删除
            if (isDefaultProject(project)) {
                Logger.business("尝试删除默认项目，操作被阻止")
                sendError("默认项目不能删除")
                return@launchSafely
            }
            
            Logger.business("删除项目: ${project.name}")
            
            when (val result = projectRepository.deleteProject(project)) {
                is Result.Success -> {
                    Logger.business("项目删除成功: ${project.name}")
                    // 删除成功后刷新列表
                    refreshProjects()
                }
                is Result.Error -> {
                    Logger.exception(result.exception, "删除项目失败")
                    handleError(result.exception)
                }
                is Result.Loading -> {
                    // 通常不会到达这里
                }
            }
        }
    }

    /**
     * 刷新项目列表
     */
    fun refreshProjects() {
        Logger.business("开始刷新项目列表")
        loadProjects()
    }

    /**
     * 清除错误状态
     */
    fun clearError() {
        val currentState = _uiState.value
        if (currentState.hasError()) {
            _uiState.value = currentState.copy(error = null)
        }
    }

    private suspend fun createDefaultProject() {
        Logger.business("创建默认项目")
        
        // 检查是否已存在默认项目
        when (val result = projectRepository.getAllProjects().first()) {
            is Result.Success -> {
                val existingProjects = result.data
                val defaultProjectExists = existingProjects.any { project -> 
                    isDefaultProject(project)
                }
                
                if (defaultProjectExists) {
                    Logger.business("默认项目已存在，跳过创建")
                    _uiState.value = UiState.success(existingProjects)
                    return
                }
            }
            is Result.Error -> {
                Logger.exception(result.exception, "检查现有项目失败")
                _uiState.value = UiState.error("检查现有项目失败")
                return
            }
            is Result.Loading -> {
                // 继续执行
            }
        }
        
        // 创建默认项目
        val defaultProject = createDefaultProjectEntity()
        
        when (val insertResult = projectRepository.insertProject(defaultProject)) {
            is Result.Success -> {
                val projectId = insertResult.data
                Logger.business("默认项目创建成功，ID: $projectId")
                
                // 创建默认施工日志
                createDefaultConstructionLog(projectId)
                
                // 重新加载项目列表
                loadProjects()
            }
            is Result.Error -> {
                Logger.exception(insertResult.exception, "创建默认项目失败")
                _uiState.value = UiState.error("创建默认项目失败: ${insertResult.exception.message}")
            }
            is Result.Loading -> {
                // 通常不会到达这里
            }
        }
    }
    
    /**
     * 创建默认项目实体
     */
    private fun createDefaultProjectEntity(): Project {
        // 创建默认项目的开始日期（当前日期）
        val startDate = Date()
        
        // 创建默认项目的结束日期（2099年12月31日）
        val endCalendar = Calendar.getInstance()
        endCalendar.set(2099, Calendar.DECEMBER, 31, 23, 59, 59)
        val endDate = endCalendar.time
        
        return Project(
            name = Constants.Defaults.PROJECT_NAME,
            type = ProjectType.水利,
            description = Constants.Defaults.PROJECT_DESCRIPTION,
            startDate = startDate,
            endDate = endDate,
            manager = Constants.Defaults.PROJECT_MANAGER
        )
    }

    private suspend fun createDefaultConstructionLog(projectId: Long) {
        try {
            Logger.business("为项目 $projectId 创建默认施工日志")
            
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
            Logger.business("默认施工日志创建成功，ID: $logId")
            
        } catch (e: Exception) {
            Logger.exception(e, "创建默认施工日志失败")
            // 不设置错误状态，因为项目已经创建成功
        }
    }
    
    /**
     * 检查是否为默认项目
     */
    fun isDefaultProject(project: Project): Boolean {
        return project.name == Constants.Defaults.PROJECT_NAME && project.manager == Constants.Defaults.PROJECT_MANAGER
    }
}