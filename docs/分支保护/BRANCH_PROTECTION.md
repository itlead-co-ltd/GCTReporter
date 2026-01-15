# GitHub分支保护规则设置指南

> **目的**: 保护main分支代码质量，强制通过Pull Request进行代码审查后才能合并

---

## 🛡️ 分支保护规则

### main分支保护（强制执行）

**保护目标**: 确保所有代码变更经过审查和测试后才能合并到main分支

#### 必须设置的规则

1. **✅ Require a pull request before merging**（合并前必须创建PR）
   - ✅ Require approvals: **1** (至少1人审查通过)
   - ✅ Dismiss stale pull request approvals when new commits are pushed（新提交后重新审查）
   - ✅ Require review from Code Owners（代码所有者必须审查）

2. **✅ Require status checks to pass before merging**（合并前必须通过状态检查）
   - ✅ Require branches to be up to date before merging（必须与main同步）
   - 状态检查项（如有CI/CD）:
     - Build & Test（构建和测试）
     - Code Quality Check（代码质量检查）

3. **✅ Require conversation resolution before merging**（合并前必须解决所有讨论）
   - 确保所有Review评论都已处理

4. **✅ Require linear history**（要求线性历史）
   - 禁止merge commits，只允许squash或rebase

5. **✅ Do not allow bypassing the above settings**（不允许绕过以上设置）
   - 包括管理员也必须遵守规则

6. **✅ Restrict who can push to matching branches**（限制谁可以推送）
   - 只允许通过PR合并，禁止直接push

---

## 📖 如何在GitHub上设置分支保护规则

### 步骤1: 进入仓库设置

1. 打开GitHub仓库：https://github.com/chuanminglu/GCTReporter
2. 点击 **Settings**（设置）
3. 左侧菜单选择 **Branches**（分支）

### 步骤2: 添加分支保护规则

1. 点击 **Add branch protection rule**（添加分支保护规则）
2. 在 **Branch name pattern** 输入: `main`

### 步骤3: 配置保护规则

#### ✅ 勾选以下选项:

**Protect matching branches**

- ☑️ **Require a pull request before merging**
  - ☑️ Require approvals: 设置为 **1**
  - ☑️ Dismiss stale pull request approvals when new commits are pushed
  - ☐ Require review from Code Owners（可选，需要创建CODEOWNERS文件）

- ☑️ **Require status checks to pass before merging**（如果有CI/CD）
  - ☑️ Require branches to be up to date before merging

- ☑️ **Require conversation resolution before merging**

- ☑️ **Require signed commits**（可选，推荐）

- ☑️ **Require linear history**

- ☑️ **Do not allow bypassing the above settings**

- ☑️ **Restrict who can push to matching branches**
  - 不添加任何人（完全禁止直接push）

### 步骤4: 保存规则

点击 **Create** 或 **Save changes** 保存规则

---

## 🔄 标准开发工作流

设置分支保护后，所有开发必须遵循以下流程：

### 1. 创建功能分支

```bash
# 确保main分支是最新的
git checkout main
git pull origin main

# 创建功能分支
git checkout -b feature/US001-user-login
```

### 2. 开发并提交代码

```bash
# 开发功能...

# 提交代码（符合Conventional Commits规范）
git add .
git commit -m "feat(auth): 添加用户登录功能

- 实现用户名密码登录
- 添加JWT token生成
- 添加登录接口单元测试"

# 推送到远程
git push origin feature/US001-user-login
```

### 3. 创建Pull Request

1. 访问 GitHub 仓库页面
2. 会看到提示 **Compare & pull request**，点击它
3. 填写 PR 信息（会自动加载PR模板）:
   - 选择PR类型
   - 填写变更说明
   - 完成检查清单
   - 添加截图（如有UI变更）
4. 选择 Reviewers（审查人）
5. 点击 **Create pull request**

### 4. 代码审查

审查人需要检查：
- [ ] 代码符合规范
- [ ] 测试覆盖充分
- [ ] 无安全风险
- [ ] 性能无问题
- [ ] 文档完整

审查人可以：
- **Approve**: 批准合并
- **Request changes**: 要求修改
- **Comment**: 添加评论

