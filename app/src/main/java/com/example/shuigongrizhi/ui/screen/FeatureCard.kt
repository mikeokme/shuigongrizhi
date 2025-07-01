package com.example.shuigongrizhi.ui.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon

@Composable
fun FeatureCard(
    title: String,
    iconRes: Int?,
    gradient: Brush,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    // 添加交互状态管理
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // 添加按压动画效果
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "card_scale"
    )
    
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .scale(scale) // 应用缩放动画
            .background(gradient, RoundedCornerShape(18.dp)) // 稍微增大圆角
            .clickable(
                interactionSource = interactionSource,
                indication = null, // 使用自定义动画效果
                onClick = onClick
            )
            .padding(18.dp), // 增加内边距
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (iconRes != null) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(52.dp) // 增大图标尺寸
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(52.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp)) // 增加间距
            Text(
                text = title,
                color = Color.White,
                fontSize = 15.sp, // 稍微增大字体
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodyMedium, // 使用一致的字体样式
                maxLines = 2 // 允许两行显示
            )
        }
    }
}