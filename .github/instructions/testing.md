# 测试规范

> **适用范围**: GCT Reporter项目测试（后端+前端）
> **最后更新**: 2026-01-15

---

## 🎯 测试覆盖率目标

### 强制要求

```yaml
后端测试（Java）:
  单元测试覆盖率: >80%（强制）
  集成测试覆盖率: >60%
  关键业务逻辑: 100%覆盖（如SQL校验、权限控制）
  SQL安全测试: 100%场景覆盖

前端测试（Vue）:
  组件单元测试: >60%
  E2E测试: 核心流程100%覆盖
    - 用户登录
    - 创建报表
    - 查询报表
    - Excel导出
```

---

## 🧪 后端测试规范（Java）

### JUnit 5 + Mockito单元测试

```java
// ✅ src/test/java/com/gct/report/service/ReportServiceTest.java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
            .description("这是一个测试报表")
            .sqlContent("SELECT * FROM users WHERE id = :userId")
            .build();
        
        Report savedReport = Report.builder()
            .id(1L)
            .name("测试报表")
            .description("这是一个测试报表")
            .sqlContent("SELECT * FROM users WHERE id = :userId")
            .creatorId(100L)
            .build();
        
        // Mock行为
        when(sqlValidator.isValid(anyString())).thenReturn(true);
        when(reportRepository.save(any(Report.class))).thenReturn(savedReport);
        
        // When（执行测试）
        Report result = reportService.createReport(request);
        
        // Then（验证结果）
        assertNotNull(result);
        assertEquals("测试报表", result.getName());
        assertEquals(1L, result.getId());
        assertEquals("SELECT * FROM users WHERE id = :userId", result.getSqlContent());
        
        // 验证方法调用
        verify(sqlValidator).isValid("SELECT * FROM users WHERE id = :userId");
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
        
        // 验证不应该调用save
        verify(sqlValidator).isValid("DROP TABLE users");
        verify(reportRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("创建报表 - 报表名称已存在时抛出异常")
    void createReport_DuplicateName_ThrowsException() {
        // Given
        CreateReportRequest request = CreateReportRequest.builder()
            .name("已存在的报表")
            .sqlContent("SELECT * FROM users")
            .build();
        
        when(sqlValidator.isValid(anyString())).thenReturn(true);
        when(reportRepository.existsByName("已存在的报表")).thenReturn(true);
        
        // When & Then
        assertThrows(BusinessException.class, () -> {
            reportService.createReport(request);
        });
    }
    
    @Test
    @DisplayName("根据ID查询报表 - 报表不存在时抛出异常")
    void getReportById_NotFound_ThrowsException() {
        // Given
        Long reportId = 999L;
        when(reportRepository.findById(reportId)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            reportService.getReportById(reportId);
        });
    }
}
```

### Spring Boot集成测试

```java
// ✅ src/test/java/com/gct/report/controller/ReportControllerIntegrationTest.java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

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
            .description("这是一个集成测试")
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
    @DisplayName("创建报表 - SQL无效返回400")
    void createReport_InvalidSql_Returns400() throws Exception {
        // Given
        CreateReportRequest request = CreateReportRequest.builder()
            .name("恶意报表")
            .sqlContent("DROP TABLE users")
            .build();
        
        String requestJson = objectMapper.writeValueAsString(request);
        
        // When & Then
        mockMvc.perform(post("/api/v1/reports")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("SQL_INVALID"))
            .andDo(print());
    }
    
    @Test
    @DisplayName("查询报表 - 未授权返回403")
    void getReport_Unauthorized_Returns403() throws Exception {
        mockMvc.perform(get("/api/v1/reports/1"))
            .andExpect(status().isForbidden());
    }
    
    @Test
    @DisplayName("查询报表列表 - 分页查询")
    void getReports_Pagination() throws Exception {
        mockMvc.perform(get("/api/v1/reports")
                .param("page", "1")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.totalElements").exists())
            .andDo(print());
    }
}
```

### SQL安全测试（重要！）

