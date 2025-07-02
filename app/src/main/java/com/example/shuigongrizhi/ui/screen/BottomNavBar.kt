package com.example.shuigongrizhi.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BottomNavBar(
    selectedIndex: Int,
    onBackClick: () -> Unit = {},
    onDesktopClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onProjectClick: () -> Unit = {},
    onMediaClick: () -> Unit = {}
) {
    val items = listOf(
        Triple(Icons.Default.Home, "主页", onHomeClick),
        Triple(Icons.Default.Assignment, "项目管理", onProjectClick),
        Triple(Icons.Default.PhotoLibrary, "媒体管理", onMediaClick),
        Triple(Icons.Default.Settings, "设置", onDesktopClick)
    )
    NavigationBar(
        containerColor = Color(0xFF2D2540),
        tonalElevation = 0.dp
    ) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = index == selectedIndex,
                onClick = { item.third() },
                icon = {
                    Icon(
                        imageVector = item.first,
                        contentDescription = item.second,
                        tint = if (index == selectedIndex) Color(0xFF8D6EFF) else Color(0xFFB0A8B9),
                        modifier = Modifier.size(28.dp) // 增大图标尺寸
                    )
                },
                label = {
                    Text(
                        text = item.second,
                        color = if (index == selectedIndex) Color(0xFF8D6EFF) else Color(0xFFB0A8B9),
                        fontSize = 12.sp // 设置合适的字体大小
                    )
                }
            )
        }
    }
}