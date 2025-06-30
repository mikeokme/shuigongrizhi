package com.example.shuigongrizhi.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.shuigongrizhi.data.converter.Converters
import java.util.Date

@Entity(
    tableName = "media_files",
    foreignKeys = [
        ForeignKey(
            entity = ConstructionLog::class,
            parentColumns = ["id"],
            childColumns = ["logId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["logId"])]
)
@TypeConverters(Converters::class)
data class MediaFile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val logId: Long,
    val filePath: String,
    val fileName: String,
    val fileType: MediaType,
    val fileSize: Long = 0,
    val description: String = "",
    val createdAt: Date = Date()
)

enum class MediaType {
    PHOTO,
    VIDEO
}