package com.example.shuigongrizhi.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*
import com.example.shuigongrizhi.R

@Composable
fun WeatherAnimation(weatherCondition: String, modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(getWeatherAnimation(weatherCondition))
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever
    )

    Box(modifier = modifier) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(100.dp)
        )
    }
}

fun getWeatherAnimation(weatherCondition: String): LottieCompositionSpec {
    return when {
        weatherCondition.contains("clear", ignoreCase = true) -> LottieCompositionSpec.RawRes(R.raw.sunny)
        weatherCondition.contains("clouds", ignoreCase = true) -> LottieCompositionSpec.RawRes(R.raw.cloudy)
        weatherCondition.contains("rain", ignoreCase = true) -> LottieCompositionSpec.RawRes(R.raw.rainy)
        weatherCondition.contains("snow", ignoreCase = true) -> LottieCompositionSpec.RawRes(R.raw.snowy)
        weatherCondition.contains("thunderstorm", ignoreCase = true) -> LottieCompositionSpec.RawRes(R.raw.thunderstorm)
        else -> LottieCompositionSpec.RawRes(R.raw.sunny) // 默认晴天
    }
}