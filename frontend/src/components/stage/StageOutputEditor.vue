<template>
  <el-form label-width="96px">
    <el-form-item label="输出摘要">
      <el-input v-model="model.outputSummary" type="textarea" :rows="4" />
    </el-form-item>
    <el-form-item label="风险点">
      <el-input v-model="model.riskPoints" type="textarea" :rows="3" />
    </el-form-item>
    <el-form-item label="后续动作">
      <el-input v-model="model.nextActions" type="textarea" :rows="3" />
    </el-form-item>
    <el-form-item label="未验证范围">
      <el-input v-model="model.unverifiedScope" type="textarea" :rows="3" />
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
import type { StageOutputPayload } from '@/types/stage'

defineProps<{ model: StageOutputPayload }>()
</script>
