<template>
  <el-tag :type="tagType" effect="plain">
    <el-icon class="status-icon"><component :is="icon" /></el-icon>
    {{ label }}
  </el-tag>
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

const tagType = computed(() => {
  if (props.value === 'COMPLETED' || props.value === 'DELIVERED' || props.value === 'SUCCESS') return 'success'
  if (props.value === 'FAILED' || props.value === 'CANCELED') return 'danger'
  if (props.value === 'RUNNING' || props.value === 'TESTING' || props.value === 'EXECUTING' || props.value === 'ANALYZING') return 'warning'
  return 'info'
})

const icon = computed(() => {
  if (props.value === 'COMPLETED' || props.value === 'DELIVERED' || props.value === 'SUCCESS') return CircleCheck
  if (props.value === 'FAILED' || props.value === 'CANCELED') return WarningFilled
  if (props.value === 'RUNNING' || props.value === 'TESTING' || props.value === 'EXECUTING' || props.value === 'ANALYZING') return VideoPlay
  return Document
})
</script>

<style scoped>
.status-icon {
  margin-right: 4px;
}
</style>
