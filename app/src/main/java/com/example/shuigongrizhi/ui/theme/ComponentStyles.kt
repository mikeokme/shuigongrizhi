package com.example.shuigongrizhi.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// 卡片样式
object AppCardDefaults {
    @Composable
    fun cardColors(
        containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor: Color = MaterialTheme.colorScheme.onSurface,
        disabledContainerColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
        disabledContentColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    ) = CardDefaults.cardColors(
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor
    )
    
    @Composable
    fun cardElevation(
        defaultElevation: androidx.compose.ui.unit.Dp = 4.dp,
        pressedElevation: androidx.compose.ui.unit.Dp = 8.dp,
        focusedElevation: androidx.compose.ui.unit.Dp = 4.dp,
        hoveredElevation: androidx.compose.ui.unit.Dp = 6.dp,
        draggedElevation: androidx.compose.ui.unit.Dp = 8.dp,
        disabledElevation: androidx.compose.ui.unit.Dp = 0.dp
    ) = CardDefaults.cardElevation(
        defaultElevation = defaultElevation,
        pressedElevation = pressedElevation,
        focusedElevation = focusedElevation,
        hoveredElevation = hoveredElevation,
        draggedElevation = draggedElevation,
        disabledElevation = disabledElevation
    )
    
    val shape = RoundedCornerShape(12.dp)
}

// 按钮样式
object AppButtonDefaults {
    @Composable
    fun buttonColors(
        containerColor: Color = MaterialTheme.colorScheme.primary,
        contentColor: Color = MaterialTheme.colorScheme.onPrimary,
        disabledContainerColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
        disabledContentColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    ) = ButtonDefaults.buttonColors(
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor
    )
    
    @Composable
    fun primaryButtonColors() = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    )
    
    @Composable
    fun secondaryButtonColors() = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.secondary,
        contentColor = MaterialTheme.colorScheme.onSecondary
    )
    
    @Composable
    fun outlinedButtonColors() = ButtonDefaults.outlinedButtonColors(
        contentColor = MaterialTheme.colorScheme.primary
    )
    
    val shape = RoundedCornerShape(8.dp)
}

// 渐变色定义
object AppGradients {
    val primaryGradient = listOf(
        Primary,
        PrimaryVariant
    )
    
    val cardGradients = mapOf(
        "champagne" to listOf(ChampagneStart, ChampagneEnd),
        "sparkling" to listOf(SparklingStart, SparklingEnd),
        "redWine" to listOf(RedWineStart, RedWineEnd),
        "roseWine" to listOf(RoseWineStart, RoseWineEnd),
        "sweetWine" to listOf(SweetWineStart, SweetWineEnd),
        "cognac" to listOf(CognacStart, CognacEnd)
    )
}

// 阴影定义
object AppShadows {
    val small = 2.dp
    val medium = 4.dp
    val large = 8.dp
    val extraLarge = 16.dp
}

// 动画时长定义
object AppAnimations {
    const val FAST = 150
    const val NORMAL = 300
    const val SLOW = 500
    const val EXTRA_SLOW = 1000
}