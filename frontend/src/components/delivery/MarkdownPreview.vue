<template>
  <pre class="plain-block markdown-block">{{ content || '暂无 Markdown 内容。' }}</pre>
</template>

<script setup lang="ts">
defineProps<{ content?: string }>()
</script>
