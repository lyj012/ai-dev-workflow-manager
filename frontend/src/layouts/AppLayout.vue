<template>
  <el-container class="app-shell">
    <el-aside class="app-sidebar" width="232px">
      <div class="brand">
        <span class="brand-mark">AI</span>
        <div>
          <div class="brand-title">开发工作流</div>
          <div class="brand-subtitle">交付复盘工具</div>
        </div>
      </div>
      <el-menu :default-active="activeMenu" router class="nav-menu">
        <el-menu-item index="/dashboard">
          <el-icon><DataBoard /></el-icon>
          <span>概览</span>
        </el-menu-item>
        <el-menu-item index="/tasks">
          <el-icon><Tickets /></el-icon>
          <span>任务管理</span>
        </el-menu-item>
        <el-menu-item index="/templates">
          <el-icon><Collection /></el-icon>
          <span>Workflow 模板</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="app-header">
        <div class="header-title">{{ currentTitle }}</div>
        <el-tag effect="plain" type="info">Mock 优先</el-tag>
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
import { Collection, DataBoard, Tickets } from '@element-plus/icons-vue'

const route = useRoute()
const activeMenu = computed(() => {
  if (route.path.startsWith('/dashboard')) return '/dashboard'
  if (route.path.startsWith('/templates')) return '/templates'
  return '/tasks'
})
const currentTitle = computed(() => (route.meta.title as string) || 'AI 开发工作流管理平台')
</script>
