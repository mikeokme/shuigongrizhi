package com.example.shuigongrizhi.core

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 权限管理器
 * 提供统一的权限请求和管理功能
 */
class PermissionManager(private val activity: ComponentActivity) {
    
    private val _permissionState = MutableStateFlow(PermissionState())
    val permissionState: StateFlow<PermissionState> = _permissionState.asStateFlow()
    
    private var permissionLauncher: ActivityResultLauncher<Array<String>>? = null
    private var onPermissionResult: ((Map<String, Boolean>) -> Unit)? = null
    
    /**
     * 初始化权限管理器
     */
    fun initialize() {
        permissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            Logger.business("权限请求结果: $permissions")
            updatePermissionState(permissions)
            onPermissionResult?.invoke(permissions)
        }
    }
    
    /**
     * 检查并请求必要的权限
     */
    fun checkAndRequestPermissions(
        permissions: Array<String> = getRequiredPermissions(),
        onResult: (Map<String, Boolean>) -> Unit = {}
    ) {
        Logger.business("检查权限: ${permissions.joinToString()}")
        
        val deniedPermissions = permissions.filter { permission ->
            !isPermissionGranted(permission)
        }
        
        if (deniedPermissions.isEmpty()) {
            Logger.business("所有权限已授予")
            val allGranted = permissions.associateWith { true }
            updatePermissionState(allGranted)
            onResult(allGranted)
        } else {
            Logger.business("需要请求权限: ${deniedPermissions.joinToString()}")
            onPermissionResult = onResult
            permissionLauncher?.launch(deniedPermissions.toTypedArray())
        }
    }
    
    /**
     * 检查单个权限是否已授予
     */
    fun isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * 检查多个权限是否都已授予
     */
    fun arePermissionsGranted(permissions: Array<String>): Boolean {
        return permissions.all { isPermissionGranted(it) }
    }
    
    /**
     * 获取应用所需的权限列表
     */
    private fun getRequiredPermissions(): Array<String> {
        val permissions = mutableListOf<String>()
        
        // 相机权限
        permissions.add(Manifest.permission.CAMERA)
        
        // 存储权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.addAll(
                listOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO
                )
            )
        } else {
            permissions.addAll(
                listOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        }
        
        // 位置权限（如果需要）
        permissions.addAll(
            listOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
        
        // 网络权限（通常自动授予）
        permissions.add(Manifest.permission.INTERNET)
        
        return permissions.toTypedArray()
    }
    
    /**
     * 更新权限状态
     */
    private fun updatePermissionState(permissions: Map<String, Boolean>) {
        val currentState = _permissionState.value
        val newState = currentState.copy(
            cameraGranted = permissions[Manifest.permission.CAMERA] ?: currentState.cameraGranted,
            storageGranted = checkStoragePermissions(permissions),
            locationGranted = checkLocationPermissions(permissions),
            allPermissionsGranted = permissions.values.all { it }
        )
        
        _permissionState.value = newState
        Logger.business("权限状态更新: $newState")
    }
    
    /**
     * 检查存储权限状态
     */
    private fun checkStoragePermissions(permissions: Map<String, Boolean>): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            (permissions[Manifest.permission.READ_MEDIA_IMAGES] ?: false) &&
                    (permissions[Manifest.permission.READ_MEDIA_VIDEO] ?: false)
        } else {
            (permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: false) &&
                    (permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: false)
        }
    }
    
    /**
     * 检查位置权限状态
     */
    private fun checkLocationPermissions(permissions: Map<String, Boolean>): Boolean {
        return (permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false) ||
                (permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false)
    }
    
    /**
     * 获取当前权限状态
     */
    fun getCurrentPermissionState(): PermissionState {
        val permissions = getRequiredPermissions()
        val permissionMap = permissions.associateWith { isPermissionGranted(it) }
        updatePermissionState(permissionMap)
        return _permissionState.value
    }
    
    /**
     * 检查是否需要显示权限说明
     */
    fun shouldShowRequestPermissionRationale(permission: String): Boolean {
        return activity.shouldShowRequestPermissionRationale(permission)
    }
    
    /**
     * 获取权限说明文本
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
     * 创建应用目录
     */
    fun setupAppDirectories(): Boolean {
        return try {
            val success = Utils.File.createAppDirectories(activity)
            Logger.business("应用目录创建${if (success) "成功" else "失败"}")
            success
        } catch (e: Exception) {
            Logger.exception(e, "创建应用目录失败")
            false
        }
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