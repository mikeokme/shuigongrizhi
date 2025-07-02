package com.example.shuigongrizhi.core

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.*
import java.security.MessageDigest
// import javax.inject.Inject
// import javax.inject.Singleton

/**
 * 缓存管理器
 * 提供内存缓存和磁盘缓存功能
 */
// @Singleton
class CacheManager /* @Inject constructor(
    private val context: Context,
    private val fileManager: FileManager
) */ {
    private val context: Context? = null
    private val fileManager: FileManager? = null
    
    companion object {
        private const val CACHE_DIR = "cache"
        private const val IMAGE_CACHE_DIR = "image_cache"
        private const val DATA_CACHE_DIR = "data_cache"
        
        // 内存缓存大小（以字节为单位）
        private const val MEMORY_CACHE_SIZE = 1024 * 1024 * 8 // 8MB
        
        // 磁盘缓存大小（以字节为单位）
        private const val DISK_CACHE_SIZE = 1024 * 1024 * 50L // 50MB
        
        // 缓存过期时间（毫秒）
        private const val DEFAULT_CACHE_EXPIRE_TIME = 24 * 60 * 60 * 1000L // 24小时
    }
    
    // 内存缓存
    internal val memoryCache: LruCache<String, CacheEntry> = object : LruCache<String, CacheEntry>(MEMORY_CACHE_SIZE) {
        override fun sizeOf(key: String, value: CacheEntry): Int {
            return value.size
        }
    }
    
    internal val bitmapCache: LruCache<String, Bitmap> = object : LruCache<String, Bitmap>(MEMORY_CACHE_SIZE / 4) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount
        }
    }
    
    internal val diskCacheMutex = Mutex()
    
    // JSON序列化器
    private val json = Json { ignoreUnknownKeys = true }
    
    /**
     * 获取缓存目录
     */
    private fun getCacheDir(): File {
        return File(fileManager?.getAppRootDir() ?: File("cache"), CACHE_DIR).apply {
            if (!exists()) mkdirs()
        }
    }
    
    /**
     * 获取图片缓存目录
     */
    private fun getImageCacheDir(): File {
        return File(getCacheDir(), IMAGE_CACHE_DIR).apply {
            if (!exists()) mkdirs()
        }
    }
    
    /**
     * 获取数据缓存目录
     */
    private fun getDataCacheDir(): File {
        return File(getCacheDir(), DATA_CACHE_DIR).apply {
            if (!exists()) mkdirs()
        }
    }
    
    /**
     * 生成缓存键的哈希值
     */
    private fun hashKey(key: String): String {
        val digest = MessageDigest.getInstance("MD5")
        val hash = digest.digest(key.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * 存储字符串到内存缓存
     */
    fun putString(key: String, value: String, expireTime: Long = DEFAULT_CACHE_EXPIRE_TIME) {
        val entry = CacheEntry(
            data = value.toByteArray(),
            timestamp = System.currentTimeMillis(),
            expireTime = expireTime,
            type = CacheType.STRING
        )
        memoryCache.put(key, entry)
        Logger.d("字符串缓存存储: $key", "CacheManager")
    }
    
    /**
     * 从内存缓存获取字符串
     */
    fun getString(key: String): String? {
        val entry = memoryCache.get(key) ?: return null
        
        if (entry.isExpired()) {
            memoryCache.remove(key)
            Logger.d("缓存已过期，移除: $key", "CacheManager")
            return null
        }
        
        return String(entry.data)
    }
    
    /**
     * 存储对象到内存缓存（使用JSON序列化）
     */
    internal inline fun <reified T> putObject(key: String, value: T, expireTime: Long = DEFAULT_CACHE_EXPIRE_TIME) {
        try {
            val jsonString = json.encodeToString(value)
            putString(key, jsonString, expireTime)
            Logger.d("对象缓存存储: $key", "CacheManager")
        } catch (e: Exception) {
            Logger.exception(e, "对象缓存存储失败: $key")
        }
    }
    
    /**
     * 从内存缓存获取对象（使用JSON反序列化）
     */
    internal inline fun <reified T> getObject(key: String): T? {
        return try {
            val jsonString = getString(key) ?: return null
            json.decodeFromString<T>(jsonString)
        } catch (e: Exception) {
            Logger.exception(e, "对象缓存获取失败: $key")
            null
        }
    }
    
    /**
     * 存储图片到内存缓存
     */
    fun putBitmap(key: String, bitmap: Bitmap) {
        bitmapCache.put(key, bitmap)
        Logger.d("图片缓存存储: $key", "CacheManager")
    }
    
    /**
     * 从内存缓存获取图片
     */
    fun getBitmap(key: String): Bitmap? {
        return bitmapCache.get(key)
    }
    
    /**
     * 存储数据到磁盘缓存
     */
    suspend fun putToDisk(key: String, data: ByteArray, expireTime: Long = DEFAULT_CACHE_EXPIRE_TIME): Result<Unit> = withContext(Dispatchers.IO) {
        diskCacheMutex.withLock {
            try {
                val hashedKey = hashKey(key)
                val cacheFile = File(getDataCacheDir(), hashedKey)
                val metaFile = File(getDataCacheDir(), "$hashedKey.meta")
                
                // 写入数据
                FileOutputStream(cacheFile).use { it.write(data) }
                
                // 写入元数据
                val metadata = CacheMetadata(
                    originalKey = key,
                    timestamp = System.currentTimeMillis(),
                    expireTime = expireTime,
                    size = data.size
                )
                val metaJson = json.encodeToString(metadata)
                FileWriter(metaFile).use { it.write(metaJson) }
                
                Logger.d("磁盘缓存存储: $key", "CacheManager")
                Result.Success(Unit)
                
            } catch (e: Exception) {
                Logger.exception(e, "磁盘缓存存储失败: $key")
                Result.Error(CacheException("磁盘缓存存储失败: ${e.message}", e))
            }
        }
    }
    
    /**
     * 从磁盘缓存获取数据
     */
    suspend fun getFromDisk(key: String): Result<ByteArray?> = withContext(Dispatchers.IO) {
        diskCacheMutex.withLock {
            try {
                val hashedKey = hashKey(key)
                val cacheFile = File(getDataCacheDir(), hashedKey)
                val metaFile = File(getDataCacheDir(), "$hashedKey.meta")
                
                if (!cacheFile.exists() || !metaFile.exists()) {
                    return@withLock Result.Success(null)
                }
                
                // 读取元数据
                val metaJson = FileReader(metaFile).use { it.readText() }
                val metadata = json.decodeFromString<CacheMetadata>(metaJson)
                
                // 检查是否过期
                if (metadata.isExpired()) {
                    cacheFile.delete()
                    metaFile.delete()
                    Logger.d("磁盘缓存已过期，移除: $key", "CacheManager")
                    return@withLock Result.Success(null)
                }
                
                // 读取数据
                val data = FileInputStream(cacheFile).use { it.readBytes() }
                Logger.d("磁盘缓存获取: $key", "CacheManager")
                Result.Success(data)
                
            } catch (e: Exception) {
                Logger.exception(e, "磁盘缓存获取失败: $key")
                Result.Error(CacheException("磁盘缓存获取失败: ${e.message}", e))
            }
        }
    }
    
    /**
     * 存储图片到磁盘缓存
     */
    suspend fun putBitmapToDisk(key: String, bitmap: Bitmap, expireTime: Long = DEFAULT_CACHE_EXPIRE_TIME): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val hashedKey = hashKey(key)
            val cacheFile = File(getImageCacheDir(), "$hashedKey.jpg")
            
            FileOutputStream(cacheFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }
            
            // 存储元数据
            val metaFile = File(getImageCacheDir(), "$hashedKey.meta")
            val metadata = CacheMetadata(
                originalKey = key,
                timestamp = System.currentTimeMillis(),
                expireTime = expireTime,
                size = cacheFile.length().toInt()
            )
            val metaJson = json.encodeToString(metadata)
            FileWriter(metaFile).use { it.write(metaJson) }
            
            Logger.d("图片磁盘缓存存储: $key", "CacheManager")
            Result.Success(Unit)
            
        } catch (e: Exception) {
            Logger.exception(e, "图片磁盘缓存存储失败: $key")
            Result.Error(CacheException("图片磁盘缓存存储失败: ${e.message}", e))
        }
    }
    
    /**
     * 从磁盘缓存获取图片
     */
    suspend fun getBitmapFromDisk(key: String): Result<Bitmap?> = withContext(Dispatchers.IO) {
        try {
            val hashedKey = hashKey(key)
            val cacheFile = File(getImageCacheDir(), "$hashedKey.jpg")
            val metaFile = File(getImageCacheDir(), "$hashedKey.meta")
            
            if (!cacheFile.exists() || !metaFile.exists()) {
                return@withContext Result.Success(null)
            }
            
            // 检查元数据
            val metaJson = FileReader(metaFile).use { it.readText() }
            val metadata = json.decodeFromString<CacheMetadata>(metaJson)
            
            if (metadata.isExpired()) {
                cacheFile.delete()
                metaFile.delete()
                Logger.d("图片磁盘缓存已过期，移除: $key", "CacheManager")
                return@withContext Result.Success(null)
            }
            
            // 加载图片
            val bitmap = BitmapFactory.decodeFile(cacheFile.absolutePath)
            Logger.d("图片磁盘缓存获取: $key", "CacheManager")
            Result.Success(bitmap)
            
        } catch (e: Exception) {
            Logger.exception(e, "图片磁盘缓存获取失败: $key")
            Result.Error(CacheException("图片磁盘缓存获取失败: ${e.message}", e))
        }
    }
    
    /**
     * 移除内存缓存
     */
    fun removeFromMemory(key: String) {
        memoryCache.remove(key)
        bitmapCache.remove(key)
        Logger.d("内存缓存移除: $key", "CacheManager")
    }
    
    /**
     * 移除磁盘缓存
     */
    suspend fun removeFromDisk(key: String): Result<Unit> = withContext(Dispatchers.IO) {
        diskCacheMutex.withLock {
            try {
                val hashedKey = hashKey(key)
                
                // 移除数据缓存
                File(getDataCacheDir(), hashedKey).delete()
                File(getDataCacheDir(), "$hashedKey.meta").delete()
                
                // 移除图片缓存
                File(getImageCacheDir(), "$hashedKey.jpg").delete()
                File(getImageCacheDir(), "$hashedKey.meta").delete()
                
                Logger.d("磁盘缓存移除: $key", "CacheManager")
                Result.Success(Unit)
                
            } catch (e: Exception) {
                Logger.exception(e, "磁盘缓存移除失败: $key")
                Result.Error(CacheException("磁盘缓存移除失败: ${e.message}", e))
            }
        }
    }
    
    /**
     * 清空内存缓存
     */
    fun clearMemoryCache() {
        memoryCache.evictAll()
        bitmapCache.evictAll()
        Logger.business("内存缓存已清空")
    }
    
    /**
     * 清空磁盘缓存
     */
    suspend fun clearDiskCache(): Result<Unit> = withContext(Dispatchers.IO) {
        diskCacheMutex.withLock {
            try {
                getDataCacheDir().deleteRecursively()
                getImageCacheDir().deleteRecursively()
                
                // 重新创建目录
                getDataCacheDir().mkdirs()
                getImageCacheDir().mkdirs()
                
                Logger.business("磁盘缓存已清空")
                Result.Success(Unit)
                
            } catch (e: Exception) {
                Logger.exception(e, "清空磁盘缓存失败")
                Result.Error(CacheException("清空磁盘缓存失败: ${e.message}", e))
            }
        }
    }
    
    /**
     * 清理过期缓存
     */
    suspend fun cleanExpiredCache(): Result<Int> = withContext(Dispatchers.IO) {
        diskCacheMutex.withLock {
            try {
                var cleanedCount = 0
                
                // 清理数据缓存
                cleanedCount += cleanExpiredCacheInDir(getDataCacheDir())
                
                // 清理图片缓存
                cleanedCount += cleanExpiredCacheInDir(getImageCacheDir())
                
                Logger.business("过期缓存清理完成，清理了 $cleanedCount 个文件")
                Result.Success(cleanedCount)
                
            } catch (e: Exception) {
                Logger.exception(e, "清理过期缓存失败")
                Result.Error(CacheException("清理过期缓存失败: ${e.message}", e))
            }
        }
    }
    
    /**
     * 清理指定目录中的过期缓存
     */
    private fun cleanExpiredCacheInDir(dir: File): Int {
        var cleanedCount = 0
        
        dir.listFiles { file -> file.name.endsWith(".meta") }?.forEach { metaFile ->
            try {
                val metaJson = FileReader(metaFile).use { it.readText() }
                val metadata = json.decodeFromString<CacheMetadata>(metaJson)
                
                if (metadata.isExpired()) {
                    val dataFileName = metaFile.nameWithoutExtension
                    val dataFile = File(dir, dataFileName)
                    val jpgFile = File(dir, "$dataFileName.jpg")
                    
                    metaFile.delete()
                    if (dataFile.exists()) dataFile.delete()
                    if (jpgFile.exists()) jpgFile.delete()
                    
                    cleanedCount++
                }
            } catch (e: Exception) {
                Logger.exception(e, "清理缓存文件失败: ${metaFile.name}")
            }
        }
        
        return cleanedCount
    }
    
    /**
     * 获取缓存统计信息
     */
    suspend fun getCacheStatistics(): Result<CacheStatistics> = withContext(Dispatchers.IO) {
        try {
            val memoryCacheSize = memoryCache.size()
            val bitmapCacheSize = bitmapCache.size()
            
            val dataCacheSize = calculateDirectorySize(getDataCacheDir())
            val imageCacheSize = calculateDirectorySize(getImageCacheDir())
            val totalDiskCacheSize = dataCacheSize + imageCacheSize
            
            val dataCacheCount = getDataCacheDir().listFiles()?.count { !it.name.endsWith(".meta") } ?: 0
            val imageCacheCount = getImageCacheDir().listFiles()?.count { it.name.endsWith(".jpg") } ?: 0
            
            val statistics = CacheStatistics(
                memoryCacheSize = memoryCacheSize,
                bitmapCacheSize = bitmapCacheSize,
                diskCacheSize = totalDiskCacheSize,
                dataCacheSize = dataCacheSize,
                imageCacheSize = imageCacheSize,
                dataCacheCount = dataCacheCount,
                imageCacheCount = imageCacheCount,
                totalCacheCount = dataCacheCount + imageCacheCount
            )
            
            Result.Success(statistics)
            
        } catch (e: Exception) {
            Logger.exception(e, "获取缓存统计信息失败")
            Result.Error(CacheException("获取缓存统计信息失败: ${e.message}", e))
        }
    }
    
    /**
     * 计算目录大小
     */
    private fun calculateDirectorySize(dir: File): Long {
        var size = 0L
        dir.walkTopDown().forEach { file ->
            if (file.isFile) {
                size += file.length()
            }
        }
        return size
    }
}

