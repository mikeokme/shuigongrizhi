package com.example.shuigongrizhi.core

import android.app.ActivityManager
import android.content.Context
import android.os.Debug
import android.os.Process
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 性能监控器
 * 提供应用性能监控和分析功能
 */
@Singleton
class PerformanceMonitor @Inject constructor(
    private val context: Context
) {
    
    companion object {
        private const val MONITORING_INTERVAL = 5000L // 5秒监控间隔
        private const val MAX_PERFORMANCE_RECORDS = 100 // 最大性能记录数
        private const val MEMORY_WARNING_THRESHOLD = 0.8f // 内存警告阈值
        private const val CPU_WARNING_THRESHOLD = 0.7f // CPU警告阈值
    }
    
    private val _isMonitoring = MutableStateFlow(false)
    val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()
    
    private val _currentPerformance = MutableStateFlow(PerformanceMetrics())
    val currentPerformance: StateFlow<PerformanceMetrics> = _currentPerformance.asStateFlow()
    
    private val performanceHistory = mutableListOf<PerformanceRecord>()
    private val methodTimings = ConcurrentHashMap<String, MethodTiming>()
    private val frameTimings = mutableListOf<Long>()
    
    private var monitoringJob: Job? = null
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    
    // 计数器
    private val networkRequestCount = AtomicLong(0)
    private val databaseQueryCount = AtomicLong(0)
    private val cacheHitCount = AtomicLong(0)
    private val cacheMissCount = AtomicLong(0)
    
    /**
     * 开始性能监控
     */
    fun startMonitoring() {
        if (_isMonitoring.value) return
        
        _isMonitoring.value = true
        Logger.business("开始性能监控")
        
        monitoringJob = CoroutineScope(Dispatchers.Default).launch {
            while (_isMonitoring.value) {
                try {
                    val metrics = collectPerformanceMetrics()
                    _currentPerformance.value = metrics
                    
                    // 记录性能历史
                    addPerformanceRecord(metrics)
                    
                    // 检查性能警告
                    checkPerformanceWarnings(metrics)
                    
                    delay(MONITORING_INTERVAL)
                } catch (e: Exception) {
                    Logger.exception(e, "性能监控异常")
                }
            }
        }
    }
    
    /**
     * 停止性能监控
     */
    fun stopMonitoring() {
        if (!_isMonitoring.value) return
        
        _isMonitoring.value = false
        monitoringJob?.cancel()
        Logger.business("停止性能监控")
    }
    
    /**
     * 收集性能指标
     */
    private fun collectPerformanceMetrics(): PerformanceMetrics {
        val memoryInfo = getMemoryInfo()
        val cpuUsage = getCpuUsage()
        val batteryInfo = getBatteryInfo()
        val networkStats = getNetworkStats()
        val storageInfo = getStorageInfo()
        
        return PerformanceMetrics(
            timestamp = System.currentTimeMillis(),
            memoryUsage = memoryInfo,
            cpuUsage = cpuUsage,
            batteryLevel = batteryInfo.level,
            batteryTemperature = batteryInfo.temperature,
            networkRequestCount = networkRequestCount.get(),
            databaseQueryCount = databaseQueryCount.get(),
            cacheHitRate = calculateCacheHitRate(),
            frameRate = calculateFrameRate(),
            storageUsage = storageInfo
        )
    }
    
    /**
     * 获取内存信息
     */
    private fun getMemoryInfo(): MemoryUsage {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val maxMemory = runtime.maxMemory()
        
        // 获取详细内存信息
        val debugMemoryInfo = Debug.MemoryInfo()
        Debug.getMemoryInfo(debugMemoryInfo)
        
        return MemoryUsage(
            usedMemory = usedMemory,
            totalMemory = runtime.totalMemory(),
            maxMemory = maxMemory,
            availableMemory = memoryInfo.availMem,
            systemTotalMemory = memoryInfo.totalMem,
            heapSize = debugMemoryInfo.dalvikPss * 1024L,
            nativeHeapSize = debugMemoryInfo.nativePss * 1024L,
            isLowMemory = memoryInfo.lowMemory
        )
    }
    
    /**
     * 获取CPU使用率
     */
    private fun getCpuUsage(): Float {
        return try {
            val pid = Process.myPid()
            val statFile = "/proc/$pid/stat"
            val statContent = java.io.File(statFile).readText()
            val statParts = statContent.split(" ")
            
            if (statParts.size >= 15) {
                val utime = statParts[13].toLongOrNull() ?: 0L
                val stime = statParts[14].toLongOrNull() ?: 0L
                val totalTime = utime + stime
                
                // 简化的CPU使用率计算
                (totalTime % 100).toFloat() / 100f
            } else {
                0f
            }
        } catch (e: Exception) {
            Logger.exception(e, "获取CPU使用率失败")
            0f
        }
    }
    
    /**
     * 获取电池信息
     */
    private fun getBatteryInfo(): BatteryInfo {
        return try {
            val batteryIntent = context.registerReceiver(null, 
                android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED))
            
            val level = batteryIntent?.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryIntent?.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1) ?: -1
            val temperature = batteryIntent?.getIntExtra(android.os.BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
            val voltage = batteryIntent?.getIntExtra(android.os.BatteryManager.EXTRA_VOLTAGE, -1) ?: -1
            
            val batteryLevel = if (level >= 0 && scale > 0) {
                (level.toFloat() / scale.toFloat() * 100).toInt()
            } else {
                -1
            }
            
            BatteryInfo(
                level = batteryLevel,
                temperature = temperature / 10f, // 温度单位转换
                voltage = voltage / 1000f // 电压单位转换
            )
        } catch (e: Exception) {
            Logger.exception(e, "获取电池信息失败")
            BatteryInfo(-1, 0f, 0f)
        }
    }
    
    /**
     * 获取网络统计
     */
    private fun getNetworkStats(): NetworkStats {
        return NetworkStats(
            requestCount = networkRequestCount.get(),
            successCount = 0L, // 需要在网络请求中统计
            errorCount = 0L // 需要在网络请求中统计
        )
    }
    
    /**
     * 获取存储使用情况
     */
    private fun getStorageInfo(): StorageUsage {
        val externalDir = context.getExternalFilesDir(null)
        val totalSpace = externalDir?.totalSpace ?: 0L
        val freeSpace = externalDir?.freeSpace ?: 0L
        val usedSpace = totalSpace - freeSpace
        
        return StorageUsage(
            totalSpace = totalSpace,
            usedSpace = usedSpace,
            freeSpace = freeSpace
        )
    }
    
    /**
     * 计算缓存命中率
     */
    private fun calculateCacheHitRate(): Float {
        val totalRequests = cacheHitCount.get() + cacheMissCount.get()
        return if (totalRequests > 0) {
            cacheHitCount.get().toFloat() / totalRequests.toFloat()
        } else {
            0f
        }
    }
    
    /**
     * 计算帧率
     */
    private fun calculateFrameRate(): Float {
        return if (frameTimings.isNotEmpty()) {
            val averageFrameTime = frameTimings.average()
            if (averageFrameTime > 0) {
                1000f / averageFrameTime.toFloat()
            } else {
                0f
            }
        } else {
            0f
        }
    }
    
    /**
     * 添加性能记录
     */
    private fun addPerformanceRecord(metrics: PerformanceMetrics) {
        synchronized(performanceHistory) {
            val record = PerformanceRecord(
                timestamp = metrics.timestamp,
                memoryUsagePercent = metrics.memoryUsage.getUsagePercentage(),
                cpuUsage = metrics.cpuUsage,
                batteryLevel = metrics.batteryLevel,
                frameRate = metrics.frameRate,
                networkRequestCount = metrics.networkRequestCount,
                databaseQueryCount = metrics.databaseQueryCount
            )
            
            performanceHistory.add(record)
            
            // 保持记录数量在限制内
            if (performanceHistory.size > MAX_PERFORMANCE_RECORDS) {
                performanceHistory.removeAt(0)
            }
        }
    }
    
    /**
     * 检查性能警告
     */
    private fun checkPerformanceWarnings(metrics: PerformanceMetrics) {
        val warnings = mutableListOf<String>()
        
        // 内存警告
        if (metrics.memoryUsage.getUsagePercentage() > MEMORY_WARNING_THRESHOLD) {
            warnings.add("内存使用率过高: ${(metrics.memoryUsage.getUsagePercentage() * 100).toInt()}%")
        }
        
        // CPU警告
        if (metrics.cpuUsage > CPU_WARNING_THRESHOLD) {
            warnings.add("CPU使用率过高: ${(metrics.cpuUsage * 100).toInt()}%")
        }
        
        // 低内存警告
        if (metrics.memoryUsage.isLowMemory) {
            warnings.add("系统内存不足")
        }
        
        // 电池温度警告
        if (metrics.batteryTemperature > 40f) {
            warnings.add("设备温度过高: ${metrics.batteryTemperature}°C")
        }
        
        // 帧率警告
        if (metrics.frameRate < 30f && metrics.frameRate > 0f) {
            warnings.add("帧率过低: ${metrics.frameRate.toInt()}fps")
        }
        
        if (warnings.isNotEmpty()) {
            Logger.w("性能警告: ${warnings.joinToString(", ")}", "PerformanceMonitor")
        }
    }
    
    /**
     * 记录方法执行时间
     */
    fun recordMethodTiming(methodName: String, executionTime: Long) {
        val timing = methodTimings.getOrPut(methodName) {
            MethodTiming(methodName, 0, 0L, Long.MAX_VALUE, 0L)
        }
        
        synchronized(timing) {
            timing.callCount++
            timing.totalTime += executionTime
            timing.minTime = minOf(timing.minTime, executionTime)
            timing.maxTime = maxOf(timing.maxTime, executionTime)
        }
    }
    
    /**
     * 记录帧时间
     */
    fun recordFrameTime(frameTime: Long) {
        synchronized(frameTimings) {
            frameTimings.add(frameTime)
            
            // 保持最近100帧的记录
            if (frameTimings.size > 100) {
                frameTimings.removeAt(0)
            }
        }
    }
    
    /**
     * 增加网络请求计数
     */
    fun incrementNetworkRequestCount() {
        networkRequestCount.incrementAndGet()
    }
    
    /**
     * 增加数据库查询计数
     */
    fun incrementDatabaseQueryCount() {
        databaseQueryCount.incrementAndGet()
    }
    
    /**
     * 增加缓存命中计数
     */
    fun incrementCacheHitCount() {
        cacheHitCount.incrementAndGet()
    }
    
    /**
     * 增加缓存未命中计数
     */
    fun incrementCacheMissCount() {
        cacheMissCount.incrementAndGet()
    }
    
    /**
     * 获取性能历史记录
     */
    fun getPerformanceHistory(): List<PerformanceRecord> {
        return synchronized(performanceHistory) {
            performanceHistory.toList()
        }
    }
    
    /**
     * 获取方法执行时间统计
     */
    fun getMethodTimings(): Map<String, MethodTiming> {
        return methodTimings.toMap()
    }
    
    /**
     * 生成性能报告
     */
    fun generatePerformanceReport(): PerformanceReport {
        val history = getPerformanceHistory()
        val timings = getMethodTimings()
        val currentMetrics = _currentPerformance.value
        
        val avgMemoryUsage = history.map { it.memoryUsagePercent }.average().toFloat()
        val avgCpuUsage = history.map { it.cpuUsage }.average().toFloat()
        val avgFrameRate = history.map { it.frameRate }.filter { it > 0 }.average().toFloat()
        
        val slowestMethods = timings.values
            .sortedByDescending { it.getAverageTime() }
            .take(10)
        
        return PerformanceReport(
            reportTimestamp = System.currentTimeMillis(),
            currentMetrics = currentMetrics,
            averageMemoryUsage = avgMemoryUsage,
            averageCpuUsage = avgCpuUsage,
            averageFrameRate = avgFrameRate,
            totalNetworkRequests = networkRequestCount.get(),
            totalDatabaseQueries = databaseQueryCount.get(),
            cacheHitRate = calculateCacheHitRate(),
            slowestMethods = slowestMethods,
            performanceHistory = history
        )
    }
    
    /**
     * 重置统计数据
     */
    fun resetStatistics() {
        networkRequestCount.set(0)
        databaseQueryCount.set(0)
        cacheHitCount.set(0)
        cacheMissCount.set(0)
        
        synchronized(performanceHistory) {
            performanceHistory.clear()
        }
        
        methodTimings.clear()
        
        synchronized(frameTimings) {
            frameTimings.clear()
        }
        
        Logger.business("性能统计数据已重置")
    }
}

