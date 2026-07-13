<template>
  <div>
    <AppPageHeader title="Workflow 模板" description="查看系统内置 workflow 模板、阶段配置和适用任务范围。" />
    <div class="metrics-grid">
      <div class="metric-card">
        <div class="metric-icon metric-blue"><el-icon><Share /></el-icon></div>
        <div>
          <div class="metric-value">{{ templates.length }}</div>
          <div class="metric-label">模板总数</div>
          <div class="metric-hint">复用既有模板接口</div>
        </div>
      </div>
      <div class="metric-card">
        <div class="metric-icon metric-green"><el-icon><CircleCheck /></el-icon></div>
        <div>
          <div class="metric-value">{{ enabledCount }}</div>
          <div class="metric-label">已启用</div>
          <div class="metric-hint">可参与 workflow 匹配</div>
        </div>
      </div>
      <div class="metric-card">
        <div class="metric-icon metric-indigo"><el-icon><List /></el-icon></div>
        <div>
          <div class="metric-value">{{ stageCount }}</div>
          <div class="metric-label">阶段配置</div>
          <div class="metric-hint">分析、设计、验证与交付</div>
        </div>
      </div>
    </div>
    <div class="template-grid">
      <WorkflowTemplateCard
        v-for="template in templates"
        :key="template.id"
        :template="template"
        @open="(id) => router.push(`/templates/${id}`)"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { CircleCheck, List, Share } from '@element-plus/icons-vue'

import { fetchTemplates } from '@/api/template'
import AppPageHeader from '@/components/common/AppPageHeader.vue'
import WorkflowTemplateCard from '@/components/workflow/WorkflowTemplateCard.vue'
import type { WorkflowTemplate } from '@/types/template'

const router = useRouter()
const templates = ref<WorkflowTemplate[]>([])
const enabledCount = computed(() => templates.value.filter((template) => template.enabled).length)
const stageCount = computed(() => templates.value.reduce((sum, template) => sum + template.stages.length, 0))

onMounted(async () => {
  templates.value = await fetchTemplates()
})
</script>

<style scoped>
.template-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

@media (max-width: 1180px) {
  .template-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 760px) {
  .template-grid {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
