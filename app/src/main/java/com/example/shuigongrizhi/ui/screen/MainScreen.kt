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

@Composable
fun MainScreen(
    onLogClick: () -> Unit = {},
    onWeatherClick: () -> Unit = {},
    onCameraClick: () -> Unit = {},
    onLocationClick: () -> Unit = {},
    onMediaClick: () -> Unit = {},
    onProjectClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
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
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FeatureCard(
                    title = "施工日志",
                    iconRes = R.drawable.ic_launcher_foreground,
                    gradient = Brush.linearGradient(listOf(Color(0xFF7F53AC), Color(0xFF647DEE))),
                    modifier = Modifier.weight(1f),
                    onClick = onLogClick
                )
                WeatherCard(
                    weatherState = weatherState,
                    modifier = Modifier.weight(1f),
                    onClick = onWeatherClick
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FeatureCard(
                    title = "拍照录像",
                    iconRes = R.drawable.ic_champagne,
                    gradient = Brush.linearGradient(listOf(Color(0xFFCB2D3E), Color(0xFFEF473A))),
                    modifier = Modifier.weight(1f),
                    onClick = onCameraClick
                )
                FeatureCard(
                    title = "位置服务",
                    iconRes = R.drawable.ic_launcher_foreground,
                    gradient = Brush.linearGradient(listOf(Color(0xFF8E54E9), Color(0xFF4776E6))),
                    modifier = Modifier.weight(1f),
                    onClick = onLocationClick
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FeatureCard(
                    title = "媒体管理",
                    iconRes = R.drawable.ic_cognac,
                    gradient = Brush.linearGradient(listOf(Color(0xFF43CEA2), Color(0xFF185A9D))),
                    modifier = Modifier.weight(1f),
                    onClick = onMediaClick
                )
                FeatureCard(
                    title = "项目管理",
                    iconRes = R.drawable.ic_launcher_foreground,
                    gradient = Brush.linearGradient(listOf(Color(0xFF43CEA2), Color(0xFF4568DC))),
                    modifier = Modifier.weight(1f),
                    onClick = onProjectClick
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
        // 底部导航栏
        BottomNavBar(selectedIndex = 2)
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "天气信息",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                is com.example.shuigongrizhi.ui.viewmodel.WeatherState.Success -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cloud,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${weatherState.weather.main.temp.toInt()}°C",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = weatherState.weather.weather[0].description,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp
                        )
                        Text(
                            text = weatherState.weather.name,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 10.sp
                        )
                    }
                }
                is com.example.shuigongrizhi.ui.viewmodel.WeatherState.Error -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "天气信息",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "点击重试",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 10.sp
                        )
                    }
                }
                else -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cloud,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "天气信息",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}