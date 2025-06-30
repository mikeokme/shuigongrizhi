package com.example.shuigongrizhi.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.shuigongrizhi.R
import com.example.shuigongrizhi.data.entity.ProjectStatus
import com.example.shuigongrizhi.ui.viewmodel.ProjectFormViewModel
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectFormScreen(
    viewModel: ProjectFormViewModel,
    projectId: Long? = null,
    onNavigateBack: () -> Unit
) {
    val formState by viewModel.formState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val saveResult by viewModel.saveResult.collectAsState()
    val error by viewModel.error.collectAsState()

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val startDateDialogState = rememberMaterialDialogState()
    val endDateDialogState = rememberMaterialDialogState()

    // 加载现有项目数据
    LaunchedEffect(projectId) {
        projectId?.let {
            viewModel.loadProject(it)
        }
    }

    // 处理保存结果
    LaunchedEffect(saveResult) {
        if (saveResult == true) {
            onNavigateBack()
            viewModel.clearSaveResult()
        }
    }

    // 错误处理
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // 显示错误信息
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (projectId == null) {
                            stringResource(R.string.create_new_project)
                        } else {
                            stringResource(R.string.edit_project)
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.saveProject() },
                        enabled = !isLoading && formState.name.isNotBlank() && formState.number.isNotBlank()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(stringResource(R.string.save))
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 项目名称
            OutlinedTextField(
                value = formState.name,
                onValueChange = viewModel::updateName,
                label = { Text(stringResource(R.string.project_name)) },
                modifier = Modifier.fillMaxWidth(),
                isError = formState.nameError != null,
                supportingText = formState.nameError?.let { { Text(it) } }
            )

            // 项目编号
            OutlinedTextField(
                value = formState.number,
                onValueChange = viewModel::updateNumber,
                label = { Text(stringResource(R.string.project_number)) },
                modifier = Modifier.fillMaxWidth(),
                isError = formState.numberError != null,
                supportingText = formState.numberError?.let { { Text(it) } }
            )

            // 施工单位
            OutlinedTextField(
                value = formState.constructionUnit,
                onValueChange = viewModel::updateConstructionUnit,
                label = { Text(stringResource(R.string.construction_unit)) },
                modifier = Modifier.fillMaxWidth()
            )

            // 监理单位
            OutlinedTextField(
                value = formState.supervisionUnit,
                onValueChange = viewModel::updateSupervisionUnit,
                label = { Text(stringResource(R.string.supervision_unit)) },
                modifier = Modifier.fillMaxWidth()
            )

            // 项目地点
            OutlinedTextField(
                value = formState.location,
                onValueChange = viewModel::updateLocation,
                label = { Text(stringResource(R.string.project_location)) },
                modifier = Modifier.fillMaxWidth()
            )

            // 开工日期
            OutlinedTextField(
                value = formState.startDate?.let { dateFormat.format(it) } ?: "",
                onValueChange = { },
                label = { Text(stringResource(R.string.start_date)) },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { startDateDialogState.show() }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "选择日期"
                        )
                    }
                }
            )

            // 计划竣工日期
            OutlinedTextField(
                value = formState.plannedCompletionDate?.let { dateFormat.format(it) } ?: "",
                onValueChange = { },
                label = { Text(stringResource(R.string.planned_completion_date)) },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { endDateDialogState.show() }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "选择日期"
                        )
                    }
                }
            )

            // 项目状态
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.project_type),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    // 项目类型选择
                    Row(modifier = Modifier.fillMaxWidth()) {
                        val projectTypes = listOf("水库", "河道", "灌区", "水电站", "泵站")
                        projectTypes.forEach { type ->
                            Row {
                                RadioButton(
                                    selected = formState.projectType == type,
                                    onClick = { viewModel.updateProjectType(type) }
                                )
                                Text(
                                    text = type,
                                    modifier = Modifier.padding(start = 4.dp, end = 16.dp)
                                )
                            }
                        }
                    }
                    
                    Text(
                        text = stringResource(R.string.project_status),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                    
                    Row {
                        ProjectStatus.values().forEach { status ->
                            Row {
                                RadioButton(
                                    selected = formState.status == status,
                                    onClick = { viewModel.updateStatus(status) }
                                )
                                Text(
                                    text = when (status) {
                                        ProjectStatus.ONGOING -> stringResource(R.string.project_status_ongoing)
                                        ProjectStatus.COMPLETED -> stringResource(R.string.project_status_completed)
                                    },
                                    modifier = Modifier.padding(start = 4.dp, end = 16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // 开工日期选择对话框
    MaterialDialog(
        dialogState = startDateDialogState,
        buttons = {
            positiveButton(text = "确定")
            negativeButton(text = "取消")
        }
    ) {
        datepicker(
            initialDate = formState.startDate?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate() ?: LocalDate.now(),
            title = "选择开工日期"
        ) { date ->
            val selectedDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant())
            viewModel.updateStartDate(selectedDate)
        }
    }

    // 计划竣工日期选择对话框
    MaterialDialog(
        dialogState = endDateDialogState,
        buttons = {
            positiveButton(text = "确定")
            negativeButton(text = "取消")
        }
    ) {
        datepicker(
            initialDate = formState.plannedCompletionDate?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate() ?: LocalDate.now(),
            title = "选择计划竣工日期"
        ) { date ->
            val selectedDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant())
            viewModel.updatePlannedCompletionDate(selectedDate)
        }
    }
}