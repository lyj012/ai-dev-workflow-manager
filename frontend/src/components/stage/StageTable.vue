<template>
  <el-table :data="stages" row-key="id" empty-text="暂无阶段，请先匹配 workflow 并初始化阶段">
    <el-table-column prop="stageOrder" label="顺序" width="72" />
    <el-table-column prop="stageName" label="阶段名称" min-width="140" />
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
    <el-table-column label="操作" width="300" fixed="right">
      <template #default="{ row }">
        <div class="stage-actions">
          <div class="stage-actions__flow" aria-label="阶段状态操作">
            <el-button
              class="stage-action-btn"
              size="small"
              :type="row.status === 'PENDING' ? 'primary' : 'info'"
              :disabled="row.status !== 'PENDING'"
              @click="$emit('action', row, 'start')"
            >
              开始
            </el-button>
            <el-button
              class="stage-action-btn"
              size="small"
              :type="row.status === 'PENDING' ? 'warning' : 'info'"
              :disabled="row.status !== 'PENDING'"
              @click="$emit('action', row, 'skip')"
            >
              跳过
            </el-button>
            <el-button
              class="stage-action-btn"
              size="small"
              :type="row.status === 'RUNNING' ? 'success' : 'info'"
              :disabled="row.status !== 'RUNNING'"
              @click="$emit('action', row, 'complete')"
            >
              完成
            </el-button>
            <el-button
              class="stage-action-btn"
              size="small"
              :type="row.status === 'RUNNING' ? 'danger' : 'info'"
              :disabled="row.status !== 'RUNNING'"
              @click="$emit('action', row, 'fail')"
            >
              失败
            </el-button>
          </div>
          <div class="stage-actions__meta">
            <el-button link type="primary" @click="$emit('select', row)">选择</el-button>
            <el-button link type="primary" @click="$emit('edit-output', row)">回填输出</el-button>
          </div>
        </div>
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

<style scoped>
.stage-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: flex-start;
}

.stage-actions__flow {
  display: inline-flex;
  gap: 6px;
  align-items: center;
  padding: 4px;
  border: 1px solid #dfe8f5;
  border-radius: 8px;
  background: #f8fbff;
}

.stage-action-btn {
  width: 46px;
  margin-left: 0;
  border-radius: 6px;
  box-shadow: none;
}

.stage-action-btn.is-disabled {
  border-color: transparent;
  background: transparent;
  color: #a8b3c4;
}

.stage-actions__meta {
  display: flex;
  gap: 12px;
  padding-left: 4px;
}

.stage-actions__meta :deep(.el-button) {
  height: 22px;
  padding: 0;
  color: var(--app-primary);
  font-weight: 600;
}
</style>
