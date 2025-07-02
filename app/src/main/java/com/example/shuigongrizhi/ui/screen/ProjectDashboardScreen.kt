package com.example.shuigongrizhi.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import com.example.shuigongrizhi.ui.theme.*
import com.example.shuigongrizhi.ui.utils.ResponsiveUtils
import com.example.shuigongrizhi.ui.utils.getResponsivePadding
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.shuigongrizhi.R
import com.example.shuigongrizhi.ui.viewmodel.ProjectDashboardViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDashboardScreen(
    viewModel: ProjectDashboardViewModel,
    projectId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToLogEntry: (Long, String) -> Unit,
    onNavigateToExport: (Long) -> Unit,
    onNavigateToLogList: (Long) -> Unit = {}
) {
    val dashboardState by viewModel.dashboardState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // 加载项目数据
    LaunchedEffect(projectId) {
        viewModel.loadProject(projectId)
    }

    // 错误处理
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            viewModel.clearError()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = dashboardState.project?.name ?: "项目详情",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            modifier = Modifier.size(IconSize.large),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onNavigateToLogList(projectId) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "日志列表",
                            modifier = Modifier.size(IconSize.medium),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = { onNavigateToExport(projectId) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = stringResource(R.string.export_logs),
                            modifier = Modifier.size(IconSize.medium),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val dateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        .format(dashboardState.selectedDate)
                    onNavigateToLogEntry(projectId, dateString)
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "编辑日志"
                )
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(ResponsiveUtils.getResponsivePadding()),
                verticalArrangement = Arrangement.spacedBy(Spacing.medium)
            ) {
                // 项目信息卡片
                dashboardState.project?.let { project ->
                    ProjectInfoCard(project)
                    Spacer(modifier = Modifier.height(16.dp))
                }
                // 日历视图
                CalendarView(
                    currentMonth = dashboardState.currentMonth,
                    selectedDate = dashboardState.selectedDate,
                    logDates = dashboardState.logDates,
                    onDateSelected = viewModel::selectDate,
                    onMonthChanged = viewModel::changeMonth,
                    onViewLog = { date ->
                        // 根据日期查找对应的日志ID并导航到详情页
                        val dateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
                        onNavigateToLogEntry(projectId, dateString)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 日志摘要
                LogSummaryCard(
                    selectedDate = dashboardState.selectedDate,
                    selectedLog = dashboardState.selectedLog,
                    onEditClick = {
                        val dateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            .format(dashboardState.selectedDate)
                        onNavigateToLogEntry(projectId, dateString)
                    }
                )
            }
        }
    }
}

@Composable
fun CalendarView(
    currentMonth: Date,
    selectedDate: Date,
    logDates: Set<String>,
    onDateSelected: (Date) -> Unit,
    onMonthChanged: (Date) -> Unit,
    onViewLog: ((Date) -> Unit)? = null
) {
    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val monthFormat = SimpleDateFormat("yyyy年MM月", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = AppCardDefaults.elevation,
        shape = AppCardDefaults.shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(ResponsiveUtils.getResponsivePadding())
        ) {
            // 月份导航
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        calendar.time = currentMonth
                        calendar.add(Calendar.MONTH, -1)
                        onMonthChanged(calendar.time)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft,
                        contentDescription = "上个月",
                        modifier = Modifier.size(IconSize.medium),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                Text(
                    text = monthFormat.format(currentMonth),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                IconButton(
                    onClick = {
                        calendar.time = currentMonth
                        calendar.add(Calendar.MONTH, 1)
                        onMonthChanged(calendar.time)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "下个月",
                        modifier = Modifier.size(IconSize.medium),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 星期标题
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("日", "一", "二", "三", "四", "五", "六").forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(Spacing.medium))
            
            // 日期网格
            val dates = generateCalendarDates(currentMonth)
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Spacing.small),
                horizontalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                items(dates) { date ->
                    val hasLog = logDates.contains(dateFormat.format(date))
                    CalendarDateItem(
                        date = date,
                        isSelected = dateFormat.format(date) == dateFormat.format(selectedDate),
                        hasLog = hasLog,
                        isCurrentMonth = isSameMonth(date, currentMonth),
                        onClick = { onDateSelected(date) },
                        onViewLog = if (hasLog && onViewLog != null) {
                            { onViewLog(date) }
                        } else null
                    )
                }
            }
        }
    }
}

