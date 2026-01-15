# Vue前端开发规范

> **适用范围**: GCT Reporter前端开发（Vue 3.3.x + TypeScript）
> **最后更新**: 2026-01-15

---

## 📋 技术栈

```yaml
语言: TypeScript
框架: Vue 3.3.x (组合式API)
构建工具: Vite 4.x
UI组件库: Element Plus 2.3.x
代码编辑器: vue-codemirror + CodeMirror 5.65.x
HTTP客户端: Axios 1.x
状态管理: Pinia 2.x
路由: Vue Router 4.x
代码检查: ESLint + Prettier
```

---

## 💻 代码规范

### Vue 3组合式API规范

```vue
<!-- ✅ 正确示例：完整的组合式API组件 -->
<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { Report } from '@/types/report'
import { reportApi } from '@/api/report'

// ==================== Props定义 ====================
interface Props {
  reportId: number
  readonly?: boolean
  showActions?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  readonly: false,
  showActions: true
})

// ==================== Emits定义 ====================
const emit = defineEmits<{
  (e: 'update', report: Report): void
  (e: 'delete', id: number): void
  (e: 'close'): void
}>()

// ==================== 响应式状态 ====================
const report = ref<Report | null>(null)
const loading = ref(false)
const error = ref<string | null>(null)

// ==================== 计算属性 ====================
const isEditable = computed(() => 
  !props.readonly && report.value?.status === 'DRAFT'
)

const displayName = computed(() => 
  report.value?.name || '未命名报表'
)

// ==================== 方法 ====================
const loadReport = async () => {
  loading.value = true
  error.value = null
  
  try {
    const response = await reportApi.getReport(props.reportId)
    report.value = response.data
  } catch (err) {
    console.error('加载报表失败:', err)
    error.value = '加载报表失败，请重试'
    ElMessage.error('加载报表失败')
  } finally {
    loading.value = false
  }
}

const handleUpdate = () => {
  if (report.value) {
    emit('update', report.value)
  }
}

const handleDelete = () => {
  emit('delete', props.reportId)
}

// ==================== 监听器 ====================
watch(() => props.reportId, (newId) => {
  if (newId) {
    loadReport()
  }
})

// ==================== 生命周期 ====================
onMounted(() => {
  loadReport()
})
</script>

<template>
  <div class="report-detail">
    <!-- 加载状态 -->
    <el-skeleton v-if="loading" :rows="5" animated />
    
    <!-- 错误状态 -->
    <el-alert
      v-else-if="error"
      type="error"
      :title="error"
      show-icon
      @close="error = null"
    />
    
    <!-- 正常内容 -->
    <div v-else-if="report" class="report-content">
      <h2>{{ displayName }}</h2>
      
      <!-- 操作按钮 -->
      <div v-if="showActions && isEditable" class="actions">
        <el-button type="primary" @click="handleUpdate">更新</el-button>
        <el-button type="danger" @click="handleDelete">删除</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.report-detail {
  padding: 20px;
}

.report-content {
  background: #fff;
  border-radius: 4px;
  padding: 24px;
}

.actions {
  margin-top: 20px;
  display: flex;
  gap: 12px;
}
</style>
```

---

## 🏷️ 命名规范

### 文件命名

```bash
# 组件文件：PascalCase
ReportList.vue
ReportDetail.vue
UserManagement.vue
SqlEditor.vue

# TypeScript文件：kebab-case
report-service.ts
user-api.ts
format-utils.ts
date-helper.ts

# 样式文件：kebab-case
report-list.css
common-styles.scss

# 类型定义文件：kebab-case
report-types.ts
user-types.ts
```

### 变量命名

```typescript
// ✅ 正确示例

// 普通变量：camelCase
const userName = ref('')
const reportList = ref<Report[]>([])
const isLoading = ref(false)

// 常量：UPPER_SNAKE_CASE
const MAX_UPLOAD_SIZE = 5 * 1024 * 1024
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL
const DEFAULT_PAGE_SIZE = 20

// 布尔变量：is/has/can开头
const isVisible = ref(false)
const hasPermission = computed(() => true)
const canEdit = ref(false)

// 私有变量：_开头（非响应式）
const _cache = new Map()
const _timer: number | null = null

// ❌ 错误示例
const user_name = ref('')  // 应使用camelCase
const ReportList = ref([])  // 应使用camelCase
const max_size = 5000       // 常量应使用UPPER_SNAKE_CASE
```

