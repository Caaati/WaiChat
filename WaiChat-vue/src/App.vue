<script setup>
import { RouterLink, RouterView } from 'vue-router'
</script>

<template>
  <div class="app-shell">
    <div class="router-fill">
      <RouterView />
    </div>
  </div>
</template>

<style scoped>
.app-shell {
  height: 100%;
  min-height: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.router-fill {
  flex: 1 1 auto;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 只负责占满高度，不要写死 flex-direction，否则会压扁聊天页横向布局（侧栏+主区域） */
.router-fill > :deep(*) {
  flex: 1 1 auto;
  min-height: 0;
  overflow: hidden;
}
</style>
