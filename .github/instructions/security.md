# 安全规范

> **适用范围**: GCT Reporter项目安全（P0级别）
> **最后更新**: 2026-01-15

---

## 🔒 SQL注入防护（P0级别 - 强制）

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
    
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.enabled = true")
    List<User> findActiveUsersByRole(@Param("role") String role);
}

// ❌ 错误示例：字符串拼接（SQL注入风险）
public List<Map<String, Object>> executeQueryUnsafe(String sql, Map<String, Object> params) {
    // 危险！不要这样做
    for (Map.Entry<String, Object> entry : params.entrySet()) {
        sql = sql.replace(":" + entry.getKey(), String.valueOf(entry.getValue()));
    }
    return jdbcTemplate.queryForList(sql);
}

// ❌ 错误示例：直接拼接WHERE条件
public List<User> findUsers(String username) {
    // 危险！
    String sql = "SELECT * FROM users WHERE username = '" + username + "'";
    return jdbcTemplate.query(sql, userRowMapper);
}
```

---

## 🛡️ SQL白名单校验（P0级别 - 强制）

### SQL校验器实现

```java
@Component
@Slf4j
public class SqlValidator {
    
    // 危险关键字黑名单
    private static final Set<String> FORBIDDEN_KEYWORDS = Set.of(
        // DDL语句
        "DROP", "CREATE", "ALTER", "TRUNCATE",
        
        // DML语句（除SELECT外）
        "DELETE", "UPDATE", "INSERT", "REPLACE",
        
        // 权限相关
        "GRANT", "REVOKE",
        
        // 存储过程/函数
        "EXEC", "EXECUTE", "CALL",
        
        // 数据库管理
        "USE", "SHOW", "DESCRIBE", "EXPLAIN",
        
        // 脚本执行
        "SCRIPT", "JAVASCRIPT", "ONERROR", "ONLOAD"
    );
    
    // 注释符号正则
    private static final Pattern COMMENT_PATTERN = Pattern.compile("--|/\\*|\\*/");
    
    // 多语句分隔符
    private static final Pattern MULTI_STATEMENT_PATTERN = Pattern.compile(";\\s*\\S");
    
    /**
     * 校验SQL是否安全
     * 
     * @param sql SQL语句
     * @return true表示安全，false表示不安全
     */
    public boolean isValid(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            log.warn("SQL校验失败: SQL为空");
            return false;
        }
        
        String trimmedSql = sql.trim();
        String upperSql = trimmedSql.toUpperCase();
        
        // 规则1: 只允许SELECT语句
        if (!upperSql.startsWith("SELECT")) {
            log.warn("SQL拒绝：不是SELECT语句, SQL前缀: {}", 
                trimmedSql.substring(0, Math.min(50, trimmedSql.length())));
            return false;
        }
        
        // 规则2: 检查危险关键字
        for (String keyword : FORBIDDEN_KEYWORDS) {
            if (upperSql.contains(keyword)) {
                log.warn("SQL拒绝：包含危险关键字 {}", keyword);
                return false;
            }
        }
        
        // 规则3: 检查注释符号（防止注释绕过）
        if (COMMENT_PATTERN.matcher(sql).find()) {
            log.warn("SQL拒绝：包含注释符号");
            return false;
        }
        
        // 规则4: 检查多语句（防止注入多条SQL）
        if (MULTI_STATEMENT_PATTERN.matcher(sql).find()) {
            log.warn("SQL拒绝：包含多条语句");
            return false;
        }
        
        // 规则5: 检查是否有参数占位符
        if (!sql.contains(":")) {
            log.warn("SQL建议：未使用命名参数，可能存在风险");
            // 不强制拒绝，因为可能是无参数的固定查询
        }
        
        // 规则6: 长度限制（防止过长的SQL）
        if (sql.length() > 5000) {
            log.warn("SQL拒绝：SQL长度超过5000字符");
            return false;
        }
        