/**
 * 性能指标数据类
 */
data class PerformanceMetrics(
    val timestamp: Long = System.currentTimeMillis(),
    val memoryUsage: MemoryUsage = MemoryUsage(),
    val cpuUsage: Float = 0f,
    val batteryLevel: Int = -1,
    val batteryTemperature: Float = 0f,
    val networkRequestCount: Long = 0L,
    val databaseQueryCount: Long = 0L,
    val cacheHitRate: Float = 0f,
    val frameRate: Float = 0f,
    val storageUsage: StorageUsage = StorageUsage()
)

/**
 * 内存使用情况
 */
data class MemoryUsage(
    val usedMemory: Long = 0L,
    val totalMemory: Long = 0L,
    val maxMemory: Long = 0L,
    val availableMemory: Long = 0L,
    val systemTotalMemory: Long = 0L,
    val heapSize: Long = 0L,
    val nativeHeapSize: Long = 0L,
    val isLowMemory: Boolean = false
) {
    fun getUsagePercentage(): Float {
        return if (maxMemory > 0) {
            usedMemory.toFloat() / maxMemory.toFloat()
        } else {
            0f
        }
    }
    
    fun getReadableUsedMemory(): String {
        return usedMemory.toReadableFileSize()
    }
    
    fun getReadableMaxMemory(): String {
        return maxMemory.toReadableFileSize()
    }
}

