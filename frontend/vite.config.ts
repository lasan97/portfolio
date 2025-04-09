import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

// https://vitejs.dev/config/
export default defineConfig(({ mode, command }) => {
  const isSSR = process.env.SSR === 'true'
  
  return {
    plugins: [vue()],
    resolve: {
      alias: {
        '@app': path.resolve(__dirname, './src/app'),
        '@processes': path.resolve(__dirname, './src/processes'),
        '@pages': path.resolve(__dirname, './src/pages'),
        '@widgets': path.resolve(__dirname, './src/widgets'),
        '@features': path.resolve(__dirname, './src/features'),
        '@entities': path.resolve(__dirname, './src/entities'),
        '@shared': path.resolve(__dirname, './src/shared')
      }
    },
    build: {
      minify: mode === 'production',
      sourcemap: mode !== 'production',
      ssrManifest: isSSR,
      outDir: isSSR ? 'dist/server' : 'dist/client',
    },
    server: {
      port: 8080,
      open: true
    },
    preview: {
      port: 8080
    },
    css: {
      postcss: './postcss.config.js'
    }
  }
})