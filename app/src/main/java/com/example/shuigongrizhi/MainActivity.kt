package com.example.shuigongrizhi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.example.shuigongrizhi.core.AppConfig
import com.example.shuigongrizhi.core.Logger
import com.example.shuigongrizhi.core.Utils
import com.example.shuigongrizhi.core.rememberAppPermissionsState
import com.example.shuigongrizhi.core.showToast
import com.example.shuigongrizhi.navigation.AppNavigation
import com.example.shuigongrizhi.ui.theme.ShuigongrizhiTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var appConfig: AppConfig

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        Logger.lifecycle("MainActivity", "onCreate")

        setContent {
            val context = LocalContext.current
            val permissionsState = rememberAppPermissionsState()

            LaunchedEffect(permissionsState) {
                if (!permissionsState.allPermissionsGranted) {
                    permissionsState.launchMultiplePermissionRequest()
                }
            }

            LaunchedEffect(permissionsState.allPermissionsGranted) {
                if (permissionsState.allPermissionsGranted) {
                    if (appConfig.isFirstLaunch) {
                        showToast("权限已授予，正在初始化应用...")
                        appConfig.markFirstLaunchCompleted()
                    }
                    val success = Utils.File.createAppDirectories(context)
                    if (!success) {
                        showToast("存储目录创建失败，部分功能可能受限")
                    }
                }
            }

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