package com.example.shuigongrizhi.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.Date
import com.example.shuigongrizhi.data.converter.Converters

@Entity(tableName = "projects")
@TypeConverters(Converters::class)
data class Project(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: ProjectType,
    val description: String? = null,
    val startDate: Date,
    val endDate: Date? = null,
    val manager: String? = null
)

enum class ProjectType {
    水利, 市政, 养护, 合作项目, 其他项目
}