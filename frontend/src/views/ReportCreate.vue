<template>
  <div class="report-create-container">
    <el-card class="report-create-card">
      <template #header>
        <div class="card-header">
          <span class="title">创建报表 - 基本信息配置</span>
          <span class="subtitle">Step 1: 配置报表名称和描述</span>
        </div>
      </template>
      <ReportForm />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import ReportForm from '@/components/ReportForm.vue'
</script>

<style scoped>
.report-create-container {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.report-create-card {
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.title {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.subtitle {
  font-size: 14px;
  color: #909399;
}
</style>
