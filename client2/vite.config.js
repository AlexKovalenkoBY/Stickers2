import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

// https://vite.dev/config/
export default defineConfig({
  // Добавляем resolve.alias для работы с путями через @/
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
      '~': path.resolve(__dirname, './node_modules')
    }
  },
  plugins: [vue()],
  build: {
    outDir: path.resolve(__dirname, '../src/main/resources/static'), // Указывает путь к выходной директории
    emptyOutDir: false, // Очищает выходную директорию перед сборкой (опционально)
  }, 
  server: {
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '/api')
      }
    }
  }
})