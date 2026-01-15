# 性能规范

> **适用范围**: GCT Reporter项目性能优化
> **最后更新**: 2026-01-15

---

## 📊 性能指标要求

### 响应时间目标（SLA）

| 指标 | 要求 | 测试方法 | 备注 |
|------|------|---------|------|
| **1000行数据查询** | P95 < 3秒 | JMeter压力测试，50并发 | 核心性能指标 |
| **Excel导出（1000行）** | < 5秒 | 功能测试 | 包含数据查询+导出 |
| **报表列表加载** | < 1秒 | Lighthouse性能测试 | 前端首屏加载 |
| **登录响应时间** | P95 < 2秒 | 50并发用户测试 | 包含密码校验 |
| **5用户并发查询** | 无阻塞 | 并发测试 | 同时执行不同报表 |

### 吞吐量目标

| 指标 | 要求 | 备注 |
|------|------|------|
| **并发用户数** | 支持50用户并发 | MVP阶段 |
| **查询TPS** | ≥10 TPS | 每秒事务数 |
| **数据导出** | 5000行/次 | 单次最大导出量 |

---

## 🗄️ 数据库优化

### 索引设计

```sql
-- ✅ 强制：关键字段添加索引

-- 用户表索引
CREATE UNIQUE INDEX idx_username ON users(username);
CREATE INDEX idx_role_enabled ON users(role, enabled);

-- 报表表索引
CREATE INDEX idx_creator_id ON reports(creator_id);
CREATE INDEX idx_created_at ON reports(created_at DESC);
CREATE INDEX idx_name ON reports(name);

-- 报表参数表索引
CREATE INDEX idx_report_id ON report_params(report_id);

-- 报表列配置表索引
CREATE INDEX idx_report_id_col ON report_columns(report_id);

-- 报表权限表索引
CREATE INDEX idx_report_role ON report_permissions(report_id, role);

-- 执行日志表索引
CREATE INDEX idx_user_id_log ON execution_logs(user_id);
CREATE INDEX idx_report_id_log ON execution_logs(report_id);
CREATE INDEX idx_execute_time ON execution_logs(execute_time DESC);

-- ✅ 建议：复合索引（按查询频率）
CREATE INDEX idx_report_creator_time ON reports(creator_id, created_at DESC);
CREATE INDEX idx_log_user_time ON execution_logs(user_id, execute_time DESC);
```

### 查询优化

```sql
-- ❌ 避免：全表扫描
SELECT * FROM execution_logs 
WHERE DATE(execute_time) = '2026-01-15';

-- ✅ 优化：使用范围查询
SELECT * FROM execution_logs 
WHERE execute_time >= '2026-01-15 00:00:00' 
  AND execute_time < '2026-01-16 00:00:00';

-- ❌ 避免：使用函数导致索引失效
SELECT * FROM users WHERE UPPER(username) = 'ADMIN';

-- ✅ 优化：直接使用字段
SELECT * FROM users WHERE username = 'admin';

-- ❌ 避免：SELECT *
SELECT * FROM reports;

-- ✅ 优化：只查询需要的字段
SELECT id, name, description, creator_id FROM reports;

-- ❌ 避免：N+1查询问题
// 先查询报表列表
List<Report> reports = reportRepository.findAll();
// 再循环查询每个报表的参数（N次查询）
for (Report report : reports) {
    List<Param> params = paramRepository.findByReportId(report.getId());
    report.setParams(params);
}

-- ✅ 优化：使用JOIN一次查询
SELECT r.*, p.* 
FROM reports r 
LEFT JOIN report_params p ON r.id = p.report_id
WHERE r.creator_id = :creatorId;
```

### 分页查询

```java
// ✅ 正确：使用JPA分页
@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {
    
    @GetMapping
    public ResponseEntity<Page<ReportDTO>> getReports(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) String keyword) {
        
        // 创建分页对象
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        // 分页查询
        Page<Report> reportPage;
        if (keyword != null && !keyword.isEmpty()) {
            reportPage = reportRepository.findByNameContaining(keyword, pageable);
        } else {
            reportPage = reportRepository.findAll(pageable);
        }
        
        // 转换为DTO
        Page<ReportDTO> dtoPage = reportPage.map(ReportDTO::fromEntity);
        
        return ResponseEntity.ok(dtoPage);
    }
}
```

---

## 🔄 缓存优化

### Spring Cache配置

```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        
        cacheManager.setCaches(Arrays.asList(
            // 用户缓存（30分钟）
            new ConcurrentMapCache("users"),
            
            // 报表列表缓存（5分钟）
            new ConcurrentMapCache("reports"),
            
            // 报表详情缓存（10分钟）
            new ConcurrentMapCache("report-detail")
        ));
        
        return cacheManager;
    }
}
```

