package com.example.shuigongrizhi.core

import android.content.Context
import com.example.shuigongrizhi.BuildConfig
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
// import javax.inject.Inject
// import javax.inject.Singleton

/**
 * 数据同步管理器
 * 提供数据备份、恢复和同步功能
 */
// @Singleton
class DataSyncManager /* @Inject constructor(
    private val context: Context,
    private val fileManager: FileManager,
    private val appConfig: AppConfig
) */ {
    private val context: Context? = null
    private val fileManager: FileManager? = null
    private val appConfig: AppConfig? = null
    
    companion object {
        private const val BACKUP_FILE_PREFIX = "shuigong_backup"
        private const val BACKUP_FILE_EXTENSION = "zip"
        private const val BACKUP_METADATA_FILE = "backup_metadata.json"
        private const val DATABASE_BACKUP_NAME = "database.db"
        private const val CONFIG_BACKUP_NAME = "config.json"
        private const val IMAGES_BACKUP_DIR = "images"
        private const val VIDEOS_BACKUP_DIR = "videos"
        
        private const val MAX_BACKUP_FILES = 10 // 最大备份文件数量
    }
    
    private val _syncState = MutableStateFlow(SyncState.IDLE)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()
    
    private val _syncProgress = MutableStateFlow(0f)
    val syncProgress: StateFlow<Float> = _syncProgress.asStateFlow()
    
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }
    
    /**
     * 创建完整备份
     */
    suspend fun createFullBackup(): Result<BackupInfo> = withContext(Dispatchers.IO) {
        try {
            _syncState.value = SyncState.BACKING_UP
            _syncProgress.value = 0f
            
            Logger.business("开始创建完整备份")
            
            val timestamp = System.currentTimeMillis()
            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val backupFileName = "${BACKUP_FILE_PREFIX}_${dateFormat.format(Date(timestamp))}.${BACKUP_FILE_EXTENSION}"
            val backupFile = File(fileManager?.getBackupDir() ?: File("backup"), backupFileName)
            
            // 创建备份元数据
            val metadata = BackupMetadata(
                version = BuildConfig.VERSION_NAME,
                timestamp = timestamp,
                type = BackupType.FULL,
                deviceInfo = getDeviceInfo(),
                includeImages = true,
                includeVideos = true,
                includeDatabase = true,
                includeConfig = true
            )
            
            ZipOutputStream(FileOutputStream(backupFile)).use { zipOut ->
                // 备份元数据
                addMetadataToZip(zipOut, metadata)
                _syncProgress.value = 0.1f
                
                // 备份数据库
                if (metadata.includeDatabase) {
                    addDatabaseToZip(zipOut)
                    _syncProgress.value = 0.3f
                }
                
                // 备份配置
                if (metadata.includeConfig) {
                    addConfigToZip(zipOut)
                    _syncProgress.value = 0.4f
                }
                
                // 备份图片
                if (metadata.includeImages) {
                    addImagesToZip(zipOut)
                    _syncProgress.value = 0.7f
                }
                
                // 备份视频
                if (metadata.includeVideos) {
                    addVideosToZip(zipOut)
                    _syncProgress.value = 0.9f
                }
            }
            
            _syncProgress.value = 1f
            
            // 清理旧备份文件
            cleanOldBackups()
            
            // 更新配置
            appConfig.lastBackupTime = timestamp
            
            val backupInfo = BackupInfo(
                fileName = backupFileName,
                filePath = backupFile.absolutePath,
                size = backupFile.length(),
                timestamp = timestamp,
                metadata = metadata
            )
            
            _syncState.value = SyncState.IDLE
            Logger.business("完整备份创建成功: ${backupFile.name}")
            
            Result.Success(backupInfo)
            
        } catch (e: Exception) {
            _syncState.value = SyncState.ERROR
            Logger.exception(e, "创建完整备份失败")
            Result.Error(SyncException("创建备份失败: ${e.message}", e))
        }
    }
    
    /**
     * 恢复备份
     */
    suspend fun restoreBackup(backupFile: File): Result<RestoreInfo> = withContext(Dispatchers.IO) {
        try {
            _syncState.value = SyncState.RESTORING
            _syncProgress.value = 0f
            
            Logger.business("开始恢复备份: ${backupFile.name}")
            
            if (!backupFile.exists()) {
                return@withContext Result.Error(SyncException("备份文件不存在"))
            }
            
            var metadata: BackupMetadata? = null
            val restoredItems = mutableListOf<String>()
            
            ZipInputStream(FileInputStream(backupFile)).use { zipIn ->
                var entry: ZipEntry?
                
                while (zipIn.nextEntry.also { entry = it } != null) {
                    val entryName = entry!!.name
                    
                    when {
                        entryName == BACKUP_METADATA_FILE -> {
                            metadata = readMetadataFromZip(zipIn)
                            _syncProgress.value = 0.1f
                        }
                        
                        entryName == DATABASE_BACKUP_NAME -> {
                            restoreDatabaseFromZip(zipIn)
                            restoredItems.add("数据库")
                            _syncProgress.value = 0.4f
                        }
                        
                        entryName == CONFIG_BACKUP_NAME -> {
                            restoreConfigFromZip(zipIn)
                            restoredItems.add("配置")
                            _syncProgress.value = 0.5f
                        }
                        
                        entryName.startsWith("$IMAGES_BACKUP_DIR/") -> {
                            restoreImageFromZip(zipIn, entryName)
                            if (!restoredItems.contains("图片")) {
                                restoredItems.add("图片")
                            }
                            _syncProgress.value = 0.8f
                        }
                        
                        entryName.startsWith("$VIDEOS_BACKUP_DIR/") -> {
                            restoreVideoFromZip(zipIn, entryName)
                            if (!restoredItems.contains("视频")) {
                                restoredItems.add("视频")
                            }
                            _syncProgress.value = 0.9f
                        }
                    }
                    
                    zipIn.closeEntry()
                }
            }
            
            _syncProgress.value = 1f
            
            val restoreInfo = RestoreInfo(
                backupFileName = backupFile.name,
                restoreTimestamp = System.currentTimeMillis(),
                restoredItems = restoredItems,
                originalMetadata = metadata
            )
            
            _syncState.value = SyncState.IDLE
            Logger.business("备份恢复成功: ${restoredItems.joinToString(", ")}")
            
            Result.Success(restoreInfo)
            
        } catch (e: Exception) {
            _syncState.value = SyncState.ERROR
            Logger.exception(e, "恢复备份失败")
            Result.Error(SyncException("恢复备份失败: ${e.message}", e))
        }
    }
    
    /**
     * 获取备份列表
     */
    suspend fun getBackupList(): Result<List<BackupInfo>> = withContext(Dispatchers.IO) {
        try {
            val backupDir = fileManager?.getBackupDir() ?: File("backup")
            val backupFiles = backupDir.listFiles { file ->
                file.isFile && file.name.startsWith(BACKUP_FILE_PREFIX) && file.name.endsWith(BACKUP_FILE_EXTENSION)
            } ?: emptyArray()
            
            val backupInfoList = backupFiles.mapNotNull { file ->
                try {
                    val metadata = readBackupMetadata(file)
                    BackupInfo(
                        fileName = file.name,
                        filePath = file.absolutePath,
                        size = file.length(),
                        timestamp = metadata?.timestamp ?: file.lastModified(),
                        metadata = metadata
                    )
                } catch (e: Exception) {
                    Logger.exception(e, "读取备份文件元数据失败: ${file.name}")
                    null
                }
            }.sortedByDescending { it.timestamp }
            
            Result.Success(backupInfoList)
            
        } catch (e: Exception) {
            Logger.exception(e, "获取备份列表失败")
            Result.Error(SyncException("获取备份列表失败: ${e.message}", e))
        }
    }
    
    /**
     * 删除备份文件
     */
    suspend fun deleteBackup(backupInfo: BackupInfo): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val file = File(backupInfo.filePath)
            if (file.exists() && file.delete()) {
                Logger.business("备份文件删除成功: ${backupInfo.fileName}")
                Result.Success(Unit)
            } else {
                Result.Error(SyncException("删除备份文件失败"))
            }
        } catch (e: Exception) {
            Logger.exception(e, "删除备份文件失败")
            Result.Error(SyncException("删除备份文件失败: ${e.message}", e))
        }
    }
    
    /**
     * 检查是否需要自动备份
     */
    fun shouldAutoBackup(): Boolean {
        if (appConfig?.isAutoBackupEnabled != true) return false
        
        val lastBackupTime = appConfig?.lastBackupTime ?: 0L
        val currentTime = System.currentTimeMillis()
        val daysSinceLastBackup = (currentTime - lastBackupTime) / (24 * 60 * 60 * 1000)
        
        return daysSinceLastBackup >= 7 // 7天自动备份一次
    }
    
    /**
     * 添加元数据到ZIP
     */
    private fun addMetadataToZip(zipOut: ZipOutputStream, metadata: BackupMetadata) {
        val entry = ZipEntry(BACKUP_METADATA_FILE)
        zipOut.putNextEntry(entry)
        
        val metadataJson = json.encodeToString(metadata)
        zipOut.write(metadataJson.toByteArray())
        zipOut.closeEntry()
    }
    
    /**
     * 添加数据库到ZIP
     */
    private fun addDatabaseToZip(zipOut: ZipOutputStream) {
        val dbFile = context?.getDatabasePath(Constants.Database.NAME) ?: File("database")
        if (dbFile.exists()) {
            val entry = ZipEntry(DATABASE_BACKUP_NAME)
            zipOut.putNextEntry(entry)
            
            FileInputStream(dbFile).use { input ->
                input.copyTo(zipOut)
            }
            zipOut.closeEntry()
        }
    }
    
    /**
     * 添加配置到ZIP
     */
    private fun addConfigToZip(zipOut: ZipOutputStream) {
        val entry = ZipEntry(CONFIG_BACKUP_NAME)
        zipOut.putNextEntry(entry)
        
        val configData = appConfig?.getConfigSummary()?.toString() ?: "{}"
        zipOut.write(configData.toByteArray())
        zipOut.closeEntry()
    }
    
    /**
     * 添加图片到ZIP
     */
    private fun addImagesToZip(zipOut: ZipOutputStream) {
        val imagesDir = fileManager?.getImagesDir() ?: File("images")
        addDirectoryToZip(zipOut, imagesDir, IMAGES_BACKUP_DIR)
    }
    
    /**
     * 添加视频到ZIP
     */
    private fun addVideosToZip(zipOut: ZipOutputStream) {
        val videosDir = fileManager?.getVideosDir() ?: File("videos")
        addDirectoryToZip(zipOut, videosDir, VIDEOS_BACKUP_DIR)
    }
    
    /**
     * 添加目录到ZIP
     */
    private fun addDirectoryToZip(zipOut: ZipOutputStream, sourceDir: File, zipDirName: String) {
        if (!sourceDir.exists()) return
        
        sourceDir.walkTopDown().forEach { file ->
            if (file.isFile) {
                val relativePath = sourceDir.toURI().relativize(file.toURI()).path
                val entry = ZipEntry("$zipDirName/$relativePath")
                zipOut.putNextEntry(entry)
                
                FileInputStream(file).use { input ->
                    input.copyTo(zipOut)
                }
                zipOut.closeEntry()
            }
        }
    }
    
    /**
     * 从ZIP读取元数据
     */
    private fun readMetadataFromZip(zipIn: ZipInputStream): BackupMetadata {
        val metadataJson = zipIn.readBytes().toString(Charsets.UTF_8)
        return json.decodeFromString(metadataJson)
    }
    
    /**
     * 从ZIP恢复数据库
     */
    private fun restoreDatabaseFromZip(zipIn: ZipInputStream) {
        val dbFile = context?.getDatabasePath(Constants.Database.NAME) ?: File("database")
        dbFile.parentFile?.mkdirs()
        
        FileOutputStream(dbFile).use { output ->
            zipIn.copyTo(output)
        }
    }
    
    /**
     * 从ZIP恢复配置
     */
    private fun restoreConfigFromZip(zipIn: ZipInputStream) {
        val configJson = zipIn.readBytes().toString(Charsets.UTF_8)
        // 这里可以根据需要恢复配置
        Logger.d("配置数据已读取，长度: ${configJson.length}", "DataSyncManager")
    }
    
    /**
     * 从ZIP恢复图片
     */
    private fun restoreImageFromZip(zipIn: ZipInputStream, entryName: String) {
        val fileName = entryName.substringAfter("$IMAGES_BACKUP_DIR/")
        val imageFile = File(fileManager?.getImagesDir() ?: File("images"), fileName)
        imageFile.parentFile?.mkdirs()
        
        FileOutputStream(imageFile).use { output ->
            zipIn.copyTo(output)
        }
    }
    
    /**
     * 从ZIP恢复视频
     */
    private fun restoreVideoFromZip(zipIn: ZipInputStream, entryName: String) {
        val fileName = entryName.substringAfter("$VIDEOS_BACKUP_DIR/")
        val videoFile = File(fileManager?.getVideosDir() ?: File("videos"), fileName)
        videoFile.parentFile?.mkdirs()
        
        FileOutputStream(videoFile).use { output ->
            zipIn.copyTo(output)
        }
    }
    
    /**
     * 读取备份文件元数据
     */
    private fun readBackupMetadata(backupFile: File): BackupMetadata? {
        return try {
            ZipInputStream(FileInputStream(backupFile)).use { zipIn ->
                var entry: ZipEntry?
                while (zipIn.nextEntry.also { entry = it } != null) {
                    if (entry!!.name == BACKUP_METADATA_FILE) {
                        return@use readMetadataFromZip(zipIn)
                    }
                    zipIn.closeEntry()
                }
                null
            }
        } catch (e: Exception) {
            Logger.exception(e, "读取备份元数据失败: ${backupFile.name}")
            null
        }
    }
    
    /**
     * 清理旧备份文件
     */
    private fun cleanOldBackups() {
        try {
            val backupDir = fileManager?.getBackupDir() ?: File("backup")
            val backupFiles = backupDir.listFiles { file ->
                file.isFile && file.name.startsWith(BACKUP_FILE_PREFIX) && file.name.endsWith(BACKUP_FILE_EXTENSION)
            } ?: return
            
            if (backupFiles.size > MAX_BACKUP_FILES) {
                val sortedFiles = backupFiles.sortedByDescending { it.lastModified() }
                val filesToDelete = sortedFiles.drop(MAX_BACKUP_FILES)
                
                filesToDelete.forEach { file ->
                    if (file.delete()) {
                        Logger.d("删除旧备份文件: ${file.name}", "DataSyncManager")
                    }
                }
            }
        } catch (e: Exception) {
            Logger.exception(e, "清理旧备份文件失败")
        }
    }
    
    /**
     * 获取设备信息
     */
    private fun getDeviceInfo(): Map<String, String> {
        return mapOf(
            "model" to android.os.Build.MODEL,
            "manufacturer" to android.os.Build.MANUFACTURER,
            "androidVersion" to android.os.Build.VERSION.RELEASE,
            "apiLevel" to android.os.Build.VERSION.SDK_INT.toString(),
            "appVersion" to BuildConfig.VERSION_NAME,
            "appVersionCode" to BuildConfig.VERSION_CODE.toString()
        )
    }
}

