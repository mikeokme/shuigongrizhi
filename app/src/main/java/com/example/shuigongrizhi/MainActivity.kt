package com.example.shuigongrizhi

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.shuigongrizhi.navigation.AppNavigation
import com.example.shuigongrizhi.ui.theme.ShuigongrizhiTheme
import com.example.shuigongrizhi.utils.PermissionManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private lateinit var permissionManager: PermissionManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // 初始化权限管理器
        permissionManager = PermissionManager(this)
        
        // 检查和请求存储权限
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
     * 检查和请求存储权限
     */
    private fun checkAndRequestPermissions() {
        permissionManager.requestStoragePermissions(
            onGranted = {
                Log.d("MainActivity", "存储权限已授予")
                setupAppDirectories()
            },
            onDenied = {
                Log.w("MainActivity", "存储权限被拒绝")
                Toast.makeText(
                    this,
                    "应用需要存储权限来保存项目数据，请在设置中手动授予权限",
                    Toast.LENGTH_LONG
                ).show()
                // 即使权限被拒绝，也尝试创建应用私有目录
                setupAppDirectories()
            }
        )
    }
    
    /**
     * 设置应用存储目录
     */
    private fun setupAppDirectories() {
        val success = permissionManager.createAppDataDirectories()
        if (success) {
            Log.d("MainActivity", "应用存储目录创建成功")
            
            // 打印存储路径信息
            val storageInfo = permissionManager.getStorageInfo()
            storageInfo.forEach { (type, path) ->
                Log.d("MainActivity", "$type 存储路径: $path")
            }
            
            Toast.makeText(
                this,
                "应用存储目录已准备就绪",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Log.e("MainActivity", "应用存储目录创建失败")
            Toast.makeText(
                this,
                "存储目录创建失败，部分功能可能受限",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}