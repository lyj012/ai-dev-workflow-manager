export function formatTime(value?: string | null) {
  return value ? value.replace('T', ' ') : '-'
}