### 5. 合并PR

满足以下条件后才能合并：
- ✅ 至少1人Approve
- ✅ 所有CI/CD检查通过
- ✅ 所有讨论已解决
- ✅ 分支已同步到最新

合并方式选择：
- **Squash and merge** ✅ 推荐（将多个commit合并为1个）
- **Rebase and merge** ✅ 可选（保持线性历史）
- **Create a merge commit** ❌ 不推荐

### 6. 删除功能分支

```bash
# PR合并后，删除本地分支
git checkout main
git pull origin main
git branch -d feature/US001-user-login

# 删除远程分支（GitHub通常会自动删除）
git push origin --delete feature/US001-user-login
```

---

## 🚫 禁止的操作

设置分支保护后，以下操作将被禁止：

❌ **直接推送到main分支**
```bash
git checkout main
git commit -m "fix: 修复bug"
git push origin main  # ❌ 被拒绝！

# 错误信息:
# remote: error: GH006: Protected branch update failed for refs/heads/main.
```

❌ **强制推送到main分支**
```bash
git push --force origin main  # ❌ 被拒绝！
```

❌ **未经审查直接合并PR**
```bash
# ❌ 无法合并，必须至少1人Approve
```

---

## ✅ 正确的操作示例

### 场景1: 紧急修复Bug

即使是紧急Bug，也必须通过PR流程：

```bash
# 1. 创建hotfix分支
git checkout main
git pull origin main
git checkout -b hotfix/critical-bug-fix

# 2. 修复bug
# ... 修改代码 ...
git add .
git commit -m "fix(critical): 修复生产环境登录失败问题"

# 3. 推送并创建PR
git push origin hotfix/critical-bug-fix

# 4. 在GitHub创建PR，选择 reviewers
# 5. 审查人快速审查并批准
# 6. 合并PR
```

### 场景2: 团队协作开发

```bash
# 开发者A: 创建功能分支
git checkout -b feature/report-export
# ... 开发 ...
git push origin feature/report-export

# 开发者B: 审查代码
# 在GitHub PR页面进行Code Review
# - 添加评论
# - 请求修改或批准

# 开发者A: 根据反馈修改
git add .
git commit -m "refactor(export): 优化导出性能"
git push origin feature/report-export

# 开发者B: 批准PR
# 开发者A或B: 合并PR到main
```

---

## 📊 分支保护效果

设置分支保护后的效果：

| 操作 | 未设置保护 | 设置保护后 |
|------|----------|----------|
| 直接push到main | ✅ 允许 | ❌ 拒绝 |
| 未审查合并PR | ✅ 允许 | ❌ 拒绝 |
| 测试未通过合并PR | ✅ 允许 | ❌ 拒绝 |
| 强制推送覆盖历史 | ✅ 允许 | ❌ 拒绝 |
| 通过PR合并代码 | ✅ 允许 | ✅ 允许 |

---

## 🔧 高级配置（可选）

### CODEOWNERS文件

创建 `.github/CODEOWNERS` 指定代码所有者：

```
# 全局代码所有者
* @chuanminglu

# 后端代码
/src/main/java/** @backend-team

# 前端代码
/frontend/** @frontend-team

# 文档
/docs/** @tech-writer

# GitHub配置
/.github/** @chuanminglu
```

### GitHub Actions自动检查

创建 `.github/workflows/pr-check.yml`:

```yaml
name: PR Quality Check

on:
  pull_request:
    branches: [ main ]

jobs:
  check:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Check commit message
        run: |
          # 检查commit message是否符合Conventional Commits
          echo "检查提交信息格式..."
      
      - name: Run tests
        run: |
          # 运行测试
          echo "运行测试..."
      
      - name: Code quality check
        run: |
          # 代码质量检查
          echo "代码质量检查..."
```

---

## 📞 问题反馈

如遇到分支保护相关问题，请联系：
- 技术负责人: @chuanminglu
- GitHub仓库: https://github.com/chuanminglu/GCTReporter/issues

---

**最后更新**: 2026-01-15
**文档版本**: v1.0
