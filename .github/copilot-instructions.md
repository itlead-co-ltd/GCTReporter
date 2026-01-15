# GCT Reporter - GitHub Copilot 全局指令

> **文档类型**: 项目开发规范与约束
> **适用范围**: 整个GCT Reporter项目
> **生效对象**: 所有开发人员、GitHub Copilot AI助手
> **最后更新**: 2026-01-15

---

## 📋 项目概述

GCT Reporter是一个面向程序员的低代码报表生成工具，采用三端分离架构（管理端/设计端/用户端）。

**核心技术栈**:
- 后端: Java 17 + SpringBoot 3.1.x
- 前端: Vue 3.3.x + TypeScript + Element Plus
- 数据库: SQLite 3.x（开发）/ Oracle 12g（生产）
- 构建工具: Maven 3.8+ / Vite 4.x

**项目约束**:
- 团队规模: 2名开发人员
- 交付周期: 2周（10个工作日）
- MVP工作量: 39人日
- 代码质量要求: 单元测试覆盖率 > 80%

---

## 💻 代码规范

### Java后端代码规范

#### 基础规范
```yaml
编码标准: 阿里巴巴Java开发手册
代码简化: 使用Lombok减少样板代码
代码检查: 
  - CheckStyle: 代码风格检查
  - SonarLint: 代码质量检查
  - PMD: 代码缺陷检查
```

#### 命名规范
```java
// ✅ 正确示例
public class ReportService {
    private static final int MAX_ROWS = 5000;
    
    private final ReportRepository reportRepository;
    
    public ReportDTO createReport(CreateReportRequest request) {
        // 方法名使用动词开头，驼峰命名
    }
    
    private boolean isValidSql(String sql) {
        // 布尔方法使用is/has/can开头
    }
}

// ❌ 错误示例
public class report_service {  // 类名应使用PascalCase
    private int max = 5000;    // 常量应使用全大写+下划线
    
    public void Report(String s) {  // 方法名应使用camelCase
        // ...
    }
}
```

#### 注解使用规范
```java
// Service层
@Service
@RequiredArgsConstructor  // Lombok生成构造器
@Slf4j                    // Lombok生成日志对象
public class ReportService {
    
    private final ReportRepository reportRepository;
    
    // 事务注解
    @Transactional(rollbackFor = Exception.class)
    public Report createReport(CreateReportRequest request) {
        // ...
    }
    
    // 缓存注解（如使用）
    @Cacheable(value = "reports", key = "#id")
    public Report getReportById(Long id) {
        // ...
    }
}

// Controller层
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Validated  // 参数校验
public class ReportController {
    
    @PostMapping
    public ResponseEntity<ReportDTO> createReport(
        @Valid @RequestBody CreateReportRequest request) {
        // @Valid触发参数校验
    }
}
```

#### 异常处理规范
```java
// ✅ 正确示例：统一异常处理
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        log.error("业务异常: {}", e.getMessage(), e);
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(e.getCode(), e.getMessage()));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("系统异常: {}", e.getMessage(), e);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse("SYSTEM_ERROR", "系统错误，请联系管理员"));
    }
}

// ❌ 错误示例：吞掉异常
try {
    // ...
} catch (Exception e) {
    // 不记录日志，不向上抛出
}

// ❌ 错误示例：捕获异常后仅打印堆栈
try {
    // ...
} catch (Exception e) {
    e.printStackTrace();  // 应使用日志框架
}
```

#### 日志规范
```java
// ✅ 正确示例
@Slf4j
public class ReportService {
    
    public Report createReport(CreateReportRequest request) {
        log.info("创建报表开始, 请求参数: {}", request);
        
        try {
            Report report = buildReport(request);
            reportRepository.save(report);
            
            log.info("创建报表成功, 报表ID: {}, 报表名称: {}", 
                report.getId(), report.getName());
            return report;
            
        } catch (Exception e) {
            log.error("创建报表失败, 请求参数: {}, 错误信息: {}", 
                request, e.getMessage(), e);
            throw new BusinessException("CREATE_REPORT_FAILED", "创建报表失败");
        }
    }
}

// 日志级别使用规范
// ERROR: 系统错误，需要立即处理
// WARN:  警告信息，系统可继续运行但需关注
// INFO:  关键业务节点（登录、创建、删除等）
// DEBUG: 调试信息，生产环境关闭
```

