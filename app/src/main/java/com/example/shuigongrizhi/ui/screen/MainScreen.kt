package com.example.shuigongrizhi.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
    weatherViewModel: com.example.shuigongrizhi.ui.viewmodel.WeatherViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val weatherState by weatherViewModel.weatherState.collectAsState()
    
    // 自动加载天气数据
    LaunchedEffect(Unit) {
        weatherViewModel.getCurrentWeather()
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
                text = "淮工施工日志系统",
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
        // 功能区
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 32.dp, vertical = 16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FeatureCard(
                    title = "施工日志",
                    iconRes = R.drawable.ic_launcher_foreground,
                    gradient = Brush.linearGradient(listOf(Color(0xFF7F53AC), Color(0xFF647DEE))),
                    modifier = Modifier.size(96.dp),
                    onClick = onLogClick
                )
                FeatureCard(
                    title = "拍照录像",
                    iconRes = R.drawable.ic_champagne,
                    gradient = Brush.linearGradient(listOf(Color(0xFFCB2D3E), Color(0xFFEF473A))),
                    modifier = Modifier.size(96.dp),
                    onClick = onCameraClick
                )
                FeatureCard(
                    title = "媒体管理",
                    iconRes = R.drawable.ic_cognac,
                    gradient = Brush.linearGradient(listOf(Color(0xFF43CEA2), Color(0xFF185A9D))),
                    modifier = Modifier.size(96.dp),
                    onClick = onMediaClick
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                WeatherCard(
                    weatherState = weatherState,
                    modifier = Modifier.size(96.dp),
                    onClick = onWeatherClick
                )
                FeatureCard(
                    title = "位置服务",
                    iconRes = R.drawable.ic_gps_fixed,
                    gradient = Brush.linearGradient(listOf(Color(0xFF8E54E9), Color(0xFF4776E6))),
                    modifier = Modifier.size(96.dp),
                    onClick = onLocationClick
                )
                FeatureCard(
                    title = "项目管理",
                    iconRes = R.drawable.ic_launcher_foreground,
                    gradient = Brush.linearGradient(listOf(Color(0xFF43CEA2), Color(0xFF4568DC))),
                    modifier = Modifier.size(96.dp),
                    onClick = onProjectClick
                )
            }
        }
        // 企业文化对联区域
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 24.dp)
                .height(90.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "生如蝼蚁当有鸿鹄之志",
                    color = Color(0xFFFFD700), // 金色
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    letterSpacing = 2.sp,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "命如纸薄应有不屈之心",
                    color = Color(0xFFFFD700),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    letterSpacing = 2.sp,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
        // 底部项目信息说明
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 40.dp),
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
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "|",
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Text(
                    text = "开发者：醉生梦死",
                    color = Color.White,
                    fontSize = 14.sp,
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
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "|",
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Text(
                    text = "射阳盐场项目出品",
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Composable
fun WeatherCard(
    weatherState: com.example.shuigongrizhi.ui.viewmodel.WeatherState,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .padding(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        listOf(Color(0xFF56CCF2), Color(0xFF2F80ED))
                    )
                )
                .padding(16.dp)
        ) {
            when (weatherState) {
                is com.example.shuigongrizhi.ui.viewmodel.WeatherState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                is com.example.shuigongrizhi.ui.viewmodel.WeatherState.Success -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${weatherState.weather.main.temp.toInt()}°C",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = weatherState.weather.weather[0].description,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            text = weatherState.weather.name,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
                is com.example.shuigongrizhi.ui.viewmodel.WeatherState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = "天气错误",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "天气信息获取失败",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cloud,
                            contentDescription = "天气",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "天气信息",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}