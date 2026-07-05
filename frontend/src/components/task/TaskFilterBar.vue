<template>
  <div class="section">
    <div class="section-body">
      <el-form :inline="true" :model="model" class="toolbar">
        <el-form-item label="任务状态">
          <el-select v-model="model.status" clearable placeholder="全部状态" style="width: 150px">
            <el-option v-for="item in taskStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="任务类型">
          <el-select v-model="model.taskType" clearable placeholder="全部类型" style="width: 150px">
            <el-option v-for="item in taskTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="复杂度">
          <el-select v-model="model.complexity" clearable placeholder="全部复杂度" style="width: 150px">
            <el-option v-for="item in complexityOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="风险标签">
          <el-select v-model="model.riskTag" clearable placeholder="全部风险" style="width: 160px">
            <el-option v-for="item in riskTagOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="$emit('search')">查询</el-button>
          <el-button :icon="Refresh" @click="$emit('reset')">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Refresh, Search } from '@element-plus/icons-vue'
import type { TaskQuery } from '@/types/task'
import { complexityOptions, riskTagOptions, taskStatusOptions, taskTypeOptions } from '@/constants/options'

defineProps<{ model: TaskQuery }>()
defineEmits<{ search: []; reset: [] }>()
</script>