#### SQL安全规范（重要！）
```java
// ✅ 正确示例：使用参数化查询
@Repository
public class ReportQueryService {
    
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;
    
    public List<Map<String, Object>> executeQuery(String sql, Map<String, Object> params) {
        // 使用命名参数，防止SQL注入
        return jdbcTemplate.queryForList(sql, params);
    }
}

// ✅ 正确示例：SQL校验
public class SqlValidator {
    
    private static final Set<String> FORBIDDEN_KEYWORDS = Set.of(
        "DROP", "DELETE", "TRUNCATE", "UPDATE", "INSERT", 
        "ALTER", "CREATE", "EXEC", "EXECUTE"
    );
    
    public boolean isValidSql(String sql) {
        String upperSql = sql.trim().toUpperCase();
        
        // 只允许SELECT语句
        if (!upperSql.startsWith("SELECT")) {
            return false;
        }
        
        // 检查是否包含危险关键字
        for (String keyword : FORBIDDEN_KEYWORDS) {
            if (upperSql.contains(keyword)) {
                return false;
            }
        }
        
        return true;
    }
}

// ❌ 错误示例：字符串拼接（SQL注入风险）
public List<Map<String, Object>> executeQueryUnsafe(String sql, Map<String, Object> params) {
    // 危险！不要这样做
    for (Map.Entry<String, Object> entry : params.entrySet()) {
        sql = sql.replace(":" + entry.getKey(), String.valueOf(entry.getValue()));
    }
    return jdbcTemplate.queryForList(sql);
}
```

---

### 前端代码规范

#### Vue 3组合式API规范
```typescript
// ✅ 正确示例：组合式API + TypeScript
<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import type { Report } from '@/types/report'

// Props定义
interface Props {
  reportId: number
  readonly?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  readonly: false
})

// Emits定义
const emit = defineEmits<{
  (e: 'update', report: Report): void
  (e: 'delete', id: number): void
}>()

// 响应式状态
const report = ref<Report | null>(null)
const loading = ref(false)

// 计算属性
const isEditable = computed(() => !props.readonly && report.value?.status === 'DRAFT')

// 方法
const loadReport = async () => {
  loading.value = true
  try {
    const response = await api.getReport(props.reportId)
    report.value = response.data
  } catch (error) {
    console.error('加载报表失败:', error)
    ElMessage.error('加载报表失败')
  } finally {
    loading.value = false
  }
}

// 生命周期
onMounted(() => {
  loadReport()
})
</script>

<template>
  <div class="report-detail">
    <el-skeleton v-if="loading" :rows="5" animated />
    <div v-else-if="report" class="report-content">
      <!-- 内容 -->
    </div>
  </div>
</template>

<style scoped>
.report-detail {
  padding: 20px;
}
</style>
```

#### 命名规范
```typescript
// 组件命名：PascalCase
ReportList.vue
ReportDetail.vue
UserManagement.vue

// 文件命名：kebab-case
report-service.ts
user-api.ts
format-utils.ts

// 变量命名：camelCase
const userName = ref('')
const isLoading = ref(false)
const reportList = ref<Report[]>([])

// 常量命名：UPPER_SNAKE_CASE
const MAX_UPLOAD_SIZE = 5 * 1024 * 1024
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL

// 类型命名：PascalCase
interface UserInfo {
  id: number
  name: string
}

type ReportStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED'
```

#### API调用规范
```typescript
// ✅ 正确示例：统一API管理
// src/api/report.ts
import request from '@/utils/request'
import type { Report, CreateReportRequest } from '@/types/report'

export const reportApi = {
  // 获取报表列表
  getReports: (params?: { page?: number; size?: number }) => 
    request.get<Report[]>('/api/v1/reports', { params }),
  
  // 获取报表详情
  getReport: (id: number) => 
    request.get<Report>(`/api/v1/reports/${id}`),
  
  // 创建报表
  createReport: (data: CreateReportRequest) => 
    request.post<Report>('/api/v1/reports', data),
  
  // 更新报表
  updateReport: (id: number, data: Partial<Report>) => 
    request.put<Report>(`/api/v1/reports/${id}`, data),
  
  // 删除报表
  deleteReport: (id: number) => 
    request.delete(`/api/v1/reports/${id}`)
}

// 使用示例
const loadReports = async () => {
  try {
    const { data } = await reportApi.getReports({ page: 1, size: 20 })
    reports.value = data
  } catch (error) {
    console.error('加载失败:', error)
    ElMessage.error('加载报表列表失败')
  }
}
```

