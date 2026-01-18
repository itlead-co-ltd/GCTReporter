# Simple Issue Resolver - 使用指南

> 自动化处理简单 GitHub Issue 的完整工作流 Skill

---

## 🎉 Skill 创建完成！

**Skill 名称**: `simple-issue-resolver`  
**版本**: v1.0  
**创建日期**: 2026年1月3日  
**打包文件**: `c:\Programs\CoursePortal\.claude\skills\simple-issue-resolver.skill`

---

## 📦 Skill 结构

```
simple-issue-resolver/
├── SKILL.md (315行)                          # 核心工作流定义
├── references/                                # 详细参考文档
│   ├── implementation-guide.md                # 代码实现指引
│   ├── testing-checklist.md                   # 测试验证清单
│   └── pr-template.md                         # PR 描述模板
└── scripts/                                   # 辅助脚本
    ├── issue_fetcher.py                       # 获取 Issue 详情
    └── pr_creator.py                          # 创建 Pull Request

总计: 6个文件
```

---

## 🚀 快速开始

### 1. 前置准备

#### 必须安装的工具

```powershell
# 1. 安装 GitHub CLI
winget install GitHub.cli

# 2. 登录 GitHub
gh auth login

# 3. 验证安装
gh --version
```

#### 验证环境

```powershell
# 测试 GitHub CLI
gh auth status

# 应该看到: ✓ Logged in to github.com as [你的用户名]
```

---

### 2. 基本使用

#### 方法1: 直接使用 Issue 号码

```
解决 Issue #123
```

#### 方法2: 使用完整 Issue URL

```
implement issue https://github.com/chuanminglu/product-service/issues/5
```

#### 方法3: 指定仓库

```
解决 chuanminglu/product-service 仓库的 Issue #123
```

---

## 📋 完整工作流示例

### 场景: 修复 product-service 项目的一个简单 Bug

假设有如下 Issue:

**Issue #5**: 修复产品价格显示精度问题

**描述**: 
```
产品价格 9.95 显示为 9.949999，需要保留2位小数。

影响文件: ProductService.java
```

### 执行流程

**第1步**: 启动 Skill

```
解决 Issue #5
```

**第2步**: Skill 自动执行

```
📥 读取 Issue #5 详情...
✅ Issue 标题: 修复产品价格显示精度问题
✅ 类型: Bug 修复
✅ 影响范围: 单文件

📊 复杂度评估...
✅ 简单级别（代码行数 <20行）
✅ 自动执行，无需确认

🌿 创建分支: fix/issue-5-price-precision

💻 实现代码...
📝 修改文件: src/main/java/cn/com/itlead/service/ProductService.java
✅ 使用 BigDecimal 替代 double 运算

✅ 测试验证...
   编译检查: ✅ 通过
   单元测试: ✅ 通过 (12/12)
   代码风格: ✅ 通过

📤 创建 Pull Request...
✅ PR #789 已创建
🔗 https://github.com/chuanminglu/product-service/pull/789

🎉 Issue #5 解决完成！
```

---

## 🎯 适用场景

### ✅ 推荐使用的场景

1. **Bug 修复**（单文件修改）
   ```
   示例: 修复邮箱验证正则表达式
   复杂度: 简单
   代码行数: <20行
   ```

2. **小功能添加**
   ```
   示例: 为产品列表添加排序功能
   复杂度: 简单-中等
   代码行数: 30-50行
   ```

3. **配置修改**
   ```
   示例: 添加最大上传文件大小配置
   复杂度: 简单
   影响: application.yml
   ```

4. **文档更新**
   ```
   示例: 更新 README 安装说明
   复杂度: 简单
   影响: README.md
   ```

### ❌ 不适用的场景

1. **架构重构** - 建议人工处理
2. **数据库迁移** - 需要 DBA 审查
3. **安全敏感代码** - 需要安全专家审查
4. **性能优化** - 需要详细测试对比

---

## 🔧 高级用法

### 跳过测试（不推荐）

```
解决 Issue #123 --skip-tests
```

⚠️ **注意**: 跳过测试会在 PR 中标注，需要格外注意 Code Review。

### 指定分支名称

```
解决 Issue #123，使用分支名 feature/custom-branch
```

### 草稿 PR

```
解决 Issue #123，创建草稿 PR
```

---

## 📊 复杂度判断标准

Skill 会自动评估 Issue 复杂度：

| 级别 | 代码行数 | 文件数 | 操作 |
|------|---------|-------|------|
| **简单** | <50行 | 1个 | ✅ 自动执行 |
| **中等** | 50-200行 | 2-5个 | ⚠️ 显示计划，等待确认 |
| **复杂** | >200行 | >5个 | ❌ 建议人工处理 |

### 示例判断

```
Issue: 修复价格显示精度
→ 预估代码: 15行
→ 影响文件: 1个
→ 判断: ✅ 简单，自动执行
```

```
Issue: 添加用户权限管理模块
→ 预估代码: 300行
→ 影响文件: 8个
→ 判断: ❌ 复杂，建议人工
```

---

## 🛠️ 测试验证流程

### 自动执行的测试

1. **编译检查**（P0）
   ```bash
   mvn clean compile  # Java
   pytest --collect-only  # Python
   npm run build  # Node.js
   ```

2. **单元测试**（P0）
   ```bash
   mvn test
   ```

3. **代码风格**（P1）
   ```bash
   mvn checkstyle:check
   ```

### 测试失败处理

```
测试失败
    ↓
🔍 第1次: 自动分析修复
    ↓
🔄 重新测试
    ↓
🔍 第2次: 深度分析修复
    ↓
🔄 重新测试
    ↓
👤 第3次: 人工介入
```

