# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is an Android application project named "crd" (package: `com.crtyiot.crd`) built with modern Android architecture using Kotlin and Jetpack Compose. The project follows a modular design with Clean Architecture principles.

### Target Architecture
- **Language**: Kotlin 2.1.10
- **UI Framework**: Jetpack Compose (BOM 2025.02.00)
- **Architecture**: MVVM + Clean Architecture
- **Dependency Injection**: Hilt
- **Network**: Retrofit + OkHttp
- **Database**: Room
- **Async Processing**: Kotlin Coroutines + Flow
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

### Layered Architecture
The project follows a three-layer architecture pattern:

```
UI Layer (Compose + ViewModels)
    ↓
Domain Layer (Use Cases)
    ↓
Data Layer (Repositories + Data Sources)
```

**Data Flow**:
- Events flow down
- Data flows up
- Reactive programming model (Kotlin Flow)

### Modular Design

#### Core Modules (core/)
- `core:analytics` - Analytics and statistics
- `core:common` - Common utilities and classes
- `core:data` - Data layer implementation
- `core:database` - Room database
- `core:datastore` - User preference storage
- `core:designsystem` - Design system and UI components
- `core:domain` - Business logic use cases
- `core:model` - Data models
- `core:network` - Network layer
- `core:ui` - Reusable UI components

#### Feature Modules (feature/)
- `feature:xxx` - Feature-specific implementations (xxx = feature name)

#### Testing and Tools
- `core:testing` - Testing utilities and mock data
- `benchmarks` - Performance benchmarks
- `lint` - Custom lint rules

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

### Local Development
```bash
# Clone project
git clone [repository-url]

# Run demo version (recommended)
./gradlew :app:assembleDemoDebug

# Run tests
./gradlew testDemoDebug
./gradlew recordRoborazziDemoDebug  # Record screenshots

# Code quality checks
./gradlew spotlessCheck
./gradlew dependencyGuard
./gradlew lint
```

### Branch Strategy
- `main`: Main branch, protected
- Feature branches: Feature development
- PR requirements: All CI checks must pass

### Pre-commit Requirements
```bash
# Required before committing
./gradlew spotlessApply           # Format code
./gradlew testDemoDebug          # Run tests
./gradlew lint                   # Check code quality
./gradlew dependencyGuard        # Verify dependencies
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