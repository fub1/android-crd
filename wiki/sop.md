# 现代Android项目开发SOP (标准操作流程)

基于Now in Android最佳实践的完整项目搭建指南

## 📋 项目创建清单

### 前置准备
- [ ] Android Studio最新稳定版
- [ ] JDK 17+
- [ ] Git配置完成
- [ ] 项目需求文档确认

---

## 🚀 Phase 1: 项目初始化 (预计2小时)

### 1.1 创建基础项目结构

```bash
# 1. 创建项目根目录
mkdir YourAppName
cd YourAppName

# 2. 初始化Git仓库
git init
```

### 1.2 创建基础文件结构

```
YourAppName/
├── app/                          # 主应用模块
├── build-logic/                  # 构建逻辑
│   ├── convention/
│   └── settings.gradle.kts
├── core/                         # 核心模块
│   ├── common/
│   ├── data/
│   ├── database/
│   ├── datastore/
│   ├── designsystem/
│   ├── domain/
│   ├── model/
│   ├── network/
│   └── ui/
├── feature/                      # 功能模块
├── gradle/
│   ├── libs.versions.toml
│   └── wrapper/
├── .github/
│   └── workflows/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── README.md
```

### 1.3 创建根目录settings.gradle.kts

```kotlin
pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
    }
}

rootProject.name = "com.crtyiot.crd"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

// 核心模块
include(":app")
include(":core:common")
include(":core:data")
include(":core:database")
include(":core:datastore")
include(":core:designsystem")
include(":core:domain")
include(":core:model")
include(":core:network")
include(":core:ui")

// 功能模块（根据需要添加）
// include(":feature:home")
// include(":feature:profile")
// include(":feature:settings")

check(JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_17)) {
    """
    项目需要JDK 17+，当前使用 ${JavaVersion.current()}
    Java Home: [${System.getProperty("java.home")}]
    """.trimIndent()
}
```

---

## 🔧 Phase 2: 构建系统配置 (预计3小时)

### 2.1 创建Version Catalog (gradle/libs.versions.toml)

```toml
[versions]
# Build tools
kotlin = "2.1.10"
androidGradlePlugin = "8.9.0"
ksp = "2.1.10-1.0.31"

# AndroidX
androidxCore = "1.15.0"
androidxLifecycle = "2.8.7"
androidxActivity = "1.9.3"
androidxComposeBom = "2025.02.00"
androidxNavigation = "2.8.5"
androidxRoom = "2.6.1"
androidxDataStore = "1.1.1"
androidxWork = "2.10.0"

# Third party
hilt = "2.56"
retrofit = "2.11.0"
okhttp = "4.12.0"
kotlinxCoroutines = "1.10.1"
kotlinxSerializationJson = "1.8.0"
coil = "2.7.0"

# Testing
junit4 = "4.13.2"
androidxTestExt = "1.2.1"
androidxEspresso = "3.6.1"
robolectric = "4.14.1"
truth = "1.4.4"
turbine = "1.2.0"

[libraries]
# AndroidX
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "androidxCore" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "androidxLifecycle" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "androidxLifecycle" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "androidxActivity" }

# Compose BOM
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "androidxComposeBom" }
androidx-compose-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
androidx-compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-compose-material3 = { group = "androidx.compose.material3", name = "material3" }

# Navigation
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "androidxNavigation" }

# Room
androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "androidxRoom" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "androidxRoom" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "androidxRoom" }

# DataStore
androidx-datastore = { group = "androidx.datastore", name = "datastore", version.ref = "androidxDataStore" }

# Hilt
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-compiler", version.ref = "hilt" }
androidx-hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version = "1.2.0" }

# Network
retrofit-core = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
retrofit-kotlin-serialization = { group = "com.squareup.retrofit2", name = "converter-kotlinx-serialization", version.ref = "retrofit" }
okhttp-logging = { group = "com.squareup.okhttp3", name = "logging-interceptor", version.ref = "okhttp" }

# Serialization
kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "kotlinxSerializationJson" }

# Coroutines
kotlinx-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "kotlinxCoroutines" }

# Image loading
coil-kt = { group = "io.coil-kt", name = "coil", version.ref = "coil" }
coil-kt-compose = { group = "io.coil-kt", name = "coil-compose", version.ref = "coil" }

# Testing
junit = { group = "junit", name = "junit", version.ref = "junit4" }
androidx-test-ext-junit = { group = "androidx.test.ext", name = "junit", version.ref = "androidxTestExt" }
androidx-espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "androidxEspresso" }
androidx-compose-ui-test-junit4 = { group = "androidx.compose.ui", name = "ui-test-junit4" }
androidx-compose-ui-test-manifest = { group = "androidx.compose.ui", name = "ui-test-manifest" }
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "kotlinxCoroutines" }
turbine = { group = "app.cash.turbine", name = "turbine", version.ref = "turbine" }
truth = { group = "com.google.truth", name = "truth", version.ref = "truth" }
hilt-android-testing = { group = "com.google.dagger", name = "hilt-android-testing", version.ref = "hilt" }

# Build logic dependencies
android-gradlePlugin = { group = "com.android.tools.build", name = "gradle", version.ref = "androidGradlePlugin" }
kotlin-gradlePlugin = { group = "org.jetbrains.kotlin", name = "kotlin-gradle-plugin", version.ref = "kotlin" }
ksp-gradlePlugin = { group = "com.google.devtools.ksp", name = "com.google.devtools.ksp.gradle.plugin", version.ref = "ksp" }

[plugins]
android-application = { id = "com.android.application", version.ref = "androidGradlePlugin" }
android-library = { id = "com.android.library", version.ref = "androidGradlePlugin" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
compose-compiler = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
room = { id = "androidx.room", version.ref = "androidxRoom" }

# Convention plugins
yourapp-android-application = { id = "yourapp.android.application" }
yourapp-android-library = { id = "yourapp.android.library" }
yourapp-android-library-compose = { id = "yourapp.android.library.compose" }
yourapp-hilt = { id = "yourapp.hilt" }
```

