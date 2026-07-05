<template>
  <el-descriptions v-if="task" :column="2" border>
    <el-descriptions-item label="标题" :span="2">{{ task.title }}</el-descriptions-item>
    <el-descriptions-item label="描述" :span="2">{{ task.description || '-' }}</el-descriptions-item>
    <el-descriptions-item label="任务类型">{{ labelOf(taskTypeOptions, task.taskType) }}</el-descriptions-item>
    <el-descriptions-item label="复杂度">{{ labelOf(complexityOptions, task.complexity) }}</el-descriptions-item>
    <el-descriptions-item label="风险标签" :span="2"><RiskTagGroup :tags="task.riskTags" /></el-descriptions-item>
    <el-descriptions-item label="任务状态"><StatusTag :value="task.status" /></el-descriptions-item>
    <el-descriptions-item label="命中 workflow">{{ task.matchedTemplateName || task.matchedTemplateId || '未绑定' }}</el-descriptions-item>
    <el-descriptions-item label="测试清单">{{ task.testChecklistGenerated ? '已生成' : '未生成' }}</el-descriptions-item>
    <el-descriptions-item label="更新时间">{{ formatTime(task.updatedAt) }}</el-descriptions-item>
  </el-descriptions>
</template>

<script setup lang="ts">
import RiskTagGroup from '@/components/common/RiskTagGroup.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import type { TaskDetail } from '@/types/task'
import { complexityOptions, labelOf, taskTypeOptions } from '@/constants/options'
import { formatTime } from '@/utils/format'

defineProps<{ task?: TaskDetail }>()
</script>
