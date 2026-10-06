import { fileURLToPath, URL } from 'node:url'
import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const target = env.VITE_PROXY_TARGET || 'http://localhost:8123'

  const proxy = {
    '/api': {
      target,
      changeOrigin: true,
      ws: true,
      // SSE 场景下关闭压缩/缓冲，保证服务端分片能实时透传
      configure: (proxyServer) => {
        proxyServer.on('proxyReq', (proxyReq) => {
          proxyReq.setHeader('Accept-Encoding', 'identity')
          proxyReq.setHeader('Cache-Control', 'no-cache')
        })
      },
    },
  }

  return {
    plugins: [vue()],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url)),
      },
    },
    server: {
      host: true,
      port: 5173,
      open: false,
      proxy,
    },
    preview: {
      host: true,
      port: 4173,
      proxy,
    },
  }
})
