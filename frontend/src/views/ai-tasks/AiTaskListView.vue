<template>
  <div>
    <AppPageHeader title="AI 任务" description="提交需求并跟踪异步分析状态、执行日志和结果。">
      <template #actions>
        <el-button :icon="Refresh" @click="reload">刷新列表</el-button>
      </template>
    </AppPageHeader>

    <div class="section">
      <div class="section-header">
        <div class="section-title">创建 AI 任务</div>
      </div>
      <div class="section-body">
        <el-form :model="form" label-width="90px" class="ai-task-form">
          <el-form-item label="任务标题" required>
            <el-input v-model="form.title" maxlength="200" show-word-limit placeholder="输入要分析的任务标题" />
          </el-form-item>
          <el-form-item label="任务类型">
            <el-select v-model="form.taskType" clearable placeholder="选择类型" style="width: 220px">
              <el-option v-for="item in aiTaskTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="需求描述" required>
            <el-input
              v-model="form.requirement"
              type="textarea"
              :rows="4"
              maxlength="2000"
              show-word-limit
              placeholder="输入需求、问题背景或需要 AI 分析的内容"
            />
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="form.remark" maxlength="300" placeholder="可选，记录上下文或来源" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Plus" :loading="creating" @click="submit">提交任务</el-button>
            <el-button @click="resetForm">清空</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>

    <div class="section">
      <div class="section-header">
        <div class="section-title">任务列表</div>
        <el-select v-model="query.status" clearable placeholder="筛选状态" style="width: 160px" @change="reload">
          <el-option v-for="item in aiTaskStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </div>
      <el-table v-loading="loading" :data="tasks" row-key="id" empty-text="暂无 AI 任务">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="任务标题" min-width="220" show-overflow-tooltip />
        <el-table-column label="状态" width="120">
          <template #default="{ row }"><StatusTag :value="row.status" kind="aiTask" /></template>
        </el-table-column>
        <el-table-column label="重试次数" width="100">
          <template #default="{ row }">{{ row.retryCount }}</template>
        </el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="更新时间" width="180">
          <template #default="{ row }">{{ formatTime(row.updatedAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="190" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="router.push(`/ai-tasks/${row.id}`)">查看详情</el-button>
            <el-button link type="primary" :disabled="row.status !== 'FAILED'" @click="handleRetry(row.id)">重试</el-button>
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
import { ElMessage } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'

import { createAiTask, fetchAiTasks, retryAiTask } from '@/api/ai-task'
import AppPageHeader from '@/components/common/AppPageHeader.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import { aiTaskStatusOptions, aiTaskTypeOptions } from '@/constants/options'
import type { AiTask, AiTaskQuery, CreateAiTaskPayload } from '@/types/ai-task'
import { formatTime } from '@/utils/format'

const router = useRouter()
const loading = ref(false)
const creating = ref(false)
const tasks = ref<AiTask[]>([])
const total = ref(0)
const query = reactive<AiTaskQuery>({ pageNo: 1, pageSize: 10 })
const form = reactive<CreateAiTaskPayload>({
  title: '',
  requirement: '',
  taskType: 'requirement_analysis',
  remark: ''
})

async function reload() {
  loading.value = true
  try {
    const result = await fetchAiTasks(query)
    tasks.value = result.records
    total.value = result.total
  } finally {
    loading.value = false
  }
}

async function submit() {
  if (!form.title.trim()) {
    ElMessage.warning('请输入任务标题')
    return
  }
  if (!form.requirement.trim()) {
    ElMessage.warning('请输入需求描述')
    return
  }
  creating.value = true
  try {
    const task = await createAiTask({
      title: form.title.trim(),
      requirement: form.requirement.trim(),
      taskType: form.taskType,
      remark: form.remark?.trim()
    })
    ElMessage.success('AI 任务已提交')
    resetForm()
    await router.push(`/ai-tasks/${task.id}`)
  } finally {
    creating.value = false
  }
}

async function handleRetry(taskId: number) {
  await retryAiTask(taskId)
  ElMessage.success('已提交重试')
  await reload()
}

function resetForm() {
  form.title = ''
  form.requirement = ''
  form.taskType = 'requirement_analysis'
  form.remark = ''
}

onMounted(reload)
</script>

<style scoped>
.ai-task-form {
  max-width: 900px;
}

.pagination-row {
  display: flex;
  justify-content: flex-end;
  padding: 14px 16px;
}
</style>
