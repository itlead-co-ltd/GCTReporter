# Git工作流规范

> **适用范围**: GCT Reporter项目Git版本控制
> **最后更新**: 2026-01-15

---

## 🔀 提交信息规范（Conventional Commits）

### 提交信息格式

```bash
<type>(<scope>): <subject>

[optional body]

[optional footer]
```

**示例**:
```bash
feat(report): 添加报表预览功能

- 实现SQL测试执行接口
- 添加参数输入表单
- 显示查询结果前100行
- 添加执行时间统计

Closes #123
```

---

## 📝 Type类型定义

| Type | 说明 | 示例 | 使用场景 |
|------|------|------|---------|
| **feat** | 新功能 | feat(user): 添加用户登录功能 | 新增功能特性 |
| **fix** | Bug修复 | fix(report): 修复报表查询参数为空的Bug | 修复问题 |
| **docs** | 文档更新 | docs(readme): 更新项目README文档 | 修改文档 |
| **style** | 代码格式 | style(report): 格式化ReportService代码 | 不影响功能的格式调整 |
| **refactor** | 重构代码 | refactor(sql): 重构SQL执行引擎 | 代码重构（不是新功能也不是Bug修复） |
| **perf** | 性能优化 | perf(query): 优化报表查询性能 | 提升性能的改动 |
| **test** | 测试相关 | test(user): 添加用户登录单元测试 | 添加或修改测试 |
| **chore** | 构建/工具 | chore(deps): 升级SpringBoot版本到3.1.5 | 构建工具或辅助工具变动 |
| **ci** | CI/CD | ci(github): 添加GitHub Actions构建流程 | 持续集成配置 |
| **revert** | 回滚提交 | revert: 回滚feat(user)提交 | 撤销之前的提交 |

---

## 🏷️ Scope范围定义

```yaml
# 业务模块
user        # 用户管理模块
report      # 报表管理模块
param       # 参数配置模块
column      # 列配置模块
query       # 查询执行模块
export      # Excel导出模块
permission  # 权限管理模块

# 技术模块
auth        # 认证授权
security    # 安全相关
db          # 数据库相关
api         # API接口相关

# 前端模块
ui          # 前端UI相关
component   # 组件相关
router      # 路由相关
store       # 状态管理

# 通用
deps        # 依赖管理
config      # 配置文件
test        # 测试相关
```

---

## ✅ 好的提交信息示例

### 新功能

```bash
feat(report): 添加报表预览功能

实现了报表预览功能，允许设计者在保存前测试SQL：
- 添加预览接口 /api/v1/reports/{id}/preview
- 实现参数输入表单组件
- 显示查询结果前100行
- 添加执行时间统计
- 添加错误提示

Closes #123
```

### Bug修复

```bash
fix(query): 修复查询超时问题

当查询结果超过5000行时会出现超时，原因是没有设置查询超时限制。

修复方法：
- 在HikariCP配置中添加socketTimeout=5000
- 在SQL执行时添加超时控制
- 添加超时异常处理

Fixes #456
```

### 简洁提交

```bash
fix(export): 修复Excel导出中文乱码

设置POI工作簿编码为UTF-8
```

### 文档更新

```bash
docs(api): 更新API文档

添加报表预览接口文档说明：
- 接口路径
- 请求参数
- 响应格式
- 错误码说明
```

### 性能优化

```bash
perf(query): 优化报表列表查询性能

- 添加索引：idx_report_creator
- 优化SQL查询，避免N+1问题
- 添加分页查询
- 查询时间从500ms降低到50ms

Performance: 查询速度提升10倍
```

### 重构

```bash
refactor(user): 重构用户服务层代码

- 提取公共方法到UserHelper
- 优化异常处理逻辑
- 简化密码加密流程
- 添加JSR-303参数校验

No breaking changes
```

---

## ❌ 禁止的提交信息

### 过于简略

```bash
# ❌ 错误
fix bug
update code
修改文件
优化
改了点东西
```

### 缺少类型

```bash
# ❌ 错误
添加用户登录功能          # 应该是 feat(user): 添加用户登录功能
修复报表查询问题          # 应该是 fix(report): 修复报表查询参数为空的Bug
更新README              # 应该是 docs(readme): 更新项目README文档
```

### 描述不清晰

```bash
# ❌ 错误
feat(report): 改了一些东西
fix(user): 修复问题
docs: 更新文档
refactor: 重构
```

### 提交内容过多