```java
// ✅ src/test/java/com/gct/report/security/SqlValidatorTest.java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class SqlValidatorTest {
    
    private final SqlValidator sqlValidator = new SqlValidator();
    
    @Test
    @DisplayName("SQL注入防护 - DROP TABLE")
    void sqlInjection_DropTable_Blocked() {
        String maliciousSql = "SELECT * FROM users; DROP TABLE users; --";
        assertFalse(sqlValidator.isValid(maliciousSql));
    }
    
    @Test
    @DisplayName("SQL注入防护 - DELETE语句")
    void sqlInjection_Delete_Blocked() {
        String maliciousSql = "SELECT * FROM users WHERE id = 1; DELETE FROM users WHERE 1=1";
        assertFalse(sqlValidator.isValid(maliciousSql));
    }
    
    @Test
    @DisplayName("SQL注入防护 - UPDATE语句")
    void sqlInjection_Update_Blocked() {
        String maliciousSql = "SELECT * FROM users; UPDATE users SET role='ADMIN'";
        assertFalse(sqlValidator.isValid(maliciousSql));
    }
    
    @Test
    @DisplayName("SQL注入防护 - 注释绕过")
    void sqlInjection_Comment_Blocked() {
        String maliciousSql = "SELECT * FROM users WHERE 1=1 -- AND role='ADMIN'";
        assertFalse(sqlValidator.isValid(maliciousSql));
    }
    
    @Test
    @DisplayName("SQL注入防护 - 多行注释")
    void sqlInjection_MultilineComment_Blocked() {
        String maliciousSql = "SELECT * FROM users /* WHERE id = 1 */ WHERE 1=1";
        assertFalse(sqlValidator.isValid(maliciousSql));
    }
    
    @ParameterizedTest
    @ValueSource(strings = {
        "DROP TABLE users",
        "DELETE FROM users",
        "UPDATE users SET role='ADMIN'",
        "INSERT INTO users VALUES (1, 'hacker')",
        "TRUNCATE TABLE users",
        "ALTER TABLE users ADD COLUMN password VARCHAR(100)",
        "CREATE TABLE hackers (id INT)",
        "EXEC sp_executesql",
        "EXECUTE('DROP TABLE users')"
    })
    @DisplayName("SQL注入防护 - 危险关键字批量测试")
    void sqlInjection_DangerousKeywords_Blocked(String maliciousSql) {
        assertFalse(sqlValidator.isValid(maliciousSql));
    }
    
    @Test
    @DisplayName("合法SQL - SELECT语句")
    void validSql_Select_Allowed() {
        String validSql = "SELECT * FROM users WHERE id = :userId";
        assertTrue(sqlValidator.isValid(validSql));
    }
    
    @Test
    @DisplayName("合法SQL - JOIN查询")
    void validSql_Join_Allowed() {
        String validSql = "SELECT u.*, r.* FROM users u JOIN reports r ON u.id = r.creator_id";
        assertTrue(sqlValidator.isValid(validSql));
    }
    
    @Test
    @DisplayName("参数化查询 - 防止注入")
    void parameterizedQuery_PreventInjection() {
        // 模拟参数化查询
        String sql = "SELECT * FROM users WHERE id = :userId";
        Map<String, Object> params = Map.of("userId", "1 OR 1=1");
        
        // 验证参数化查询不会被注入
        // 实际执行应该只返回id=1的用户（如果存在），而不是所有用户
        List<Map<String, Object>> result = queryService.executeQuery(sql, params);
        assertTrue(result.isEmpty() || result.size() == 1);
    }
}
```

### 测试配置

```yaml
# src/test/resources/application-test.yml
spring:
  datasource:
    # 使用H2内存数据库进行测试
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  
  jpa:
    hibernate:
      ddl-auto: create-drop  # 测试结束后自动删除表
    show-sql: true           # 显示SQL语句
  
  # 禁用缓存
  cache:
    type: none

# 日志配置
logging:
  level:
    com.gct.report: DEBUG
```

---

## 🎨 前端测试规范（Vue）

### Vitest单元测试