### 2.2 创建根build.gradle.kts

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.room) apply false
}
```

### 2.3 创建gradle.properties

```properties
# Project-wide Gradle settings
org.gradle.jvmargs=-Dfile.encoding=UTF-8 -XX:+UseG1GC -XX:SoftRefLRUPolicyMSPerMB=1 -XX:ReservedCodeCacheSize=256m -XX:+HeapDumpOnOutOfMemoryError -Xmx4g -Xms4g
kotlin.daemon.jvmargs=-Dfile.encoding=UTF-8 -XX:+UseG1GC -XX:SoftRefLRUPolicyMSPerMB=1 -XX:ReservedCodeCacheSize=320m -XX:+HeapDumpOnOutOfMemoryError -Xmx4g -Xms4g

# Build optimizations
org.gradle.parallel=true
org.gradle.configureondemand=false
org.gradle.caching=true
org.gradle.configuration-cache=true

# AndroidX
android.useAndroidX=true
android.defaults.buildfeatures.resvalues=false
android.defaults.buildfeatures.shaders=false

# Kotlin
kotlin.code.style=official
```

---

## 🏗️ Phase 3: Convention Plugins设置 (预计2小时)

### 3.1 创建build-logic结构

```
build-logic/
├── convention/
│   ├── build.gradle.kts
│   └── src/main/kotlin/
│       ├── AndroidApplicationConventionPlugin.kt
│       ├── AndroidLibraryConventionPlugin.kt
│       ├── AndroidLibraryComposeConventionPlugin.kt
│       ├── HiltConventionPlugin.kt
│       └── com/yourcompany/yourapp/
│           ├── KotlinAndroid.kt
│           └── ProjectExtensions.kt
└── settings.gradle.kts
```

### 3.2 build-logic/settings.gradle.kts

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "build-logic"
include(":convention")
```

### 3.3 build-logic/convention/build.gradle.kts

```kotlin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "com.yourcompany.yourapp.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}

gradlePlugin {
    plugins {
        register("androidApplicationConvention") {
            id = "yourapp.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibraryConvention") {
            id = "yourapp.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidLibraryComposeConvention") {
            id = "yourapp.android.library.compose"
            implementationClass = "AndroidLibraryComposeConventionPlugin"
        }
        register("hiltConvention") {
            id = "yourapp.hilt"
            implementationClass = "HiltConventionPlugin"
        }
    }
}
```

### 3.4 AndroidApplicationConventionPlugin.kt

```kotlin
import com.android.build.api.dsl.ApplicationExtension
import com.yourcompany.yourapp.configureKotlinAndroid
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
                apply("org.jetbrains.kotlin.android")
            }

            extensions.configure<ApplicationExtension> {
                configureKotlinAndroid(this)
                
                compileSdk = 35
                
                defaultConfig {
                    applicationId = "com.yourcompany.yourapp"
                    targetSdk = 35
                    minSdk = 24
                    
                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                    vectorDrawables {
                        useSupportLibrary = true
                    }
                }

                buildTypes {
                    release {
                        isMinifyEnabled = true
                        proguardFiles(
                            getDefaultProguardFile("proguard-android-optimize.txt"),
                            "proguard-rules.pro"
                        )
                    }
                }
                
                packaging {
                    resources {
                        excludes += "/META-INF/{AL2.0,LGPL2.1}"
                    }
                }
                
                testOptions {
                    unitTests {
                        isIncludeAndroidResources = true
                    }
                }
            }
        }
    }
}
```

