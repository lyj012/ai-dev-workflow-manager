<template>
  <div class="page-header">
    <div>
      <h1 class="page-title">{{ title }}</h1>
      <div v-if="description" class="page-description">{{ description }}</div>
    </div>
    <div v-if="$slots.actions" class="page-actions">
      <slot name="actions" />
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  title: string
  description?: string
}>()
</script>
