<template>
  <div class="change-password-page">
    <!-- 赛博朋克网格背景 -->
    <div class="cyber-grid"></div>
    <div class="particles" ref="particlesContainer"></div>

    <!-- 修改密码表单 -->
    <div class="change-password-container">
      <div class="change-password-box">
        <div class="header-section">
          <h1 class="page-title">修改密码</h1>
          <p class="page-subtitle">Change Password</p>
        </div>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          class="cyber-form"
          @submit.prevent="handleSubmit"
        >
          <el-form-item prop="oldPassword">
            <label class="form-label">旧密码 Old Password</label>
            <div class="input-container">
              <span class="input-icon">◆</span>
              <input
                v-model="form.oldPassword"
                type="password"
                class="form-input"
                placeholder="Enter your old password"
                autocomplete="off"
              />
            </div>
          </el-form-item>

          <el-form-item prop="newPassword">
            <label class="form-label">新密码 New Password</label>
            <div class="input-container">
              <span class="input-icon">◇</span>
              <input
                v-model="form.newPassword"
                type="password"
                class="form-input"
                placeholder="Enter new password (6-50 characters)"
                autocomplete="off"
              />
            </div>
          </el-form-item>

          <el-form-item prop="confirmPassword">
            <label class="form-label">确认密码 Confirm Password</label>
            <div class="input-container">
              <span class="input-icon">◇</span>
              <input
                v-model="form.confirmPassword"
                type="password"
                class="form-input"
                placeholder="Confirm new password"
                autocomplete="off"
              />
            </div>
          </el-form-item>

          <div class="button-group">
            <button
              type="submit"
              class="cyber-button primary"
              :disabled="loading"
            >
              {{ loading ? 'Processing...' : 'Submit' }}
            </button>
            <button
              type="button"
              class="cyber-button secondary"
              @click="handleCancel"
              :disabled="loading"
            >
              Cancel
            </button>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { changePassword } from '@/api/auth'

const router = useRouter()
const formRef = ref<FormInstance>()
const particlesContainer = ref<HTMLElement>()
const loading = ref(false)