/**
 * 缓存条目数据类
 */
data class CacheEntry(
    val data: ByteArray,
    val timestamp: Long,
    val expireTime: Long,
    val type: CacheType
) {
    val size: Int get() = data.size
    
    fun isExpired(): Boolean {
        return System.currentTimeMillis() - timestamp > expireTime
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        
        other as CacheEntry
        
        if (!data.contentEquals(other.data)) return false
        if (timestamp != other.timestamp) return false
        if (expireTime != other.expireTime) return false
        if (type != other.type) return false
        
        return true
    }
    
    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + expireTime.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }
}

/**
 * 缓存类型枚举
 */
enum class CacheType {
    STRING,
    OBJECT,
    BITMAP,
    BINARY
}

/**
 * 缓存元数据
 */
@kotlinx.serialization.Serializable
data class CacheMetadata(
    val originalKey: String,
    val timestamp: Long,
    val expireTime: Long,
    val size: Int
) {
    fun isExpired(): Boolean {
        return System.currentTimeMillis() - timestamp > expireTime
    }
}

/**
 * 缓存统计信息
 */
data class CacheStatistics(
    val memoryCacheSize: Int,
    val bitmapCacheSize: Int,
    val diskCacheSize: Long,
    val dataCacheSize: Long,
    val imageCacheSize: Long,
    val dataCacheCount: Int,
    val imageCacheCount: Int,
    val totalCacheCount: Int
) {
    fun getReadableDiskCacheSize(): String {
        return diskCacheSize.toReadableFileSize()
    }
    
    fun getReadableDataCacheSize(): String {
        return dataCacheSize.toReadableFileSize()
    }
    
    fun getReadableImageCacheSize(): String {
        return imageCacheSize.toReadableFileSize()
    }
}

/**
 * 缓存异常类
 */
class CacheException(message: String, cause: Throwable? = null) : Exception(message, cause)