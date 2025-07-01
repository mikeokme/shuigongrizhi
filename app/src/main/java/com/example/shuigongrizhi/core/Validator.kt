package com.example.shuigongrizhi.core

import com.example.shuigongrizhi.data.entity.Project
import com.example.shuigongrizhi.data.entity.ConstructionLog
import java.util.Date
import java.util.regex.Pattern

/**
 * 数据验证器
 * 提供统一的数据验证逻辑
 */
object Validator {
    
    /**
     * 项目验证器
     */
    object Project {
        
        /**
         * 验证项目名称
         */
        fun validateName(name: String): ValidationResult {
            val trimmedName = name.trim()
            return when {
                trimmedName.isEmpty() -> 
                    ValidationResult.Error("项目名称不能为空")
                trimmedName.length < Constants.Validation.MIN_PROJECT_NAME_LENGTH -> 
                    ValidationResult.Error("项目名称至少需要${Constants.Validation.MIN_PROJECT_NAME_LENGTH}个字符")
                trimmedName.length > Constants.Validation.MAX_PROJECT_NAME_LENGTH -> 
                    ValidationResult.Error("项目名称不能超过${Constants.Validation.MAX_PROJECT_NAME_LENGTH}个字符")
                containsInvalidCharacters(trimmedName) -> 
                    ValidationResult.Error("项目名称包含无效字符")
                else -> ValidationResult.Success
            }
        }
        
        /**
         * 验证项目描述
         */
        fun validateDescription(description: String): ValidationResult {
            return when {
                description.length > Constants.Validation.MAX_DESCRIPTION_LENGTH -> 
                    ValidationResult.Error("项目描述不能超过${Constants.Validation.MAX_DESCRIPTION_LENGTH}个字符")
                else -> ValidationResult.Success
            }
        }
        
        /**
         * 验证项目管理者
         */
        fun validateManager(manager: String): ValidationResult {
            val trimmedManager = manager.trim()
            return when {
                trimmedManager.isEmpty() -> 
                    ValidationResult.Error("项目管理者不能为空")
                trimmedManager.length > 50 -> 
                    ValidationResult.Error("项目管理者名称不能超过50个字符")
                else -> ValidationResult.Success
            }
        }
        
        /**
         * 验证项目日期
         */
        fun validateDates(startDate: Date, endDate: Date?): ValidationResult {
            return when {
                endDate != null && endDate.before(startDate) -> 
                    ValidationResult.Error("结束日期不能早于开始日期")
                startDate.after(Date()) -> 
                    ValidationResult.Error("开始日期不能晚于当前日期")
                else -> ValidationResult.Success
            }
        }
        
        /**
         * 验证完整项目信息
         */
        fun validateProject(project: com.example.shuigongrizhi.data.entity.Project): ValidationResult {
            validateName(project.name).let { if (it.isError) return it }
            project.description?.let { validateDescription(it).let { result -> if (result.isError) return result } }
            project.manager?.let { validateManager(it).let { result -> if (result.isError) return result } }
            validateDates(project.startDate, project.endDate).let { if (it.isError) return it }
            
            return ValidationResult.Success
        }
        
        /**
         * 检查是否包含无效字符
         */
        private fun containsInvalidCharacters(text: String): Boolean {
            // 定义不允许的字符模式
            val invalidPattern = Pattern.compile("[<>:\"/\\|?*]")
            return invalidPattern.matcher(text).find()
        }
    }
    
    /**
     * 施工日志验证器
     */
    object ConstructionLog {
        
        /**
         * 验证日志内容
         */
        fun validateContent(content: String): ValidationResult {
            val trimmedContent = content.trim()
            return when {
                trimmedContent.isEmpty() -> 
                    ValidationResult.Error("施工内容不能为空")
                trimmedContent.length > Constants.Validation.MAX_CONTENT_LENGTH -> 
                    ValidationResult.Error("施工内容不能超过${Constants.Validation.MAX_CONTENT_LENGTH}个字符")
                else -> ValidationResult.Success
            }
        }
        
        /**
         * 验证天气信息
         */
        fun validateWeather(weather: String): ValidationResult {
            val trimmedWeather = weather.trim()
            return when {
                trimmedWeather.isEmpty() -> 
                    ValidationResult.Error("天气信息不能为空")
                trimmedWeather.length > 50 -> 
                    ValidationResult.Error("天气信息不能超过50个字符")
                else -> ValidationResult.Success
            }
        }
        
        /**
         * 验证温度信息
         */
        fun validateTemperature(temperature: String): ValidationResult {
            val trimmedTemp = temperature.trim()
            return when {
                trimmedTemp.isEmpty() -> 
                    ValidationResult.Error("温度信息不能为空")
                trimmedTemp.length > 20 -> 
                    ValidationResult.Error("温度信息不能超过20个字符")
                else -> ValidationResult.Success
            }
        }
        
        /**
         * 验证风力信息
         */
        fun validateWind(wind: String): ValidationResult {
            val trimmedWind = wind.trim()
            return when {
                trimmedWind.isEmpty() -> 
                    ValidationResult.Error("风力信息不能为空")
                trimmedWind.length > 20 -> 
                    ValidationResult.Error("风力信息不能超过20个字符")
                else -> ValidationResult.Success
            }
        }
        
        /**
         * 验证施工人数
         */
        fun validateWorkerCount(count: Int): ValidationResult {
            return when {
                count < 0 -> 
                    ValidationResult.Error("施工人数不能为负数")
                count > 1000 -> 
                    ValidationResult.Error("施工人数不能超过1000人")
                else -> ValidationResult.Success
            }
        }
        
