# Architecture

## 项目概述

**该项目完全使用Kotlin和Jetpack Compose构建，现代Android架构、模块化设计和CI/CD流程。

### 技术栈
- **语言**: Kotlin 2.1.10
- **UI框架**: Jetpack Compose (BOM 2025.02.00)
- **架构**: MVVM + Clean Architecture
- **依赖注入**: Hilt
- **网络**: Retrofit + OkHttp
- **数据库**: Room
- **异步处理**: Kotlin Coroutines + Flow
- **构建工具**: Gradle 8.9.0 + AGP
- **测试**: JUnit, Espresso, Roborazzi截图测试

## 1. 项目架构设计

### 1.1 分层架构
项目采用三层架构模式：

```
UI Layer (Compose + ViewModels)
    ↓
Domain Layer (Use Cases)
    ↓
Data Layer (Repositories + Data Sources)
```

**数据流向**：
- 事件向下流动
- 数据向上流动
- 响应式编程模型（Kotlin Flow）

### 1.2 模块化设计

#### 核心模块 (core/)
- `core:analytics` - 分析和统计
- `core:common` - 通用工具和类
- `core:data` - 数据层实现
- `core:database` - Room数据库
- `core:datastore` - 用户偏好存储
- `core:designsystem` - 设计系统和UI组件
- `core:domain` - 业务逻辑用例
- `core:model` - 数据模型
- `core:network` - 网络层
- `core:ui` - 可复用UI组件

#### 功能模块 (feature/)

> 功能模块的命名约定：（xxx代码功能名称）

- `feature:xxx` - xxx


#### 测试和工具模块
- `core:testing` - 测试工具和假数据
- `benchmarks` - 性能基准测试
- `lint` - 自定义Lint规则

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