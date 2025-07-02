package com.example.shuigongrizhi.ui.theme

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 间距规范
object Spacing {
    val extraSmall = 4.dp
    val small = 8.dp
    val medium = 16.dp
    val large = 24.dp
    val extraLarge = 32.dp
    val huge = 48.dp
}

// 圆角规范
object CornerRadius {
    val small = 4.dp
    val medium = 8.dp
    val large = 12.dp
    val extraLarge = 16.dp
    val round = 50.dp
}

// 图标尺寸规范
object IconSize {
    val small = 16.dp
    val medium = 24.dp
    val large = 32.dp
    val extraLarge = 48.dp
    val huge = 64.dp
}

// 字体尺寸规范
object FontSize {
    val caption = 10.sp
    val small = 12.sp
    val body = 14.sp
    val subtitle = 16.sp
    val title = 18.sp
    val heading = 20.sp
    val display = 24.sp
}

// 卡片规范
object CardDefaults {
    val elevation = 4.dp
    val cornerRadius = CornerRadius.large
    val padding = Spacing.medium
}

// 按钮规范
object ButtonDefaults {
    val height = 48.dp
    val cornerRadius = CornerRadius.medium
    val padding = Spacing.medium
}

// 底部导航栏规范
object BottomNavDefaults {
    val height = 80.dp
    val iconSize = IconSize.large
    val fontSize = FontSize.small
    val padding = Spacing.small
}