package com.example.shuigongrizhi.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.example.shuigongrizhi.core.Constants
import com.example.shuigongrizhi.data.dao.ConstructionLogDao
import com.example.shuigongrizhi.data.dao.MediaFileDao
import com.example.shuigongrizhi.data.dao.ProjectDao
import com.example.shuigongrizhi.data.dao.LocationRecordDao
import com.example.shuigongrizhi.data.entity.ConstructionLog
import com.example.shuigongrizhi.data.entity.MediaFile
import com.example.shuigongrizhi.data.entity.Project
import com.example.shuigongrizhi.data.entity.LocationRecord
import com.example.shuigongrizhi.data.converter.Converters

/**
 * 应用数据库类
 * 遵循Room数据库最佳实践，包括版本管理和迁移策略
 */
@Database(
    entities = [
        Project::class, 
        ConstructionLog::class, 
        MediaFile::class, 
        LocationRecord::class
    ],
    version = Constants.Database.VERSION,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun projectDao(): ProjectDao
    abstract fun constructionLogDao(): ConstructionLogDao
    abstract fun mediaFileDao(): MediaFileDao
    abstract fun locationRecordDao(): LocationRecordDao

    companion object {
        const val DATABASE_NAME = Constants.Database.NAME
        
        /**
         * 数据库迁移：从版本3到版本4
         * 添加索引以提高查询性能
         */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 为经常查询的字段添加索引
                database.execSQL("CREATE INDEX IF NOT EXISTS index_construction_logs_project_id ON construction_logs(projectId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_construction_logs_date ON construction_logs(date)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_media_files_log_id ON media_files(logId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_location_records_log_id ON location_records(logId)")
            }
        }
        
        /**
         * 获取数据库实例的工厂方法
         * 注意：在使用Hilt时，这个方法不会被直接使用
         * 保留是为了向后兼容和测试目的
         */
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                .addMigrations(MIGRATION_3_4)
                .fallbackToDestructiveMigration() // 开发阶段允许重建数据库
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}