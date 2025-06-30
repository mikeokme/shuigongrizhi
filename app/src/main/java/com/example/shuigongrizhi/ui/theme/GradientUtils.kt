package com.example.shuigongrizhi.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * 工具类，用于创建渐变背景修饰符
 */
object GradientUtils {
    
    /**
     * 创建渐变背景修饰符
     * 
     * @param startColor 渐变起始颜色
     * @param endColor 渐变结束颜色
     * @param cornerRadius 圆角大小，默认16dp
     * @return 带有渐变背景和圆角的Modifier
     */
    @Composable
    fun gradientBackground(
        startColor: Color,
        endColor: Color,
        cornerRadius: Int = 16
    ): Modifier {
        return Modifier
            .clip(RoundedCornerShape(cornerRadius.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(startColor, endColor)
                )
            )
    }
    
    /**
     * 根据项目类型获取对应的渐变背景修饰符
     * 
     * @param projectType 项目类型
     * @param cornerRadius 圆角大小，默认16dp
     * @return 带有渐变背景和圆角的Modifier
     */
    @Composable
    fun projectTypeGradient(projectType: String, cornerRadius: Int = 16): Modifier {
        val (startColor, endColor) = when (projectType.lowercase()) {
            "水库" -> Pair(ChampagneStart, ChampagneEnd)
            "河道" -> Pair(SparklingStart, SparklingEnd)
            "灌区" -> Pair(RedWineStart, RedWineEnd)
            "水电站" -> Pair(RoseWineStart, RoseWineEnd)
            "泵站" -> Pair(SweetWineStart, SweetWineEnd)
            else -> Pair(CognacStart, CognacEnd)
        }
        
        return gradientBackground(startColor, endColor, cornerRadius)
    }
}