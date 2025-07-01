package com.example.shuigongrizhi.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shuigongrizhi.ui.viewmodel.LocationViewModel
import com.example.shuigongrizhi.ui.viewmodel.MapProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationScreen(
    viewModel: LocationViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("位置服务") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 地图类型选择
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MapProvider.values().forEach { provider ->
                    Button(
                        onClick = { viewModel.setMapProvider(provider) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (uiState.mapProvider == provider) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Text(provider.name)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // 地图展示（伪代码，需根据 provider 切换实际地图组件）
            when (uiState.mapProvider) {
                MapProvider.GOOGLE -> {
                    Text("Google 地图区域（请集成 Google Maps Compose）")
                }
                MapProvider.GAODE -> {
                    Text("高德地图区域（请集成高德地图SDK）")
                }
                MapProvider.BAIDU -> {
                    Text("百度地图区域（请集成百度地图SDK）")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("经度: ${uiState.longitude ?: "--"}")
            Text("纬度: ${uiState.latitude ?: "--"}")
            Text("地址: ${uiState.address ?: "--"}")
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { viewModel.fetchCurrentLocation() }) {
                Text("获取当前位置")
            }
            if (uiState.error != null) {
                Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("历史轨迹：")
            uiState.history.forEach {
                Text("${it.timestamp}: (${it.latitude}, ${it.longitude}) [${it.provider}]")
            }
        }
    }
}