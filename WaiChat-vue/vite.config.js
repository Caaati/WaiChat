// vite.config.js
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  server: {
    host: '0.0.0.0', // 允许局域网访问，方便手机端调试
    port: 5173,      // 前端开发端口
    proxy: {
      // 1. API 代理：将本地 http://localhost:5173/api/xxx
      // 转发到 http://127.0.0.1:8080/xxx
      '/api': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true,
        // 关键：这里要和 Nginx 的 rewrite 逻辑保持一致
        rewrite: (path) => path.replace(/^\/api/, '')
      },

      // 2. 静态资源代理 (重要)：
      // 解决开发环境下无法访问服务器上 /uploads 语音文件的问题
      '/uploads': {
        // 如果你本地没有语音文件，可以直接指向线上服务器的域名
        target: 'http://127.0.0.1:8080',
        changeOrigin: true,
        secure: false // 如果证书校验有问题可以设为 false
      },

      // 3. WebSocket 代理
      // 使用更通用的匹配方式
      '/ws': {
        // 开发环境建议指向本地后端，如果是调试线上数据则指向域名
        target: 'ws://127.0.0.1:8080',
        ws: true,
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/ws/, '/ws') // 根据后端路径决定是否保留前缀
      }
    }
  },
  resolve: {
    alias: {
      '@': resolve(__dirname, './src'),
      '*': resolve('')
    },
  }
})
