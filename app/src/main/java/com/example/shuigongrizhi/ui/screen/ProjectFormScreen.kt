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
import com.example.shuigongrizhi.data.entity.ProjectType
import com.example.shuigongrizhi.ui.viewmodel.ProjectFormViewModel
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.*
import androidx.compose.ui.text.input.TextFieldValue

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

    // 错误处理和成功提示
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }
    var isErrorSnackbar by remember { mutableStateOf(false) }

    // 加载现有项目数据
    LaunchedEffect(projectId) {
        projectId?.let {
            viewModel.loadProject(it)
        }
    }

    // 处理保存结果
    LaunchedEffect(saveResult) {
        if (saveResult == true) {
            // 显示成功提示
            snackbarMessage = if (projectId == null) "项目创建成功！" else "项目更新成功！"
            isErrorSnackbar = false
            showSnackbar = true
            
            // 延迟导航，确保数据库操作和UI更新完成，并让用户看到成功提示
            kotlinx.coroutines.delay(1500)
            onNavigateBack()
            viewModel.clearSaveResult()
        }
    }
    
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            snackbarMessage = errorMessage
            isErrorSnackbar = true
            showSnackbar = true
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
                        enabled = !isLoading && formState.name.isNotBlank() && formState.startDate != null
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
        },
        snackbarHost = {
            if (showSnackbar) {
                Snackbar(
                    action = {
                        TextButton(onClick = { showSnackbar = false }) {
                            Text("确定")
                        }
                    },
                    modifier = Modifier.padding(8.dp),
                    containerColor = if (isErrorSnackbar) {
                        MaterialTheme.colorScheme.errorContainer
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    }
                ) {
                    Text(
                        text = snackbarMessage,
                        color = if (isErrorSnackbar) {
                            MaterialTheme.colorScheme.onErrorContainer
                        } else {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        }
                    )
                }
            }
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
                label = { Text("项目名称") },
                modifier = Modifier.fillMaxWidth(),
                isError = formState.nameError != null,
                supportingText = formState.nameError?.let { { Text(it) } }
            )

            // 项目类型
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = formState.type.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("项目类型") },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    ProjectType.values().forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.name) },
                            onClick = {
                                viewModel.updateType(type)
                                expanded = false
                            }
                        )
                    }
                }
            }

            // 负责人
            OutlinedTextField(
                value = formState.manager,
                onValueChange = viewModel::updateManager,
                label = { Text("负责人") },
                modifier = Modifier.fillMaxWidth()
            )

            // 开始日期
            OutlinedTextField(
                value = formState.startDate?.let { dateFormat.format(it) } ?: "",
                onValueChange = {},
                label = { Text("开始日期") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                isError = formState.startDateError != null,
                supportingText = formState.startDateError?.let { { Text(it) } },
                trailingIcon = {
                    IconButton(onClick = { startDateDialogState.show() }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "选择日期"
                        )
                    }
                }
            )
            MaterialDialog(
                dialogState = startDateDialogState,
                buttons = {
                    positiveButton("确定")
                    negativeButton("取消")
                }
            ) {
                datepicker { date: LocalDate ->
                    val selectedDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant())
                    viewModel.updateStartDate(selectedDate)
                }
            }

            // 结束日期
            OutlinedTextField(
                value = formState.endDate?.let { dateFormat.format(it) } ?: "",
                onValueChange = {},
                label = { Text("结束日期") },
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
            MaterialDialog(
                dialogState = endDateDialogState,
                buttons = {
                    positiveButton("确定")
                    negativeButton("取消")
                }
            ) {
                datepicker { date: LocalDate ->
                    val selectedDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant())
                    viewModel.updateEndDate(selectedDate)
                }
            }

            // 描述
            OutlinedTextField(
                value = formState.description,
                onValueChange = viewModel::updateDescription,
                label = { Text("项目描述") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 4
            )
        }
    }
}