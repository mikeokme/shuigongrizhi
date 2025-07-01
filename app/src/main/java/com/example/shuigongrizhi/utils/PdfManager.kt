package com.example.shuigongrizhi.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * PDF文件管理器
 * 负责PDF文件的存储、命名和查看功能
 */
class PdfManager(private val context: Context) {
    
    companion object {
        private const val PDF_FOLDER_NAME = "ConstructionLogs"
        private const val DATE_FORMAT = "yyMMdd"
    }
    
    /**
     * 获取PDF存储目录
     */
    fun getPdfStorageDirectory(): File {
        // 优先使用外部存储的Documents目录
        val documentsDir = if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), PDF_FOLDER_NAME)
        } else {
            // 如果外部存储不可用，使用应用私有目录
            File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), PDF_FOLDER_NAME)
        }
        
        // 确保目录存在
        if (!documentsDir.exists()) {
            documentsDir.mkdirs()
        }
        
        return documentsDir
    }
    
    /**
     * 生成PDF文件名
     * 格式：yymmdd施工日志_项目名称.pdf
     */
    fun generatePdfFileName(projectName: String, date: Date): String {
        val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        val dateStr = dateFormat.format(date)
        // 清理项目名称中的特殊字符
        val cleanProjectName = projectName.replace(Regex("[^\\w\\s-]"), "")
        return "${dateStr}施工日志_${cleanProjectName}.pdf"
    }
    
    /**
     * 创建PDF文件
     */
    fun createPdfFile(projectName: String, date: Date): File {
        val directory = getPdfStorageDirectory()
        val fileName = generatePdfFileName(projectName, date)
        return File(directory, fileName)
    }
    
    /**
     * 获取所有PDF文件列表
     */
    fun getAllPdfFiles(): List<File> {
        val directory = getPdfStorageDirectory()
        return directory.listFiles { file ->
            file.isFile && file.extension.lowercase() == "pdf"
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }
    
    /**
     * 根据项目名称获取PDF文件列表
     */
    fun getPdfFilesByProject(projectName: String): List<File> {
        return getAllPdfFiles().filter { file ->
            file.name.contains(projectName)
        }
    }
    
    /**
     * 根据日期获取PDF文件
     */
    fun getPdfFileByDate(date: Date): List<File> {
        val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        val dateStr = dateFormat.format(date)
        return getAllPdfFiles().filter { file ->
            file.name.startsWith(dateStr)
        }
    }
    
    /**
     * 打开PDF文件
     */
    fun openPdfFile(file: File): Intent? {
        return try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            
            // 检查是否有应用可以处理PDF
            if (intent.resolveActivity(context.packageManager) != null) {
                intent
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * 分享PDF文件
     */
    fun sharePdfFile(file: File): Intent? {
        return try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "施工日志 - ${file.nameWithoutExtension}")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * 删除PDF文件
     */
    fun deletePdfFile(file: File): Boolean {
        return try {
            file.delete()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * 获取PDF文件信息
     */
    fun getPdfFileInfo(file: File): PdfFileInfo? {
        return if (file.exists()) {
            PdfFileInfo(
                name = file.nameWithoutExtension,
                size = file.length(),
                lastModified = Date(file.lastModified()),
                path = file.absolutePath
            )
        } else {
            null
        }
    }
    
    /**
     * PDF文件信息数据类
     */
    data class PdfFileInfo(
        val name: String,
        val size: Long,
        val lastModified: Date,
        val path: String
    )
}