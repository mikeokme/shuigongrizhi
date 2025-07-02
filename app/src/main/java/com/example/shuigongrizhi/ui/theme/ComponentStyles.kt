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
    fun cardColors() = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurface
    )
    
    @Composable
    fun cardElevation() = CardDefaults.cardElevation(
        defaultElevation = CardDefaults.elevation
    )
    
    val shape = RoundedCornerShape(CardDefaults.cornerRadius)
}

// 按钮样式
object AppButtonDefaults {
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
    
    val shape = RoundedCornerShape(ButtonDefaults.cornerRadius)
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