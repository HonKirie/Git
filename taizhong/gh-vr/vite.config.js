import { fileURLToPath, URL } from 'node:url'
import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import cesium from 'vite-plugin-cesium'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  // 加载 .env 文件中的环境变量（vite.config.js 中 process.env 不会自动拿到 VITE_ 变量）
  const env = loadEnv(mode, process.cwd(), '')
  const tiandituProxy = {
    target: env.VITE_TIANDITU_SERVER || 'https://t0.tianditu.gov.cn',
    changeOrigin: true,
    rewrite: (path) => path.replace(/^\/tianditu/, ''),
  }

  return {
    plugins: [
      vue(),
      cesium(),
    ],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url)),
      },
    },
    server: {
      host: '0.0.0.0', // 监听所有网络接口，允许局域网通过 IP 访问
      port: 5173,
      proxy: {
        // VR 网关代理：前端访问 /vr-api/xxx 会被代理到 VITE_VR_SERVER/xxx
        '/vr-api': {
          target: env.VITE_VR_SERVER,
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/vr-api/, ''),
          configure: (proxy) => {
            proxy.on('proxyReq', (proxyReq, req) => {
              const target = env.VITE_VR_SERVER
              console.log(`[proxy] ${req.method} ${req.url} → ${target}${proxyReq.path}`)
            })
          },
        },
        // 天地图代理（解决跨域/CORS 问题）
        '/tianditu': tiandituProxy,
      },
    },
    // 让 pnpm preview 继续使用天地图代理；生产静态服务器仍需配置反向代理。
    preview: {
      proxy: {
        '/tianditu': tiandituProxy,
      },
    },
    build: {
      chunkSizeWarningLimit: 5000,
    },
  }
})