#### 错误处理规范
```typescript
// ✅ 正确示例：统一错误处理
import axios, { type AxiosError } from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000
})

// 响应拦截器
request.interceptors.response.use(
  response => response,
  (error: AxiosError) => {
    if (error.response) {
      const { status, data } = error.response
      
      switch (status) {
        case 401:
          ElMessage.error('未登录或登录已过期')
          router.push('/login')
          break
        case 403:
          ElMessage.error('无权限访问')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error(data?.message || '服务器错误')
          break
        default:
          ElMessage.error(data?.message || '请求失败')
      }
    } else {
      ElMessage.error('网络错误，请检查网络连接')
    }
    
    return Promise.reject(error)
  }
)
```

---

## 🔀 Git提交规范

### 提交信息格式（Conventional Commits）

```bash
<type>(<scope>): <subject>

[optional body]

[optional footer]
```

### Type类型定义

| Type | 说明 | 示例 |
|------|------|------|
| feat | 新功能 | feat(user): 添加用户登录功能 |
| fix | Bug修复 | fix(report): 修复报表查询参数为空的Bug |
| docs | 文档更新 | docs(readme): 更新项目README文档 |
| style | 代码格式调整（不影响功能） | style(report): 格式化ReportService代码 |
| refactor | 重构代码 | refactor(sql): 重构SQL执行引擎 |
| perf | 性能优化 | perf(query): 优化报表查询性能 |
| test | 测试相关 | test(user): 添加用户登录单元测试 |
| chore | 构建工具或辅助工具变动 | chore(deps): 升级SpringBoot版本到3.1.5 |
| ci | CI/CD相关 | ci(github): 添加GitHub Actions构建流程 |
| revert | 回滚提交 | revert: 回滚feat(user)提交 |

### Scope范围定义

```bash
user      # 用户管理模块
report    # 报表管理模块
param     # 参数配置模块
query     # 查询执行模块
export    # Excel导出模块
auth      # 认证授权模块
security  # 安全相关
db        # 数据库相关
ui        # 前端UI相关
api       # API接口相关
```

### 提交示例

```bash
# 好的提交信息
feat(report): 添加报表预览功能

- 实现SQL测试执行接口
- 添加参数输入表单
- 显示查询结果前100行
- 添加执行时间统计

Closes #123

# 简洁的提交信息
fix(query): 修复查询超时问题

# Bug修复
fix(export): 修复Excel导出中文乱码

设置POI工作簿编码为UTF-8

Fixes #456

# 文档更新
docs(api): 更新API文档，添加报表查询接口说明
```

### 禁止的提交信息

```bash
# ❌ 太过简略
fix bug
update code
修改文件

# ❌ 缺少类型
添加用户登录功能
修复报表查询问题

# ❌ 描述不清晰
feat(report): 改了一些东西
fix(user): 修复问题
```

---

## 🌳 分支管理规范

### 分支命名规范

```bash
main                    # 主分支，受保护，仅合并经过审查的代码
develop                 # 开发分支，日常开发合并目标
feature/US001-login     # 功能分支，格式: feature/<Story编号>-<简短描述>
feature/add-excel-export
bugfix/fix-query-timeout    # Bug修复分支
bugfix/SQL-injection-fix
hotfix/critical-security-fix  # 紧急修复分支（生产环境）
release/v1.0.0          # 发布分支
```

### 分支保护规则

**main分支**:
- ✅ 必须通过Pull Request合并
- ✅ 至少1人Code Review通过
- ✅ 所有测试通过
- ✅ 无合并冲突
- ❌ 禁止直接推送

**develop分支**:
- ✅ 建议通过Pull Request合并
- ✅ 代码审查可选
- ✅ 本地测试通过

