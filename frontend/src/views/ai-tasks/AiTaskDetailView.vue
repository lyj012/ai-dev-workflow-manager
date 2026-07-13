<template>
  <div>
    <AppPageHeader title="AI 任务详情" description="查看异步任务状态、执行日志、失败信息和分析结果。">
      <template #actions>
        <el-button :icon="Refresh" :loading="loading" @click="loadAll">刷新任务</el-button>
        <el-button type="primary" :disabled="task?.status !== 'FAILED'" :loading="retrying" @click="handleRetry">
          重试失败任务
        </el-button>
        <el-button :icon="ArrowLeft" @click="router.push('/ai-tasks')">返回列表</el-button>
      </template>
    </AppPageHeader>

    <section class="section hero-panel">
      <div class="section-body">
        <el-skeleton v-if="loading && !task" :rows="4" animated />
        <div v-else class="hero-content">
          <div class="hero-main">
            <div class="hero-meta">#{{ task?.id }} · {{ labelOf(aiTaskTypeOptions, task?.taskType ?? null) }}</div>
            <h2>{{ task?.title || '-' }}</h2>
            <p>{{ task?.remark || task?.requirement || '暂无任务描述' }}</p>
          </div>
          <div class="hero-status">
            <StatusTag :value="task?.status" kind="aiTask" />
            <div>重试 {{ task?.retryCount ?? 0 }} 次</div>
            <small>更新于 {{ formatTime(task?.updatedAt) }}</small>
          </div>
        </div>
      </div>
    </section>

    <div class="detail-grid">
      <section class="section">
        <div class="section-header"><div class="section-title">状态流转</div></div>
        <div class="section-body">
          <el-steps :active="activeStep" finish-status="success" process-status="process" align-center>
            <el-step v-for="item in statusFlow" :key="item.value" :title="item.label" :description="item.description" />
          </el-steps>
          <el-alert
            v-if="task?.status === 'FAILED'"
            class="gap-alert"
            type="error"
            show-icon
            :closable="false"
            :title="task.errorMessage || '任务执行失败'"
          />
        </div>
      </section>

      <section class="section">
        <div class="section-header"><div class="section-title">任务基础信息</div></div>
        <div class="section-body">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="任务标题">{{ task?.title || '-' }}</el-descriptions-item>
            <el-descriptions-item label="当前状态"><StatusTag :value="task?.status" kind="aiTask" /></el-descriptions-item>
            <el-descriptions-item label="任务类型">{{ labelOf(aiTaskTypeOptions, task?.taskType ?? null) }}</el-descriptions-item>
            <el-descriptions-item label="重试次数">{{ task?.retryCount ?? 0 }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ formatTime(task?.createdAt) }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ formatTime(task?.updatedAt) }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </section>
    </div>

    <div class="detail-grid detail-grid--wide">
      <section class="section">
        <div class="section-header"><div class="section-title">需求输入</div></div>
        <div class="section-body">
          <pre class="plain-block compact-block">{{ task?.requirement || '暂无需求描述。' }}</pre>
        </div>
      </section>

      <section class="section">
        <div class="section-header"><div class="section-title">执行日志时间线</div></div>
        <div class="section-body">
          <el-timeline v-if="logs.length" class="log-timeline">
            <el-timeline-item
              v-for="log in logs"
              :key="log.id"
              :timestamp="formatTime(log.createdAt)"
              :type="log.logLevel === 'ERROR' ? 'danger' : log.logLevel === 'WARN' ? 'warning' : 'primary'"
            >
              <div class="log-card">
                <div class="log-card__head">
                  <span :class="['log-level', `log-level--${log.logLevel.toLowerCase()}`]">{{ log.logLevel }}</span>
                  <strong>{{ log.logNode }}</strong>
                </div>
                <p>{{ log.message }}</p>
              </div>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无执行日志" :image-size="72" />
        </div>
      </section>
    </div>

    <section class="section">
      <div class="section-header">
        <div>
          <div class="section-title">分析结果</div>
          <div class="section-subtitle">结构化展示 summary、riskPoints、suggestedSteps 和 testSuggestions</div>
        </div>
      </div>
      <div class="section-body">
        <el-empty v-if="!result" description="暂无分析结果" :image-size="72" />
        <div v-else class="result-grid">
          <article class="result-card">
            <span>需求摘要</span>
            <p>{{ result.summary }}</p>
          </article>
          <article class="result-card result-card--danger">
            <span>风险点</span>
            <pre>{{ result.riskPoints }}</pre>
          </article>
          <article class="result-card">
            <span>建议步骤</span>
            <pre>{{ result.suggestedSteps }}</pre>
          </article>
          <article class="result-card">
            <span>验证建议</span>
            <pre>{{ result.testSuggestions }}</pre>
          </article>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Refresh } from '@element-plus/icons-vue'

import { fetchAiTaskDetail, fetchAiTaskLogs, fetchAiTaskResult, retryAiTask } from '@/api/ai-task'
import AppPageHeader from '@/components/common/AppPageHeader.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import { aiTaskTypeOptions, labelOf } from '@/constants/options'
import type { AiTask, AiTaskLog, AiTaskResult, AiTaskStatus } from '@/types/ai-task'
import { formatTime } from '@/utils/format'

