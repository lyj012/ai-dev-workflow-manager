<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">任务详情</h1>
      <div class="page-actions">
        <el-button :icon="Refresh" @click="loadDetail">刷新详情</el-button>
        <el-button :icon="ArrowLeft" @click="router.push('/tasks')">返回列表</el-button>
      </div>
    </div>

    <el-alert
      v-if="apiGapVisible"
      class="gap-alert"
      type="warning"
      :closable="false"
      show-icon
      title="当前后端尚未完整实现匹配、阶段推进、Prompt、交付记录接口；前端入口已按文档预留，接口补齐后即可对接。"
    />

    <div class="detail-grid">
      <div>
        <div class="section">
          <div class="section-header">
            <div class="section-title">基础信息</div>
            <el-tag v-if="task" :type="statusType(task.status)">
              {{ labelOf(taskStatusOptions, task.status) }}
            </el-tag>
          </div>
          <div class="section-body">
            <el-skeleton v-if="loading" :rows="6" animated />
            <el-descriptions v-else-if="task" :column="2" border>
              <el-descriptions-item label="标题" :span="2">{{ task.title }}</el-descriptions-item>
              <el-descriptions-item label="描述" :span="2">
                <span class="description-text">{{ task.description || '-' }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="任务类型">
                {{ labelOf(taskTypeOptions, task.taskType) }}
              </el-descriptions-item>
              <el-descriptions-item label="复杂度">
                {{ labelOf(complexityOptions, task.complexity) }}
              </el-descriptions-item>
              <el-descriptions-item label="风险标签" :span="2">
                <el-space wrap>
                  <el-tag v-for="tag in task.riskTags" :key="tag" type="warning" effect="plain">
                    {{ labelOf(riskTagOptions, tag) }}
                  </el-tag>
                  <span v-if="!task.riskTags?.length" class="muted">无</span>
                </el-space>
              </el-descriptions-item>
              <el-descriptions-item label="命中模板">
                {{ task.matchedTemplateId ?? '未绑定' }}
              </el-descriptions-item>
              <el-descriptions-item label="测试清单">
                {{ task.testChecklistGenerated ? '已生成' : '未生成' }}
              </el-descriptions-item>
              <el-descriptions-item label="更新时间" :span="2">
                {{ formatTime(task.updatedAt) }}
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </div>

        <div class="section">
          <div class="section-header">
            <div class="section-title">阶段列表</div>
            <el-button type="primary" plain :loading="stageInitLoading" @click="handleInitStages">
              初始化阶段
            </el-button>
          </div>
          <el-table :data="stages" row-key="id" empty-text="暂无阶段，请先匹配 workflow 并初始化阶段">
            <el-table-column prop="stageOrder" label="顺序" width="72" />
            <el-table-column prop="stageName" label="阶段名称" min-width="140" />
            <el-table-column prop="stageKey" label="阶段标识" min-width="130" />
            <el-table-column label="状态" width="110">
              <template #default="{ row }">
                <el-tag :type="stageStatusType(row.status)" effect="plain">
                  {{ labelOf(stageStatusOptions, row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="开始时间" width="170">
              <template #default="{ row }">{{ formatTime(row.startedAt) }}</template>
            </el-table-column>
            <el-table-column label="完成时间" width="170">
              <template #default="{ row }">{{ formatTime(row.completedAt) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="310" fixed="right">
              <template #default="{ row }">
                <el-button v-if="row.status === 'PENDING'" link type="primary" @click="runStageAction(row, 'start')">
                  开始
                </el-button>
                <el-button v-if="row.status === 'RUNNING'" link type="success" @click="runStageAction(row, 'complete')">
                  完成
                </el-button>
                <el-button v-if="row.status === 'PENDING'" link type="warning" @click="runStageAction(row, 'skip')">
                  跳过
                </el-button>
                <el-button v-if="row.status === 'RUNNING'" link type="danger" @click="runStageAction(row, 'fail')">
                  失败
                </el-button>
                <el-button link type="primary" @click="selectStage(row)">生成 Prompt</el-button>
                <el-button link type="primary" @click="openOutputDialog(row)">回填输出</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div class="section">
          <div class="section-header">
            <div class="section-title">Prompt 预览</div>
            <div class="page-actions">
              <el-select v-model="selectedStageId" placeholder="选择阶段" style="width: 220px">
                <el-option
                  v-for="stage in stages"
                  :key="stage.id"
                  :label="`${stage.stageOrder}. ${stage.stageName}`"
                  :value="stage.id"
                />
              </el-select>
              <el-button type="primary" :loading="promptLoading" @click="handleGeneratePrompt">生成 Prompt</el-button>
              <el-button :icon="CopyDocument" @click="copyText(promptContent)">复制</el-button>
            </div>
          </div>
          <div class="section-body">
            <pre class="code-block">{{ promptContent || '选择阶段后生成 Prompt。' }}</pre>
          </div>
        </div>
      </div>

      <div>
        <div class="section">
          <div class="section-header">
            <div class="section-title">Workflow 匹配</div>
            <el-button type="primary" :loading="matchingLoading" @click="handleMatch">匹配 workflow</el-button>
          </div>
          <div class="section-body">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="模板名称">
                {{ matchResult?.matchedTemplateName ?? (task?.matchedTemplateId ? `模板 ID ${task.matchedTemplateId}` : '未匹配') }}
              </el-descriptions-item>
              <el-descriptions-item label="匹配分数">{{ matchResult?.score ?? '-' }}</el-descriptions-item>
              <el-descriptions-item label="自动绑定">
                {{ matchResult ? (matchResult.autoBound ? '是' : '否') : '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="匹配原因">{{ matchResult?.reason ?? '-' }}</el-descriptions-item>
            </el-descriptions>
            <el-table v-if="matchResult?.candidates?.length" :data="matchResult.candidates" class="mini-table">
              <el-table-column prop="templateName" label="候选模板" />
              <el-table-column prop="score" label="分数" width="80" />
            </el-table>
          </div>
        </div>

        <div class="section">
          <div class="section-header">
            <div class="section-title">阶段输出回填</div>
            <el-button :disabled="!selectedStage" @click="openOutputDialog(selectedStage)">编辑当前阶段</el-button>
          </div>
          <div class="section-body">
            <el-empty v-if="!selectedStage" description="请先在阶段列表选择阶段" :image-size="72" />
            <el-descriptions v-else :column="1" border>
              <el-descriptions-item label="当前阶段">
                {{ selectedStage.stageName }} / {{ labelOf(stageStatusOptions, selectedStage.status) }}
              </el-descriptions-item>
              <el-descriptions-item label="输出摘要">{{ outputForm.outputSummary || '-' }}</el-descriptions-item>
              <el-descriptions-item label="风险点">{{ outputForm.riskPoints || '-' }}</el-descriptions-item>
              <el-descriptions-item label="后续动作">{{ outputForm.nextActions || '-' }}</el-descriptions-item>
              <el-descriptions-item label="未验证范围">{{ outputForm.unverifiedScope || '-' }}</el-descriptions-item>
            </el-descriptions>
          </div>
        </div>

        <div class="section">
          <div class="section-header">
            <div class="section-title">测试清单和交付记录</div>
            <div class="page-actions">
              <el-button :loading="deliveryLoading" @click="handleGenerateChecklist">生成测试清单</el-button>
              <el-button type="primary" :loading="deliveryLoading" @click="handleGenerateSummary">生成交付总结</el-button>
            </div>
          </div>
          <div class="section-body">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="测试清单">{{ deliveryRecord.testChecklist || '-' }}</el-descriptions-item>
              <el-descriptions-item label="测试结果">{{ deliveryRecord.testResult || '-' }}</el-descriptions-item>
              <el-descriptions-item label="风险说明">{{ deliveryRecord.riskNotes || '-' }}</el-descriptions-item>
              <el-descriptions-item label="未验证范围">{{ deliveryRecord.unverifiedScope || '-' }}</el-descriptions-item>
              <el-descriptions-item label="交付总结">{{ deliveryRecord.summary || '-' }}</el-descriptions-item>
            </el-descriptions>
          </div>
        </div>

        <div class="section">
          <div class="section-header">
            <div class="section-title">Markdown 预览</div>
            <div class="page-actions">
              <el-button type="primary" :loading="markdownLoading" @click="handleExportMarkdown">导出 Markdown</el-button>
              <el-button :icon="CopyDocument" @click="copyText(markdownContent)">复制</el-button>
            </div>
          </div>
          <div class="section-body">
            <pre class="plain-block">{{ markdownContent || '点击导出 Markdown 后显示交付复盘记录。' }}</pre>
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="outputDialogVisible" title="阶段输出回填" width="620px">
      <el-form label-width="96px">
        <el-form-item label="输出摘要">
          <el-input v-model="outputForm.outputSummary" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="风险点">
          <el-input v-model="outputForm.riskPoints" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="后续动作">
          <el-input v-model="outputForm.nextActions" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="未验证范围">
          <el-input v-model="outputForm.unverifiedScope" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="outputDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="outputSaving" @click="saveOutput">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, CopyDocument, Refresh } from '@element-plus/icons-vue'

import { generateDeliverySummary, generateTestChecklist, exportMarkdown } from '@/api/delivery'
import { matchTemplate } from '@/api/matching'
import { generatePrompt } from '@/api/prompt'
import { initializeStages, saveStageOutput, startStage, completeStage, skipStage, failStage } from '@/api/stage'
import { fetchTaskDetail } from '@/api/task'
import type { DeliveryRecord } from '@/types/delivery'
import type { StageOutputPayload, WorkflowStage } from '@/types/stage'
import type { MatchTemplateResponse } from '@/types/template'
import type { TaskDetail } from '@/types/task'
import {
  complexityOptions,
  labelOf,
  riskTagOptions,
  stageStatusOptions,
  taskStatusOptions,
  taskTypeOptions
} from '@/utils/options'

const props = defineProps<{ taskId: string }>()
const router = useRouter()
const id = computed(() => Number(props.taskId))

const loading = ref(false)
const matchingLoading = ref(false)
const stageInitLoading = ref(false)
const promptLoading = ref(false)
const outputSaving = ref(false)
const deliveryLoading = ref(false)
const markdownLoading = ref(false)
const outputDialogVisible = ref(false)

const task = ref<TaskDetail>()
const stages = ref<WorkflowStage[]>([])
const selectedStageId = ref<number>()
const matchResult = ref<MatchTemplateResponse>()
const promptContent = ref('')
const markdownContent = ref('')
const deliveryRecord = reactive<DeliveryRecord>({})
const outputForm = reactive<StageOutputPayload>({
  outputSummary: '',
  riskPoints: '',
  nextActions: '',
  unverifiedScope: ''
})

const selectedStage = computed(() => stages.value.find((stage) => stage.id === selectedStageId.value))
const apiGapVisible = computed(() => true)

function statusType(status: string) {
  if (status === 'DELIVERED') return 'success'
  if (status === 'CANCELED' || status === 'ARCHIVED') return 'info'
  if (status === 'TESTING') return 'warning'
  return 'primary'
}

function stageStatusType(status: string) {
  if (status === 'COMPLETED') return 'success'
  if (status === 'FAILED') return 'danger'
  if (status === 'SKIPPED') return 'info'
  if (status === 'RUNNING') return 'warning'
  return 'primary'
}

function formatTime(value?: string | null) {
  return value ? value.replace('T', ' ') : '-'
}

async function loadDetail() {
  loading.value = true
  try {
    const detail = await fetchTaskDetail(id.value)
    task.value = detail
    const detailStages = (detail as TaskDetail & { stages?: WorkflowStage[] }).stages
    if (Array.isArray(detailStages)) {
      stages.value = detailStages
      selectedStageId.value = stages.value[0]?.id
    }
  } finally {
    loading.value = false
  }
}

async function handleMatch() {
  matchingLoading.value = true
  try {
    matchResult.value = await matchTemplate(id.value)
    ElMessage.success('Workflow 匹配完成')
    await loadDetail()
  } finally {
    matchingLoading.value = false
  }
}

async function handleInitStages() {
  stageInitLoading.value = true
  try {
    const result = await initializeStages(id.value)
    stages.value = result.stages
    selectedStageId.value = stages.value[0]?.id
    ElMessage.success('阶段初始化完成')
    await loadDetail()
  } finally {
    stageInitLoading.value = false
  }
}

function selectStage(stage: WorkflowStage) {
  selectedStageId.value = stage.id
}

async function runStageAction(stage: WorkflowStage, action: 'start' | 'complete' | 'skip' | 'fail') {
  const handlers = {
    start: startStage,
    complete: completeStage,
    skip: skipStage,
    fail: failStage
  }
  await handlers[action](id.value, stage.id)
  ElMessage.success('阶段状态已更新')
  await handleInitStages()
}

async function handleGeneratePrompt() {
  if (!selectedStageId.value) {
    ElMessage.warning('请先选择阶段')
    return
  }
  promptLoading.value = true
  try {
    const result = await generatePrompt(id.value, selectedStageId.value)
    promptContent.value = result.promptContent
  } finally {
    promptLoading.value = false
  }
}

function openOutputDialog(stage?: WorkflowStage) {
  if (!stage) {
    ElMessage.warning('请先选择阶段')
    return
  }
  selectedStageId.value = stage.id
  outputDialogVisible.value = true
}

async function saveOutput() {
  if (!selectedStageId.value) return
  outputSaving.value = true
  try {
    await saveStageOutput(id.value, selectedStageId.value, { ...outputForm })
    ElMessage.success('阶段输出已保存')
    outputDialogVisible.value = false
  } finally {
    outputSaving.value = false
  }
}

async function handleGenerateChecklist() {
  deliveryLoading.value = true
  try {
    Object.assign(deliveryRecord, await generateTestChecklist(id.value))
    ElMessage.success('测试清单已生成')
    await loadDetail()
  } finally {
    deliveryLoading.value = false
  }
}

async function handleGenerateSummary() {
  deliveryLoading.value = true
  try {
    Object.assign(deliveryRecord, await generateDeliverySummary(id.value))
    ElMessage.success('交付总结已生成')
    await loadDetail()
  } finally {
    deliveryLoading.value = false
  }
}

async function handleExportMarkdown() {
  markdownLoading.value = true
  try {
    markdownContent.value = await exportMarkdown(id.value)
  } finally {
    markdownLoading.value = false
  }
}

async function copyText(text: string) {
  if (!text) {
    ElMessage.warning('暂无可复制内容')
    return
  }
  await navigator.clipboard.writeText(text)
  ElMessage.success('已复制')
}

onMounted(loadDetail)
</script>

<style scoped>
.gap-alert {
  margin-bottom: 16px;
}

.detail-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.4fr) minmax(380px, 0.8fr);
  gap: 16px;
}

.description-text {
  white-space: pre-wrap;
}

.mini-table {
  margin-top: 12px;
}

@media (max-width: 1440px) {
  .detail-grid {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
