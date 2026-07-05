<template>
  <el-tag :type="tagType" effect="plain">{{ label }}</el-tag>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { labelOf, stageStatusOptions, taskStatusOptions } from '@/constants/options'

const props = defineProps<{
  value?: string | null
  kind?: 'task' | 'stage'
}>()

const label = computed(() => {
  const options = props.kind === 'stage' ? stageStatusOptions : taskStatusOptions
  return labelOf(options, props.value as never)
})

const tagType = computed(() => {
  if (props.value === 'COMPLETED' || props.value === 'DELIVERED') return 'success'
  if (props.value === 'FAILED' || props.value === 'CANCELED') return 'danger'
  if (props.value === 'RUNNING' || props.value === 'TESTING' || props.value === 'EXECUTING') return 'warning'
  return 'info'
})
</script>
