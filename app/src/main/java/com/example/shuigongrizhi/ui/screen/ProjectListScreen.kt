package com.example.shuigongrizhi.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.shuigongrizhi.R
import com.example.shuigongrizhi.data.entity.Project
import com.example.shuigongrizhi.data.entity.ProjectType
import com.example.shuigongrizhi.ui.components.GradientCard
import com.example.shuigongrizhi.ui.theme.*
import com.example.shuigongrizhi.ui.utils.ResponsiveUtils
import com.example.shuigongrizhi.ui.utils.getResponsivePadding
import com.example.shuigongrizhi.ui.components.ProjectTypeCard
import com.example.shuigongrizhi.ui.components.LoadingIndicator
import com.example.shuigongrizhi.ui.components.EmptyState
import com.example.shuigongrizhi.ui.theme.*
import com.example.shuigongrizhi.ui.utils.ResponsiveUtils
import com.example.shuigongrizhi.ui.utils.getResponsivePadding
import com.example.shuigongrizhi.ui.viewmodel.ProjectListViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.clickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectListScreen(
    viewModel: ProjectListViewModel,
    onNavigateToCreateProject: () -> Unit,
    onNavigateToProject: (Long) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val projects = uiState.data ?: emptyList()
    val isLoading = uiState.isLoading
    val error = uiState.error

    // ViewModel在init中已经自动加载项目列表，无需重复刷新
    
    // 监听项目列表变化，确保数据实时更新
    LaunchedEffect(projects) {
        // 当项目列表发生变化时，这里可以添加额外的处理逻辑
        // 例如日志记录或状态更新
    }

    // 错误处理
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // 显示错误信息
            viewModel.clearError()
        }
    }

    val responsivePadding = ResponsiveUtils.getResponsivePadding()
    
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.project_list),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings),
                            modifier = Modifier.size(IconSize.medium),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreateProject,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.create_project),
                    modifier = Modifier.size(IconSize.medium)
                )
            }
        }
    ) { paddingValues ->
        Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
            when {
                isLoading -> {
                    LoadingIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        message = "加载项目列表中..."
                    )
                }
                projects.isEmpty() -> {
                    EmptyState(
                        modifier = Modifier.align(Alignment.Center),
                        title = "暂无项目",
                        description = "点击右下角的 + 按钮创建您的第一个项目",
                        actionText = "创建项目",
                        onActionClick = onNavigateToCreateProject
                    )
                }
                else -> {
                    ProjectList(
                        projects = projects,
                        onProjectClick = onNavigateToProject,
                        onDeleteProject = viewModel::deleteProject
                    )
                }
            }
        }
    }
}



@Composable
fun ProjectList(
    projects: List<Project>,
    onProjectClick: (Long) -> Unit,
    onDeleteProject: (Project) -> Unit
) {
    val responsivePadding = ResponsiveUtils.getResponsivePadding()
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(responsivePadding),
        verticalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        items(projects) { project ->
            ProjectCard(
                project = project,
                onClick = { onProjectClick(project.id) },
                onDelete = { onDeleteProject(project) }
            )
        }
    }
}

@Composable
fun ProjectCard(
    project: Project,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    // 检查是否为默认项目
    val isDefaultProject = project.name == "淮工自营水利工程项目" && project.manager == "自营"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showMenu = true },
        colors = if (isDefaultProject) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        } else {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        },
        elevation = AppCardDefaults.elevation,
        shape = AppCardDefaults.shape
    ) {
        Column(modifier = Modifier.padding(ResponsiveUtils.getResponsivePadding())) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = project.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isDefaultProject) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = project.type.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDefaultProject) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "更多选项",
                            modifier = Modifier.size(IconSize.medium),
                            tint = if (isDefaultProject) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    "查看详情",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                ) 
                            },
                            onClick = {
                                showMenu = false
                                onClick()
                            }
                        )
                        if (!isDefaultProject) {
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        "删除项目",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.error
                                    ) 
                                },
                                onClick = {
                                    showMenu = false
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(Spacing.small))
            
            if (!project.manager.isNullOrBlank()) {
                Text(
                    text = "负责人：${project.manager}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDefaultProject) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = "${dateFormat.format(project.startDate)}" +
                    (project.endDate?.let { " - ${dateFormat.format(it)}" } ?: ""),
                style = MaterialTheme.typography.bodySmall,
                color = if (isDefaultProject) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (!project.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(Spacing.extraSmall))
                Text(
                    text = project.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDefaultProject) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
    DropdownMenu(
        expanded = showMenu,
        onDismissRequest = { showMenu = false }
    ) {
        DropdownMenuItem(
            text = { Text("进入详情") },
            onClick = {
                showMenu = false
                onClick()
            }
        )
        DropdownMenuItem(
            text = { Text("导出") },
            onClick = {
                showMenu = false
                // TODO: 导出操作，可在此处实现
            }
        )
        if (!isDefaultProject) {
            DropdownMenuItem(
                text = { Text("删除") },
                onClick = {
                    showMenu = false
                    showDeleteDialog = true
                }
            )
        }
    }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除项目") },
            text = { Text("确定要删除该项目吗？") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}