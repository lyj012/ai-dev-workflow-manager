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
  gap: 22px;
  margin-bottom: 34px;
}

.metric-card {
  min-height: 194px;
  padding: 28px 26px;
  border: 1px solid var(--app-border);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: var(--app-shadow);
}

.metric-icon {
  display: inline-flex;
  width: 48px;
  height: 48px;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  font-size: 24px;
}

.metric-icon--blue {
  background: #edf4ff;
  color: #2864e8;
}

.metric-icon--green {
  background: #e8f9ef;
  color: #27b66d;
}

.metric-icon--orange {
  background: #fff4df;
  color: #f2991b;
}

.metric-icon--purple {
  background: #f1eaff;
  color: #7c52ff;
}

.metric-icon--red {
  background: #ffecec;
  color: #f04444;
}

.metric-value {
  margin-top: 20px;
  font-size: 36px;
  font-weight: 800;
  color: var(--app-text);
  line-height: 1;
}

.metric-label {
  margin-top: 12px;
  color: var(--app-muted);
  font-size: 16px;
}
</style>
