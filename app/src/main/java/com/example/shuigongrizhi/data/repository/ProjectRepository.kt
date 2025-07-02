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

    private suspend fun <T> safeDatabaseCall(errorMessage: String, call: suspend () -> T): Result<T> {
        return try {
            Result.Success(call())
        } catch (e: Exception) {
            Result.Error(DatabaseException.QueryFailed("$errorMessage: ${e.message}"))
        }
    }
    
    /**
     * 获取所有项目
     * @return Flow<Result<List<Project>>>
     */
    fun getAllProjects(): Flow<Result<List<Project>>> {
        return projectDao.getAllProjects()
            .map<List<Project>, Result<List<Project>>> { Result.Success(it) }
            .catch { e -> emit(Result.Error(DatabaseException.QueryFailed(e.message ?: "查询所有项目失败"))) }
    }

    /**
     * 根据ID获取项目
     * @param id 项目ID
     * @return Result<Project?>
     */
    suspend fun getProjectById(id: Long): Result<Project?> {
        return safeDatabaseCall("根据ID查询项目失败") { projectDao.getProjectById(id) }
    }

    /**
     * 插入新项目
     * @param project 项目实体
     * @return Result<Long> 返回插入的项目ID
     */
    suspend fun insertProject(project: Project): Result<Long> {
        return safeDatabaseCall("插入项目失败") { projectDao.insertProject(project) }
    }

    /**
     * 更新项目
     * @param project 项目实体
     * @return Result<Unit>
     */
    suspend fun updateProject(project: Project): Result<Unit> {
        return safeDatabaseCall("更新项目失败") { 
            projectDao.updateProject(project)
            Result.Success(Unit)
        }
    }

    /**
     * 删除项目
     * @param project 项目实体
     * @return Result<Unit>
     */
    suspend fun deleteProject(project: Project): Result<Unit> {
        return safeDatabaseCall("删除项目失败") { 
            projectDao.deleteProject(project)
            Result.Success(Unit)
        }
    }

    /**
     * 根据ID删除项目
     * @param id 项目ID
     * @return Result<Unit>
     */
    suspend fun deleteProjectById(id: Long): Result<Unit> {
        return safeDatabaseCall("根据ID删除项目失败") { 
            projectDao.deleteProjectById(id)
            Result.Success(Unit)
        }
    }
    
    /**
     * 检查项目名称是否已存在
     * @param name 项目名称
     * @param excludeId 排除的项目ID（用于更新时检查）
     * @return Result<Boolean>
     */
    suspend fun isProjectNameExists(name: String, excludeId: Long? = null): Result<Boolean> {
        return safeDatabaseCall("检查项目名称是否存在失败") {
            val count = if (excludeId == null) {
                projectDao.countProjectByName(name)
            } else {
                projectDao.countProjectByNameAndIdNot(name, excludeId)
            }
            count > 0
        }
    }
}