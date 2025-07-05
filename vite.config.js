// vite.config.js
import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// --- 新增引入 ---
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
// --- 新增引入结束 ---

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    // --- 新增配置 ---
    AutoImport({
      resolvers: [ElementPlusResolver()],
    }),
    Components({
      resolvers: [ElementPlusResolver()],
    }),
    // --- 新增配置结束 ---
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  // 【恢复】添加代理配置，解决CORS跨域问题
  server: {
    host: '0.0.0.0',
    port: 5173,
    proxy: {
      // 代理小车API请求
      '/prod-api': {
        target: 'http://192.168.2.57',
        changeOrigin: true,
      },
      // 代理本地API请求
      '/local-api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      }
    }
  }
})
