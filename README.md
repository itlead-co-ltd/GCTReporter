# GCT Reporter - 程序员报表生成工具

> **项目类型**: 低代码报表平台MVP
> **技术架构**: 三端分离（管理端/设计端/用户端）
> **开发阶段**: 需求设计阶段
> **项目状态**: 🟡 规划中

---

## 📋 项目概览

GCT Reporter是一个面向程序员的轻量级报表生成工具，支持通过SQL查询快速创建、分享和管理数据报表。本项目采用三端架构设计，为不同角色提供专业的功能模块。

### 核心特性

- ✅ **SQL查询管理**: 支持SELECT语句编写，参数化查询，SQL安全校验
- ✅ **报表模板设计**: 动态列配置，数据格式化（日期/数字/货币）
- ✅ **三端架构**: 管理端（用户管理）、设计端（报表设计）、用户端（报表查询）
- ✅ **Excel导出**: 支持导出查询结果为Excel文件（最多5000行）
- ✅ **RBAC权限控制**: 基于角色的权限管理（ADMIN/DESIGNER/VIEWER）
- ✅ **报表预览**: 设计阶段测试执行，验证SQL正确性

### 项目约束

- **人员**: 2名开发人员（熟悉SpringBoot + Vue框架）
- **时间**: MVP交付周期2周（10个工作日）
- **技术**: 后端Java SpringBoot，前端Vue.js，数据库SQLite（开发）/Oracle 12g（生产）
- **部署**: 单机部署，暂不考虑分布式

---

## 🎯 业务价值

### 为客户（终端用户）
- 多角色统一平台，权限清晰
- 报表查询简单快捷，无需编写SQL
- Excel导出方便后续分析

### 为设计者（报表开发者）
- SQL编辑器支持语法高亮
- 报表预览功能验证配置
- 权限分配灵活控制

### 为管理员
- 用户和角色集中管理
- 全局浏览所有报表
- 系统配置统一维护

### 为企业
- 降低报表开发和维护成本30-50%
- 数据资产集中管理
- 支持快速业务需求响应

---

## 🏗️ 项目架构

### 三端架构设计

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│   管理端      │     │   设计端      │     │   用户端      │
│  (Admin)     │     │ (Designer)   │     │  (Viewer)    │
├──────────────┤     ├──────────────┤     ├──────────────┤
│• 用户管理    │     │• SQL编辑器   │     │• 报表列表    │
│• 角色分配    │     │• 参数配置    │     │• 参数输入    │
│• 全局浏览    │     │• 列配置      │     │• 执行查询    │
│              │     │• 报表预览    │     │• 数据展示    │
│              │     │• 权限分配    │     │• Excel导出   │
└──────────────┘     └──────────────┘     └──────────────┘
       │                    │                    │
       └────────────────────┴────────────────────┘
                            │
                   ┌────────▼────────┐
                   │   后端API服务   │
                   │  (REST API)     │
                   └────────┬────────┘
                            │
                   ┌────────▼────────┐
                   │   MySQL数据库   │
                   │ (6张核心表)     │
                   └─────────────────┘
```

### 角色权限矩阵

| 功能模块 | ADMIN | DESIGNER | VIEWER |
|---------|-------|----------|--------|
| 用户管理 | ✅ | ❌ | ❌ |
| 报表设计 | ✅ | ✅ | ❌ |
| 报表预览 | ✅ | ✅ | ❌ |
| 报表查询 | ✅ | ✅ | ✅ |
| Excel导出 | ✅ | ✅ | ✅ |

---

## 📊 数据库设计

### 核心表结构（6张表）

```sql
users                  -- 用户表
├── id (PK)
├── username (UNIQUE)
├── password (BCrypt)
├── role (ADMIN/DESIGNER/VIEWER)
└── enabled (Boolean)

reports                -- 报表表
├── id (PK)
├── name (UNIQUE)
├── description
├── sql_content
└── creator_id (FK → users)

report_params          -- 参数表
├── id (PK)
├── report_id (FK → reports)
├── param_name
├── param_type (STRING/NUMBER/DATE/DATETIME)
└── required (Boolean)

report_columns         -- 列配置表
├── id (PK)
├── report_id (FK → reports)
├── field_name
├── display_name
├── column_width
└── format_type

report_permissions     -- 权限表
├── id (PK)
├── report_id (FK → reports)
└── role (ADMIN/DESIGNER/VIEWER)

