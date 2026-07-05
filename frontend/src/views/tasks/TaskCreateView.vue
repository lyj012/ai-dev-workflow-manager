<template>
  <div>
    <AppPageHeader title="创建任务" description="创建一个 AI 开发任务，后续进入 workflow 匹配和阶段推进。">
      <template #actions>
        <el-button :icon="ArrowLeft" @click="router.push('/tasks')">返回列表</el-button>
      </template>
    </AppPageHeader>

    <div class="section create-panel">
      <div class="section-body">
        <TaskForm :submitting="submitting" @submit="submit" @cancel="router.push('/tasks')" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'

import { createTask } from '@/api/task'
import AppPageHeader from '@/components/common/AppPageHeader.vue'
import TaskForm from '@/components/task/TaskForm.vue'
import type { CreateTaskPayload } from '@/types/task'

const router = useRouter()
const submitting = ref(false)

async function submit(payload: CreateTaskPayload) {
  submitting.value = true
  try {
    const task = await createTask(payload)
    ElMessage.success('任务创建成功')
    router.push(`/tasks/${task.id}`)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.create-panel {
  max-width: 860px;
}
</style>
