<template>
  <div class="report-form">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-width="120px"
      label-position="right"
      @submit.prevent
    >
      <!-- 报表名称 -->
      <el-form-item label="报表名称" prop="name">
        <el-input
          v-model="formData.name"
          placeholder="请输入报表名称（最多50字符）"
          maxlength="50"
          show-word-limit
          clearable
          @input="handleNameInput"
        >
          <template #suffix>
            <el-icon v-if="nameCheckLoading" class="is-loading">
              <Loading />
            </el-icon>
            <el-icon v-else-if="nameCheckResult === 'success'" class="check-icon success">
              <CircleCheck />
            </el-icon>
            <el-icon v-else-if="nameCheckResult === 'error'" class="check-icon error">
              <CircleClose />
            </el-icon>
          </template>
        </el-input>
        <div v-if="nameCheckMessage" :class="['check-message', nameCheckResult]">
          {{ nameCheckMessage }}
        </div>
      </el-form-item>

      <!-- 报表描述 -->
      <el-form-item label="报表描述" prop="description">
        <el-input
          v-model="formData.description"
          type="textarea"
          :rows="4"
          placeholder="请输入报表描述（选填，最多500字符）"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>

      <!-- 表单按钮 -->
      <el-form-item>
        <el-button type="primary" :disabled="!canSubmit" @click="handleSubmit">
          下一步
        </el-button>
        <el-button @click="handleReset">
          重置
        </el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { Loading, CircleCheck, CircleClose } from '@element-plus/icons-vue'
import { checkReportName } from '@/api/report'

// 表单数据
interface ReportFormData {
  name: string
  description: string
}

const formRef = ref<FormInstance>()
const formData = reactive<ReportFormData>({
  name: '',
  description: ''
})

// 名称唯一性检查状态
const nameCheckLoading = ref(false)
const nameCheckResult = ref<'success' | 'error' | ''>('')
const nameCheckMessage = ref('')
let checkNameTimer: ReturnType<typeof setTimeout> | null = null

// 自定义校验器：检查名称唯一性
const validateNameUniqueness = async (_rule: any, value: string, callback: any) => {
  if (!value || value.trim() === '') {
    callback()
    return
  }

  try {
    const response = await checkReportName(value)
    if (response.exists) {
      nameCheckResult.value = 'error'
      nameCheckMessage.value = '报表名称已存在，请使用其他名称'
      callback(new Error('报表名称已存在'))
    } else {
      nameCheckResult.value = 'success'
      nameCheckMessage.value = '✓ 名称可用'
      callback()
    }
  } catch (error) {
    // 网络错误时，允许继续（后端保存时再校验）
    nameCheckResult.value = ''
    nameCheckMessage.value = '⚠️ 无法校验名称唯一性，提交时将再次校验'
    callback()
  }
}

// 表单校验规则
const rules = reactive<FormRules<ReportFormData>>({
  name: [
    { required: true, message: '报表名称不能为空', trigger: 'blur' },
    { min: 1, max: 50, message: '名称长度在 1 到 50 个字符', trigger: 'blur' },
    { 
      asyncValidator: validateNameUniqueness, 
      trigger: 'blur' 
    } as any
  ],
  description: [
    { max: 500, message: '描述不能超过500字符', trigger: 'blur' }
  ]
})

// 处理名称输入（防抖）
const handleNameInput = () => {
  // 清除之前的定时器
  if (checkNameTimer) {
    clearTimeout(checkNameTimer)
  }

  // 重置检查状态
  nameCheckResult.value = ''
  nameCheckMessage.value = ''

  // 如果名称为空，不进行检查
  if (!formData.name || formData.name.trim() === '') {
    return
  }

  // 设置新的定时器（500ms防抖）
  nameCheckLoading.value = true
  checkNameTimer = setTimeout(async () => {
    try {
      const response = await checkReportName(formData.name)
      if (response.exists) {
        nameCheckResult.value = 'error'
        nameCheckMessage.value = '报表名称已存在，请使用其他名称'
      } else {
        nameCheckResult.value = 'success'
        nameCheckMessage.value = '✓ 名称可用'
      }
    } catch (error) {
      nameCheckResult.value = ''
      nameCheckMessage.value = '⚠️ 无法校验名称唯一性'
    } finally {
      nameCheckLoading.value = false
    }
  }, 500)
}

// 是否可以提交
const canSubmit = computed(() => {
  return (
    formData.name.trim() !== '' &&
    formData.name.length <= 50 &&
    formData.description.length <= 500 &&
    nameCheckResult.value === 'success'
  )
})

// 处理表单提交
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate((valid, fields) => {
    if (valid) {
      ElMessage.success('校验通过，准备进入下一步')
      // 这里可以触发事件，让父组件处理
      console.log('Form data:', formData)
    } else {
      ElMessage.error('请检查表单输入')
      console.log('Validation failed:', fields)
    }
  })
}

// 重置表单
const handleReset = () => {
  formRef.value?.resetFields()
  nameCheckResult.value = ''
  nameCheckMessage.value = ''
}
</script>

<style scoped>
.report-form {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;
}

.check-icon {
  font-size: 16px;
}

.check-icon.success {
  color: #52c41a;
}

.check-icon.error {
  color: #f5222d;
}

.check-message {
  margin-top: 5px;
  font-size: 12px;
}

.check-message.success {
  color: #52c41a;
}

.check-message.error {
  color: #f5222d;
}

:deep(.el-input.is-error .el-input__wrapper) {
  box-shadow: 0 0 0 1px #f5222d inset;
}

:deep(.el-input.is-success .el-input__wrapper) {
  box-shadow: 0 0 0 1px #52c41a inset;
}

.is-loading {
  animation: rotating 2s linear infinite;
}

@keyframes rotating {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
