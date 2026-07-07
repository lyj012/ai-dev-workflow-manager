<template>
  <div>
    <AppPageHeader title="任务详情" description="围绕 workflow、阶段、Prompt、输出回填和交付记录推进任务。">
      <template #actions>
        <el-button :icon="Refresh" @click="loadDetail">刷新任务</el-button>
        <el-button @click="router.push(`/delivery/${taskId}`)">Markdown 预览</el-button>
        <el-button :icon="ArrowLeft" @click="router.push('/tasks')">返回列表</el-button>
      </template>
    </AppPageHeader>

    <el-alert v-if="USE_MOCK" class="gap-alert" type="info" :closable="false" show-icon title="当前使用 mock 数据展示页面骨架。" />

    <div class="section">
      <div class="section-header"><div class="section-title">任务基础信息</div></div>
      <div class="section-body">
        <el-skeleton v-if="loading" :rows="6" animated />
        <TaskBasicInfo v-else :task="task" />
      </div>
    </div>

    <div class="detail-grid">
      <div class="section">
        <div class="section-header">
          <div class="section-title">Workflow 匹配区</div>
          <div class="page-actions">
            <el-button type="primary" :loading="matchingLoading" @click="handleMatch">匹配 workflow</el-button>
            <el-button :loading="stageInitLoading" @click="handleInitStages">初始化阶段</el-button>
          </div>
        </div>
        <div class="section-body">
          <WorkflowMatchPanel :result="matchResult" :current-template-name="task?.matchedTemplateName" />
        </div>
      </div>

      <div class="section">
        <div class="section-header"><div class="section-title">当前阶段操作区</div></div>
        <div class="section-body">
          <StageActionBar :stage="selectedStage" />
        </div>
      </div>
    </div>

    <div class="section">
      <div class="section-header"><div class="section-title">阶段流程区</div></div>
      <StageTable :stages="stages" @select="selectStage" @action="runStageAction" @edit-output="openOutputDialog" />
    </div>

    <div class="detail-grid">
      <div class="section">
        <div class="section-header">
          <div class="section-title">Prompt 预览区</div>
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
        <div class="section-body"><PromptPreview :content="promptContent" /></div>
      </div>

      <div class="section">
        <div class="section-header">
          <div class="section-title">阶段输出区</div>
          <el-button :disabled="!selectedStage" @click="openOutputDialog(selectedStage)">编辑输出</el-button>
        </div>
        <div class="section-body">
          <el-descriptions v-if="selectedStage" :column="1" border>
            <el-descriptions-item label="输出摘要">{{ outputForm.outputSummary || selectedStage.outputSummary || '-' }}</el-descriptions-item>
            <el-descriptions-item label="风险点">{{ outputForm.riskPoints || selectedStage.riskPoints || '-' }}</el-descriptions-item>
            <el-descriptions-item label="后续动作">{{ outputForm.nextActions || selectedStage.nextActions || '-' }}</el-descriptions-item>
            <el-descriptions-item label="未验证范围">{{ outputForm.unverifiedScope || selectedStage.unverifiedScope || '-' }}</el-descriptions-item>
          </el-descriptions>
          <el-empty v-else description="请选择阶段" :image-size="72" />
        </div>
      </div>
    </div>

    <div class="detail-grid">
      <div class="section">
        <div class="section-header">
          <div class="section-title">测试清单区</div>
          <el-button :loading="deliveryLoading" @click="handleGenerateChecklist">生成测试清单</el-button>
        </div>
        <div class="section-body"><TestChecklistPanel :content="deliveryRecord.testChecklist" /></div>
      </div>

      <div class="section">
        <div class="section-header">
          <div class="section-title">交付总结区</div>
          <div class="page-actions">
            <el-button type="primary" :loading="deliveryLoading" @click="handleGenerateSummary">生成交付总结</el-button>
            <el-button @click="router.push(`/delivery/${taskId}`)">进入 Markdown 预览</el-button>
          </div>
        </div>
        <div class="section-body"><DeliverySummaryPanel :record="deliveryRecord" /></div>
      </div>
    </div>

    <el-dialog v-model="outputDialogVisible" title="阶段输出回填" width="620px">
      <StageOutputEditor :model="outputForm" />
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

