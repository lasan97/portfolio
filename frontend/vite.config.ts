import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

// https://vitejs.dev/config/
export default defineConfig(({ mode, command }) => {
  const env = loadEnv(mode, process.cwd(), '');
  const isSSR = process.env.SSR === 'true'
  const port = Number(env.VITE_BASE_PORT || 8080);



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
      port: port,
      open: false
    },
    preview: {
      port: port
    },
    css: {
      postcss: './postcss.config.js'
    }
  }
})