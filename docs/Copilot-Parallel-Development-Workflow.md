# GitHub Copilot 多任务并行开发工作流

> **文档类型**: AI辅助开发实践指南  
> **适用项目**: GCT Reporter  
> **最后更新**: 2026-01-16

---

## 📋 目录

- [概述](#概述)
- [核心概念](#核心概念)
- [并行策略](#并行策略)
- [实战场景](#实战场景)
- [提示词模板库](#提示词模板库)
- [工作流程](#工作流程)
- [最佳实践](#最佳实践)
- [常见问题](#常见问题)

---

## 概述

### 什么是多任务并行开发

在敏捷开发中，团队经常需要同时推进多个User Story：
- **场景1**：Sprint中有3个Story需要并行开发
- **场景2**：正在开发功能A时，需要紧急修复Bug
- **场景3**：需要同时开发前端和后端，互不干扰
- **场景4**：Code Review不能打断当前开发进度

传统方式需要频繁切换分支，容易丢失上下文。**Copilot + Worktree**的组合可以优雅地解决这个问题。

### 为什么需要并行开发

| 痛点 | 传统方式 | Copilot + Worktree |
|------|---------|-------------------|
| 多Story并行 | ❌ 频繁切换分支，容易混乱 | ✅ 每个Story独立环境 |
| 紧急Bug修复 | ❌ stash当前工作，心流被打断 | ✅ 新窗口修复，不影响当前 |
| 前后端联调 | ❌ 只能运行一个分支 | ✅ 同时运行不同分支 |
| Code Review | ❌ 打断当前开发 | ✅ 临时环境Review |
| AI上下文 | ❌ 切换分支后需要重新建立上下文 | ✅ 每个窗口保持独立上下文 |

---

## 核心概念

### GitHub Copilot的工作机制

#### 单会话模式（传统）

```
VS Code窗口
├── Copilot Chat（单一会话）
├── 当前分支: feature/US010
└── 上下文: 基于当前打开的文件
```

**限制**：
- ❌ 只能专注一个任务
- ❌ 切换分支会丢失Copilot的上下文
- ❌ 无法同时处理多个Story

#### 多会话模式（推荐）

```
VS Code窗口1                VS Code窗口2                VS Code窗口3
├── Copilot实例A            ├── Copilot实例B            ├── Copilot实例C
├── feature/US010           ├── feature/US011           ├── bugfix/timeout
├── 上下文: US010相关       ├── 上下文: US011相关       ├── 上下文: Bug修复
└── 独立对话历史            └── 独立对话历史            └── 独立对话历史
```

**优势**：
- ✅ 每个Story有独立的Copilot上下文
- ✅ 对话历史不混乱
- ✅ 可以同时运行前后端服务
- ✅ 真正的并行开发

---

## 并行策略

### 策略1: 物理并行（推荐）⭐

**原理**：利用Worktree创建多个工作目录，每个目录打开独立的VS Code窗口。

```powershell
# 创建3个worktree
git worktree add ../worktrees/feature-US010 -b feature/US010-sql-editor
git worktree add ../worktrees/feature-US011 -b feature/US011-excel-export
git worktree add ../worktrees/bugfix-timeout -b bugfix/query-timeout

# 在3个VS Code窗口中打开
code ../worktrees/feature-US010  # 窗口1 - Copilot实例A
code ../worktrees/feature-US011  # 窗口2 - Copilot实例B
code ../worktrees/bugfix-timeout # 窗口3 - Copilot实例C
```

**适用场景**：
- ✅ 多个Story并行开发
- ✅ 前后端分离开发
- ✅ 紧急Bug修复不打断当前工作
- ✅ Code Review独立环境

**优势**：
- 🚀 真正的并行，互不干扰
- 🎯 每个任务保持独立的AI上下文
- 💾 每个环境有独立的构建产物和依赖
- 🔄 可以同时运行服务（不同端口）

---

### 策略2: 逻辑并行（子代理）

**原理**：在单个Copilot会话中使用`runSubagent`工具执行多步骤任务。

```typescript
// 提示词示例
我需要完成以下3个任务，请为每个任务创建子代理：

**任务1 - 后端API实现**（子代理A）
- 创建ReportController
- 实现预览接口
- 添加单元测试

**任务2 - 前端组件开发**（子代理B）
- 创建SqlEditor.vue
- 实现语法高亮
- 添加参数输入表单

**任务3 - 文档更新**（子代理C）
- 更新API文档
- 添加使用示例
- 生成测试用例文档

每个子代理独立工作，完成后汇总结果。
```

**适用场景**：
- ✅ 单个Story内的多步骤任务
- ✅ 需要AI帮助规划和执行
- ✅ 任务间有依赖关系
- ✅ 需要最终汇总结果

**限制**：
- ⚠️ 子代理是**顺序执行**（非真正并行）
- ⚠️ 所有任务在同一个分支/目录
- ⚠️ 共享同一个Copilot上下文

---

### 策略3: 混合模式（最灵活）

**原理**：物理并行 + 逻辑并行的组合。

```powershell
# 物理并行：3个worktree
窗口1: feature-US010 (Copilot A)
  └── 子代理A1: 实现后端API
  └── 子代理A2: 编写单元测试
  └── 子代理A3: 更新文档

窗口2: feature-US011 (Copilot B)
  └── 子代理B1: 实现导出逻辑
  └── 子代理B2: 集成Apache POI
  └── 子代理B3: 编写集成测试

窗口3: bugfix-timeout (Copilot C)
  └── 子代理C1: 分析性能瓶颈
  └── 子代理C2: 优化SQL查询
  └── 子代理C3: 验证性能提升
```

**适用场景**：
- ✅ 复杂Sprint，多Story并行 + 每个Story多步骤
- ✅ 需要最大化开发效率
- ✅ 团队协作（每人负责不同窗口）

---

## 实战场景

### 场景1: Sprint并行开发3个Story

**背景**：
- Sprint 1有3个Must-Have Story
- 团队规模：2人
- 开发周期：10个工作日

**任务清单**：
- **US010**: SQL编辑器（5人日）
- **US011**: Excel导出（4人日）
- **US012**: 权限管理（6人日）

#### 步骤1: 创建Worktree环境

```powershell
# 进入主仓库
cd d:\Programs\GCTReporter

# 同步develop分支
git checkout develop
git pull origin develop

# 创建3个功能分支worktree
git worktree add ..\GCTReporter-worktrees\feature-US010 -b feature/US010-sql-editor
git worktree add ..\GCTReporter-worktrees\feature-US011 -b feature/US011-excel-export
git worktree add ..\GCTReporter-worktrees\feature-US012 -b feature/US012-permission

# 验证创建成功
git worktree list
```

#### 步骤2: 打开3个VS Code窗口

```powershell
# 窗口1: US010 - SQL编辑器
code ..\GCTReporter-worktrees\feature-US010

# 窗口2: US011 - Excel导出
code ..\GCTReporter-worktrees\feature-US011

# 窗口3: US012 - 权限管理
code ..\GCTReporter-worktrees\feature-US012
```

#### 步骤3: 在每个窗口使用Copilot开发

**窗口1 - US010 SQL编辑器**（Copilot Chat）

```markdown
# 角色定义
你是一位资深前端开发工程师，擅长Vue 3和Monaco Editor集成。

# 任务
实现SQL编辑器组件，包含以下功能：
1. 基于Monaco Editor的SQL编辑器
2. SQL语法高亮（支持Oracle和SQLite）
3. 自动补全（表名、字段名）
4. 参数输入表单
5. 查询结果预览

# 技术栈
- Vue 3 Composition API
- TypeScript
- Monaco Editor
- Element Plus

# 输出要求
1. 创建SqlEditor.vue组件
2. 实现上述所有功能
3. 添加组件单元测试
4. 更新路由配置
5. 添加API调用逻辑

请分步骤实现，每完成一个功能告诉我。
```

**窗口2 - US011 Excel导出**（Copilot Chat）

```markdown
# 角色定义
你是一位资深Java后端工程师，擅长Apache POI和数据导出。

# 任务
实现报表Excel导出功能：
1. 后端API: POST /api/v1/reports/{id}/export
2. 使用Apache POI生成Excel（XLSX格式）
3. 支持自定义列顺序和显示名
4. 支持大数据量导出（流式写入）
5. 中文文件名支持

# 技术栈
- Spring Boot 3.1.x
- Apache POI 5.2.x
- Java 17

# 输出要求
1. 创建ExportService
2. 实现导出逻辑
3. 添加单元测试和集成测试
4. 添加性能测试（10000行数据）
5. 更新API文档

请分步骤实现，先搭建基础架构。
```

**窗口3 - US012 权限管理**（Copilot Chat）

```markdown
# 角色定义
你是一位资深安全架构师，擅长Spring Security和RBAC权限模型。

# 任务
实现基于角色的权限管理（RBAC）：
1. 设计权限模型（用户-角色-权限）
2. 实现Spring Security配置
3. 实现权限拦截器
4. 实现权限校验注解
5. 前端路由权限控制

# 技术栈
- Spring Security 6.x
- JWT认证
- Vue 3 + Router

# 输出要求
1. 设计数据库Schema
2. 实现认证授权逻辑
3. 添加权限测试
4. 前端权限控制组件
5. 更新用户手册

请先设计权限模型，等我确认后再实现。
```

#### 步骤4: 并行开发，独立提交

```powershell
# 窗口1 - US010
cd d:\Programs\GCTReporter-worktrees\feature-US010
git add .
git commit -m "feat(report): 实现SQL编辑器组件"
git push origin feature/US010-sql-editor

# 窗口2 - US011
cd d:\Programs\GCTReporter-worktrees\feature-US011
git add .
git commit -m "feat(export): 实现Excel导出功能"
git push origin feature/US011-excel-export

# 窗口3 - US012
cd d:\Programs\GCTReporter-worktrees\feature-US012
git add .
git commit -m "feat(auth): 实现RBAC权限管理"
git push origin feature/US012-permission
```

#### 步骤5: 创建PR并清理

```powershell
# 在GitHub创建3个PR
# - feature/US010-sql-editor -> develop
# - feature/US011-excel-export -> develop
# - feature/US012-permission -> develop

# PR合并后清理worktree
cd d:\Programs\GCTReporter
git worktree remove ..\GCTReporter-worktrees\feature-US010
git worktree remove ..\GCTReporter-worktrees\feature-US011
git worktree remove ..\GCTReporter-worktrees\feature-US012
git worktree prune
```

---

### 场景2: 紧急Bug修复不打断当前开发

**背景**：
- 正在开发US010（50%完成，代码未提交）
- 生产环境发现SQL注入漏洞，需要紧急修复

#### 步骤1: 保持当前窗口继续开发

```powershell
# 窗口1: 继续在US010开发（不动）
当前窗口: d:\Programs\GCTReporter-worktrees\feature-US010
状态: 正在编辑SqlEditor.vue，未提交
```

#### 步骤2: 新窗口创建hotfix

```powershell
# 新开PowerShell终端
cd d:\Programs\GCTReporter

# 确保main是最新的
git checkout main
git pull origin main

# 创建hotfix worktree（基于main）
git worktree add ..\GCTReporter-worktrees\hotfix-sql-injection -b hotfix/SQL-injection-fix main

# 新VS Code窗口打开
code ..\GCTReporter-worktrees\hotfix-sql-injection
```

#### 步骤3: 在hotfix窗口使用Copilot修复

**窗口2 - Hotfix**（Copilot Chat）

```markdown
# 紧急任务
生产环境发现SQL注入漏洞，需要立即修复。

# 问题描述
用户可以通过报表查询参数注入恶意SQL：
- 位置: ReportQueryService.executeQuery()
- 风险: 可能导致数据泄露或破坏

# 修复要求
1. 分析当前SQL执行方式
2. 强制使用参数化查询
3. 添加SQL关键字黑名单校验
4. 添加SQL注入安全测试
5. 验证修复效果

# 验收标准
- 所有动态SQL使用NamedParameterJdbcTemplate
- 禁止字符串拼接SQL
- 添加至少5个SQL注入测试用例
- 所有测试通过

请先分析代码，找出所有SQL注入风险点。
```

#### 步骤4: 修复、测试、提交

```powershell
# 在hotfix窗口
cd d:\Programs\GCTReporter-worktrees\hotfix-sql-injection

# Copilot帮助修复代码...
# 运行测试
cd backend
mvn test

# 提交修复
git add .
git commit -m "fix(security): 修复SQL注入漏洞

- 强制使用参数化查询
- 添加SQL关键字黑名单校验
- 添加SQL注入安全测试

Fixes #456"

# 推送
git push origin hotfix/SQL-injection-fix

# 在GitHub创建紧急PR: hotfix/SQL-injection-fix -> main
```

#### 步骤5: 回到原来的开发（无缝切换）

```powershell
# 关闭hotfix窗口
# 回到窗口1: feature-US010
# 继续开发SqlEditor.vue，代码和Copilot上下文完全保留
```

---

### 场景3: 前后端并行开发

**背景**：
- US010需要前后端配合开发
- 前端需要Mock数据，后端需要独立测试
- 需要同时运行前端和后端服务

#### 步骤1: 创建前后端独立worktree

```powershell
cd d:\Programs\GCTReporter

# 前端分支
git worktree add ..\GCTReporter-worktrees\frontend-US010 -b feature/US010-frontend

# 后端分支
git worktree add ..\GCTReporter-worktrees\backend-US010 -b feature/US010-backend
```

#### 步骤2: 前端窗口开发

```powershell
# 窗口1: 前端开发
code ..\GCTReporter-worktrees\frontend-US010

# 启动前端服务
cd frontend
npm install
npm run dev  # localhost:5173
```

**Copilot Chat - 窗口1**

```markdown
# 前端任务
实现报表预览页面，包含：
1. SQL编辑器组件
2. 参数输入表单
3. 查询结果表格
4. 错误提示

# Mock数据
暂时使用Mock API响应：
```json
{
  "data": {
    "columns": ["id", "name", "email"],
    "rows": [
      [1, "张三", "zhang@example.com"],
      [2, "李四", "li@example.com"]
    ],
    "executionTime": 0.123
  }
}
```

请先实现UI和Mock数据交互。
```

#### 步骤3: 后端窗口开发

```powershell
# 窗口2: 后端开发
code ..\GCTReporter-worktrees\backend-US010

# 启动后端服务
cd backend
mvn spring-boot:run  # localhost:8080
```

**Copilot Chat - 窗口2**

```markdown
# 后端任务
实现报表预览API：
- 接口: POST /api/v1/reports/{id}/preview
- 功能: 执行SQL查询，返回前100行
- 参数: 报表ID + 参数Map

# 技术要求
- 使用NamedParameterJdbcTemplate
- 查询超时5秒
- 添加SQL校验
- 返回执行时间

请实现完整的Controller、Service、Repository。
```

#### 步骤4: 前后端联调

```powershell
# 窗口1（前端）：修改API地址为真实后端
# src/api/report.ts
const API_BASE_URL = 'http://localhost:8080'

# 窗口2（后端）：添加CORS配置
# 允许localhost:5173访问

# 测试联调
# 前端页面: http://localhost:5173/reports/preview
# 后端API: http://localhost:8080/api/v1/reports/1/preview
```

#### 步骤5: 分别提交和合并

```powershell
# 前端分支提交
cd d:\Programs\GCTReporter-worktrees\frontend-US010
git add .
git commit -m "feat(ui): 实现报表预览前端页面"
git push origin feature/US010-frontend
# 创建PR: feature/US010-frontend -> develop

# 后端分支提交
cd d:\Programs\GCTReporter-worktrees\backend-US010
git add .
git commit -m "feat(api): 实现报表预览后端接口"
git push origin feature/US010-backend
# 创建PR: feature/US010-backend -> develop

# 两个PR可以独立Review和合并
```

---

## 提示词模板库

### 模板1: 功能开发（完整Story实现）

```markdown
# 🎭 角色定义
你是一位资深{技术栈}工程师，拥有{N}年{领域}开发经验，擅长：
- {能力1}
- {能力2}
- {能力3}

# 📋 任务描述
实现User Story US{编号}: {Story标题}

## 背景
{Story背景描述}

## 验收标准
- [ ] {AC1}
- [ ] {AC2}
- [ ] {AC3}

# 🎯 实现要求
## 技术栈
- {技术1}
- {技术2}
- {技术3}

## 功能清单
1. {功能1描述}
2. {功能2描述}
3. {功能3描述}

## 质量要求
- 单元测试覆盖率 > 80%
- 代码符合项目规范
- API文档完整
- 无安全漏洞

# 📤 输出要求
请分步骤实现：
1. 先搭建基础架构
2. 实现核心功能
3. 添加边界处理
4. 编写单元测试
5. 更新文档

每完成一个步骤告诉我，等我确认后再进行下一步。
```

**使用示例**：

```markdown
# 🎭 角色定义
你是一位资深Java后端工程师，拥有10年Spring Boot开发经验，擅长：
- RESTful API设计
- SQL优化与数据库设计
- 单元测试与集成测试

# 📋 任务描述
实现User Story US010: SQL编辑器后端支持

## 背景
设计者需要在保存报表前测试SQL的正确性，需要一个预览接口来执行SQL并返回结果。

## 验收标准
- [ ] 提供预览接口，执行SQL并返回前100行
- [ ] 查询超时控制在5秒
- [ ] 显示查询执行时间
- [ ] 禁止执行危险SQL（DROP/DELETE等）
- [ ] 参数化查询，防止SQL注入

# 🎯 实现要求
## 技术栈
- Spring Boot 3.1.x
- Spring JDBC
- HikariCP连接池

## 功能清单
1. 创建预览接口 POST /api/v1/reports/{id}/preview
2. SQL安全校验（黑名单关键字）
3. 参数替换与执行
4. 结果集封装（前100行）
5. 执行时间统计

## 质量要求
- 单元测试覆盖率 > 80%
- SQL注入防护测试100%覆盖
- 性能测试：1000行数据 < 3秒
- 代码符合阿里巴巴Java规范

# 📤 输出要求
请分步骤实现：
1. 设计DTO和VO
2. 实现SQL校验器
3. 实现查询服务
4. 实现Controller
5. 编写单元测试
6. 编写集成测试

每完成一个步骤告诉我。
```

---

### 模板2: Bug修复（问题定位与修复）

```markdown
# 🐛 Bug修复任务

## 问题描述
- Bug ID: #{编号}
- 优先级: {P0/P1/P2}
- 发现环境: {开发/测试/生产}
- 影响范围: {功能模块}

## 错误现象
{详细描述错误现象}

## 复现步骤
1. {步骤1}
2. {步骤2}
3. {步骤3}

## 预期行为
{正确的行为应该是什么}

# 🔍 分析要求
请帮我：
1. 分析可能的原因（列出所有可能性）
2. 定位问题代码位置
3. 提出修复方案（至少2个备选方案）
4. 评估修复风险
5. 建议测试用例

# 🛠️ 修复要求
- 修复方案必须经过我确认
- 必须添加回归测试
- 不引入新的Bug
- 代码审查通过

请先分析问题，不要直接修改代码。
```

**使用示例**：

```markdown
# 🐛 Bug修复任务

## 问题描述
- Bug ID: #456
- 优先级: P0（生产环境）
- 发现环境: 生产
- 影响范围: 报表查询功能

## 错误现象
用户可以通过报表参数注入SQL，查询到不属于自己的数据。

## 复现步骤
1. 创建报表，SQL包含参数 :userId
2. 在查询页面输入参数: 1 OR 1=1
3. 提交查询
4. 返回所有用户的数据（权限绕过）

## 预期行为
只返回当前用户（userId=1）的数据，参数"1 OR 1=1"应被当作字符串处理。

# 🔍 分析要求
请帮我：
1. 分析SQL注入的根本原因
2. 定位问题代码（ReportQueryService.java）
3. 提出修复方案（参数化查询 vs 输入校验）
4. 评估修复风险（是否影响现有功能）
5. 建议SQL注入测试用例（至少5个场景）

# 🛠️ 修复要求
- 强制使用参数化查询
- 添加SQL注入防护测试
- 不影响现有正常查询
- 需要通过安全审查

请先分析问题，找出所有SQL拼接的地方。
```

---

### 模板3: Code Review（审查分支代码）

```markdown
# 📝 Code Review任务

## 审查目标
- PR编号: #{编号}
- 分支名: {branch-name}
- Story: US{编号} - {标题}
- 提交人: {开发者}

## 审查范围
- 文件数: {N}个
- 代码行数: {N}行
- 主要变更: {模块名称}

# 🔍 审查清单

## 功能性
- [ ] 是否实现了所有验收标准？
- [ ] 是否处理了异常情况？
- [ ] 是否有潜在的空指针异常？
- [ ] 边界条件是否考虑周全？

## 安全性
- [ ] 是否使用参数化查询？
- [ ] 是否有SQL注入风险？
- [ ] 密码是否加密存储？
- [ ] 敏感信息是否脱敏？

## 性能
- [ ] 是否有N+1查询问题？
- [ ] 是否需要添加索引？
- [ ] 是否有不必要的循环？
- [ ] 是否有内存泄漏风险？

## 可维护性
- [ ] 命名是否清晰？
- [ ] 是否有过长的方法（>50行）？
- [ ] 是否有重复代码？
- [ ] 注释是否完整？

## 测试
- [ ] 单元测试覆盖率是否达标？
- [ ] 是否测试了异常场景？
- [ ] 是否有集成测试？

# 📤 输出要求
请生成Code Review报告，包含：
1. 总体评价（✅通过 / ⚠️需修改 / ❌不通过）
2. 详细问题清单（按优先级分类）
3. 改进建议
4. 优点总结

问题分级：
- P0：必须修复（阻塞合并）
- P1：建议修复（合并前修复）
- P2：可选修复（后续优化）
```

---

### 模板4: 性能优化

```markdown
# ⚡ 性能优化任务

## 性能问题
- 问题描述: {描述}
- 当前性能: {指标}
- 目标性能: {指标}
- 影响范围: {模块}

## 性能指标
- 响应时间: P95 {N}秒 -> 目标 <{N}秒
- 吞吐量: {N} QPS -> 目标 >{N} QPS
- 资源占用: CPU {N}% / 内存 {N}MB

# 🔍 分析要求
请帮我：
1. 分析性能瓶颈（数据库/网络/计算/内存）
2. 使用工具定位问题代码
3. 提出优化方案（至少3个）
4. 评估优化效果
5. 建议性能测试用例

# 🎯 优化要求
- 不改变功能行为
- 向后兼容
- 代码可读性不降低
- 添加性能测试

请先分析瓶颈，不要直接优化代码。
```

---

### 模板5: 子代理任务分解

```markdown
# 🤖 多任务并行执行

我需要完成以下任务，请为每个任务创建独立的子代理：

## 任务1 - {任务名称}（子代理A）
**目标**: {目标描述}

**具体工作**:
- [ ] {工作项1}
- [ ] {工作项2}
- [ ] {工作项3}

**输出**: {输出物}

---

## 任务2 - {任务名称}（子代理B）
**目标**: {目标描述}

**具体工作**:
- [ ] {工作项1}
- [ ] {工作项2}
- [ ] {工作项3}

**输出**: {输出物}

---

## 任务3 - {任务名称}（子代理C）
**目标**: {目标描述}

**具体工作**:
- [ ] {工作项1}
- [ ] {工作项2}
- [ ] {工作项3}

**输出**: {输出物}

---

# 🎯 协调要求
- 每个子代理独立工作
- 按优先级顺序执行
- 任务完成后汇报进度
- 最终生成汇总报告

请开始执行。
```

**使用示例**：

```markdown
# 🤖 多任务并行执行

我需要完成US010的以下任务，请为每个任务创建独立的子代理：

## 任务1 - 后端API实现（子代理A）
**目标**: 实现报表预览接口

**具体工作**:
- [ ] 创建ReportPreviewController
- [ ] 实现SQL校验逻辑
- [ ] 实现查询执行逻辑
- [ ] 添加单元测试（覆盖率>80%）
- [ ] 添加API文档（Swagger）

**输出**: 
- ReportPreviewController.java
- SqlValidator.java
- ReportQueryService.java
- 测试类

---

## 任务2 - 前端组件开发（子代理B）
**目标**: 实现SQL编辑器组件

**具体工作**:
- [ ] 集成Monaco Editor
- [ ] 实现SQL语法高亮
- [ ] 实现参数输入表单
- [ ] 实现查询结果展示
- [ ] 添加组件测试

**输出**:
- SqlEditor.vue
- ParamForm.vue
- ResultTable.vue
- 测试文件

---

## 任务3 - 集成测试（子代理C）
**目标**: 编写端到端测试

**具体工作**:
- [ ] 编写API集成测试
- [ ] 编写前端E2E测试
- [ ] 编写性能测试
- [ ] 生成测试报告

**输出**:
- 集成测试类
- E2E测试脚本
- 测试报告

---

# 🎯 协调要求
- 任务1和任务2可以并行（独立开发）
- 任务3依赖任务1和任务2完成
- 每完成一个任务汇报进度
- 最终生成US010完成报告

请按优先级开始执行。
```

---

## 工作流程

### 标准并行开发流程

#### 阶段1: Sprint规划（0.5天）

```powershell
# 1. Sprint Planning会议
# - 确定Must-Have Story
# - 评估工作量
# - 分配Story

# 2. 创建Worktree环境
cd d:\Programs\GCTReporter

# 同步develop
git checkout develop
git pull origin develop

# 为每个Story创建worktree
git worktree add ..\GCTReporter-worktrees\feature-US010 -b feature/US010-sql-editor
git worktree add ..\GCTReporter-worktrees\feature-US011 -b feature/US011-excel-export
git worktree add ..\GCTReporter-worktrees\feature-US012 -b feature/US012-permission

# 验证
git worktree list
```

#### 阶段2: 并行开发（7天）

```powershell
# 每天工作流程

# 上午9:00-12:00
# - 窗口1: 开发US010（主要任务）
# - 窗口2: 开发US011（次要任务）

# 下午13:30-17:30
# - 窗口1: 继续US010
# - 窗口3: Code Review同事的PR（不打断主任务）

# 每天结束前
# - 提交所有改动
# - 推送到远程
# - 更新Story状态
```

**Copilot使用技巧**：

```markdown
# 每天开始时（在每个窗口）
请根据昨天的进度，总结今天需要完成的任务：
- 昨天完成了：{功能A、功能B}
- 今天计划：{功能C、功能D}
- 预计时间：{N小时}

请帮我制定今天的开发计划。

# 每天结束时
请总结今天的工作进度：
- 完成的功能：{列表}
- 未完成的任务：{列表}
- 遇到的问题：{列表}
- 明天计划：{列表}
```

#### 阶段3: 集成测试（1天）

```powershell
# 合并所有分支到develop
# - US010合并后，其他Story基于新develop开发
# - 解决冲突
# - 运行集成测试
```

#### 阶段4: 清理与发布（0.5天）

```powershell
# 清理所有worktree
cd d:\Programs\GCTReporter
git worktree remove ..\GCTReporter-worktrees\feature-US010
git worktree remove ..\GCTReporter-worktrees\feature-US011
git worktree remove ..\GCTReporter-worktrees\feature-US012
git worktree prune

# 创建release分支
git checkout -b release/v1.0.0 develop
```

---

## 最佳实践

### ✅ 推荐做法

#### 1. 保持Copilot上下文聚焦

```markdown
# ✅ 好的做法：明确上下文
窗口1（US010）: 只讨论SQL编辑器相关内容
窗口2（US011）: 只讨论Excel导出相关内容
窗口3（Bug修复）: 只讨论Bug相关内容

# ❌ 避免：在一个窗口混合讨论多个任务
在US010窗口问："顺便帮我看看US011的导出逻辑..."
→ 会混淆Copilot的上下文
```

#### 2. 使用Todo List跟踪进度

```markdown
# 在每个窗口的Copilot Chat中维护Todo List

## US010 - SQL编辑器进度
- [x] 集成Monaco Editor
- [x] 实现语法高亮
- [ ] 实现自动补全（进行中 70%）
- [ ] 参数输入表单
- [ ] 查询结果展示

每完成一项，更新状态。
```

#### 3. 定期同步基础分支

```powershell
# 每天开始前，在每个worktree同步develop
cd d:\Programs\GCTReporter-worktrees\feature-US010
git fetch origin
git rebase origin/develop

# 避免最后合并时大量冲突
```

#### 4. 合理利用Copilot的记忆能力

```markdown
# 在长时间开发后，提醒Copilot上下文

我们正在开发US010 - SQL编辑器功能，目前完成了：
1. Monaco Editor集成 ✅
2. 语法高亮 ✅
3. 自动补全 ✅

接下来要实现参数输入表单，请基于上述已完成的代码继续开发。
```

#### 5. 利用Copilot生成测试用例

```markdown
# 在每个功能完成后

刚刚实现了SqlValidator.java，请帮我生成完整的单元测试：
- 测试正常的SELECT语句
- 测试SQL注入场景（至少5个）
- 测试SQL关键字黑名单
- 测试超时控制
- 测试参数校验

要求：
- 使用JUnit 5
- 覆盖率>80%
- 包含边界测试
```

---

### ❌ 避免的陷阱

#### 陷阱1: 在一个窗口频繁切换分支

```powershell
# ❌ 错误做法
cd worktree-US010
git checkout feature/US011  # 切换到US011
# Copilot上下文混乱，之前的对话历史不适用

# ✅ 正确做法
# US010和US011各自有独立的worktree和VS Code窗口
```

#### 陷阱2: 忘记同步其他窗口的代码

```powershell
# 场景：US010先完成并合并到develop
cd worktree-US010
git push origin feature/US010
# 在GitHub合并PR

# ⚠️ 警告：其他worktree还是旧的develop
cd worktree-US011  # 基于旧的develop，可能缺少US010的代码

# ✅ 解决：及时同步
cd worktree-US011
git fetch origin
git rebase origin/develop  # 获取US010的更新
```

#### 陷阱3: Copilot生成的代码不一致

```markdown
# 问题：在不同窗口让Copilot生成相同的工具类

窗口1（US010）：请生成一个DateUtils工具类
窗口2（US011）：请生成一个DateUtils工具类

# 结果：两个窗口生成的DateUtils不一样，合并时冲突

# ✅ 解决：
# 1. 公共代码在一个窗口统一生成
# 2. 提交后其他窗口同步
# 3. 或者使用子代理在一个会话中生成
```

#### 陷阱4: 过度依赖Copilot

```markdown
# ❌ 错误：不审查Copilot生成的代码

Copilot生成了ReportService，直接提交
→ 可能包含安全漏洞或性能问题

# ✅ 正确：人工审查

1. Copilot生成代码
2. 人工审查：
   - 逻辑是否正确
   - 是否有安全风险
   - 性能是否可接受
   - 是否符合项目规范
3. 运行测试验证
4. Code Review
5. 提交
```

---

## 常见问题

### Q1: 如何在多个窗口间共享代码片段？

**方案1：通过Git提交**（推荐）

```powershell
# 窗口1: 创建公共工具类
cd worktree-US010
# 创建 src/main/java/com/gct/common/DateUtils.java
git add .
git commit -m "feat(common): 添加DateUtils工具类"
git push origin feature/US010

# 合并到develop
# 在GitHub创建PR并合并

# 窗口2: 同步更新
cd worktree-US011
git fetch origin
git rebase origin/develop  # 获取DateUtils
```

**方案2：手动复制**（临时）

```powershell
# 从worktree-US010复制到worktree-US011
Copy-Item worktree-US010\src\main\java\com\gct\common\DateUtils.java `
          worktree-US011\src\main\java\com\gct\common\DateUtils.java
```

### Q2: Copilot上下文混乱怎么办？

**症状**：
- Copilot建议的代码与当前任务无关
- 引用了不存在的文件或类
- 代码风格突然改变

**解决方案**：

```markdown
# 方案1: 重置上下文
关闭当前VS Code窗口
重新打开worktree
新建Copilot Chat会话

# 方案2: 明确提示Copilot
我们当前在开发US010 - SQL编辑器功能，
工作目录是 feature/US010-sql-editor 分支。

请忽略之前的对话，专注于SQL编辑器相关的开发。

当前任务：实现Monaco Editor集成。

# 方案3: 使用@workspace限定范围
@workspace 请帮我实现SqlEditor.vue组件
```

### Q3: 多个worktree如何共享依赖？

**Java后端（Maven）**：

```xml
<!-- 共享.m2本地仓库，无需重复下载 -->
<localRepository>${user.home}/.m2/repository</localRepository>

<!-- 每个worktree运行mvn install时共享依赖 -->
```

**前端（npm）**：

```powershell
# 方案1: 每个worktree独立安装（推荐）
cd worktree-US010/frontend
npm install  # 独立node_modules

cd worktree-US011/frontend
npm install  # 独立node_modules

# 优点：环境隔离，不相互影响
# 缺点：占用更多磁盘空间

# 方案2: 使用pnpm（推荐）
# pnpm使用硬链接，节省磁盘空间
npm install -g pnpm
cd worktree-US010/frontend
pnpm install  # 共享.pnpm-store
```

### Q4: 如何在worktree间快速切换？

**PowerShell快捷命令**：

```powershell
# 添加到 $PROFILE
function goto-us010 { Set-Location "d:\Programs\GCTReporter-worktrees\feature-US010" }
function goto-us011 { Set-Location "d:\Programs\GCTReporter-worktrees\feature-US011" }
function goto-main { Set-Location "d:\Programs\GCTReporter" }

# 使用
goto-us010  # 快速切换到US010 worktree
code .      # 在VS Code中打开
```

**VS Code快捷方式**：

```json
// 在VS Code中添加Workspace
// File -> Add Folder to Workspace
// 保存为 GCTReporter.code-workspace

{
  "folders": [
    { "path": "d:\\Programs\\GCTReporter", "name": "Main" },
    { "path": "d:\\Programs\\GCTReporter-worktrees\\feature-US010", "name": "US010" },
    { "path": "d:\\Programs\\GCTReporter-worktrees\\feature-US011", "name": "US011" }
  ]
}

// 在同一个VS Code窗口管理所有worktree
```

### Q5: 如何协调团队成员使用worktree？

**协作规范**：

```markdown
# 团队协作约定

## 分支命名
- feature/{Story编号}-{描述}
- bugfix/{描述}
- hotfix/{描述}

## Worktree命名（本地）
- 每人自己决定worktree目录名称
- 建议：feature-{Story编号}

## 沟通
- 每天同步：哪个Story在哪个worktree开发
- Slack消息：@team 我在开发US010（feature/US010分支）
- 避免多人同时修改同一个文件

## 冲突处理
- 优先合并完成的分支
- 其他人及时rebase
- 沟通解决冲突
```

**示例对话**：

```
开发者A: @team 我刚完成US010并合并到develop，请大家同步一下。
开发者B: 收到，我这边US011正在进行，马上rebase。
cd worktree-US011
git fetch origin
git rebase origin/develop
# 解决冲突...
git rebase --continue
```

### Q6: Copilot生成的代码质量如何保证？

**质量检查清单**：

```markdown
# Copilot代码质量检查

## 功能性
- [ ] 实现了需求的所有功能
- [ ] 边界条件处理正确
- [ ] 异常处理完整

## 安全性
- [ ] 无SQL注入风险
- [ ] 密码已加密
- [ ] 敏感信息已脱敏
- [ ] 输入已校验

## 性能
- [ ] 无明显性能问题
- [ ] 数据库查询已优化
- [ ] 无内存泄漏

## 可维护性
- [ ] 命名清晰
- [ ] 代码结构合理
- [ ] 注释完整
- [ ] 符合项目规范

## 测试
- [ ] 单元测试覆盖率>80%
- [ ] 集成测试通过
- [ ] 边界测试完整
```

**审查流程**：

```markdown
1. Copilot生成代码
2. 开发者审查（参考清单）
3. 运行测试（单元测试 + 集成测试）
4. 静态代码检查（SonarLint/ESLint）
5. Code Review（至少1人）
6. 合并到develop
```

---

## 总结

### 核心价值

| 传统开发 | Copilot + Worktree并行开发 | 效率提升 |
|---------|---------------------------|---------|
| 单任务串行 | 多任务并行 | 🚀 3倍 |
| 频繁切换分支 | 独立环境 | ⏱️ 节省30%时间 |
| 上下文丢失 | 保持状态 | 🧠 减少认知负担 |
| 紧急Bug打断 | 独立修复 | 😌 心流不中断 |
| AI上下文混乱 | 独立会话 | 🎯 建议更精准 |

### 实施建议

**Week 1: 试用期**
- 从1个Story开始，熟悉worktree操作
- 学习Copilot提示词技巧
- 建立个人工作流程

**Week 2: 扩展期**
- 同时开发2-3个Story
- 尝试前后端并行开发
- 优化提示词模板

**Week 3: 成熟期**
- 全面采用多任务并行
- 团队协作规范建立
- 持续改进工作流程

### 关键成功因素

1. ✅ **工具熟练**：熟练使用Git Worktree和VS Code
2. ✅ **提示词优化**：编写高质量的Copilot提示词
3. ✅ **代码审查**：不盲目信任AI生成的代码
4. ✅ **团队协作**：建立清晰的沟通规范
5. ✅ **持续改进**：根据实践优化流程

---

## 附录

### 相关文档

- [Git Worktree最佳实践指南](Git-Worktree最佳实践指南.md)
- [Git工作流规范](../.github/instructions/git-workflow.md)
- [GitHub Copilot使用指南](GitHub-Copilot使用指南.md)

### 工具推荐

- **VS Code扩展**:
  - GitHub Copilot
  - GitHub Copilot Chat
  - GitLens（可视化Git历史）
  - Project Manager（快速切换项目）

- **命令行工具**:
  - [GitHub CLI (gh)](https://cli.github.com/)
  - [tig](https://jonas.github.io/tig/)（Git命令行工具）

---

**最后更新**: 2026-01-16  
**文档版本**: v1.0  
**维护人**: 技术组
