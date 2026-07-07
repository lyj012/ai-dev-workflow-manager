<template>
  <div>
    <el-descriptions :column="1" border>
      <el-descriptions-item label="模板名称">
        {{ result?.matchedTemplateName ?? currentTemplateName ?? '未匹配' }}
      </el-descriptions-item>
      <el-descriptions-item label="匹配分数">{{ result?.matchScore ?? '-' }}</el-descriptions-item>
      <el-descriptions-item label="自动绑定">{{ result ? (result.autoBound ? '是' : '否') : '-' }}</el-descriptions-item>
      <el-descriptions-item label="匹配原因">{{ matchReason }}</el-descriptions-item>
    </el-descriptions>
    <el-table v-if="result?.candidates?.length" :data="result.candidates" class="mini-table">
      <el-table-column prop="templateName" label="候选模板" />
      <el-table-column prop="matchScore" label="分数" width="80" />
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

import type { MatchTemplateResponse } from '@/types/template'

const props = defineProps<{
  result?: MatchTemplateResponse
  currentTemplateName?: string | null
}>()

const matchReason = computed(() => props.result?.matchReasons?.join('；') || '-')
</script>
