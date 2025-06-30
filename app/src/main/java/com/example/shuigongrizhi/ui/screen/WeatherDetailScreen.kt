package com.example.shuigongrizhi.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.shuigongrizhi.ui.viewmodel.WeatherViewModel
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherDetailScreen(
    viewModel: WeatherViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val weatherState by viewModel.weatherState.collectAsState()
    val weatherData by viewModel.weatherData.collectAsState()
    
    val context = LocalContext.current
    // 自动加载天气数据
    LaunchedEffect(Unit) {
        viewModel.getCurrentWeatherAuto(context)
    }
    
    // 错误处理
    LaunchedEffect(weatherState) {
        if (weatherState is com.example.shuigongrizhi.ui.viewmodel.WeatherState.Error) {
            // 可以在这里显示错误消息
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF2196F3),
                        Color(0xFF21CBF3)
                    )
                )
            )
    ) {
        // 顶部栏
        TopAppBar(
            title = {
                Text(
                    text = "天气详情",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "返回",
                        tint = Color.White
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = { viewModel.refreshWeather() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "刷新",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )
        
        when (weatherState) {
            is com.example.shuigongrizhi.ui.viewmodel.WeatherState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
            is com.example.shuigongrizhi.ui.viewmodel.WeatherState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 主要天气信息卡片
                    item {
                        MainWeatherCard(
                            cityName = weatherData.cityName,
                            temperature = weatherData.temperature,
                            condition = weatherData.weatherCondition,
                            description = weatherData.description,
                            feelsLike = weatherData.feelsLike
                        )
                    }
                    
                    // 详细信息网格
                    item {
                        WeatherDetailsGrid(
                            humidity = weatherData.humidity,
                            windSpeed = weatherData.windSpeed,
                            windDirection = weatherData.windDirection,
                            pressure = weatherData.pressure,
                            visibility = weatherData.visibility
                        )
                    }
                    
                    // 更新时间
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.2f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "最后更新: ${weatherData.lastUpdated}",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                textAlign = TextAlign.Center,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
            is com.example.shuigongrizhi.ui.viewmodel.WeatherState.Error -> {
                val errorState = weatherState as com.example.shuigongrizhi.ui.viewmodel.WeatherState.Error
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = errorState.message,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.getCurrentWeather() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.2f)
                            )
                        ) {
                            Text("重试", color = Color.White)
                        }
                    }
                }
            }
            else -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "点击刷新获取天气信息",
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun MainWeatherCard(
    cityName: String,
    temperature: String,
    condition: String,
    description: String,
    feelsLike: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = cityName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = temperature,
                fontSize = 64.sp,
                fontWeight = FontWeight.Light,
                color = Color.White
            )
            
            Text(
                text = condition,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            
            Text(
                text = description,
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "体感温度 $feelsLike",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun WeatherDetailsGrid(
    humidity: String,
    windSpeed: String,
    windDirection: String,
    pressure: String,
    visibility: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            WeatherDetailItem(
                icon = Icons.Default.Water,
                title = "湿度",
                value = humidity,
                modifier = Modifier.weight(1f)
            )
            WeatherDetailItem(
                icon = Icons.Default.Air,
                title = "风速",
                value = windSpeed,
                modifier = Modifier.weight(1f)
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            WeatherDetailItem(
                icon = Icons.Default.Navigation,
                title = "风向",
                value = windDirection,
                modifier = Modifier.weight(1f)
            )
            WeatherDetailItem(
                icon = Icons.Default.Speed,
                title = "气压",
                value = pressure,
                modifier = Modifier.weight(1f)
            )
        }
        
        WeatherDetailItem(
            icon = Icons.Default.Visibility,
            title = "能见度",
            value = visibility,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun WeatherDetailItem(
    icon: ImageVector,
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = title,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
            
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}