---

## 📝 PR 描述自动生成

### 生成的 PR 示例

````markdown
Fixes #5

## 📋 变更说明

修复产品价格显示精度问题，使用 BigDecimal 替代 double 运算，确保价格显示精确到2位小数。

## 🔧 实现细节

**修改文件**:
- `src/main/java/cn/com/itlead/service/ProductService.java`: 
  使用 BigDecimal 进行价格计算

**代码行数**: +12 -3

## ✅ 测试

- [x] 编译检查通过
- [x] 单元测试通过 (12/12)
- [x] 代码风格检查通过

**新增测试用例**:
- `testGetPrice_Precision`: 验证价格精度保留2位小数

## 📝 相关 Issue

- Closes #5

## 🤖 自动化说明

此 PR 由 `simple-issue-resolver` Skill 自动生成。
如有问题，请在评论中反馈。
````

---

## ⚠️ 注意事项

### 安全提示

- ⚠️ **AI 生成代码仍需人工审查**
- ⚠️ **关键业务代码务必进行充分测试**
- ⚠️ **建议先在开发分支验证，再合并到主分支**

### 质量保障

- ✅ **必须通过**: 编译 + 单元测试
- ⭐ **推荐通过**: 代码风格 + 覆盖率
- 🎯 **最佳实践**: 集成测试 + 安全扫描

### 权限要求

需要 GitHub 仓库的以下权限:
- `repo`: 读取 Issue、创建分支、推送代码
- `workflow`: 触发 GitHub Actions（可选）

---

## 📚 参考文档

### 核心文档

1. **SKILL.md** (315行) - 完整工作流定义
2. **implementation-guide.md** - 代码实现详细指引
3. **testing-checklist.md** - 测试验证清单
4. **pr-template.md** - PR 描述模板库

### 辅助脚本

```bash
# 手动获取 Issue 详情
python scripts/issue_fetcher.py chuanminglu/product-service 5

# 手动创建 PR
python scripts/pr_creator.py \
  --title "fix: 修复价格精度问题" \
  --body-file pr_description.md \
  --base main
```

---

## 🧪 测试建议

### 第一次使用

选择一个**简单的 Bug 修复 Issue**验证 Skill 效果：

**推荐 Issue 特征**:
- 单文件修改
- 逻辑清晰
- 有测试用例
- 代码行数 <20行

### 示例测试 Issue

你可以在 product-service 创建如下测试 Issue:

```markdown
标题: 修复产品名称显示的空格问题

描述: 
产品名称前后有多余空格，显示为 "  iPhone 15  "，
需要去除首尾空格，显示为 "iPhone 15"。

影响文件: Product.java 的 getName() 方法
```

---

## 🐛 故障排查

### 问题1: GitHub CLI 未认证

**症状**: 
```
❌ gh: To authenticate, please run: gh auth login
```

**解决**:
```powershell
gh auth login
# 选择: GitHub.com
# 选择: HTTPS
# 选择: Login with a web browser
```

---

### 问题2: 测试失败

**症状**:
```
❌ 测试验证失败（第3次）
⚠️ 需要人工介入
```

**解决**:
1. 查看测试失败日志
2. 本地检出分支调试
   ```bash
   git checkout fix/issue-123
   mvn test
   ```
3. 手动修复后推送
4. 或关闭 PR，重新尝试

---

### 问题3: PR 创建失败

**症状**:
```
❌ PR 创建失败: branch already has a pull request
```

**解决**:
- 该分支已有 PR，检查是否重复操作
- 或先关闭旧 PR，删除分支后重试

---

## 🎓 最佳实践

### 1. 从简单开始

第一次使用时：
- ✅ 选择简单的 Bug 修复
- ✅ 单文件修改
- ✅ 代码行数 <20行
- ✅ 有现有测试覆盖

### 2. 逐步信任

随着使用经验增加：
- Week 1: 仅用于简单 Bug 修复
- Week 2: 扩展到小功能添加
- Week 3: 尝试中等复杂度的 Issue
- Month 2: 根据团队反馈调整使用范围

### 3. 保持审查

即使 AI 生成：
- 🔍 **必须 Code Review**
- 🔍 **必须手动验证功能**
- 🔍 **必须检查边界条件**
- 🔍 **必须评估是否引入技术债**

---

## 📊 效率提升

### 预期效果

- ⏱️ **简单 Issue**: 从 2小时 → 10分钟（节省 85%）
- 📈 **质量保障**: 自动测试 + 规范流程
- 🎯 **专注价值**: 让开发者专注于复杂问题

### 投资回报

**投资**:
- 第一次使用: 10分钟（阅读文档）
- 后续使用: 1分钟（输入 Issue 号）

**回报**:
- 每个简单 Issue 节省 1.5小时
- 10个 Issue → 节省 15小时

---

## 🚀 下一步

1. **验证环境**: 确保 `gh` CLI 已配置
2. **选择 Issue**: 找一个简单的 Bug 修复 Issue
3. **启动 Skill**: 输入 `解决 Issue #<number>`
4. **审查 PR**: 验证生成的代码和测试
5. **合并代码**: Code Review 通过后合并
6. **反馈优化**: 根据使用体验调整 Skill

---

## 📞 支持与反馈

**遇到问题？**
1. 检查 [故障排查](#-故障排查) 章节
2. 查看详细文档 (SKILL.md)
3. 提交 Issue 反馈

**改进建议？**
- 欢迎提出新功能需求
- 欢迎分享使用经验
- 欢迎贡献代码优化

---

**祝使用愉快！🎉**

*Last updated: 2026-01-03*
