package com.example.shuigongrizhi.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.shuigongrizhi.ui.theme.*

/**
 * 统一的加载组件
 */
@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    message: String = "加载中..."
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.padding(Spacing.large),
            shape = RoundedCornerShape(CornerRadius.large),
            colors = AppCardDefaults.cardColors(),
            elevation = AppCardDefaults.cardElevation()
        ) {
            Column(
                modifier = Modifier.padding(Spacing.large),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.medium)
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 3.dp
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * 统一的错误组件
 */
@Composable
fun ErrorMessage(
    error: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.medium),
        shape = RoundedCornerShape(CornerRadius.medium),
        colors = AppCardDefaults.cardColors()
    ) {
        Column(
            modifier = Modifier.padding(Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            Text(
                text = "出现错误",
                style = MaterialTheme.typography.titleMedium,
                color = Error
            )
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (onRetry != null) {
                Spacer(modifier = Modifier.height(Spacing.small))
                Button(
                    onClick = onRetry,
                    colors = AppButtonDefaults.primaryButtonColors(),
                    shape = AppButtonDefaults.shape
                ) {
                    Text("重试")
                }
            }
        }
    }
}

/**
 * 空状态组件
 */
@Composable
fun EmptyState(
    message: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (actionText != null && onAction != null) {
                Button(
                    onClick = onAction,
                    colors = AppButtonDefaults.primaryButtonColors(),
                    shape = AppButtonDefaults.shape
                ) {
                    Text(actionText)
                }
            }
        }
    }
}

/**
 * 成功状态组件
 */
@Composable
fun SuccessMessage(
    message: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.medium),
        shape = RoundedCornerShape(CornerRadius.medium),
        colors = AppCardDefaults.cardColors()
    ) {
        Row(
            modifier = Modifier.padding(Spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            Text(
                text = "✓",
                style = MaterialTheme.typography.titleMedium,
                color = Success
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}