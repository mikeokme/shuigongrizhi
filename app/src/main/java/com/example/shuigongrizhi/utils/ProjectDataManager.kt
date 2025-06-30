package com.example.shuigongrizhi.utils

import android.content.Context
import android.util.Log
import com.example.shuigongrizhi.data.entity.Project
import com.example.shuigongrizhi.data.entity.ConstructionLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Serializable
data class ProjectBackup(
    val projectId: Long,
    val projectName: String,
    val projectType: String,
    val description: String?,
    val startDate: String,
    val endDate: String?,
    val manager: String?,
    val logs: List<LogBackup> = emptyList()
)

@Serializable
data class LogBackup(
    val logId: Long,
    val projectId: Long,
    val date: String,
    val weatherCondition: String?,
    val temperature: String?,
    val wind: String?,
    val constructionSite: String?,
    val mainContent: String?,
    val personnelEquipment: String?,
    val qualityManagement: String?,
    val safetyManagement: String?,
    val mediaFiles: List<String> = emptyList(),
    val createdAt: String,
    val updatedAt: String
)

class ProjectDataManager(private val context: Context) {
    
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
    private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    
    /**
     * 获取项目数据存储目录
     */
    private fun getProjectDataDir(): File? {
        return context.getExternalFilesDir("ProjectData")
    }
    
    /**
     * 获取项目备份目录
     */
    private fun getBackupDir(): File? {
        val backupDir = File(getProjectDataDir(), "backups")
        if (!backupDir.exists()) {
            backupDir.mkdirs()
        }
        return backupDir
    }
    
