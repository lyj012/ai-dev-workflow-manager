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
        <el-menu-item index="/tasks">
          <el-icon><Tickets /></el-icon>
          <span>任务管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="app-header">
        <div class="header-title">AI 开发工作流管理平台</div>
        <el-tag effect="plain" type="info">MVP 主链路</el-tag>
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
import { Tickets } from '@element-plus/icons-vue'

const route = useRoute()
const activeMenu = computed(() => (route.path.startsWith('/tasks') ? '/tasks' : route.path))
</script>