@Composable
fun CalendarDateItem(
    date: Date,
    isSelected: Boolean,
    hasLog: Boolean,
    isCurrentMonth: Boolean,
    onClick: () -> Unit,
    onViewLog: (() -> Unit)? = null
) {
    val calendar = Calendar.getInstance()
    calendar.time = date
    val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
    var showViewIcon by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .size(ButtonSize.medium)
            .clip(CircleShape)
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    else -> Color.Transparent
                }
            )
            .clickable(enabled = isCurrentMonth) { 
                if (hasLog && onViewLog != null) {
                    showViewIcon = true
                } else {
                    onClick()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    !isCurrentMonth -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    else -> MaterialTheme.colorScheme.onSurface
                },
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
            
            if (hasLog && isCurrentMonth) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        )
                )
            }
        }
        
        // 查看图标弹出层
        if (showViewIcon && hasLog && onViewLog != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                        CircleShape
                    )
                    .clickable { 
                        onViewLog()
                        showViewIcon = false
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = "查看日志",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            // 点击外部区域关闭图标
            LaunchedEffect(showViewIcon) {
                if (showViewIcon) {
                    kotlinx.coroutines.delay(3000) // 3秒后自动隐藏
                    showViewIcon = false
                }
            }
        }
    }
}

@Composable
fun LogSummaryCard(
    selectedDate: Date,
    selectedLog: com.example.shuigongrizhi.data.entity.ConstructionLog?,
    onEditClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = AppCardDefaults.elevation,
        shape = AppCardDefaults.shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(ResponsiveUtils.getResponsivePadding())
        ) {
            Text(
                text = dateFormat.format(selectedDate),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(Spacing.small))
            
            if (selectedLog != null) {
                // 显示日志摘要
                if (selectedLog.weatherCondition.isNotBlank()) {
                    Text(
                        text = "天气：${selectedLog.weatherCondition} ${selectedLog.temperature}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                if (selectedLog.mainContent.isNotBlank()) {
                    Spacer(modifier = Modifier.height(Spacing.extraSmall))
                    Text(
                        text = "主要工作：${selectedLog.mainContent.take(50)}${if (selectedLog.mainContent.length > 50) "..." else ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Spacer(modifier = Modifier.height(Spacing.small))
                
                Button(
                    onClick = onEditClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("编辑日志")
                }
            } else {
                Text(
                    text = stringResource(R.string.no_log_today),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(Spacing.small))
                
                Button(
                    onClick = onEditClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("创建日志")
                }
            }
        }
    }
}

@Composable
fun ProjectInfoRow(label: String, value: String) {
    Column(
        modifier = Modifier.padding(vertical = Spacing.extraSmall)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ProjectInfoCard(
    project: com.example.shuigongrizhi.data.entity.Project,
    onEditClick: () -> Unit
) {
    val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = AppCardDefaults.elevation,
        shape = AppCardDefaults.shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(ResponsiveUtils.getResponsivePadding())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "项目信息",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "编辑项目",
                        modifier = Modifier.size(IconSize.medium),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(Spacing.small))
            
            ProjectInfoRow("项目名称", project.name)
            ProjectInfoRow("项目类型", project.type.name)
            if (!project.manager.isNullOrBlank()) {
                ProjectInfoRow("项目经理", project.manager)
            }
            ProjectInfoRow("开始日期", dateFormat.format(project.startDate))
            ProjectInfoRow("结束日期", project.endDate?.let { dateFormat.format(it) } ?: "未设定")
            if (!project.description.isNullOrBlank()) {
                ProjectInfoRow("项目描述", project.description)
            }
        }
    }
}

// 辅助函数
fun generateCalendarDates(currentMonth: Date): List<Date> {
    val calendar = Calendar.getInstance()
    calendar.time = currentMonth
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    
    // 获取月份第一天是星期几
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
    
    // 获取月份天数
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    
    val dates = mutableListOf<Date>()
    
    // 添加上个月的日期填充
    calendar.add(Calendar.MONTH, -1)
    val daysInPrevMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    for (i in firstDayOfWeek - 1 downTo 0) {
        calendar.set(Calendar.DAY_OF_MONTH, daysInPrevMonth - i)
        dates.add(Date(calendar.timeInMillis))
    }
    
    // 添加当前月份的日期
    calendar.add(Calendar.MONTH, 1)
    for (day in 1..daysInMonth) {
        calendar.set(Calendar.DAY_OF_MONTH, day)
        dates.add(Date(calendar.timeInMillis))
    }
    
    // 添加下个月的日期填充
    calendar.add(Calendar.MONTH, 1)
    val remainingDays = 42 - dates.size // 6行 x 7列
    for (day in 1..remainingDays) {
        calendar.set(Calendar.DAY_OF_MONTH, day)
        dates.add(Date(calendar.timeInMillis))
    }
    
    return dates
}

fun isSameMonth(date1: Date, date2: Date): Boolean {
    val cal1 = Calendar.getInstance().apply { time = date1 }
    val cal2 = Calendar.getInstance().apply { time = date2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
}