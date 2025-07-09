# GitHub CI/CD 完整指导手册 - 基于 Now in Android 项目

## 目录
1. [项目概述](#项目概述)
2. [GitHub Actions 工作流程分析](#github-actions-工作流程分析)
3. [Issue 模板系统](#issue-模板系统)
4. [Pull Request 流程](#pull-request-流程)
5. [CI/CD 最佳实践](#cicd-最佳实践)
6. [自动化工具集成](#自动化工具集成)
7. [实施指导](#实施指导)

## 项目概述

Now in Android 项目展示了一套完整的 GitHub CI/CD 实践，包含：
- 自动化构建和测试
- 代码质量检查
- 自动化发布流程
- 完善的 Issue 管理
- 规范化的 PR 流程

## GitHub Actions 工作流程分析

### 1. 主构建工作流 (Build.yaml)

#### 触发条件
```yaml
on:
  workflow_dispatch:     # 手动触发
  push:
    branches:
      - main            # 主分支推送
  pull_request:         # PR 提交时
```

#### 并发控制
```yaml
concurrency:
  group: build-${{ github.ref }}
  cancel-in-progress: true    # 取消正在进行的相同任务
```

#### 主要任务流程

**1. 本地测试和 APK 构建 (test_and_apk)**

**环境配置:**
```yaml
runs-on: ubuntu-latest
permissions:
  contents: write
  pull-requests: write
  security-events: write
timeout-minutes: 60
```

**核心步骤:**

1. **代码检出和环境设置**
   ```yaml
   - name: Checkout
     uses: actions/checkout@v4
   
   - name: Copy CI gradle.properties
     run: mkdir -p ~/.gradle ; cp .github/ci-gradle.properties ~/.gradle/gradle.properties
   
   - name: Set up JDK 17
     uses: actions/setup-java@v4
     with:
       distribution: 'zulu'
       java-version: 17
   ```

2. **Gradle 缓存配置**
   ```yaml
   - name: Setup Gradle
     uses: gradle/actions/setup-gradle@v4
     with:
       cache-encryption-key: ${{ secrets.GRADLE_ENCRYPTION_KEY }}
   ```

3. **代码质量检查**
   ```yaml
   - name: Check build-logic
     run: ./gradlew :build-logic:convention:check
   
   - name: Check spotless
     run: ./gradlew spotlessCheck --init-script gradle/init.gradle.kts --no-configuration-cache
   ```

4. **依赖管理自动化**
   ```yaml
   - name: Check Dependency Guard
     id: dependencyguard_verify
     continue-on-error: true
     run: ./gradlew dependencyGuard
   
   # 自动更新依赖基线
   - name: Generate new Dependency Guard baselines if verification failed and it's a PR
     if: steps.dependencyguard_verify.outcome == 'failure' && github.event_name == 'pull_request'
     run: ./gradlew dependencyGuardBaseline
   ```

5. **截图测试自动化**
   ```yaml
   - name: Run all local screenshot tests (Roborazzi)
     id: screenshotsverify
     continue-on-error: true
     run: ./gradlew verifyRoborazziDemoDebug
   
   # 自动生成新截图
   - name: Generate new screenshots if verification failed and it's a PR
     if: steps.screenshotsverify.outcome == 'failure' && github.event_name == 'pull_request'
     run: ./gradlew recordRoborazziDemoDebug
   ```

6. **自动提交机制**
   ```yaml
   - name: Push new screenshots if available
     uses: stefanzweifel/git-auto-commit-action@v5
     if: steps.screenshotsrecord.outcome == 'success'
     with:
       file_pattern: '*/*.png'
       disable_globbing: true
       commit_message: "🤖 Updates screenshots"
   ```

**2. Android 仪器化测试 (androidTest)**

**矩阵策略:**
```yaml
strategy:
  matrix:
    api-level: [26, 34]    # 多 API 级别测试
```

**核心功能:**
- 模拟器环境配置
- 仪器化测试执行
- 覆盖率报告生成
- 测试结果上传

### 2. 夜间基线配置生成 (NightlyBaselineProfiles.yaml)

**调度配置:**
```yaml
on:
  workflow_dispatch:
  schedule:
    - cron: '42 4 * * *'    # 每天凌晨 4:42 执行
```

**主要功能:**
- 自动生成 Baseline Profiles
- 提升应用启动性能
- 定期优化编译配置

### 3. 发布工作流 (Release.yml)

**触发条件:**
```yaml
on:
  workflow_dispatch:
  push:
    tags:
    - 'v*'                 # 版本标签推送时触发
```

**发布流程:**
- 构建 Release 版本
- 生成基线配置文件
- 创建 GitHub Release
- 上传 APK 文件

## Issue 模板系统

### 1. Bug 报告模板 (bug_report.yml)

**表单结构:**
```yaml
name: Bug Report
description: File a bug report
title: "[Bug]: "
labels: ["bug", "triage me"]
```

**包含字段:**
- 重复性检查
- StackOverflow 搜索确认
- 问题描述
- 日志输出
- 行为准则确认

### 2. 功能请求模板 (feature_request.yml)

**表单字段:**
- 问题描述
- 解决方案
- 附加上下文
- 合规性确认

### 3. 文档问题模板 (docs_issue.yml)

**专门处理:**
- 文档改进建议
- 新页面请求
- 文档错误报告

## Pull Request 流程

### PR 模板要求

**必要步骤:**
1. **首次贡献者**
    - 签署 CLA 协议

2. **代码质量保证**
   ```bash
   ./gradlew testDemoDebug                    # 运行测试
   ./gradlew --init-script gradle/init.gradle.kts spotlessApply  # 格式化代码
   ```

3. **描述要求**
    - 变更内容说明
    - 变更原因
    - 相关 Issue 链接

## CI/CD 最佳实践

### 1. 构建优化策略

**Gradle 配置 (ci-gradle.properties):**
```properties
org.gradle.daemon=false              # CI 环境禁用守护进程
org.gradle.parallel=true             # 启用并行构建
org.gradle.workers.max=2             # 限制工作线程数
org.gradle.configuration-cache=true  # 启用配置缓存
kotlin.incremental=false             # CI 环境禁用增量编译
```

### 2. 安全实践

**权限最小化:**
```yaml
permissions:
  contents: write           # 仅必要的写权限
  pull-requests: write      # PR 评论权限
  security-events: write    # 安全事件上报
```

**密钥管理:**
- 使用 GitHub Secrets 存储敏感信息
- Gradle 缓存加密
- 避免在日志中暴露敏感数据

### 3. 性能优化

**缓存策略:**
- Gradle 构建缓存
- 依赖包缓存
- Android SDK 缓存

**资源管理:**
```yaml
- name: Delete unnecessary tools 🔧
  uses: jlumbroso/free-disk-space@v1.3.1
  with:
    tool-cache: true
    dotnet: true
    haskell: true
    swap-storage: true
```

## 自动化工具集成

### 1. 代码质量工具

**Spotless (代码格式化):**
```bash
./gradlew spotlessCheck     # 检查格式
./gradlew spotlessApply     # 自动格式化
```

**Lint 检查:**
```bash
./gradlew :app:lintProdRelease
./gradlew :app-nia-catalog:lintRelease
```

### 2. 依赖管理

**Dependency Guard:**
- 自动检测依赖变更
- 自动更新基线文件
- 防止意外依赖引入

**Renovate Bot:**
```json
{
  "extends": ["local>android/.github:renovate-config"],
  "baseBranches": ["main"],
  "gitIgnoredAuthors": [
    "renovate[bot]@users.noreply.github.com"
  ]
}
```

### 3. 测试自动化

**截图测试 (Roborazzi):**
- 自动生成 UI 截图
- 回归测试检测
- 跨设备兼容性验证

**覆盖率报告:**
```yaml
- name: Display local test coverage
  uses: madrapps/jacoco-report@v1.7.1
  with:
    title: Combined test coverage report
    min-coverage-overall: 40
    min-coverage-changed-files: 60
```

## 实施指导

### 1. 项目初始化

**1. 创建 .github 目录结构:**
```
.github/
├── workflows/
│   ├── build.yaml
│   ├── release.yml
│   └── nightly.yaml
├── ISSUE_TEMPLATE/
│   ├── bug_report.yml
│   ├── feature_request.yml
│   └── docs_issue.yml
├── pull_request_template.md
├── ci-gradle.properties
└── renovate.json
```

**2. 配置必要的 Secrets:**
- `GRADLE_ENCRYPTION_KEY`: Gradle 缓存加密密钥
- `GITHUB_TOKEN`: GitHub API 访问令牌

### 2. 工作流配置

**基础构建工作流:**
```yaml
name: CI
on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: 'zulu'
          java-version: 17
      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v4
      - name: Run tests
        run: ./gradlew test
```

### 3. 质量检查集成

**代码格式化检查:**
```yaml
- name: Check code formatting
  run: ./gradlew spotlessCheck
```

**Lint 检查:**
```yaml
- name: Run lint
  run: ./gradlew lint
```

### 4. 自动化发布

**标签触发发布:**
```yaml
name: Release
on:
  push:
    tags:
      - 'v*'
jobs:
  release:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Build release
        run: ./gradlew assembleRelease
      - name: Create release
        uses: actions/create-release@v1
        with:
          tag_name: ${{ github.ref }}
          release_name: Release ${{ github.ref }}
```

## 高级功能

### 1. 矩阵构建

**多环境测试:**
```yaml
strategy:
  matrix:
    os: [ubuntu-latest, macos-latest, windows-latest]
    java-version: [11, 17]
```

### 2. 条件执行

**基于变更文件的条件执行:**
```yaml
- name: Check if Android files changed
  uses: dorny/paths-filter@v2
  id: changes
  with:
    filters: |
      android:
        - 'app/**'
        - 'gradle/**'
        - '*.gradle*'
```

### 3. 并行任务

**依赖关系管理:**
```yaml
jobs:
  test:
    runs-on: ubuntu-latest
  
  lint:
    runs-on: ubuntu-latest
  
  build:
    needs: [test, lint]    # 依赖测试和 lint 任务
    runs-on: ubuntu-latest
```

## 监控和维护

### 1. 工作流监控

**关键指标:**
- 构建成功率
- 构建时间
- 测试覆盖率
- 依赖更新频率

### 2. 维护任务

**定期检查:**
- Action 版本更新
- 依赖包安全性
- 构建性能优化
- 缓存效率分析

### 3. 故障排查

**常见问题:**
- 构建超时处理
- 缓存失效问题
- 权限配置错误
- 环境不一致性

## 总结

Now in Android 项目展示了现代 Android 项目的完整 CI/CD 实践，包括：

1. **完整的构建流水线**: 从代码检查到发布的全自动化流程
2. **质量保证体系**: 多层次的代码质量检查和测试
3. **自动化维护**: 依赖更新、截图测试、基线配置等自动化
4. **规范化流程**: 标准化的 Issue 和 PR 管理
5. **性能优化**: 构建缓存、并行执行、资源管理等优化策略

这套 CI/CD 体系可以直接应用到其他 Android 项目中，提供了可靠、高效、可维护的开发和发布流程。

---
*文档生成时间: 2025-07-08*
*基于 Now in Android 项目最新 CI/CD 配置*