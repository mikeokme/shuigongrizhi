package com.example.shuigongrizhi.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date
import androidx.room.TypeConverters
import com.example.shuigongrizhi.data.converter.Converters

@Entity(
    tableName = "construction_logs",
    foreignKeys = [
        ForeignKey(
            entity = Project::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["projectId"])]
)
@TypeConverters(Converters::class)
data class ConstructionLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long,
    val date: Date,
    
    // 天气信息
    val weatherCondition: String = "",
    val temperature: String = "",
    val wind: String = "",
    
    // 施工信息
    val constructionSite: String = "",
    val mainContent: String = "",
    val personnelEquipment: String = "",
    val qualityManagement: String = "",
    val safetyManagement: String = "",
    val mediaFiles: List<String> = emptyList(),
    
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

enum class WeatherCondition(val displayName: String) {
    SUNNY("晴"),
    CLOUDY("多云"),
    OVERCAST("阴"),
    RAINY("雨"),
    SNOWY("雪")
}