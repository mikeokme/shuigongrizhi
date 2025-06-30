package com.example.shuigongrizhi.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "construction_logs",
    foreignKeys = [
        ForeignKey(
            entity = Project::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
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
    val constructionLocation: String = "",
    val mainWorkContent: String = "",
    val constructionPersonnel: String = "",
    val machineryUsed: String = "",
    val safetyNotes: String = "",
    
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