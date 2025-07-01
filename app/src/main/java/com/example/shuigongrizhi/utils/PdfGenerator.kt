package com.example.shuigongrizhi.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.graphics.pdf.PdfDocument.Page
import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.example.shuigongrizhi.R
import com.example.shuigongrizhi.data.entity.ConstructionLog
import com.example.shuigongrizhi.data.entity.Project
import com.example.shuigongrizhi.data.entity.MediaFile
import com.example.shuigongrizhi.data.entity.MediaType
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class PdfGenerator(private val context: Context) {
    
    private val pdfManager = PdfManager(context)
    
    private val paint = Paint()
    private val textPaint = Paint()
    private val titlePaint = Paint()
    private val tablePaint = Paint()
    private val imagePaint = Paint()
    
    init {
        setupPaints()
    }
    
    private fun setupPaints() {
        // 标题画笔
        titlePaint.apply {
            color = Color.BLACK
            textSize = 18f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        
        // 正文画笔
        textPaint.apply {
            color = Color.BLACK
            textSize = 12f
            textAlign = Paint.Align.LEFT
        }
        
        // 表格画笔
        tablePaint.apply {
            color = Color.BLACK
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }
        
        // 图片画笔
        imagePaint.apply {
            isFilterBitmap = true
            isAntiAlias = true
        }
    }
    
    /**
     * 生成当日施工日志 PDF
     */
    fun generateDailyConstructionLog(
        project: Project,
        constructionLog: ConstructionLog,
        mediaFiles: List<MediaFile>
    ): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PageInfo.Builder(595, 842, 1).create() // A4 尺寸
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        
        var yPosition = 50f
        
        // 1. 生成标题
        yPosition = drawTitle(canvas, yPosition)
        
        // 2. 生成基本信息表格
        yPosition = drawBasicInfoTable(canvas, yPosition, project, constructionLog)
        
        // 3. 生成天气信息表格
        yPosition = drawWeatherTable(canvas, yPosition, constructionLog)
        
        // 4. 生成施工内容表格
        yPosition = drawConstructionContentTable(canvas, yPosition, constructionLog)
        
        // 5. 生成人员设备表格
        yPosition = drawPersonnelEquipmentTable(canvas, yPosition, constructionLog)
        
        // 6. 生成质量管理表格
        yPosition = drawQualityManagementTable(canvas, yPosition, constructionLog)
        
        // 7. 生成安全管理表格
        yPosition = drawSafetyManagementTable(canvas, yPosition, constructionLog)
        
        // 8. 生成现场照片区域
        yPosition = drawMediaSection(canvas, yPosition, mediaFiles)
        
        // 9. 生成签名区域
        drawSignatureSection(canvas, yPosition)
        
        pdfDocument.finishPage(page)
        
        // 保存文件
        val file = pdfManager.createPdfFile(project.name, constructionLog.date)
        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()
        
        return file
    }
    
    private fun drawTitle(canvas: Canvas, yPosition: Float): Float {
        val title = "淮工集团施工日志"
        canvas.drawText(title, 595f / 2, yPosition, titlePaint)
        return yPosition + 40f
    }
    
    private fun drawBasicInfoTable(canvas: Canvas, yPosition: Float, project: Project, log: ConstructionLog): Float {
        val leftMargin = 50f
        val tableWidth = 495f
        val rowHeight = 25f
        val dateFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
        
        // 表格标题
        canvas.drawText("基本信息", leftMargin, yPosition, titlePaint.apply { textAlign = Paint.Align.LEFT; textSize = 14f })
        var currentY = yPosition + 20f
        
        // 绘制表格边框
        canvas.drawRect(leftMargin, currentY, leftMargin + tableWidth, currentY + rowHeight * 4, tablePaint)
        
        // 项目名称
        canvas.drawLine(leftMargin, currentY + rowHeight, leftMargin + tableWidth, currentY + rowHeight, tablePaint)
        canvas.drawText("项目名称", leftMargin + 10f, currentY + 15f, textPaint)
        canvas.drawText(project.name, leftMargin + 100f, currentY + 15f, textPaint)
        
        // 施工日期
        currentY += rowHeight
        canvas.drawLine(leftMargin, currentY + rowHeight, leftMargin + tableWidth, currentY + rowHeight, tablePaint)
        canvas.drawText("施工日期", leftMargin + 10f, currentY + 15f, textPaint)
        canvas.drawText(dateFormat.format(log.date), leftMargin + 100f, currentY + 15f, textPaint)
        
        // 施工部位
        currentY += rowHeight
        canvas.drawLine(leftMargin, currentY + rowHeight, leftMargin + tableWidth, currentY + rowHeight, tablePaint)
        canvas.drawText("施工部位", leftMargin + 10f, currentY + 15f, textPaint)
        canvas.drawText(log.constructionSite, leftMargin + 100f, currentY + 15f, textPaint)
        
        // 负责人
        currentY += rowHeight
        canvas.drawText("负责人", leftMargin + 10f, currentY + 15f, textPaint)
        canvas.drawText(project.manager ?: "", leftMargin + 100f, currentY + 15f, textPaint)
        
        return currentY + 30f
    }
    
    private fun drawWeatherTable(canvas: Canvas, yPosition: Float, log: ConstructionLog): Float {
        val leftMargin = 50f
        val tableWidth = 495f
        val rowHeight = 25f
        
        // 表格标题
        canvas.drawText("天气信息", leftMargin, yPosition, titlePaint.apply { textAlign = Paint.Align.LEFT; textSize = 14f })
        var currentY = yPosition + 20f
        
        // 绘制表格边框
        canvas.drawRect(leftMargin, currentY, leftMargin + tableWidth, currentY + rowHeight * 3, tablePaint)
        
        // 天气状况
        canvas.drawLine(leftMargin, currentY + rowHeight, leftMargin + tableWidth, currentY + rowHeight, tablePaint)
        canvas.drawText("天气状况", leftMargin + 10f, currentY + 15f, textPaint)
        canvas.drawText(log.weatherCondition, leftMargin + 100f, currentY + 15f, textPaint)
        
        // 温度
        currentY += rowHeight
        canvas.drawLine(leftMargin, currentY + rowHeight, leftMargin + tableWidth, currentY + rowHeight, tablePaint)
        canvas.drawText("温度", leftMargin + 10f, currentY + 15f, textPaint)
        canvas.drawText(log.temperature, leftMargin + 100f, currentY + 15f, textPaint)
        
        // 风力风向
        currentY += rowHeight
        canvas.drawText("风力/风向", leftMargin + 10f, currentY + 15f, textPaint)
        canvas.drawText(log.wind, leftMargin + 100f, currentY + 15f, textPaint)
        
        return currentY + 30f
    }
    
    private fun drawConstructionContentTable(canvas: Canvas, yPosition: Float, log: ConstructionLog): Float {
        val leftMargin = 50f
        val tableWidth = 495f
        val rowHeight = 60f
        
        // 表格标题
        canvas.drawText("施工内容", leftMargin, yPosition, titlePaint.apply { textAlign = Paint.Align.LEFT; textSize = 14f })
        var currentY = yPosition + 20f
        
        // 绘制表格边框
        canvas.drawRect(leftMargin, currentY, leftMargin + tableWidth, currentY + rowHeight, tablePaint)
        
        // 主要内容
        canvas.drawText("主要内容", leftMargin + 10f, currentY + 15f, textPaint)
        drawWrappedText(canvas, log.mainContent, leftMargin + 10f, currentY + 35f, tableWidth - 20f, textPaint)
        
        return currentY + rowHeight + 30f
    }
    
    private fun drawPersonnelEquipmentTable(canvas: Canvas, yPosition: Float, log: ConstructionLog): Float {
        val leftMargin = 50f
        val tableWidth = 495f
        val rowHeight = 60f
        
        // 表格标题
        canvas.drawText("人员设备", leftMargin, yPosition, titlePaint.apply { textAlign = Paint.Align.LEFT; textSize = 14f })
        var currentY = yPosition + 20f
        
        // 绘制表格边框
        canvas.drawRect(leftMargin, currentY, leftMargin + tableWidth, currentY + rowHeight, tablePaint)
        
        // 人员设备信息
        canvas.drawText("人员设备", leftMargin + 10f, currentY + 15f, textPaint)
        drawWrappedText(canvas, log.personnelEquipment, leftMargin + 10f, currentY + 35f, tableWidth - 20f, textPaint)
        
        return currentY + rowHeight + 30f
    }
    
    private fun drawQualityManagementTable(canvas: Canvas, yPosition: Float, log: ConstructionLog): Float {
        val leftMargin = 50f
        val tableWidth = 495f
        val rowHeight = 60f
        
        // 表格标题
        canvas.drawText("质量管理", leftMargin, yPosition, titlePaint.apply { textAlign = Paint.Align.LEFT; textSize = 14f })
        var currentY = yPosition + 20f
        
        // 绘制表格边框
        canvas.drawRect(leftMargin, currentY, leftMargin + tableWidth, currentY + rowHeight, tablePaint)
        
        // 质量管理信息
        canvas.drawText("质量管理", leftMargin + 10f, currentY + 15f, textPaint)
        drawWrappedText(canvas, log.qualityManagement, leftMargin + 10f, currentY + 35f, tableWidth - 20f, textPaint)
        
        return currentY + rowHeight + 30f
    }
    
    private fun drawSafetyManagementTable(canvas: Canvas, yPosition: Float, log: ConstructionLog): Float {
        val leftMargin = 50f
        val tableWidth = 495f
        val rowHeight = 60f
        
        // 表格标题
        canvas.drawText("安全管理", leftMargin, yPosition, titlePaint.apply { textAlign = Paint.Align.LEFT; textSize = 14f })
        var currentY = yPosition + 20f
        
        // 绘制表格边框
        canvas.drawRect(leftMargin, currentY, leftMargin + tableWidth, currentY + rowHeight, tablePaint)
        
        // 安全管理信息
        canvas.drawText("安全管理", leftMargin + 10f, currentY + 15f, textPaint)
        drawWrappedText(canvas, log.safetyManagement, leftMargin + 10f, currentY + 35f, tableWidth - 20f, textPaint)
        
        return currentY + rowHeight + 30f
    }
    
    private fun drawMediaSection(canvas: Canvas, yPosition: Float, mediaFiles: List<MediaFile>): Float {
        val leftMargin = 50f
        val tableWidth = 495f
        
        // 表格标题
        canvas.drawText("现场照片", leftMargin, yPosition, titlePaint.apply { textAlign = Paint.Align.LEFT; textSize = 14f })
        var currentY = yPosition + 20f
        
        if (mediaFiles.isEmpty()) {
            canvas.drawText("暂无现场照片", leftMargin + 10f, currentY + 15f, textPaint)
            return currentY + 30f
        }
        
        // 按创建时间排序
        val sortedMediaFiles = mediaFiles.sortedBy { it.createdAt }
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        
        // 计算图片布局
        val imageWidth = 120f
        val imageHeight = 90f
        val imagesPerRow = 4
        val spacing = 10f
        
        var imageIndex = 0
        for (mediaFile in sortedMediaFiles) {
            if (mediaFile.fileType == MediaType.PHOTO) {
                val row = imageIndex / imagesPerRow
                val col = imageIndex % imagesPerRow
                
                val x = leftMargin + col * (imageWidth + spacing)
                val y = currentY + row * (imageHeight + 40f) // 40f for text
                
                // 绘制图片边框
                canvas.drawRect(x, y, x + imageWidth, y + imageHeight, tablePaint)
                
                // 尝试加载和绘制图片
                try {
                    val bitmap = BitmapFactory.decodeFile(mediaFile.filePath)
                    if (bitmap != null) {
                        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, imageWidth.toInt(), imageHeight.toInt(), true)
                        canvas.drawBitmap(scaledBitmap, x, y, imagePaint)
                        scaledBitmap.recycle()
                        bitmap.recycle()
                    }
                } catch (e: Exception) {
                    // 如果图片加载失败，绘制占位符
                    canvas.drawText("图片加载失败", x + 10f, y + imageHeight / 2, textPaint)
                }
                
                // 绘制图片信息
                val photoInfo = "${dateFormat.format(mediaFile.createdAt)} - ${mediaFile.description}"
                canvas.drawText(photoInfo, x, y + imageHeight + 15f, textPaint.apply { textSize = 10f })
                
                imageIndex++
            }
        }
        
        // 计算总高度
        val totalRows = (imageIndex + imagesPerRow - 1) / imagesPerRow
        val totalHeight = totalRows * (imageHeight + 40f)
        
        return currentY + totalHeight + 30f
    }
    
    private fun drawSignatureSection(canvas: Canvas, yPosition: Float) {
        val leftMargin = 50f
        val tableWidth = 495f
        val rowHeight = 40f
        
        // 表格标题
        canvas.drawText("签名确认", leftMargin, yPosition, titlePaint.apply { textAlign = Paint.Align.LEFT; textSize = 14f })
        var currentY = yPosition + 20f
        
        // 绘制表格边框
        canvas.drawRect(leftMargin, currentY, leftMargin + tableWidth, currentY + rowHeight * 3, tablePaint)
        
        // 分割线
        canvas.drawLine(leftMargin, currentY + rowHeight, leftMargin + tableWidth, currentY + rowHeight, tablePaint)
        canvas.drawLine(leftMargin, currentY + rowHeight * 2, leftMargin + tableWidth, currentY + rowHeight * 2, tablePaint)
        
        // 签名栏
        canvas.drawText("施工员签名：", leftMargin + 10f, currentY + 25f, textPaint)
        canvas.drawText("_________________", leftMargin + 100f, currentY + 25f, textPaint)
        
        currentY += rowHeight
        canvas.drawText("质量员签名：", leftMargin + 10f, currentY + 25f, textPaint)
        canvas.drawText("_________________", leftMargin + 100f, currentY + 25f, textPaint)
        
        currentY += rowHeight
        canvas.drawText("安全员签名：", leftMargin + 10f, currentY + 25f, textPaint)
        canvas.drawText("_________________", leftMargin + 100f, currentY + 25f, textPaint)
    }
    
    private fun drawWrappedText(canvas: Canvas, text: String, x: Float, y: Float, maxWidth: Float, paint: Paint) {
        val words = text.split(" ")
        var currentY = y
        var currentLine = ""
        
        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            val bounds = Rect()
            paint.getTextBounds(testLine, 0, testLine.length, bounds)
            
            if (bounds.width() > maxWidth && currentLine.isNotEmpty()) {
                canvas.drawText(currentLine, x, currentY, paint)
                currentLine = word
                currentY += paint.textSize + 2f
            } else {
                currentLine = testLine
            }
        }
        
        if (currentLine.isNotEmpty()) {
            canvas.drawText(currentLine, x, currentY, paint)
        }
    }
}