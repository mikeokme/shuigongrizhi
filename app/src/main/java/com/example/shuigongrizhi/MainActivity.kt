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
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.example.shuigongrizhi.core.showToast
import com.example.shuigongrizhi.navigation.AppNavigation
import com.example.shuigongrizhi.ui.theme.ShuigongrizhiTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.shuigongrizhi.ui.components.PermissionWrapper

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
            ShuigongrizhiTheme {
                PermissionWrapper(appConfig = appConfig) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            val navController = rememberNavController()
                            AppNavigation(navController = navController, onNavigate = { route -> navController.navigate(route) })
                        }
                    }
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