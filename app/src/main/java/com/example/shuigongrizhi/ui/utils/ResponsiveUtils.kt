package com.example.shuigongrizhi.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 屏幕尺寸类型
 */
enum class ScreenSize {
    COMPACT,    // 手机竖屏
    MEDIUM,     // 手机横屏/小平板
    EXPANDED    // 大平板/桌面
}

/**
 * 响应式设计工具类
 */
object ResponsiveUtils {
    
    /**
     * 获取当前屏幕尺寸类型
     */
    @Composable
    fun getScreenSize(): ScreenSize {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp.dp
        
        return when {
            screenWidth < 600.dp -> ScreenSize.COMPACT
            screenWidth < 840.dp -> ScreenSize.MEDIUM
            else -> ScreenSize.EXPANDED
        }
    }
    
    /**
     * 根据屏幕尺寸获取网格列数
     */
    @Composable
    fun getGridColumns(compact: Int = 2, medium: Int = 3, expanded: Int = 4): Int {
        return when (getScreenSize()) {
            ScreenSize.COMPACT -> compact
            ScreenSize.MEDIUM -> medium
            ScreenSize.EXPANDED -> expanded
        }
    }
    
    /**
     * 根据屏幕尺寸获取间距
     */
    @Composable
    fun getResponsivePadding(compact: Dp = 8.dp, medium: Dp = 16.dp, expanded: Dp = 24.dp): Dp {
        return when (getScreenSize()) {
            ScreenSize.COMPACT -> compact
            ScreenSize.MEDIUM -> medium
            ScreenSize.EXPANDED -> expanded
        }
    }
    
    /**
     * 根据屏幕尺寸获取字体大小缩放比例
     */
    @Composable
    fun getFontScale(): Float {
        return when (getScreenSize()) {
            ScreenSize.COMPACT -> 1.0f
            ScreenSize.MEDIUM -> 1.1f
            ScreenSize.EXPANDED -> 1.2f
        }
    }
    
    /**
     * 判断是否为横屏
     */
    @Composable
    fun isLandscape(): Boolean {
        val configuration = LocalConfiguration.current
        return configuration.screenWidthDp > configuration.screenHeightDp
    }
    
    /**
     * 获取底部导航栏高度
     */
    @Composable
    fun getBottomNavHeight(): Dp {
        return when (getScreenSize()) {
            ScreenSize.COMPACT -> 80.dp
            ScreenSize.MEDIUM -> 90.dp
            ScreenSize.EXPANDED -> 100.dp
        }
    }
    
    /**
     * 获取卡片最大宽度
     */
    @Composable
    fun getMaxCardWidth(): Dp {
        return when (getScreenSize()) {
            ScreenSize.COMPACT -> Dp.Unspecified
            ScreenSize.MEDIUM -> 400.dp
            ScreenSize.EXPANDED -> 500.dp
        }
    }
}

/**
 * 响应式网格布局配置
 */
data class ResponsiveGridConfig(
    val columns: Int,
    val spacing: Dp,
    val contentPadding: Dp
)

/**
 * 获取响应式网格配置
 */
@Composable
fun getResponsiveGridConfig(): ResponsiveGridConfig {
    val screenSize = ResponsiveUtils.getScreenSize()
    
    return when (screenSize) {
        ScreenSize.COMPACT -> ResponsiveGridConfig(
            columns = 2,
            spacing = 8.dp,
            contentPadding = 16.dp
        )
        ScreenSize.MEDIUM -> ResponsiveGridConfig(
            columns = 3,
            spacing = 12.dp,
            contentPadding = 20.dp
        )
        ScreenSize.EXPANDED -> ResponsiveGridConfig(
            columns = 4,
            spacing = 16.dp,
            contentPadding = 24.dp
        )
    }
}