```typescript
// ✅ src/components/__tests__/ReportList.test.ts
import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { ElMessage } from 'element-plus'
import ReportList from '@/components/ReportList.vue'
import { reportApi } from '@/api/report'

// Mock API
vi.mock('@/api/report', () => ({
  reportApi: {
    getReports: vi.fn()
  }
}))

// Mock Element Plus
vi.mock('element-plus', () => ({
  ElMessage: {
    success: vi.fn(),
    error: vi.fn()
  }
}))

describe('ReportList.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })
  
  it('应该渲染报表列表', async () => {
    // Given
    const mockReports = [
      { id: 1, name: '报表1', description: '描述1' },
      { id: 2, name: '报表2', description: '描述2' }
    ]
    
    vi.mocked(reportApi.getReports).mockResolvedValue({
      data: mockReports
    })
    
    // When
    const wrapper = mount(ReportList)
    await wrapper.vm.$nextTick()
    
    // Then
    expect(wrapper.findAll('.report-item')).toHaveLength(2)
    expect(wrapper.text()).toContain('报表1')
    expect(wrapper.text()).toContain('报表2')
  })
  
  it('应该显示加载状态', async () => {
    // Given
    vi.mocked(reportApi.getReports).mockImplementation(() => 
      new Promise(resolve => setTimeout(resolve, 1000))
    )
    
    // When
    const wrapper = mount(ReportList)
    
    // Then
    expect(wrapper.find('.el-skeleton').exists()).toBe(true)
  })
  
  it('加载失败时应该显示错误信息', async () => {
    // Given
    vi.mocked(reportApi.getReports).mockRejectedValue(
      new Error('网络错误')
    )
    
    // When
    const wrapper = mount(ReportList)
    await wrapper.vm.$nextTick()
    
    // Then
    expect(ElMessage.error).toHaveBeenCalledWith('加载报表列表失败')
  })
  
  it('点击删除按钮应该触发delete事件', async () => {
    // Given
    const mockReports = [
      { id: 1, name: '报表1', description: '描述1' }
    ]
    
    vi.mocked(reportApi.getReports).mockResolvedValue({
      data: mockReports
    })
    
    const wrapper = mount(ReportList)
    await wrapper.vm.$nextTick()
    
    // When
    await wrapper.find('.delete-btn').trigger('click')
    
    // Then
    expect(wrapper.emitted()).toHaveProperty('delete')
    expect(wrapper.emitted('delete')?.[0]).toEqual([1])
  })
})
```

### E2E测试（Playwright）

```typescript
// ✅ tests/e2e/report-workflow.spec.ts
import { test, expect } from '@playwright/test'

test.describe('报表工作流', () => {
  test.beforeEach(async ({ page }) => {
    // 登录
    await page.goto('/login')
    await page.fill('input[name="username"]', 'admin')
    await page.fill('input[name="password"]', 'admin123')
    await page.click('button[type="submit"]')
    await page.waitForURL('/reports')
  })
  
  test('应该能够创建报表', async ({ page }) => {
    // 1. 进入创建报表页面
    await page.click('text=创建报表')
    await expect(page).toHaveURL('/reports/create')
    
    // 2. 填写报表信息
    await page.fill('input[name="name"]', 'E2E测试报表')
    await page.fill('textarea[name="description"]', '这是一个E2E测试报表')
    
    // 3. 填写SQL内容
    await page.fill('.sql-editor', 'SELECT * FROM users WHERE id = :userId')
    
    // 4. 添加参数
    await page.click('text=添加参数')
    await page.fill('input[name="paramName"]', 'userId')
    await page.selectOption('select[name="paramType"]', 'NUMBER')
    
    // 5. 提交
    await page.click('button:has-text("保存")')
    
    // 6. 验证
    await expect(page).toHaveURL(/\/reports\/\d+/)
    await expect(page.locator('.el-message--success')).toHaveText('创建成功')
  })
  
  test('应该能够查询报表', async ({ page }) => {
    // 1. 选择一个报表
    await page.click('.report-item:first-child')
    
    // 2. 输入参数
    await page.fill('input[name="userId"]', '1')
    
    // 3. 执行查询
    await page.click('button:has-text("查询")')
    
    // 4. 验证结果
    await expect(page.locator('.result-table')).toBeVisible()
    await expect(page.locator('.result-row')).toHaveCount(1)
  })
  
  test('应该能够导出Excel', async ({ page }) => {
    // 1. 执行查询
    await page.click('.report-item:first-child')
    await page.fill('input[name="userId"]', '1')
    await page.click('button:has-text("查询")')
    await page.waitForSelector('.result-table')
    
    // 2. 导出Excel
    const downloadPromise = page.waitForEvent('download')
    await page.click('button:has-text("导出Excel")')
    const download = await downloadPromise
    
    // 3. 验证文件名
    expect(download.suggestedFilename()).toMatch(/.*\.xlsx$/)
  })
  
  test('SQL注入应该被阻止', async ({ page }) => {
    // 1. 尝试创建包含恶意SQL的报表
    await page.click('text=创建报表')
    await page.fill('input[name="name"]', '恶意报表')
    await page.fill('.sql-editor', 'DROP TABLE users')
    
    // 2. 提交
    await page.click('button:has-text("保存")')
    
    // 3. 验证错误提示
    await expect(page.locator('.el-message--error'))
      .toHaveText(/SQL包含非法关键字/)
  })
})
```