        return true;
    }
    
    /**
     * 校验SQL并返回详细错误信息
     */
    public SqlValidationResult validateWithDetails(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return new SqlValidationResult(false, "SQL不能为空");
        }
        
        String trimmedSql = sql.trim();
        String upperSql = trimmedSql.toUpperCase();
        
        if (!upperSql.startsWith("SELECT")) {
            return new SqlValidationResult(false, "仅允许SELECT语句");
        }
        
        for (String keyword : FORBIDDEN_KEYWORDS) {
            if (upperSql.contains(keyword)) {
                return new SqlValidationResult(false, 
                    String.format("SQL包含非法关键字: %s", keyword));
            }
        }
        
        if (COMMENT_PATTERN.matcher(sql).find()) {
            return new SqlValidationResult(false, "SQL不能包含注释");
        }
        
        if (MULTI_STATEMENT_PATTERN.matcher(sql).find()) {
            return new SqlValidationResult(false, "SQL不能包含多条语句");
        }
        
        if (sql.length() > 5000) {
            return new SqlValidationResult(false, "SQL长度不能超过5000字符");
        }
        
        return new SqlValidationResult(true, "SQL校验通过");
    }
}

// 校验结果类
@Data
@AllArgsConstructor
public class SqlValidationResult {
    private boolean valid;
    private String message;
}
```

---

## ⏱️ 查询超时控制

### HikariCP配置

```java
@Configuration
public class DataSourceConfig {
    
    @Value("${spring.datasource.url}")
    private String jdbcUrl;
    
    @Value("${spring.datasource.username}")
    private String username;
    
    @Value("${spring.datasource.password}")
    private String password;
    
    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        
        // 数据库连接配置
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        
        // 连接池配置
        config.setConnectionTimeout(30000);     // 连接超时：30秒
        config.setIdleTimeout(600000);          // 空闲超时：10分钟
        config.setMaxLifetime(1800000);         // 连接最大存活：30分钟
        config.setMaximumPoolSize(10);          // 最大连接数
        config.setMinimumIdle(2);               // 最小空闲连接数
        
        // ✅ 查询超时：5秒（重要！）
        config.addDataSourceProperty("socketTimeout", "5000");
        
        // 连接测试
        config.setConnectionTestQuery("SELECT 1");
        
        return new HikariDataSource(config);
    }
}
```

### 查询超时注解

```java
@Service
public class ReportQueryService {
    
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;
    
    /**
     * 执行查询（带超时控制）
     */
    @Transactional(timeout = 5)  // 事务超时5秒
    public List<Map<String, Object>> executeQuery(String sql, Map<String, Object> params) {
        try {
            // 设置查询超时
            jdbcTemplate.getJdbcTemplate().setQueryTimeout(5);
            
            return jdbcTemplate.queryForList(sql, params);
            
        } catch (Exception e) {
            if (e.getCause() instanceof TimeoutException) {
                log.error("查询超时: SQL={}, params={}", sql, params);
                throw new BusinessException("QUERY_TIMEOUT", "查询超时，请优化查询条件");
            }
            throw e;
        }
    }
}
```

---

## 🔐 密码安全

### BCrypt密码加密

```java
// ✅ 配置密码编码器
@Configuration
public class SecurityConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        // 使用BCrypt算法，强度10
        return new BCryptPasswordEncoder(10);
    }
}

// ✅ 使用密码编码器
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * 创建用户（加密密码）
     */
    public User createUser(CreateUserRequest request) {
        // 密码加密
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        
        User user = User.builder()
            .username(request.getUsername())
            .password(encodedPassword)  // ✅ 存储加密后的密码
            .role(request.getRole())
            .enabled(true)
            .build();
        
        log.info("创建用户成功, username: {}", user.getUsername());
        // ❌ 不要记录密码
        
        return userRepository.save(user);
    }
    
    /**
     * 校验密码
     */
    public boolean checkPassword(String username, String rawPassword) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        
        // 使用BCrypt校验密码
        boolean matches = passwordEncoder.matches(rawPassword, user.getPassword());
        
        if (!matches) {
            log.warn("密码校验失败, username: {}", username);
        }
        
        return matches;
    }
    
    /**
     * 修改密码
     */
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        
        // 校验旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("PASSWORD_MISMATCH", "原密码错误");
        }
        
        // 加密新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        log.info("用户修改密码成功, userId: {}", userId);
    }
}

