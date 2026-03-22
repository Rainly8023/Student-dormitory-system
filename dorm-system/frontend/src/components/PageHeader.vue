<template>
  <div class="page-header">
    <div>
      <h2 class="page-title">{{ title }}</h2>
      <p v-if="subtitle" class="page-sub">{{ subtitle }}</p>
    </div>
    <div class="page-actions">
      <slot />
    </div>
  </div>
</template>

<script setup>
defineProps({ title: String, subtitle: String })
</script>

<style scoped>
.page-header {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 20px;
}
.page-title { font-size: 20px; font-weight: 700; color: #111827; }
.page-sub   { font-size: 13px; color: #6b7280; margin-top: 4px; }
.page-actions { display: flex; gap: 10px; align-items: center; }
</style>
