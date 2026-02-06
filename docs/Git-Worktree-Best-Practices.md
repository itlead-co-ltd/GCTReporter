# Git Worktree 最佳实践指南

> **文档类型**: 开发工具使用指南  
> **适用项目**: GCT Reporter  
> **最后更新**: 2026-01-16

---

## 📋 目录

- [什么是Git Worktree](#什么是git-worktree)
- [为什么使用Worktree](#为什么使用worktree)
- [目录结构设计](#目录结构设计)
- [初始化设置](#初始化设置)
- [典型使用场景](#典型使用场景)
- [最佳实践](#最佳实践)
- [快捷脚本](#快捷脚本)
- [常见问题](#常见问题)
- [与分支保护配合](#与分支保护配合)

---

## 什么是Git Worktree

Git Worktree允许你在同一个仓库中同时检出**多个分支**到不同的目录，每个目录称为一个"工作树"。

### 传统方式 vs Worktree

```bash
# ❌ 传统方式：频繁切换分支
git checkout feature-A    # 开发功能A
git checkout main         # 临时查看main代码
git checkout feature-A    # 回到功能A
git stash                 # 保存未提交的改动
git checkout bugfix-B     # 修复紧急Bug
git stash pop             # 恢复改动

# ✅ Worktree方式：并行工作
GCTReporter/             (main分支)      - 只读，查看代码
GCTReporter-worktrees/
  ├── feature-A/         (功能A开发)     - VS Code实例1
  └── bugfix-B/          (Bug修复)       - VS Code实例2
```

### 核心优势

1. **多任务并行**：同时开发多个功能，无需切换分支
2. **环境隔离**：每个worktree有独立的工作目录、依赖、构建产物
3. **快速切换**：通过目录切换代替分支切换，保留所有状态
4. **独立运行**：前后端可以在不同分支同时启动服务
5. **代码审查友好**：Review PR时不影响当前开发

---

## 为什么使用Worktree

### 解决的痛点

#### 痛点1：频繁切换分支导致状态丢失

```bash
# 传统方式：正在开发功能A
git checkout feature-A
# 编辑了10个文件，还没提交...

# 突然需要紧急修复Bug
git stash save "feature-A WIP"
git checkout main
git checkout -b hotfix/critical-fix
# 修复Bug、测试、提交...

# 回到功能A
git checkout feature-A
git stash pop  # ❌ 可能有冲突！
```

**使用Worktree后**：
```bash
# 主工作树：一直在feature-A开发
cd GCTReporter/

# 创建新worktree修复Bug（不影响当前工作）
git worktree add ../GCTReporter-worktrees/hotfix hotfix/critical-fix
cd ../GCTReporter-worktrees/hotfix
# 修复Bug、测试、提交...

# 直接回到feature-A继续开发（状态完全保留）
cd ../../GCTReporter/
```

#### 痛点2：无法并行运行前后端

```bash
# 传统方式：只能运行一个分支的代码
cd frontend/
npm run dev  # 前端运行在localhost:5173

# ❌ 想同时测试另一个分支的后端，必须停止前端
```

**使用Worktree后**：
```powershell
# 终端1：主工作树运行前端
cd GCTReporter/frontend
npm run dev  # localhost:5173

# 终端2：worktree运行后端（不同分支）
cd GCTReporter-worktrees/feature-US010/backend
mvn spring-boot:run  # localhost:8080

# 终端3：另一个worktree测试新版本
cd GCTReporter-worktrees/feature-US011/frontend
npm run dev -- --port 5174  # localhost:5174
```

#### 痛点3：Code Review打断当前开发

```bash
# 传统方式：正在开发feature-A
git checkout feature-A
# 编辑中...

# 同事请求Review PR（feature-B分支）
git stash
git checkout feature-B
# Review、测试、添加评论...

git checkout feature-A
git stash pop
# ❌ 心流被打断，需要重新进入状态
```

**使用Worktree后**：
```powershell
# 主工作树：继续在feature-A开发
cd GCTReporter/

# 新终端：创建临时worktree进行Review
git worktree add ../GCTReporter-worktrees/review-feature-B feature-B
cd ../GCTReporter-worktrees/review-feature-B
code .  # 新VS Code窗口
mvn test

# Review完成后删除worktree，回到主工作树继续开发
# 完全不打断当前工作流
```

---

## 目录结构设计

### 推荐结构

```
d:\Programs\
├── GCTReporter\                 # ⭐ 主工作树（main分支）
│   ├── .git\                   # Git仓库（唯一）
│   ├── backend\
│   ├── frontend\
│   └── docs\
│
└── GCTReporter-worktrees\       # 📁 所有worktree集中目录
    ├── develop\                # 长期分支worktree
    ├── feature-US010\          # 功能开发
    ├── feature-US011\          # 并行功能开发
    ├── bugfix-timeout\         # Bug修复
    ├── hotfix-security\        # 紧急修复
    └── review-pr-123\          # 临时Code Review
```

### 目录命名规范

```bash
# 功能分支worktree
feature-<Story编号>           # feature-US010
feature-<描述>                # feature-excel-export

# Bug修复worktree
bugfix-<描述>                 # bugfix-query-timeout
bugfix-issue-<编号>           # bugfix-issue-123

# 紧急修复worktree
hotfix-<描述>                 # hotfix-sql-injection

# 临时Review worktree
review-<分支名>               # review-feature-US011
review-pr-<编号>              # review-pr-123

# 长期分支worktree
develop                       # develop分支
release-<版本>                # release-v1.0.0
```

---

## 初始化设置

### 前置条件

```powershell
# 1. 确保Git版本支持worktree（Git 2.5+）
git --version  # 应该 >= 2.5.0

# 2. 确保当前在Git仓库中
cd d:\Programs\GCTReporter
git status
```

### 初始化步骤

```powershell
# 步骤1: 确保主工作树在main分支（只读模式）
git checkout main
git pull origin main

# 步骤2: 创建worktree集中目录
mkdir ..\GCTReporter-worktrees

# 步骤3: 创建develop分支worktree（推荐）
# develop作为日常开发的主要工作区
git worktree add ..\GCTReporter-worktrees\develop develop

# 步骤4: 验证worktree创建成功
git worktree list

# 输出示例：
# d:/Programs/GCTReporter                        (main)
# d:/Programs/GCTReporter-worktrees/develop      (develop)
```

### 首次使用建议

```powershell
# 将develop worktree设为主要开发环境
cd ..\GCTReporter-worktrees\develop
code .  # 在VS Code中打开

# 主工作树（main分支）仅用于：
# - 查看最新发布代码
# - 同步更新
# - 管理worktree（创建/删除）
```

---

## 典型使用场景

### 场景1: 开发新功能

```powershell
# 当前位置：任意目录
# 目标：开发Story US010 - SQL编辑器功能

# 步骤1: 进入主仓库
cd d:\Programs\GCTReporter

# 步骤2: 同步最新代码
git checkout develop
git pull origin develop

# 步骤3: 创建功能分支worktree
git worktree add ..\GCTReporter-worktrees\feature-US010 -b feature/US010-sql-editor develop

# 步骤4: 进入worktree开发
cd ..\GCTReporter-worktrees\feature-US010
code .  # 在VS Code中打开

# 步骤5: 开发、提交
git add .
git commit -m "feat(report): 添加SQL编辑器组件"
git commit -m "feat(report): 实现语法高亮"
git commit -m "test(report): 添加编辑器单元测试"

# 步骤6: 推送到远程
git push origin feature/US010-sql-editor

# 步骤7: 在GitHub创建PR
# feature/US010-sql-editor -> develop

# 步骤8: PR合并后清理worktree
cd d:\Programs\GCTReporter
git worktree remove ..\GCTReporter-worktrees\feature-US010
git worktree prune
git branch -d feature/US010-sql-editor
```

---

### 场景2: 紧急修复生产Bug

```powershell
# ⚠️ 生产环境Bug必须基于main分支修复

# 步骤1: 进入主仓库
cd d:\Programs\GCTReporter

# 步骤2: 确保main是最新的
git checkout main
git pull origin main

# 步骤3: 创建hotfix worktree（基于main）
git worktree add ..\GCTReporter-worktrees\hotfix-sql-injection -b hotfix/SQL-injection-fix main

# 步骤4: 进入hotfix worktree修复
cd ..\GCTReporter-worktrees\hotfix-sql-injection

# 步骤5: 修复Bug、测试
# 修改代码...
mvn test  # 确保测试通过

git add .
git commit -m "fix(security): 修复SQL注入漏洞

- 强制使用参数化查询
- 添加SQL关键字黑名单校验
- 添加SQL注入安全测试

Fixes #456"

# 步骤6: 推送并创建PR到main
git push origin hotfix/SQL-injection-fix

# 在GitHub创建PR: hotfix/SQL-injection-fix -> main

# 步骤7: PR合并到main后，cherry-pick到develop
cd d:\Programs\GCTReporter
git checkout develop
git pull origin develop
git cherry-pick <hotfix-commit-hash>
git push origin develop

# 步骤8: 清理hotfix worktree
git worktree remove ..\GCTReporter-worktrees\hotfix-sql-injection
git branch -d hotfix/SQL-injection-fix
```

---

### 场景3: Code Review不打断当前开发

```powershell
# 当前状态：正在feature-US010开发（未完成、未提交）
cd d:\Programs\GCTReporter-worktrees\feature-US010

# 同事请求Review PR: feature/US011-export

# 步骤1: 新开终端，进入主仓库
cd d:\Programs\GCTReporter

# 步骤2: 获取远程分支
git fetch origin

# 步骤3: 创建临时Review worktree
git worktree add ..\GCTReporter-worktrees\review-US011 feature/US011-export

# 步骤4: 进入Review worktree
cd ..\GCTReporter-worktrees\review-US011

# 步骤5: 运行测试、查看代码
mvn test
code .  # 新VS Code窗口

# 步骤6: 在GitHub PR页面添加Review评论

# 步骤7: Review完成，删除临时worktree
cd d:\Programs\GCTReporter
git worktree remove ..\GCTReporter-worktrees\review-US011

# 步骤8: 回到原来的开发（状态完全保留）
cd ..\GCTReporter-worktrees\feature-US010
# 继续开发，完全不受影响
```

---

### 场景4: 前后端并行开发不同分支

```powershell
# 场景：前端开发feature-A，后端开发feature-B，需要联调

# 终端1: 创建前端功能分支worktree
cd d:\Programs\GCTReporter
git worktree add ..\GCTReporter-worktrees\frontend-US010 -b feature/US010-frontend
cd ..\GCTReporter-worktrees\frontend-US010\frontend
npm install
npm run dev  # 运行在 localhost:5173

# 终端2: 创建后端功能分支worktree
cd d:\Programs\GCTReporter
git worktree add ..\GCTReporter-worktrees\backend-US011 -b feature/US011-backend
cd ..\GCTReporter-worktrees\backend-US011\backend
mvn clean install
mvn spring-boot:run  # 运行在 localhost:8080

# 现在前后端在不同分支同时运行，可以独立开发和测试
```

---

### 场景5: 多个功能并行开发

```powershell
# Sprint中有3个Story需要并行开发

# 功能1: SQL编辑器
git worktree add ..\GCTReporter-worktrees\feature-US010 -b feature/US010-sql-editor
# VS Code窗口1

# 功能2: Excel导出
git worktree add ..\GCTReporter-worktrees\feature-US011 -b feature/US011-excel-export
# VS Code窗口2

# 功能3: 权限管理
git worktree add ..\GCTReporter-worktrees\feature-US012 -b feature/US012-permission
# VS Code窗口3

# 查看所有worktree
git worktree list

# 在不同窗口并行开发，互不干扰
# 各自提交、推送、创建PR
```

---

## 最佳实践

### ✅ 推荐做法

#### 1. 主工作树保持干净

```powershell
# 主工作树（main分支）作为：
# ✅ 只读参考：查看最新发布代码
# ✅ 管理中心：创建/删除worktree
# ✅ 同步枢纽：拉取最新代码
# ❌ 不要直接开发：所有开发在worktree中进行

# 如果意外在main修改了代码
cd d:\Programs\GCTReporter
git status  # 发现有未提交的改动
git stash   # 暂存改动
# 创建worktree并恢复改动
git worktree add ..\GCTReporter-worktrees\temp-fix -b feature/temp-fix
cd ..\GCTReporter-worktrees\temp-fix
git stash pop
```

#### 2. 统一worktree存放位置

```powershell
# ✅ 推荐：所有worktree集中管理
GCTReporter-worktrees/
  ├── develop/
  ├── feature-US010/
  ├── feature-US011/
  └── bugfix-timeout/

# ❌ 不推荐：worktree分散各处
d:/Projects/feature-A/
d:/Temp/bugfix-B/
d:/Desktop/worktree-C/
# 难以管理，容易遗忘
```

#### 3. 及时清理已合并的worktree

```powershell
# PR合并后立即清理worktree
cd d:\Programs\GCTReporter
git worktree remove ..\GCTReporter-worktrees\feature-US010

# 定期检查并清理悬空引用
git worktree prune

# 查看所有worktree
git worktree list

# 批量清理（PowerShell脚本）
Get-ChildItem ..\GCTReporter-worktrees\ | ForEach-Object {
    $worktree = $_.Name
    Write-Host "检查 $worktree ..." -ForegroundColor Cyan
    cd ..\GCTReporter-worktrees\$worktree
    git status
}
```

#### 4. worktree命名与分支名保持一致

```powershell
# ✅ 推荐：一目了然
git worktree add ../worktrees/feature-US010 -b feature/US010-sql-editor
# 目录名: feature-US010
# 分支名: feature/US010-sql-editor

# ❌ 不推荐：难以识别
git worktree add ../worktrees/temp1 -b feature/US010-sql-editor
```

#### 5. 在主仓库管理所有worktree

```powershell
# ✅ 正确：始终在主仓库操作
cd d:\Programs\GCTReporter
git worktree add ..\GCTReporter-worktrees\feature-A -b feature/A
git worktree remove ..\GCTReporter-worktrees\feature-A

# ❌ 错误：在worktree中管理其他worktree
cd d:\Programs\GCTReporter-worktrees\feature-A
git worktree add ...  # ❌ 不要这样做
```

#### 6. 使用脚本自动化常见操作

参见 [快捷脚本](#快捷脚本) 章节。

---

### ❌ 避免的陷阱

#### 陷阱1: 在主工作树目录内创建worktree

```powershell
# ❌ 错误：在主仓库内创建worktree
cd d:\Programs\GCTReporter
git worktree add .\worktree-feature-A -b feature/A
# 导致目录混乱，.gitignore可能失效

# ✅ 正确：在外部目录创建
git worktree add ..\GCTReporter-worktrees\feature-A -b feature/A
```

#### 陷阱2: 忘记删除已合并的worktree

```powershell
# ❌ 问题：3个月后磁盘占用30GB
GCTReporter-worktrees/
  ├── feature-US001/  # 已合并2个月
  ├── feature-US002/  # 已合并1个月
  ├── feature-US003/  # 已合并3周
  ...
  └── feature-US050/  # 当前开发

# ✅ 解决：定期清理
git worktree remove ..\GCTReporter-worktrees\feature-US001
git worktree prune
```

#### 陷阱3: 在不同worktree修改同一个文件

```powershell
# ⚠️ 警告：容易产生冲突
# Worktree A: 修改了 ReportService.java
cd GCTReporter-worktrees/feature-A
# 编辑 ReportService.java
git commit -m "feat: 添加功能A"

# Worktree B: 也修改了 ReportService.java
cd GCTReporter-worktrees/feature-B
# 编辑 ReportService.java
git commit -m "feat: 添加功能B"

# 合并时会冲突！

# ✅ 建议：
# 1. 功能间尽量减少耦合
# 2. 如需修改公共代码，先合并一个分支，再基于新base开发另一个
```

#### 陷阱4: 忘记同步基础分支

```powershell
# ❌ 错误：创建worktree时develop不是最新
cd d:\Programs\GCTReporter
git worktree add ..\GCTReporter-worktrees\feature-A -b feature/A develop
# develop可能落后origin/develop很多提交

# ✅ 正确：先同步再创建
git checkout develop
git pull origin develop
git worktree add ..\GCTReporter-worktrees\feature-A -b feature/A develop
```

#### 陷阱5: 在worktree中直接checkout到其他分支

```powershell
# ❌ 错误：在worktree中切换到已存在的分支
cd GCTReporter-worktrees/feature-A
git checkout main  # ❌ 错误！main已在主工作树

# Git错误信息：
# fatal: 'main' is already checked out at 'd:/Programs/GCTReporter'

# ✅ 正确：每个分支只能在一个worktree中
# 如需切换，删除当前worktree，创建新worktree
```

---

## 快捷脚本

### PowerShell脚本集

#### 脚本1: 创建功能分支worktree

文件：`scripts/new-feature.ps1`

```powershell
<#
.SYNOPSIS
创建新功能分支的worktree

.DESCRIPTION
从develop分支创建新的功能分支worktree，自动命名并打开VS Code

.PARAMETER StoryId
User Story编号（如US010）

.PARAMETER Description
功能简短描述（如sql-editor）

.PARAMETER BaseBranch
基础分支，默认为develop

.EXAMPLE
.\new-feature.ps1 -StoryId "US010" -Description "sql-editor"

.EXAMPLE
.\new-feature.ps1 -StoryId "US011" -Description "excel-export" -BaseBranch "main"
#>

param(
    [Parameter(Mandatory=$true)]
    [string]$StoryId,
    
    [Parameter(Mandatory=$true)]
    [string]$Description,
    
    [string]$BaseBranch = "develop",
    
    [switch]$NoVSCode
)

# 配置
$MainRepo = "d:\Programs\GCTReporter"
$WorktreeBase = "d:\Programs\GCTReporter-worktrees"

# 生成分支名和worktree路径
$BranchName = "feature/$StoryId-$Description"
$WorktreeName = "feature-$StoryId"
$WorktreePath = "$WorktreeBase\$WorktreeName"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "创建功能分支Worktree" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# 检查主仓库
if (-not (Test-Path $MainRepo)) {
    Write-Host "❌ 错误: 主仓库不存在 $MainRepo" -ForegroundColor Red
    exit 1
}

# 进入主仓库
Set-Location $MainRepo

# 同步基础分支
Write-Host "`n📥 同步基础分支: $BaseBranch ..." -ForegroundColor Yellow
git checkout $BaseBranch
git pull origin $BaseBranch

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ 错误: 同步基础分支失败" -ForegroundColor Red
    exit 1
}

# 创建worktree
Write-Host "`n🌳 创建worktree: $WorktreeName ..." -ForegroundColor Yellow
git worktree add $WorktreePath -b $BranchName $BaseBranch

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ 错误: 创建worktree失败" -ForegroundColor Red
    exit 1
}

# 进入worktree
Set-Location $WorktreePath

# 显示信息
Write-Host "`n========================================" -ForegroundColor Green
Write-Host "✅ Worktree创建成功！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host "📁 工作目录: $WorktreePath" -ForegroundColor Cyan
Write-Host "🌿 分支名称: $BranchName" -ForegroundColor Cyan
Write-Host "📌 基础分支: $BaseBranch" -ForegroundColor Cyan

# 打开VS Code
if (-not $NoVSCode) {
    Write-Host "`n🚀 正在打开VS Code..." -ForegroundColor Yellow
    code .
}

Write-Host "`n💡 接下来的步骤:" -ForegroundColor Cyan
Write-Host "1. 开发功能并提交" -ForegroundColor White
Write-Host "   git add ." -ForegroundColor Gray
Write-Host "   git commit -m `"feat($StoryId): 功能描述`"" -ForegroundColor Gray
Write-Host "`n2. 推送到远程" -ForegroundColor White
Write-Host "   git push origin $BranchName" -ForegroundColor Gray
Write-Host "`n3. 在GitHub创建PR" -ForegroundColor White
Write-Host "   $BranchName -> develop" -ForegroundColor Gray
Write-Host "`n4. PR合并后清理worktree" -ForegroundColor White
Write-Host "   cd $MainRepo" -ForegroundColor Gray
Write-Host "   .\scripts\cleanup-worktree.ps1 -WorktreeName `"$WorktreeName`"" -ForegroundColor Gray
```

#### 脚本2: 清理worktree

文件：`scripts/cleanup-worktree.ps1`

```powershell
<#
.SYNOPSIS
删除指定的worktree

.PARAMETER WorktreeName
Worktree目录名称

.PARAMETER Force
强制删除（即使有未提交的改动）

.EXAMPLE
.\cleanup-worktree.ps1 -WorktreeName "feature-US010"

.EXAMPLE
.\cleanup-worktree.ps1 -WorktreeName "feature-US010" -Force
#>

param(
    [Parameter(Mandatory=$true)]
    [string]$WorktreeName,
    
    [switch]$Force
)

$MainRepo = "d:\Programs\GCTReporter"
$WorktreeBase = "d:\Programs\GCTReporter-worktrees"
$WorktreePath = "$WorktreeBase\$WorktreeName"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "清理Worktree" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# 检查worktree是否存在
if (-not (Test-Path $WorktreePath)) {
    Write-Host "⚠️  警告: Worktree不存在 $WorktreePath" -ForegroundColor Yellow
    Write-Host "`n当前存在的worktrees:" -ForegroundColor Cyan
    Set-Location $MainRepo
    git worktree list
    exit 0
}

# 进入主仓库
Set-Location $MainRepo

# 检查未提交的改动
Write-Host "`n🔍 检查未提交的改动..." -ForegroundColor Yellow
Set-Location $WorktreePath
$status = git status --porcelain

if ($status -and -not $Force) {
    Write-Host "⚠️  警告: Worktree有未提交的改动！" -ForegroundColor Yellow
    Write-Host "`n未提交的文件:" -ForegroundColor Yellow
    git status --short
    
    Write-Host "`n请选择操作:" -ForegroundColor Cyan
    Write-Host "1. 提交改动后再删除" -ForegroundColor White
    Write-Host "2. 放弃改动并强制删除（使用 -Force 参数）" -ForegroundColor White
    Write-Host "3. 取消删除" -ForegroundColor White
    
    exit 1
}

# 删除worktree
Set-Location $MainRepo
Write-Host "`n🗑️  删除worktree: $WorktreeName ..." -ForegroundColor Yellow

if ($Force) {
    git worktree remove -f $WorktreePath
} else {
    git worktree remove $WorktreePath
}

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ 错误: 删除worktree失败" -ForegroundColor Red
    exit 1
}

# 清理引用
Write-Host "`n🧹 清理悬空引用..." -ForegroundColor Yellow
git worktree prune

# 显示结果
Write-Host "`n========================================" -ForegroundColor Green
Write-Host "✅ Worktree已删除！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

Write-Host "`n📋 当前存在的worktrees:" -ForegroundColor Cyan
git worktree list
```

#### 脚本3: 列出所有worktree

文件：`scripts/list-worktrees.ps1`

```powershell
<#
.SYNOPSIS
列出所有worktree及其状态
#>

param(
    [switch]$ShowStatus
)

$MainRepo = "d:\Programs\GCTReporter"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "GCT Reporter - Worktree列表" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

Set-Location $MainRepo

# 获取worktree列表
$worktrees = git worktree list --porcelain

Write-Host "`n📋 所有Worktree:" -ForegroundColor Yellow
git worktree list

if ($ShowStatus) {
    Write-Host "`n📊 详细状态:" -ForegroundColor Yellow
    
    # 解析worktree信息
    $currentWorktree = $null
    $worktreeList = @()
    
    foreach ($line in $worktrees) {
        if ($line -match "^worktree (.+)$") {
            if ($currentWorktree) {
                $worktreeList += $currentWorktree
            }
            $currentWorktree = @{
                Path = $matches[1]
                Branch = ""
                Commit = ""
            }
        } elseif ($line -match "^branch (.+)$") {
            $currentWorktree.Branch = $matches[1]
        } elseif ($line -match "^HEAD (.+)$") {
            $currentWorktree.Commit = $matches[1].Substring(0, 7)
        }
    }
    if ($currentWorktree) {
        $worktreeList += $currentWorktree
    }
    
    # 显示详细信息
    foreach ($wt in $worktreeList) {
        Write-Host "`n📁 $($wt.Path)" -ForegroundColor Cyan
        Write-Host "   🌿 分支: $($wt.Branch)" -ForegroundColor White
        Write-Host "   📌 提交: $($wt.Commit)" -ForegroundColor Gray
        
        if (Test-Path $wt.Path) {
            Push-Location $wt.Path
            $status = git status --short
            if ($status) {
                Write-Host "   ⚠️  有未提交的改动" -ForegroundColor Yellow
            } else {
                Write-Host "   ✅ 工作区干净" -ForegroundColor Green
            }
            Pop-Location
        }
    }
}

Write-Host "`n💡 提示:" -ForegroundColor Cyan
Write-Host "创建新worktree: .\scripts\new-feature.ps1 -StoryId `"US010`" -Description `"sql-editor`"" -ForegroundColor Gray
Write-Host "删除worktree:    .\scripts\cleanup-worktree.ps1 -WorktreeName `"feature-US010`"" -ForegroundColor Gray
Write-Host "查看详细状态:    .\scripts\list-worktrees.ps1 -ShowStatus" -ForegroundColor Gray
```

#### 脚本4: 创建Code Review worktree

文件：`scripts/new-review.ps1`

```powershell
<#
.SYNOPSIS
创建临时Code Review worktree

.PARAMETER BranchName
要Review的分支名称

.PARAMETER PRNumber
Pull Request编号（可选）

.EXAMPLE
.\new-review.ps1 -BranchName "feature/US011-export"

.EXAMPLE
.\new-review.ps1 -PRNumber 123
#>

param(
    [Parameter(Mandatory=$false)]
    [string]$BranchName,
    
    [Parameter(Mandatory=$false)]
    [int]$PRNumber
)

$MainRepo = "d:\Programs\GCTReporter"
$WorktreeBase = "d:\Programs\GCTReporter-worktrees"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "创建Code Review Worktree" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# 参数验证
if (-not $BranchName -and -not $PRNumber) {
    Write-Host "❌ 错误: 必须提供 -BranchName 或 -PRNumber 参数" -ForegroundColor Red
    exit 1
}

# 进入主仓库
Set-Location $MainRepo

# 获取最新的远程分支
Write-Host "`n📥 获取远程分支..." -ForegroundColor Yellow
git fetch origin

# 确定分支名
if ($PRNumber) {
    # 从PR编号获取分支名（需要GitHub CLI）
    if (Get-Command gh -ErrorAction SilentlyContinue) {
        $BranchName = gh pr view $PRNumber --json headRefName -q .headRefName
        Write-Host "PR #$PRNumber 的分支: $BranchName" -ForegroundColor Cyan
    } else {
        Write-Host "❌ 错误: 需要安装GitHub CLI (gh) 来通过PR编号获取分支" -ForegroundColor Red
        Write-Host "请使用 -BranchName 参数手动指定分支" -ForegroundColor Yellow
        exit 1
    }
}

# 生成worktree名称
$WorktreeName = if ($PRNumber) { "review-pr-$PRNumber" } else { "review-$(Split-Path $BranchName -Leaf)" }
$WorktreePath = "$WorktreeBase\$WorktreeName"

# 检查worktree是否已存在
if (Test-Path $WorktreePath) {
    Write-Host "⚠️  警告: Review worktree已存在: $WorktreeName" -ForegroundColor Yellow
    Write-Host "是否删除并重新创建? (Y/N)" -ForegroundColor Cyan
    $confirm = Read-Host
    if ($confirm -eq "Y" -or $confirm -eq "y") {
        git worktree remove -f $WorktreePath
    } else {
        exit 0
    }
}

# 创建worktree
Write-Host "`n🌳 创建Review worktree..." -ForegroundColor Yellow
git worktree add $WorktreePath $BranchName

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ 错误: 创建worktree失败" -ForegroundColor Red
    exit 1
}

# 进入worktree
Set-Location $WorktreePath

# 运行测试
Write-Host "`n🧪 运行测试..." -ForegroundColor Yellow
if (Test-Path "backend/pom.xml") {
    cd backend
    mvn test
    cd ..
}

if (Test-Path "frontend/package.json") {
    cd frontend
    npm test
    cd ..
}

# 显示信息
Write-Host "`n========================================" -ForegroundColor Green
Write-Host "✅ Review Worktree创建成功！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host "📁 工作目录: $WorktreePath" -ForegroundColor Cyan
Write-Host "🌿 分支名称: $BranchName" -ForegroundColor Cyan
if ($PRNumber) {
    Write-Host "🔗 PR编号: #$PRNumber" -ForegroundColor Cyan
}

Write-Host "`n💡 Review完成后删除worktree:" -ForegroundColor Cyan
Write-Host "   cd $MainRepo" -ForegroundColor Gray
Write-Host "   .\scripts\cleanup-worktree.ps1 -WorktreeName `"$WorktreeName`"" -ForegroundColor Gray

# 打开VS Code
Write-Host "`n🚀 正在打开VS Code..." -ForegroundColor Yellow
code .
```

---

### 安装脚本

```powershell
# 创建scripts目录
cd d:\Programs\GCTReporter
mkdir scripts -Force

# 将上述4个脚本保存到scripts目录
# - new-feature.ps1
# - cleanup-worktree.ps1
# - list-worktrees.ps1
# - new-review.ps1

# 设置执行权限
Set-ExecutionPolicy -Scope CurrentUser -ExecutionPolicy RemoteSigned -Force
```

### 使用示例

```powershell
# 1. 创建功能分支
cd d:\Programs\GCTReporter
.\scripts\new-feature.ps1 -StoryId "US010" -Description "sql-editor"

# 2. 列出所有worktree
.\scripts\list-worktrees.ps1

# 3. 列出详细状态
.\scripts\list-worktrees.ps1 -ShowStatus

# 4. 创建Review worktree
.\scripts\new-review.ps1 -BranchName "feature/US011-export"
.\scripts\new-review.ps1 -PRNumber 123  # 需要GitHub CLI

# 5. 清理worktree
.\scripts\cleanup-worktree.ps1 -WorktreeName "feature-US010"

# 6. 强制删除worktree（有未提交改动）
.\scripts\cleanup-worktree.ps1 -WorktreeName "feature-US010" -Force
```

---

## 常见问题

### Q1: 如何查看所有worktree？

```powershell
cd d:\Programs\GCTReporter
git worktree list

# 输出示例：
# d:/Programs/GCTReporter                        main
# d:/Programs/GCTReporter-worktrees/develop      develop
# d:/Programs/GCTReporter-worktrees/feature-US010 feature/US010-sql-editor
```

### Q2: 如何在worktree之间共享Git配置？

所有worktree共享同一个`.git`目录，因此Git配置（`.git/config`）、钩子（`.git/hooks`）、忽略规则等都是共享的。

```powershell
# 在任意worktree中修改配置，所有worktree生效
git config user.name "Your Name"
git config user.email "your.email@example.com"
```

### Q3: worktree占用多少磁盘空间？

Worktree**非常节省空间**，因为：
- Git对象（commits、blobs、trees）只存储一份
- 每个worktree只包含工作区文件（未压缩）

```powershell
# 示例：
# .git目录：100MB（所有历史）
# 主工作树：50MB（工作区文件）
# 每个worktree：约50MB（仅工作区文件）

# 对比传统clone方式：
# clone 1: 150MB
# clone 2: 150MB
# clone 3: 150MB
# 总计: 450MB

# worktree方式：
# .git: 100MB
# 主工作树: 50MB
# worktree 1: 50MB
# worktree 2: 50MB
# 总计: 250MB（节省44%）
```

### Q4: 可以在worktree中创建子worktree吗？

**不可以**。所有worktree必须从主仓库（包含`.git`目录的那个）创建。

```powershell
# ❌ 错误
cd d:\Programs\GCTReporter-worktrees\feature-A
git worktree add ../feature-B -b feature/B  # 失败！

# ✅ 正确
cd d:\Programs\GCTReporter
git worktree add ..\GCTReporter-worktrees\feature-B -b feature/B
```

### Q5: 如何移动worktree到其他目录？

Git 2.17+支持移动worktree：

```powershell
cd d:\Programs\GCTReporter

# 方法1: 使用git worktree move（推荐）
git worktree move ..\GCTReporter-worktrees\feature-A d:\NewLocation\feature-A

# 方法2: 手动移动（旧版本Git）
# 1. 移动目录
Move-Item ..\GCTReporter-worktrees\feature-A d:\NewLocation\feature-A
# 2. 修复引用
git worktree repair d:\NewLocation\feature-A
```

### Q6: worktree可以跨项目使用吗？

**不可以**。Worktree只能用于同一个Git仓库的不同分支。

### Q7: 如何在worktree中切换分支？

**不建议**在worktree中切换分支，因为会导致混乱。

```powershell
# ❌ 不推荐：在worktree中切换分支
cd GCTReporter-worktrees/feature-A
git checkout feature/B  # 可能失败（如果feature/B已在其他worktree）

# ✅ 推荐：删除当前worktree，创建新worktree
cd d:\Programs\GCTReporter
git worktree remove ..\GCTReporter-worktrees\feature-A
git worktree add ..\GCTReporter-worktrees\feature-B feature/B
```

### Q8: worktree删除后分支还在吗？

**分支仍然存在**，删除worktree只是删除工作区文件。

```powershell
# 删除worktree
git worktree remove ..\GCTReporter-worktrees\feature-A

# 分支仍然存在
git branch  # feature/A 仍在列表中

# 如需删除分支
git branch -d feature/A  # 本地分支
git push origin --delete feature/A  # 远程分支
```

### Q9: 如何备份worktree？

Worktree中的Git提交会自动同步到主仓库，只需备份：

```powershell
# 1. 提交所有改动
cd GCTReporter-worktrees/feature-A
git add .
git commit -m "WIP: 备份点"

# 2. 推送到远程
git push origin feature/A

# 此时即使worktree丢失，也可以从远程恢复
```

### Q10: 如何处理worktree中的合并冲突？

与普通分支处理方式完全相同：

```powershell
cd GCTReporter-worktrees/feature-A

# 同步develop
git fetch origin
git merge origin/develop

# 如有冲突
# 1. 手动解决冲突文件
# 2. 标记为已解决
git add <resolved-files>
# 3. 完成合并
git commit
```

---

## 与分支保护配合

### 分支保护规则回顾

根据 [BRANCH_PROTECTION.md](分支保护/BRANCH_PROTECTION.md)：

- **main分支**：强制保护，禁止直接push，必须通过PR
- **develop分支**：建议通过PR合并
- **功能分支**：自由开发

### Worktree + 分支保护的正确姿势

#### 策略1: 主工作树在main（只读）

```powershell
# 主工作树：main分支（只读，查看代码）
d:\Programs\GCTReporter\  (main)

# 日常开发：develop worktree
d:\Programs\GCTReporter-worktrees\develop\  (develop)

# 功能开发：功能分支worktree
d:\Programs\GCTReporter-worktrees\feature-US010\  (feature/US010)
```

**优点**：
- ✅ main分支永远是最新发布版本
- ✅ 不会误提交到main
- ✅ 紧急修复可以基于main快速创建hotfix

**缺点**：
- ⚠️ develop也需要在worktree中，多占用空间

#### 策略2: 主工作树在develop（推荐）

```powershell
# 主工作树：develop分支（日常开发）
d:\Programs\GCTReporter\  (develop)

# 功能开发：功能分支worktree
d:\Programs\GCTReporter-worktrees\feature-US010\  (feature/US010)

# 紧急修复：基于main创建hotfix worktree
d:\Programs\GCTReporter-worktrees\hotfix-critical\  (hotfix/critical)
```

**优点**：
- ✅ develop作为主工作区，开发最方便
- ✅ 节省空间（不需要develop worktree）
- ✅ 仍然不会误提交到main

**缺点**：
- ⚠️ 需要切换到main查看发布版本代码

#### 推荐策略：主工作树在main，创建develop worktree

```powershell
# 初始化
cd d:\Programs\GCTReporter
git checkout main

# 创建develop worktree作为主要开发区
git worktree add ..\GCTReporter-worktrees\develop develop

# 日常开发在develop worktree
cd ..\GCTReporter-worktrees\develop
code .  # VS Code主窗口

# 功能开发在功能分支worktree
cd d:\Programs\GCTReporter
git worktree add ..\GCTReporter-worktrees\feature-US010 -b feature/US010
cd ..\GCTReporter-worktrees\feature-US010
code .  # VS Code新窗口
```

### 工作流程

```powershell
# 1. 在develop worktree进行代码查看、小改动
cd d:\Programs\GCTReporter-worktrees\develop
git pull origin develop

# 2. 新功能开发：创建功能分支worktree
cd d:\Programs\GCTReporter
git worktree add ..\GCTReporter-worktrees\feature-US010 -b feature/US010

# 3. 开发、提交
cd ..\GCTReporter-worktrees\feature-US010
# ... 开发 ...
git push origin feature/US010

# 4. 在GitHub创建PR: feature/US010 -> develop

# 5. PR合并后清理
cd d:\Programs\GCTReporter
git worktree remove ..\GCTReporter-worktrees\feature-US010
git branch -d feature/US010

# 6. 同步develop
cd ..\GCTReporter-worktrees\develop
git pull origin develop
```

---

## 总结

### Worktree的价值

| 场景 | 传统方式 | Worktree方式 |
|------|---------|-------------|
| 并行开发2个功能 | ❌ 频繁切换分支 | ✅ 2个VS Code窗口 |
| 紧急修复Bug | ❌ stash、切换、恢复 | ✅ 新worktree，不打断 |
| Code Review | ❌ 打断当前工作 | ✅ 临时worktree审查 |
| 前后端并行运行 | ❌ 只能运行一个 | ✅ 不同worktree同时运行 |
| 磁盘占用 | ❌ 每个clone 150MB | ✅ 每个worktree 50MB |

### 核心原则

1. ✅ **主工作树保持稳定**：main或develop，不频繁改动
2. ✅ **统一管理worktree**：集中存放在`GCTReporter-worktrees/`
3. ✅ **及时清理**：PR合并后立即删除worktree
4. ✅ **命名规范**：worktree名称与分支名对应
5. ✅ **自动化**：使用脚本简化重复操作

### 快速参考卡片

```powershell
# 📋 Git Worktree 命令速查

# 创建worktree
git worktree add <path> -b <new-branch> <base-branch>
git worktree add ../worktrees/feature-A -b feature/A develop

# 列出worktree
git worktree list

# 删除worktree
git worktree remove <path>
git worktree remove ../worktrees/feature-A

# 强制删除
git worktree remove -f <path>

# 清理引用
git worktree prune

# 移动worktree
git worktree move <old-path> <new-path>

# 修复worktree
git worktree repair <path>
```

---

## 附录

### 相关文档

- [Git工作流规范](../.github/instructions/git-workflow.md)
- [分支保护规则](分支保护/BRANCH_PROTECTION.md)
- [代码审查指南](../.github/instructions/code-review.md)

### 外部资源

- [Git Worktree官方文档](https://git-scm.com/docs/git-worktree)
- [Git Worktree教程](https://www.atlassian.com/git/tutorials/git-worktree)

---

**最后更新**: 2026-01-16  
**文档版本**: v1.0  
**维护人**: 技术组
