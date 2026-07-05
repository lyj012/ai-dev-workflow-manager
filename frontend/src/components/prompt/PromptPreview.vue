<template>
  <pre class="code-block">{{ content || '选择阶段后生成 Prompt。' }}</pre>
</template>

<script setup lang="ts">
defineProps<{ content?: string }>()
</script>
