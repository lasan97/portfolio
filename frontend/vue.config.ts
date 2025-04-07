import { defineConfig } from '@vue/cli-service'
import path from 'path'
import webpack from 'webpack'

export default defineConfig({
  transpileDependencies: true,
  chainWebpack: (config) => {
    // FSD 아키텍처 경로 별칭 설정
    config.resolve.alias
      .set('@', path.resolve(__dirname, 'src'))
      .set('@app', path.resolve(__dirname, 'src/app'))
      .set('@processes', path.resolve(__dirname, 'src/processes'))
      .set('@pages', path.resolve(__dirname, 'src/pages'))
      .set('@widgets', path.resolve(__dirname, 'src/widgets'))
      .set('@features', path.resolve(__dirname, 'src/features'))
      .set('@entities', path.resolve(__dirname, 'src/entities'))
      .set('@shared', path.resolve(__dirname, 'src/shared'));
    
    // Vue 옵션 설정
    config.plugin('define')
      .tap(args => {
        Object.assign(args[0], {
          __VUE_OPTIONS_API__: JSON.stringify(true),
          __VUE_PROD_DEVTOOLS__: JSON.stringify(false)
        });
        return args;
      });
  }
})
