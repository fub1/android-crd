# Architecture

## 项目概述

**该项目完全使用Kotlin和Jetpack Compose构建，现代Android架构、模块化设计和CI/CD流程。

### 技术栈
- **语言**: Kotlin 2.1.10
- **UI框架**: Jetpack Compose (BOM 2025.02.00)
- **架构**: MVI + Clean Architecture
- **依赖注入**: Hilt
- **网络**: Retrofit + OkHttp
- **数据库**: Room
- **异步处理**: Kotlin Coroutines + Flow
- **构建工具**: Gradle 8.9.0 + AGP
- **测试**: JUnit, Espresso, Roborazzi截图测试

## 1. 项目架构设计

### 1.1 官方架构指导原则

遵循 [Android官方架构指导](https://developer.android.com/jetpack/guide)，确保：
- 架构简单易懂，避免过度实验性
- 支持多开发者协作
- 便于本地和仪器化测试
- 最小化构建时间
- 遵循关注点分离原则

### 1.2 三层架构模式

基于Google推荐的现代Android架构：

```
UI Layer (Compose + ViewModels + UI State)
    ↓
Domain Layer (Use Cases + Business Logic)
    ↓  
Data Layer (Repositories + Data Sources)
```

**核心设计原则**：
- **单向数据流 (UDF)**: 采用响应式编程模型
- **事件向下流动**: 用户交互事件从UI层向下传递
- **数据向上流动**: 数据从数据层向上流动到UI层
- **状态驱动**: UI状态完全由数据层驱动，数据层是唯一的真实数据源

### 1.3 MVI架构模式

采用**Model-View-Intent (MVI)**架构模式：

```
Intent (用户交互) → ViewModel → State (UI状态) → View (Compose UI)
```

**MVI核心组件**：
- **Model**: 表示应用程序状态的不可变数据结构
- **View**: 响应状态变化的UI组件(Jetpack Compose)
- **Intent**: 用户交互事件和系统事件
- **State**: 密封类hierarchy建模的UI状态

**MVI优势**：
- 单向数据流，状态可预测
- 易于测试和调试
- 状态恢复简单
- 并发安全

### 1.4 模块化设计策略

基于Google Now in Android项目的模块化最佳实践：

#### 模块化原则
- **低耦合**: 模块间尽可能独立，变更影响最小
- **高内聚**: 模块内部功能紧密相关，职责明确
- **单一职责**: 每个模块有明确的业务边界
- **可复用性**: 模块可在不同应用间复用

#### 模块依赖层次

```
app (应用级)
    ↓
feature:* (功能模块)
    ↓
core:* (核心模块)
```

**依赖规则**：
- `app`模块依赖所有`feature`模块和必要的`core`模块
- `feature`模块只依赖`core`模块，不依赖其他`feature`模块
- `core`模块可以依赖其他`core`模块，但不能依赖`feature`或`app`模块

#### 核心模块 (core/)

| 模块 | 职责 | 关键类 |
|------|------|--------|
| `core:data` | 数据层实现，Repository公共API | `TopicsRepository`, `NewsRepository` |
| `core:database` | 本地数据库存储 | `NiaDatabase`, `Dao`类 |
| `core:datastore` | 用户偏好存储 | `NiaPreferences`, `UserPreferencesSerializer` |
| `core:network` | 网络请求和响应处理 | `RetrofitNiaNetworkApi` |
| `core:model` | 应用数据模型 | `Topic`, `NewsResource`, `UserData` |
| `core:common` | 通用工具类 | `NiaDispatchers`, `Result` |
| `core:designsystem` | 设计系统核心组件 | `NiaIcons`, `NiaButton`, `NiaTheme` |
| `core:ui` | 依赖数据的复合UI组件 | `NewsFeed`, `NewsResourceCard` |
| `core:domain` | 业务逻辑用例 | `GetUserNewsResourcesUseCase` |
| `core:testing` | 测试工具和假数据 | `NiaTestRunner`, `TestDispatcherRule` |

#### 功能模块 (feature/)

功能模块遵循以下约定：

**命名规则**: `feature:功能名称`

**职责范围**:
- 处理特定用户旅程的功能
- 包含UI组件和ViewModels
- 从其他模块读取数据
- 不依赖其他功能模块

**示例模块**:
- `feature:foryou` - 个性化新闻推荐页面
- `feature:bookmarks` - 书签管理功能
- `feature:topic` - 话题详情页面
- `feature:search` - 搜索功能
- `feature:settings` - 应用设置

#### 应用模块 (app)

**职责**:
- 应用级别的胶水代码
- 主要Activity和Application类
- 应用级导航控制
- 依赖注入设置

**关键组件**:
- `MainActivity` - 应用入口
- `NiaApp` - 应用根组件
- `NiaNavHost` - 导航设置
- `TopLevelDestination` - 顶级导航目标


#### 测试和工具模块
- `core:testing` - 测试工具和假数据
- `benchmarks` - 性能基准测试
- `lint` - 自定义Lint规则

## 2. 数据层 (Data Layer)

### 2.1 离线优先架构

采用**离线优先**策略，数据层作为应用数据和业务逻辑的**唯一真实来源**：

```
Repository (Public API) 
    ↓
Local Storage (Room) ← → Remote API (Retrofit)
    ↓
Data Synchronization (WorkManager)
```

**核心原则**：
- 本地存储为主要数据源
- 远程数据仅用于同步
- 错误处理与数据一致性保证
- 指数退避策略处理网络错误

### 2.2 Repository模式

每个Repository都有独立的数据模型：

```kotlin
// 示例：TopicsRepository
interface TopicsRepository {
    fun getTopics(): Flow<List<Topic>>
    suspend fun syncTopics()
}

class OfflineFirstTopicsRepository @Inject constructor(
    private val topicsDao: TopicsDao,
    private val networkDataSource: NiaNetworkDataSource
) : TopicsRepository {
    
    override fun getTopics(): Flow<List<Topic>> =
        topicsDao.getTopics().map { it.asExternalModel() }
    
    override suspend fun syncTopics() {
        // 同步逻辑
    }
}
```

### 2.3 数据源类型

| 数据源类型 | 技术栈 | 用途 |
|----------|--------|------|
| 本地数据库 | Room/SQLite | 持久化结构化数据 |
| 用户偏好 | Proto DataStore | 用户设置和偏好 |
| 远程API | Retrofit + OkHttp | 网络数据获取 |
| 缓存 | Memory Cache | 临时数据存储 |

### 2.4 数据同步机制

使用`WorkManager`实现后台数据同步：

```kotlin
class SyncWorker : CoroutineWorker {
    override suspend fun doWork(): Result {
        return try {
            // 同步所有数据源
            syncManager.syncAll()
            Result.success()
        } catch (e: Exception) {
            Result.retry() // 指数退避重试
        }
    }
}
```

## 3. 领域层 (Domain Layer)

### 3.1 Use Case模式

领域层包含业务逻辑用例，简化和去重ViewModel逻辑：

```kotlin
class GetUserNewsResourcesUseCase @Inject constructor(
    private val newsRepository: NewsRepository,
    private val userDataRepository: UserDataRepository
) {
    operator fun invoke(): Flow<List<UserNewsResource>> {
        return newsRepository.getNewsResources()
            .combine(userDataRepository.userData) { news, userData ->
                news.map { newsResource ->
                    UserNewsResource(
                        newsResource = newsResource,
                        isBookmarked = userData.bookmarkedNewsResources.contains(newsResource.id)
                    )
                }
            }
    }
}
```

### 3.2 业务逻辑封装

**Use Case特点**：
- 单一职责原则
- 可复用的业务逻辑
- 组合多个Repository
- 转换数据格式
- 处理复杂的业务规则

### 3.3 响应式编程

使用Kotlin Flow实现响应式数据流：

```kotlin
// 组合多个数据源
fun getFollowableTopics(): Flow<List<FollowableTopic>> =
    combine(
        topicsRepository.getTopics(),
        userDataRepository.userData
    ) { topics, userData ->
        topics.map { topic ->
            FollowableTopic(
                topic = topic,
                isFollowed = userData.followedTopics.contains(topic.id)
            )
        }
    }
```

## 4. UI层 (UI Layer)

### 4.1 Jetpack Compose声明式UI

UI层组成：
- **UI元素**: Jetpack Compose组件
- **状态管理**: Android ViewModels
- **状态建模**: 密封类hierarchy

### 4.2 UI状态建模

使用密封接口创建状态hierarchy：

```kotlin
sealed interface NewsFeedUiState {
    data object Loading : NewsFeedUiState
    
    data class Success(
        val feed: List<UserNewsResource>
    ) : NewsFeedUiState
    
    data class Error(
        val exception: Throwable
    ) : NewsFeedUiState
}
```

### 4.3 ViewModel最佳实践

```kotlin
@HiltViewModel
class ForYouViewModel @Inject constructor(
    private val getUserNewsResourcesUseCase: GetUserNewsResourcesUseCase
) : ViewModel() {
    
    val uiState: StateFlow<NewsFeedUiState> = 
        getUserNewsResourcesUseCase()
            .map { newsResources ->
                NewsFeedUiState.Success(newsResources)
            }
            .catch { 
                emit(NewsFeedUiState.Error(it))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = NewsFeedUiState.Loading
            )
    
    fun onNewsResourceBookmarked(newsResource: UserNewsResource) {
        // 处理用户交互
    }
}
```

### 4.4 Compose UI组件

```kotlin
@Composable
fun ForYouScreen(
    uiState: NewsFeedUiState,
    onBookmarkChanged: (String, Boolean) -> Unit
) {
    when (uiState) {
        is NewsFeedUiState.Loading -> LoadingIndicator()
        is NewsFeedUiState.Success -> NewsFeed(
            feed = uiState.feed,
            onBookmarkChanged = onBookmarkChanged
        )
        is NewsFeedUiState.Error -> ErrorMessage(uiState.exception)
    }
}
```

## 5. 架构最佳实践

### 5.1 数据流管理

**冷流到热流转换**：
```kotlin
// 在ViewModel中转换为StateFlow
private val _uiState = MutableStateFlow(InitialState)
val uiState: StateFlow<UiState> = _uiState.asStateFlow()

// 或使用stateIn操作符
val uiState = repository.data
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = InitialState
    )
```

### 5.2 错误处理

```kotlin
// Repository层错误处理
suspend fun syncData(): Result<Unit> = try {
    networkDataSource.fetchData()
    Result.success(Unit)
} catch (e: Exception) {
    Result.failure(e)
}

// UI层错误处理
sealed interface UiState {
    data object Loading : UiState
    data class Success(val data: List<Item>) : UiState
    data class Error(val message: String) : UiState
}
```

### 5.3 测试策略

```kotlin
// Repository测试
@Test
fun `when sync data success, should emit correct data`() = runTest {
    // Arrange
    val fakeData = listOf(Topic("1", "Android"))
    fakeNetworkDataSource.setTopics(fakeData)
    
    // Act
    repository.syncTopics()
    
    // Assert
    val topics = repository.getTopics().first()
    assertEquals(fakeData, topics)
}

// ViewModel测试
@Test
fun `when use case emits data, should update ui state`() = runTest {
    // Arrange
    val testData = listOf(UserNewsResource(...))
    fakeUseCase.emit(testData)
    
    // Act & Assert
    assertEquals(
        NewsFeedUiState.Success(testData),
        viewModel.uiState.value
    )
}
```

## 2. Gradle构建系统

### 2.1 版本目录 (Version Catalog)
使用`gradle/libs.versions.toml`集中管理依赖版本：

```toml
[versions]
kotlin = "2.1.10"
androidGradlePlugin = "8.9.0"
androidxComposeBom = "2025.02.00"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "androidxCore" }

[plugins]
android-application = { id = "com.android.application", version.ref = "androidGradlePlugin" }
```

### 2.2 Convention Plugins (build-logic/)
创建自定义插件避免重复配置：

- `nowinandroid.android.application` - Android应用配置
- `nowinandroid.android.library` - Android库配置
- `nowinandroid.android.library.compose` - Compose库配置
- `nowinandroid.hilt` - Hilt依赖注入配置

**核心配置文件**：
- `build-logic/convention/src/main/kotlin/AndroidApplicationConventionPlugin.kt`
- `build-logic/convention/src/main/kotlin/com/google/samples/apps/nowinandroid/KotlinAndroid.kt`

### 2.3 构建优化配置
```properties
# gradle.properties
org.gradle.jvmargs=-Xmx4g -XX:+UseG1GC
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configuration-cache=true
kotlin.code.style=official
```

### 2.4 产品变体 (Product Flavors)
- **demo**: 使用本地静态数据
- **prod**: 连接真实后端服务

## 3. GitHub Actions CI/CD

### 3.1 主要工作流

#### Build.yaml - 主构建流程
**触发条件**：
- Push到main分支
- Pull Request
- 手动触发

**主要步骤**：
1. **环境准备**
    - JDK 17 (Zulu发行版)
    - Gradle缓存配置
    - CI专用gradle.properties

2. **代码质量检查**
   ```bash
   ./gradlew spotlessCheck
   ./gradlew dependencyGuard
   ./gradlew :build-logic:convention:check
   ```

3. **测试执行**
   ```bash
   ./gradlew verifyRoborazziDemoDebug  # 截图测试
   ./gradlew testDemoDebug :lint:test   # 单元测试
   ```

4. **构建和检查**
   ```bash
   ./gradlew :app:assemble
   ./gradlew :app:lintProdRelease
   ./gradlew :app:checkProdReleaseBadging
   ```

5. **Android测试**
    - 多API级别测试矩阵 (26, 34)
    - 使用Android模拟器
    - 覆盖率报告生成

### 3.2 自动化特性
- **自动截图更新**: 失败时自动生成新截图
- **依赖基线更新**: 自动更新依赖守护基线
- **覆盖率报告**: Jacoco生成覆盖率并展示

### 3.3 工件上传
- APK构建产物
- 测试结果(XML)
- 截图测试结果(PNG)
- Lint报告(HTML/SARIF)
- 覆盖率报告

## 4. 代码质量工具

### 4.1 Lint检查
- Android原生Lint
- 自定义Lint规则 (`lint/`)
- SARIF格式报告支持

### 4.2 代码格式化
- **Spotless**: 统一代码格式
- **ktlint**: Kotlin代码风格检查
- 自动格式化集成

### 4.3 依赖管理
- **Dependency Guard**: 防止意外依赖变更
- 依赖基线文件管理
- 自动化依赖更新检查

### 4.4 测试策略
```
单元测试 (JVM)
    ↓
集成测试 (Android Test)
    ↓
UI测试 (Compose Test)
    ↓
截图测试 (Roborazzi)
    ↓
性能测试 (Macrobenchmark)
```

## 5. 性能优化

### 5.1 Baseline Profiles
- 位置：`app/src/main/baseline-prof.txt`
- 自动生成：通过Macrobenchmark
- 作用：启动性能优化

### 5.2 Compose编译器优化
```bash
./gradlew assembleRelease -PenableComposeCompilerMetrics=true
```
- 编译器稳定性报告
- 重组性能分析

### 5.3 构建性能
- Gradle配置缓存
- 并行构建
- 增量编译
- 模块化带来的构建隔离

## 6. 开发工作流程

### 6.1 开发环境要求
- JDK 17+
- Android Studio最新稳定版
- Git

### 6.2 本地开发
```bash
# 克隆项目
git clone https://github.com/android/nowinandroid.git

# 运行demo版本（推荐）
./gradlew :app:assembleDemoDebug

# 运行测试
./gradlew testDemoDebug
./gradlew recordRoborazziDemoDebug  # 记录截图
```

### 6.3 分支策略
- `main`: 主分支，受保护
- Feature分支: 功能开发
- PR要求: 所有检查通过

### 6.4 提交流程
1. 创建feature分支
2. 开发功能
3. 运行本地测试
4. 提交PR
5. CI自动检查
6. 代码审查
7. 合并到main

## 7. 学习要点总结

### 7.1 架构设计原则
- 清晰的分层架构
- 单向数据流
- 关注点分离
- 可测试性优先

### 7.2 模块化最佳实践
- 按功能模块化
- 明确的模块边界
- 避免循环依赖
- Convention plugins复用配置

### 7.3 CI/CD最佳实践
- 自动化质量检查
- 多维度测试覆盖
- 构建产物管理
- 自动化修复机制

### 7.4 现代Android技术栈
- Jetpack Compose声明式UI
- Kotlin协程异步编程
- Hilt依赖注入
- Room数据库
- DataStore偏好存储

## 8. 关键文件参考

### 构建配置
- `settings.gradle.kts` - 项目设置和模块声明
- `build.gradle.kts` - 根项目构建脚本
- `gradle/libs.versions.toml` - 版本目录
- `gradle.properties` - 全局Gradle配置

### CI/CD
- `.github/workflows/Build.yaml` - 主构建流程
- `.github/ci-gradle.properties` - CI专用配置

### 代码质量
- `spotless/` - 代码格式化规则
- `lint/` - 自定义Lint规则
- `compose_compiler_config.conf` - Compose编译器配置

### 文档
- `docs/ArchitectureLearningJourney.md` - 架构学习指南
- `docs/ModularizationLearningJourney.md` - 模块化学习指南

这个项目是学习现代Android开发的宝贵资源，涵盖了从基础架构到高级CI/CD的各个方面，值得深入研究和实践。