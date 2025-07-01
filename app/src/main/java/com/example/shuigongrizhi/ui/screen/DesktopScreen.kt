package com.example.shuigongrizhi.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shuigongrizhi.R

@Composable
fun DesktopScreen(
    onNavigateBack: () -> Unit = {}
) {
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
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "返回",
                    tint = Color(0xFF8D6EFF)
                )
            }
            Text(
                text = "桌面",
                color = Color(0xFF8D6EFF),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        
        // 桌面应用网格
        val desktopApps = listOf(
            DesktopApp("日历", Icons.Default.CalendarToday),
            DesktopApp("计算器", Icons.Default.Calculate),
            DesktopApp("笔记", Icons.Default.Note),
            DesktopApp("天气", Icons.Default.Cloud),
            DesktopApp("相册", Icons.Default.PhotoLibrary),
            DesktopApp("设置", Icons.Default.Settings),
            DesktopApp("文件", Icons.Default.Folder),
            DesktopApp("联系人", Icons.Default.Contacts)
        )
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(desktopApps) { app ->
                DesktopAppItem(app)
            }
        }
        
        // 底部导航栏
        BottomNavBar(
            selectedIndex = 2,
            onBackClick = onNavigateBack,
            onDesktopClick = { /* 已在桌面，不执行任何操作 */ },
            onHomeClick = { /* 跳转到首页 */ }
        )
    }
}

data class DesktopApp(val name: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@Composable
fun DesktopAppItem(app: DesktopApp) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Card(
            modifier = Modifier
                .size(64.dp)
                .padding(4.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF2D2540)
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = app.icon,
                    contentDescription = app.name,
                    tint = Color(0xFF8D6EFF),
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = app.name,
            color = Color.White,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}