// ❌ 禁止：明文存储密码
user.setPassword(rawPassword);  // 危险！
```

---

## 🍪 Session安全

### Session配置

```yaml
# application.yml
server:
  servlet:
    session:
      timeout: 30m              # Session超时30分钟
      cookie:
        name: GCTSESSIONID      # Cookie名称
        http-only: true         # ✅ 防止XSS攻击（JavaScript无法访问）
        secure: true            # ✅ 仅HTTPS传输（生产环境）
        same-site: strict       # ✅ 防止CSRF攻击
        path: /                 # Cookie路径
        max-age: 1800           # Cookie最大存活时间（秒）
```

### Session管理

```java
@Service
@Slf4j
public class SessionService {
    
    /**
     * 创建Session
     */
    public void createSession(HttpServletRequest request, User user) {
        HttpSession session = request.getSession(true);
        
        // 设置Session属性
        session.setAttribute("userId", user.getId());
        session.setAttribute("username", user.getUsername());
        session.setAttribute("role", user.getRole());
        session.setAttribute("loginTime", LocalDateTime.now());
        
        // 设置Session超时（30分钟）
        session.setMaxInactiveInterval(1800);
        
        log.info("创建Session成功, userId: {}, sessionId: {}", 
            user.getId(), session.getId());
    }
    
    /**
     * 销毁Session
     */
    public void destroySession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Long userId = (Long) session.getAttribute("userId");
            session.invalidate();
            log.info("销毁Session成功, userId: {}", userId);
        }
    }
    
    /**
     * 获取当前用户ID
     */
    public Long getCurrentUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new UnauthorizedException("未登录");
        }
        
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            throw new UnauthorizedException("Session已失效");
        }
        
        return userId;
    }
}
```

---

## 🌐 CORS跨域配置

### 生产环境配置（白名单）

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Value("${app.cors.allowed-origins}")
    private String[] allowedOrigins;
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            // ✅ 白名单模式（生产环境）
            .allowedOrigins(allowedOrigins)  // 从配置文件读取
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
    }
}
```

```yaml
# application-prod.yml
app:
  cors:
    allowed-origins:
      - https://report.example.com
      - https://admin.example.com
```

### 开发环境配置

```yaml
# application-dev.yml
app:
  cors:
    allowed-origins:
      - http://localhost:5173
      - http://localhost:3000
      - http://127.0.0.1:5173
```

---

## 📝 日志脱敏

### 敏感信息脱敏

```java
@Slf4j
public class UserService {
    
    public User createUser(CreateUserRequest request) {
        // ✅ 正确：不记录密码
        log.info("创建用户开始, username: {}", request.getUsername());
        
        // ❌ 错误：记录密码
        // log.info("密码: {}", request.getPassword());
        
        User user = buildUser(request);
        userRepository.save(user);
        
        return user;
    }
    
    public User getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        
        // ✅ 正确：脱敏后记录
        log.debug("查询用户信息, userId: {}, username: {}, role: {}", 
            user.getId(), user.getUsername(), user.getRole());
        
        // ❌ 错误：记录完整身份证号
        // log.debug("身份证: {}", user.getIdCard());
        
        // ✅ 正确：脱敏身份证号
        if (user.getIdCard() != null) {
            String maskedIdCard = maskIdCard(user.getIdCard());
            log.debug("身份证: {}", maskedIdCard);
        }
        
        return user;
    }
    
    /**
     * 身份证号脱敏
     * 示例：110101199001011234 -> 110101****1234
     */
    private String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 14) {
            return "****";
        }
        return idCard.substring(0, 6) + "****" + idCard.substring(idCard.length() - 4);
    }
    
    /**
     * 手机号脱敏
     * 示例：13812345678 -> 138****5678
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 11) {
            return "****";
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
}
```

