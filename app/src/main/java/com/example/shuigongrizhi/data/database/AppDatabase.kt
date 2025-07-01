package com.example.shuigongrizhi.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.example.shuigongrizhi.data.converter.Converters
import com.example.shuigongrizhi.data.dao.ConstructionLogDao
import com.example.shuigongrizhi.data.dao.MediaFileDao
import com.example.shuigongrizhi.data.dao.ProjectDao
import com.example.shuigongrizhi.data.dao.LocationRecordDao
import com.example.shuigongrizhi.data.entity.ConstructionLog
import com.example.shuigongrizhi.data.entity.MediaFile
import com.example.shuigongrizhi.data.entity.Project
import com.example.shuigongrizhi.data.entity.LocationRecord

@Database(
    entities = [Project::class, ConstructionLog::class, MediaFile::class, LocationRecord::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun constructionLogDao(): ConstructionLogDao
    abstract fun mediaFileDao(): MediaFileDao
    abstract fun locationRecordDao(): LocationRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "construction_log_database"
                )
                .fallbackToDestructiveMigration() // 开发阶段允许重建数据库
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}