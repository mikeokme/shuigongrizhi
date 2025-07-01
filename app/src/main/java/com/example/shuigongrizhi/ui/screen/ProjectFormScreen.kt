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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.ui.Alignment
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import java.text.ParseException

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
    val yymmddFormat = SimpleDateFormat("yyMMdd", Locale.getDefault())
    val startDateDialogState = rememberMaterialDialogState()
    val endDateDialogState = rememberMaterialDialogState()

    // 日期输入模式状态
    var startDateInputMode by remember { mutableStateOf("picker") } // "picker" 或 "manual"
    var endDateInputMode by remember { mutableStateOf("picker") }
    
    // 手动输入的日期文本
    var startDateText by remember { mutableStateOf("") }
    var endDateText by remember { mutableStateOf("") }

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

    // 初始化手动输入文本
    LaunchedEffect(formState.startDate) {
        if (formState.startDate != null) {
            startDateText = yymmddFormat.format(formState.startDate)
        }
    }
    
    LaunchedEffect(formState.endDate) {
        if (formState.endDate != null) {
            endDateText = yymmddFormat.format(formState.endDate)
        }
    }

    // 日期输入转换器
    val yymmddTransformation = object : VisualTransformation {
        override fun filter(text: AnnotatedString): TransformedText {
            val input = text.text
            val formatted = buildString {
                input.forEachIndexed { index, char ->
                    if (index == 2 || index == 4) append("-")
                    append(char)
                }
            }
            return TransformedText(
                AnnotatedString(formatted),
                object : OffsetMapping {
                    override fun originalToTransformed(offset: Int): Int = offset + (offset / 2)
                    override fun transformedToOriginal(offset: Int): Int = offset - (offset / 2)
                }
            )
        }
    }

    // 解析 yymmdd 格式日期
    fun parseYymmddDate(dateText: String): Date? {
        return try {
            if (dateText.length == 6) {
                yymmddFormat.parse(dateText)
            } else null
        } catch (e: ParseException) {
            null
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "开始日期",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // 输入模式切换
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = startDateInputMode == "picker",
                            onClick = { startDateInputMode = "picker" },
                            label = { Text("日期选择") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                        FilterChip(
                            selected = startDateInputMode == "manual",
                            onClick = { startDateInputMode = "manual" },
                            label = { Text("手动输入") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                    
                    // 日期选择器模式
                    if (startDateInputMode == "picker") {
                        OutlinedTextField(
                            value = formState.startDate?.let { dateFormat.format(it) } ?: "",
                            onValueChange = {},
                            label = { Text("选择开始日期") },
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
                    } else {
                        // 手动输入模式
                        OutlinedTextField(
                            value = startDateText,
                            onValueChange = { text ->
                                val filtered = text.filter { it.isDigit() }.take(6)
                                startDateText = filtered
                                val parsedDate = parseYymmddDate(filtered)
                                viewModel.updateStartDate(parsedDate)
                            },
                            label = { Text("输入日期 (YYMMDD)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            visualTransformation = yymmddTransformation,
                            isError = formState.startDateError != null,
                            supportingText = {
                                Text(
                                    text = formState.startDateError ?: "格式：YYMMDD，如：240101"
                                )
                            }
                        )
                    }
                }
            }
            
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
                    startDateText = yymmddFormat.format(selectedDate)
                }
            }

            // 结束日期
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "结束日期 (可选)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // 输入模式切换
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = endDateInputMode == "picker",
                            onClick = { endDateInputMode = "picker" },
                            label = { Text("日期选择") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                        FilterChip(
                            selected = endDateInputMode == "manual",
                            onClick = { endDateInputMode = "manual" },
                            label = { Text("手动输入") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                    
                    // 日期选择器模式
                    if (endDateInputMode == "picker") {
                        OutlinedTextField(
                            value = formState.endDate?.let { dateFormat.format(it) } ?: "",
                            onValueChange = {},
                            label = { Text("选择结束日期") },
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
                    } else {
                        // 手动输入模式
                        OutlinedTextField(
                            value = endDateText,
                            onValueChange = { text ->
                                val filtered = text.filter { it.isDigit() }.take(6)
                                endDateText = filtered
                                val parsedDate = parseYymmddDate(filtered)
                                viewModel.updateEndDate(parsedDate)
                            },
                            label = { Text("输入日期 (YYMMDD)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            visualTransformation = yymmddTransformation,
                            supportingText = {
                                Text("格式：YYMMDD，如：241231 (可选)")
                            }
                        )
                    }
                }
            }
            
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
                    endDateText = yymmddFormat.format(selectedDate)
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