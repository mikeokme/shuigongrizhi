package com.example.shuigongrizhi.data.repository

import com.example.shuigongrizhi.core.Result
import com.example.shuigongrizhi.core.DatabaseException
import com.example.shuigongrizhi.data.dao.ProjectDao
import com.example.shuigongrizhi.data.entity.Project
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 项目数据仓库
 * 遵循Repository模式，提供数据访问的抽象层
 * 使用Result包装类进行错误处理
 */
@Singleton
class ProjectRepository @Inject constructor(
    private val projectDao: ProjectDao
) {
    
    /**
     * 获取所有项目
     * @return Flow<Result<List<Project>>>
     */
    fun getAllProjects(): Flow<Result<List<Project>>> {
        return projectDao.getAllProjects()
            .map { projects -> Result.Success(projects) as Result<List<Project>> }
            .catch { exception -> 
                emit(Result.Error(DatabaseException("获取项目列表失败: ${exception.message}")))
            }
    }

    /**
     * 根据ID获取项目
     * @param id 项目ID
     * @return Result<Project?>
     */
    suspend fun getProjectById(id: Long): Result<Project?> {
        return try {
            val project = projectDao.getProjectById(id)
            Result.Success(project)
        } catch (exception: Exception) {
            Result.Error(DatabaseException("获取项目详情失败: ${exception.message}"))
        }
    }

    /**
     * 插入新项目
     * @param project 项目实体
     * @return Result<Long> 返回插入的项目ID
     */
    suspend fun insertProject(project: Project): Result<Long> {
        return try {
            val projectId = projectDao.insertProject(project)
            Result.Success(projectId)
        } catch (exception: Exception) {
            Result.Error(DatabaseException("创建项目失败: ${exception.message}"))
        }
    }

    /**
     * 更新项目
     * @param project 项目实体
     * @return Result<Unit>
     */
    suspend fun updateProject(project: Project): Result<Unit> {
        return try {
            projectDao.updateProject(project)
            Result.Success(Unit)
        } catch (exception: Exception) {
            Result.Error(DatabaseException("更新项目失败: ${exception.message}"))
        }
    }

    /**
     * 删除项目
     * @param project 项目实体
     * @return Result<Unit>
     */
    suspend fun deleteProject(project: Project): Result<Unit> {
        return try {
            projectDao.deleteProject(project)
            Result.Success(Unit)
        } catch (exception: Exception) {
            Result.Error(DatabaseException("删除项目失败: ${exception.message}"))
        }
    }

    /**
     * 根据ID删除项目
     * @param id 项目ID
     * @return Result<Unit>
     */
    suspend fun deleteProjectById(id: Long): Result<Unit> {
        return try {
            projectDao.deleteProjectById(id)
            Result.Success(Unit)
        } catch (exception: Exception) {
            Result.Error(DatabaseException("删除项目失败: ${exception.message}"))
        }
    }
    
    /**
     * 检查项目名称是否已存在
     * @param name 项目名称
     * @param excludeId 排除的项目ID（用于更新时检查）
     * @return Result<Boolean>
     */
    suspend fun isProjectNameExists(name: String, excludeId: Long? = null): Result<Boolean> {
        return try {
            // 这里需要在DAO中添加相应的方法
            // val exists = projectDao.isProjectNameExists(name, excludeId)
            // Result.Success(exists)
            Result.Success(false) // 临时返回，需要实现DAO方法
        } catch (exception: Exception) {
            Result.Error(DatabaseException("检查项目名称失败: ${exception.message}"))
        }
    }
}