/**
 * 电池信息
 */
data class BatteryInfo(
    val level: Int,
    val temperature: Float,
    val voltage: Float
)

/**
 * 网络统计
 */
data class NetworkStats(
    val requestCount: Long,
    val successCount: Long,
    val errorCount: Long
) {
    fun getSuccessRate(): Float {
        return if (requestCount > 0) {
            successCount.toFloat() / requestCount.toFloat()
        } else {
            0f
        }
    }
}

/**
 * 存储使用情况
 */
data class StorageUsage(
    val totalSpace: Long = 0L,
    val usedSpace: Long = 0L,
    val freeSpace: Long = 0L
) {
    fun getUsagePercentage(): Float {
        return if (totalSpace > 0) {
            usedSpace.toFloat() / totalSpace.toFloat()
        } else {
            0f
        }
    }
    
    fun getReadableTotalSpace(): String {
        return totalSpace.toReadableFileSize()
    }
    
    fun getReadableUsedSpace(): String {
        return usedSpace.toReadableFileSize()
    }
    
    fun getReadableFreeSpace(): String {
        return freeSpace.toReadableFileSize()
    }
}

/**
 * 性能记录
 */
data class PerformanceRecord(
    val timestamp: Long,
    val memoryUsagePercent: Float,
    val cpuUsage: Float,
    val batteryLevel: Int,
    val frameRate: Float,
    val networkRequestCount: Long,
    val databaseQueryCount: Long
) {
    fun getFormattedTime(): String {
        return Utils.DateTime.formatTimestamp(timestamp)
    }
}

