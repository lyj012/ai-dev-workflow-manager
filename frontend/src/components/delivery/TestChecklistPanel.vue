<template>
  <pre class="plain-block compact-block">{{ content || '尚未生成测试清单。' }}</pre>
</template>

<script setup lang="ts">
defineProps<{ content?: string }>()
</script>
