<template>
  <div class="user-list-container">
    <div class="header">
      <h2>用户管理</h2>
    </div>

    <!-- 搜索框区域 -->
    <el-card class="search-card">
      <div class="search-box">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索用户名或邮箱"
          clearable
          class="search-input"
          @input="handleSearch"
          @clear="handleClear"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <div v-if="searchKeyword" class="search-info">
          搜索到 <span class="highlight">{{ filteredUsers.length }}</span> 个用户
        </div>
      </div>
    </el-card>

    <!-- 用户列表 -->
    <el-card class="user-list-card">
      <el-table
        v-loading="loading"
        :data="filteredUsers"
        style="width: 100%"
        stripe
        border
      >
        <el-table-column prop="id" label="用户ID" width="80" />
        <el-table-column label="用户名" width="200">
          <template #default="{ row }">
            <span v-html="highlightText(row.username, searchKeyword)"></span>
          </template>
        </el-table-column>
        <el-table-column label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="getRoleType(row.role)">
              {{ getRoleText(row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'danger'">
              {{ row.enabled ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="更新时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.updatedAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="150">
          <template #default="{ row: _row }">
            <el-button type="primary" size="small" link>编辑</el-button>
            <el-button type="danger" size="small" link>删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="filteredUsers.length === 0 && !loading" class="empty-state">
        <el-empty
          :description="searchKeyword ? '未找到匹配的用户' : '暂无用户数据'"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getUsers, type User } from '@/api/user'

const loading = ref(false)
const searchKeyword = ref('')
const users = ref<User[]>([])

// 防抖定时器
let searchTimer: ReturnType<typeof setTimeout> | null = null

/**
 * 过滤后的用户列表（基于搜索关键字）
 */
const filteredUsers = computed(() => {
  if (!searchKeyword.value) {
    return users.value
  }
  
  const keyword = searchKeyword.value.toLowerCase()
  return users.value.filter(user => 
    user.username.toLowerCase().includes(keyword)
  )
})

/**
 * 加载用户列表
 */
const loadUsers = async () => {
  loading.value = true
  try {
    const { data } = await getUsers()
    users.value = data
  } catch (error) {
    console.error('加载用户列表失败:', error)
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 搜索处理（带防抖）
 * 
 * 使用300ms防抖优化搜索性能
 */
const handleSearch = () => {
  // 清除之前的定时器
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  
  // 设置300ms防抖
  searchTimer = setTimeout(() => {
    // 这里可以调用API进行服务端搜索，但由于数据量小，使用客户端过滤即可
    // 如果需要服务端搜索，可以取消注释下面的代码
    // loadUsersWithSearch(searchKeyword.value)
  }, 300)
}

/**
 * 清空搜索
 */
const handleClear = () => {
  searchKeyword.value = ''
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
}

/**
 * 高亮显示匹配的文本
 * 
 * @param text 原始文本
 * @param keyword 搜索关键字
 * @returns HTML字符串
 */
const highlightText = (text: string, keyword: string): string => {
  if (!keyword || !text) {
    return text
  }
  
  const regex = new RegExp(`(${keyword})`, 'gi')
  return text.replace(regex, '<span class="highlight-text">$1</span>')
}

/**
 * 获取角色类型
 */
const getRoleType = (role: string) => {
  const typeMap: Record<string, string> = {
    'ADMIN': 'danger',
    'DESIGNER': 'warning',
    'VIEWER': 'info'
  }
  return typeMap[role] || 'info'
}

/**
 * 获取角色文本
 */
const getRoleText = (role: string) => {
  const textMap: Record<string, string> = {
    'ADMIN': '管理员',
    'DESIGNER': '设计者',
    'VIEWER': '查看者'
  }
  return textMap[role] || role
}

/**
 * 格式化日期
 */
const formatDate = (dateString: string) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.user-list-container {
  padding: 20px;
  min-height: 100vh;
  background: #f5f7fa;
}

.header {
  margin-bottom: 20px;
}

.header h2 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.search-card {
  margin-bottom: 20px;
}

.search-box {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.search-input {
  max-width: 400px;
}

.search-info {
  font-size: 14px;
  color: #606266;
}

.search-info .highlight {
  color: #409eff;
  font-weight: 600;
  font-size: 16px;
}

.user-list-card {
  background: white;
}

.empty-state {
  padding: 40px 0;
}

/* 高亮文本样式 */
:deep(.highlight-text) {
  background-color: #ffd04b;
  color: #303133;
  font-weight: 600;
  padding: 2px 4px;
  border-radius: 2px;
}

/* 表格自适应 */
:deep(.el-table) {
  font-size: 14px;
}

:deep(.el-table__header th) {
  background-color: #f5f7fa;
  color: #303133;
  font-weight: 600;
}

:deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}
</style>
