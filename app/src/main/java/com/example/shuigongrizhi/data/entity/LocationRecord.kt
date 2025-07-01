package com.example.shuigongrizhi.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "location_records")
data class LocationRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val latitude: Double,
    val longitude: Double,
    val address: String?,
    val timestamp: Date = Date(),
    val provider: String = "Google", // "Google"|"Gaode"|"Baidu"
    val logId: Long? = null // 关联日志ID
) 