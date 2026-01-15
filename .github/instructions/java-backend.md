# Java后端开发规范

> **适用范围**: GCT Reporter后端开发（Java 17 + SpringBoot 3.1.x）
> **最后更新**: 2026-01-15

---

## 📋 技术栈

```yaml
语言: Java 17 LTS
框架: SpringBoot 3.1.x
ORM: Spring Data JPA (开发) + MyBatis (生产)
数据库: 
  开发环境: SQLite 3.x
  生产环境: Oracle 12g
连接池: HikariCP
Excel: Apache POI 5.x
安全: Spring Security
日志: SLF4J + Logback
版本管理: Flyway
构建工具: Maven 3.8+
```

---

## 💻 代码规范

### 基础规范

```yaml
编码标准: 阿里巴巴Java开发手册
代码简化: 使用Lombok减少样板代码
代码检查: 
  - CheckStyle: 代码风格检查
  - SonarLint: 代码质量检查
  - PMD: 代码缺陷检查
```

### 命名规范

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

**命名约定**:
- **类名**: PascalCase（`ReportService`, `UserController`）
- **方法名**: camelCase（`createReport`, `getUserById`）
- **变量名**: camelCase（`reportList`, `userName`）
- **常量名**: UPPER_SNAKE_CASE（`MAX_ROWS`, `DEFAULT_TIMEOUT`）
- **包名**: 小写+点分隔（`com.gct.report.service`）

---

## 🏗️ 架构规范

### 分层架构

```
Controller层（接口层）
    ↓
Service层（业务逻辑层）
    ↓
Repository层（数据访问层）
    ↓
Entity层（实体层）
```

**职责划分**:
- **Controller**: 接收HTTP请求，参数校验，调用Service，返回响应
- **Service**: 业务逻辑处理，事务控制
- **Repository**: 数据库操作，SQL执行
- **Entity**: 数据模型，与数据库表映射

### 注解使用规范

```java
// ✅ Controller层
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Validated  // 参数校验
@Tag(name = "报表管理", description = "报表的CRUD操作")
public class ReportController {
    
    private final ReportService reportService;
    
    @PostMapping
    @Operation(summary = "创建报表")
    public ResponseEntity<ReportDTO> createReport(
        @Valid @RequestBody CreateReportRequest request) {
        // @Valid触发参数校验
        return ResponseEntity.ok(reportService.createReport(request));
    }
}

// ✅ Service层
@Service
@RequiredArgsConstructor
@Slf4j  // Lombok生成日志对象
public class ReportService {
    
    private final ReportRepository reportRepository;
    private final SqlValidator sqlValidator;
    
    @Transactional(rollbackFor = Exception.class)
    public Report createReport(CreateReportRequest request) {
        // 事务控制
        log.info("创建报表开始, 请求参数: {}", request);
        // ...
    }
    
    @Cacheable(value = "reports", key = "#id")  // 如使用缓存
    public Report getReportById(Long id) {
        return reportRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("报表不存在"));
    }
}

// ✅ Repository层
@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    
    Optional<Report> findByName(String name);
    
    @Query("SELECT r FROM Report r WHERE r.creatorId = :userId")
    List<Report> findByCreator(@Param("userId") Long userId);
}
```

---

## 🔧 异常处理规范

### 统一异常处理

```java
// ✅ 全局异常处理器
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        log.error("业务异常: {}", e.getMessage(), e);
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(e.getCode(), e.getMessage()));
    }
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(ResourceNotFoundException e) {
        log.warn("资源不存在: {}", e.getMessage());
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse("NOT_FOUND", e.getMessage()));
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));
        
        log.warn("参数校验失败: {}", message);
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse("VALIDATION_ERROR", message));
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

### 自定义异常

```java
// 业务异常基类
public class BusinessException extends RuntimeException {
    private final String code;
    
    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }
}

// 具体业务异常
public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String message) {
        super("NOT_FOUND", message);
    }
}

public class SqlValidationException extends BusinessException {
    public SqlValidationException(String message) {
        super("SQL_INVALID", message);
    }
}
```

---

## 📝 日志规范

### 日志级别使用

```java
@Slf4j
public class ReportService {
    
