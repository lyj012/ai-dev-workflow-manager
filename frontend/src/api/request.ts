import axios, { AxiosError, type AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'

import type { ApiResponse } from '@/types/common'

const client = axios.create({
  baseURL: '/api/v1',
  timeout: 15000
})

function handleError(error: AxiosError<ApiResponse<unknown>>) {
  const message = error.response?.data?.message || error.message || '网络请求失败'
  ElMessage.error(message)
  return Promise.reject(new Error(message))
}

client.interceptors.response.use((response) => response, handleError)

function unwrap<T>(body: ApiResponse<T>): T {
  if (typeof body?.code === 'number' && body.code !== 0) {
    const message = body.message || '请求失败'
    ElMessage.error(message)
    throw new Error(message)
  }
  return body.data
}

const request = {
  async get<T>(url: string, config?: AxiosRequestConfig) {
    const response = await client.get<ApiResponse<T>>(url, config)
    return unwrap(response.data)
  },
  async post<T>(url: string, data?: unknown, config?: AxiosRequestConfig) {
    const response = await client.post<ApiResponse<T>>(url, data, config)
    return unwrap(response.data)
  },
  async put<T>(url: string, data?: unknown, config?: AxiosRequestConfig) {
    const response = await client.put<ApiResponse<T>>(url, data, config)
    return unwrap(response.data)
  },
  async delete<T>(url: string, config?: AxiosRequestConfig) {
    const response = await client.delete<ApiResponse<T>>(url, config)
    return unwrap(response.data)
  }
}

export default request

