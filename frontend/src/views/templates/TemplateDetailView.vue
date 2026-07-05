<template>
  <div>
    <AppPageHeader title="模板详情" description="查看 workflow 模板基础信息和阶段配置。">
      <template #actions>
        <el-button @click="router.push('/templates')">返回模板列表</el-button>
      </template>
    </AppPageHeader>

    <div class="section">
      <div class="section-header"><div class="section-title">模板信息</div></div>
      <div class="section-body">
        <el-descriptions v-if="template" :column="2" border>
          <el-descriptions-item label="模板名称">{{ template.name }}</el-descriptions-item>
          <el-descriptions-item label="启用状态">{{ template.enabled ? '启用' : '停用' }}</el-descriptions-item>
          <el-descriptions-item label="说明" :span="2">{{ template.description }}</el-descriptions-item>
          <el-descriptions-item label="优先级">{{ template.priority }}</el-descriptions-item>
          <el-descriptions-item label="版本">{{ template.version }}</el-descriptions-item>
          <el-descriptions-item label="任务类型" :span="2">{{ template.applicableTaskTypes.join(', ') }}</el-descriptions-item>
          <el-descriptions-item label="复杂度" :span="2">{{ template.applicableComplexities.join(', ') }}</el-descriptions-item>
          <el-descriptions-item label="风险标签" :span="2"><RiskTagGroup :tags="template.riskTags" /></el-descriptions-item>
        </el-descriptions>
      </div>
    </div>

    <div class="section">
      <div class="section-header"><div class="section-title">阶段配置</div></div>
      <el-table :data="template?.stages ?? []">
        <el-table-column prop="stageOrder" label="顺序" width="80" />
        <el-table-column prop="stageKey" label="阶段标识" width="150" />
        <el-table-column prop="stageName" label="阶段名称" width="150" />
        <el-table-column label="必需" width="80">
          <template #default="{ row }">{{ row.required ? '是' : '否' }}</template>
        </el-table-column>
        <el-table-column prop="stageGoal" label="阶段说明" min-width="260" />
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

import { fetchTemplateDetail } from '@/api/template'
import AppPageHeader from '@/components/common/AppPageHeader.vue'
import RiskTagGroup from '@/components/common/RiskTagGroup.vue'
import type { WorkflowTemplate } from '@/types/template'

const props = defineProps<{ templateId: string }>()
const router = useRouter()
const template = ref<WorkflowTemplate>()

onMounted(async () => {
  template.value = await fetchTemplateDetail(Number(props.templateId))
})
</script>
