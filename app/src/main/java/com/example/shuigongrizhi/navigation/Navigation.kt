package com.example.shuigongrizhi.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.shuigongrizhi.ui.screen.*
import com.example.shuigongrizhi.ui.viewmodel.*

// 导航路由常量
object NavigationRoutes {
    const val MAIN = "main"
    const val PROJECT_LIST = "project_list"
    const val PROJECT_FORM = "project_form"
    const val PROJECT_FORM_WITH_ID = "project_form/{projectId}"
    const val PROJECT_DASHBOARD = "project_dashboard/{projectId}"
    const val LOG_ENTRY = "log_entry/{projectId}/{date}"
    const val EXPORT = "export/{projectId}"
    const val WEATHER_DETAIL = "weather_detail"
}

@Composable
fun AppNavigation(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = NavigationRoutes.MAIN
    ) {
        // 主界面
        composable(NavigationRoutes.MAIN) {
            MainScreen(
                onLogClick = { navController.navigate(NavigationRoutes.PROJECT_LIST) },
                onWeatherClick = { navController.navigate(NavigationRoutes.WEATHER_DETAIL) },
                onCameraClick = { /* TODO: 跳转到拍照录像页面 */ },
                onLocationClick = { /* TODO: 跳转到位置服务页面 */ },
                onMediaClick = { /* TODO: 跳转到媒体管理页面 */ },
                onProjectClick = { navController.navigate(NavigationRoutes.PROJECT_LIST) },
                onSettingsClick = { /* TODO: 跳转到设置页面 */ }
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
                }
            )
        }
        
        // 天气详情页面
        composable(NavigationRoutes.WEATHER_DETAIL) {
            val viewModel: WeatherViewModel = hiltViewModel()
            WeatherDetailScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
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