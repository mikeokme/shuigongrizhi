package com.example.shuigongrizhi.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogDetailScreen(
    viewModel: LogDetailViewModel,
    logId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long, String) -> Unit,
    onNavigateToMediaGallery: (Long) -> Unit
) {
    val logDetailState by viewModel.logDetailState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    // 初始化数据
    LaunchedEffect(logId) {
        viewModel.loadLogDetail(logId)
    }
    
    val err = error
    val logVal = logDetailState.log
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "日志详情",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    logVal?.let { log ->
                        IconButton(
                            onClick = {
                                val dateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                    .format(log.date)
                                onNavigateToEdit(log.projectId, dateString)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "编辑"
                            )
                        }
                        
                        IconButton(
                            onClick = { /* TODO: 分享功能 */ }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "分享"
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            err != null -> {
                ErrorMessage(
                    message = err,
                    onRetry = { viewModel.loadLogDetail(logId) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
            logVal != null -> {
                LogDetailContent(
                    log = logVal,
                    onNavigateToMediaGallery = onNavigateToMediaGallery,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        }
    }
}

@Composable
fun LogDetailContent(
    log: ConstructionLog,
    onNavigateToMediaGallery: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("yyyy年MM月dd日 EEEE", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 日期和基本信息卡片
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = dateFormat.format(log.date),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        InfoChip(
                            label = "创建时间",
                            value = timeFormat.format(log.createdAt),
                            icon = Icons.Default.Schedule
                        )
                        
                        if (log.updatedAt != log.createdAt) {
                            InfoChip(
                                label = "更新时间",
                                value = timeFormat.format(log.updatedAt),
                                icon = Icons.Default.Update
                            )
                        }
                    }
                }
            }
        }
        
        // 天气信息卡片
        if (log.weatherCondition.isNotEmpty() || log.temperature.isNotEmpty() || log.wind.isNotEmpty()) {
            item {
                WeatherInfoCard(
                    weatherCondition = log.weatherCondition,
                    temperature = log.temperature,
                    wind = log.wind
                )
            }
        }
        
        // 施工地点
        if (log.constructionSite.isNotEmpty()) {
            item {
                InfoCard(
                    title = "施工地点",
                    content = log.constructionSite,
                    icon = Icons.Default.LocationOn
                )
            }
        }
        
        // 主要内容
        if (log.mainContent.isNotEmpty()) {
            item {
                InfoCard(
                    title = "主要施工内容",
                    content = log.mainContent,
                    icon = Icons.Default.Construction
                )
            }
        }
        
        // 人员设备
        if (log.personnelEquipment.isNotEmpty()) {
            item {
                InfoCard(
                    title = "人员设备情况",
                    content = log.personnelEquipment,
                    icon = Icons.Default.Group
                )
            }
        }
        
        // 质量管理
        if (log.qualityManagement.isNotEmpty()) {
            item {
                InfoCard(
                    title = "质量管理",
                    content = log.qualityManagement,
                    icon = Icons.Default.VerifiedUser
                )
            }
        }
        
        // 安全管理
        if (log.safetyManagement.isNotEmpty()) {
            item {
                InfoCard(
                    title = "安全管理",
                    content = log.safetyManagement,
                    icon = Icons.Default.Security
                )
            }
        }
        
        // 媒体文件
        if (log.mediaFiles.isNotEmpty()) {
            item {
                MediaFilesCard(
                    mediaFiles = log.mediaFiles,
                    onViewAllClick = { onNavigateToMediaGallery(log.projectId) }
                )
            }
        }
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
fun InfoCard(
    title: String,
    content: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
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
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun MediaFilesCard(
    mediaFiles: List<String>,
    onViewAllClick: () -> Unit
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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Photo,
                        contentDescription = "媒体文件",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "媒体文件 (${mediaFiles.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                TextButton(onClick = onViewAllClick) {
                    Text("查看全部")
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(mediaFiles.take(5)) { mediaFile ->
                    MediaThumbnail(
                        mediaPath = mediaFile,
                        modifier = Modifier.size(80.dp)
                    )
                }
                
                if (mediaFiles.size > 5) {
                    item {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+${mediaFiles.size - 5}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MediaThumbnail(
    mediaPath: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(mediaPath)
            .crossfade(true)
            .build(),
        contentDescription = "媒体文件",
        modifier = modifier
            .clip(RoundedCornerShape(8.dp)),
        contentScale = ContentScale.Crop
    )
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