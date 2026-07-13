<template>
  <span :class="['status-tag', `status-tag--${statusClass}`]">
    <el-icon class="status-icon"><component :is="icon" /></el-icon>
    {{ label }}
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { CircleCheck, Document, VideoPlay, WarningFilled } from '@element-plus/icons-vue'
import { aiTaskStatusOptions, labelOf, stageStatusOptions, taskStatusOptions } from '@/constants/options'

const props = defineProps<{
  value?: string | null
  kind?: 'task' | 'stage' | 'aiTask'
}>()

const label = computed(() => {
  const options = props.kind === 'stage' ? stageStatusOptions : props.kind === 'aiTask' ? aiTaskStatusOptions : taskStatusOptions
  return labelOf(options, props.value as never)
})

const statusClass = computed(() => String(props.value || 'unknown').toLowerCase())

const icon = computed(() => {
  if (props.value === 'COMPLETED' || props.value === 'DELIVERED' || props.value === 'SUCCESS') return CircleCheck
  if (props.value === 'FAILED' || props.value === 'CANCELED') return WarningFilled
  if (props.value === 'RUNNING' || props.value === 'TESTING' || props.value === 'EXECUTING' || props.value === 'ANALYZING') return VideoPlay
  return Document
})
</script>

<style scoped>
.status-tag {
  display: inline-flex;
  min-height: 28px;
  align-items: center;
  border-radius: 999px;
  padding: 0 11px;
  font-size: 12px;
  font-weight: 700;
  line-height: 1;
}

.status-icon {
  margin-right: 4px;
}

.status-tag--success,
.status-tag--completed,
.status-tag--delivered {
  background: var(--app-success-soft);
  color: var(--app-success);
}

.status-tag--failed,
.status-tag--canceled {
  background: var(--app-danger-soft);
  color: var(--app-danger);
}

.status-tag--analyzing,
.status-tag--running,
.status-tag--executing,
.status-tag--testing {
  background: var(--app-warning-soft);
  color: var(--app-warning);
}

.status-tag--created,
.status-tag--pending,
.status-tag--draft,
.status-tag--skipped,
.status-tag--archived,
.status-tag--unknown {
  background: var(--app-info-soft);
  color: var(--app-info);
}
</style>