### 分支工作流

```bash
# 1. 从develop创建功能分支
git checkout develop
git pull origin develop
git checkout -b feature/US010-sql-editor

# 2. 开发功能，频繁提交
git add .
git commit -m "feat(report): 添加SQL编辑器组件"

# 3. 推送到远程
git push origin feature/US010-sql-editor

# 4. 创建Pull Request到develop
# 在GitHub/GitLab界面操作

# 5. Code Review通过后合并

# 6. 删除功能分支
git branch -d feature/US010-sql-editor
git push origin --delete feature/US010-sql-editor
```

---

## 🧪 测试规范

### 测试覆盖率目标

```yaml
后端测试:
  单元测试覆盖率: >80%（强制）
  集成测试覆盖率: >60%
  关键业务逻辑: 100%覆盖
  SQL安全测试: 100%场景覆盖

前端测试:
  组件单元测试: >60%
  E2E测试: 核心流程100%覆盖（登录、创建报表、查询、导出）
```

### 单元测试规范（Java）

```java
// ✅ 正确示例：使用JUnit 5 + Mockito
@ExtendWith(MockitoExtension.class)
class ReportServiceTest {
    
    @Mock
    private ReportRepository reportRepository;
    
    @Mock
    private SqlValidator sqlValidator;
    
    @InjectMocks
    private ReportService reportService;
    
    @Test
    @DisplayName("创建报表 - 成功场景")
    void createReport_Success() {
        // Given（准备测试数据）
        CreateReportRequest request = CreateReportRequest.builder()
            .name("测试报表")
            .sqlContent("SELECT * FROM users")
            .build();
        
        Report savedReport = Report.builder()
            .id(1L)
            .name("测试报表")
            .build();
        
        when(sqlValidator.isValid(anyString())).thenReturn(true);
        when(reportRepository.save(any(Report.class))).thenReturn(savedReport);
        
        // When（执行测试）
        Report result = reportService.createReport(request);
        
        // Then（验证结果）
        assertNotNull(result);
        assertEquals("测试报表", result.getName());
        verify(sqlValidator).isValid("SELECT * FROM users");
        verify(reportRepository).save(any(Report.class));
    }
    
    @Test
    @DisplayName("创建报表 - SQL无效时抛出异常")
    void createReport_InvalidSql_ThrowsException() {
        // Given
        CreateReportRequest request = CreateReportRequest.builder()
            .name("恶意报表")
            .sqlContent("DROP TABLE users")
            .build();
        
        when(sqlValidator.isValid(anyString())).thenReturn(false);
        
        // When & Then
        assertThrows(BusinessException.class, () -> {
            reportService.createReport(request);
        });
        
        verify(reportRepository, never()).save(any());
    }
}
```

### 集成测试规范（Java）

```java
// ✅ 正确示例：Spring Boot集成测试
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional  // 每个测试方法执行后自动回滚
class ReportControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    @DisplayName("创建报表 - API集成测试")
    void createReport_Integration() throws Exception {
        // Given
        CreateReportRequest request = CreateReportRequest.builder()
            .name("集成测试报表")
            .sqlContent("SELECT * FROM users WHERE id = :userId")
            .build();
        
        String requestJson = objectMapper.writeValueAsString(request);
        
        // When & Then
        mockMvc.perform(post("/api/v1/reports")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("集成测试报表"))
            .andExpect(jsonPath("$.id").exists())
            .andDo(print());
    }
    
    @Test
    @DisplayName("查询报表 - 未授权返回403")
    void getReport_Unauthorized_Returns403() throws Exception {
        mockMvc.perform(get("/api/v1/reports/1"))
            .andExpect(status().isForbidden());
    }
}
```

### SQL安全测试规范

