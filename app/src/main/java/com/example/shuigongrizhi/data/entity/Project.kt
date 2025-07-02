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

enum class ProjectType(val displayName: String) {
    水利("水利工程"),
    市政("市政工程"),
    养护("养护工程"),
    合作项目("合作项目"),
    其他项目("其他项目")
}