const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 验证确认密码
const validateConfirmPassword = (rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请输入确认密码'))
  } else if (value !== form.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules: FormRules = {
  oldPassword: [
    { required: true, message: '请输入旧密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 50, message: '密码长度必须在6-50个字符之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请输入确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 生成粒子背景
const createParticles = () => {
  if (!particlesContainer.value) return
  
  for (let i = 0; i < 20; i++) {
    const particle = document.createElement('div')
    particle.className = 'particle'
    particle.style.left = Math.random() * 100 + 'vw'
    particle.style.top = Math.random() * 100 + 'vh'
    particle.style.animationDelay = Math.random() * 10 + 's'
    particle.style.animationDuration = (Math.random() * 10 + 10) + 's'
    particlesContainer.value.appendChild(particle)
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    
    loading.value = true

    const username = localStorage.getItem('username')
    if (!username) {
      ElMessage.error('用户未登录')
      router.push('/')
      return
    }

    // 调用后端API
    await changePassword({
      username,
      oldPassword: form.oldPassword,
      newPassword: form.newPassword,
      confirmPassword: form.confirmPassword
    })

    ElMessage.success('密码修改成功，请重新登录')
    
    // 清除登录信息
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    localStorage.removeItem('role')
    localStorage.removeItem('userId')
    
    // 跳转到登录页
    setTimeout(() => {
      router.push('/')
    }, 1500)
  } catch (error: any) {
    console.error('Change password failed:', error)
    // 错误已被axios拦截器处理
  } finally {
    loading.value = false
  }
}

// 取消操作
const handleCancel = () => {
  router.push('/dashboard')
}

onMounted(() => {
  createParticles()
  console.log('%c🔒 Change Password', 'color: #0ff; font-size: 14px; font-weight: bold;')
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Orbitron:wght@400;700;900&family=Noto+Sans+SC:wght@300;400;500;600&display=swap');

/* CSS变量定义 */
.change-password-page {
  --primary: #0ff;
  --secondary: #f0f;
  --dark: #0a0a0f;
  --darker: #050508;
  --light: #e0e0ff;
  --grid-color: rgba(0, 255, 255, 0.08);
  --glow: rgba(0, 255, 255, 0.5);
}

.change-password-page {
  min-height: 100vh;
  background: var(--darker);
  color: var(--light);
  overflow-x: hidden;
  position: relative;
}

/* 赛博朋克网格背景 */
.cyber-grid {
  position: fixed;
  inset: 0;
  background-image: 
    linear-gradient(var(--grid-color) 1px, transparent 1px),
    linear-gradient(90deg, var(--grid-color) 1px, transparent 1px);
  background-size: 50px 50px;
  animation: gridScroll 20s linear infinite;
  pointer-events: none;
  z-index: 0;
}

@keyframes gridScroll {
  0% { transform: perspective(500px) rotateX(60deg) translateZ(0); }
  100% { transform: perspective(500px) rotateX(60deg) translateZ(50px); }
}

/* 粒子效果 */
.particles {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 1;
}
.particle {
  position: absolute;
  width: 2px;
  height: 2px;
  background: #0ff;
  border-radius: 50%;
  box-shadow: 0 0 10px #0ff;
  animation: float 10s infinite;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0); opacity: 0; }
  10% { opacity: 1; }
  90% { opacity: 1; }
  100% { transform: translate(100vw, -100vh); opacity: 0; }
}

/* 容器 */
.change-password-container {
  position: relative;
  z-index: 10;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
}

.change-password-box {
  position: relative;
  width: 100%;
  max-width: 500px;
  background: rgba(10, 10, 15, 0.85);
  border: 2px solid var(--primary);
  border-radius: 0;
  padding: 50px 40px;
  backdrop-filter: blur(20px);
  box-shadow: 
    0 0 40px var(--glow),
    inset 0 0 40px rgba(0, 255, 255, 0.05);
  animation: boxGlow 3s ease-in-out infinite;
}

@keyframes boxGlow {
  0%, 100% { box-shadow: 0 0 40px var(--glow), inset 0 0 40px rgba(0, 255, 255, 0.05); }
  50% { box-shadow: 0 0 60px var(--glow), inset 0 0 60px rgba(0, 255, 255, 0.08); }
}

/* 标题区域 */
.header-section {
  text-align: center;
  margin-bottom: 40px;
  animation: slideDown 0.8s cubic-bezier(0.68, -0.55, 0.265, 1.55);
}

@keyframes slideDown {
  from { opacity: 0; transform: translateY(-30px); }
  to { opacity: 1; transform: translateY(0); }
}

.page-title {
  font-family: 'Noto Sans SC', sans-serif;
  font-size: 28px;
  font-weight: 600;
  color: var(--primary);
  letter-spacing: 2px;
  margin-bottom: 8px;
}

.page-subtitle {
  font-size: 12px;
  color: var(--primary);
  letter-spacing: 4px;
  text-transform: uppercase;
  opacity: 0.7;
  font-weight: 300;
}

/* 表单样式 */
.cyber-form {
  animation: fadeIn 1s ease-out 0.3s both;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

/* Element Plus表单项覆盖 */
:deep(.el-form-item) {
  margin-bottom: 30px;
  border: none;
}

:deep(.el-form-item__content) {
  line-height: normal;
}

:deep(.el-form-item__error) {
  color: #ff3366;
  font-size: 11px;
  padding-top: 6px;
}

.form-label {
  display: block;
  color: var(--primary);
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 2px;
  margin-bottom: 10px;
  font-weight: 600;
  font-family: 'Noto Sans SC', sans-serif;
}

.input-container {
  position: relative;
}

.form-input {
  width: 100%;
  padding: 14px 20px 14px 45px;
  background: rgba(0, 255, 255, 0.03);
  border: 1px solid var(--primary);
  color: var(--light);
  font-size: 14px;
  outline: none;
  transition: all 0.3s;
  font-family: 'Noto Sans SC', sans-serif;
}

.form-input:focus {
  background: rgba(0, 255, 255, 0.08);
  border-color: var(--secondary);
  box-shadow: 0 0 20px rgba(255, 0, 255, 0.3);
}

.form-input::placeholder {
  color: rgba(224, 224, 255, 0.3);
}

.input-icon {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 16px;
  opacity: 0.6;
  pointer-events: none;
  color: var(--primary);
}

/* 按钮组 */
.button-group {
  display: flex;
  gap: 15px;
  margin-top: 30px;
}

.cyber-button {
  flex: 1;
  padding: 14px;
  background: transparent;
  border: 2px solid var(--primary);
  color: var(--primary);
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 2px;
  text-transform: uppercase;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
  font-family: 'Orbitron', sans-serif;
}

.cyber-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, var(--primary), transparent);
  transition: left 0.5s;
}

.cyber-button:hover::before {
  left: 100%;
}

.cyber-button.primary:hover {
  color: var(--dark);
  background: var(--primary);
  box-shadow: 0 0 30px var(--glow);
  transform: translateY(-2px);
}

.cyber-button.secondary {
  border-color: rgba(224, 224, 255, 0.3);
  color: rgba(224, 224, 255, 0.7);
}

.cyber-button.secondary:hover {
  border-color: var(--light);
  color: var(--light);
  transform: translateY(-2px);
}

.cyber-button:active {
  transform: translateY(0);
}

.cyber-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 响应式 */
@media (max-width: 768px) {
  .change-password-box {
    padding: 40px 30px;
  }

  .page-title {
    font-size: 24px;
  }

  .button-group {
    flex-direction: column;
  }
}
</style>