### 3.5 KotlinAndroid.kt工具函数

```kotlin
package com.yourcompany.yourapp

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal fun Project.configureKotlinAndroid(
    commonExtension: CommonExtension<*, *, *, *, *, *>,
) {
    commonExtension.apply {
        compileSdk = 35

        defaultConfig {
            minSdk = 24
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }
    }

    configureKotlin()
}

private fun Project.configureKotlin() {
    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_17.toString()
            freeCompilerArgs += listOf(
                "-opt-in=kotlin.RequiresOptIn",
                "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            )
        }
    }
}
```

---

## 📱 Phase 4: 核心模块创建 (预计4小时)

### 4.1 核心模块创建清单

按以下顺序创建模块（避免依赖问题）：

1. [ ] core:model
2. [ ] core:common
3. [ ] core:database
4. [ ] core:datastore
5. [ ] core:network
6. [ ] core:data
7. [ ] core:domain
8. [ ] core:designsystem
9. [ ] core:ui

### 4.2 core:model模块

**core/model/build.gradle.kts**
```kotlin
plugins {
    alias(libs.plugins.yourapp.android.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.yourcompany.yourapp.core.model"
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
}
```

**示例数据模型**：
```kotlin
// core/model/src/main/kotlin/com/yourcompany/yourapp/core/model/User.kt
@Serializable
data class User(
    val id: String,
    val name: String,
    val email: String,
    val avatar: String? = null
)

// core/model/src/main/kotlin/com/yourcompany/yourapp/core/model/NetworkResult.kt
sealed interface NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>
    data class Error(val exception: Throwable) : NetworkResult<Nothing>
    data object Loading : NetworkResult<Nothing>
}
```

### 4.3 core:common模块

**core/common/build.gradle.kts**
```kotlin
plugins {
    alias(libs.plugins.yourapp.android.library)
    alias(libs.plugins.yourapp.hilt)
}

android {
    namespace = "com.yourcompany.yourapp.core.common"
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.lifecycle.runtime.ktx)
}
```

**示例工具类**：
```kotlin
// core/common/src/main/kotlin/com/yourcompany/yourapp/core/common/Dispatchers.kt
@Module
@InstallIn(SingletonComponent::class)
object DispatchersModule {
    
    @Provides
    @Singleton
    @Dispatcher(IO)
    fun providesIODispatcher(): CoroutineDispatcher = Dispatchers.IO
    
    @Provides
    @Singleton  
    @Dispatcher(Default)
    fun providesDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
}

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(val dispatcher: AppDispatchers)

enum class AppDispatchers {
    Default,
    IO,
}
```

### 4.4 core:network模块

**core/network/build.gradle.kts**
```kotlin
plugins {
    alias(libs.plugins.yourapp.android.library)
    alias(libs.plugins.yourapp.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.yourcompany.yourapp.core.network"
    
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.model)
    
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.kotlin.serialization)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)
}
```

### 4.5 app模块基础配置

**app/build.gradle.kts**
```kotlin
plugins {
    alias(libs.plugins.yourapp.android.application)
    alias(libs.plugins.yourapp.android.application.compose)
    alias(libs.plugins.yourapp.hilt)
}

android {
    namespace = "com.yourcompany.yourapp"
    
    defaultConfig {
        applicationId = "com.yourcompany.yourapp"
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    // Core modules
    implementation(projects.core.common)
    implementation(projects.core.data)
    implementation(projects.core.designsystem)
    implementation(projects.core.ui)
    
    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    
    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    
    // Navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
```

---

## 🎯 Phase 5: 基础代码模板 (预计3小时)

### 5.1 Application类

```kotlin
// app/src/main/kotlin/com/yourcompany/yourapp/YourApplication.kt
@HiltAndroidApp
class YourApplication : Application()
```

### 5.2 MainActivity

```kotlin
// app/src/main/kotlin/com/yourcompany/yourapp/MainActivity.kt
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            YourAppTheme {
                YourApp()
            }
        }
    }
}

@Composable
fun YourApp() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen()
        }
    }
}
```

### 5.3 Repository模板

```kotlin
// core/data/src/main/kotlin/com/yourcompany/yourapp/core/data/repository/UserRepository.kt
interface UserRepository {
    suspend fun getUser(id: String): NetworkResult<User>
    suspend fun updateUser(user: User): NetworkResult<User>
}

@Singleton
class DefaultUserRepository @Inject constructor(
    private val networkDataSource: NetworkDataSource,
    private val localDataSource: LocalDataSource,
    @Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
) : UserRepository {
    
    override suspend fun getUser(id: String): NetworkResult<User> = 
        withContext(ioDispatcher) {
            try {
                val user = networkDataSource.getUser(id)
                NetworkResult.Success(user)
            } catch (exception: Exception) {
                NetworkResult.Error(exception)
            }
        }
}
```

