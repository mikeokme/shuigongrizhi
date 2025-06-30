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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
    onNavigateToExport: (Long) -> Unit
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
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = dashboardState.project?.name ?: "项目详情",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onNavigateToExport(projectId) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = stringResource(R.string.export_logs)
                        )
                    }
                }
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
                    .padding(16.dp)
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
                    onMonthChanged = viewModel::changeMonth
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
    onMonthChanged: (Date) -> Unit
) {
    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val monthFormat = SimpleDateFormat("yyyy年MM月", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
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
                        contentDescription = "上个月"
                    )
                }
                
                Text(
                    text = monthFormat.format(currentMonth),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
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
                        contentDescription = "下个月"
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
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 日期网格
            val dates = generateCalendarDates(currentMonth)
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(dates) { date ->
                    CalendarDateItem(
                        date = date,
                        isSelected = dateFormat.format(date) == dateFormat.format(selectedDate),
                        hasLog = logDates.contains(dateFormat.format(date)),
                        isCurrentMonth = isSameMonth(date, currentMonth),
                        onClick = { onDateSelected(date) }
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
    onClick: () -> Unit
) {
    val calendar = Calendar.getInstance()
    calendar.time = date
    val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
    
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    else -> Color.Transparent
                }
            )
            .clickable(enabled = isCurrentMonth) { onClick() },
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
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = dateFormat.format(selectedDate),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (selectedLog != null) {
                // 显示日志摘要
                if (selectedLog.weatherCondition.isNotBlank()) {
                    Text(
                        text = "天气：${selectedLog.weatherCondition} ${selectedLog.temperature}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                if (selectedLog.mainContent.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "主要工作：${selectedLog.mainContent.take(50)}${if (selectedLog.mainContent.length > 50) "..." else ""}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
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
                
                Spacer(modifier = Modifier.height(8.dp))
                
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
fun ProjectInfoCard(project: com.example.shuigongrizhi.data.entity.Project) {
    val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = project.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = project.type.name,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (!project.manager.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "负责人：${project.manager}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${project.startDate.let { dateFormat.format(it) }}" +
                    (project.endDate?.let { " - ${dateFormat.format(it)}" } ?: ""),
                style = MaterialTheme.typography.bodySmall
            )
            if (!project.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = project.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 4
                )
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