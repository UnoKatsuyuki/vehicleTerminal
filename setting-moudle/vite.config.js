import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      // 字符串简写写法
      // '/foo': 'http://localhost:4567',
      // 选项写法
      '/prod-api': {
        target: 'http://192.168.2.57', // 目标后端服务地址 (已更新为文档中的正确地址)
        changeOrigin: true, // 需要虚拟主机站点
        // 如果你的后端api路径中不希望包含/prod-api，可以重写路径
        // rewrite: (path) => path.replace(/^\/prod-api/, '') 
      },
    }
  }
}) 