execution_logs         -- 执行日志表
├── id (PK)
├── user_id (FK → users)
├── report_id (FK → reports)
├── params_json
├── execute_time
└── success (Boolean)
```

---

## 🛠️ 技术栈

### 采用方案：Java SpringBoot（团队技术栈）

```yaml
后端:
  语言: Java 17 LTS
  框架: SpringBoot 3.1.x
  ORM: Spring Data JPA (开发阶段) + MyBatis (生产阶段)
  数据库: 
    开发环境: SQLite 3.x（嵌入式，无需安装）
    生产环境: Oracle 12g（企业级）
  连接池: HikariCP
  Excel: Apache POI 5.x
  安全: Spring Security
  日志: SLF4J + Logback
  版本管理: Flyway
  构建工具: Maven 3.8+

前端:
  语言: TypeScript
  框架: Vue 3.3.x (组合式API)
  构建工具: Vite 4.x
  UI组件库: Element Plus 2.3.x
  代码编辑器: vue-codemirror + CodeMirror 5.65.x
  HTTP客户端: Axios 1.x
  状态管理: Pinia 2.x

数据库方案:
  开发阶段: SQLite 3.x
    - 优势: 零配置、嵌入式、文件数据库
    - 适用: 本地开发、快速原型验证
  生产阶段: Oracle 12g
    - 优势: 企业级、高可用、事务支持
    - 迁移: Flyway脚本自动迁移

开发工具:
  IDE: IntelliJ IDEA Community / Eclipse
  API测试: Postman / Insomnia
  版本控制: Git 2.x
  数据库工具: DBeaver / SQL Developer
```

**选择理由**: 
- 团队熟悉SpringBoot + Vue技术栈
- Java强类型保障代码质量
- Spring生态成熟，企业级支持完善
- SQLite简化开发环境搭建
- Oracle 12g满足企业级生产要求

---

## 📂 项目文档结构

```
docs/
├── 01-需求澄清报告.md                          # 需求分析起点
├── 程序员报表生成工具-Epic需求描述.md           # Epic级需求总览
├── 程序员报表生成工具-Epic分析报告.md           # Epic分解分析
├── 程序员报表生成工具-用户故事列表.md           # 完整Story清单
├── 程序员报表生成工具-Must-Have故事.md          # MVP核心功能（30个Story）
├── 程序员报表生成工具-INVEST评估报告.md         # Story质量评估
├── 程序员报表生成工具-验收标准.md               # 详细验收标准（GWT格式）
├── 程序员报表生成工具-后端技术选型对比.md       # Java vs Python技术对比
├── 程序员报表生成工具-设计端-交互设计.md        # 前端交互设计
├── 程序员报表生成工具-设计端-设计元素分析.md    # 设计规范
├── 程序员报表生成工具-设计端任务拆解.md         # 前端任务分解
├── 程序员报表生成工具-项目分析报告.md           # 综合分析报告
├── 核心功能优化说明-v2.0.md                    # 版本迭代说明
└── 程序员报表生成工具-原型/
    ├── report-list.html                        # 报表列表原型
    ├── report-create-step1-2.html              # 报表创建第1-2步
    ├── report-create-step3-4.html              # 报表创建第3-4步
    └── ...                                     # 其他原型页面
```

---

## 🚀 快速开始

### 前置要求

```bash
# 系统要求
- Java 17 LTS
- Node.js 18.x LTS
- SQLite 3.x（开发环境，通常系统自带或自动下载）
- Oracle 12g（生产环境，可选）
- Maven 3.8+
- Git 2.x

# 推荐工具
- IntelliJ IDEA Community（推荐）或 Eclipse
- DBeaver（数据库管理工具，支持SQLite和Oracle）
- Postman（API测试）
```

### 环境搭建

```bash
# 1. 克隆项目（待创建）
git clone https://github.com/your-org/gct-reporter.git
cd gct-reporter

# 2. 后端环境搭建
cd backend
mvn clean install

# 3. 配置数据库（开发环境使用SQLite，无需额外配置）
# SQLite数据库文件会自动创建在项目根目录：data/report_generator.db
# 如需修改配置，编辑 src/main/resources/application-dev.yml

# 执行数据库迁移（Flyway自动执行）
mvn spring-boot:run
# 首次启动会自动创建SQLite数据库文件和表结构

# 4. 启动后端服务（默认8080端口）
# 访问 http://localhost:8080
# 访问 http://localhost:8080/swagger-ui.html 查看API文档

# 5. 前端环境搭建
cd ../frontend
npm install

# 6. 启动前端服务
npm run dev
# 访问 http://localhost:5173

# 7. 生产环境迁移到Oracle（可选）
# 编辑 src/main/resources/application-prod.yml
# 修改数据库连接为Oracle 12g
# spring:
#   datasource:
#     url: jdbc:oracle:thin:@//host:1521/service_name
#     username: report_user
#     password: your_password
#     driver-class-name: oracle.jdbc.OracleDriver
```

### 测试账号

```yaml
管理员账号:
  用户名: admin
  密码: admin123
  权限: 所有功能