### 缓存使用

```java
@Service
@RequiredArgsConstructor
public class ReportService {
    
    private final ReportRepository reportRepository;
    
    /**
     * 查询报表详情（带缓存）
     */
    @Cacheable(value = "report-detail", key = "#id")
    public Report getReportById(Long id) {
        return reportRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("报表不存在"));
    }
    
    /**
     * 更新报表（清除缓存）
     */
    @CacheEvict(value = "report-detail", key = "#id")
    @Transactional
    public Report updateReport(Long id, UpdateReportRequest request) {
        Report report = getReportById(id);
        // 更新逻辑...
        return reportRepository.save(report);
    }
    
    /**
     * 删除报表（清除缓存）
     */
    @CacheEvict(value = {"report-detail", "reports"}, allEntries = true)
    @Transactional
    public void deleteReport(Long id) {
        reportRepository.deleteById(id);
    }
}
```

---

## 🚀 前端性能优化

### 路由懒加载

```typescript
// ✅ router/index.ts - 路由懒加载
import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')  // 懒加载
  },
  {
    path: '/reports',
    name: 'ReportList',
    component: () => import('@/views/ReportList.vue')
  },
  {
    path: '/reports/:id',
    name: 'ReportDetail',
    component: () => import('@/views/ReportDetail.vue')
  },
  {
    path: '/admin/users',
    name: 'UserManagement',
    component: () => import('@/views/admin/UserManagement.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
```

### 组件懒加载

```vue
<script setup lang="ts">
import { defineAsyncComponent } from 'vue'

// ✅ 懒加载重型组件
const SqlEditor = defineAsyncComponent(() => 
  import('@/components/SqlEditor.vue')
)

const DataTable = defineAsyncComponent(() => 
  import('@/components/DataTable.vue')
)
</script>

<template>
  <div>
    <Suspense>
      <template #default>
        <SqlEditor v-if="showEditor" />
      </template>
      <template #fallback>
        <el-skeleton :rows="5" animated />
      </template>
    </Suspense>
  </div>
</template>
```

### 防抖和节流

```vue
<script setup lang="ts">
import { ref } from 'vue'
import { debounce, throttle } from 'lodash-es'

// ✅ 防抖搜索（300ms）
const keyword = ref('')

const handleSearch = debounce((value: string) => {
  console.log('搜索:', value)
  // 调用API搜索
  searchReports(value)
}, 300)

// ✅ 节流滚动（100ms）
const handleScroll = throttle(() => {
  console.log('滚动事件')
  // 处理滚动逻辑
}, 100)

// ✅ 节流窗口resize（200ms）
const handleResize = throttle(() => {
  console.log('窗口大小变化')
  // 更新布局
}, 200)
</script>

<template>
  <div>
    <el-input 
      v-model="keyword" 
      placeholder="搜索报表"
      @input="handleSearch(keyword)"
    />
  </div>
</template>
```

### 虚拟滚动

```vue
<template>
  <!-- ✅ 大数据量表格使用虚拟滚动 -->
  <el-table-v2
    :columns="columns"
    :data="largeDataList"
    :width="800"
    :height="600"
    :row-height="50"
    :estimated-row-height="50"
  />
  
  <!-- ✅ 大数据量列表使用虚拟滚动 -->
  <el-virtual-list
    :data="reportList"
    :height="600"
    :item-height="80"
  >
    <template #default="{ item }">
      <report-item :report="item" />
    </template>
  </el-virtual-list>
</template>
```

### 图片懒加载

```vue
<template>
  <!-- ✅ 使用v-lazy指令 -->
  <img v-lazy="report.thumbnail" alt="报表缩略图" />
  
  <!-- ✅ 使用Intersection Observer -->
  <img 
    :src="imageSrc" 
    :data-src="report.thumbnail"
    class="lazy-image"
  />
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

const imageSrc = ref('placeholder.png')

onMounted(() => {
  // Intersection Observer实现懒加载
  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        const img = entry.target as HTMLImageElement
        img.src = img.dataset.src || ''
        observer.unobserve(img)
      }
    })
  })
  
  document.querySelectorAll('.lazy-image').forEach(img => {
    observer.observe(img)
  })
})
</script>
```

### 代码分割

```typescript
// ✅ vite.config.ts - 代码分割配置
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          // 第三方库单独打包
          'element-plus': ['element-plus'],
          'vue-vendor': ['vue', 'vue-router', 'pinia'],
          'chart': ['echarts'],
          'codemirror': ['codemirror', '@codemirror/lang-sql']
        }
      }
    },
    chunkSizeWarningLimit: 1000  // 警告阈值1MB
  }
})
```

---

## 🔍 性能监控

### 后端性能监控

