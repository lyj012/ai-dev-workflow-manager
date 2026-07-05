<template>
  <el-alert
    v-if="stage"
    type="info"
    :closable="false"
    show-icon
    :title="`当前阶段：${stage.stageName}。${stage.stageGoal ?? '请按本阶段目标推进。'}`"
  />
  <el-empty v-else description="请选择一个阶段" :image-size="72" />
</template>

<script setup lang="ts">
import type { WorkflowStage } from '@/types/stage'

defineProps<{ stage?: WorkflowStage }>()
</script>
