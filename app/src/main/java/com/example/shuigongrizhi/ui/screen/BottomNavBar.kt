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

@Composable
fun BottomNavBar(
    selectedIndex: Int,
    onBackClick: () -> Unit = {},
    onDesktopClick: () -> Unit = {},
    onHomeClick: () -> Unit = {}
) {
    val items = listOf(
        Triple(Icons.Default.ArrowBack, "返回", onBackClick),
        Triple(Icons.Default.Home, "首页", onHomeClick),
        Triple(Icons.Default.DesktopWindows, "桌面", onDesktopClick)
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
                        tint = if (index == selectedIndex) Color(0xFFFF6F91) else Color(0xFFB0A8B9)
                    )
                },
                label = {
                    Text(
                        text = item.second,
                        color = if (index == selectedIndex) Color(0xFFFF6F91) else Color(0xFFB0A8B9)
                    )
                }
            )
        }
    }
}