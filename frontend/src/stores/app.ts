import { defineStore } from 'pinia'

import { USE_MOCK } from '@/constants/enums'

export const useAppStore = defineStore('app', {
  state: () => ({
    useMock: USE_MOCK
  })
})
