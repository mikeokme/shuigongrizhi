package com.example.shuigongrizhi.data.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date
import com.example.shuigongrizhi.data.entity.ProjectType

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return Gson().toJson(value ?: emptyList<String>())
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return try {
            Gson().fromJson(value, listType) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromProjectType(projectType: ProjectType): String {
        return projectType.name
    }

    @TypeConverter
    fun toProjectType(projectType: String): ProjectType {
        return try {
            ProjectType.valueOf(projectType)
        } catch (e: Exception) {
            ProjectType.其他项目
        }
    }
}