import { generateDeliverySummary, generateTestChecklist } from '@/api/delivery'
import { matchTemplate } from '@/api/matching'
import { generatePrompt } from '@/api/prompt'
import { completeStage, failStage, initializeStages, saveStageOutput, skipStage, startStage } from '@/api/stage'
import { fetchTaskDetail } from '@/api/task'
import AppPageHeader from '@/components/common/AppPageHeader.vue'
import DeliverySummaryPanel from '@/components/delivery/DeliverySummaryPanel.vue'
import TestChecklistPanel from '@/components/delivery/TestChecklistPanel.vue'
import PromptPreview from '@/components/prompt/PromptPreview.vue'
import StageActionBar from '@/components/stage/StageActionBar.vue'
import StageOutputEditor from '@/components/stage/StageOutputEditor.vue'
import StageTable from '@/components/stage/StageTable.vue'
import TaskBasicInfo from '@/components/task/TaskBasicInfo.vue'
import WorkflowMatchPanel from '@/components/workflow/WorkflowMatchPanel.vue'
import { USE_MOCK } from '@/constants/enums'
import type { DeliveryRecord } from '@/types/delivery'
import type { StageOutputPayload, WorkflowStage } from '@/types/stage'
import type { MatchTemplateResponse } from '@/types/template'
import type { TaskDetail } from '@/types/task'
import { copyText } from '@/utils/clipboard'

const props = defineProps<{ taskId: string }>()
const router = useRouter()
const id = computed(() => Number(props.taskId))

const loading = ref(false)
const matchingLoading = ref(false)
const stageInitLoading = ref(false)
const promptLoading = ref(false)
const outputSaving = ref(false)
const deliveryLoading = ref(false)
const outputDialogVisible = ref(false)

const task = ref<TaskDetail>()
const stages = ref<WorkflowStage[]>([])
const selectedStageId = ref<number>()
const matchResult = ref<MatchTemplateResponse>()
const promptContent = ref('')
const deliveryRecord = reactive<DeliveryRecord>({})
const outputForm = reactive<StageOutputPayload>({ outputSummary: '', riskPoints: '', nextActions: '', unverifiedScope: '' })

const selectedStage = computed(() => stages.value.find((stage) => stage.id === selectedStageId.value))

async function loadDetail() {
  loading.value = true
  try {
    const detail = await fetchTaskDetail(id.value)
    task.value = detail
    const detailStages = (detail as TaskDetail & { stages?: WorkflowStage[] }).stages
    if (Array.isArray(detailStages)) {
      stages.value = detailStages
      selectedStageId.value = selectedStageId.value ?? stages.value[0]?.id
    }
  } finally {
    loading.value = false
  }
}

async function handleMatch() {
  matchingLoading.value = true
  try {
    matchResult.value = await matchTemplate(id.value)
    if (matchResult.value.autoBound && task.value) {
      task.value.matchedTemplateId = matchResult.value.matchedTemplateId
      task.value.matchedTemplateName = matchResult.value.matchedTemplateName
    }
    ElMessage.success('Workflow 匹配完成')
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
  } finally {
    stageInitLoading.value = false
  }
}

function selectStage(stage: WorkflowStage) {
  selectedStageId.value = stage.id
}

async function runStageAction(stage: WorkflowStage, action: 'start' | 'complete' | 'skip' | 'fail') {
  const handlers = { start: startStage, complete: completeStage, skip: skipStage, fail: failStage }
  const updatedStage = await handlers[action](id.value, stage.id)
  stages.value = stages.value.map((item) => (item.id === updatedStage.id ? updatedStage : item))
  selectedStageId.value = updatedStage.id
  ElMessage.success('阶段状态已更新')
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
  outputForm.outputSummary = stage.outputSummary ?? outputForm.outputSummary
  outputForm.riskPoints = stage.riskPoints ?? outputForm.riskPoints
  outputForm.nextActions = stage.nextActions ?? outputForm.nextActions
  outputForm.unverifiedScope = stage.unverifiedScope ?? outputForm.unverifiedScope
  outputDialogVisible.value = true
}

async function saveOutput() {
  if (!selectedStageId.value) return
  outputSaving.value = true
  try {
    const savedOutput = await saveStageOutput(id.value, selectedStageId.value, { ...outputForm })
    stages.value = stages.value.map((stage) =>
      stage.id === selectedStageId.value ? { ...stage, ...savedOutput } : stage
    )
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
  } finally {
    deliveryLoading.value = false
  }
}

async function handleGenerateSummary() {
  deliveryLoading.value = true
  try {
    Object.assign(deliveryRecord, await generateDeliverySummary(id.value))
    ElMessage.success('交付总结已生成')
  } finally {
    deliveryLoading.value = false
  }
}

onMounted(async () => {
  await loadDetail()
})
</script>

<style scoped>
.gap-alert {
  margin-bottom: 16px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
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
