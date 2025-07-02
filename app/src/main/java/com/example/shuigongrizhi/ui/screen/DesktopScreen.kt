package com.example.shuigongrizhi.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.shuigongrizhi.ui.theme.*
import com.example.shuigongrizhi.ui.utils.ResponsiveUtils
import com.example.shuigongrizhi.ui.utils.getResponsiveGridConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesktopScreen(
    onNavigateBack: () -> Unit = {}
) {
    val gridConfig = getResponsiveGridConfig()
    
    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = Spacing.large,
                        start = Spacing.large,
                        end = Spacing.large,
                        bottom = Spacing.medium
                    )
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(IconSize.large)
                    )
                }
                Text(
                    text = "常用工具",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        // 常用工具网格
        val commonTools = listOf(
            DesktopApp("计算器", Icons.Default.Calculate),
            DesktopApp("日历", Icons.Default.CalendarToday),
            DesktopApp("二维码", Icons.Default.QrCode),
            DesktopApp("单位换算", Icons.Default.SwapHoriz),
            DesktopApp("时间工具", Icons.Default.AccessTime),
            DesktopApp("文件管理", Icons.Default.Folder),
            DesktopApp("笔记本", Icons.Default.Note),
            DesktopApp("系统设置", Icons.Default.Settings),
            DesktopApp("网络工具", Icons.Default.Wifi),
            DesktopApp("测量工具", Icons.Default.Straighten),
            DesktopApp("颜色工具", Icons.Default.Palette),
            DesktopApp("备份恢复", Icons.Default.Backup)
        )
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(gridConfig.columns),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(gridConfig.contentPadding),
            verticalArrangement = Arrangement.spacedBy(gridConfig.spacing),
            horizontalArrangement = Arrangement.spacedBy(gridConfig.spacing)
        ) {
            items(commonTools) { tool ->
                DesktopAppItem(tool)
            }
        }
    }
}

data class DesktopApp(val name: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@Composable
fun DesktopAppItem(app: DesktopApp) {
    val maxWidth = ResponsiveUtils.getMaxCardWidth()
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .let { if (maxWidth != androidx.compose.ui.unit.Dp.Unspecified) it.widthIn(max = maxWidth) else it }
            .padding(Spacing.small)
    ) {
        Card(
            modifier = Modifier
                .size(IconSize.huge)
                .padding(Spacing.extraSmall),
            colors = AppCardDefaults.cardColors(),
            elevation = AppCardDefaults.cardElevation(),
            shape = AppCardDefaults.shape
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = app.icon,
                    contentDescription = app.name,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(IconSize.large)
                )
            }
        }
        Spacer(modifier = Modifier.height(Spacing.extraSmall))
        Text(
            text = app.name,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}