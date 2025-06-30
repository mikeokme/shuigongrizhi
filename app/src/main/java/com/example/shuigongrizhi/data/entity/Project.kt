package com.example.shuigongrizhi.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val number: String,
    val constructionUnit: String = "",
    val supervisionUnit: String = "",
    val location: String = "",
    val projectType: String = "水库", // 默认为水库类型
    val startDate: Date? = null,
    val plannedCompletionDate: Date? = null,
    val status: ProjectStatus = ProjectStatus.ONGOING,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

enum class ProjectStatus {
    ONGOING,
    COMPLETED
}