package com.example.shuigongrizhi.utils

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import com.example.shuigongrizhi.data.entity.ConstructionLog
import com.example.shuigongrizhi.data.entity.MediaFile
import com.example.shuigongrizhi.data.entity.MediaType
import com.example.shuigongrizhi.data.entity.Project
import com.itextpdf.io.font.constants.StandardFonts
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.property.TextAlignment
import com.itextpdf.layout.property.UnitValue
import java.io.File
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
        val file = pdfManager.createPdfFile(project.name, constructionLog.date)
        val writer = PdfWriter(file)
        val pdfDoc = PdfDocument(writer)
        val document = Document(pdfDoc, PageSize.A4)
        document.setMargins(36f, 36f, 36f, 36f)

        val titleFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)
        val bodyFont = PdfFontFactory.createFont(StandardFonts.HELVETICA)

        // 1. 生成标题
        document.add(Paragraph("淮工集团施工日志").setFont(titleFont).setFontSize(18f).setTextAlignment(TextAlignment.CENTER))

        // 2. 生成基本信息表格
        addBasicInfoTable(document, project, constructionLog, bodyFont)

        // 3. 生成天气信息表格
        addWeatherTable(document, constructionLog, bodyFont)

        // 4. 生成施工内容等表格
        addContentTable(document, "施工内容", constructionLog.mainContent, bodyFont)
        addContentTable(document, "人员设备", constructionLog.personnelEquipment, bodyFont)
        addContentTable(document, "质量管理", constructionLog.qualityManagement, bodyFont)
        addContentTable(document, "安全管理", constructionLog.safetyManagement, bodyFont)

        // 8. 生成现场照片区域
        addMediaSection(document, mediaFiles, bodyFont)

        // 9. 生成签名区域
        addSignatureSection(document, bodyFont)

        document.close()
        return file
    }
    

    
    private fun addBasicInfoTable(document: Document, project: Project, log: ConstructionLog, font: com.itextpdf.kernel.font.PdfFont) {
        val table = Table(UnitValue.createPercentArray(floatArrayOf(1f, 4f))).useAllAvailableWidth()
        val dateFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())

        table.addCell(createCell("项目名称", font))
        table.addCell(createCell(project.name, font))
        table.addCell(createCell("施工日期", font))
        table.addCell(createCell(dateFormat.format(log.date), font))
        table.addCell(createCell("施工部位", font))
        table.addCell(createCell(log.constructionSite, font))
        table.addCell(createCell("负责人", font))
        table.addCell(createCell(project.manager ?: "", font))

        document.add(table.setMarginBottom(20f))
    }
    
    private fun addWeatherTable(document: Document, log: ConstructionLog, font: com.itextpdf.kernel.font.PdfFont) {
        val table = Table(UnitValue.createPercentArray(floatArrayOf(1f, 4f))).useAllAvailableWidth()

        table.addCell(createCell("天气状况", font))
        table.addCell(createCell(log.weatherCondition, font))
        table.addCell(createCell("温度", font))
        table.addCell(createCell(log.temperature, font))
        table.addCell(createCell("风力/风向", font))
        table.addCell(createCell(log.wind, font))

        document.add(table.setMarginBottom(20f))
    }
    
    private fun addContentTable(document: Document, title: String, content: String, font: com.itextpdf.kernel.font.PdfFont) {
        val table = Table(UnitValue.createPercentArray(floatArrayOf(1f))).useAllAvailableWidth()
        table.addHeaderCell(createCell(title, font, TextAlignment.LEFT).setBackgroundColor(ColorConstants.LIGHT_GRAY))
        table.addCell(createCell(content, font))
        document.add(table.setMarginBottom(20f))
    }
    

    

    

    
    private fun addMediaSection(document: Document, mediaFiles: List<MediaFile>, font: com.itextpdf.kernel.font.PdfFont) {
        document.add(Paragraph("现场照片").setFont(font).setBold().setMarginBottom(10f))

        if (mediaFiles.any { it.fileType == MediaType.PHOTO }) {
            val table = Table(UnitValue.createPercentArray(floatArrayOf(1f, 1f, 1f, 1f))).useAllAvailableWidth()
            val sortedPhotos = mediaFiles.filter { it.fileType == MediaType.PHOTO }.sortedBy { it.createdAt }
            val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

            for (photo in sortedPhotos) {
                try {
                    val imageData = ImageDataFactory.create(photo.filePath)
                    val image = Image(imageData).setAutoScale(true)
                    val cell = Cell().add(image)
                    val description = "${dateFormat.format(photo.createdAt)} - ${photo.description}"
                    cell.add(Paragraph(description).setFont(font).setFontSize(8f))
                    table.addCell(cell)
                } catch (e: Exception) {
                    // Handle image loading failure
                    table.addCell(createCell("图片加载失败", font))
                }
            }
            document.add(table.setMarginBottom(20f))
        } else {
            document.add(Paragraph("暂无现场照片").setFont(font).setMarginBottom(20f))
        }
    }
    
    private fun addSignatureSection(document: Document, font: com.itextpdf.kernel.font.PdfFont) {
        val table = Table(UnitValue.createPercentArray(floatArrayOf(1f, 4f))).useAllAvailableWidth()

        table.addCell(createCell("施工员签名：", font))
        table.addCell(createCell("_________________", font))
        table.addCell(createCell("质量员签名：", font))
        table.addCell(createCell("_________________", font))
        table.addCell(createCell("安全员签名：", font))
        table.addCell(createCell("_________________", font))

        document.add(table)
    }
    
    private fun createCell(content: String, font: com.itextpdf.kernel.font.PdfFont, alignment: TextAlignment = TextAlignment.LEFT): Cell {
        return Cell().add(Paragraph(content).setFont(font).setTextAlignment(alignment)).setPadding(5f)
    }
}