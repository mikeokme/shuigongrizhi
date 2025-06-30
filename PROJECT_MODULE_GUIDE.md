# 项目信息模块保存和选取功能使用指南

## 功能概述

本次更新实现了完整的项目信息模块保存和选取功能，包括：

1. **项目信息保存功能增强**
2. **项目选择器组件**
3. **项目选择页面**
4. **用户偏好设置管理**
5. **错误处理和用户反馈优化**

## 新增文件

### 1. ProjectSelectionScreen.kt
- 位置：`ui/screen/ProjectSelectionScreen.kt`
- 功能：提供项目选择界面，支持项目列表展示和选择
- 特性：
  - 显示项目基本信息（名称、类型、负责人、日期范围、描述）
  - 支持选中状态显示
  - 空状态处理
  - 加载状态和错误处理

### 2. ProjectSelectionViewModel.kt
- 位置：`ui/viewmodel/ProjectSelectionViewModel.kt`
- 功能：管理项目选择状态和数据
- 特性：
  - 项目列表加载
  - 选择状态管理
  - 错误处理
  - 数据刷新

### 3. ProjectSelector.kt
- 位置：`ui/components/ProjectSelector.kt`
- 功能：可复用的项目选择器组件
- 提供两种样式：
  - `ProjectSelector`：完整信息显示
  - `CompactProjectSelector`：紧凑型显示

### 4. ProjectPreferences.kt
- 位置：`data/preferences/ProjectPreferences.kt`
- 功能：管理用户项目选择偏好
- 特性：
  - 保存最后选择的项目
  - 获取用户偏好
  - 清除偏好设置

## 功能增强

### 1. ProjectFormViewModel.kt 增强
- 改进的错误处理和用户反馈
- 数据验证增强
- 更好的保存状态管理
- 支持数据库约束错误的友好提示

### 2. ProjectFormScreen.kt 增强
- 添加Snackbar错误和成功提示
- 改进的用户交互体验
- 更好的加载状态显示

### 3. Navigation.kt 更新
- 添加项目选择页面路由
- 支持页面间数据传递
- 导航扩展函数

## 使用方法

### 1. 项目信息保存

```kotlin
// 在ProjectFormScreen中
ProjectFormScreen(
    viewModel = viewModel,
    projectId = projectId, // null表示新建，非null表示编辑
    onNavigateBack = { navController.popBackStack() }
)
```

### 2. 项目选择页面

```kotlin
// 导航到项目选择页面
navController.navigate(NavigationRoutes.PROJECT_SELECTION)

// 在ProjectSelectionScreen中处理选择结果
ProjectSelectionScreen(
    viewModel = viewModel,
    selectedProjectId = currentProjectId,
    onProjectSelected = { project ->
        // 处理项目选择
        navController.previousBackStackEntry?.savedStateHandle?.set("selected_project", project)
        navController.popBackStack()
    },
    onNavigateBack = { navController.popBackStack() }
)
```

### 3. 项目选择器组件使用

```kotlin
// 完整样式
ProjectSelector(
    selectedProject = selectedProject,
    onProjectClick = { 
        // 导航到项目选择页面
        navController.navigate(NavigationRoutes.PROJECT_SELECTION)
    },
    label = "选择项目"
)

// 紧凑样式
CompactProjectSelector(
    selectedProject = selectedProject,
    onProjectClick = { 
        // 导航到项目选择页面
    }
)
```

### 4. 获取选择结果

```kotlin
// 在目标页面中获取选择的项目
val selectedProject = navController.currentBackStackEntry
    ?.savedStateHandle
    ?.get<Project>("selected_project")
```

### 5. 用户偏好管理

```kotlin
// 注入ProjectPreferences
@Inject
latevar projectPreferences: ProjectPreferences

// 保存用户选择
projectPreferences.saveLastSelectedProject(project.id, project.name)

// 获取上次选择
val lastProjectId = projectPreferences.getLastSelectedProjectId()
val lastProjectName = projectPreferences.getLastSelectedProjectName()

// 检查是否有历史选择
if (projectPreferences.hasLastSelectedProject()) {
    // 加载上次选择的项目
}
```

## 错误处理

### 1. 保存错误处理
- 数据库约束错误（如重复项目名称）
- 网络连接错误
- 数据验证错误
- 未知错误

### 2. 用户反馈
- Snackbar提示保存成功/失败
- 加载状态指示器
- 表单验证错误提示
- 空状态页面

## 技术特性

### 1. 架构模式
- MVVM架构
- Repository模式
- Dependency Injection (Hilt)
- StateFlow状态管理

### 2. UI组件
- Material Design 3
- Jetpack Compose
- 响应式设计
- 可复用组件

### 3. 数据持久化
- Room数据库
- SharedPreferences用户偏好
- 数据验证和约束

## 下一步扩展

1. **项目搜索功能**：在项目选择页面添加搜索框
2. **项目分类筛选**：按项目类型筛选
3. **最近使用项目**：显示最近使用的项目列表
4. **项目收藏功能**：允许用户收藏常用项目
5. **批量操作**：支持批量删除、导出等操作

## 故障排除

### 常见问题

1. **项目保存失败**
   - 检查必填字段是否填写完整
   - 确认项目名称是否重复
   - 检查数据库连接状态

2. **项目选择不生效**
   - 确认导航参数传递正确
   - 检查savedStateHandle的使用
   - 验证ViewModel注入是否正确

3. **界面显示异常**
   - 检查Compose版本兼容性
   - 确认Material Design 3主题配置
   - 验证字符串资源是否存在

通过以上功能的实现，项目信息模块现在具备了完整的保存和选取功能，为用户提供了便捷的项目管理体验。