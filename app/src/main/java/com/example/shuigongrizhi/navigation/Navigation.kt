package com.example.shuigongrizhi.navigation

import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
// import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.shuigongrizhi.ui.screen.*
import com.example.shuigongrizhi.ui.viewmodel.*
import com.example.shuigongrizhi.ui.screen.CameraScreen
import com.example.shuigongrizhi.ui.screen.MediaGalleryScreen
import com.example.shuigongrizhi.ui.screen.MediaDetailScreen
import com.example.shuigongrizhi.ui.screen.BottomNavBar
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.remember

// 导航路由常量
object NavigationRoutes {
    const val MAIN = "main"
    const val PROJECT_LIST = "project_list"
    const val PROJECT_SELECTION = "project_selection"
    const val PROJECT_SELECTION_FOR_CAMERA = "project_selection_for_camera"
    const val PROJECT_FORM = "project_form"
    const val PROJECT_FORM_WITH_ID = "project_form/{projectId}"
    const val PROJECT_DASHBOARD = "project_dashboard/{projectId}"
    const val LOG_ENTRY = "log_entry/{projectId}/{date}"
    const val LOG_LIST = "log_list/{projectId}"
    const val LOG_DETAIL = "log_detail/{logId}"
    const val EXPORT = "export/{projectId}"
    const val WEATHER_DETAIL = "weather_detail"
    const val CAMERA = "camera/{projectId}"
    const val MEDIA_GALLERY = "media_gallery/{projectId}"
    const val MEDIA_DETAIL = "media_detail/{mediaFileId}"
    const val PHOTO_DESCRIPTION = "photo_description/{photoUri}/{projectId}"
    const val LOCATION = "location"
    const val PDF_VIEWER = "pdf_viewer"
    const val DESKTOP = "desktop"
    const val DISCOVERY = "discovery"
    const val FEEDBACK = "feedback"
    const val WEATHER_SETTINGS = "weather_settings"
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    onNavigate: (String) -> Unit
) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val selectedIndex = when (currentRoute) {
        NavigationRoutes.MAIN -> 0
        NavigationRoutes.PROJECT_LIST -> 1
        NavigationRoutes.DESKTOP -> 2
        else -> 0
    }
    
    Scaffold(
        bottomBar = { 
             BottomNavBar(
                 selectedIndex = selectedIndex,
                 onHomeClick = { navController.navigate(NavigationRoutes.MAIN) {
                     // 清除回退栈，使首页成为唯一目的地
                     popUpTo(NavigationRoutes.MAIN) { inclusive = true }
                 }},
                 onProjectClick = { navController.navigate(NavigationRoutes.PROJECT_LIST) },
                 onMediaClick = { navController.navigate(NavigationRoutes.PROJECT_SELECTION) },
                 onDesktopClick = { navController.navigate(NavigationRoutes.DESKTOP) }
             )
         }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavigationRoutes.MAIN,
            modifier = Modifier.padding(
                bottom = innerPadding.calculateBottomPadding(),
                top = 0.dp // 移除顶部边距，让内容上移
            )
        ) {
            // 主界面
            composable(NavigationRoutes.MAIN) {
                MainScreen(
                    onLogClick = { navController.navigate(NavigationRoutes.PROJECT_LIST) },
                    onWeatherClick = { navController.navigate(NavigationRoutes.WEATHER_DETAIL) },
                    onCameraClick = { navController.navigate(NavigationRoutes.PROJECT_SELECTION_FOR_CAMERA) },
                    onLocationClick = { navController.navigate(NavigationRoutes.LOCATION) },
                    onMediaClick = { /* TODO: 跳转到媒体管理页面 */ },
                    onProjectClick = { navController.navigate(NavigationRoutes.PROJECT_LIST) },
                    onProjectSelectionClick = { navController.navigate(NavigationRoutes.PROJECT_SELECTION) },
                    onSettingsClick = { /* TODO: 跳转到设置页面 */ },
                    onPdfViewerClick = { navController.navigate(NavigationRoutes.PDF_VIEWER) },
                    onDesktopClick = { navController.navigate(NavigationRoutes.DESKTOP) },
                    onFeedbackClick = { navController.navigate(NavigationRoutes.FEEDBACK) }
                )
            }
            // 项目列表页面
            composable(NavigationRoutes.PROJECT_LIST) {
                val viewModel: ProjectListViewModel = hiltViewModel()
                
                ProjectListScreen(
                    viewModel = viewModel,
                    onNavigateToCreateProject = {
                        navController.navigate(NavigationRoutes.PROJECT_FORM)
                    },
                    onNavigateToProject = { projectId ->
                        navController.navigate("project_dashboard/$projectId")
                    },
                    onNavigateToSettings = {
                        // TODO: Navigate to settings screen
                    }
                )
            }
            
            // 项目选择页面
            composable(NavigationRoutes.PROJECT_SELECTION) {
                val viewModel: ProjectListViewModel = hiltViewModel()
                ProjectSelectionScreen(
                    viewModel = viewModel,
                    onProjectSelected = { project ->
                        // 返回到上一页面并传递选中的项目
                        navController.previousBackStackEntry?.savedStateHandle?.set("selected_project", project)
                        navController.popBackStack()
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            // 相机功能的项目选择页面
            composable(NavigationRoutes.PROJECT_SELECTION_FOR_CAMERA) {
                val viewModel: ProjectListViewModel = hiltViewModel()
                ProjectSelectionScreen(
                    viewModel = viewModel,
                    onProjectSelected = { project ->
                        // 直接导航到相机页面
                        navController.navigateToCamera(project.id)
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            // 创建项目页面
            composable(NavigationRoutes.PROJECT_FORM) {
                val viewModel: ProjectFormViewModel = hiltViewModel()
                ProjectFormScreen(
                    viewModel = viewModel,
                    projectId = null,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            // 编辑项目页面
            composable(
                route = NavigationRoutes.PROJECT_FORM_WITH_ID,
                arguments = listOf(
                    navArgument("projectId") {
                        type = NavType.LongType
                    }
                )
            ) { backStackEntry ->
                val projectId = backStackEntry.arguments?.getLong("projectId") ?: 0L
                val viewModel: ProjectFormViewModel = hiltViewModel()
                ProjectFormScreen(
                    viewModel = viewModel,
                    projectId = projectId,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            // 项目主控台页面
            composable(
                route = NavigationRoutes.PROJECT_DASHBOARD,
                arguments = listOf(
                    navArgument("projectId") {
                        type = NavType.LongType
                    }
                )
            ) { backStackEntry ->
                val projectId = backStackEntry.arguments?.getLong("projectId") ?: 0L
                val viewModel: ProjectDashboardViewModel = hiltViewModel()
                ProjectDashboardScreen(
                    viewModel = viewModel,
                    projectId = projectId,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToLogEntry = { pId, date ->
                        navController.navigate("log_entry/$pId/$date")
                    },
                    onNavigateToExport = { pId ->
                        navController.navigate("export/$pId")
                    },
                    onNavigateToLogList = { pId ->
                        navController.navigateToLogList(pId)
                    }
                )
            }
            
            // 施工日志填写页面
            composable(
                route = NavigationRoutes.LOG_ENTRY,
                arguments = listOf(
                    navArgument("projectId") {
                        type = NavType.LongType
                    },
                    navArgument("date") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val projectId = backStackEntry.arguments?.getLong("projectId") ?: 0L
                val date = backStackEntry.arguments?.getString("date") ?: ""
                val viewModel: LogEntryViewModel = hiltViewModel()
                LogEntryScreen(
                    viewModel = viewModel,
                    projectId = projectId,
                    date = date,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToPhotoDescription = { uri, pId ->
                        navController.navigateToPhotoDescription(uri, pId)
                    }
                )
            }
            
            // 导出页面
            composable(
                route = NavigationRoutes.EXPORT,
                arguments = listOf(
                    navArgument("projectId") {
                        type = NavType.LongType
                    }
                )
            ) { backStackEntry ->
                val projectId = backStackEntry.arguments?.getLong("projectId") ?: 0L
                ExportScreen(
                    projectId = projectId,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onExportPdf = { pId, exportType, startDate, endDate ->
                        // TODO: 实现PDF导出功能
                        // 这里可以调用PDF生成服务
                        navController.popBackStack()
                    },
                    onViewPdf = {
                        navController.navigate(NavigationRoutes.PDF_VIEWER)
                    }
                )
            }
            
            // 相机页面
            composable(
                route = NavigationRoutes.CAMERA,
                arguments = listOf(
                    navArgument("projectId") {
                        type = NavType.LongType
                    }
                )
            ) { backStackEntry ->
                val projectId = backStackEntry.arguments?.getLong("projectId") ?: 0L
                val context = LocalContext.current
                val viewModel: CameraViewModel = hiltViewModel()
                CameraScreen(
                    viewModel = viewModel,
                    projectId = projectId,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToGallery = {
                        navController.navigateToMediaGallery(projectId)
                    },
                    onMediaCaptured = { uri, mediaType ->
                        // 拍照录像完成后自动导航到当日施工日志编辑页面
                        val currentDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                        navController.navigateToLogEntry(projectId, currentDate)
                    }
                )
            }
            
            // 媒体浏览页面
            composable(
                route = NavigationRoutes.MEDIA_GALLERY,
                arguments = listOf(
                    navArgument("projectId") {
                        type = NavType.LongType
                    }
                )
            ) { backStackEntry ->
                val projectId = backStackEntry.arguments?.getLong("projectId") ?: 0L
                val context = LocalContext.current
                val viewModel: MediaGalleryViewModel = hiltViewModel()
                MediaGalleryScreen(
                    viewModel = viewModel,
                    projectId = projectId,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onMediaClick = { mediaFile ->
                        navController.navigateToMediaDetail(mediaFile.id)
                    }
                )
            }
            
            // 媒体详情页面
            composable(
                route = NavigationRoutes.MEDIA_DETAIL,
                arguments = listOf(
                    navArgument("mediaFileId") {
                        type = NavType.LongType
                    }
                )
            ) { backStackEntry ->
                val mediaFileId = backStackEntry.arguments?.getLong("mediaFileId") ?: 0L
                val context = LocalContext.current
                val viewModel: MediaGalleryViewModel = hiltViewModel()
                val mediaFiles = viewModel.mediaFiles.collectAsState()
                val mediaFile = mediaFiles.value.find { it.id == mediaFileId }
                if (mediaFile != null) {
                    MediaDetailScreen(
                        mediaFile = mediaFile,
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onDelete = {
                            viewModel.deleteMediaFile(mediaFile)
                        },
                        onShare = {
                            // TODO: 实现分享功能
                        }
                    )
                } else {
                    LaunchedEffect(Unit) {
                        navController.popBackStack()
                    }
                }
            }
            
            // 照片描述页面
            composable(
                route = NavigationRoutes.PHOTO_DESCRIPTION,
                arguments = listOf(
                    navArgument("photoUri") {
                        type = NavType.StringType
                    },
                    navArgument("projectId") {
                        type = NavType.LongType
                    }
                )
            ) { backStackEntry ->
                val photoUriString = backStackEntry.arguments?.getString("photoUri") ?: ""
                val projectId = backStackEntry.arguments?.getLong("projectId") ?: 0L
                val photoUri = android.net.Uri.parse(photoUriString)
                val context = LocalContext.current
                val viewModel: PhotoDescriptionViewModel = hiltViewModel()
                PhotoDescriptionScreen(
                    photoUri = photoUri,
                    projectId = projectId,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onSavePhoto = { uri, description, location, notes ->
                        viewModel.savePhotoWithDescription(uri, projectId, description, location, notes)
                        navController.popBackStack()
                    }
                )
            }
            
            // 日志列表页面
            composable(
                route = NavigationRoutes.LOG_LIST,
                arguments = listOf(
                    navArgument("projectId") {
                        type = NavType.LongType
                    }
                )
            ) { backStackEntry ->
                val projectId = backStackEntry.arguments?.getLong("projectId") ?: 0L
                val viewModel: LogListViewModel = hiltViewModel()
                LogListScreen(
                    viewModel = viewModel,
                    projectId = projectId,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToLogEntry = { pId, date ->
                        navController.navigateToLogEntry(pId, date)
                    },
                    onNavigateToLogDetail = { logId ->
                        navController.navigateToLogDetail(logId)
                    }
                )
            }
            
            // 日志详情页面
            composable(
                route = NavigationRoutes.LOG_DETAIL,
                arguments = listOf(
                    navArgument("logId") {
                        type = NavType.LongType
                    }
                )
            ) { backStackEntry ->
                val logId = backStackEntry.arguments?.getLong("logId") ?: 0L
                val viewModel: LogDetailViewModel = hiltViewModel()
                LogDetailScreen(
                    viewModel = viewModel,
                    logId = logId,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // 天气详情页面
            composable(NavigationRoutes.WEATHER_DETAIL) {
                val context = LocalContext.current
                val viewModel: WeatherViewModel = hiltViewModel()
                WeatherDetailScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToSettings = {
                        navController.navigate(NavigationRoutes.WEATHER_SETTINGS)
                    }
                )
            }
            
            // 天气设置页面
            composable(NavigationRoutes.WEATHER_SETTINGS) {
                WeatherSettingsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // 位置页面
            composable(NavigationRoutes.LOCATION) {
                // 这里可用 hiltViewModel 或自定义 ViewModelProvider 传 dao
                // 伪代码如下，实际需根据你的依赖注入方式调整：
                val context = LocalContext.current.applicationContext
                val db = com.example.shuigongrizhi.data.database.AppDatabase.getDatabase(context)
                val locationDao = db.locationRecordDao()
                val viewModel = remember { com.example.shuigongrizhi.ui.viewmodel.LocationViewModel(context as android.app.Application, locationDao) }
                LocationScreen(viewModel = viewModel, onNavigateBack = { navController.popBackStack() })
            }
            
            // 桌面页面
            composable(NavigationRoutes.DESKTOP) {
                DesktopScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            // 发现页面
            composable(NavigationRoutes.DISCOVERY) {
                DiscoveryScreen(
                    onBackClick = { navController.popBackStack() },
                    onHomeClick = { navController.navigate(NavigationRoutes.MAIN) {
                        popUpTo(NavigationRoutes.MAIN) { inclusive = true }
                    }},
                    onDesktopClick = { navController.navigate(NavigationRoutes.DESKTOP) }
                )
            }
            
            // PDF查看器页面
            composable(NavigationRoutes.PDF_VIEWER) {
                val context = LocalContext.current
                val viewModel: PdfViewerViewModel = hiltViewModel()
                PdfViewerScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
            
            // 反馈界面
            composable(NavigationRoutes.FEEDBACK) {
                FeedbackScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

// 导航扩展函数
fun NavHostController.navigateToProjectForm(projectId: Long? = null) {
    if (projectId != null) {
        navigate("project_form/$projectId")
    } else {
        navigate(NavigationRoutes.PROJECT_FORM)
    }
}

fun NavHostController.navigateToProjectDashboard(projectId: Long) {
    navigate("project_dashboard/$projectId")
}

fun NavHostController.navigateToLogEntry(projectId: Long, date: String) {
    navigate("log_entry/$projectId/$date")
}

fun NavHostController.navigateToExport(projectId: Long) {
    navigate("export/$projectId")
}

fun NavHostController.navigateToCamera(projectId: Long) {
    navigate("camera/$projectId")
}

fun NavHostController.navigateToMediaGallery(projectId: Long) {
    navigate("media_gallery/$projectId")
}

fun NavHostController.navigateToMediaDetail(mediaFileId: Long) {
    navigate("media_detail/$mediaFileId")
}

fun NavHostController.navigateToLogList(projectId: Long) {
    navigate("log_list/$projectId")
}

fun NavHostController.navigateToLogDetail(logId: Long) {
    navigate("log_detail/$logId")
}

fun NavHostController.navigateToPhotoDescription(photoUri: android.net.Uri, projectId: Long) {
    navigate("photo_description/${photoUri}/${projectId}")
}