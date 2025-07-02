package com.example.shuigongrizhi.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.shuigongrizhi.R
import java.text.SimpleDateFormat
import java.util.*

data class ExportPeriod(
    val type: ExportType,
    val displayName: String
)

enum class ExportType {
    WEEKLY, MONTHLY, QUARTERLY, YEARLY
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    projectId: Long,
    onNavigateBack: () -> Unit,
    onExportPdf: (Long, ExportType, String, String) -> Unit,
    onViewPdf: () -> Unit = {}
) {
    var selectedPeriod by remember { mutableStateOf(ExportType.MONTHLY) }
    var selectedTimeRange by remember { mutableStateOf("") }
    var isExporting by remember { mutableStateOf(false) }
    
    val exportPeriods = listOf(
        ExportPeriod(ExportType.WEEKLY, "按周"),
        ExportPeriod(ExportType.MONTHLY, "按月"),
        ExportPeriod(ExportType.QUARTERLY, "按季度"),
        ExportPeriod(ExportType.YEARLY, "按年度")
    )
    
    // 初始化默认时间范围
    LaunchedEffect(selectedPeriod) {
        selectedTimeRange = getDefaultTimeRange(selectedPeriod)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.export_logs),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 导出周期选择
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "导出周期",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Column(
                        modifier = Modifier.selectableGroup()
                    ) {
                        exportPeriods.forEach { period ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = (selectedPeriod == period.type),
                                        onClick = { selectedPeriod = period.type },
                                        role = Role.RadioButton
                                    )
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (selectedPeriod == period.type),
                                    onClick = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = period.displayName,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }
            
            // 时间范围选择
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "时间范围",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    TimeRangeSelector(
                        exportType = selectedPeriod,
                        selectedRange = selectedTimeRange,
                        onRangeSelected = { selectedTimeRange = it }
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 生成PDF按钮
            Button(
                onClick = {
                    isExporting = true
                    val (startDate, endDate) = getDateRange(selectedPeriod, selectedTimeRange)
                    onExportPdf(projectId, selectedPeriod, startDate, endDate)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isExporting && selectedTimeRange.isNotBlank()
            ) {
                if (isExporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("正在生成报告...")
                } else {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "生成PDF报告",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 查看PDF文档按钮
            OutlinedButton(
                onClick = onViewPdf,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "查看PDF文档",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
fun TimeRangeSelector(
    exportType: ExportType,
    selectedRange: String,
    onRangeSelected: (String) -> Unit
) {
    when (exportType) {
        ExportType.WEEKLY -> {
            WeekSelector(
                selectedWeek = selectedRange,
                onWeekSelected = onRangeSelected
            )
        }
        ExportType.MONTHLY -> {
            MonthSelector(
                selectedMonth = selectedRange,
                onMonthSelected = onRangeSelected
            )
        }
        ExportType.QUARTERLY -> {
            QuarterSelector(
                selectedQuarter = selectedRange,
                onQuarterSelected = onRangeSelected
            )
        }
        ExportType.YEARLY -> {
            YearSelector(
                selectedYear = selectedRange,
                onYearSelected = onRangeSelected
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeekSelector(
    selectedWeek: String,
    onWeekSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val calendar = Calendar.getInstance()
    val currentYear = calendar.get(Calendar.YEAR)
    val weeks = generateWeekOptions(currentYear)
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedWeek,
            onValueChange = { },
            readOnly = true,
            label = { Text("选择周") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            weeks.forEach { week ->
                DropdownMenuItem(
                    text = { Text(week) },
                    onClick = {
                        onWeekSelected(week)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthSelector(
    selectedMonth: String,
    onMonthSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val calendar = Calendar.getInstance()
    val currentYear = calendar.get(Calendar.YEAR)
    val months = generateMonthOptions(currentYear)
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedMonth,
            onValueChange = { },
            readOnly = true,
            label = { Text("选择月份") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            months.forEach { month ->
                DropdownMenuItem(
                    text = { Text(month) },
                    onClick = {
                        onMonthSelected(month)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuarterSelector(
    selectedQuarter: String,
    onQuarterSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val calendar = Calendar.getInstance()
    val currentYear = calendar.get(Calendar.YEAR)
    val quarters = generateQuarterOptions(currentYear)
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedQuarter,
            onValueChange = { },
            readOnly = true,
            label = { Text("选择季度") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            quarters.forEach { quarter ->
                DropdownMenuItem(
                    text = { Text(quarter) },
                    onClick = {
                        onQuarterSelected(quarter)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YearSelector(
    selectedYear: String,
    onYearSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val years = generateYearOptions()
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedYear,
            onValueChange = { },
            readOnly = true,
            label = { Text("选择年份") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            years.forEach { year ->
                DropdownMenuItem(
                    text = { Text(year) },
                    onClick = {
                        onYearSelected(year)
                        expanded = false
                    }
                )
            }
        }
    }
}

// 辅助函数
fun getDefaultTimeRange(exportType: ExportType): String {
    val calendar = Calendar.getInstance()
    return when (exportType) {
        ExportType.WEEKLY -> {
            val year = calendar.get(Calendar.YEAR)
            val week = calendar.get(Calendar.WEEK_OF_YEAR)
            "${year}年第${week}周"
        }
        ExportType.MONTHLY -> {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1
            "${year}年${month}月"
        }
        ExportType.QUARTERLY -> {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1
            val quarter = (month - 1) / 3 + 1
            "${year}年第${quarter}季度"
        }
        ExportType.YEARLY -> {
            calendar.get(Calendar.YEAR).toString() + "年"
        }
    }
}

fun generateWeekOptions(year: Int): List<String> {
    val weeks = mutableListOf<String>()
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.YEAR, year)
    calendar.set(Calendar.MONTH, Calendar.DECEMBER)
    calendar.set(Calendar.DAY_OF_MONTH, 31)
    val maxWeek = calendar.get(Calendar.WEEK_OF_YEAR)
    
    for (week in 1..maxWeek) {
        weeks.add("${year}年第${week}周")
    }
    return weeks.reversed()
}

fun generateMonthOptions(year: Int): List<String> {
    val months = mutableListOf<String>()
    for (month in 1..12) {
        months.add("${year}年${month}月")
    }
    return months.reversed()
}

fun generateQuarterOptions(year: Int): List<String> {
    return listOf(
        "${year}年第4季度",
        "${year}年第3季度",
        "${year}年第2季度",
        "${year}年第1季度"
    )
}

fun generateYearOptions(): List<String> {
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val years = mutableListOf<String>()
    for (year in currentYear downTo currentYear - 10) {
        years.add("${year}年")
    }
    return years
}

fun getDateRange(exportType: ExportType, timeRange: String): Pair<String, String> {
    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    return when (exportType) {
        ExportType.WEEKLY -> {
            // 解析周信息
            val regex = "(\\d{4})年第(\\d+)周".toRegex()
            val matchResult = regex.find(timeRange)
            if (matchResult != null) {
                val year = matchResult.groupValues[1].toInt()
                val week = matchResult.groupValues[2].toInt()
                
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.WEEK_OF_YEAR, week)
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                val startDate = dateFormat.format(calendar.time)
                
                calendar.add(Calendar.DAY_OF_WEEK, 6)
                val endDate = dateFormat.format(calendar.time)
                
                Pair(startDate, endDate)
            } else {
                Pair("", "")
            }
        }
        ExportType.MONTHLY -> {
            // 解析月份信息
            val regex = "(\\d{4})年(\\d+)月".toRegex()
            val matchResult = regex.find(timeRange)
            if (matchResult != null) {
                val year = matchResult.groupValues[1].toInt()
                val month = matchResult.groupValues[2].toInt()
                
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month - 1)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val startDate = dateFormat.format(calendar.time)
                
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                val endDate = dateFormat.format(calendar.time)
                
                Pair(startDate, endDate)
            } else {
                Pair("", "")
            }
        }
        ExportType.QUARTERLY -> {
            // 解析季度信息
            val regex = "(\\d{4})年第(\\d+)季度".toRegex()
            val matchResult = regex.find(timeRange)
            if (matchResult != null) {
                val year = matchResult.groupValues[1].toInt()
                val quarter = matchResult.groupValues[2].toInt()
                
                val startMonth = (quarter - 1) * 3
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, startMonth)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val startDate = dateFormat.format(calendar.time)
                
                calendar.set(Calendar.MONTH, startMonth + 2)
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                val endDate = dateFormat.format(calendar.time)
                
                Pair(startDate, endDate)
            } else {
                Pair("", "")
            }
        }
        ExportType.YEARLY -> {
            // 解析年份信息
            val regex = "(\\d{4})年".toRegex()
            val matchResult = regex.find(timeRange)
            if (matchResult != null) {
                val year = matchResult.groupValues[1].toInt()
                
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, Calendar.JANUARY)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val startDate = dateFormat.format(calendar.time)
                
                calendar.set(Calendar.MONTH, Calendar.DECEMBER)
                calendar.set(Calendar.DAY_OF_MONTH, 31)
                val endDate = dateFormat.format(calendar.time)
                
                Pair(startDate, endDate)
            } else {
                Pair("", "")
            }
        }
    }
}