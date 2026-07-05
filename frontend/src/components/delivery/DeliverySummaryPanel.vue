<template>
  <el-descriptions :column="1" border>
    <el-descriptions-item label="测试结果">{{ record.testResult || '-' }}</el-descriptions-item>
    <el-descriptions-item label="风险说明">{{ record.riskNotes || '-' }}</el-descriptions-item>
    <el-descriptions-item label="未验证范围">{{ record.unverifiedScope || '-' }}</el-descriptions-item>
    <el-descriptions-item label="交付总结">{{ record.summary || '-' }}</el-descriptions-item>
  </el-descriptions>
</template>

<script setup lang="ts">
import type { DeliveryRecord } from '@/types/delivery'

defineProps<{ record: DeliveryRecord }>()
</script>
