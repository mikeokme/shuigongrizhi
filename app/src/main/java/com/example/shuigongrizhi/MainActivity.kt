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
import com.example.shuigongrizhi.core.AppConfig
import com.example.shuigongrizhi.core.Logger
import com.example.shuigongrizhi.core.PermissionManager
import com.example.shuigongrizhi.core.showToast
import com.example.shuigongrizhi.navigation.AppNavigation
import com.example.shuigongrizhi.ui.theme.ShuigongrizhiTheme
// import dagger.hilt.android.AndroidEntryPoint
// import javax.inject.Inject

// @AndroidEntryPoint // 临时禁用
class MainActivity : ComponentActivity() {
    
    // @Inject // 临时禁用
    // lateinit var appConfig: AppConfig
    private val appConfig = AppConfig() // 临时直接实例化
    
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
        
        // 如果是首次启动，强制请求所有权限
        if (appConfig.isFirstLaunch) {
            Logger.business("首次启动，请求所有必要权限")
            showToast("首次启动，正在申请必要权限...")
        }
        
        permissionManager.checkAndRequestPermissions { permissions ->
            val grantedPermissions = permissions.filter { it.value }
            val deniedPermissions = permissions.filter { !it.value }
            
            Logger.business("权限检查完成 - 已授予: ${grantedPermissions.size}, 被拒绝: ${deniedPermissions.size}")
            
            if (deniedPermissions.isNotEmpty()) {
                Logger.business("被拒绝的权限: ${deniedPermissions.keys.joinToString()}")
                if (appConfig.isFirstLaunch) {
                    showToast("部分权限被拒绝，建议在设置中手动开启以获得完整功能")
                } else {
                    showToast("部分权限被拒绝，某些功能可能受限")
                }
            } else if (appConfig.isFirstLaunch) {
                showToast("权限申请完成，欢迎使用淮工施工日志系统！")
            }
            
            // 标记首次启动完成
            if (appConfig.isFirstLaunch) {
                appConfig.markFirstLaunchCompleted()
                Logger.business("首次启动流程完成")
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