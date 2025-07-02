package com.example.shuigongrizhi.core

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.net.Uri
import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
// import javax.inject.Inject
// import javax.inject.Singleton

/**
 * 文件管理器
 * 提供统一的文件操作和媒体文件处理功能
 */
// @Singleton
class FileManager /* @Inject constructor(
    private val context: Context,
    private val appConfig: AppConfig
) */ {
    private val context: Context? = null
    private val appConfig: AppConfig? = null
    
    companion object {
        private const val IMAGES_DIR = "images"
        private const val VIDEOS_DIR = "videos"
        private const val EXPORTS_DIR = "exports"
        private const val TEMP_DIR = "temp"
        private const val BACKUP_DIR = "backup"
        
        private const val MAX_IMAGE_SIZE = 2048 // 最大图片尺寸
        private const val JPEG_QUALITY = 85 // JPEG压缩质量
    }
    
    /**
     * 获取应用根目录
     */
    fun getAppRootDir(): File {
        return File(context?.getExternalFilesDir(null) ?: File("app"), Constants.Storage.APP_FOLDER_NAME)
    }
    
    /**
     * 获取图片目录
     */
    fun getImagesDir(): File {
        return File(getAppRootDir(), IMAGES_DIR).apply {
            if (!exists()) mkdirs()
        }
    }
    
    /**
     * 获取视频目录
     */
    fun getVideosDir(): File {
        return File(getAppRootDir(), VIDEOS_DIR).apply {
            if (!exists()) mkdirs()
        }
    }
    
    /**
     * 获取导出目录
     */
    fun getExportsDir(): File {
        return File(getAppRootDir(), EXPORTS_DIR).apply {
            if (!exists()) mkdirs()
        }
    }
    
    /**
     * 获取临时目录
     */
    fun getTempDir(): File {
        return File(getAppRootDir(), TEMP_DIR).apply {
            if (!exists()) mkdirs()
        }
    }
    
    /**
     * 获取备份目录
     */
    fun getBackupDir(): File {
        return File(getAppRootDir(), BACKUP_DIR).apply {
            if (!exists()) mkdirs()
        }
    }
    
    /**
     * 生成唯一文件名
     */
    fun generateUniqueFileName(prefix: String = "file", extension: String): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(Date())
        val uuid = Utils.Crypto.generateUUID().take(8)
        return "${prefix}_${timestamp}_${uuid}.${extension.removePrefix(".")}"
    }
    
    /**
     * 保存图片文件
     */
    suspend fun saveImage(
        bitmap: Bitmap,
        fileName: String? = null,
        compress: Boolean = true
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val finalFileName = fileName ?: generateUniqueFileName("img", "jpg")
            val file = File(getImagesDir(), finalFileName)
            
            val finalBitmap = if (compress && appConfig?.isCompressImages == true) {
                compressBitmap(bitmap)
            } else {
                bitmap
            }
            
            FileOutputStream(file).use { out ->
                val format = if (finalFileName.endsWith(".png", true)) {
                    Bitmap.CompressFormat.PNG
                } else {
                    Bitmap.CompressFormat.JPEG
                }
                
                val quality = if (format == Bitmap.CompressFormat.JPEG) {
                    appConfig?.imageQuality ?: 100
                } else {
                    100
                }
                
                finalBitmap.compress(format, quality, out)
            }
            
            Logger.business("图片保存成功: ${file.absolutePath}")
            Result.Success(file)
            
        } catch (e: Exception) {
            Logger.exception(e, "保存图片失败")
            Result.Error(FileException("保存图片失败: ${e.message}", e))
        }
    }
    
    /**
     * 压缩图片
     */
    private fun compressBitmap(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        if (width <= MAX_IMAGE_SIZE && height <= MAX_IMAGE_SIZE) {
            return bitmap
        }
        
        val ratio = minOf(
            MAX_IMAGE_SIZE.toFloat() / width,
            MAX_IMAGE_SIZE.toFloat() / height
        )
        
        val newWidth = (width * ratio).toInt()
        val newHeight = (height * ratio).toInt()
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
    
    /**
     * 从URI加载图片
     */
    suspend fun loadImageFromUri(uri: Uri): Result<Bitmap> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context?.contentResolver?.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            if (bitmap != null) {
                Result.Success(bitmap)
            } else {
                Result.Error(FileException("无法解码图片"))
            }
        } catch (e: Exception) {
            Logger.exception(e, "从URI加载图片失败")
            Result.Error(FileException("加载图片失败: ${e.message}", e))
        }
    }
    
    /**
     * 复制文件
     */
    suspend fun copyFile(source: File, destination: File): Result<File> = withContext(Dispatchers.IO) {
        try {
            destination.parentFile?.mkdirs()
            
            FileInputStream(source).use { input ->
                FileOutputStream(destination).use { output ->
                    input.copyTo(output)
                }
            }
            
            Logger.business("文件复制成功: ${source.name} -> ${destination.name}")
            Result.Success(destination)
            
        } catch (e: Exception) {
            Logger.exception(e, "复制文件失败")
            Result.Error(FileException("复制文件失败: ${e.message}", e))
        }
    }
    
    /**
     * 移动文件
     */
    suspend fun moveFile(source: File, destination: File): Result<File> = withContext(Dispatchers.IO) {
        try {
            destination.parentFile?.mkdirs()
            
            if (source.renameTo(destination)) {
                Logger.business("文件移动成功: ${source.name} -> ${destination.name}")
                Result.Success(destination)
            } else {
                // 如果重命名失败，尝试复制后删除
                val copyResult = copyFile(source, destination)
                if (copyResult is Result.Success) {
                    if (source.delete()) {
                        Result.Success(destination)
                    } else {
                        Result.Error(FileException("移动文件失败：无法删除源文件"))
                    }
                } else {
                    copyResult
                }
            }
        } catch (e: Exception) {
            Logger.exception(e, "移动文件失败")
            Result.Error(FileException("移动文件失败: ${e.message}", e))
        }
    }
    
    /**
     * 删除文件
     */
    suspend fun deleteFile(file: File): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val deleted = if (file.isDirectory) {
                file.deleteRecursively()
            } else {
                file.delete()
            }
            
            if (deleted) {
                Logger.business("文件删除成功: ${file.name}")
                Result.Success(true)
            } else {
                Result.Error(FileException("删除文件失败：文件可能不存在或无权限"))
            }
        } catch (e: Exception) {
            Logger.exception(e, "删除文件失败")
            Result.Error(FileException("删除文件失败: ${e.message}", e))
        }
    }
    
    /**
     * 获取文件信息
     */
    fun getFileInfo(file: File): FileInfo? {
        if (!file.exists()) return null
        
        return FileInfo(
            name = file.name,
            path = file.absolutePath,
            size = file.length(),
            lastModified = file.lastModified(),
            isDirectory = file.isDirectory,
            extension = file.extension,
            mimeType = getMimeType(file),
            readableSize = file.length().toReadableFileSize()
        )
    }
    
    /**
     * 获取MIME类型
     */
    private fun getMimeType(file: File): String {
        return when (file.extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            "mp4" -> "video/mp4"
            "avi" -> "video/avi"
            "mov" -> "video/quicktime"
            "pdf" -> "application/pdf"
            "txt" -> "text/plain"
            "json" -> "application/json"
            "xml" -> "application/xml"
            else -> "application/octet-stream"
        }
    }
    
    /**
     * 获取图片EXIF信息
     */
    suspend fun getImageExifInfo(file: File): Result<ExifInfo> = withContext(Dispatchers.IO) {
        try {
            val exif = ExifInterface(file.absolutePath)
            
            val exifInfo = ExifInfo(
                dateTime = exif.getAttribute(ExifInterface.TAG_DATETIME),
                latitude = run {
                    val latLong = FloatArray(2)
                    if (exif.getLatLong(latLong)) latLong[0].toDouble() else null
                },
                longitude = run {
                    val latLong = FloatArray(2)
                    if (exif.getLatLong(latLong)) latLong[1].toDouble() else null
                },
                orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL),
                make = exif.getAttribute(ExifInterface.TAG_MAKE),
                model = exif.getAttribute(ExifInterface.TAG_MODEL),
                imageWidth = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0),
                imageHeight = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0)
            )
            
            Result.Success(exifInfo)
        } catch (e: Exception) {
            Logger.exception(e, "读取EXIF信息失败")
            Result.Error(FileException("读取EXIF信息失败: ${e.message}", e))
        }
    }
    
    /**
     * 清理临时文件
     */
    suspend fun cleanTempFiles(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val tempDir = getTempDir()
            var deletedCount = 0
            
            tempDir.listFiles()?.forEach { file ->
                if (file.delete()) {
                    deletedCount++
                }
            }
            
            Logger.business("清理临时文件完成，删除 $deletedCount 个文件")
            Result.Success(deletedCount)
            
        } catch (e: Exception) {
            Logger.exception(e, "清理临时文件失败")
            Result.Error(FileException("清理临时文件失败: ${e.message}", e))
        }
    }
    
    /**
     * 获取目录大小
     */
    suspend fun getDirectorySize(directory: File): Result<Long> = withContext(Dispatchers.IO) {
        try {
            var size = 0L
            
            directory.walkTopDown().forEach { file ->
                if (file.isFile) {
                    size += file.length()
                }
            }
            
            Result.Success(size)
        } catch (e: Exception) {
            Logger.exception(e, "计算目录大小失败")
            Result.Error(FileException("计算目录大小失败: ${e.message}", e))
        }
    }
    
    /**
     * 获取存储空间信息
     */
    fun getStorageInfo(): StorageInfo {
        val externalDir = context?.getExternalFilesDir(null)
        val totalSpace = externalDir?.totalSpace ?: 0L
        val freeSpace = externalDir?.freeSpace ?: 0L
        val usedSpace = totalSpace - freeSpace
        
        return StorageInfo(
            totalSpace = totalSpace,
            freeSpace = freeSpace,
            usedSpace = usedSpace,
            totalSpaceReadable = totalSpace.toReadableFileSize(),
            freeSpaceReadable = freeSpace.toReadableFileSize(),
            usedSpaceReadable = usedSpace.toReadableFileSize()
        )
    }
    
    /**
     * 检查存储空间是否充足
     */
    fun hasEnoughSpace(requiredBytes: Long): Boolean {
        val storageInfo = getStorageInfo()
        return storageInfo.freeSpace > requiredBytes
    }
}

