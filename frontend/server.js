const fs = require('fs');
const path = require('path');
const express = require('express');
const { createServer: createViteServer } = require('vite');

async function createServer() {
  const app = express();

  // Vite 서버를 미들웨어 모드로 생성
  const vite = await createViteServer({
    server: { middlewareMode: true },
    appType: 'custom'
  });

  // Vite의 미들웨어 사용
  app.use(vite.middlewares);

  // SSR을 적용할 경로 목록
  const SSR_ROUTES = ['/ssr'];

  // 모든 요청 처리
  app.use('*', async (req, res, next) => {
    const url = req.originalUrl;
    
    // SSR을 적용할 경로인지 확인
    const shouldSSR = SSR_ROUTES.some(route => url.startsWith(route));
    
    try {
      // index.html 읽기
      let template = fs.readFileSync(
        path.resolve(__dirname, 'index.html'),
        'utf-8'
      );
      
      // Vite를 통해 HTML 변환
      template = await vite.transformIndexHtml(url, template);
      
      if (shouldSSR) {
        // SSR 처리
        console.log(`SSR 렌더링: ${url}`);
        
        // 서버 측 진입점 로드
        const { render } = await vite.ssrLoadModule('/src/entry-server.ts');
        
        // 렌더링된 HTML 생성
        const { appHtml, preloadLinks, initialState } = await render(url);
        
        // 템플릿에 렌더링된 HTML 삽입
        const html = template
          .replace('<!--preload-links-->', preloadLinks)
          .replace('<div id="app"></div>', `<div id="app">${appHtml}</div>`)
          .replace('<!--initial-state-->', `<script>window.__INITIAL_STATE__=${JSON.stringify(initialState || {})}</script>`);
        
        // HTML 응답 전송
        res.status(200).set({ 'Content-Type': 'text/html' }).end(html);
      } else {
        // CSR 처리 - index.html을 그대로 제공
        console.log(`CSR 렌더링: ${url}`);
        res.status(200).set({ 'Content-Type': 'text/html' }).end(template);
      }
    } catch (e) {
      // 오류를 Vite에게 전달하여 더 나은 스택 추적 제공
      vite.ssrFixStacktrace(e);
      console.error(e);
      next(e);
    }
  });

  app.listen(3000, () => {
    console.log('하이브리드 CSR/SSR 서버가 http://localhost:3000 에서 실행 중입니다');
  });
}

createServer();
