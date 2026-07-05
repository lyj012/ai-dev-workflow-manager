<template>
  <el-space wrap>
    <el-tag v-for="tag in tags" :key="tag" :type="tagType(tag)" effect="plain">
      {{ labelOf(riskTagOptions, tag) }}
    </el-tag>
    <span v-if="!tags?.length" class="muted">无</span>
  </el-space>
</template>

<script setup lang="ts">
import type { RiskTag } from '@/types/task'
import { labelOf, riskTagOptions } from '@/constants/options'

defineProps<{
  tags: RiskTag[]
}>()

function tagType(tag: RiskTag) {
  if (['permission', 'database', 'payment', 'callback'].includes(tag)) return 'danger'
  if (['config', 'file', 'download'].includes(tag)) return 'warning'
  return 'info'
}
</script>