```java
// ✅ 使用AOP记录方法执行时间
@Aspect
@Component
@Slf4j
public class PerformanceAspect {
    
    @Around("@annotation(com.gct.report.annotation.PerformanceMonitor)")
    public Object monitorPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            // 记录执行时间
            log.info("方法: {}, 执行时间: {}ms", methodName, duration);
            
            // 如果超过3秒，记录警告
            if (duration > 3000) {
                log.warn("方法: {} 执行时间过长: {}ms", methodName, duration);
            }
            
            return result;
        } catch (Throwable e) {
            long endTime = System.currentTimeMillis();
            log.error("方法: {} 执行失败, 耗时: {}ms", methodName, endTime - startTime);
            throw e;
        }
    }
}

// 使用示例
@Service
public class ReportQueryService {
    
    @PerformanceMonitor  // 监控性能
    public List<Map<String, Object>> executeQuery(String sql, Map<String, Object> params) {
        // 查询逻辑...
    }
}
```

### 前端性能监控

```typescript
// ✅ src/utils/performance.ts
export class PerformanceMonitor {
  
  /**
   * 监控API请求性能
   */
  static monitorApiRequest(url: string, startTime: number, endTime: number) {
    const duration = endTime - startTime
    
    console.log(`API请求: ${url}, 耗时: ${duration}ms`)
    
    // 如果超过3秒，记录警告
    if (duration > 3000) {
      console.warn(`API请求过慢: ${url}, 耗时: ${duration}ms`)
    }
    
    // 发送到监控系统（可选）
    // this.sendToMonitoring('api-request', { url, duration })
  }
  
  /**
   * 监控页面加载性能
   */
  static monitorPageLoad() {
    if ('performance' in window) {
      const perfData = window.performance.timing
      const pageLoadTime = perfData.loadEventEnd - perfData.navigationStart
      const domReadyTime = perfData.domContentLoadedEventEnd - perfData.navigationStart
      
      console.log(`页面加载时间: ${pageLoadTime}ms`)
      console.log(`DOM Ready时间: ${domReadyTime}ms`)
      
      // 发送到监控系统
      // this.sendToMonitoring('page-load', { pageLoadTime, domReadyTime })
    }
  }
}

// 在axios拦截器中使用
request.interceptors.request.use(config => {
  config.metadata = { startTime: Date.now() }
  return config
})

request.interceptors.response.use(response => {
  const endTime = Date.now()
  const startTime = response.config.metadata?.startTime || endTime
  
  PerformanceMonitor.monitorApiRequest(
    response.config.url || '',
    startTime,
    endTime
  )
  
  return response
})
```

---

## 📏 资源压缩

### Gzip压缩

```yaml
# application.yml
server:
  compression:
    enabled: true
    mime-types:
      - application/json
      - application/xml
      - text/html
      - text/xml
      - text/plain
      - text/css
      - text/javascript
      - application/javascript
    min-response-size: 1024  # 最小压缩大小（字节）
```

### 前端资源压缩

```typescript
// vite.config.ts
import { defineConfig } from 'vite'
import viteCompression from 'vite-plugin-compression'

export default defineConfig({
  plugins: [
    viteCompression({
      algorithm: 'gzip',
      ext: '.gz',
      threshold: 10240,  // 大于10KB的文件才压缩
      deleteOriginFile: false
    })
  ],
  build: {
    minify: 'terser',  // 代码压缩
    terserOptions: {
      compress: {
        drop_console: true,  // 删除console
        drop_debugger: true  // 删除debugger
      }
    }
  }
})
```

---

## ✅ 性能检查清单

### 后端性能检查

- [ ] 关键字段已添加索引
- [ ] 避免SELECT *，只查询需要的字段
- [ ] 使用分页查询（避免一次查询大量数据）
- [ ] 避免N+1查询问题（使用JOIN或批量查询）
- [ ] 查询有超时控制（5秒）
- [ ] 使用连接池（HikariCP）
- [ ] 启用Gzip压缩
- [ ] 关键方法有性能监控

### 前端性能检查

- [ ] 路由懒加载
- [ ] 组件懒加载
- [ ] 图片懒加载
- [ ] 搜索使用防抖（300ms）
- [ ] 滚动使用节流（100ms）
- [ ] 大数据量表格使用虚拟滚动
- [ ] 代码分割（第三方库单独打包）
- [ ] 启用Gzip压缩
- [ ] 生产环境删除console.log

### 数据库性能检查

- [ ] users.username有唯一索引
- [ ] reports.creator_id有索引
- [ ] execution_logs.execute_time有索引
- [ ] 避免使用函数导致索引失效
- [ ] 使用EXPLAIN分析慢查询
- [ ] 定期清理执行日志（保留最近6个月）

---

**最后更新**: 2026-01-15
**性能目标**: P95 < 3秒（核心查询）
