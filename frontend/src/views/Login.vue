<template>
  <div class="login-page">
    <!-- 赛博朋克网格背景 -->
    <div class="cyber-grid"></div>
    <div class="particles" ref="particlesContainer"></div>

    <!-- 登录表单 -->
    <div class="login-container">
      <div class="login-box">
        <div class="logo-section">
          <h1 class="logo-title">GCT REPORTER</h1>
          <p class="logo-subtitle">System Access</p>
        </div>

        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          class="cyber-form"
          @submit.prevent="handleLogin"
        >
          <el-form-item prop="username">
            <label class="form-label">Username</label>
            <div class="input-container">
              <span class="input-icon">▶</span>
              <input
                v-model="loginForm.username"
                type="text"
                class="form-input"
                placeholder="Enter your username"
                autocomplete="off"
                @blur="validateField('username')"
              />
            </div>
            <div v-if="errors.username" class="error-msg show">
              {{ errors.username }}
            </div>
          </el-form-item>

          <el-form-item prop="password">
            <label class="form-label">Password</label>
            <div class="input-container">
              <span class="input-icon">◆</span>
              <input
                v-model="loginForm.password"
                type="password"
                class="form-input"
                placeholder="Enter your password"
                @blur="validateField('password')"
              />
            </div>
            <div v-if="errors.password" class="error-msg show">
              {{ errors.password }}
            </div>
          </el-form-item>

          <button
            type="submit"
            class="cyber-button"
            :disabled="loading"
          >
            {{ loading ? 'AUTHENTICATING...' : 'Access System' }}
          </button>
        </el-form>

        <div class="test-info">
          <h4>// Test Accounts</h4>
          <ul>
            <li><code>admin</code> : <code>admin123</code> [ADMIN]</li>
            <li><code>designer</code> : <code>designer123</code> [DESIGNER]</li>
            <li><code>viewer</code> : <code>viewer123</code> [VIEWER]</li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { login } from '@/api/auth'

const router = useRouter()
const loginFormRef = ref<FormInstance>()
const particlesContainer = ref<HTMLElement>()
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const errors = reactive({
  username: '',
  password: ''
})

const loginRules: FormRules = {
  username: [
    { required: true, message: 'Please enter username', trigger: 'blur' }
  ],
  password: [
    { required: true, message: 'Please enter password', trigger: 'blur' }
  ]
}

// 生成粒子背景
const createParticles = () => {
  if (!particlesContainer.value) return
  
  for (let i = 0; i < 30; i++) {
    const particle = document.createElement('div')
    particle.className = 'particle'
    particle.style.left = Math.random() * 100 + 'vw'
    particle.style.top = Math.random() * 100 + 'vh'
    particle.style.animationDelay = Math.random() * 10 + 's'
    particle.style.animationDuration = (Math.random() * 10 + 10) + 's'
    particlesContainer.value.appendChild(particle)
  }
}

// 字段验证
const validateField = (field: 'username' | 'password') => {
  if (!loginForm[field]) {
    errors[field] = field === 'username' ? 'Invalid username' : 'Invalid password'
  } else {
    errors[field] = ''
  }
}

