package com.example.shuigongrizhi.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.example.shuigongrizhi.data.entity.ProjectStatus
import com.example.shuigongrizhi.ui.component.GradientCard
import com.example.shuigongrizhi.ui.component.ProjectTypeCard
import com.example.shuigongrizhi.ui.theme.DeepPurple
import com.example.shuigongrizhi.ui.theme.TextWhite
import com.example.shuigongrizhi.ui.viewmodel.ProjectListViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectListScreen(
    viewModel: ProjectListViewModel,
    onNavigateToCreateProject: () -> Unit,
    onNavigateToProject: (Long) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val projects by viewModel.projects.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // 错误处理
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // 可以在这里显示Snackbar或其他错误提示
            viewModel.clearError()
        }
    }

    Scaffold(
        containerColor = DeepPurple,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.project_list_title),
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepPurple,
                    titleContentColor = TextWhite,
                    actionIconContentColor = TextWhite
                ),
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings),
                            tint = TextWhite
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreateProject,
                modifier = Modifier.padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.create_project),
                    tint = TextWhite
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
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                projects.isEmpty() -> {
                    EmptyProjectsView(
                        modifier = Modifier.align(Alignment.Center),
                        textColor = TextWhite
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
fun EmptyProjectsView(
    modifier: Modifier = Modifier,
    textColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.no_projects_hint),
            style = MaterialTheme.typography.bodyLarge,
            color = textColor
        )
    }
}

@Composable
fun ProjectList(
    projects: List<Project>,
    onProjectClick: (Long) -> Unit,
    onDeleteProject: (Project) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
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
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    ProjectTypeCard(
        projectType = project.projectType,
        title = project.name,
        onClick = onClick,
        onLongClick = { showDeleteDialog = true },
        content = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "编号：${project.number}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextWhite.copy(alpha = 0.8f)
                    )
                    
                    ProjectStatusChip(
                        status = project.status
                    )
                }
                
                if (project.location.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "地点：${project.location}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextWhite.copy(alpha = 0.7f)
                    )
                }
                
                if (project.startDate != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "开工：${dateFormat.format(project.startDate)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextWhite.copy(alpha = 0.7f)
                    )
                }
            }
        }
    )

    // 删除确认对话框
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除项目") },
            text = { Text("确定要删除项目 \"${project.name}\" 吗？此操作不可撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun ProjectStatusChip(
    status: ProjectStatus
) {
    val (text, color) = when (status) {
        ProjectStatus.ONGOING -> stringResource(R.string.project_status_ongoing) to MaterialTheme.colorScheme.primary
        ProjectStatus.COMPLETED -> stringResource(R.string.project_status_completed) to MaterialTheme.colorScheme.secondary
    }
    
    Surface(
        color = color.copy(alpha = 0.3f),
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.padding(4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = TextWhite,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}