### 类型命名

```typescript
// 接口：PascalCase，I开头（可选）
interface Report {
  id: number
  name: string
}

interface IUserInfo {
  userId: number
  userName: string
}

// 类型别名：PascalCase
type ReportStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED'
type UserId = number
type ReportList = Report[]

// 枚举：PascalCase
enum UserRole {
  ADMIN = 'ADMIN',
  DESIGNER = 'DESIGNER',
  VIEWER = 'VIEWER'
}

// 泛型：单个大写字母或PascalCase
function identity<T>(arg: T): T {
  return arg
}

interface Response<TData> {
  data: TData
  message: string
}
```

---

## 🔌 API调用规范

### 统一API管理

```typescript
// ✅ src/api/report.ts - 统一管理API
import request from '@/utils/request'
import type { Report, CreateReportRequest, UpdateReportRequest } from '@/types/report'

export const reportApi = {
  /**
   * 获取报表列表
   */
  getReports: (params?: { 
    page?: number
    size?: number
    keyword?: string 
  }) => {
    return request.get<Report[]>('/api/v1/reports', { params })
  },
  
  /**
   * 获取报表详情
   */
  getReport: (id: number) => {
    return request.get<Report>(`/api/v1/reports/${id}`)
  },
  
  /**
   * 创建报表
   */
  createReport: (data: CreateReportRequest) => {
    return request.post<Report>('/api/v1/reports', data)
  },
  
  /**
   * 更新报表
   */
  updateReport: (id: number, data: UpdateReportRequest) => {
    return request.put<Report>(`/api/v1/reports/${id}`, data)
  },
  
  /**
   * 删除报表
   */
  deleteReport: (id: number) => {
    return request.delete(`/api/v1/reports/${id}`)
  },
  
  /**
   * 预览报表（测试执行）
   */
  previewReport: (id: number, params: Record<string, any>) => {
    return request.post<any[]>(`/api/v1/reports/${id}/preview`, params)
  }
}
```

### Axios封装

```typescript
// ✅ src/utils/request.ts - 统一错误处理
import axios, { type AxiosError, type AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// 创建axios实例
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 添加token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response: AxiosResponse) => {
    return response
  },
  (error: AxiosError<{ message?: string; code?: string }>) => {
    if (error.response) {
      const { status, data } = error.response
      
      switch (status) {
        case 401:
          ElMessage.error('未登录或登录已过期，请重新登录')
          localStorage.removeItem('token')
          router.push('/login')
          break
          
        case 403:
          ElMessage.error('无权限访问该资源')
          break
          
        case 404:
          ElMessage.error('请求的资源不存在')
          break
          
        case 500:
          ElMessage.error(data?.message || '服务器错误，请稍后重试')
          break
          
        default:
          ElMessage.error(data?.message || `请求失败: ${status}`)
      }
    } else if (error.request) {
      ElMessage.error('网络错误，请检查网络连接')
    } else {
      ElMessage.error('请求配置错误')
    }
    
    return Promise.reject(error)
  }
)

export default request
```

### 组件中使用API

```vue
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { reportApi } from '@/api/report'
import type { Report } from '@/types/report'

const reports = ref<Report[]>([])
const loading = ref(false)

// ✅ 正确：使用async/await + try-catch
const loadReports = async () => {
  loading.value = true
  
  try {
    const { data } = await reportApi.getReports({ page: 1, size: 20 })
    reports.value = data
    ElMessage.success('加载成功')
  } catch (error) {
    console.error('加载失败:', error)
    // 错误已在axios拦截器中处理，这里可选择性处理
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadReports()
})
</script>
```

---

## 🎨 组件设计规范

### 组件拆分原则

