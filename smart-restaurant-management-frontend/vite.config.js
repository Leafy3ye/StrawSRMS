import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'
import compression from 'vite-plugin-compression'

export default defineConfig({
  plugins: [
    vue(),
    // 添加Gzip压缩插件
    compression({
      algorithm: 'gzip',
      ext: '.gz',
      threshold: 1024,
      deleteOriginFile: false
    })
  ],
  base: '/',
  build: {
    target: 'es2015',
    minify: 'terser',
    // 添加压缩配置
    terserOptions: {
      compress: {
        drop_console: true,
        drop_debugger: true
      }
    },
    rollupOptions: {
      input: {
        main: resolve(__dirname, 'index.html')
      },
      output: {
        // 分包策略
        manualChunks: {
          vendor: ['vue', 'vue-router', 'element-plus'],
          pinyin: ['pinyin-pro']
        }
      }
    },
    // 启用资源压缩
    assetsInlineLimit: 4096
  },
  optimizeDeps: {
    include: ['vue', 'vue-router', 'element-plus', 'pinyin']
  },
  server: {
    host: '0.0.0.0',
    port: 5173,
    historyApiFallback: {
      rewrites: [
        { from: /^\/customer\/.*$/, to: '/index.html' }
      ]
    }
  },
  preview: {
    host: '0.0.0.0',
    port: 5173,
    historyApiFallback: {
      rewrites: [
        { from: /^\/customer\/.*$/, to: '/index.html' }
      ]
    }
  }
})