设计者账号:
  用户名: designer
  密码: designer123
  权限: 报表设计、查询

普通用户账号:
  用户名: viewer
  密码: viewer123
  权限: 报表查询、导出
```

---

## 📈 开发计划

### MVP功能范围（Must Have）

**30个核心用户故事，预估工作量**:
- Java SpringBoot: **39人日**

### Sprint规划（2人团队，共3个Sprint）

#### Sprint 1（第1-2周）- 基础设施 + 管理端 + 设计端核心
**目标**: 搭建框架，完成报表设计功能

- US000: 开发环境搭建（1天）
- US001-004: 基础设施层（登录、Session、数据库）
- US005-008: 管理端（用户管理、角色分配）
- US010-016: 设计端核心（SQL编辑器、参数/列配置、报表CRUD）

**工作量**: 22人日

**交付物**:
- ✅ 用户能够登录系统
- ✅ 管理员能够管理用户和角色
- ✅ 设计者能够创建、编辑、删除报表
- ✅ SQL编辑器支持语法高亮

---

#### Sprint 2（第3周）- 设计端预览与授权 + 技术增强
**目标**: 完成报表预览和权限分配

- US017-018: 报表预览、权限分配
- US033-034: SQL安全校验、动态列渲染

**工作量**: 9人日

**交付物**:
- ✅ 设计者能够测试执行报表
- ✅ 设计者能够为报表分配角色权限
- ✅ SQL安全校验防止恶意查询

---

#### Sprint 3（第4周）- 用户端查询与展示 + 权限控制
**目标**: 完成用户端查询和展示功能

- US021-026: 用户端报表列表、查询、展示、分页
- US028: Excel导出
- US029-032: 权限控制层（角色权限矩阵、拦截器、日志）

**工作量**: 14.5人日

**交付物**:
- ✅ 普通用户能够查看授权报表列表
- ✅ 普通用户能够输入参数并执行查询
- ✅ 查询结果以表格形式展示
- ✅ 用户能够导出Excel文件
- ✅ 权限控制生效

---

### MVP之外的功能（V2版本）

以下功能已规划但不在MVP范围内：

- ❌ 高级分组权限（组织架构树）- 8人日
- ❌ 报表样式美化（配色主题、模板）- 5人日
- ❌ 复杂参数联动（省市区级联）- 3人日
- ❌ 图表支持（ECharts集成）- 5人日
- ❌ 定时任务（邮件发送）- 5人日
- ❌ 数据缓存优化（Redis）- 5人日

**延期理由**: MVP重功能轻美观，时间优先保证核心功能

---

## 📝 开发规范

### 代码规范

**Java代码规范**:
- 遵循阿里巴巴Java开发手册
- 使用Lombok减少样板代码
- 使用CheckStyle检查代码风格
- 使用SonarLint进行代码质量检查

**前端代码规范**:
- 遵循Vue 3官方风格指南
- 使用ESLint + Prettier格式化
- 使用TypeScript类型检查
- 组件命名采用PascalCase

### Git提交规范

```bash
# 提交信息格式
<type>(<scope>): <subject>

# 类型（type）
feat: 新功能
fix: 修复Bug
docs: 文档更新
style: 代码格式调整（不影响功能）
refactor: 重构代码
test: 测试相关
chore: 构建工具或辅助工具变动

