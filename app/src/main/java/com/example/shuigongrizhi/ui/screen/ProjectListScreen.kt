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
import com.example.shuigongrizhi.ui.components.ProjectTypeCard
import com.example.shuigongrizhi.ui.theme.DeepPurple
import com.example.shuigongrizhi.ui.theme.TextWhite
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
    var showMenu by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showMenu = true },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = project.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = project.type.name,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (!project.manager.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "负责人：${project.manager}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${project.startDate.let { dateFormat.format(it) }}" +
                    (project.endDate?.let { " - ${dateFormat.format(it)}" } ?: ""),
                style = MaterialTheme.typography.bodySmall
            )
            if (!project.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = project.description,
                    style = MaterialTheme.typography.bodySmall,
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
        DropdownMenuItem(
            text = { Text("删除") },
            onClick = {
                showMenu = false
                showDeleteDialog = true
            }
        )
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