        /**
         * 验证完整施工日志信息
         */
        fun validateConstructionLog(log: com.example.shuigongrizhi.data.entity.ConstructionLog): ValidationResult {
            validateContent(log.mainContent).let { if (it.isError) return it }
            validateWeather(log.weatherCondition).let { if (it.isError) return it }
            validateTemperature(log.temperature).let { if (it.isError) return it }
            validateWind(log.wind).let { if (it.isError) return it }
            
            return ValidationResult.Success
        }
    }
    
    /**
     * 文件验证器
     */
    object File {
        
        /**
         * 验证文件名
         */
        fun validateFileName(fileName: String): ValidationResult {
            val trimmedName = fileName.trim()
            return when {
                trimmedName.isEmpty() -> 
                    ValidationResult.Error("文件名不能为空")
                trimmedName.length > 255 -> 
                    ValidationResult.Error("文件名不能超过255个字符")
                containsInvalidFileNameCharacters(trimmedName) -> 
                    ValidationResult.Error("文件名包含无效字符")
                else -> ValidationResult.Success
            }
        }
        
        /**
         * 验证文件路径
         */
        fun validateFilePath(filePath: String): ValidationResult {
            val trimmedPath = filePath.trim()
            return when {
                trimmedPath.isEmpty() -> 
                    ValidationResult.Error("文件路径不能为空")
                trimmedPath.length > 260 -> 
                    ValidationResult.Error("文件路径不能超过260个字符")
                else -> ValidationResult.Success
            }
        }
        
        /**
         * 验证文件大小
         */
        fun validateFileSize(sizeInBytes: Long, maxSizeInMB: Long = 100): ValidationResult {
            val maxSizeInBytes = maxSizeInMB * 1024 * 1024
            return when {
                sizeInBytes < 0 -> 
                    ValidationResult.Error("文件大小不能为负数")
                sizeInBytes > maxSizeInBytes -> 
                    ValidationResult.Error("文件大小不能超过${maxSizeInMB}MB")
                else -> ValidationResult.Success
            }
        }
        
        /**
         * 验证图片文件扩展名
         */
        fun validateImageExtension(fileName: String): ValidationResult {
            val allowedExtensions = listOf(".jpg", ".jpeg", ".png", ".bmp", ".webp")
            val extension = fileName.substringAfterLast('.', "").lowercase()
            
            return if (allowedExtensions.any { it.endsWith(extension) }) {
                ValidationResult.Success
            } else {
                ValidationResult.Error("不支持的图片格式，支持的格式: ${allowedExtensions.joinToString()}")
            }
        }
        
        /**
         * 验证视频文件扩展名
         */
        fun validateVideoExtension(fileName: String): ValidationResult {
            val allowedExtensions = listOf(".mp4", ".avi", ".mov", ".mkv", ".3gp")
            val extension = fileName.substringAfterLast('.', "").lowercase()
            
            return if (allowedExtensions.any { it.endsWith(extension) }) {
                ValidationResult.Success
            } else {
                ValidationResult.Error("不支持的视频格式，支持的格式: ${allowedExtensions.joinToString()}")
            }
        }
        
        /**
         * 检查文件名是否包含无效字符
         */
        private fun containsInvalidFileNameCharacters(fileName: String): Boolean {
            val invalidPattern = Pattern.compile("[<>:\"/\\|?*]")
            return invalidPattern.matcher(fileName).find()
        }
    }
    
    /**
     * 通用验证器
     */
    object Common {
        
        /**
         * 验证邮箱格式
         */
        fun validateEmail(email: String): ValidationResult {
            val emailPattern = Pattern.compile(
                "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
            )
            
            return when {
                email.trim().isEmpty() -> 
                    ValidationResult.Error("邮箱地址不能为空")
                !emailPattern.matcher(email).matches() -> 
                    ValidationResult.Error("邮箱格式不正确")
                else -> ValidationResult.Success
            }
        }
        
        /**
         * 验证手机号格式
         */
        fun validatePhoneNumber(phoneNumber: String): ValidationResult {
            val phonePattern = Pattern.compile("^1[3-9]\\d{9}$")
            
            return when {
                phoneNumber.trim().isEmpty() -> 
                    ValidationResult.Error("手机号不能为空")
                !phonePattern.matcher(phoneNumber).matches() -> 
                    ValidationResult.Error("手机号格式不正确")
                else -> ValidationResult.Success
            }
        }
        
        /**
         * 验证URL格式
         */
        fun validateUrl(url: String): ValidationResult {
            val urlPattern = Pattern.compile(
                "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$",
                Pattern.CASE_INSENSITIVE
            )
            
            return when {
                url.trim().isEmpty() -> 
                    ValidationResult.Error("URL不能为空")
                !urlPattern.matcher(url).matches() -> 
                    ValidationResult.Error("URL格式不正确")
                else -> ValidationResult.Success
            }
        }
        
        /**
         * 验证非空字符串
         */
        fun validateNotEmpty(value: String, fieldName: String): ValidationResult {
            return if (value.trim().isEmpty()) {
                ValidationResult.Error("${fieldName}不能为空")
            } else {
                ValidationResult.Success
            }
        }
        
        /**
         * 验证字符串长度
         */
        fun validateLength(
            value: String,
            fieldName: String,
            minLength: Int = 0,
            maxLength: Int = Int.MAX_VALUE
        ): ValidationResult {
            return when {
                value.length < minLength -> 
                    ValidationResult.Error("${fieldName}长度不能少于${minLength}个字符")
                value.length > maxLength -> 
                    ValidationResult.Error("${fieldName}长度不能超过${maxLength}个字符")
                else -> ValidationResult.Success
            }
        }
    }
}