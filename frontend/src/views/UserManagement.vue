<template>
  <div class="user-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="title">用户管理</span>
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            新建用户
          </el-button>
        </div>
      </template>

      <!-- 用户列表 -->
      <el-table
        v-loading="loading"
        :data="userList"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="200" />
        <el-table-column prop="role" label="角色" width="150">
          <template #default="{ row }">
            <el-tag :type="getRoleTagType(row.role)">
              {{ getRoleLabel(row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="enabled" label="状态" width="120">
          <template #default="{ row }">
            <el-switch
              v-model="row.enabled"
              @change="handleToggleStatus(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="200">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="更新时间" width="200">
          <template #default="{ row }">
            {{ formatDateTime(row.updatedAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="formData.username"
            placeholder="请输入用户名（3-50个字符）"
            :disabled="isEdit"
          />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="formData.password"
            type="password"
            placeholder="请输入密码（6-50个字符）"
            show-password
          />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="formData.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="设计者" value="DESIGNER" />
            <el-option label="普通用户" value="VIEWER" />
          </el-select>
        </el-form-item>
        <el-form-item label="启用状态" prop="enabled">
          <el-switch v-model="formData.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { userApi, type User, type CreateUserRequest, type UpdateUserRequest, type UserRole } from '@/api/user'

// 响应式数据
const loading = ref(false)
const userList = ref<User[]>([])
const dialogVisible = ref(false)
const submitting = ref(false)
const isEdit = ref(false)
const currentUserId = ref<number | null>(null)
const formRef = ref<FormInstance>()

// 表单数据
const formData = reactive({
  username: '',
  password: '',
  role: 'VIEWER' as UserRole,
  enabled: true
})

// 表单验证规则
const formRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度必须在3-50个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 50, message: '密码长度必须在6-50个字符之间', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

// 计算属性
const dialogTitle = ref('新建用户')

// 获取角色标签类型
const getRoleTagType = (role: UserRole) => {
  switch (role) {
    case 'ADMIN':
      return 'danger'
    case 'DESIGNER':
      return 'warning'
    case 'VIEWER':
      return 'info'
    default:
      return 'info'
  }
}

// 获取角色标签文本
const getRoleLabel = (role: UserRole) => {
  switch (role) {
    case 'ADMIN':
      return '管理员'
    case 'DESIGNER':
      return '设计者'
    case 'VIEWER':
      return '普通用户'
    default:
      return role
  }
}

// 格式化日期时间
const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 加载用户列表
const loadUsers = async () => {
  loading.value = true
  try {
    const response = await userApi.getAllUsers()
    userList.value = response.data
  } catch (error) {
    console.error('加载用户列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 打开创建对话框
const handleCreate = () => {
  isEdit.value = false
  dialogTitle.value = '新建用户'
  resetForm()
  dialogVisible.value = true
}

// 打开编辑对话框
const handleEdit = (user: User) => {
  isEdit.value = true
  currentUserId.value = user.id
  dialogTitle.value = '编辑用户'
  formData.username = user.username
  formData.password = ''
  formData.role = user.role
  formData.enabled = user.enabled
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      if (isEdit.value && currentUserId.value) {
        // 编辑用户
        const updateData: UpdateUserRequest = {
          role: formData.role,
          enabled: formData.enabled
        }
        if (formData.password) {
          updateData.password = formData.password
        }
        await userApi.updateUser(currentUserId.value, updateData)
        ElMessage.success('更新用户成功')
      } else {
        // 创建用户
        const createData: CreateUserRequest = {
          username: formData.username,
          password: formData.password,
          role: formData.role,
          enabled: formData.enabled
        }
        await userApi.createUser(createData)
        ElMessage.success('创建用户成功')
      }
      
      dialogVisible.value = false
      await loadUsers()
    } catch (error) {
      console.error('保存用户失败:', error)
    } finally {
      submitting.value = false
    }
  })
}

// 切换用户状态
const handleToggleStatus = async (user: User) => {
  try {
    await userApi.toggleUserStatus(user.id, user.enabled)
    ElMessage.success('状态更新成功')
  } catch (error) {
    console.error('更新状态失败:', error)
    // 恢复原状态
    user.enabled = !user.enabled
  }
}

// 删除用户
const handleDelete = async (user: User) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 "${user.username}" 吗？此操作不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await userApi.deleteUser(user.id)
    ElMessage.success('删除用户成功')
    await loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除用户失败:', error)
    }
  }
}

// 重置表单
const resetForm = () => {
  formData.username = ''
  formData.password = ''
  formData.role = 'VIEWER' as UserRole
  formData.enabled = true
  formRef.value?.clearValidate()
}

// 对话框关闭时清理
const handleDialogClose = () => {
  resetForm()
  currentUserId.value = null
}

// 组件挂载时加载数据
onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.user-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  font-size: 18px;
  font-weight: bold;
}
</style>
