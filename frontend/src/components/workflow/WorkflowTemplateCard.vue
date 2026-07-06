<template>
  <div class="template-card" @click="$emit('open', template.id)">
    <div class="template-card__title">{{ template.name }}</div>
    <div class="template-card__desc">{{ template.description }}</div>
    <div class="template-card__meta">
      <el-tag :type="template.enabled ? 'success' : 'info'" effect="plain">{{ template.enabled ? '启用' : '停用' }}</el-tag>
      <el-tag effect="plain">v{{ template.version }}</el-tag>
      <el-tag effect="plain">优先级 {{ template.priority }}</el-tag>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { WorkflowTemplate } from '@/types/template'

defineProps<{ template: WorkflowTemplate }>()
defineEmits<{ open: [id: number] }>()
</script>

<style scoped>
.template-card {
  min-height: 172px;
  padding: 18px;
  border: 1px solid var(--app-border);
  border-radius: 8px;
  background: var(--app-surface);
  box-shadow: var(--app-shadow-soft);
  cursor: pointer;
  transition:
    border-color 0.2s ease,
    box-shadow 0.2s ease,
    transform 0.2s ease;
}

.template-card:hover {
  border-color: #c9ced8;
  box-shadow: var(--app-shadow);
  transform: translateY(-1px);
}

.template-card__title {
  font-size: 16px;
  font-weight: 650;
  color: var(--app-text);
}

.template-card__desc {
  min-height: 48px;
  margin-top: 8px;
  color: var(--app-muted);
  font-size: 13px;
  line-height: 1.55;
}

.template-card__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}
</style>