### 5.4 ViewModel模板

```kotlin
// feature/home/src/main/kotlin/com/yourcompany/yourapp/feature/home/HomeViewModel.kt
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    fun loadUser(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            when (val result = userRepository.getUser(id)) {
                is NetworkResult.Success -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            user = result.data
                        )
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = result.exception.message
                        )
                    }
                }
                NetworkResult.Loading -> {
                    // Already handled above
                }
            }
        }
    }
}

data class HomeUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val errorMessage: String? = null,
)
```

---

## 🔄 Phase 6: CI/CD设置 (预计2小时)

### 6.1 创建GitHub Actions工作流

**.github/workflows/ci.yml**
```yaml
name: CI

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

concurrency:
  group: build-${{ github.ref }}
  cancel-in-progress: true

jobs:
  test:
    runs-on: ubuntu-latest
    timeout-minutes: 60

    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: 'zulu'
          java-version: 17

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v4

      - name: Run unit tests
        run: ./gradlew testDebugUnitTest

      - name: Run lint
        run: ./gradlew lintDebug

      - name: Build debug APK
        run: ./gradlew assembleDebug

      - name: Upload build artifacts
        uses: actions/upload-artifact@v4
        with:
          name: apk
          path: app/build/outputs/apk/debug/*.apk
```

### 6.2 代码质量配置

**创建.editorconfig**
```ini
root = true

[*]
charset = utf-8
insert_final_newline = true
trim_trailing_whitespace = true

[*.{kt,kts}]
indent_style = space
indent_size = 4
max_line_length = 120

[*.{xml,json}]
indent_style = space
indent_size = 2
```

**创建detekt配置（可选）**
```yaml
# detekt.yml
complexity:
  LongMethod:
    threshold: 50
  LongParameterList:
    threshold: 6

naming:
  ClassNaming:
    classPattern: '[A-Z$][a-zA-Z0-9$]*'
```

---

## ✅ Phase 7: 验证和测试 (预计1小时)

### 7.1 验证清单

- [ ] 项目编译成功
- [ ] 单元测试通过
- [ ] Lint检查通过
- [ ] APK构建成功
- [ ] 基础功能运行正常
- [ ] CI/CD流程运行正常

### 7.2 测试命令

```bash
# 编译检查
./gradlew compileDebugKotlin

# 运行测试
./gradlew testDebugUnitTest

# Lint检查
./gradlew lintDebug

# 构建APK
./gradlew assembleDebug

# 清理项目
./gradlew clean
```

---

## 📚 Phase 8: 文档和团队协作 (预计1小时)

### 8.1 创建README.md

```markdown
# YourAppName

## 开发环境
- Android Studio最新稳定版
- JDK 17+
- Kotlin 2.1.10+

## 构建
```bash
./gradlew assembleDebug
```

## 测试
```bash
./gradlew testDebugUnitTest
```

## 架构
本项目采用MVVM + Clean Architecture，详见[架构文档](docs/ARCHITECTURE.md)

## 贡献指南
请阅读[贡献指南](CONTRIBUTING.md)
```

### 8.2 团队开发规范

**Git提交规范**
```
feat: 新功能
fix: 修复bug
docs: 文档更新
style: 代码格式调整
refactor: 重构
test: 测试相关
chore: 构建和工具相关
```

**分支策略**
- `main`: 主分支
- `develop`: 开发分支
- `feature/xxx`: 功能分支
- `hotfix/xxx`: 紧急修复分支

---

## 🎉 完成检查清单

### 最终验证
- [ ] 所有模块创建完成
- [ ] 构建系统配置正确
- [ ] CI/CD流程设置完成
- [ ] 基础代码模板就位
- [ ] 文档完善
- [ ] 团队规范制定

### 下一步
1. 根据具体需求添加feature模块
2. 完善设计系统
3. 添加更多测试
4. 设置代码覆盖率检查
5. 配置发布流程

---

## 📖 参考资源

- [Now in Android 项目](https://github.com/android/nowinandroid)
- [Android架构指南](https://developer.android.com/topic/architecture)
- [Compose指南](https://developer.android.com/jetpack/compose)
- [Gradle最佳实践](https://docs.gradle.org/current/userguide/best_practices.html)

**预计总时间**: 18小时（可根据团队经验调整）

这个SOP确保你能快速搭建一个具备现代Android开发最佳实践的项目基础。