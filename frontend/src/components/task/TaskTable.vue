<template>
  <el-table :data="tasks" row-key="id" empty-text="暂无任务">
    <el-table-column label="任务" min-width="280">
      <template #default="{ row }">
        <div class="task-title-cell">
          <span class="task-id">#{{ row.id }}</span>
          <div>
            <strong>{{ row.title }}</strong>
            <span>{{ row.description }}</span>
          </div>
        </div>
      </template>
    </el-table-column>
    <el-table-column label="任务类型" width="120">
      <template #default="{ row }"><span class="type-chip">{{ labelOf(taskTypeOptions, row.taskType) }}</span></template>
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
    <el-table-column prop="matchedTemplateName" label="命中 workflow" min-width="170" show-overflow-tooltip />
    <el-table-column label="测试清单" width="130">
      <template #default="{ row }">
        <span :class="['check-state', row.testChecklistGenerated ? 'check-state--done' : 'check-state--todo']">
          <el-icon><component :is="row.testChecklistGenerated ? CircleCheck : Remove" /></el-icon>
          {{ row.testChecklistGenerated ? '已生成' : '未生成' }}
        </span>
      </template>
    </el-table-column>
    <el-table-column label="更新时间" width="180">
      <template #default="{ row }">{{ formatTime(row.updatedAt) }}</template>
    </el-table-column>
    <el-table-column label="操作" width="130" fixed="right" align="right">
      <template #default="{ row }">
        <el-button link type="primary" @click="$emit('detail', row.id)">
          查看详情
          <el-icon><ArrowRight /></el-icon>
        </el-button>
      </template>
    </el-table-column>
  </el-table>
</template>

<script setup lang="ts">
import RiskTagGroup from '@/components/common/RiskTagGroup.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import { ArrowRight, CircleCheck, Remove } from '@element-plus/icons-vue'
import type { Task } from '@/types/task'
import { complexityOptions, labelOf, taskTypeOptions } from '@/constants/options'
import { formatTime } from '@/utils/format'

defineProps<{ tasks: Task[] }>()
defineEmits<{ detail: [id: number] }>()
</script>

<style scoped>
.task-title-cell {
  display: flex;
  gap: 12px;
  align-items: center;
}

.task-id {
  display: inline-flex;
  min-width: 46px;
  height: 30px;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  background: #eef4ff;
  color: var(--app-primary);
  font-size: 12px;
  font-weight: 900;
}

.task-title-cell strong {
  display: block;
  color: var(--app-text);
}

.task-title-cell span:last-child {
  display: block;
  max-width: 520px;
  overflow: hidden;
  margin-top: 3px;
  color: var(--app-muted);
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.type-chip {
  display: inline-flex;
  min-height: 28px;
  align-items: center;
  padding: 0 10px;
  border-radius: 999px;
  background: #f1f5f9;
  color: #475569;
  font-size: 12px;
  font-weight: 800;
}

.check-state {
  display: inline-flex;
  gap: 5px;
  align-items: center;
  font-weight: 600;
}

.check-state--done {
  color: #20a857;
}

.check-state--todo {
  color: #64748b;
}
</style>
