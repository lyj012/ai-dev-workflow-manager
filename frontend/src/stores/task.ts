import { defineStore } from 'pinia'

import type { TaskDetail } from '@/types/task'

export const useTaskStore = defineStore('task', {
  state: () => ({
    currentTask: undefined as TaskDetail | undefined
  }),
  actions: {
    setCurrentTask(task?: TaskDetail) {
      this.currentTask = task
    }
  }
})
