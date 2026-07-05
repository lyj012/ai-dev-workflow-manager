<template>
  <div>
    <AppPageHeader title="任务列表" description="查看、筛选并进入 AI 开发任务详情。">
      <template #actions>
        <el-button type="primary" :icon="Plus" @click="router.push('/tasks/create')">创建任务</el-button>
      </template>
    </AppPageHeader>

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
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'

import { fetchTasks } from '@/api/task'
import AppPageHeader from '@/components/common/AppPageHeader.vue'
import TaskFilterBar from '@/components/task/TaskFilterBar.vue'
import TaskTable from '@/components/task/TaskTable.vue'
import type { Task, TaskQuery } from '@/types/task'

const router = useRouter()
const loading = ref(false)
const tasks = ref<Task[]>([])
const total = ref(0)
const query = reactive<TaskQuery>({ pageNo: 1, pageSize: 10 })

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
