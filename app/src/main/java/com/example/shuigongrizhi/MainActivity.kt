package com.example.shuigongrizhi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.shuigongrizhi.core.Logger
import com.example.shuigongrizhi.core.PermissionManager
import com.example.shuigongrizhi.core.showToast
import com.example.shuigongrizhi.navigation.AppNavigation
import com.example.shuigongrizhi.ui.theme.ShuigongrizhiTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private lateinit var permissionManager: PermissionManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        Logger.lifecycle("MainActivity", "onCreate")
        
        // 初始化权限管理器
        permissionManager = PermissionManager(this)
        permissionManager.initialize()
        
        // 检查和请求权限
        checkAndRequestPermissions()
        
        setContent {
            ShuigongrizhiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavigation(navController = navController)
                }
            }
        }
    }
    
    /**
     * 检查和请求权限
     */
    private fun checkAndRequestPermissions() {
        Logger.business("开始检查应用权限")
        
        permissionManager.checkAndRequestPermissions { permissions ->
            val grantedPermissions = permissions.filter { it.value }
            val deniedPermissions = permissions.filter { !it.value }
            
            Logger.business("权限检查完成 - 已授予: ${grantedPermissions.size}, 被拒绝: ${deniedPermissions.size}")
            
            if (deniedPermissions.isNotEmpty()) {
                Logger.business("被拒绝的权限: ${deniedPermissions.keys.joinToString()}")
                showToast("部分权限被拒绝，某些功能可能受限")
            }
            
            // 无论权限状态如何，都尝试设置应用目录
            setupAppDirectories()
        }
    }
    
    /**
     * 设置应用存储目录
     */
    private fun setupAppDirectories() {
        Logger.business("开始设置应用存储目录")
        
        val success = permissionManager.setupAppDirectories()
        if (success) {
            Logger.business("应用存储目录创建成功")
            showToast("应用存储目录已准备就绪")
        } else {
            Logger.business("应用存储目录创建失败")
            showToast("存储目录创建失败，部分功能可能受限")
        }
    }
    
    override fun onStart() {
        super.onStart()
        Logger.lifecycle("MainActivity", "onStart")
    }
    
    override fun onResume() {
        super.onResume()
        Logger.lifecycle("MainActivity", "onResume")
    }
    
    override fun onPause() {
        super.onPause()
        Logger.lifecycle("MainActivity", "onPause")
    }
    
    override fun onStop() {
        super.onStop()
        Logger.lifecycle("MainActivity", "onStop")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Logger.lifecycle("MainActivity", "onDestroy")
    }
}