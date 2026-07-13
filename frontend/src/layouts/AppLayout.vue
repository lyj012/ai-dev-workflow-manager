<template>
  <el-container class="app-shell">
    <el-aside class="app-sidebar" width="290px">
      <div class="brand">
        <span class="brand-mark">A</span>
        <div>
          <div class="brand-title">开发工作流</div>
          <div class="brand-subtitle">AI 异步任务平台</div>
        </div>
      </div>
      <div class="nav-label">工作台</div>
      <el-menu :default-active="activeMenu" router class="nav-menu">
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <span>概览</span>
        </el-menu-item>
        <el-menu-item index="/ai-tasks">
          <el-icon><Cpu /></el-icon>
          <span>AI 任务</span>
          <span class="nav-badge">4</span>
        </el-menu-item>
        <el-menu-item index="/tasks">
          <el-icon><Document /></el-icon>
          <span>任务管理</span>
        </el-menu-item>
        <el-menu-item index="/templates">
          <el-icon><Share /></el-icon>
          <span>Workflow 模板</span>
        </el-menu-item>
      </el-menu>
      <div class="runtime-card">
        <div class="runtime-row"><span class="runtime-dot" />Mock 运行环境</div>
        <p>保留真实接口字段，支持本地 mock 演示。</p>
      </div>
      <div class="team-panel">
        <span class="team-avatar">AI</span>
        <div>
          <div class="team-title">开发团队</div>
          <div class="team-subtitle">团队空间</div>
        </div>
        <el-icon class="team-icon"><ArrowUpBold /></el-icon>
      </div>
    </el-aside>

    <el-container>
      <el-header class="app-header">
        <div class="breadcrumb">
          <span>开发工作流</span>
          <span>/</span>
          <strong>{{ currentTitle }}</strong>
        </div>
        <div class="mock-pill"><span class="mock-dot" />Mock 优先</div>
      </el-header>
      <el-main class="app-main">
        <RouterView />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { ArrowUpBold, Cpu, Document, HomeFilled, Share } from '@element-plus/icons-vue'

const route = useRoute()
const activeMenu = computed(() => {
  if (route.path.startsWith('/dashboard')) return '/dashboard'
  if (route.path.startsWith('/ai-tasks')) return '/ai-tasks'
  if (route.path.startsWith('/templates')) return '/templates'
  return '/tasks'
})
const currentTitle = computed(() => (route.meta.title as string) || 'AI 开发工作流管理平台')
</script>