/**
 * 同步状态枚举
 */
enum class SyncState {
    IDLE,
    BACKING_UP,
    RESTORING,
    ERROR
}

/**
 * 备份类型枚举
 */
@kotlinx.serialization.Serializable
enum class BackupType {
    FULL,
    INCREMENTAL,
    SELECTIVE
}

/**
 * 备份元数据
 */
@kotlinx.serialization.Serializable
data class BackupMetadata(
    val version: String,
    val timestamp: Long,
    val type: BackupType,
    val deviceInfo: Map<String, String>,
    val includeImages: Boolean,
    val includeVideos: Boolean,
    val includeDatabase: Boolean,
    val includeConfig: Boolean
) {
    fun getFormattedDate(): String {
        return Utils.DateTime.formatTimestamp(timestamp)
    }
}

/**
 * 备份信息
 */
data class BackupInfo(
    val fileName: String,
    val filePath: String,
    val size: Long,
    val timestamp: Long,
    val metadata: BackupMetadata?
) {
    fun getReadableSize(): String {
        return size.toReadableFileSize()
    }
    
    fun getFormattedDate(): String {
        return Utils.DateTime.formatTimestamp(timestamp)
    }
    
    fun getBackupTypeDisplay(): String {
        return metadata?.type?.name ?: "未知"
    }
}

/**
 * 恢复信息
 */
data class RestoreInfo(
    val backupFileName: String,
    val restoreTimestamp: Long,
    val restoredItems: List<String>,
    val originalMetadata: BackupMetadata?
) {
    fun getFormattedDate(): String {
        return Utils.DateTime.formatTimestamp(restoreTimestamp)
    }
    
    fun getRestoredItemsDisplay(): String {
        return restoredItems.joinToString(", ")
    }
}

/**
 * 同步异常类
 */
class SyncException(message: String, cause: Throwable? = null) : Exception(message, cause)