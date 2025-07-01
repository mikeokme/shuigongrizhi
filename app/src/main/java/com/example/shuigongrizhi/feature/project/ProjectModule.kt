package com.example.shuigongrizhi.feature.project

import com.example.shuigongrizhi.data.repository.ProjectRepository
import com.example.shuigongrizhi.data.repository.ConstructionLogRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

/**
 * 项目功能模块的依赖注入配置
 * 遵循模块化架构，将相关功能组织在一起
 */
@Module
@InstallIn(ViewModelComponent::class)
object ProjectModule {
    
    // 如果需要特定于项目功能的依赖，可以在这里提供
    // 目前ProjectRepository已在DatabaseModule中提供
    
    /**
     * 提供项目用例（Use Case）
     * 这里可以添加业务逻辑层的用例类
     */
    @Provides
    @ViewModelScoped
    fun provideProjectUseCase(
        projectRepository: ProjectRepository,
        constructionLogRepository: ConstructionLogRepository
    ): ProjectUseCase {
        return ProjectUseCase(projectRepository, constructionLogRepository)
    }
}

/**
 * 项目相关的业务用例类
 * 封装复杂的业务逻辑，保持ViewModel的简洁
 */
class ProjectUseCase(
    private val projectRepository: ProjectRepository,
    private val constructionLogRepository: ConstructionLogRepository
) {
    // 这里可以添加复杂的业务逻辑方法
    // 例如：创建项目并初始化相关数据、项目数据验证等
}