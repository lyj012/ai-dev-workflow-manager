<template>
  <div class="template-card" @click="$emit('open', template.id)">
    <div class="template-card__head">
      <div>
        <div class="template-card__eyebrow">WORKFLOW TEMPLATE</div>
        <div class="template-card__title">{{ template.name }}</div>
      </div>
      <el-tag :type="template.enabled ? 'success' : 'info'" effect="plain">{{ template.enabled ? '启用' : '停用' }}</el-tag>
    </div>
    <div class="template-card__desc">{{ template.description }}</div>
    <div class="template-card__stats">
      <div>
        <strong>{{ template.stages.length }}</strong>
        <span>阶段</span>
      </div>
      <div>
        <strong>{{ template.priority }}</strong>
        <span>优先级</span>
      </div>
      <div>
        <strong>v{{ template.version }}</strong>
        <span>版本</span>
      </div>
    </div>
    <div class="template-card__meta">
      <el-tag v-for="item in template.applicableTaskTypes" :key="item" effect="plain">{{ item }}</el-tag>
      <el-tag v-for="item in template.riskTags.slice(0, 3)" :key="item" effect="plain">{{ item }}</el-tag>
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
  min-height: 242px;
  padding: 18px;
  border: 1px solid var(--app-border-soft);
  border-radius: var(--app-radius);
  background: var(--app-surface);
  cursor: pointer;
  box-shadow: var(--app-shadow-soft);
  transition:
    transform 0.16s ease,
    border-color 0.16s ease,
    box-shadow 0.16s ease;
}

.template-card:hover {
  border-color: var(--app-primary);
  box-shadow: var(--app-shadow);
  transform: translateY(-2px);
}

.template-card__head {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  justify-content: space-between;
}

.template-card__eyebrow {
  margin-bottom: 6px;
  color: var(--app-primary);
  font-size: 11px;
  font-weight: 900;
  letter-spacing: 0.08em;
}

.template-card__title {
  font-size: 17px;
  font-weight: 700;
  color: var(--app-text);
}

.template-card__desc {
  min-height: 54px;
  margin-top: 12px;
  color: var(--app-muted);
  line-height: 1.6;
}

.template-card__stats {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
  margin-top: 16px;
}

.template-card__stats div {
  padding: 10px;
  border: 1px solid var(--app-border-soft);
  border-radius: 8px;
  background: #f8fafc;
}

.template-card__stats strong,
.template-card__stats span {
  display: block;
}

.template-card__stats strong {
  color: var(--app-text);
}

.template-card__stats span {
  margin-top: 2px;
  color: var(--app-muted);
  font-size: 12px;
}

.template-card__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}
</style>
