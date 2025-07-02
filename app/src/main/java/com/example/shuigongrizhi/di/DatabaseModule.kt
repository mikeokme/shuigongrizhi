package com.example.shuigongrizhi.di

import android.content.Context
import androidx.room.Room
import com.example.shuigongrizhi.data.database.AppDatabase
import com.example.shuigongrizhi.data.dao.ConstructionLogDao
import com.example.shuigongrizhi.data.dao.MediaFileDao
import com.example.shuigongrizhi.data.dao.ProjectDao
import com.example.shuigongrizhi.data.repository.ConstructionLogRepository
import com.example.shuigongrizhi.data.repository.MediaFileRepository
import com.example.shuigongrizhi.data.repository.ProjectRepository
import com.example.shuigongrizhi.core.AppConfig
// import dagger.Module
// import dagger.Provides
// import dagger.hilt.InstallIn
// import dagger.hilt.android.qualifiers.ApplicationContext
// import dagger.hilt.components.SingletonComponent
// import javax.inject.Singleton

// @Module
// @InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    // @Provides
    // @Singleton
    fun provideAppDatabase(
        /* @ApplicationContext */ context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .addMigrations(AppDatabase.MIGRATION_3_4)
            .fallbackToDestructiveMigration()
            .build()
    }
    
    // @Provides
    fun provideProjectDao(database: AppDatabase): ProjectDao {
        return database.projectDao()
    }
    
    // @Provides
    fun provideConstructionLogDao(database: AppDatabase): ConstructionLogDao {
        return database.constructionLogDao()
    }
    
    // @Provides
    fun provideMediaFileDao(database: AppDatabase): MediaFileDao {
        return database.mediaFileDao()
    }
    
    // @Provides
    // @Singleton
    fun provideProjectRepository(
        projectDao: ProjectDao
    ): ProjectRepository {
        return ProjectRepository(projectDao)
    }
    
    // @Provides
    // @Singleton
    fun provideConstructionLogRepository(
        constructionLogDao: ConstructionLogDao
    ): ConstructionLogRepository {
        return ConstructionLogRepository(constructionLogDao)
    }
    
    // @Provides
    // @Singleton
    fun provideMediaFileRepository(
        mediaFileDao: MediaFileDao
    ): MediaFileRepository {
        return MediaFileRepository(mediaFileDao)
    }
    
    // @Provides
    // @Singleton
    fun provideAppConfig(
        /* @ApplicationContext */ context: Context
    ): AppConfig {
        return AppConfig(context)
    }
}