    /**
     * 备份单个项目数据
     */
    suspend fun backupProject(
        project: Project,
        logs: List<ConstructionLog> = emptyList()
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val projectBackup = ProjectBackup(
                projectId = project.id,
                projectName = project.name,
                projectType = project.type.name,
                description = project.description,
                startDate = isoDateFormat.format(project.startDate),
                endDate = project.endDate?.let { isoDateFormat.format(it) },
                manager = project.manager,
                logs = logs.map { log ->
                    LogBackup(
                        logId = log.id,
                        projectId = log.projectId,
                        date = isoDateFormat.format(log.date),
                        weatherCondition = log.weatherCondition,
                        temperature = log.temperature,
                        wind = log.wind,
                        constructionSite = log.constructionSite,
                        mainContent = log.mainContent,
                        personnelEquipment = log.personnelEquipment,
                        qualityManagement = log.qualityManagement,
                        safetyManagement = log.safetyManagement,
                        mediaFiles = log.mediaFiles,
                        createdAt = isoDateFormat.format(log.createdAt),
                        updatedAt = isoDateFormat.format(log.updatedAt)
                    )
                }
            )
            
            val backupDir = getBackupDir()
                ?: return@withContext Result.failure(Exception("无法创建备份目录"))
            
            val timestamp = dateFormat.format(Date())
            val fileName = "project_${project.id}_${project.name}_$timestamp.json"
            val backupFile = File(backupDir, fileName)
            
            val jsonContent = json.encodeToString(projectBackup)
            backupFile.writeText(jsonContent)
            
            Log.d("ProjectDataManager", "项目备份成功: ${backupFile.absolutePath}")
            Result.success(backupFile.absolutePath)
            
        } catch (e: Exception) {
            Log.e("ProjectDataManager", "项目备份失败", e)
            Result.failure(e)
        }
    }
    
    /**
     * 自动备份项目（在项目保存时调用）
     */
    suspend fun autoBackupProject(
        project: Project,
        logs: List<ConstructionLog> = emptyList()
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            // 检查是否需要备份（避免频繁备份）
            if (shouldCreateBackup(project.id)) {
                val result = backupProject(project, logs)
                if (result.isSuccess) {
                    updateLastBackupTime(project.id)
                    Log.d("ProjectDataManager", "自动备份项目 ${project.name} 成功")
                    true
                } else {
                    Log.w("ProjectDataManager", "自动备份项目 ${project.name} 失败")
                    false
                }
            } else {
                Log.d("ProjectDataManager", "项目 ${project.name} 无需备份")
                true
            }
        } catch (e: Exception) {
            Log.e("ProjectDataManager", "自动备份项目失败", e)
            false
        }
    }
    
    /**
     * 检查是否需要创建备份
     */
    private fun shouldCreateBackup(projectId: Long): Boolean {
        val lastBackupTime = getLastBackupTime(projectId)
        val currentTime = System.currentTimeMillis()
        val backupInterval = 24 * 60 * 60 * 1000L // 24小时
        
        return (currentTime - lastBackupTime) > backupInterval
    }
    
    /**
     * 获取上次备份时间
     */
    private fun getLastBackupTime(projectId: Long): Long {
        val prefs = context.getSharedPreferences("project_backup", Context.MODE_PRIVATE)
        return prefs.getLong("last_backup_$projectId", 0L)
    }
    
    /**
     * 更新上次备份时间
     */
    private fun updateLastBackupTime(projectId: Long) {
        val prefs = context.getSharedPreferences("project_backup", Context.MODE_PRIVATE)
        prefs.edit().putLong("last_backup_$projectId", System.currentTimeMillis()).apply()
    }
    
    /**
     * 获取所有备份文件
     */
    fun getAllBackupFiles(): List<File> {
        val backupDir = getBackupDir() ?: return emptyList()
        return backupDir.listFiles { file ->
            file.isFile && file.name.endsWith(".json")
        }?.toList() ?: emptyList()
    }
    
    /**
     * 清理旧备份文件（保留最近30个备份）
     */
    suspend fun cleanupOldBackups(keepCount: Int = 30): Int = withContext(Dispatchers.IO) {
        try {
            val backupFiles = getAllBackupFiles()
                .sortedByDescending { it.lastModified() }
            
            var deletedCount = 0
            if (backupFiles.size > keepCount) {
                val filesToDelete = backupFiles.drop(keepCount)
                filesToDelete.forEach { file ->
                    if (file.delete()) {
                        deletedCount++
                        Log.d("ProjectDataManager", "删除旧备份文件: ${file.name}")
                    }
                }
            }
            
            Log.d("ProjectDataManager", "清理完成，删除了 $deletedCount 个旧备份文件")
            deletedCount
        } catch (e: Exception) {
            Log.e("ProjectDataManager", "清理旧备份文件失败", e)
            0
        }
    }
    
    /**
     * 导出项目数据到指定路径
     */
    suspend fun exportProjectData(
        project: Project,
        logs: List<ConstructionLog>,
        exportPath: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val projectBackup = ProjectBackup(
                projectId = project.id,
                projectName = project.name,
                projectType = project.type.name,
                description = project.description,
                startDate = isoDateFormat.format(project.startDate),
                endDate = project.endDate?.let { isoDateFormat.format(it) },
                manager = project.manager,
                logs = logs.map { log ->
                    LogBackup(
                        logId = log.id,
                        projectId = log.projectId,
                        date = isoDateFormat.format(log.date),
                        weatherCondition = log.weatherCondition,
                        temperature = log.temperature,
                        wind = log.wind,
                        constructionSite = log.constructionSite,
                        mainContent = log.mainContent,
                        personnelEquipment = log.personnelEquipment,
                        qualityManagement = log.qualityManagement,
                        safetyManagement = log.safetyManagement,
                        mediaFiles = log.mediaFiles,
                        createdAt = isoDateFormat.format(log.createdAt),
                        updatedAt = isoDateFormat.format(log.updatedAt)
                    )
                }
            )
            
            val exportFile = File(exportPath)
            val jsonContent = json.encodeToString(projectBackup)
            exportFile.writeText(jsonContent)
            
            Log.d("ProjectDataManager", "项目数据导出成功: $exportPath")
            Result.success(exportPath)
            
        } catch (e: Exception) {
            Log.e("ProjectDataManager", "项目数据导出失败", e)
            Result.failure(e)
        }
    }
    
    /**
     * 获取存储使用情况
     */
    fun getStorageUsage(): Map<String, Long> {
        val projectDataDir = getProjectDataDir()
        val backupDir = getBackupDir()
        
        return mapOf(
            "projectDataSize" to (projectDataDir?.let { calculateDirSize(it) } ?: 0L),
            "backupSize" to (backupDir?.let { calculateDirSize(it) } ?: 0L),
            "totalSize" to (projectDataDir?.let { calculateDirSize(it) } ?: 0L)
        )
    }
    
    /**
     * 计算目录大小
     */
    private fun calculateDirSize(dir: File): Long {
        var size = 0L
        if (dir.exists() && dir.isDirectory) {
            dir.listFiles()?.forEach { file ->
                size += if (file.isDirectory) {
                    calculateDirSize(file)
                } else {
                    file.length()
                }
            }
        }
        return size
    }
}