/**
 * 方法执行时间统计
 */
data class MethodTiming(
    val methodName: String,
    var callCount: Int,
    var totalTime: Long,
    var minTime: Long,
    var maxTime: Long
) {
    fun getAverageTime(): Long {
        return if (callCount > 0) totalTime / callCount else 0L
    }
    
    fun getReadableAverageTime(): String {
        return "${getAverageTime()}ms"
    }
    
    fun getReadableTotalTime(): String {
        return "${totalTime}ms"
    }
}

/**
 * 性能报告
 */
data class PerformanceReport(
    val reportTimestamp: Long,
    val currentMetrics: PerformanceMetrics,
    val averageMemoryUsage: Float,
    val averageCpuUsage: Float,
    val averageFrameRate: Float,
    val totalNetworkRequests: Long,
    val totalDatabaseQueries: Long,
    val cacheHitRate: Float,
    val slowestMethods: List<MethodTiming>,
    val performanceHistory: List<PerformanceRecord>
) {
    fun getFormattedReportTime(): String {
        return Utils.DateTime.formatTimestamp(reportTimestamp)
    }
    
    fun getOverallPerformanceScore(): Int {
        var score = 100
        
        // 内存使用率影响
        if (averageMemoryUsage > 0.8f) score -= 20
        else if (averageMemoryUsage > 0.6f) score -= 10
        
        // CPU使用率影响
        if (averageCpuUsage > 0.7f) score -= 20
        else if (averageCpuUsage > 0.5f) score -= 10
        
        // 帧率影响
        if (averageFrameRate < 30f && averageFrameRate > 0f) score -= 15
        else if (averageFrameRate < 45f && averageFrameRate > 0f) score -= 5
        
        // 缓存命中率影响
        if (cacheHitRate < 0.5f) score -= 10
        else if (cacheHitRate < 0.7f) score -= 5
        
        return maxOf(0, score)
    }
    
    fun getPerformanceGrade(): String {
        return when (getOverallPerformanceScore()) {
            in 90..100 -> "优秀"
            in 80..89 -> "良好"
            in 70..79 -> "一般"
            in 60..69 -> "较差"
            else -> "很差"
        }
    }
}