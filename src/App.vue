<template>
  <RouterView />
</template>

<script setup>
import { RouterView } from 'vue-router'
</script>

<style>
/* 此处的样式是全局的，会应用到整个应用。
  我们移除所有默认边距，并确保 #app 容器占满屏幕。
*/
html, body, #app {
  height: 100%;
  width: 100%;
  margin: 0;
  padding: 0;
  overflow: hidden; /* 防止出现意外的滚动条 */
}
</style>