<template>
  <el-container class="app-shell">
    <el-aside class="app-sidebar" width="290px">
      <div class="brand">
        <span class="brand-mark">A</span>
        <div class="brand-copy">
          <div class="brand-title">开发工作流</div>
          <div class="brand-subtitle">交付复盘工具</div>
        </div>
      </div>
      <el-menu :default-active="activeMenu" router class="nav-menu">
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <span>概览</span>
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
        <div class="header-title">{{ currentTitle }}</div>
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
import { ArrowUpBold, Document, HomeFilled, Share } from '@element-plus/icons-vue'

const route = useRoute()
const activeMenu = computed(() => {
  if (route.path.startsWith('/dashboard')) return '/dashboard'
  if (route.path.startsWith('/templates')) return '/templates'
  return '/tasks'
})
const currentTitle = computed(() => (route.meta.title as string) || 'AI 开发工作流管理平台')
</script>
