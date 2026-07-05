<template>
  <el-table :data="stages" row-key="id" empty-text="暂无阶段，请先匹配 workflow 并初始化阶段">
    <el-table-column prop="stageOrder" label="顺序" width="72" />
    <el-table-column prop="stageName" label="阶段名称" min-width="140" />
    <el-table-column prop="stageKey" label="阶段标识" min-width="130" />
    <el-table-column label="必需" width="80">
      <template #default="{ row }">{{ row.required === false ? '否' : '是' }}</template>
    </el-table-column>
    <el-table-column label="状态" width="110">
      <template #default="{ row }"><StatusTag :value="row.status" kind="stage" /></template>
    </el-table-column>
    <el-table-column label="开始时间" width="170">
      <template #default="{ row }">{{ formatTime(row.startedAt) }}</template>
    </el-table-column>
    <el-table-column label="完成时间" width="170">
      <template #default="{ row }">{{ formatTime(row.completedAt) }}</template>
    </el-table-column>
    <el-table-column label="操作" width="340" fixed="right">
      <template #default="{ row }">
        <el-button v-if="row.status === 'PENDING'" link type="primary" @click="$emit('action', row, 'start')">开始</el-button>
        <el-button v-if="row.status === 'RUNNING'" link type="success" @click="$emit('action', row, 'complete')">完成</el-button>
        <el-button v-if="row.status === 'PENDING'" link type="warning" @click="$emit('action', row, 'skip')">跳过</el-button>
        <el-button v-if="row.status === 'RUNNING'" link type="danger" @click="$emit('action', row, 'fail')">失败</el-button>
        <el-button link type="primary" @click="$emit('select', row)">选择阶段</el-button>
        <el-button link type="primary" @click="$emit('edit-output', row)">回填输出</el-button>
      </template>
    </el-table-column>
  </el-table>
</template>

<script setup lang="ts">
import StatusTag from '@/components/common/StatusTag.vue'
import type { WorkflowStage } from '@/types/stage'
import { formatTime } from '@/utils/format'

defineProps<{ stages: WorkflowStage[] }>()
defineEmits<{
  select: [stage: WorkflowStage]
  action: [stage: WorkflowStage, action: 'start' | 'complete' | 'skip' | 'fail']
  'edit-output': [stage: WorkflowStage]
}>()
</script>
