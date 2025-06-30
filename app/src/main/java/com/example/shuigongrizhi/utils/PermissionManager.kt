package com.example.shuigongrizhi.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class PermissionManager(private val activity: ComponentActivity) {
    
    // 存储权限请求启动器
    private val storagePermissionLauncher: ActivityResultLauncher<Array<String>> =
        activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            handleStoragePermissionResult(permissions)
        }
    
    // 管理外部存储权限启动器 (Android 11+)
    private val manageStorageLauncher: ActivityResultLauncher<Intent> =
        activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    onStoragePermissionGranted()
                } else {
                    onStoragePermissionDenied()
                }
            }
        }
    
    private var onPermissionGranted: (() -> Unit)? = null
    private var onPermissionDenied: (() -> Unit)? = null
    
    /**
     * 请求存储权限
     */
    fun requestStoragePermissions(
        onGranted: () -> Unit,
        onDenied: () -> Unit
    ) {
        this.onPermissionGranted = onGranted
        this.onPermissionDenied = onDenied
        
        if (hasStoragePermissions()) {
            onGranted()
            return
        }
        
        // Android 11+ 需要特殊处理
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                // 请求管理外部存储权限
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.parse("package:${activity.packageName}")
                }
                manageStorageLauncher.launch(intent)
            } else {
                onGranted()
            }
        } else {
            // Android 10 及以下版本
            val permissions = mutableListOf<String>()
            
            if (!hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            
            if (!hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            
            if (permissions.isNotEmpty()) {
                storagePermissionLauncher.launch(permissions.toTypedArray())
            } else {
                onGranted()
            }
        }
    }
    
    /**
     * 检查是否有存储权限
     */
    fun hasStoragePermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE) &&
            hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }
    
    /**
     * 检查单个权限
     */
    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * 处理存储权限请求结果
     */
    private fun handleStoragePermissionResult(permissions: Map<String, Boolean>) {
        val allGranted = permissions.values.all { it }
        
        if (allGranted) {
            onStoragePermissionGranted()
        } else {
            onStoragePermissionDenied()
        }
    }
    
    private fun onStoragePermissionGranted() {
        activity.lifecycleScope.launch {
            onPermissionGranted?.invoke()
        }
    }
    
    private fun onStoragePermissionDenied() {
        activity.lifecycleScope.launch {
            onPermissionDenied?.invoke()
        }
    }
    
    /**
     * 创建应用数据目录
     */
    fun createAppDataDirectories(): Boolean {
        return try {
            val context = activity.applicationContext
            
            // 创建项目数据目录
            val projectDataDir = context.getExternalFilesDir("ProjectData")
            projectDataDir?.mkdirs()
            
            // 创建图片目录
            val picturesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            picturesDir?.mkdirs()
            
            // 创建视频目录
            val moviesDir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
            moviesDir?.mkdirs()
            
            // 创建文档目录
            val documentsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            documentsDir?.mkdirs()
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * 获取项目数据存储路径
     */
    fun getProjectDataPath(): String? {
        return activity.getExternalFilesDir("ProjectData")?.absolutePath
    }
    
    /**
     * 获取应用存储信息
     */
    fun getStorageInfo(): Map<String, String?> {
        val context = activity.applicationContext
        return mapOf(
            "projectData" to context.getExternalFilesDir("ProjectData")?.absolutePath,
            "pictures" to context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath,
            "movies" to context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)?.absolutePath,
            "documents" to context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)?.absolutePath,
            "cache" to context.externalCacheDir?.absolutePath
        )
    }
}