// 登录处理
const handleLogin = async () => {
  if (!loginFormRef.value) return

  try {
    await loginFormRef.value.validate()
    
    loading.value = true

    // 调用后端API
    const response = await login({
      username: loginForm.username,
      password: loginForm.password
    })

    ElMessage.success(`Access granted: Welcome ${response.username}!`)
    
    // 保存token和用户信息
    localStorage.setItem('token', response.token)
    localStorage.setItem('username', response.username)
    localStorage.setItem('role', response.role)
    localStorage.setItem('userId', response.userId.toString())
    
    // 跳转到首页
    setTimeout(() => {
      router.push('/dashboard')
    }, 1000)
  } catch (error: any) {
    console.error('Login failed:', error)
    
    // 错误已被axios拦截器处理，这里可以做额外处理
    if (error.response?.status === 401) {
      errors.username = 'Invalid credentials'
      errors.password = 'Invalid credentials'
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  createParticles()
  console.log('%c⚡ GCT REPORTER SYSTEM ONLINE', 'color: #0ff; font-size: 16px; font-weight: bold;')
  console.log('%cCyberpunk Edition - Enhanced Design', 'color: #f0f; font-size: 12px;')
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Orbitron:wght@400;700;900&family=Noto+Sans+SC:wght@300;400;500;600&display=swap');

/* CSS变量定义 */
.login-page {
  --primary: #0ff;
  --secondary: #f0f;
  --dark: #0a0a0f;
  --darker: #050508;
  --light: #e0e0ff;
  --grid-color: rgba(0, 255, 255, 0.08);
  --glow: rgba(0, 255, 255, 0.5);
}

.login-page {
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

/* 登录容器 */
.login-container {
  position: relative;
  z-index: 10;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
}

.login-box {
  position: relative;
  width: 100%;
  max-width: 480px;
  background: rgba(10, 10, 15, 0.85);
  border: 2px solid var(--primary);
  border-radius: 0;
  padding: 60px 50px;
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

/* Logo区域 */
.logo-section {
  text-align: center;
  margin-bottom: 50px;
  animation: slideDown 0.8s cubic-bezier(0.68, -0.55, 0.265, 1.55);
}

@keyframes slideDown {
  from { opacity: 0; transform: translateY(-50px); }
  to { opacity: 1; transform: translateY(0); }
}

.logo-title {
  font-family: 'Orbitron', sans-serif;
  font-size: 42px;
  font-weight: 900;
  background: linear-gradient(135deg, var(--primary), var(--secondary));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 4px;
  text-transform: uppercase;
  margin-bottom: 10px;
  text-shadow: 0 0 30px var(--glow);
  animation: titlePulse 2s ease-in-out infinite;
}

@keyframes titlePulse {
  0%, 100% { filter: brightness(1); }
  50% { filter: brightness(1.3); }
}

.logo-subtitle {
  font-size: 13px;
  color: var(--primary);
  letter-spacing: 6px;
  text-transform: uppercase;
  opacity: 0.8;
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
  margin-bottom: 35px;
  border: none;
}

:deep(.el-form-item__content) {
  line-height: normal;
}

:deep(.el-form-item__error) {
  display: none; /* 使用自定义错误提示 */
}

.form-label {
  display: block;
  color: var(--primary);
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 2px;
  margin-bottom: 12px;
  font-weight: 600;
}

.input-container {
  position: relative;
}

.form-input {
  width: 100%;
  padding: 16px 20px 16px 50px;
  background: rgba(0, 255, 255, 0.03);
  border: 1px solid var(--primary);
  color: var(--light);
  font-size: 15px;
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
  left: 18px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 18px;
  opacity: 0.6;
  pointer-events: none;
  color: var(--primary);
}

.error-msg {
  position: absolute;
  bottom: -22px;
  left: 0;
  color: #ff3366;
  font-size: 11px;
  opacity: 0;
  transition: opacity 0.3s;
}

.error-msg.show {
  opacity: 1;
}

/* 登录按钮 */
.cyber-button {
  width: 100%;
  padding: 18px;
  background: transparent;
  border: 2px solid var(--primary);
  color: var(--primary);
  font-size: 14px;
  font-weight: 700;
  letter-spacing: 3px;
  text-transform: uppercase;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
  font-family: 'Orbitron', sans-serif;
  margin-top: 20px;
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

.cyber-button:hover {
  color: var(--dark);
  background: var(--primary);
  box-shadow: 0 0 30px var(--glow);
  transform: translateY(-2px);
}

.cyber-button:active {
  transform: translateY(0);
}

.cyber-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 测试账号提示 */
.test-info {
  margin-top: 40px;
  padding: 20px;
  background: rgba(0, 255, 255, 0.03);
  border-left: 3px solid var(--primary);
  animation: fadeIn 1.2s ease-out 0.6s both;
}

.test-info h4 {
  color: var(--primary);
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 2px;
  margin-bottom: 12px;
  font-family: 'Orbitron', sans-serif;
}

.test-info ul {
  list-style: none;
}

.test-info li {
  font-size: 12px;
  color: rgba(224, 224, 255, 0.7);
  margin-bottom: 8px;
  font-family: monospace;
}

.test-info code {
  color: var(--secondary);
  background: rgba(255, 0, 255, 0.1);
  padding: 2px 8px;
  border-radius: 2px;
}

/* 响应式 */
@media (max-width: 768px) {
  .login-box {
    padding: 40px 30px;
  }

  .logo-title {
    font-size: 32px;
  }
}
</style>
