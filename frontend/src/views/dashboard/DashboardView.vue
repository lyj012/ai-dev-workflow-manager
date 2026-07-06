<template>
  <div>
    <AppPageHeader title="概览" description="轻量展示任务、交付和高风险任务概况，不做复杂统计大屏。">
      <template #actions>
        <el-button type="primary" size="large" :icon="CirclePlusFilled" @click="router.push('/tasks/create')">创建任务</el-button>
      </template>
    </AppPageHeader>

    <div class="metric-grid">
      <div v-for="item in metrics" :key="item.label" class="metric-card">
        <div class="metric-icon" :class="`metric-icon--${item.tone}`">
          <el-icon><component :is="item.icon" /></el-icon>
        </div>
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
import { Box, CircleCheck, CirclePlusFilled, Document, VideoPlay, Warning } from '@element-plus/icons-vue'

import { fetchTasks } from '@/api/task'
import AppPageHeader from '@/components/common/AppPageHeader.vue'
import TaskTable from '@/components/task/TaskTable.vue'
import type { Task } from '@/types/task'

const router = useRouter()
const tasks = ref<Task[]>([])
const recentTasks = computed(() => tasks.value.slice(0, 5))
const metrics = computed(() => [
  { label: '任务总数', value: tasks.value.length, tone: 'blue', icon: Box },
  { label: '草稿任务', value: tasks.value.filter((item) => item.status === 'DRAFT').length, tone: 'green', icon: Document },
  { label: '执行中任务', value: tasks.value.filter((item) => ['ANALYZING', 'EXECUTING', 'TESTING'].includes(item.status)).length, tone: 'orange', icon: VideoPlay },
  { label: '已交付任务', value: tasks.value.filter((item) => item.status === 'DELIVERED').length, tone: 'purple', icon: CircleCheck },
  { label: '高风险任务', value: tasks.value.filter((item) => item.riskTags.length > 0 || item.complexity === 'COMPLEX').length, tone: 'red', icon: Warning }
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
  gap: 14px;
  margin-bottom: 18px;
}

.metric-card {
  min-height: 154px;
  padding: 20px;
  border: 1px solid var(--app-border);
  border-radius: 8px;
  background: var(--app-surface);
  box-shadow: var(--app-shadow-soft);
}

.metric-icon {
  display: inline-flex;
  width: 36px;
  height: 36px;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  font-size: 19px;
}

.metric-icon--blue {
  background: #eef2ff;
  color: #4f46e5;
}

.metric-icon--green {
  background: #e8f8ef;
  color: var(--app-success);
}

.metric-icon--orange {
  background: #fff7e8;
  color: var(--app-warning);
}

.metric-icon--purple {
  background: #f2efff;
  color: var(--app-accent);
}

.metric-icon--red {
  background: #fff0f0;
  color: var(--app-danger);
}

.metric-value {
  margin-top: 16px;
  font-size: 30px;
  font-weight: 650;
  color: var(--app-text);
  line-height: 1;
}

.metric-label {
  margin-top: 9px;
  color: var(--app-muted);
  font-size: 13px;
}
</style>
