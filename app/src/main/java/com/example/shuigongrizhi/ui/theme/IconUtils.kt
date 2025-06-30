package com.example.shuigongrizhi.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * 图标工具类，提供线性风格的自定义图标
 */
object IconUtils {
    
    /**
     * 水库图标 - 线性风格
     * 
     * @param color 图标颜色
     * @param modifier 修饰符
     */
    @Composable
    fun ReservoirIcon(color: Color = IconLight, modifier: Modifier = Modifier.size(24.dp)) {
        Canvas(modifier = modifier) {
            val width = size.width
            val height = size.height
            val strokeWidth = width * 0.08f
            
            // 绘制水库轮廓
            val path = Path().apply {
                moveTo(width * 0.2f, height * 0.3f)
                lineTo(width * 0.2f, height * 0.7f)
                lineTo(width * 0.8f, height * 0.7f)
                lineTo(width * 0.8f, height * 0.3f)
                close()
            }
            
            drawPath(
                path = path,
                color = color,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            
            // 绘制水波纹
            drawLine(
                color = color,
                start = Offset(width * 0.3f, height * 0.5f),
                end = Offset(width * 0.7f, height * 0.5f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            
            drawLine(
                color = color,
                start = Offset(width * 0.35f, height * 0.4f),
                end = Offset(width * 0.65f, height * 0.4f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }
    
    /**
     * 河道图标 - 线性风格
     * 
     * @param color 图标颜色
     * @param modifier 修饰符
     */
    @Composable
    fun RiverIcon(color: Color = IconLight, modifier: Modifier = Modifier.size(24.dp)) {
        Canvas(modifier = modifier) {
            val width = size.width
            val height = size.height
            val strokeWidth = width * 0.08f
            
            // 绘制河道曲线
            val path = Path().apply {
                moveTo(width * 0.2f, height * 0.4f)
                cubicTo(
                    width * 0.4f, height * 0.3f,
                    width * 0.6f, height * 0.5f,
                    width * 0.8f, height * 0.4f
                )
            }
            
            drawPath(
                path = path,
                color = color,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            
            // 绘制第二条河道曲线
            val path2 = Path().apply {
                moveTo(width * 0.2f, height * 0.6f)
                cubicTo(
                    width * 0.4f, height * 0.5f,
                    width * 0.6f, height * 0.7f,
                    width * 0.8f, height * 0.6f
                )
            }
            
            drawPath(
                path = path2,
                color = color,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
    }
    
    /**
     * 灌区图标 - 线性风格
     * 
     * @param color 图标颜色
     * @param modifier 修饰符
     */
    @Composable
    fun IrrigationIcon(color: Color = IconLight, modifier: Modifier = Modifier.size(24.dp)) {
        Canvas(modifier = modifier) {
            val width = size.width
            val height = size.height
            val strokeWidth = width * 0.08f
            
            // 绘制灌区网格
            drawLine(
                color = color,
                start = Offset(width * 0.2f, height * 0.2f),
                end = Offset(width * 0.2f, height * 0.8f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            
            drawLine(
                color = color,
                start = Offset(width * 0.5f, height * 0.2f),
                end = Offset(width * 0.5f, height * 0.8f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            
            drawLine(
                color = color,
                start = Offset(width * 0.8f, height * 0.2f),
                end = Offset(width * 0.8f, height * 0.8f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            
            drawLine(
                color = color,
                start = Offset(width * 0.2f, height * 0.2f),
                end = Offset(width * 0.8f, height * 0.2f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            
            drawLine(
                color = color,
                start = Offset(width * 0.2f, height * 0.5f),
                end = Offset(width * 0.8f, height * 0.5f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            
            drawLine(
                color = color,
                start = Offset(width * 0.2f, height * 0.8f),
                end = Offset(width * 0.8f, height * 0.8f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }
    
    /**
     * 水电站图标 - 线性风格
     * 
     * @param color 图标颜色
     * @param modifier 修饰符
     */
    @Composable
    fun HydropowerIcon(color: Color = IconLight, modifier: Modifier = Modifier.size(24.dp)) {
        Canvas(modifier = modifier) {
            val width = size.width
            val height = size.height
            val strokeWidth = width * 0.08f
            
            // 绘制水电站建筑
            drawLine(
                color = color,
                start = Offset(width * 0.3f, height * 0.2f),
                end = Offset(width * 0.3f, height * 0.7f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            
            drawLine(
                color = color,
                start = Offset(width * 0.7f, height * 0.2f),
                end = Offset(width * 0.7f, height * 0.7f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            
            drawLine(
                color = color,
                start = Offset(width * 0.3f, height * 0.2f),
                end = Offset(width * 0.7f, height * 0.2f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            
            // 绘制电力符号
            drawLine(
                color = color,
                start = Offset(width * 0.5f, height * 0.3f),
                end = Offset(width * 0.5f, height * 0.6f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            
            drawLine(
                color = color,
                start = Offset(width * 0.4f, height * 0.4f),
                end = Offset(width * 0.6f, height * 0.4f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            
            // 绘制水波纹
            drawLine(
                color = color,
                start = Offset(width * 0.2f, height * 0.8f),
                end = Offset(width * 0.8f, height * 0.8f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }
    
    /**
     * 泵站图标 - 线性风格
     * 
     * @param color 图标颜色
     * @param modifier 修饰符
     */
    @Composable
    fun PumpStationIcon(color: Color = IconLight, modifier: Modifier = Modifier.size(24.dp)) {
        Canvas(modifier = modifier) {
            val width = size.width
            val height = size.height
            val strokeWidth = width * 0.08f
            val centerX = width / 2
            val centerY = height / 2
            val radius = width * 0.3f
            
            // 绘制圆形泵体
            drawCircle(
                color = color,
                radius = radius,
                center = Offset(centerX, centerY),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            
            // 绘制泵的入口管道
            drawLine(
                color = color,
                start = Offset(width * 0.2f, height * 0.8f),
                end = Offset(width * 0.4f, height * 0.6f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            
            // 绘制泵的出口管道
            drawLine(
                color = color,
                start = Offset(width * 0.6f, height * 0.4f),
                end = Offset(width * 0.8f, height * 0.2f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }
    
    /**
     * 导出图标 - 线性风格
     * 
     * @param color 图标颜色
     * @param modifier 修饰符
     */
    @Composable
    fun ExportIcon(color: Color = IconLight, modifier: Modifier = Modifier.size(24.dp)) {
        Canvas(modifier = modifier) {
            val width = size.width
            val height = size.height
            val strokeWidth = width * 0.08f
            
            // 绘制文档轮廓
            val path = Path().apply {
                moveTo(width * 0.3f, height * 0.2f)
                lineTo(width * 0.7f, height * 0.2f)
                lineTo(width * 0.7f, height * 0.8f)
                lineTo(width * 0.3f, height * 0.8f)
                close()
            }
            
            drawPath(
                path = path,
                color = color,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            
            // 绘制导出箭头
            drawLine(
                color = color,
                start = Offset(width * 0.4f, height * 0.4f),
                end = Offset(width * 0.6f, height * 0.4f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            
            drawLine(
                color = color,
                start = Offset(width * 0.5f, height * 0.3f),
                end = Offset(width * 0.5f, height * 0.5f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            
            drawLine(
                color = color,
                start = Offset(width * 0.4f, height * 0.6f),
                end = Offset(width * 0.6f, height * 0.6f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }
}