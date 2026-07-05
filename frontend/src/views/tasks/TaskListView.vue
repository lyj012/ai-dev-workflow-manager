<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">任务列表</h1>
      <el-button type="primary" :icon="Plus" @click="router.push('/tasks/create')">创建任务</el-button>
    </div>

    <div class="section">
      <div class="section-body">
        <el-form :inline="true" :model="query" class="toolbar">
          <el-form-item label="任务状态">
            <el-select v-model="query.status" clearable placeholder="全部状态" style="width: 150px">
              <el-option v-for="item in taskStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="任务类型">
            <el-select v-model="query.taskType" clearable placeholder="全部类型" style="width: 150px">
              <el-option v-for="item in taskTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="复杂度">
            <el-select v-model="query.complexity" clearable placeholder="全部复杂度" style="width: 150px">
              <el-option v-for="item in complexityOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="风险标签">
            <el-select v-model="query.riskTag" clearable placeholder="全部风险" style="width: 160px">
              <el-option v-for="item in riskTagOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="reload">查询</el-button>
            <el-button :icon="Refresh" @click="reset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>

    <div class="section">
      <el-table v-loading="loading" :data="tasks" row-key="id">
        <el-table-column prop="title" label="任务标题" min-width="220" show-overflow-tooltip />
        <el-table-column label="任务类型" width="120">
          <template #default="{ row }">{{ labelOf(taskTypeOptions, row.taskType) }}</template>
        </el-table-column>
        <el-table-column label="复杂度" width="110">
          <template #default="{ row }">
            <el-tag :type="complexityType(row.complexity)" effect="plain">
              {{ labelOf(complexityOptions, row.complexity) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="风险标签" min-width="180">
          <template #default="{ row }">
            <el-space wrap>
              <el-tag v-for="tag in row.riskTags" :key="tag" type="warning" effect="plain">
                {{ labelOf(riskTagOptions, tag) }}
              </el-tag>
              <span v-if="!row.riskTags?.length" class="muted">无</span>
            </el-space>
          </template>
        </el-table-column>
        <el-table-column label="当前状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ labelOf(taskStatusOptions, row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="测试清单" width="110">
          <template #default="{ row }">
            <el-tag :type="row.testChecklistGenerated ? 'success' : 'info'" effect="plain">
              {{ row.testChecklistGenerated ? '已生成' : '未生成' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="更新时间" width="180">
          <template #default="{ row }">{{ formatTime(row.updatedAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="router.push(`/tasks/${row.id}`)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
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
import { Plus, Refresh, Search } from '@element-plus/icons-vue'

import { fetchTasks } from '@/api/task'
import type { Task } from '@/types/task'
import {
  complexityOptions,
  labelOf,
  riskTagOptions,
  taskStatusOptions,
  taskTypeOptions
} from '@/utils/options'

const router = useRouter()
const loading = ref(false)
const tasks = ref<Task[]>([])
const total = ref(0)
const query = reactive({
  pageNo: 1,
  pageSize: 10,
  status: undefined,
  taskType: undefined,
  complexity: undefined,
  riskTag: undefined
})

function statusType(status: string) {
  if (status === 'DELIVERED') return 'success'
  if (status === 'CANCELED' || status === 'ARCHIVED') return 'info'
  if (status === 'TESTING') return 'warning'
  return 'primary'
}

function complexityType(complexity: string) {
  if (complexity === 'COMPLEX') return 'danger'
  if (complexity === 'MEDIUM') return 'warning'
  return 'success'
}

function formatTime(value?: string) {
  return value ? value.replace('T', ' ') : '-'
}

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