---

## 📊 测试覆盖率报告

### JaCoCo配置（Java）

```xml
<!-- pom.xml -->
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.10</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
        <execution>
            <id>jacoco-check</id>
            <goals>
                <goal>check</goal>
            </goals>
            <configuration>
                <rules>
                    <rule>
                        <element>PACKAGE</element>
                        <limits>
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.80</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### Vitest覆盖率配置（Vue）

```typescript
// vite.config.ts
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  test: {
    globals: true,
    environment: 'jsdom',
    coverage: {
      provider: 'v8',
      reporter: ['text', 'json', 'html'],
      exclude: [
        'node_modules/',
        'dist/',
        '**/*.spec.ts',
        '**/*.test.ts',
        '**/types/'
      ],
      statements: 60,
      branches: 60,
      functions: 60,
      lines: 60
    }
  }
})
```

---

## 🔄 持续测试

### 本地测试命令

```bash
# 后端测试
mvn test                    # 运行所有测试
mvn test -Dtest=ReportServiceTest  # 运行特定测试类
mvn verify                  # 运行测试并生成覆盖率报告

# 前端测试
npm run test                # 运行单元测试
npm run test:unit           # 运行单元测试
npm run test:e2e            # 运行E2E测试
npm run test:coverage       # 生成覆盖率报告
```

### CI/CD集成（GitHub Actions）

```yaml
# .github/workflows/test.yml
name: Test

on: [push, pull_request]

jobs:
  backend-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Run tests
        run: mvn verify
      
      - name: Upload coverage
        uses: codecov/codecov-action@v3
        with:
          file: ./target/site/jacoco/jacoco.xml
  
  frontend-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18'
      
      - name: Install dependencies
        run: npm ci
      
      - name: Run tests
        run: npm run test:coverage
      
      - name: Upload coverage
        uses: codecov/codecov-action@v3
        with:
          file: ./coverage/coverage-final.json
```

---

## ✅ 测试清单

### 单元测试清单

- [ ] 成功场景测试（Happy Path）
- [ ] 异常场景测试（Exception Path）
- [ ] 边界条件测试（Edge Cases）
- [ ] 空值/null测试
- [ ] 参数校验测试
- [ ] 业务逻辑测试
- [ ] Mock外部依赖

### SQL安全测试清单（强制）

- [ ] DROP TABLE注入测试
- [ ] DELETE语句注入测试
- [ ] UPDATE语句注入测试
- [ ] INSERT语句注入测试
- [ ] 注释绕过测试（--、/* */）
- [ ] UNION注入测试
- [ ] EXEC/EXECUTE注入测试
- [ ] 参数化查询防注入测试

### 集成测试清单

- [ ] API接口测试（200/400/403/404/500）
- [ ] 数据库事务测试
- [ ] 权限控制测试
- [ ] 分页查询测试
- [ ] 文件上传/下载测试

### E2E测试清单

- [ ] 用户登录流程
- [ ] 创建报表流程
- [ ] 查询报表流程
- [ ] Excel导出流程
- [ ] 权限切换测试
- [ ] 错误处理测试

---

**最后更新**: 2026-01-15
**测试框架**: JUnit 5 + Mockito（Java）、Vitest + Playwright（Vue）