```vue
<!-- ✅ 正确：功能清晰的小组件 -->

<!-- ReportList.vue - 列表容器 -->
<template>
  <div class="report-list">
    <report-search @search="handleSearch" />
    <report-table :data="reports" :loading="loading" />
    <report-pagination 
      :total="total" 
      :page="page" 
      @change="handlePageChange" 
    />
  </div>
</template>

<!-- ReportSearch.vue - 搜索组件 -->
<template>
  <el-input 
    v-model="keyword" 
    placeholder="搜索报表"
    @input="handleInput"
  />
</template>

<!-- ReportTable.vue - 表格组件 -->
<template>
  <el-table :data="data" :loading="loading">
    <!-- 表格列 -->
  </el-table>
</template>
```

### Props和Emits规范

```vue
<script setup lang="ts">
// ✅ 正确：使用TypeScript定义Props和Emits

// Props - 使用接口定义
interface Props {
  // 必需属性
  reportId: number
  title: string
  
  // 可选属性
  readonly?: boolean
  showActions?: boolean
  
  // 带默认值的属性
  pageSize?: number
}

const props = withDefaults(defineProps<Props>(), {
  readonly: false,
  showActions: true,
  pageSize: 20
})

// Emits - 明确定义事件类型
const emit = defineEmits<{
  // 带参数的事件
  (e: 'update', data: Report): void
  (e: 'delete', id: number): void
  
  // 无参数的事件
  (e: 'close'): void
  (e: 'refresh'): void
}>()

// 使用emit
const handleUpdate = (report: Report) => {
  emit('update', report)
}
</script>
```

---

## 🎯 状态管理（Pinia）

```typescript
// ✅ src/stores/user.ts - 用户状态管理
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User } from '@/types/user'

export const useUserStore = defineStore('user', () => {
  // State
  const user = ref<User | null>(null)
  const token = ref<string | null>(localStorage.getItem('token'))
  
  // Getters
  const isLoggedIn = computed(() => !!token.value)
  const userName = computed(() => user.value?.username || '游客')
  const userRole = computed(() => user.value?.role || 'VIEWER')
  
  // Actions
  const login = async (username: string, password: string) => {
    try {
      const response = await userApi.login({ username, password })
      token.value = response.data.token
      user.value = response.data.user
      localStorage.setItem('token', response.data.token)
      return true
    } catch (error) {
      console.error('登录失败:', error)
      return false
    }
  }
  
  const logout = () => {
    user.value = null
    token.value = null
    localStorage.removeItem('token')
  }
  
  return {
    // State
    user,
    token,
    
    // Getters
    isLoggedIn,
    userName,
    userRole,
    
    // Actions
    login,
    logout
  }
})
```

---

## 🛡️ 错误处理规范

### 全局错误处理

```typescript
// ✅ src/utils/error-handler.ts
import { ElMessage } from 'element-plus'

export function handleError(error: unknown, context?: string) {
  console.error(context ? `[${context}] 错误:` : '错误:', error)
  
  if (error instanceof Error) {
    ElMessage.error(error.message)
  } else if (typeof error === 'string') {
    ElMessage.error(error)
  } else {
    ElMessage.error('发生未知错误')
  }
}

// 使用示例
import { handleError } from '@/utils/error-handler'

try {
  await reportApi.createReport(data)
} catch (error) {
  handleError(error, '创建报表')
}
```

### 组件错误处理

```vue
<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const error = ref<string | null>(null)

// ✅ 正确：完整的错误处理
const loadData = async () => {
  loading.value = true
  error.value = null
  
  try {
    const response = await api.getData()
    // 处理数据
  } catch (err) {
    console.error('加载失败:', err)
    error.value = '加载数据失败，请重试'
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div>
    <el-skeleton v-if="loading" />
    <el-alert v-else-if="error" type="error" :title="error" />
    <div v-else>
      <!-- 正常内容 -->
    </div>
  </div>
</template>
```

---

## ⚡ 性能优化

### 懒加载

```typescript
// ✅ 路由懒加载
const routes = [
  {
    path: '/reports',
    component: () => import('@/views/ReportList.vue')
  },
  {
    path: '/reports/:id',
    component: () => import('@/views/ReportDetail.vue')
  }
]

// ✅ 组件懒加载
import { defineAsyncComponent } from 'vue'

const HeavyComponent = defineAsyncComponent(() => 
  import('@/components/HeavyComponent.vue')
)
```

