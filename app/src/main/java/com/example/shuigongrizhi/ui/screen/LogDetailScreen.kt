package com.example.shuigongrizhi.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.shuigongrizhi.data.entity.ConstructionLog
import com.example.shuigongrizhi.data.entity.MediaType
import com.example.shuigongrizhi.ui.viewmodel.LogDetailViewModel
import com.example.shuigongrizhi.ui.viewmodel.LogDetailState
import java.text.SimpleDateFormat
import java.util.*
// import androidx.hilt.navigation.compose.hiltViewModel
import com.example.shuigongrizhi.ui.theme.AppCardDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogDetailScreen(
    logId: Long,
    viewModel: LogDetailViewModel = remember { LogDetailViewModel() },
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
    
    // 加载日志详情
    LaunchedEffect(logId) {
        viewModel.loadLogDetail(logId)
    }
    
    // 处理错误
    state.error?.let { error ->
        LaunchedEffect(error) {
            // 可以显示 Snackbar 或其他错误提示
            viewModel.clearError()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("施工日志详情") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // PDF 生成按钮
                    IconButton(
                        onClick = { viewModel.generatePdf() },
                        enabled = !state.isGeneratingPdf && state.constructionLog != null
                    ) {
                        if (state.isGeneratingPdf) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Download, contentDescription = "生成PDF")
                        }
                    }
                    
                    // 分享按钮（当PDF生成后）
                    state.pdfFile?.let { pdfFile ->
                        IconButton(
                            onClick = {
                                // 实现分享功能
                                val uri = androidx.core.content.FileProvider.getUriForFile(
                                    context,
                                    context.packageName + ".fileprovider",
                                    pdfFile
                                )
                                val shareIntent = android.content.Intent().apply {
                                    action = android.content.Intent.ACTION_SEND
                                    type = "application/pdf"
                                    putExtra(android.content.Intent.EXTRA_STREAM, uri)
                                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(android.content.Intent.createChooser(shareIntent, "分享PDF文件"))
                            }
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "分享")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                state.constructionLog == null -> {
                    Text(
                        text = "日志不存在",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LogDetailContent(
                        state = state,
                        dateFormat = dateFormat,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun LogDetailContent(
    state: LogDetailState,
    dateFormat: SimpleDateFormat,
    modifier: Modifier = Modifier
) {
    val log = state.constructionLog!!
    val project = state.project!!
    
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 标题
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = AppCardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "淮工集团施工日志",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = dateFormat.format(log.date),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        
        // 基本信息
        InfoCard(
            title = "基本信息",
            content = {
                InfoRow("项目名称", project.name)
                InfoRow("施工日期", dateFormat.format(log.date))
                InfoRow("施工部位", log.constructionSite)
                InfoRow("负责人", project.manager ?: "")
            }
        )
        
        // 天气信息
        InfoCard(
            title = "天气信息",
            content = {
                InfoRow("天气状况", log.weatherCondition)
                InfoRow("温度", log.temperature)
                InfoRow("风力/风向", log.wind)
            }
        )
        
        // 施工内容
        ContentCard(
            title = "施工内容",
            content = log.mainContent
        )
        
        // 人员设备
        ContentCard(
            title = "人员设备",
            content = log.personnelEquipment
        )
        
        // 质量管理
        ContentCard(
            title = "质量管理",
            content = log.qualityManagement
        )
        
        // 安全管理
        ContentCard(
            title = "安全管理",
            content = log.safetyManagement
        )
        
        // 现场照片
        MediaCard(
            title = "现场照片",
            mediaFiles = state.mediaFiles
        )
        
        // PDF 生成状态
        state.pdfFile?.let { pdfFile ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = AppCardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "PDF 生成成功",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "文件路径: ${pdfFile.absolutePath}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = AppCardDefaults.cardColors()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            content()
        }
    }
}

@Composable
private fun ContentCard(
    title: String,
    content: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = AppCardDefaults.cardColors()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = content.ifEmpty { "暂无内容" },
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun MediaCard(
    title: String,
    mediaFiles: List<com.example.shuigongrizhi.data.entity.MediaFile>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = AppCardDefaults.cardColors()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            if (mediaFiles.isEmpty()) {
                Text(
                    text = "暂无现场照片",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val photoFiles = mediaFiles.filter { it.fileType == MediaType.PHOTO }
            val videoFiles = mediaFiles.filter { it.fileType == MediaType.VIDEO }
                
                if (photoFiles.isNotEmpty()) {
                    Text(
                        text = "照片: ${photoFiles.size} 张",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                if (videoFiles.isNotEmpty()) {
                    Text(
                        text = "视频: ${videoFiles.size} 个",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value.ifEmpty { "暂无" },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun WeatherInfoCard(
    weatherCondition: String,
    temperature: String,
    wind: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Cloud,
                    contentDescription = "天气",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "天气情况",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (weatherCondition.isNotEmpty()) {
                    WeatherItem(
                        label = "天气",
                        value = weatherCondition,
                        icon = Icons.Default.WbSunny
                    )
                }
                
                if (temperature.isNotEmpty()) {
                    WeatherItem(
                        label = "温度",
                        value = temperature,
                        icon = Icons.Default.Thermostat
                    )
                }
                
                if (wind.isNotEmpty()) {
                    WeatherItem(
                        label = "风力",
                        value = wind,
                        icon = Icons.Default.Air
                    )
                }
            }
        }
    }
}

@Composable
fun WeatherItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun InfoChip(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$label: $value",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}