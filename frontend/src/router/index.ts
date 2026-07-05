import { createRouter, createWebHistory } from 'vue-router'

import AppLayout from '@/layouts/AppLayout.vue'
import TaskCreateView from '@/views/tasks/TaskCreateView.vue'
import TaskDetailView from '@/views/tasks/TaskDetailView.vue'
import TaskListView from '@/views/tasks/TaskListView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: AppLayout,
      children: [
        { path: '', redirect: '/tasks' },
        { path: 'tasks', name: 'tasks', component: TaskListView },
        { path: 'tasks/create', name: 'task-create', component: TaskCreateView },
        { path: 'tasks/:taskId', name: 'task-detail', component: TaskDetailView, props: true }
      ]
    }
  ]
})

export default router
