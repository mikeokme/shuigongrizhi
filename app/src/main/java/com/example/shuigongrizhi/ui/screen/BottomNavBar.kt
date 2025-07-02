package com.example.shuigongrizhi.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shuigongrizhi.ui.theme.*

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
        // 移除媒体管理选项，避免弹窗干扰
        Triple(Icons.Default.Build, "常用工具", onDesktopClick)
    )
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = AppShadows.medium,
        modifier = Modifier.height(BottomNavDefaults.height)
    ) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = index == selectedIndex,
                onClick = { item.third() },
                icon = {
                    Icon(
                        imageVector = item.first,
                        contentDescription = item.second,
                        modifier = Modifier.size(BottomNavDefaults.iconSize)
                    )
                },
                label = {
                    Text(
                        text = item.second,
                        fontSize = BottomNavDefaults.fontSize
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}