```java
// ✅ 必须测试的SQL注入场景
@Test
@DisplayName("SQL注入防护 - DROP TABLE")
void sqlInjection_DropTable_Blocked() {
    String maliciousSql = "SELECT * FROM users; DROP TABLE users; --";
    assertFalse(sqlValidator.isValid(maliciousSql));
}

@Test
@DisplayName("SQL注入防护 - UNION注入")
void sqlInjection_Union_Blocked() {
    String maliciousSql = "SELECT * FROM reports UNION SELECT * FROM users";
    // 根据业务需求决定是否允许UNION
}

@Test
@DisplayName("参数化查询 - 防止注入")
void parameterizedQuery_PreventInjection() {
    String sql = "SELECT * FROM users WHERE id = :userId";
    Map<String, Object> params = Map.of("userId", "1 OR 1=1");
    
    // 验证参数化查询不会被注入
    List<Map<String, Object>> result = queryService.executeQuery(sql, params);
    assertTrue(result.isEmpty() || result.size() == 1);
}
```

---

## 🔒 安全规范

### SQL注入防护（P0级别）

#### 强制要求

```java
// ✅ 必须使用参数化查询
@Repository
public class ReportQueryRepository {
    
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;
    
    // ✅ 正确：使用命名参数
    public List<Map<String, Object>> executeQuery(String sql, Map<String, Object> params) {
        return jdbcTemplate.queryForList(sql, params);
    }
    
    // ❌ 禁止：字符串拼接
    public List<Map<String, Object>> executeQueryUnsafe(String sql, Map<String, Object> params) {
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            sql = sql.replace(":" + entry.getKey(), String.valueOf(entry.getValue()));
        }
        return jdbcTemplate.queryForList(sql);  // 危险！
    }
}
```

#### SQL白名单校验

```java
// ✅ 强制：SQL关键字黑名单
public class SqlValidator {
    
    private static final Set<String> FORBIDDEN_KEYWORDS = Set.of(
        "DROP", "DELETE", "TRUNCATE", "UPDATE", "INSERT",
        "ALTER", "CREATE", "GRANT", "REVOKE", "EXEC", "EXECUTE",
        "SCRIPT", "JAVASCRIPT", "ONERROR", "ONLOAD"
    );
    
    public boolean isValid(String sql) {
        String upperSql = sql.trim().toUpperCase();
        
        // 1. 只允许SELECT语句
        if (!upperSql.startsWith("SELECT")) {
            log.warn("SQL拒绝：不是SELECT语句");
            return false;
        }
        
        // 2. 检查危险关键字
        for (String keyword : FORBIDDEN_KEYWORDS) {
            if (upperSql.contains(keyword)) {
                log.warn("SQL拒绝：包含危险关键字 {}", keyword);
                return false;
            }
        }
        
        // 3. 检查注释符号（防止注释绕过）
        if (upperSql.contains("--") || upperSql.contains("/*") || upperSql.contains("*/")) {
            log.warn("SQL拒绝：包含注释符号");
            return false;
        }
        
        return true;
    }
}

// ✅ 强制：查询超时控制
@Configuration
public class DataSourceConfig {
    
    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setConnectionTimeout(30000);  // 连接超时30秒
        config.setMaxLifetime(1800000);      // 连接最大存活30分钟
        config.setMaximumPoolSize(10);
        
        // ✅ 查询超时5秒
        config.addDataSourceProperty("socketTimeout", "5000");
        
        return new HikariDataSource(config);
    }
}
```

### 密码安全

```java
// ✅ 强制：使用BCrypt加密存储
@Service
public class UserService {
    
    @Autowired
    private PasswordEncoder passwordEncoder;  // BCrypt
    
    public User createUser(CreateUserRequest request) {
        User user = User.builder()
            .username(request.getUsername())
            .password(passwordEncoder.encode(request.getPassword()))  // ✅ 加密
            .build();
        return userRepository.save(user);
    }
    
    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}

// ❌ 禁止：明文存储密码
user.setPassword(request.getPassword());  // 危险！
```

### Session安全

```yaml
# application.yml - 强制配置
server:
  servlet:
    session:
      timeout: 30m              # ✅ Session超时30分钟
      cookie:
        http-only: true         # ✅ 防止XSS攻击
        secure: true            # ✅ 仅HTTPS传输（生产环境）
        same-site: strict       # ✅ 防止CSRF攻击
```

### CORS跨域配置

```java
// ✅ 正确：限制来源
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:5173", "https://report.example.com")  // ✅ 白名单
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
    }
}

// ❌ 禁止：允许所有来源
.allowedOrigins("*")  // 危险！生产环境禁止
```

