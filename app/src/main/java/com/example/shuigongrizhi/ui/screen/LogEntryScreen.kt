package com.example.shuigongrizhi.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.shuigongrizhi.R
import com.example.shuigongrizhi.data.entity.MediaType
import com.example.shuigongrizhi.data.entity.WeatherCondition
import com.example.shuigongrizhi.ui.viewmodel.LogEntryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogEntryScreen(
    viewModel: LogEntryViewModel,
    projectId: Long,
    date: String,
    onNavigateBack: () -> Unit
) {
    val logState by viewModel.logState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val saveResult by viewModel.saveResult.collectAsState()
    val context = LocalContext.current

    // 保存成功提示状态
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }
    var isErrorSnackbar by remember { mutableStateOf(false) }

    // 权限请求
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // 权限已授予，可以使用相机
        }
    }

    // 拍照启动器
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            // 照片拍摄成功
        }
    }

    // 录像启动器
    val takeVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo()
    ) { success ->
        if (success) {
            // 视频录制成功
        }
    }

    // 选择图片启动器
    val selectImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = "image_${System.currentTimeMillis()}.jpg"
            val newList = logState.mediaFiles.toMutableList().apply { add(it.toString()) }
            viewModel.updateMediaFiles(newList)
        }
    }

    // 选择视频启动器
    val selectVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = "video_${System.currentTimeMillis()}.mp4"
            val newList = logState.mediaFiles.toMutableList().apply { add(it.toString()) }
            viewModel.updateMediaFiles(newList)
        }
    }

    // 加载日志数据
    LaunchedEffect(projectId, date) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val parsedDate = dateFormat.parse(date) ?: Date()
        viewModel.initializeLog(projectId, parsedDate)
    }

    // 处理保存结果
    saveResult?.let { success ->
        LaunchedEffect(success) {
            if (success) {
                // 显示成功提示
                snackbarMessage = "施工日志保存成功！"
                isErrorSnackbar = false
                showSnackbar = true
                
                // 延迟导航，确保用户看到成功提示
                kotlinx.coroutines.delay(1500)
                onNavigateBack()
            }
            viewModel.clearSaveResult()
        }
    }

    // 错误处理
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
                    Column {
                        Text(
                            text = "淮工集团施工日志",
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = formatDate(date),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
                        onClick = { viewModel.saveLog() },
                        enabled = !isLoading
                    ) {
                        Text(stringResource(R.string.save))
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
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 天气信息模块
                item {
                    WeatherInfoCard(
                        weatherCondition = logState.weatherCondition,
                        temperature = logState.temperature,
                        wind = logState.wind,
                        onWeatherConditionChange = viewModel::updateWeatherCondition,
                        onTemperatureChange = viewModel::updateTemperature,
                        onWindChange = viewModel::updateWind,
                        onGetWeatherClick = { viewModel.fetchWeatherData() }
                    )
                }

                // 施工日志模块
                item {
                    ConstructionInfoCard(
                        constructionSite = logState.constructionSite,
                        mainContent = logState.mainContent,
                        personnelEquipment = logState.personnelEquipment,
                        qualityManagement = logState.qualityManagement,
                        safetyManagement = logState.safetyManagement,
                        onConstructionSiteChange = viewModel::updateConstructionSite,
                        onMainContentChange = viewModel::updateMainContent,
                        onPersonnelEquipmentChange = viewModel::updatePersonnelEquipment,
                        onQualityManagementChange = viewModel::updateQualityManagement,
                        onSafetyManagementChange = viewModel::updateSafetyManagement
                    )
                }

                // 现场照片/视频模块
                item {
                    MediaFilesCard(
                        mediaFiles = logState.mediaFiles,
                        onAddMedia = { uri ->
                            val newList = logState.mediaFiles.toMutableList().apply { add(uri) }
                            viewModel.updateMediaFiles(newList)
                        },
                        onRemoveMedia = { uri ->
                            val newList = logState.mediaFiles.toMutableList().apply { remove(uri) }
                            viewModel.updateMediaFiles(newList)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherInfoCard(
    weatherCondition: String,
    temperature: String,
    wind: String,
    onWeatherConditionChange: (String) -> Unit,
    onTemperatureChange: (String) -> Unit,
    onWindChange: (String) -> Unit,
    onGetWeatherClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.weather_info),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                OutlinedButton(
                    onClick = onGetWeatherClick
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("获取天气")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 天气状况选择
            Text(
                text = stringResource(R.string.weather_condition),
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = weatherCondition,
                    onValueChange = { },
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    WeatherCondition.values().forEach { condition ->
                        DropdownMenuItem(
                            text = { Text(condition.displayName) },
                            onClick = {
                                onWeatherConditionChange(condition.displayName)
                                expanded = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 温度
            OutlinedTextField(
                value = temperature,
                onValueChange = onTemperatureChange,
                label = { Text(stringResource(R.string.temperature)) },
                placeholder = { Text("例如：15 ~ 25 °C") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 风力/风向
            OutlinedTextField(
                value = wind,
                onValueChange = onWindChange,
                label = { Text(stringResource(R.string.wind_info)) },
                placeholder = { Text("例如：3级/东南风") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConstructionInfoCard(
    constructionSite: String,
    mainContent: String,
    personnelEquipment: String,
    qualityManagement: String,
    safetyManagement: String,
    onConstructionSiteChange: (String) -> Unit,
    onMainContentChange: (String) -> Unit,
    onPersonnelEquipmentChange: (String) -> Unit,
    onQualityManagementChange: (String) -> Unit,
    onSafetyManagementChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.construction_log),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 施工部位
            OutlinedTextField(
                value = constructionSite,
                onValueChange = onConstructionSiteChange,
                label = { Text(stringResource(R.string.construction_site)) },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 主要施工内容
            OutlinedTextField(
                value = mainContent,
                onValueChange = onMainContentChange,
                label = { Text(stringResource(R.string.main_work_content)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 施工人员
            OutlinedTextField(
                value = personnelEquipment,
                onValueChange = onPersonnelEquipmentChange,
                label = { Text(stringResource(R.string.construction_personnel)) },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 使用机械
            OutlinedTextField(
                value = qualityManagement,
                onValueChange = onQualityManagementChange,
                label = { Text(stringResource(R.string.machinery)) },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 安全记事
            OutlinedTextField(
                value = safetyManagement,
                onValueChange = onSafetyManagementChange,
                label = { Text(stringResource(R.string.safety_notes)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaFilesCard(
    mediaFiles: List<String>,
    onAddMedia: (String) -> Unit,
    onRemoveMedia: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.media_files),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        // 实现添加媒体文件的逻辑
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("添加媒体文件")
                }
            }
            
            if (mediaFiles.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // 媒体文件网格
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(mediaFiles) { mediaFile ->
                        MediaFileItem(
                            mediaFile = mediaFile,
                            onDelete = { onRemoveMedia(mediaFile) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MediaFileItem(
    mediaFile: String,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.size(80.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box {
            AsyncImage(
                model = mediaFile,
                contentDescription = "Media File",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            IconButton(
                onClick = onDelete,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// 辅助函数
fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}