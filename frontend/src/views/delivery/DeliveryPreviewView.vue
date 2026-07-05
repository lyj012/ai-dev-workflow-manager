<template>
  <div>
    <AppPageHeader title="交付预览" description="集中展示任务交付记录和 Markdown 复盘内容。">
      <template #actions>
        <el-button @click="router.push(`/tasks/${taskId}`)">返回任务详情</el-button>
        <el-button type="primary" @click="copyText(preview?.markdown)">复制 Markdown</el-button>
      </template>
    </AppPageHeader>

    <div class="section">
      <div class="section-header"><div class="section-title">任务信息</div></div>
      <div class="section-body">
        <TaskBasicInfo :task="preview?.task" />
      </div>
    </div>

    <div class="section">
      <div class="section-header"><div class="section-title">阶段记录</div></div>
      <StageTable :stages="preview?.stages ?? []" @select="() => undefined" @action="() => undefined" @edit-output="() => undefined" />
    </div>

    <div class="delivery-grid">
      <div class="section">
        <div class="section-header"><div class="section-title">测试清单</div></div>
        <div class="section-body"><TestChecklistPanel :content="preview?.record.testChecklist" /></div>
      </div>
      <div class="section">
        <div class="section-header"><div class="section-title">交付总结</div></div>
        <div class="section-body"><DeliverySummaryPanel :record="preview?.record ?? {}" /></div>
      </div>
    </div>

    <div class="section">
      <div class="section-header"><div class="section-title">Markdown 原文</div></div>
      <div class="section-body"><MarkdownPreview :content="preview?.markdown" /></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

import { fetchDeliveryPreview } from '@/api/delivery'
import AppPageHeader from '@/components/common/AppPageHeader.vue'
import DeliverySummaryPanel from '@/components/delivery/DeliverySummaryPanel.vue'
import MarkdownPreview from '@/components/delivery/MarkdownPreview.vue'
import TestChecklistPanel from '@/components/delivery/TestChecklistPanel.vue'
import StageTable from '@/components/stage/StageTable.vue'
import TaskBasicInfo from '@/components/task/TaskBasicInfo.vue'
import type { DeliveryPreview } from '@/types/delivery'
import { copyText } from '@/utils/clipboard'

const props = defineProps<{ taskId: string }>()
const router = useRouter()
const preview = ref<DeliveryPreview>()

onMounted(async () => {
  preview.value = await fetchDeliveryPreview(Number(props.taskId))
})
</script>

<style scoped>
.delivery-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}
</style>
