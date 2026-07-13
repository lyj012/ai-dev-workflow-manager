<template>
  <div>
    <AppPageHeader title="任务列表" description="查看、筛选并进入 AI 开发任务详情。">
      <template #actions>
        <el-button type="primary" :icon="Plus" @click="router.push('/tasks/create')">创建任务</el-button>
      </template>
    </AppPageHeader>

    <div class="metrics-grid">
      <div class="metric-card">
        <div class="metric-icon metric-blue"><el-icon><Document /></el-icon></div>
        <div>
          <div class="metric-value">{{ total }}</div>
          <div class="metric-label">全部任务</div>
          <div class="metric-hint">覆盖 workflow 主链路</div>
        </div>
      </div>
      <div class="metric-card">
        <div class="metric-icon metric-indigo"><el-icon><DataAnalysis /></el-icon></div>
        <div>
          <div class="metric-value">{{ statusCount.ANALYZING }}</div>
          <div class="metric-label">分析中</div>
          <div class="metric-hint">等待匹配与拆解</div>
        </div>
      </div>
      <div class="metric-card">
        <div class="metric-icon metric-green"><el-icon><CircleCheck /></el-icon></div>
        <div>
          <div class="metric-value">{{ statusCount.DELIVERED }}</div>
          <div class="metric-label">已交付</div>
          <div class="metric-hint">总结和清单已沉淀</div>
        </div>
      </div>
      <div class="metric-card">
        <div class="metric-icon metric-red"><el-icon><WarningFilled /></el-icon></div>
        <div>
          <div class="metric-value">{{ highRiskCount }}</div>
          <div class="metric-label">高风险</div>
          <div class="metric-hint">含支付/权限/数据库等标签</div>
        </div>
      </div>
    </div>

    <TaskFilterBar :model="query" @search="reload" @reset="reset" />

    <div class="section">
      <el-table v-if="!loading && !tasks.length" :data="[]" empty-text="暂无任务，请创建任务" />
      <TaskTable v-else v-loading="loading" :tasks="tasks" @detail="(id) => router.push(`/tasks/${id}`)" />
      <div class="pagination-row">
        <el-pagination
          v-model:current-page="query.pageNo"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="reload"
          @current-change="reload"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { CircleCheck, DataAnalysis, Document, Plus, WarningFilled } from '@element-plus/icons-vue'

import { fetchTasks } from '@/api/task'
import AppPageHeader from '@/components/common/AppPageHeader.vue'
import TaskFilterBar from '@/components/task/TaskFilterBar.vue'
import TaskTable from '@/components/task/TaskTable.vue'
import type { Task, TaskQuery, TaskStatus } from '@/types/task'

const router = useRouter()
const loading = ref(false)
const tasks = ref<Task[]>([])
const total = ref(0)
const query = reactive<TaskQuery>({ pageNo: 1, pageSize: 10 })
const statusCount = computed<Record<TaskStatus, number>>(() => ({
  DRAFT: tasks.value.filter((task) => task.status === 'DRAFT').length,
  ANALYZING: tasks.value.filter((task) => task.status === 'ANALYZING').length,
  EXECUTING: tasks.value.filter((task) => task.status === 'EXECUTING').length,
  TESTING: tasks.value.filter((task) => task.status === 'TESTING').length,
  DELIVERED: tasks.value.filter((task) => task.status === 'DELIVERED').length,
  ARCHIVED: tasks.value.filter((task) => task.status === 'ARCHIVED').length,
  CANCELED: tasks.value.filter((task) => task.status === 'CANCELED').length
}))
const highRiskCount = computed(
  () => tasks.value.filter((task) => task.riskTags.some((tag) => ['payment', 'permission', 'database', 'auth'].includes(tag))).length
)

async function reload() {
  loading.value = true
  try {
    const result = await fetchTasks(query)
    tasks.value = result.records
    total.value = result.total
  } finally {
    loading.value = false
  }
}

function reset() {
  query.pageNo = 1
  query.status = undefined
  query.taskType = undefined
  query.complexity = undefined
  query.riskTag = undefined
  reload()
}

onMounted(reload)
</script>

<style scoped>
.pagination-row {
  display: flex;
  justify-content: flex-end;
  padding: 14px 16px;
}
</style>
