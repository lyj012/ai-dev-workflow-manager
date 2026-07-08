import { createRouter, createWebHistory } from 'vue-router'

import AppLayout from '@/layouts/AppLayout.vue'
import AiTaskDetailView from '@/views/ai-tasks/AiTaskDetailView.vue'
import AiTaskListView from '@/views/ai-tasks/AiTaskListView.vue'
import DashboardView from '@/views/dashboard/DashboardView.vue'
import DeliveryPreviewView from '@/views/delivery/DeliveryPreviewView.vue'
import TaskCreateView from '@/views/tasks/TaskCreateView.vue'
import TaskDetailView from '@/views/tasks/TaskDetailView.vue'
import TaskListView from '@/views/tasks/TaskListView.vue'
import TemplateDetailView from '@/views/templates/TemplateDetailView.vue'
import TemplateListView from '@/views/templates/TemplateListView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: AppLayout,
      children: [
        { path: '', redirect: '/ai-tasks' },
        { path: 'dashboard', name: 'dashboard', component: DashboardView, meta: { title: '概览' } },
        { path: 'ai-tasks', name: 'ai-tasks', component: AiTaskListView, meta: { title: 'AI 任务' } },
        {
          path: 'ai-tasks/:taskId',
          name: 'ai-task-detail',
          component: AiTaskDetailView,
          props: true,
          meta: { title: 'AI 任务详情' }
        },
        { path: 'tasks', name: 'tasks', component: TaskListView, meta: { title: '任务管理' } },
        { path: 'tasks/create', name: 'task-create', component: TaskCreateView, meta: { title: '创建任务' } },
        { path: 'tasks/:taskId', name: 'task-detail', component: TaskDetailView, props: true, meta: { title: '任务详情' } },
        { path: 'templates', name: 'templates', component: TemplateListView, meta: { title: 'Workflow 模板' } },
        {
          path: 'templates/:templateId',
          name: 'template-detail',
          component: TemplateDetailView,
          props: true,
          meta: { title: '模板详情' }
        },
        {
          path: 'delivery/:taskId',
          name: 'delivery-preview',
          component: DeliveryPreviewView,
          props: true,
          meta: { title: '交付预览' }
        }
      ]
    }
  ]
})

export default router
