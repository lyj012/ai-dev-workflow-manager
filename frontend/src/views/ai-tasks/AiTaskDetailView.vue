<template>
  <div>
    <AppPageHeader title="AI 任务详情" description="查看异步任务状态、执行日志、失败信息和分析结果。">
      <template #actions>
        <el-button :icon="Refresh" :loading="loading" @click="loadAll">刷新任务</el-button>
        <el-button :disabled="task?.status !== 'FAILED'" :loading="retrying" @click="handleRetry">重试失败任务</el-button>
        <el-button :icon="ArrowLeft" @click="router.push('/ai-tasks')">返回列表</el-button>
      </template>
    </AppPageHeader>

    <div class="detail-grid">
      <div class="section">
        <div class="section-header"><div class="section-title">任务基础信息</div></div>
        <div class="section-body">
          <el-skeleton v-if="loading && !task" :rows="6" animated />
          <el-descriptions v-else :column="1" border>
            <el-descriptions-item label="任务标题">{{ task?.title || '-' }}</el-descriptions-item>
            <el-descriptions-item label="当前状态"><StatusTag :value="task?.status" kind="aiTask" /></el-descriptions-item>
            <el-descriptions-item label="任务类型">{{ labelOf(aiTaskTypeOptions, task?.taskType ?? null) }}</el-descriptions-item>
            <el-descriptions-item label="重试次数">{{ task?.retryCount ?? 0 }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ formatTime(task?.createdAt) }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ formatTime(task?.updatedAt) }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </div>

      <div class="section">
        <div class="section-header"><div class="section-title">需求输入</div></div>
        <div class="section-body">
          <pre class="plain-block compact-block">{{ task?.requirement || '暂无需求描述。' }}</pre>
        </div>
      </div>
    </div>

    <el-alert
      v-if="task?.status === 'FAILED'"
      class="gap-alert"
      type="error"
      show-icon
      :closable="false"
      :title="task.errorMessage || '任务执行失败'"
    />

    <div class="detail-grid">
      <div class="section">
        <div class="section-header"><div class="section-title">执行日志</div></div>
        <div class="section-body">
          <el-timeline v-if="logs.length">
            <el-timeline-item
              v-for="log in logs"
              :key="log.id"
              :timestamp="formatTime(log.createdAt)"
              :type="log.logLevel === 'ERROR' ? 'danger' : log.logLevel === 'WARN' ? 'warning' : 'primary'"
            >
              <div class="log-line">
                <span :class="['log-level', `log-level--${log.logLevel.toLowerCase()}`]">{{ log.logLevel }}</span>
                <strong>{{ log.logNode }}</strong>
                <span>{{ log.message }}</span>
              </div>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无执行日志" :image-size="72" />
        </div>
      </div>

      <div class="section">
        <div class="section-header"><div class="section-title">分析结果</div></div>
        <div class="section-body">
          <el-empty v-if="!result" description="暂无分析结果" :image-size="72" />
          <el-descriptions v-else :column="1" border>
            <el-descriptions-item label="需求摘要">{{ result.summary }}</el-descriptions-item>
            <el-descriptions-item label="风险点"><pre class="result-text">{{ result.riskPoints }}</pre></el-descriptions-item>
            <el-descriptions-item label="建议步骤"><pre class="result-text">{{ result.suggestedSteps }}</pre></el-descriptions-item>
            <el-descriptions-item label="验证建议"><pre class="result-text">{{ result.testSuggestions }}</pre></el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
    </div>
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
import type { AiTask, AiTaskLog, AiTaskResult } from '@/types/ai-task'
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
.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.gap-alert {
  margin-bottom: 16px;
}

.log-line {
  display: flex;
  gap: 8px;
  align-items: center;
  color: #25324a;
}

.log-level {
  min-width: 44px;
  font-weight: 700;
}

.log-level--error {
  color: #e5484d;
}

.log-level--warn {
  color: #d97706;
}

.log-level--info {
  color: var(--app-primary);
}

.result-text {
  margin: 0;
  white-space: pre-wrap;
  font-family: inherit;
}

@media (max-width: 1440px) {
  .detail-grid {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
