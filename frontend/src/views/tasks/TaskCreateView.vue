<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">创建任务</h1>
      <el-button :icon="ArrowLeft" @click="router.push('/tasks')">返回列表</el-button>
    </div>

    <div class="section create-panel">
      <div class="section-body">
        <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
          <el-form-item label="标题" prop="title">
            <el-input v-model="form.title" maxlength="200" show-word-limit placeholder="输入 AI 开发任务标题" />
          </el-form-item>
          <el-form-item label="描述" prop="description">
            <el-input
              v-model="form.description"
              type="textarea"
              :rows="7"
              placeholder="描述需求目标、背景、约束和期望输出"
            />
          </el-form-item>
          <el-form-item label="任务类型" prop="taskType">
            <el-select v-model="form.taskType" placeholder="选择任务类型" style="width: 260px">
              <el-option v-for="item in taskTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="复杂度" prop="complexity">
            <el-segmented v-model="form.complexity" :options="complexityOptions" />
          </el-form-item>
          <el-form-item label="风险标签">
            <el-select v-model="form.riskTags" multiple clearable placeholder="可选" style="width: 520px">
              <el-option v-for="item in riskTagOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="submitting" @click="submit">保存任务</el-button>
            <el-button @click="router.push('/tasks')">取消</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'

import { createTask } from '@/api/task'
import type { Complexity, RiskTag, TaskType } from '@/types/task'
import { complexityOptions, riskTagOptions, taskTypeOptions } from '@/utils/options'

interface TaskForm {
  title: string
  description: string
  taskType?: TaskType
  complexity?: Complexity
  riskTags: RiskTag[]
}

const router = useRouter()
const formRef = ref<FormInstance>()
const submitting = ref(false)
const form = reactive<TaskForm>({
  title: '',
  description: '',
  taskType: undefined,
  complexity: undefined,
  riskTags: []
})

const rules: FormRules<TaskForm> = {
  title: [
    { required: true, message: '请输入任务标题', trigger: 'blur' },
    { max: 200, message: '标题最多 200 字符', trigger: 'blur' }
  ],
  taskType: [{ required: true, message: '请选择任务类型', trigger: 'change' }],
  complexity: [{ required: true, message: '请选择复杂度', trigger: 'change' }]
}

async function submit() {
  await formRef.value?.validate()
  submitting.value = true
  try {
    const task = await createTask({
      title: form.title,
      description: form.description,
      taskType: form.taskType as TaskType,
      complexity: form.complexity as Complexity,
      riskTags: form.riskTags
    })
    ElMessage.success('任务创建成功')
    router.push(`/tasks/${task.id}`)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.create-panel {
  max-width: 860px;
}
</style>