```bash
# ❌ 错误：一次提交包含多个不相关的改动
feat(user): 添加用户登录 + 修复报表查询Bug + 更新文档

# ✅ 正确：拆分成多个提交
feat(user): 添加用户登录功能
fix(report): 修复报表查询参数为空的Bug
docs(readme): 更新项目README文档
```

---

## 🌳 分支管理规范

### 分支命名规范

```bash
# 主分支
main                              # 主分支，受保护，仅合并经过审查的代码
develop                           # 开发分支，日常开发合并目标

# 功能分支：feature/<Story编号>-<简短描述>
feature/US001-user-login          # 用户登录功能
feature/US010-sql-editor          # SQL编辑器
feature/add-excel-export          # 添加Excel导出（无Story编号时）

# Bug修复分支：bugfix/<简短描述>
bugfix/fix-query-timeout          # 修复查询超时
bugfix/SQL-injection-fix          # SQL注入修复
bugfix/issue-123                  # 修复Issue #123

# 紧急修复分支：hotfix/<简短描述>
hotfix/critical-security-fix      # 紧急安全修复（生产环境）
hotfix/prod-login-error           # 生产环境登录错误修复

# 发布分支：release/<版本号>
release/v1.0.0                    # 1.0.0版本发布
release/v1.1.0                    # 1.1.0版本发布
```

---

## 🔒 分支保护规则

### main分支（强制）

- ✅ **必须**通过Pull Request合并
- ✅ **必须**至少1人Code Review通过
- ✅ **必须**所有测试通过
- ✅ **必须**无合并冲突
- ✅ **必须**通过CI/CD检查
- ❌ **禁止**直接推送（git push）
- ❌ **禁止**强制推送（git push -f）

### develop分支（建议）

- ✅ **建议**通过Pull Request合并
- ✅ **建议**代码审查（可选）
- ✅ **必须**本地测试通过
- ✅ **建议**无冲突再合并

### 功能分支（自由）

- ✅ 开发者可自由推送
- ✅ 可以强制推送（慎用）
- ✅ 可以随时删除

---

## 🔄 分支工作流

### 标准功能开发流程

```bash
# 步骤1: 从develop创建功能分支
git checkout develop
git pull origin develop
git checkout -b feature/US010-sql-editor

# 步骤2: 开发功能，频繁提交
# 编写代码...
git add .
git commit -m "feat(report): 添加SQL编辑器组件"

# 继续开发...
git add .
git commit -m "feat(report): 实现语法高亮功能"

# 步骤3: 同步develop最新代码（避免冲突）
git checkout develop
git pull origin develop
git checkout feature/US010-sql-editor
git rebase develop  # 或 git merge develop

# 步骤4: 推送到远程
git push origin feature/US010-sql-editor

# 步骤5: 创建Pull Request到develop
# 在GitHub/GitLab界面操作：
# - 选择 feature/US010-sql-editor -> develop
# - 填写PR描述
# - 指定审查人

# 步骤6: Code Review通过后合并
# 审查人在Web界面点击"Merge"

# 步骤7: 删除功能分支
git checkout develop
git pull origin develop
git branch -d feature/US010-sql-editor
git push origin --delete feature/US010-sql-editor
```

---

## 🐛 Bug修复流程

### 开发环境Bug

```bash
# 从develop创建bugfix分支
git checkout develop
git pull origin develop
git checkout -b bugfix/fix-query-timeout

# 修复Bug
git add .
git commit -m "fix(query): 修复查询超时问题"

# 推送并创建PR到develop
git push origin bugfix/fix-query-timeout
# 创建PR: bugfix/fix-query-timeout -> develop
```

### 生产环境紧急Bug

```bash
# 从main创建hotfix分支
git checkout main
git pull origin main
git checkout -b hotfix/critical-security-fix

# 修复Bug
git add .
git commit -m "fix(security): 修复SQL注入漏洞"

# 推送并创建PR到main
git push origin hotfix/critical-security-fix
# 创建PR: hotfix/critical-security-fix -> main

# 合并到main后，立即cherry-pick到develop
git checkout develop
git cherry-pick <commit-hash>
git push origin develop
```

---

## 🚀 发布流程

```bash
# 步骤1: 从develop创建release分支
git checkout develop
git pull origin develop
git checkout -b release/v1.0.0

# 步骤2: 版本号修改
# 修改 pom.xml、package.json 中的版本号为 1.0.0
git add .
git commit -m "chore(release): 版本号升级到v1.0.0"

# 步骤3: 最后的Bug修复（仅修复阻塞发布的Bug）
git add .
git commit -m "fix(export): 修复导出文件名错误"

# 步骤4: 合并到main（发布）
git checkout main
git merge release/v1.0.0
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin main --tags

# 步骤5: 合并回develop
git checkout develop
git merge release/v1.0.0
git push origin develop

# 步骤6: 删除release分支
git branch -d release/v1.0.0
git push origin --delete release/v1.0.0
```

