package com.example.shuigongrizhi.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.shuigongrizhi.R
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import com.example.shuigongrizhi.ui.screen.WeatherAnimation

@Composable
fun MainScreen(
    onLogClick: () -> Unit = {},
    onWeatherClick: () -> Unit = {},
    onCameraClick: () -> Unit = {},
    onLocationClick: () -> Unit = {},
    onMediaClick: () -> Unit = {},
    onProjectClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onProjectSelectionClick: () -> Unit = {},
    onPdfViewerClick: () -> Unit = {},
    onDesktopClick: () -> Unit = {},
    onFeedbackClick: () -> Unit = {},
    weatherViewModel: com.example.shuigongrizhi.ui.viewmodel.WeatherViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val weatherState by weatherViewModel.weatherState.collectAsState()
    
    val context = androidx.compose.ui.platform.LocalContext.current
    // 自动加载天气数据
    LaunchedEffect(Unit) {
        weatherViewModel.getCurrentWeatherAuto(context)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF231942))
    ) {
        // 顶部栏
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, start = 24.dp, end = 24.dp, bottom = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = null,
                tint = Color(0xFF8D6EFF),
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.CenterStart)
            )
            Text(
                text = "淮工集团施工日志系统",
                color = Color(0xFF8D6EFF),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "设置",
                    tint = Color(0xFF8D6EFF)
                )
            }
        }
        // 功能区 - 3x3宫格布局
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(20.dp), // 增加内边距
            horizontalArrangement = Arrangement.spacedBy(20.dp), // 增加间距
            verticalArrangement = Arrangement.spacedBy(20.dp), // 增加间距
            modifier = Modifier.fillMaxWidth()
        ) {
            items(9) { index ->
                when (index) {
                    0 -> FeatureCard(
                        title = "施工日志",
                        iconRes = R.drawable.ic_construction_log,
                        gradient = Brush.linearGradient(listOf(Color(0xFF7F53AC), Color(0xFF647DEE))),
                        modifier = Modifier.aspectRatio(1f),
                        onClick = onLogClick
                    )
                    1 -> WeatherCard(
                        weatherState = weatherState,
                        modifier = Modifier.aspectRatio(1f),
                        onClick = onWeatherClick
                    )
                    2 -> FeatureCard(
                        title = "拍照录像",
                        iconRes = R.drawable.ic_camera,
                        gradient = Brush.linearGradient(listOf(Color(0xFFCB2D3E), Color(0xFFEF473A))),
                        modifier = Modifier.aspectRatio(1f),
                        onClick = onCameraClick
                    )
                    3 -> FeatureCard(
                        title = "位置服务",
                        iconRes = R.drawable.ic_location,
                        gradient = Brush.linearGradient(listOf(Color(0xFF8E54E9), Color(0xFF4776E6))),
                        modifier = Modifier.aspectRatio(1f),
                        onClick = onLocationClick
                    )
                    4 -> FeatureCard(
                        title = "媒体管理",
                        iconRes = R.drawable.ic_media,
                        gradient = Brush.linearGradient(listOf(Color(0xFF43CEA2), Color(0xFF185A9D))),
                        modifier = Modifier.aspectRatio(1f),
                        onClick = onMediaClick
                    )
                    5 -> FeatureCard(
                        title = "项目管理",
                        iconRes = R.drawable.ic_project,
                        gradient = Brush.linearGradient(listOf(Color(0xFF43CEA2), Color(0xFF4568DC))),
                        modifier = Modifier.aspectRatio(1f),
                        onClick = onProjectClick
                    )
                    6 -> FeatureCard(
                        title = "PDF文档",
                        iconRes = R.drawable.ic_pdf,
                        gradient = Brush.linearGradient(listOf(Color(0xFFFF6B6B), Color(0xFF4ECDC4))),
                        modifier = Modifier.aspectRatio(1f),
                        onClick = onPdfViewerClick
                    )
                    7 -> FeatureCard(
                        title = "设置",
                        iconRes = R.drawable.ic_settings,
                        gradient = Brush.linearGradient(listOf(Color(0xFF9C27B0), Color(0xFF673AB7))),
                        modifier = Modifier.aspectRatio(1f),
                        onClick = onSettingsClick
                    )
                    8 -> FeatureCard(
                        title = "待开发区域",
                        iconRes = R.drawable.ic_development,
                        gradient = Brush.linearGradient(listOf(Color(0xFFFF416C), Color(0xFFFF4B2B))),
                        modifier = Modifier.aspectRatio(1f),
                        onClick = onFeedbackClick
                    )
                }
            }
        }

        // 下移对联和项目信息说明
        Spacer(modifier = Modifier.weight(1f))

        // 企业文化对联 - 优化字体一致性和对比度
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 24.dp)
                .height(90.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "生如蝼蚁，当有鸿鹄之志；\n命如纸薄，应有不屈之心。",
                    color = Color(0xFFF5F5F5), // 改为更高对比度的浅色
                    fontSize = 20.sp, // 稍微减小字体大小
                    fontWeight = FontWeight.Medium, // 使用Medium权重保持一致性
                    textAlign = TextAlign.Center,
                    letterSpacing = 1.sp, // 减少字间距
                    style = MaterialTheme.typography.titleMedium, // 使用一致的字体样式
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 项目信息说明 - 缩小并优化显示
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "版本号：bate1.0",
                    color = Color.White.copy(alpha = 0.7f), // 降低透明度减少视觉干扰
                    fontSize = 12.sp, // 缩小字体
                    fontWeight = FontWeight.Normal,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "开发者：醉生梦死",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "开发单位：淮工集团",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "射阳盐场项目出品",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End
                )
            }
        }
        // 添加底部空间，确保内容不被底部导航栏遮挡
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun WeatherCard(
    weatherState: com.example.shuigongrizhi.ui.viewmodel.WeatherState,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(
                brush = Brush.linearGradient(
                    listOf(Color(0xFF56CCF2), Color(0xFF2F80ED))
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(20.dp), // 增加内边距
        contentAlignment = Alignment.Center
    ) {
        when (weatherState) {
            is com.example.shuigongrizhi.ui.viewmodel.WeatherState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp), // 稍微增大加载指示器
                    strokeWidth = 3.dp,
                    color = Color.White
                )
            }
            is com.example.shuigongrizhi.ui.viewmodel.WeatherState.Success -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 温度显示在独立的框内
                    Box(
                        modifier = Modifier
                            .background(
                                Color.White.copy(alpha = 0.2f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${weatherState.weather.result.realtime.temperature.toInt()}°C",
                            fontSize = 24.sp, // 增大字体
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "天气",
                        fontSize = 16.sp, // 增大标签字体
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            is com.example.shuigongrizhi.ui.viewmodel.WeatherState.Error -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = "天气错误",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp) // 增大图标
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "天气",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            else -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Cloud,
                        contentDescription = "天气",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "天气",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}