/**
 * 文件信息数据类
 */
data class FileInfo(
    val name: String,
    val path: String,
    val size: Long,
    val lastModified: Long,
    val isDirectory: Boolean,
    val extension: String,
    val mimeType: String,
    val readableSize: String
) {
    fun getFormattedDate(): String {
        return Utils.DateTime.formatTimestamp(lastModified)
    }
    
    fun isImage(): Boolean {
        return mimeType.startsWith("image/")
    }
    
    fun isVideo(): Boolean {
        return mimeType.startsWith("video/")
    }
}

/**
 * EXIF信息数据类
 */
data class ExifInfo(
    val dateTime: String?,
    val latitude: Double?,
    val longitude: Double?,
    val orientation: Int,
    val make: String?,
    val model: String?,
    val imageWidth: Int,
    val imageHeight: Int
) {
    fun hasLocation(): Boolean {
        return latitude != null && longitude != null
    }
    
    fun getLocationString(): String? {
        return if (hasLocation()) {
            "${latitude}, ${longitude}"
        } else {
            null
        }
    }
}

/**
 * 存储空间信息数据类
 */
data class StorageInfo(
    val totalSpace: Long,
    val freeSpace: Long,
    val usedSpace: Long,
    val totalSpaceReadable: String,
    val freeSpaceReadable: String,
    val usedSpaceReadable: String
) {
    val usagePercentage: Float
        get() = if (totalSpace > 0) (usedSpace.toFloat() / totalSpace * 100) else 0f
    
    fun isLowSpace(): Boolean {
        return usagePercentage > 90f
    }
}

/**
 * 文件异常类
 */
class FileException(message: String, cause: Throwable? = null) : Exception(message, cause)