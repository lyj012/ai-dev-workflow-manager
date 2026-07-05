<template>
  <el-table :data="tasks" row-key="id">
    <el-table-column prop="title" label="任务标题" min-width="220" show-overflow-tooltip />
    <el-table-column label="任务类型" width="120">
      <template #default="{ row }">{{ labelOf(taskTypeOptions, row.taskType) }}</template>
    </el-table-column>
    <el-table-column label="复杂度" width="110">
      <template #default="{ row }">{{ labelOf(complexityOptions, row.complexity) }}</template>
    </el-table-column>
    <el-table-column label="风险标签" min-width="180">
      <template #default="{ row }"><RiskTagGroup :tags="row.riskTags" /></template>
    </el-table-column>
    <el-table-column label="状态" width="110">
      <template #default="{ row }"><StatusTag :value="row.status" /></template>
    </el-table-column>
    <el-table-column prop="matchedTemplateName" label="命中 workflow" min-width="160" show-overflow-tooltip />
    <el-table-column label="测试清单" width="110">
      <template #default="{ row }">{{ row.testChecklistGenerated ? '已生成' : '未生成' }}</template>
    </el-table-column>
    <el-table-column label="更新时间" width="180">
      <template #default="{ row }">{{ formatTime(row.updatedAt) }}</template>
    </el-table-column>
    <el-table-column label="操作" width="110" fixed="right">
      <template #default="{ row }">
        <el-button link type="primary" @click="$emit('detail', row.id)">查看详情</el-button>
      </template>
    </el-table-column>
  </el-table>
</template>

<script setup lang="ts">
import RiskTagGroup from '@/components/common/RiskTagGroup.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import type { Task } from '@/types/task'
import { complexityOptions, labelOf, taskTypeOptions } from '@/constants/options'
import { formatTime } from '@/utils/format'

defineProps<{ tasks: Task[] }>()
defineEmits<{ detail: [id: number] }>()
</script>
