<template>
  <div class="dashboard-container">
    <div class="header">
      <h1>GCT Reporter 控制台</h1>
      <div class="user-info">
        <span>{{ userInfo.username }} ({{ userInfo.role }})</span>
        <el-button type="danger" size="small" @click="handleLogout">退出登录</el-button>
      </div>
    </div>
    <div class="content">
      <el-card class="welcome-card">
        <h2>🎉 欢迎使用 GCT Reporter</h2>
        <p>这是一个程序员报表生成工具的控制台页面</p>
        <el-divider />
        <div class="info-grid">
          <div class="info-item">
            <div class="label">用户名</div>
            <div class="value">{{ userInfo.username }}</div>
          </div>
          <div class="info-item">
            <div class="label">角色</div>
            <div class="value">{{ roleText }}</div>
          </div>
          <div class="info-item">
            <div class="label">用户ID</div>
            <div class="value">{{ userInfo.userId }}</div>
          </div>
          <div class="info-item">
            <div class="label">Token</div>
            <div class="value token-value">{{ userInfo.token?.substring(0, 30) }}...</div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { logout } from '@/api/auth'

const router = useRouter()

interface UserInfo {
  username: string
  role: string
  userId: number
  token: string
}

const userInfo = ref<UserInfo>({
  username: '',
  role: '',
  userId: 0,
  token: ''
})

const roleText = computed(() => {
  const roleMap: Record<string, string> = {
    'ADMIN': '管理员',
    'DESIGNER': '设计者',
    'VIEWER': '查看者'
  }
  return roleMap[userInfo.value.role] || userInfo.value.role
})

const loadUserInfo = () => {
  const username = localStorage.getItem('username') || ''
  const role = localStorage.getItem('role') || ''
  const userId = Number(localStorage.getItem('userId')) || 0
  const token = localStorage.getItem('token') || ''
  
  userInfo.value = { username, role, userId, token }
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await logout()
    
    // 清除本地存储
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    localStorage.removeItem('role')
    localStorage.removeItem('userId')
    
    ElMessage.success('已退出登录')
    router.push('/')
  } catch (error) {
    // 用户取消或其他错误
  }
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.dashboard-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.header h1 {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.user-info span {
  font-size: 14px;
  color: #666;
}

.content {
  max-width: 1200px;
  margin: 0 auto;
}

.welcome-card {
  text-align: center;
  padding: 40px;
}

.welcome-card h2 {
  margin: 0 0 15px 0;
  font-size: 32px;
  color: #333;
}

.welcome-card p {
  margin: 0 0 30px 0;
  font-size: 16px;
  color: #666;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-top: 30px;
}

.info-item {
  text-align: left;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
  transition: all 0.3s;
}

.info-item:hover {
  background: #ecf0f5;
  transform: translateY(-2px);
}

.info-item .label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.info-item .value {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.token-value {
  font-family: 'Courier New', monospace;
  font-size: 12px;
  word-break: break-all;
}

:deep(.el-divider) {
  margin: 30px 0;
}
</style>