    public Report createReport(CreateReportRequest request) {
        // INFO: 关键业务节点
        log.info("创建报表开始, 用户: {}, 报表名称: {}", 
            getCurrentUserId(), request.getName());
        
        try {
            // DEBUG: 调试信息（生产环境关闭）
            log.debug("SQL校验开始, SQL: {}", request.getSqlContent());
            
            if (!sqlValidator.isValid(request.getSqlContent())) {
                // WARN: 警告信息，系统可继续运行
                log.warn("SQL校验失败, 用户: {}, SQL: {}", 
                    getCurrentUserId(), request.getSqlContent());
                throw new SqlValidationException("SQL包含非法关键字");
            }
            
            Report report = buildReport(request);
            reportRepository.save(report);
            
            log.info("创建报表成功, 报表ID: {}, 报表名称: {}", 
                report.getId(), report.getName());
            return report;
            
        } catch (SqlValidationException e) {
            // ERROR: 业务异常
            log.error("创建报表失败, 用户: {}, 原因: {}", 
                getCurrentUserId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            // ERROR: 系统错误，需要立即处理
            log.error("创建报表失败, 系统异常, 用户: {}, 请求参数: {}", 
                getCurrentUserId(), request, e);
            throw new BusinessException("CREATE_REPORT_FAILED", "创建报表失败");
        }
    }
}
```

**日志级别说明**:
- **ERROR**: 系统错误，需要立即处理（数据库连接失败、系统异常）
- **WARN**: 警告信息，系统可继续运行但需关注（SQL校验失败、参数异常）
- **INFO**: 关键业务节点（用户登录、创建报表、删除数据）
- **DEBUG**: 调试信息，生产环境关闭（SQL内容、参数详情）

### 日志脱敏

```java
// ✅ 正确：敏感信息脱敏
@Slf4j
public class UserService {
    
    public User createUser(CreateUserRequest request) {
        log.info("创建用户, username: {}", request.getUsername());
        // ❌ 不要记录密码
        // log.info("密码: {}", request.getPassword());
        
        // ❌ 不要记录完整的身份证号
        // log.info("身份证: {}", user.getIdCard());
        
        // ✅ 可以记录脱敏后的信息
        log.info("身份证: {}****{}", 
            user.getIdCard().substring(0, 6),
            user.getIdCard().substring(14));
        
        return userRepository.save(user);
    }
}
```

---

## 🔒 SQL安全规范（P0级别）

### 强制使用参数化查询

```java
// ✅ 正确示例：使用NamedParameterJdbcTemplate
@Repository
public class ReportQueryRepository {
    
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;
    
    public List<Map<String, Object>> executeQuery(String sql, Map<String, Object> params) {
        // 使用命名参数，防止SQL注入
        return jdbcTemplate.queryForList(sql, params);
    }
}

// ✅ 正确示例：使用JPA
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    @Query("SELECT u FROM User u WHERE u.username = :username")
    Optional<User> findByUsername(@Param("username") String username);
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

### SQL校验器

```java
@Component
@Slf4j
public class SqlValidator {
    
    private static final Set<String> FORBIDDEN_KEYWORDS = Set.of(
        "DROP", "DELETE", "TRUNCATE", "UPDATE", "INSERT",
        "ALTER", "CREATE", "GRANT", "REVOKE", "EXEC", "EXECUTE",
        "SCRIPT", "JAVASCRIPT", "ONERROR", "ONLOAD"
    );
    
    private static final Pattern COMMENT_PATTERN = Pattern.compile("--|/\\*|\\*/");
    
    public boolean isValid(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            log.warn("SQL校验失败: SQL为空");
            return false;
        }
        
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
        if (COMMENT_PATTERN.matcher(sql).find()) {
            log.warn("SQL拒绝：包含注释符号");
            return false;
        }
        
        // 4. 检查多语句（防止注入）
        if (sql.contains(";")) {
            long selectCount = sql.chars().filter(ch -> ch == ';').count();
            if (selectCount > 1 || !sql.trim().endsWith(";")) {
                log.warn("SQL拒绝：包含多条语句");
                return false;
            }
        }
        
        return true;
    }
}
```

### 查询超时控制

```java
@Configuration
public class DataSourceConfig {
    
    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        
        // 连接池配置
        config.setConnectionTimeout(30000);  // 连接超时30秒
        config.setMaxLifetime(1800000);      // 连接最大存活30分钟
        config.setMaximumPoolSize(10);       // 最大连接数
        config.setMinimumIdle(2);            // 最小空闲连接数
        
        // ✅ 查询超时5秒
        config.addDataSourceProperty("socketTimeout", "5000");
        
        return new HikariDataSource(config);
    }
}
```

---

## 🔐 安全规范

### 密码加密

```java
// ✅ 配置BCrypt密码编码器
@Configuration
public class SecurityConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

// ✅ 使用密码编码器
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public User createUser(CreateUserRequest request) {
        User user = User.builder()
            .username(request.getUsername())
            .password(passwordEncoder.encode(request.getPassword()))  // ✅ 加密
            .role(request.getRole())
            .enabled(true)
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

### Session配置

```yaml
# application.yml
server:
  servlet:
    session:
      timeout: 30m              # Session超时30分钟
      cookie:
        http-only: true         # 防止XSS攻击
        secure: true            # 仅HTTPS传输（生产环境）
        same-site: strict       # 防止CSRF攻击
```

### CORS配置

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            // ✅ 白名单模式
            .allowedOrigins("http://localhost:5173", "https://report.example.com")
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
        
        // ❌ 禁止：允许所有来源（生产环境）
        // .allowedOrigins("*")  
    }
}
```

---

## 🧪 单元测试规范

### JUnit 5 + Mockito

```java
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
            .sqlContent("SELECT * FROM users")
            .build();
        
        when(sqlValidator.isValid(anyString())).thenReturn(true);
        when(reportRepository.save(any(Report.class))).thenReturn(savedReport);
        
        // When（执行测试）
        Report result = reportService.createReport(request);
        
        // Then（验证结果）
        assertNotNull(result);
        assertEquals("测试报表", result.getName());
        assertEquals(1L, result.getId());
        
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
        assertThrows(SqlValidationException.class, () -> {
            reportService.createReport(request);
        });
        
        verify(sqlValidator).isValid("DROP TABLE users");
        verify(reportRepository, never()).save(any());
    }
}
```

### Spring Boot集成测试

```java
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
            .description("集成测试")
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
            .andExpect(jsonPath("$.sqlContent").value("SELECT * FROM users WHERE id = :userId"))
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

---

## 📦 依赖管理

### Maven配置

```xml
<project>
    <properties>
        <java.version>17</java.version>
        <spring-boot.version>3.1.5</spring-boot.version>
    </properties>
    