### 敏感信息保护

```java
// ✅ 正确：日志脱敏
@Slf4j
public class UserService {
    
    public User createUser(CreateUserRequest request) {
        log.info("创建用户, username: {}", request.getUsername());
        // ❌ 不要记录密码
        // log.info("密码: {}", request.getPassword());  
        
        User user = // ...
        return user;
    }
}

// ✅ 正确：配置文件加密（生产环境）
# application-prod.yml
spring:
  datasource:
    password: ENC(加密后的密码)  # 使用jasypt加密
```

---

## ⚡ 性能规范

### 性能指标要求

| 指标 | 要求 | 测试方法 |
|------|------|---------|
| 1000行数据查询 | P95 < 3秒 | JMeter压力测试，50并发 |
| Excel导出（1000行） | < 5秒 | 功能测试 |
| 报表列表加载 | < 1秒 | Lighthouse性能测试 |
| 登录响应时间 | P95 < 2秒 | 50并发用户测试 |
| 5用户并发查询 | 无阻塞 | 并发测试 |

### 数据库优化规范

```sql
-- ✅ 强制：关键字段添加索引
CREATE INDEX idx_username ON users(username);
CREATE INDEX idx_report_id ON report_params(report_id);
CREATE INDEX idx_execute_time ON execution_logs(execute_time);

-- ✅ 建议：复合索引
CREATE INDEX idx_report_creator ON reports(creator_id, created_at);

-- ❌ 避免：全表扫描
SELECT * FROM execution_logs WHERE DATE(execute_time) = '2026-01-15';

-- ✅ 优化：使用范围查询
SELECT * FROM execution_logs 
WHERE execute_time >= '2026-01-15 00:00:00' 
  AND execute_time < '2026-01-16 00:00:00';
```

### 前端性能优化

```typescript
// ✅ 懒加载路由
const routes = [
  {
    path: '/reports',
    component: () => import('@/views/ReportList.vue')  // 懒加载
  }
]

// ✅ 虚拟滚动（大数据量表格）
<el-table-v2
  :columns="columns"
  :data="largeDataList"
  :width="800"
  :height="600"
  :row-height="50"
/>

// ✅ 防抖搜索
import { debounce } from 'lodash-es'

const handleSearch = debounce((keyword: string) => {
  // 搜索逻辑
}, 300)

// ✅ 图片懒加载
<img v-lazy="imageUrl" alt="报表预览" />
```

---

## 🛡️ 质量保障

### Definition of Done（DoD）

每个用户故事必须满足：

- [ ] ✅ 代码编写完成并通过编译
- [ ] ✅ 单元测试编写并通过（覆盖率>80%）
- [ ] ✅ 集成测试通过（关键接口）
- [ ] ✅ 代码审查通过（至少1人Review）
- [ ] ✅ 满足所有验收标准（AC）
- [ ] ✅ 无P0/P1级别Bug
- [ ] ✅ API文档更新（Swagger/JavaDoc）
- [ ] ✅ 提交到develop分支
- [ ] ✅ 通过静态代码检查（SonarLint/ESLint）
- [ ] ✅ 日志记录完整（关键操作）

### 代码审查清单（Code Review Checklist）

#### 功能性
- [ ] 是否实现了所有验收标准？
- [ ] 是否处理了异常情况？
- [ ] 是否有潜在的空指针异常？
- [ ] 边界条件是否考虑周全？

#### 安全性
- [ ] 是否使用参数化查询？
- [ ] 是否有SQL注入风险？
- [ ] 密码是否加密存储？
- [ ] 敏感信息是否脱敏？

#### 性能
- [ ] 是否有N+1查询问题？
- [ ] 是否需要添加索引？
- [ ] 是否有不必要的循环？
- [ ] 是否有内存泄漏风险？

#### 可维护性
- [ ] 命名是否清晰？
- [ ] 是否有过长的方法（>50行）？
- [ ] 是否有重复代码？
- [ ] 注释是否完整？

#### 测试
- [ ] 单元测试覆盖率是否达标？
- [ ] 是否测试了异常场景？
- [ ] 是否有集成测试？

---

## 📦 依赖管理规范