### 防抖和节流

```vue
<script setup lang="ts">
import { ref } from 'vue'
import { debounce } from 'lodash-es'

// ✅ 防抖搜索
const keyword = ref('')

const handleSearch = debounce((value: string) => {
  // 搜索逻辑
  console.log('搜索:', value)
}, 300)

// ✅ 节流滚动
const handleScroll = throttle(() => {
  // 滚动逻辑
}, 100)
</script>

<template>
  <el-input 
    v-model="keyword" 
    @input="handleSearch(keyword)"
  />
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
  />
</template>
```

---

## 🎨 样式规范

### Scoped样式

```vue
<style scoped>
/* ✅ 正确：使用scoped避免样式污染 */
.report-list {
  padding: 20px;
}

.report-list__header {
  margin-bottom: 16px;
  display: flex;
  justify-content: space-between;
}

.report-list__actions {
  display: flex;
  gap: 12px;
}

/* 深度选择器 - 修改Element Plus组件样式 */
:deep(.el-table__header) {
  background-color: #f5f7fa;
}
</style>
```

### CSS命名规范（BEM）

```vue
<template>
  <!-- ✅ BEM命名：Block__Element--Modifier -->
  <div class="report-card">
    <div class="report-card__header">
      <h3 class="report-card__title">{{ report.name }}</h3>
      <span class="report-card__status report-card__status--active">
        活跃
      </span>
    </div>
    <div class="report-card__body">
      <p class="report-card__description">{{ report.description }}</p>
    </div>
  </div>
</template>

<style scoped>
/* Block */
.report-card {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 16px;
}

/* Element */
.report-card__header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}

.report-card__title {
  font-size: 18px;
  font-weight: 600;
}

.report-card__status {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

/* Modifier */
.report-card__status--active {
  background-color: #67c23a;
  color: white;
}

.report-card__status--inactive {
  background-color: #909399;
  color: white;
}
</style>
```

---

## 🚫 禁止事项

### 绝对禁止

1. ❌ **禁止使用any类型（除非必要）**
   ```typescript
   // 错误
   const data: any = response.data
   
   // 正确
   const data: Report[] = response.data
   ```

2. ❌ **禁止直接操作DOM**
   ```typescript
   // 错误
   document.getElementById('app')?.style.color = 'red'
   
   // 正确：使用ref
   const appRef = ref<HTMLElement>()
   appRef.value.style.color = 'red'
   ```

3. ❌ **禁止在template中使用复杂逻辑**
   ```vue
   <!-- 错误 -->
   <template>
     <div>{{ reports.filter(r => r.status === 'ACTIVE').map(r => r.name).join(', ') }}</div>
   </template>
   
   <!-- 正确：使用computed -->
   <script setup lang="ts">
   const activeReportNames = computed(() => 
     reports.value
       .filter(r => r.status === 'ACTIVE')
       .map(r => r.name)
       .join(', ')
   )
   </script>
   
   <template>
     <div>{{ activeReportNames }}</div>
   </template>
   ```

4. ❌ **禁止使用console.log在生产环境**
   ```typescript
   // 开发环境可以，生产环境禁止
   if (import.meta.env.DEV) {
     console.log('调试信息:', data)
   }
   ```

---

## ✅ 检查清单

### 代码提交前检查

- [ ] 使用TypeScript定义类型（Props/Emits/API返回值）
- [ ] 组件使用组合式API（<script setup lang="ts">）
- [ ] API调用统一管理（api目录）
- [ ] 错误处理完整（try-catch + ElMessage）
- [ ] 样式使用scoped避免污染
- [ ] 命名符合规范（camelCase/PascalCase/kebab-case）
- [ ] 无any类型（除非必要）
- [ ] 无直接DOM操作
- [ ] 无console.log（生产环境）
- [ ] 通过ESLint + Prettier检查

---

**最后更新**: 2026-01-15
**适用版本**: Vue 3.3.x + TypeScript + Element Plus 2.3.x
