package com.example.shuigongrizhi.core

import android.Manifest
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState

/**
 * 提供一个 Composable 函数来管理应用所需的核心权限。
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberAppPermissionsState(): MultiplePermissionsState {
    val permissions = remember {
        mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.READ_MEDIA_IMAGES)
                add(Manifest.permission.READ_MEDIA_VIDEO)
            } else {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }
    return rememberMultiplePermissionsState(permissions = permissions)
}

/**
 * 获取特定权限的说明文本。
 */
fun getPermissionRationaleText(permission: String): String {
    return when (permission) {
        Manifest.permission.CAMERA -> "需要相机权限来拍摄施工现场照片和视频"
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_VIDEO -> "需要存储权限来保存和读取施工日志文件"
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION -> "需要位置权限来记录施工地点信息"
        else -> "需要此权限来正常使用应用功能"
    }
}

/**
 * 权限状态数据类
 */
data class PermissionState(
    val cameraGranted: Boolean = false,
    val storageGranted: Boolean = false,
    val locationGranted: Boolean = false,
    val allPermissionsGranted: Boolean = false
) {
    /**
     * 检查核心权限是否已授予（相机和存储）
     */
    val corePermissionsGranted: Boolean
        get() = cameraGranted && storageGranted
    
    /**
     * 获取缺失的权限描述
     */
    fun getMissingPermissionsDescription(): String {
        val missing = mutableListOf<String>()
        if (!cameraGranted) missing.add("相机")
        if (!storageGranted) missing.add("存储")
        if (!locationGranted) missing.add("位置")
        
        return if (missing.isEmpty()) {
            "所有权限已授予"
        } else {
            "缺少权限: ${missing.joinToString("、")}"
        }
    }
}