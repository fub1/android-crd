# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is an Android application project named "crd" (package: `com.crtyiot.crd`) built with modern Android architecture using Kotlin and Jetpack Compose. The project follows a modular design with Clean Architecture principles.

### Target Architecture
- **Language**: Kotlin 2.1.10
- **UI Framework**: Jetpack Compose (BOM 2025.02.00)
- **Architecture**: MVI + Clean Architecture (following [Official Android Architecture Guide](https://developer.android.com/jetpack/guide))
- **Dependency Injection**: Hilt
- **Network**: Retrofit + OkHttp
- **Database**: Room (offline-first)
- **Async Processing**: Kotlin Coroutines + Flow
- **State Management**: StateFlow with unidirectional data flow
- **Testing**: JUnit, Espresso, Roborazzi screenshot testing

## Development Commands

### Build
```bash
./gradlew build                    # Build the entire project
./gradlew app:assembleDebug        # Build debug APK
./gradlew app:assembleRelease      # Build release APK
./gradlew :app:assemble            # Assemble all variants
```

### Testing
```bash
./gradlew test                     # Run unit tests
./gradlew app:testDebugUnitTest    # Run unit tests for debug variant
./gradlew connectedAndroidTest     # Run instrumented tests (requires device/emulator)
./gradlew app:connectedDebugAndroidTest  # Run instrumented tests for debug variant
./gradlew testDemoDebug            # Run demo variant tests
./gradlew verifyRoborazziDemoDebug # Run screenshot tests
./gradlew recordRoborazziDemoDebug # Record new screenshots
```

### Lint and Code Quality
```bash
./gradlew lint                     # Run lint checks
./gradlew app:lintDebug           # Run lint for debug variant
./gradlew :app:lintProdRelease    # Run lint for production release
./gradlew spotlessCheck           # Check code formatting
./gradlew spotlessApply           # Apply code formatting
./gradlew dependencyGuard         # Check dependency changes
```

### Clean
```bash
./gradlew clean                    # Clean build artifacts
```

## Architecture Design

### Official Android Architecture Guidelines

Following the [Official Android Architecture Guide](https://developer.android.com/jetpack/guide), the project ensures:

- **Developer-friendly**: Easy to understand, avoiding experimental patterns
- **Team collaboration**: Supports multiple developers working on the same codebase
- **Testing support**: Facilitates local and instrumented tests, including CI
- **Performance**: Minimizes build times through proper modularization
- **Separation of concerns**: Clear boundaries between layers

### Three-Layer Architecture

Based on Google's recommended modern Android architecture:

```
UI Layer (Compose + ViewModels + UI State)
    ↓
Domain Layer (Use Cases + Business Logic)
    ↓
Data Layer (Repositories + Data Sources)
```

**Core Design Principles**:
- **Unidirectional Data Flow (UDF)**: Reactive programming model
- **Events flow down**: User interactions flow from UI to lower layers
- **Data flows up**: Data streams from data layer to UI layer
- **State-driven**: UI state is driven by data layer (single source of truth)

### MVI Architecture Pattern

Adopting **Model-View-Intent (MVI)** architecture:

```
Intent (User Interaction) → ViewModel → State (UI State) → View (Compose UI)
```

**MVI Core Components**:
- **Model**: Immutable data structures representing app state
- **View**: UI components that react to state changes (Jetpack Compose)
- **Intent**: User interaction events and system events
- **State**: UI state modeled using sealed class hierarchies

**MVI Benefits**:
- Predictable state management
- Easy testing and debugging
- Simple state restoration
- Thread safety

### Modular Design Strategy

Based on Google's Now in Android modularization best practices:

#### Modularization Principles
- **Low coupling**: Modules are as independent as possible
- **High cohesion**: Module contents are closely related with clear responsibilities
- **Single responsibility**: Each module has a well-defined purpose
- **Reusability**: Modules can be reused across different apps

#### Module Dependency Hierarchy

```
app (application level)
    ↓
feature:* (feature modules)
    ↓
core:* (core modules)
```

**Dependency Rules**:
- `app` module depends on all `feature` modules and required `core` modules
- `feature` modules only depend on `core` modules, never on other `feature` modules
- `core` modules can depend on other `core` modules, but not on `feature` or `app` modules

#### Core Modules (core/)

| Module | Responsibility | Key Classes |
|--------|----------------|-------------|
| `core:data` | Data layer implementation, Repository public API | `TopicsRepository`, `NewsRepository` |
| `core:database` | Local database storage using Room | `NiaDatabase`, `Dao` classes |
| `core:datastore` | User preferences storage | `NiaPreferences`, `UserPreferencesSerializer` |
| `core:network` | Network requests and remote data source | `RetrofitNiaNetworkApi` |
| `core:model` | Application data models | `Topic`, `NewsResource`, `UserData` |
| `core:common` | Common utilities and classes | `NiaDispatchers`, `Result` |
| `core:designsystem` | Design system core components | `NiaIcons`, `NiaButton`, `NiaTheme` |
| `core:ui` | Composite UI components dependent on data | `NewsFeed`, `NewsResourceCard` |
| `core:domain` | Business logic use cases | `GetUserNewsResourcesUseCase` |
| `core:testing` | Testing utilities and mock data | `NiaTestRunner`, `TestDispatcherRule` |

#### Feature Modules (feature/)

Feature modules follow these conventions:

**Naming Convention**: `feature:featurename`

**Scope**:
- Handle specific user journeys
- Contain UI components and ViewModels
- Read data from other modules
- No dependencies on other feature modules

**Example Modules**:
- `feature:foryou` - Personalized news feed
- `feature:bookmarks` - Bookmark management
- `feature:topic` - Topic detail screen
- `feature:search` - Search functionality
- `feature:settings` - Application settings

#### Application Module (app)

**Responsibilities**:
- App-level scaffolding code
- Main Activity and Application classes
- App-level navigation control
- Dependency injection setup

**Key Components**:
- `MainActivity` - App entry point
- `NiaApp` - Root app component
- `NiaNavHost` - Navigation setup
- `TopLevelDestination` - Top-level navigation destinations

#### Testing and Tools
- `core:testing` - Testing utilities and mock data
- `benchmarks` - Performance benchmarks
- `lint` - Custom lint rules

### Data Layer Implementation

#### Offline-First Architecture

The data layer implements an **offline-first** approach where:

- **Local storage is the source of truth**: All data reads come from local storage
- **Remote data is for synchronization**: Network calls only update local storage
- **Reactive data streams**: Data is exposed as Kotlin Flows, not snapshots
- **Automatic synchronization**: Background sync using WorkManager with exponential backoff

#### Repository Pattern

Each repository provides a clean API for data access:

```kotlin
interface NewsRepository {
    fun getNewsResources(): Flow<List<NewsResource>>
    suspend fun syncWith(synchronizer: Synchronizer)
}

class OfflineFirstNewsRepository @Inject constructor(
    private val newsResourceDao: NewsResourceDao,
    private val network: NiaNetworkDataSource
) : NewsRepository {
    
    override fun getNewsResources(): Flow<List<NewsResource>> =
        newsResourceDao.getNewsResources()
            .map { it.asExternalModel() }
    
    override suspend fun syncWith(synchronizer: Synchronizer) {
        // Synchronization logic with error handling
    }
}
```

#### Data Sources

| Data Source | Technology | Purpose |
|-------------|------------|---------|
| Local Database | Room/SQLite | Persistent structured data |
| User Preferences | Proto DataStore | User settings and preferences |
| Remote API | Retrofit + OkHttp | Network data fetching |
| Memory Cache | In-memory | Temporary data storage |

#### Error Handling and Synchronization

```kotlin
// Error handling in repositories
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<T>()
    data object Loading : Result<Nothing>()
}

// Synchronization with exponential backoff
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncManager: SyncManager
) : CoroutineWorker(appContext, workerParams) {
    
    override suspend fun doWork(): Result = try {
        syncManager.sync()
        Result.success()
    } catch (e: Exception) {
        Result.retry()
    }
}
```

### Domain Layer Implementation

#### Use Cases

Domain layer contains use cases that:
- Combine data from multiple repositories
- Transform data for UI consumption
- Encapsulate business logic
- Follow single responsibility principle

```kotlin
class GetUserNewsResourcesUseCase @Inject constructor(
    private val newsRepository: NewsRepository,
    private val userDataRepository: UserDataRepository
) {
    operator fun invoke(): Flow<List<UserNewsResource>> {
        return combine(
            newsRepository.getNewsResources(),
            userDataRepository.userData
        ) { newsResources, userData ->
            newsResources.map { newsResource ->
                UserNewsResource(
                    newsResource = newsResource,
                    isBookmarked = userData.bookmarkedNewsResources.contains(newsResource.id),
                    isSaved = userData.savedNewsResources.contains(newsResource.id)
                )
            }
        }
    }
}
```

### UI Layer Implementation

#### State Management

UI state is modeled using sealed interfaces:

```kotlin
sealed interface NewsFeedUiState {
    data object Loading : NewsFeedUiState
    data class Success(val feed: List<UserNewsResource>) : NewsFeedUiState
    data class Error(val messages: List<ErrorMessage>) : NewsFeedUiState
}

sealed interface NewsUiAction {
    data class BookmarkNews(val newsResourceId: String) : NewsUiAction
    data class ShareNews(val newsResourceId: String) : NewsUiAction
    data class ExpandNews(val newsResourceId: String) : NewsUiAction
}
```

#### ViewModel Implementation

```kotlin
@HiltViewModel
class ForYouViewModel @Inject constructor(
    private val getUserNewsResourcesUseCase: GetUserNewsResourcesUseCase,
    private val userDataRepository: UserDataRepository
) : ViewModel() {
    
    val feedState: StateFlow<NewsFeedUiState> = 
        getUserNewsResourcesUseCase()
            .map(NewsFeedUiState::Success)
            .catch { emit(NewsFeedUiState.Error(listOf(ErrorMessage.LoadError))) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = NewsFeedUiState.Loading
            )
    
    fun updateNewsResourceBookmark(newsResourceId: String, isBookmarked: Boolean) {
        viewModelScope.launch {
            userDataRepository.updateNewsResourceBookmark(newsResourceId, isBookmarked)
        }
    }
}
```

#### Compose UI Components

```kotlin
@Composable
fun ForYouRoute(
    viewModel: ForYouViewModel = hiltViewModel()
) {
    val feedState by viewModel.feedState.collectAsStateWithLifecycle()
    
    ForYouScreen(
        feedState = feedState,
        onNewsResourceBookmarkChanged = viewModel::updateNewsResourceBookmark
    )
}

@Composable
fun ForYouScreen(
    feedState: NewsFeedUiState,
    onNewsResourceBookmarkChanged: (String, Boolean) -> Unit
) {
    when (feedState) {
        NewsFeedUiState.Loading -> LoadingIndicator()
        is NewsFeedUiState.Success -> NewsFeed(
            feed = feedState.feed,
            onNewsResourceBookmarkChanged = onNewsResourceBookmarkChanged
        )
        is NewsFeedUiState.Error -> ErrorMessages(feedState.messages)
    }
}
```

## Project Structure

- **Package**: `com.crtyiot.crd`
- **Min SDK**: 30
- **Target SDK**: 35
- **Compile SDK**: 35
- **Kotlin Version**: 2.1.10 (target)
- **AGP Version**: 8.9.0 (target)

### Key Configuration Files
- `build.gradle.kts` - Root project build configuration
- `app/build.gradle.kts` - App module build configuration
- `gradle/libs.versions.toml` - Version catalog for dependency management
- `settings.gradle.kts` - Project settings and module inclusion
- `build-logic/` - Convention plugins for build configuration

### Source Structure
- `app/src/main/java/com/crtyiot/crd/` - Main source code
- `app/src/test/java/com/crtyiot/crd/` - Unit tests
- `app/src/androidTest/java/com/crtyiot/crd/` - Instrumented tests
- `app/src/main/res/` - Android resources
- `app/src/main/baseline-prof.txt` - Baseline profiles for performance

### Product Flavors
- **demo**: Uses local static data
- **prod**: Connects to real backend services

## Build System

### Version Catalog
Uses `gradle/libs.versions.toml` for centralized dependency management:

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

### Convention Plugins (build-logic/)
Custom plugins to avoid configuration duplication:

- `nowinandroid.android.application` - Android app configuration
- `nowinandroid.android.library` - Android library configuration  
- `nowinandroid.android.library.compose` - Compose library configuration
- `nowinandroid.hilt` - Hilt dependency injection configuration

### Build Optimization
```properties
# gradle.properties
org.gradle.jvmargs=-Xmx4g -XX:+UseG1GC
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configuration-cache=true
kotlin.code.style=official
```

## Dependencies

The project uses version catalog for dependency management with:
- AndroidX Core KTX
- Jetpack Compose BOM
- Hilt for dependency injection
- Retrofit + OkHttp for networking
- Room for local database
- Kotlin Coroutines + Flow
- JUnit for unit testing
- Espresso for UI testing
- Roborazzi for screenshot testing

## Testing Strategy

```
Unit Tests (JVM)
    ↓
Integration Tests (Android Test)
    ↓
UI Tests (Compose Test)
    ↓
Screenshot Tests (Roborazzi)
    ↓
Performance Tests (Macrobenchmark)
```

## Code Quality Tools

### Lint Checks
- Android native Lint
- Custom Lint rules (`lint/`)
- SARIF format reporting

### Code Formatting
- **Spotless**: Unified code formatting
- **ktlint**: Kotlin code style checks
- Automatic formatting integration

### Dependency Management
- **Dependency Guard**: Prevents accidental dependency changes
- Dependency baseline file management
- Automated dependency update checks

## Performance Optimization

### Baseline Profiles
- Location: `app/src/main/baseline-prof.txt`
- Auto-generated through Macrobenchmark
- Purpose: Startup performance optimization

### Compose Compiler Optimization
```bash
./gradlew assembleRelease -PenableComposeCompilerMetrics=true
```

## CI/CD Pipeline

### GitHub Actions Workflows

The project uses GitHub Actions for continuous integration and deployment with the following workflows:

#### Main Build Workflow (.github/workflows/build.yaml)

**Triggers:**
- Push to main branch
- Pull requests
- Manual dispatch

**Key Jobs:**

1. **Local Tests and APK Build**
   ```bash
   # Environment: Ubuntu Latest, JDK 17 (Zulu)
   ./gradlew :build-logic:convention:check
   ./gradlew spotlessCheck --init-script gradle/init.gradle.kts
   ./gradlew dependencyGuard
   ./gradlew verifyRoborazziDemoDebug
   ./gradlew testDemoDebug :lint:test
   ./gradlew :app:assemble
   ./gradlew :app:lintProdRelease
   ./gradlew :app:checkProdReleaseBadging
   ```

2. **Android Instrumented Tests**
   ```bash
   # Matrix: API levels 26, 34
   ./gradlew :app:connectedDemoDebugAndroidTest
   ./gradlew createDemoDebugCoverageReport
   ```

**Automated Features:**
- **Screenshot Test Updates**: Automatically updates screenshots when tests fail
- **Dependency Baseline Updates**: Auto-updates dependency guard baselines
- **Code Coverage Reports**: Generates and displays Jacoco coverage reports
- **Artifact Upload**: Uploads APKs, test results, lint reports

#### Release Workflow (.github/workflows/release.yml)

**Triggers:**
- Version tags (v*)
- Manual dispatch

**Process:**
1. Build release APK
2. Generate baseline profiles
3. Create GitHub release
4. Upload release artifacts

#### Nightly Baseline Profiles (.github/workflows/nightly.yaml)

**Schedule:** Daily at 4:42 AM UTC
**Purpose:** Automatically generate baseline profiles for performance optimization

### CI Configuration

**Environment Setup:**
```properties
# .github/ci-gradle.properties
org.gradle.daemon=false
org.gradle.parallel=true
org.gradle.workers.max=2
org.gradle.configuration-cache=true
kotlin.incremental=false
```

**Security:**
- Minimal required permissions
- Gradle cache encryption
- Secure secret management

### Quality Gates

**Required Checks:**
- Code formatting (Spotless)
- Lint analysis
- Unit tests
- Screenshot tests (Roborazzi)
- Dependency verification
- Build validation

**Automated Fixes:**
- Code formatting corrections
- Screenshot test updates
- Dependency baseline updates

## R8 Compilation Configuration

### Build Type Configuration

#### Debug Build
```kotlin
debug {
    // R8 disabled for fast development cycle
    isMinifyEnabled = false
    isDebuggable = true
    applicationIdSuffix = ".debug"
}
```

#### Release Build
```kotlin
release {
    // Full R8 optimization enabled
    isMinifyEnabled = true
    isShrinkResources = true
    proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
    )
    signingConfig = signingConfigs.getByName("debug")
    
    // Baseline profile integration
    baselineProfile.automaticGenerationDuringBuild = true
}
```

#### Benchmark Build
```kotlin
benchmark {
    // Partial R8 - compression without obfuscation
    isMinifyEnabled = true
    proguardFiles("benchmark-rules.pro")
    signingConfig = signingConfigs.getByName("debug")
    
    // Required for baseline profile generation
    baselineProfile.automaticGenerationDuringBuild = false
}
```

### ProGuard Rules Structure

#### Core Rules (app/proguard-rules.pro)
```proguard
# Keep Android components
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep Parcelable implementations
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

# Keep enum methods
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep serialization
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Retrofit and networking
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# Keep data binding
-keep class * extends androidx.databinding.ViewDataBinding {
    <methods>;
}

# Keep reflection-based annotations
-keepclassmembers class * {
    @org.jetbrains.annotations.Keep *;
}
```

#### Benchmark Rules (benchmark-rules.pro)
```proguard
# Disable obfuscation for baseline profile generation
-dontobfuscate

# Suppress third-party library warnings
-dontwarn org.bouncycastle.jsse.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**
```

#### Consumer Rules (core modules)
```proguard
# Example: core/datastore/consumer-proguard-rules.pro
# Protect Protocol Buffer fields
-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite* {
    <fields>;
}
```

### R8 Optimization Strategy

**Compression Levels:**
- Debug: 0% (disabled)
- Benchmark: ~8% (compression only)
- Release: ~10% (full optimization)

**Optimization Passes:**
```proguard
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
```

**Performance Integration:**
- Baseline Profile generation
- DEX layout optimization
- Resource shrinking
- Dead code elimination

### Baseline Profile Configuration

```kotlin
baselineProfile {
    // Controlled generation
    automaticGenerationDuringBuild = false
    
    // Enable DEX layout optimization
    dexLayoutOptimization = true
    
    // Benchmark module dependency
    baselineProfile(projects.benchmarks)
}
```

### Common R8 Issues and Solutions

**ClassNotFoundException:**
```proguard
# Solution: Add keep rules for reflected classes
-keep class com.example.ReflectedClass { <methods>; }
```

**Serialization Failures:**
```proguard
# Solution: Protect serialization fields
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    <fields>;
}
```

**Third-party Library Issues:**
```proguard
# Solution: Add library-specific rules
-keep class com.squareup.retrofit2.** { *; }
-keep interface com.squareup.retrofit2.** { *; }
```

## Development Workflow

### Local Development Setup
```bash
# Clone project
git clone https://github.com/fub1/android-crd.git
cd android-crd

# Build project
./gradlew build

# Run debug version
./gradlew app:assembleDebug

# Run tests
./gradlew testDebug
./gradlew connectedDebugAndroidTest

# Code quality checks
./gradlew spotlessCheck
./gradlew lint
```

### Branch Strategy and Version Control Rules

#### Branch Types
- `main`: Main development branch, protected
- `feature/*`: Feature development branches
- `bugfix/*`: Bug fix branches
- `hotfix/*`: Critical production fixes
- `release/*`: Release preparation branches

#### Branch Naming Convention
```
feature/user-authentication
feature/home-screen-ui
bugfix/login-crash
hotfix/security-patch
release/v1.0.0
```

#### Commit Message Format
Follow conventional commits specification:

```
<type>(<scope>): <description>

[optional body]

[optional footer]
```

**Types:**
- `feat`: New features
- `fix`: Bug fixes
- `docs`: Documentation updates
- `style`: Code style/formatting
- `refactor`: Code refactoring
- `test`: Test additions/modifications
- `chore`: Build/tooling changes

**Examples:**
```
feat(auth): implement user login with biometric authentication
fix(home): resolve crash when loading empty state
docs(readme): update installation instructions
style(ui): apply Material Design 3 theming
refactor(data): migrate to offline-first architecture
test(auth): add unit tests for login validation
chore(deps): update Compose BOM to 2025.02.00
```

#### Pre-commit Workflow
**Required checks before committing:**

```bash
# 1. Code formatting
./gradlew spotlessApply

# 2. Build verification
./gradlew build

# 3. Run tests
./gradlew testDebug
./gradlew lint

# 4. Git staging
git add .

# 5. Commit with proper message
git commit -m "feat(home): implement MainActivity with MVI architecture

- Add MainActivity with Jetpack Compose UI
- Implement MVI architecture pattern
- Add Hilt dependency injection
- Support responsive design with window size classes
- Include comprehensive unit tests

🤖 Generated with [Claude Code](https://claude.ai/code)

Co-Authored-By: Claude <noreply@anthropic.com>"
```

### Feature Development Process

#### 1. Create Feature Branch
```bash
# Create and switch to feature branch
git checkout -b feature/new-feature-name

# Or for bug fixes
git checkout -b bugfix/fix-description
```

#### 2. Development Cycle
```bash
# Make changes
# Edit files...

# Format code
./gradlew spotlessApply

# Run tests
./gradlew testDebug

# Build project
./gradlew build

# Stage changes
git add .

# Commit changes
git commit -m "feat(scope): description of changes"
```

#### 3. Pre-Push Validation
```bash
# Final validation before push
./gradlew clean build
./gradlew testDebug
./gradlew lint

# Push feature branch
git push -u origin feature/new-feature-name
```

#### 4. Pull Request Process
1. Create PR from feature branch to main
2. Ensure all CI checks pass
3. Request code review
4. Address review feedback
5. Merge after approval

### Code Quality Standards

#### Automated Checks
- **Spotless**: Code formatting
- **Lint**: Code quality and potential issues
- **Tests**: Unit and integration tests
- **Build**: Successful compilation

#### Manual Review Checklist
- [ ] Code follows MVI architecture patterns
- [ ] Proper error handling implemented
- [ ] UI follows Material Design 3 guidelines
- [ ] Tests cover new functionality
- [ ] Documentation updated if needed
- [ ] Performance considerations addressed

### Release Process

#### Version Tagging
```bash
# Create release branch
git checkout -b release/v1.0.0

# Update version in build.gradle.kts
# versionCode = 1
# versionName = "1.0.0"

# Commit version update
git commit -m "chore(release): prepare version 1.0.0"

# Create and push tag
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0

# Merge to main
git checkout main
git merge release/v1.0.0
git push origin main
```

#### Release Notes Template
```markdown
## Release v1.0.0

### 🚀 Features
- Implement MainActivity with MVI architecture
- Add Jetpack Compose UI system
- Support responsive design

### 🐛 Bug Fixes
- Fix crash on app startup
- Resolve navigation issues

### 🔧 Technical
- Upgrade to Kotlin 2.1.10
- Update Compose BOM to 2025.02.00
- Add comprehensive test suite

### 📚 Documentation
- Update architecture documentation
- Add development workflow guide
```

### Continuous Integration

#### Pre-commit Hooks (Recommended)
```bash
# Install pre-commit hooks
cat > .git/hooks/pre-commit << 'EOF'
#!/bin/sh
echo "Running pre-commit checks..."

# Format code
./gradlew spotlessApply

# Run tests
./gradlew testDebug

# Run lint
./gradlew lint

if [ $? -ne 0 ]; then
    echo "❌ Pre-commit checks failed. Please fix issues before committing."
    exit 1
fi

echo "✅ Pre-commit checks passed."
EOF

chmod +x .git/hooks/pre-commit
```

#### GitHub Actions Integration
The project includes automated CI/CD that runs on:
- Every push to main
- Every pull request
- Manual workflow dispatch

**Automated checks include:**
- Build verification
- Unit tests
- Lint analysis
- Screenshot tests (when implemented)
- APK generation

### Emergency Procedures

#### Hotfix Process
```bash
# Create hotfix branch from main
git checkout main
git checkout -b hotfix/critical-fix

# Make minimal fix
# Edit files...

# Test thoroughly
./gradlew testDebug
./gradlew connectedDebugAndroidTest

# Commit with hotfix prefix
git commit -m "hotfix(auth): fix critical security vulnerability"

# Push and create emergency PR
git push -u origin hotfix/critical-fix
```

#### Rollback Process
```bash
# Revert to previous version
git checkout main
git revert HEAD

# Or reset to specific commit
git reset --hard <commit-hash>
git push --force-with-lease origin main
```

## Build Configuration

- **Java Compatibility**: Version 17
- **Kotlin JVM Target**: 17
- **Minification**: Full R8 optimization for release
- **ProGuard**: Multi-layered rules (global, module, scenario-specific)
- **Baseline Profiles**: Automated generation for performance
- **Resource Shrinking**: Enabled for release builds

## Architecture Principles

### Design Principles
- Clear layered architecture
- Unidirectional data flow
- Separation of concerns
- Testability first

### Modularization Best Practices
- Feature-based modularization
- Clear module boundaries
- Avoid circular dependencies
- Convention plugins for configuration reuse

### Modern Android Tech Stack
- Jetpack Compose declarative UI
- Kotlin coroutines async programming
- Hilt dependency injection
- Room database
- DataStore preference storage

## Notes

This project is designed to follow modern Android development best practices with a focus on scalability, maintainability, and performance. The modular architecture allows for independent development and testing of features while maintaining clean separation of concerns.