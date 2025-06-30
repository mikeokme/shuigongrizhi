package com.example.shuigongrizhi.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shuigongrizhi.ui.theme.*

/**
 * 渐变背景卡片组件
 * 
 * @param title 卡片标题
 * @param startColor 渐变起始颜色
 * @param endColor 渐变结束颜色
 * @param onClick 点击事件回调
 * @param icon 卡片图标（可选）
 * @param content 卡片内容（可选）
 */
@Composable
fun GradientCard(
    title: String,
    startColor: Color,
    endColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null,
    content: @Composable (RowScope.() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .then(GradientUtils.gradientBackground(startColor, endColor))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (icon != null) {
                    Box(modifier = Modifier.size(32.dp)) {
                        icon()
                    }
                }
                
                Text(
                    text = title,
                    color = TextWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (content != null) {
                Row(content = content)
            }
        }
    }
}

/**
 * 项目类型渐变卡片
 * 
 * @param title 卡片标题
 * @param projectType 项目类型
 * @param onClick 点击事件回调
 * @param modifier 修饰符
 * @param content 卡片内容（可选）
 */
@Composable
fun ProjectTypeCard(
    title: String,
    projectType: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (RowScope.() -> Unit)? = null
) {
    val (startColor, endColor) = when (projectType.lowercase()) {
        "水库" -> Pair(ChampagneStart, ChampagneEnd)
        "河道" -> Pair(SparklingStart, SparklingEnd)
        "灌区" -> Pair(RedWineStart, RedWineEnd)
        "水电站" -> Pair(RoseWineStart, RoseWineEnd)
        "泵站" -> Pair(SweetWineStart, SweetWineEnd)
        else -> Pair(CognacStart, CognacEnd)
    }
    
    val icon: @Composable () -> Unit = when (projectType.lowercase()) {
        "水库" -> { { IconUtils.ReservoirIcon() } }
        "河道" -> { { IconUtils.RiverIcon() } }
        "灌区" -> { { IconUtils.IrrigationIcon() } }
        "水电站" -> { { IconUtils.HydropowerIcon() } }
        "泵站" -> { { IconUtils.PumpStationIcon() } }
        else -> { { IconUtils.ReservoirIcon() } }
    }
    
    GradientCard(
        title = title,
        startColor = startColor,
        endColor = endColor,
        onClick = onClick,
        modifier = modifier,
        icon = icon,
        content = content
    )
}