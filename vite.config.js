import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    vue(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    proxy: {
      // 代理AGV相关的API请求
      '/prod-api': {
        target: 'http://192.168.2.57',
        changeOrigin: true,
      },
      // 代理摄像头相关的API请求
      '/easy-api': {
        target: 'http://192.168.2.57',
        changeOrigin: true,
      },
      // **新增：代理视频流相关的请求**
      '/live': {
        target: 'http://192.168.2.57',
        changeOrigin: true,
      }
    }
  }
})