# 示例
feat(user): 添加用户登录功能
fix(report): 修复报表查询参数为空的Bug
docs(readme): 更新项目README文档
```

### 分支管理

```bash
main        # 主分支，受保护，仅合并经过审查的代码
develop     # 开发分支，日常开发合并目标
feature/*   # 功能分支，从develop创建
bugfix/*    # Bug修复分支
release/*   # 发布分支
```

---

## 🧪 测试策略

### 测试覆盖目标

```yaml
后端测试:
  单元测试覆盖率: >80%
  API集成测试: 100%核心接口
  SQL安全测试: 100%SQL注入场景

前端测试:
  组件单元测试: >60%
  E2E测试: 核心流程（登录、创建报表、查询）
```

### 测试框架

**Java SpringBoot**:
- JUnit 5（单元测试）
- MockMvc（API集成测试）
- Mockito（Mock框架）
- JaCoCo（覆盖率统计）
- Selenium / Playwright（E2E测试）

---

## 🔒 安全要求

### SQL安全

```java
// ✅ 正确示例（PreparedStatement参数化查询）
public List<Map<String, Object>> executeQuery(String sql, Map<String, Object> params) {
    // 使用JdbcTemplate的NamedParameterJdbcTemplate，参数化执行
    NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    return jdbcTemplate.queryForList(sql, params);
}

// ❌ 错误示例（字符串拼接，SQL注入风险）
public List<Map<String, Object>> executeQueryUnsafe(String sql, Map<String, Object> params) {
    // 危险！不要这样做
    for (Map.Entry<String, Object> entry : params.entrySet()) {
        sql = sql.replace(":" + entry.getKey(), String.valueOf(entry.getValue()));
    }
    return jdbcTemplate.queryForList(sql);
}
```

### 安全要求清单

- ✅ SQL注入防护（PreparedStatement/参数化查询）
- ✅ 密码加密存储（BCrypt）
- ✅ Session安全（超时30分钟）
- ✅ SQL关键字黑名单（DROP/DELETE/TRUNCATE等）
- ✅ 查询超时控制（5秒）
- ✅ 使用只读数据库账号执行查询
- ✅ 执行日志记录（审计）
- ✅ CORS跨域配置（限制来源）

---

## 📊 性能指标

### 性能要求

| 指标 | 要求 | 测试方法 |
|------|------|---------|
| 1000行数据查询 | P95 < 3秒 | JMeter压力测试 |
| Excel导出（1000行） | < 5秒 | 功能测试 |
| 报表列表加载 | < 1秒 | Lighthouse性能测试 |
| 登录响应时间 | P95 < 2秒 | 50并发用户测试 |
| 5用户并发查询 | 无阻塞 | 并发测试 |

### 性能优化策略

**数据库优化**:
- 索引：username、report_id、execute_time字段
- 连接池：HikariCP（最大连接数10）
- 查询超时：5秒限制
- SQLite开发环境：使用WAL模式提升并发性能
- Oracle生产环境：使用分区表、物化视图优化查询

**前端优化**:
- 虚拟滚动（大数据量表格）
- 懒加载（报表列表分页加载）
- 防抖/节流（搜索输入框）
- 代码分割（路由懒加载）

---

## 🛡️ 质量保障

### 定义完成（Definition of Done）

每个用户故事必须满足：

- [ ] ✅ 代码编写完成并通过编译
- [ ] ✅ 单元测试编写并通过（覆盖率>80%）
- [ ] ✅ 代码审查通过（至少1人Review）
- [ ] ✅ 集成测试通过
- [ ] ✅ 满足所有验收标准（AC）
- [ ] ✅ 无P0/P1级别Bug
- [ ] ✅ API文档更新
- [ ] ✅ 提交到develop分支

### 质量门禁

**代码提交前**:
- 通过ESLint/mypy静态检查
- 通过单元测试
- 代码格式化（Prettier/Black）

**合并到develop前**:
- 代码审查通过
- 集成测试通过
- 无冲突

**发布到main前**:
- 所有测试通过
- 性能测试达标
- 安全扫描无高危漏洞
- 产品验收通过

---

## 📞 联系方式

### 项目负责人

```yaml
项目经理: TBD
技术负责人: TBD
产品经理: TBD
```

### 沟通渠道

- 项目看板: [GitHub Projects / Jira / Trello]
- 文档协作: [Confluence / Notion]
- 即时通讯: [企业微信 / Slack / Teams]
- 代码仓库: [GitHub / GitLab / Gitee]

---

## 📚 参考资源

### 技术文档
- [SpringBoot官方文档](https://spring.io/projects/spring-boot)
- [Spring Data JPA文档](https://spring.io/projects/spring-data-jpa)
- [Vue 3官方文档](https://cn.vuejs.org/)
- [Element Plus文档](https://element-plus.org/)
- [SQLite官方文档](https://www.sqlite.org/docs.html)
- [Oracle 12g文档](https://docs.oracle.com/database/121/index.html)

### 设计规范
- [Material Design](https://material.io/design)
- [Ant Design设计规范](https://ant.design/docs/spec/introduce-cn)
- [Element Plus设计规范](https://element-plus.org/zh-CN/guide/design.html)

### 最佳实践
- [12-Factor App](https://12factor.net/zh_cn/)
- [RESTful API设计规范](https://restfulapi.net/)
- [阿里巴巴Java开发手册](https://github.com/alibaba/p3c)
- [Spring Boot最佳实践](https://spring.io/guides)
- [SQLite最佳实践](https://www.sqlite.org/bestpractice.html)

---

## 📄 许可证

[待定 - MIT / Apache 2.0 / 企业专有]

---

## 🎉 致谢

感谢所有为本项目做出贡献的开发者！

---

**最后更新**: 2026-01-15
**文档版本**: v1.0
**项目状态**: 🟡 需求设计阶段，代码未开始实施
