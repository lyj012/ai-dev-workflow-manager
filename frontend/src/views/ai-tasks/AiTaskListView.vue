<template>
  <div>
    <AppPageHeader
      title="AI 异步任务"
      description="提交需求并跟踪异步分析状态、执行日志、结构化结果与失败重试。"
    >
      <template #actions>
        <el-button :icon="Refresh" :loading="loading" @click="reload">刷新</el-button>
      </template>
    </AppPageHeader>

    <div class="metrics-grid">
      <div class="metric-card">
        <div class="metric-icon metric-blue"><el-icon><Cpu /></el-icon></div>
        <div>
          <div class="metric-value">{{ total }}</div>
          <div class="metric-label">全部任务</div>
          <div class="metric-hint">保留现有异步任务字段</div>
        </div>
      </div>
      <div class="metric-card">
        <div class="metric-icon metric-indigo"><el-icon><Clock /></el-icon></div>
        <div>
          <div class="metric-value">{{ statusCount.ANALYZING }}</div>
          <div class="metric-label">分析中</div>
          <div class="metric-hint">详情页自动轮询状态</div>
        </div>
      </div>
      <div class="metric-card">
        <div class="metric-icon metric-green"><el-icon><CircleCheck /></el-icon></div>
        <div>
          <div class="metric-value">{{ statusCount.SUCCESS }}</div>
          <div class="metric-label">执行成功</div>
          <div class="metric-hint">分析结果可查看</div>
        </div>
      </div>
      <div class="metric-card">
        <div class="metric-icon metric-red"><el-icon><WarningFilled /></el-icon></div>
        <div>
          <div class="metric-value">{{ statusCount.FAILED }}</div>
          <div class="metric-label">执行失败</div>
          <div class="metric-hint">支持失败重试</div>
        </div>
      </div>
    </div>

    <div class="ai-task-layout">
      <section class="section create-panel">
        <div class="section-header">
          <div>
            <div class="section-title">创建 AI 任务</div>
            <div class="section-subtitle">输入需求后立即创建异步分析任务</div>
          </div>
        </div>
        <div class="section-body">
          <el-form :model="form" label-position="top" class="ai-task-form">
            <el-form-item label="任务标题" required>
              <el-input v-model="form.title" maxlength="200" show-word-limit placeholder="输入要分析的任务标题" />
            </el-form-item>
            <el-form-item label="任务类型">
              <el-select v-model="form.taskType" clearable placeholder="选择类型">
                <el-option v-for="item in aiTaskTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="需求描述" required>
              <el-input
                v-model="form.requirement"
                type="textarea"
                :rows="6"
                maxlength="2000"
                show-word-limit
                placeholder="输入需求、问题背景或需要 AI 分析的内容"
              />
            </el-form-item>
            <el-form-item label="备注">
              <el-input v-model="form.remark" maxlength="300" placeholder="可选，记录上下文或来源" />
            </el-form-item>
            <div class="form-actions">
              <el-button type="primary" :icon="Plus" :loading="creating" @click="submit">提交任务</el-button>
              <el-button @click="resetForm">清空</el-button>
            </div>
          </el-form>
        </div>
      </section>

      <section class="section task-panel">
        <div class="filter-row">
          <div class="status-tabs">
            <button :class="{ active: !query.status }" @click="setStatus()">
              全部<span>{{ total }}</span>
            </button>
            <button
              v-for="item in aiTaskStatusOptions"
              :key="item.value"
              :class="{ active: query.status === item.value }"
              @click="setStatus(item.value)"
            >
              {{ item.label }}<span>{{ statusCount[item.value] }}</span>
            </button>
          </div>
          <div class="search-box">
            <el-icon><Search /></el-icon>
            <input v-model="keyword" placeholder="搜索标题或任务 ID" aria-label="搜索任务" />
          </div>
        </div>

        <el-table
          v-loading="loading"
          :data="filteredTasks"
          row-key="id"
          empty-text="暂无 AI 任务"
          @row-click="(row: AiTask) => router.push(`/ai-tasks/${row.id}`)"
        >
          <el-table-column label="任务" min-width="280">
            <template #default="{ row }">
              <div class="task-title-cell">
                <span :class="['task-symbol', `task-symbol--${row.status.toLowerCase()}`]">
                  <el-icon><component :is="statusIcon(row.status)" /></el-icon>
                </span>
                <div>
                  <strong>{{ row.title }}</strong>
                  <span>#{{ row.id }} · {{ row.remark || row.requirement }}</span>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="类型" width="132">
            <template #default="{ row }">
              <span class="type-chip">{{ labelOf(aiTaskTypeOptions, row.taskType ?? null) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="130">
            <template #default="{ row }"><StatusTag :value="row.status" kind="aiTask" /></template>
          </el-table-column>
          <el-table-column label="执行信息" min-width="150">
            <template #default="{ row }">
              <div class="execution-cell">
                <span>重试 {{ row.retryCount }} 次</span>
                <small>{{ executionHint(row) }}</small>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="更新时间" width="150">
            <template #default="{ row }">
              <span class="time-cell">{{ formatTime(row.updatedAt) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="170" fixed="right" align="right">
            <template #default="{ row }">
              <div class="row-actions" @click.stop>
                <el-button v-if="row.status === 'FAILED'" link type="danger" @click="handleRetry(row.id)">重试</el-button>
                <el-button link type="primary" @click="router.push(`/ai-tasks/${row.id}`)">查看</el-button>
              </div>
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
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { CircleCheck, Clock, Cpu, Document, Plus, Refresh, Search, WarningFilled } from '@element-plus/icons-vue'

import { createAiTask, fetchAiTasks, retryAiTask } from '@/api/ai-task'
import AppPageHeader from '@/components/common/AppPageHeader.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import { aiTaskStatusOptions, aiTaskTypeOptions, labelOf } from '@/constants/options'
import type { AiTask, AiTaskQuery, AiTaskStatus, CreateAiTaskPayload } from '@/types/ai-task'
import { formatTime } from '@/utils/format'

const router = useRouter()
const loading = ref(false)
const creating = ref(false)
const keyword = ref('')
const tasks = ref<AiTask[]>([])
const total = ref(0)
const query = reactive<AiTaskQuery>({ pageNo: 1, pageSize: 10 })
const form = reactive<CreateAiTaskPayload>({
  title: '',
  requirement: '',
  taskType: 'requirement_analysis',
  remark: ''
})

const statusCount = computed<Record<AiTaskStatus, number>>(() => ({
  CREATED: tasks.value.filter((task) => task.status === 'CREATED').length,
  ANALYZING: tasks.value.filter((task) => task.status === 'ANALYZING').length,
  SUCCESS: tasks.value.filter((task) => task.status === 'SUCCESS').length,
  FAILED: tasks.value.filter((task) => task.status === 'FAILED').length
}))

const filteredTasks = computed(() => {
  const text = keyword.value.trim().toLowerCase()
  if (!text) return tasks.value
  return tasks.value.filter((task) => task.title.toLowerCase().includes(text) || String(task.id).includes(text))
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

function setStatus(status?: AiTaskStatus) {
  query.status = status
  query.pageNo = 1
  void reload()
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

function statusIcon(status: AiTaskStatus) {
  if (status === 'SUCCESS') return CircleCheck
  if (status === 'FAILED') return WarningFilled
  if (status === 'ANALYZING') return Clock
  return Document
}

function executionHint(task: AiTask) {
  if (task.status === 'SUCCESS') return '结果已保存'
  if (task.status === 'FAILED') return task.errorMessage || '等待人工处理'
  if (task.status === 'ANALYZING') return '执行器处理中'
  return '等待调度'
}

onMounted(reload)
</script>

<style scoped>
.ai-task-layout {
  display: grid;
  grid-template-columns: 360px minmax(0, 1fr);
  gap: 18px;
  align-items: start;
}

.section-subtitle {
  margin-top: 4px;
  color: var(--app-muted);
  font-size: 12px;
}

.create-panel {
  position: sticky;
  top: 88px;
}

.ai-task-form :deep(.el-select) {
  width: 100%;
}

.form-actions {
  display: flex;
  gap: 8px;
}

.task-panel {
  min-width: 0;
}

.filter-row {
  display: flex;
  gap: 14px;
  align-items: center;
  justify-content: space-between;
  padding: 16px 18px;
  border-bottom: 1px solid var(--app-border-soft);
  background: #ffffff;
}

.status-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.status-tabs button {
  display: inline-flex;
  gap: 6px;
  align-items: center;
  height: 34px;
  padding: 0 12px;
  border: 1px solid var(--app-border-soft);
  border-radius: 999px;
  background: #ffffff;
  color: var(--app-muted);
  cursor: pointer;
  font-weight: 800;
}

.status-tabs button.active {
  border-color: #dbeafe;
  background: #eff6ff;
  color: var(--app-primary);
}

.status-tabs span {
  color: var(--app-subtle);
  font-size: 12px;
}

.search-box {
  display: flex;
  align-items: center;
  min-width: 230px;
  height: 36px;
  padding: 0 12px;
  border: 1px solid var(--app-border-soft);
  border-radius: 8px;
  background: #f8fafc;
  color: var(--app-muted);
}

.search-box input {
  width: 100%;
  border: 0;
  outline: 0;
  background: transparent;
  color: var(--app-text);
}

.task-title-cell {
  display: flex;
  gap: 12px;
  align-items: center;
}

.task-title-cell strong {
  display: block;
  max-width: 460px;
  overflow: hidden;
  color: var(--app-text);
  font-size: 14px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.task-title-cell span:last-child {
  display: block;
  max-width: 520px;
  overflow: hidden;
  margin-top: 3px;
  color: var(--app-muted);
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.task-symbol {
  display: inline-flex;
  width: 36px;
  height: 36px;
  flex: 0 0 36px;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
}

.task-symbol--success {
  background: var(--app-success-soft);
  color: var(--app-success);
}

.task-symbol--failed {
  background: var(--app-danger-soft);
  color: var(--app-danger);
}

.task-symbol--analyzing {
  background: var(--app-warning-soft);
  color: var(--app-warning);
}

.task-symbol--created {
  background: var(--app-info-soft);
  color: var(--app-info);
}

.type-chip {
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  padding: 0 10px;
  border-radius: 999px;
  background: #f1f5f9;
  color: #475569;
  font-size: 12px;
  font-weight: 800;
}

.execution-cell span {
  display: block;
  color: var(--app-text);
  font-weight: 800;
}

.execution-cell small {
  display: block;
  max-width: 180px;
  overflow: hidden;
  margin-top: 3px;
  color: var(--app-muted);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.time-cell {
  color: var(--app-muted);
  font-size: 12px;
  font-weight: 700;
}

.row-actions {
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 1280px) {
  .ai-task-layout {
    grid-template-columns: minmax(0, 1fr);
  }

  .create-panel {
    position: static;
  }
}

@media (max-width: 900px) {
  .filter-row {
    flex-direction: column;
    align-items: stretch;
  }

  .search-box {
    min-width: 0;
  }
}
</style>
