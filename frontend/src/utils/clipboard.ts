import { ElMessage } from 'element-plus'

export async function copyText(text?: string) {
  if (!text) {
    ElMessage.warning('暂无可复制内容')
    return
  }
  await navigator.clipboard.writeText(text)
  ElMessage.success('已复制')
}
