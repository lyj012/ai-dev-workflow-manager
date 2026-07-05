<template>
  <el-space wrap>
    <el-tag v-for="tag in tags" :key="tag" type="warning" effect="plain">
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
</script>