    <!-- ✅ 使用dependencyManagement统一版本 -->
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>${spring-boot.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
    
    <dependencies>
        <!-- Spring Boot -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        
        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <scope>provided</scope>
        </dependency>
        
        <!-- 测试依赖 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

---

## 📖 JavaDoc规范

```java
/**
 * 报表服务类
 * 
 * 提供报表的CRUD操作，包括创建、查询、更新、删除等功能。
 * 支持SQL安全校验、参数配置、列配置等。
 * 
 * @author 张三
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {
    
    private final ReportRepository reportRepository;
    private final SqlValidator sqlValidator;
    
    /**
     * 创建报表
     * 
     * <p>创建一个新的报表模板，包括SQL内容、参数配置、列配置等。
     * 在创建前会进行SQL安全校验，仅允许SELECT语句。</p>
     * 
     * @param request 创建报表请求对象，包含报表名称、SQL内容等信息
     * @return 创建成功的报表对象，包含自动生成的ID
     * @throws SqlValidationException 当SQL校验失败时抛出（包含非法关键字）
     * @throws IllegalArgumentException 当参数为null或报表名称已存在时抛出
     * @throws BusinessException 当创建过程中发生业务异常时抛出
     */
    @Transactional(rollbackFor = Exception.class)
    public Report createReport(CreateReportRequest request) {
        // ...
    }
    
    /**
     * 根据ID查询报表
     * 
     * @param id 报表ID
     * @return 报表对象
     * @throws ResourceNotFoundException 当报表不存在时抛出
     */
    public Report getReportById(Long id) {
        return reportRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("报表不存在: " + id));
    }
}
```

---

## 🚫 禁止事项

### 绝对禁止

1. ❌ **禁止字符串拼接SQL**
   ```java
   String sql = "SELECT * FROM users WHERE id = " + userId;  // 危险！
   ```

2. ❌ **禁止硬编码**
   ```java
   String dbUrl = "jdbc:mysql://192.168.1.100:3306/report";  // 错误
   
   // ✅ 正确：使用配置文件
   @Value("${spring.datasource.url}")
   private String dbUrl;
   ```

3. ❌ **禁止吞掉异常**
   ```java
   try {
       // ...
   } catch (Exception e) {
       // 什么都不做 - 危险！
   }
   ```

4. ❌ **禁止使用System.out.println**
   ```java
   System.out.println("用户登录成功");  // 错误
   
   // ✅ 正确：使用日志框架
   log.info("用户登录成功, userId: {}", userId);
   ```

5. ❌ **禁止明文存储密码**
   ```java
   user.setPassword(rawPassword);  // 危险！
   
   // ✅ 正确：使用BCrypt加密
   user.setPassword(passwordEncoder.encode(rawPassword));
   ```

---

## ✅ 检查清单

### 代码提交前检查

- [ ] 代码符合命名规范（类/方法/变量）
- [ ] 添加了必要的JavaDoc注释
- [ ] 使用了正确的注解（@Service/@Controller/@Repository）
- [ ] SQL使用参数化查询（NamedParameterJdbcTemplate）
- [ ] 异常处理完整（try-catch + 日志）
- [ ] 日志记录完整（关键操作使用INFO级别）
- [ ] 敏感信息已脱敏（密码/身份证）
- [ ] 通过CheckStyle/SonarLint检查
- [ ] 单元测试覆盖率>80%
- [ ] 无System.out.println/e.printStackTrace()

---

**最后更新**: 2026-01-15
**适用版本**: Java 17 + SpringBoot 3.1.x