---

## 🔍 Code Review规范

### PR描述模板

```markdown
## 变更类型
- [ ] 新功能
- [ ] Bug修复
- [ ] 重构
- [ ] 文档更新
- [ ] 其他

## 变更说明
简要描述本次PR的目的和实现方式

## 关联Issue
Closes #123

## 测试情况
- [ ] 单元测试通过
- [ ] 集成测试通过
- [ ] 本地手动测试通过

## 截图（如适用）
[添加截图]

## 检查清单
- [ ] 代码符合项目规范
- [ ] 添加了必要的注释
- [ ] 更新了相关文档
- [ ] 无明显性能问题
- [ ] 通过静态代码检查
```

### 审查重点

**功能性**:
- [ ] 是否实现了所有验收标准？
- [ ] 是否处理了异常情况？
- [ ] 边界条件是否考虑周全？

**安全性**:
- [ ] 是否有SQL注入风险？
- [ ] 密码是否加密存储？
- [ ] 敏感信息是否脱敏？

**性能**:
- [ ] 是否有N+1查询问题？
- [ ] 是否需要添加索引？
- [ ] 是否有内存泄漏风险？

**可维护性**:
- [ ] 命名是否清晰？
- [ ] 是否有重复代码？
- [ ] 注释是否完整？

---

## 🛠️ Git常用命令

### 基础操作

```bash
# 查看状态
git status

# 查看提交历史
git log --oneline --graph --all

# 查看差异
git diff
git diff --cached  # 查看已暂存的改动

# 撤销修改
git checkout -- <file>      # 撤销工作区修改
git reset HEAD <file>       # 撤销暂存区修改
git reset --soft HEAD~1     # 撤销最近一次提交（保留改动）
git reset --hard HEAD~1     # 撤销最近一次提交（丢弃改动）
```

### 分支操作

```bash
# 查看分支
git branch           # 本地分支
git branch -r        # 远程分支
git branch -a        # 所有分支

# 创建分支
git checkout -b feature/new-feature

# 切换分支
git checkout develop

# 删除分支
git branch -d feature/old-feature       # 删除本地分支
git push origin --delete feature/old    # 删除远程分支

# 重命名分支
git branch -m old-name new-name
```

### 合并与变基

```bash
# 合并（保留分支历史）
git merge feature/new-feature

# 变基（线性历史）
git rebase develop

# 解决冲突后继续
git add <resolved-files>
git rebase --continue  # 或 git merge --continue

# 放弃合并/变基
git rebase --abort
git merge --abort
```

### 远程操作

```bash
# 同步远程分支
git fetch origin

# 拉取并合并
git pull origin develop

# 推送
git push origin feature/new-feature

# 强制推送（慎用！）
git push -f origin feature/new-feature
```

---

## 🚫 禁止操作

### 绝对禁止

1. ❌ **禁止直接推送到main分支**
   ```bash
   git push origin main  # 危险！
   ```

2. ❌ **禁止强制推送到main/develop分支**
   ```bash
   git push -f origin main     # 危险！
   git push -f origin develop  # 危险！
   ```

3. ❌ **禁止提交敏感信息**
   - 数据库密码
   - API密钥
   - 用户真实数据
   - 内部服务器地址

4. ❌ **禁止提交生成文件**
   ```bash
   # 应在.gitignore中排除
   target/
   node_modules/
   *.class
   *.log
   .DS_Store
   ```

5. ❌ **禁止提交大文件**
   - 二进制文件（>10MB）
   - 视频/图片原文件
   - 数据库备份文件

---

## ✅ 检查清单

### 提交前检查

- [ ] 提交信息符合Conventional Commits格式
- [ ] type和scope选择正确
- [ ] subject描述清晰（<50字符）
- [ ] 本地测试通过
- [ ] 代码格式化完成
- [ ] 无敏感信息泄露
- [ ] 无不必要的文件（如.DS_Store、node_modules）

### PR创建前检查

- [ ] 分支从最新的develop创建
- [ ] 已同步develop最新代码（无冲突）
- [ ] 所有测试通过
- [ ] PR描述完整
- [ ] 关联了对应的Issue
- [ ] 指定了审查人

---

**最后更新**: 2026-01-15
**团队规模**: 2人开发团队
