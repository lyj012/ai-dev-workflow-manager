<template>
  <div>
    <AppPageHeader title="概览" description="轻量展示任务、交付和高风险任务概况，不做复杂统计大屏。">
      <template #actions>
        <el-button type="primary" @click="router.push('/tasks/create')">创建任务</el-button>
      </template>
    </AppPageHeader>

    <div class="metric-grid">
      <div v-for="item in metrics" :key="item.label" class="metric-card">
        <div class="metric-value">{{ item.value }}</div>
        <div class="metric-label">{{ item.label }}</div>
      </div>
    </div>

    <div class="section">
      <div class="section-header">
        <div class="section-title">最近任务</div>
        <el-button link type="primary" @click="router.push('/tasks')">查看全部</el-button>
      </div>
      <TaskTable :tasks="recentTasks" @detail="(id) => router.push(`/tasks/${id}`)" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

import { fetchTasks } from '@/api/task'
import AppPageHeader from '@/components/common/AppPageHeader.vue'
import TaskTable from '@/components/task/TaskTable.vue'
import type { Task } from '@/types/task'

const router = useRouter()
const tasks = ref<Task[]>([])
const recentTasks = computed(() => tasks.value.slice(0, 5))
const metrics = computed(() => [
  { label: '任务总数', value: tasks.value.length },
  { label: '草稿任务', value: tasks.value.filter((item) => item.status === 'DRAFT').length },
  { label: '执行中任务', value: tasks.value.filter((item) => ['ANALYZING', 'EXECUTING', 'TESTING'].includes(item.status)).length },
  { label: '已交付任务', value: tasks.value.filter((item) => item.status === 'DELIVERED').length },
  { label: '高风险任务', value: tasks.value.filter((item) => item.riskTags.length > 0 || item.complexity === 'COMPLEX').length }
])

onMounted(async () => {
  const result = await fetchTasks({ pageNo: 1, pageSize: 20 })
  tasks.value = result.records
})
</script>

<style scoped>
.metric-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.metric-card {
  padding: 18px;
  border: 1px solid var(--app-border);
  border-radius: 8px;
  background: var(--app-surface);
}

.metric-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--app-text);
}

.metric-label {
  margin-top: 4px;
  color: var(--app-muted);
}
</style>