---

## 🔒 配置文件加密

### Jasypt加密配置

```xml
<!-- pom.xml -->
<dependency>
    <groupId>com.github.ulisesbocchio</groupId>
    <artifactId>jasypt-spring-boot-starter</artifactId>
    <version>3.0.5</version>
</dependency>
```

```yaml
# application-prod.yml
spring:
  datasource:
    url: jdbc:oracle:thin:@//db.example.com:1521/orcl
    username: report_user
    password: ENC(加密后的密码)  # ✅ 使用jasypt加密

# Jasypt配置
jasypt:
  encryptor:
    password: ${JASYPT_PASSWORD}  # 从环境变量读取密钥
    algorithm: PBEWithMD5AndDES
```

### 生成加密密码

```bash
# 使用jasypt-maven-plugin加密
mvn jasypt:encrypt-value \
  -Djasypt.encryptor.password="your-secret-key" \
  -Djasypt.plugin.value="your-database-password"

# 输出: ENC(encrypted-value)
```

---

## 🔑 API密钥管理

### 环境变量方式

```java
@Configuration
public class ApiConfig {
    
    // ✅ 正确：从环境变量读取
    @Value("${api.third-party.key:#{null}}")
    private String apiKey;
    
    @PostConstruct
    public void init() {
        if (apiKey == null) {
            log.warn("第三方API密钥未配置");
        }
    }
}
```

```bash
# 设置环境变量
export API_THIRD_PARTY_KEY="your-api-key"
```

```yaml
# application.yml
api:
  third-party:
    key: ${API_THIRD_PARTY_KEY}
```

---

## 🚫 禁止事项

### 绝对禁止

1. ❌ **禁止提交敏感信息到Git**
   ```bash
   # .gitignore
   application-prod.yml
   *.key
   *.pem
   .env
   ```

2. ❌ **禁止字符串拼接SQL**
   ```java
   // 危险！
   String sql = "SELECT * FROM users WHERE id = " + userId;
   ```

3. ❌ **禁止明文存储密码**
   ```java
   user.setPassword(rawPassword);  // 危险！
   ```

4. ❌ **禁止记录敏感信息到日志**
   ```java
   log.info("密码: {}", password);        // 危险！
   log.info("身份证: {}", idCard);         // 危险！
   log.info("API密钥: {}", apiKey);       // 危险！
   ```

5. ❌ **禁止使用弱加密算法**
   ```java
   // 错误：MD5/SHA1已被破解
   MessageDigest.getInstance("MD5")
   
   // 正确：使用BCrypt
   new BCryptPasswordEncoder()
   ```

---

## ✅ 安全检查清单

### 代码提交前检查

- [ ] SQL使用参数化查询（NamedParameterJdbcTemplate）
- [ ] SQL通过SqlValidator校验
- [ ] 查询有超时控制（5秒）
- [ ] 密码使用BCrypt加密存储
- [ ] 敏感信息已脱敏（日志）
- [ ] Session配置安全（http-only/secure/same-site）
- [ ] CORS使用白名单（生产环境）
- [ ] 无硬编码密码/密钥
- [ ] 配置文件已加密（生产环境）
- [ ] 无敏感信息提交到Git

### SQL安全测试清单（强制）

- [ ] DROP TABLE注入测试
- [ ] DELETE语句注入测试
- [ ] UPDATE语句注入测试
- [ ] 注释绕过测试
- [ ] 多语句注入测试
- [ ] 参数化查询防注入测试
- [ ] 查询超时测试

---

**最后更新**: 2026-01-15
**安全级别**: P0（生产环境强制执行）
