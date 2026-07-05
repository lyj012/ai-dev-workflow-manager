<template>
  <div>
    <AppPageHeader title="Workflow 模板" description="查看系统内置 workflow 模板，第一阶段只做查看。" />
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
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

import { fetchTemplates } from '@/api/template'
import AppPageHeader from '@/components/common/AppPageHeader.vue'
import WorkflowTemplateCard from '@/components/workflow/WorkflowTemplateCard.vue'
import type { WorkflowTemplate } from '@/types/template'

const router = useRouter()
const templates = ref<WorkflowTemplate[]>([])

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
</style>
