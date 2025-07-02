package com.example.shuigongrizhi.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = TextWhite,
    primaryContainer = PrimaryVariant,
    onPrimaryContainer = TextWhite,
    secondary = Secondary,
    onSecondary = TextWhite,
    secondaryContainer = Secondary,
    onSecondaryContainer = TextWhite,
    tertiary = Tertiary,
    onTertiary = TextWhite,
    background = DeepPurple,
    onBackground = TextWhite,
    surface = DarkPurple,
    onSurface = TextWhite,
    surfaceVariant = CardBackground,
    onSurfaceVariant = TextGray,
    outline = TextGray,
    error = Error,
    onError = TextWhite
)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = TextWhite,
    primaryContainer = PrimaryVariant,
    onPrimaryContainer = TextWhite,
    secondary = Secondary,
    onSecondary = TextWhite,
    secondaryContainer = Secondary,
    onSecondaryContainer = TextWhite,
    tertiary = Tertiary,
    onTertiary = TextWhite,
    background = TextWhite,
    onBackground = DeepPurple,
    surface = TextWhite,
    onSurface = DeepPurple,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextGray,
    outline = TextGray,
    error = Error,
    onError = TextWhite
)

@Composable
fun ShuigongrizhiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // 跟随系统主题
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // 禁用动态颜色以保持一致的主题
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme // 支持浅色主题
    }

    // 设置状态栏颜色
    val activity = LocalContext.current as? Activity
    activity?.window?.statusBarColor = colorScheme.background.hashCode()

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}