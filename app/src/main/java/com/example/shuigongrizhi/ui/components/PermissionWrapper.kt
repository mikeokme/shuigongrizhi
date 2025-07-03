package com.example.shuigongrizhi.ui.components

import android.Manifest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.example.shuigongrizhi.core.AppConfig
import com.example.shuigongrizhi.core.Utils
import com.example.shuigongrizhi.core.showToast

import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionWrapper(
    appConfig: AppConfig,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    )

    LaunchedEffect(permissionsState) {
        if (!permissionsState.allPermissionsGranted) {
            permissionsState.launchMultiplePermissionRequest()
        }
    }

    LaunchedEffect(permissionsState.allPermissionsGranted) {
        if (permissionsState.allPermissionsGranted) {
            if (appConfig.isFirstLaunch) {
                context.showToast("权限已授予，正在初始化应用...")
                appConfig.markFirstLaunchCompleted()
            }
            val success = Utils.File.createAppDirectories(context)
            if (!success) {
                context.showToast("存储目录创建失败，部分功能可能受限")
            }
        }
    }

    content()
}