### 后端依赖（Maven）

```xml
<!-- ✅ 使用dependencyManagement统一版本 -->
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-dependencies</artifactId>
            <version>3.1.5</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<!-- ❌ 避免：直接指定版本（可能冲突） -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.15.0</version>  <!-- 可能与SpringBoot版本冲突 -->
</dependency>
```

### 前端依赖（npm）

```json
// ✅ 锁定主要版本号
{
  "dependencies": {
    "vue": "^3.3.4",           // 允许patch版本更新
    "element-plus": "~2.3.14"  // 仅允许patch版本更新
  }
}

// ✅ 使用pnpm-lock.yaml或package-lock.json
// 确保团队使用相同版本

// ❌ 避免：使用*版本号
"axios": "*"  // 危险！可能引入breaking changes
```

---

## 📖 文档规范

### JavaDoc规范

```java
/**
 * 报表服务类
 * 
 * 提供报表的CRUD操作，包括创建、查询、更新、删除等功能。
 * 
 * @author 张三
 * @since 1.0.0
 */
@Service
public class ReportService {
    
    /**
     * 创建报表
     * 
     * @param request 创建报表请求对象
     * @return 创建成功的报表对象
     * @throws BusinessException 当SQL校验失败时抛出
     * @throws IllegalArgumentException 当参数为null时抛出
     */
    public Report createReport(CreateReportRequest request) {
        // ...
    }
}
```

### API文档规范

```java
// ✅ 使用Swagger/SpringDoc注解
@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "报表管理", description = "报表的CRUD操作接口")
public class ReportController {
    
    @PostMapping
    @Operation(summary = "创建报表", description = "创建一个新的报表模板")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "创建成功"),
        @ApiResponse(responseCode = "400", description = "参数错误"),
        @ApiResponse(responseCode = "401", description = "未登录")
    })
    public ResponseEntity<ReportDTO> createReport(
        @Parameter(description = "报表创建请求", required = true)
        @Valid @RequestBody CreateReportRequest request) {
        // ...
    }
}
```

---

## 🚫 禁止事项

### 绝对禁止

1. ❌ **禁止提交敏感信息**
   - 数据库密码
   - API密钥
   - 用户真实数据
   - 内部服务器地址

2. ❌ **禁止字符串拼接SQL**
   ```java
   // 危险！
   String sql = "SELECT * FROM users WHERE id = " + userId;
   ```

3. ❌ **禁止硬编码**
   ```java
   // 错误
   String dbUrl = "jdbc:mysql://192.168.1.100:3306/report";
   
   // 正确
   @Value("${spring.datasource.url}")
   private String dbUrl;
   ```

4. ❌ **禁止吞掉异常**
   ```java
   // 错误
   try {
       // ...
   } catch (Exception e) {
       // 什么都不做
   }
   ```

5. ❌ **禁止使用System.out.println**
   ```java
   // 错误
   System.out.println("用户登录成功");
   
   // 正确
   log.info("用户登录成功, userId: {}", userId);
   ```

---

## ✅ 检查清单

### 开发阶段检查清单

- [ ] 代码符合命名规范
- [ ] 添加了必要的注释和JavaDoc
- [ ] 单元测试覆盖率>80%
- [ ] 通过静态代码检查
- [ ] 日志记录完整
- [ ] SQL使用参数化查询
- [ ] 异常处理完整
- [ ] 性能优化（如需要）

### 提交前检查清单

- [ ] 通过本地所有测试
- [ ] 代码格式化完成
- [ ] 提交信息符合规范
- [ ] 无敏感信息泄露
- [ ] 无console.log/System.out
- [ ] 依赖版本无冲突

### Code Review检查清单

- [ ] 功能实现符合需求
- [ ] 代码可读性良好
- [ ] 无明显性能问题
- [ ] 安全性检查通过
- [ ] 测试覆盖充分
- [ ] 文档更新完整

---

## 📞 问题反馈

如对本规范有疑问或建议，请联系：
- 技术负责人: [TBD]
- 团队讨论: [技术群组]
- 文档更新: 提交Issue到项目仓库

---

**最后更新**: 2026-01-15
**文档版本**: v1.0
**维护人**: 技术组