const props = defineProps<{ taskId: string }>()
const router = useRouter()
const id = computed(() => Number(props.taskId))
const loading = ref(false)
const retrying = ref(false)
const task = ref<AiTask>()
const logs = ref<AiTaskLog[]>([])
const result = ref<AiTaskResult | null>(null)
const pollingTimer = ref<number>()
let disposed = false

const statusFlow: Array<{ value: AiTaskStatus; label: string; description: string }> = [
  { value: 'CREATED', label: '已创建', description: '等待调度' },
  { value: 'ANALYZING', label: '分析中', description: '执行器处理中' },
  { value: 'SUCCESS', label: '执行成功', description: '结果已保存' },
  { value: 'FAILED', label: '执行失败', description: '可重试' }
]

const activeStep = computed(() => {
  if (task.value?.status === 'FAILED') return 3
  return Math.max(0, statusFlow.findIndex((item) => item.value === task.value?.status))
})

function shouldPoll(status?: AiTask['status']) {
  return status === 'CREATED' || status === 'ANALYZING'
}

function stopPolling() {
  if (pollingTimer.value) {
    window.clearTimeout(pollingTimer.value)
    pollingTimer.value = undefined
  }
}

function schedulePolling() {
  stopPolling()
  if (disposed || !shouldPoll(task.value?.status)) return
  pollingTimer.value = window.setTimeout(() => {
    void loadAll(false)
  }, 1500)
}

async function loadAll(showLoading = true) {
  if (showLoading) {
    loading.value = true
  }
  try {
    const [detail, detailLogs, detailResult] = await Promise.all([
      fetchAiTaskDetail(id.value),
      fetchAiTaskLogs(id.value),
      fetchAiTaskResult(id.value)
    ])
    task.value = detail
    logs.value = detailLogs
    result.value = detailResult
  } finally {
    if (showLoading) {
      loading.value = false
    }
    schedulePolling()
  }
}

async function handleRetry() {
  if (!task.value || task.value.status !== 'FAILED') return
  retrying.value = true
  try {
    task.value = await retryAiTask(task.value.id)
    ElMessage.success('已提交重试')
    await loadAll()
  } finally {
    retrying.value = false
  }
}

onMounted(() => {
  void loadAll()
})

onUnmounted(() => {
  disposed = true
  stopPolling()
})
</script>

<style scoped>
.hero-panel {
  background: linear-gradient(135deg, #ffffff 0%, #f8fbff 100%);
}

.hero-content {
  display: flex;
  gap: 24px;
  align-items: center;
  justify-content: space-between;
}

.hero-main h2 {
  margin: 6px 0 8px;
  color: var(--app-text);
  font-size: 24px;
  line-height: 1.25;
}

.hero-main p {
  max-width: 860px;
  margin: 0;
  color: var(--app-muted);
}

.hero-meta {
  color: var(--app-primary);
  font-size: 12px;
  font-weight: 900;
  letter-spacing: 0.06em;
}

.hero-status {
  min-width: 180px;
  padding: 16px;
  border: 1px solid var(--app-border-soft);
  border-radius: var(--app-radius);
  background: #ffffff;
  text-align: right;
}

.hero-status > div {
  margin-top: 10px;
  color: var(--app-text);
  font-weight: 850;
}

.hero-status small {
  display: block;
  margin-top: 3px;
  color: var(--app-muted);
}

.detail-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(360px, 0.9fr);
  gap: 18px;
}

.detail-grid--wide {
  grid-template-columns: minmax(0, 0.9fr) minmax(0, 1.1fr);
}

.section-subtitle {
  margin-top: 4px;
  color: var(--app-muted);
  font-size: 12px;
}

.gap-alert {
  margin-top: 18px;
}

.log-timeline {
  padding-left: 2px;
}

.log-card {
  padding: 12px 14px;
  border: 1px solid var(--app-border-soft);
  border-radius: 8px;
  background: #f8fafc;
}

.log-card__head {
  display: flex;
  gap: 8px;
  align-items: center;
}

.log-card p {
  margin: 7px 0 0;
  color: var(--app-muted);
}

.log-level {
  min-width: 44px;
  font-size: 12px;
  font-weight: 900;
}

.log-level--error {
  color: var(--app-danger);
}

.log-level--warn {
  color: var(--app-warning);
}

.log-level--info {
  color: var(--app-primary);
}

.result-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.result-card {
  min-height: 154px;
  padding: 16px;
  border: 1px solid var(--app-border-soft);
  border-radius: var(--app-radius);
  background: #f8fafc;
}

.result-card--danger {
  background: var(--app-danger-soft);
}

.result-card span {
  display: block;
  margin-bottom: 8px;
  color: var(--app-text);
  font-weight: 900;
}

.result-card p,
.result-card pre {
  margin: 0;
  color: #334155;
  font-family: inherit;
  line-height: 1.7;
  white-space: pre-wrap;
}

@media (max-width: 1280px) {
  .detail-grid,
  .detail-grid--wide,
  .result-grid {
    grid-template-columns: minmax(0, 1fr);
  }

  .hero-content {
    align-items: flex-start;
    flex-direction: column;
  }

  .hero-status {
    width: 100%;
    text-align: left;
  }
}
</style>
