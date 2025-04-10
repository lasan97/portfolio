const fs = require('fs');
const path = require('path');
const express = require('express');
const { createServer: createViteServer, loadEnv} = require('vite');

/**
 * 개발 환경에서 사용할 SSR 서버
 */
async function createDevSsrServer() {
  const app = express();
  
  // 개발 환경 표시 (process.env.NODE_ENV로 구분할 수도 있음)
  const isDev = true;

  const env = loadEnv(process.env.NODE_ENV, process.cwd(), '');
  const isSsrDebugMode = env.VITE_SSR_DEBUG === true;

  // Vite 서버를 미들웨어 모드로 생성
  const vite = await createViteServer({
    server: { middlewareMode: true },
    appType: 'custom',
    logLevel: isSsrDebugMode ? 'info' : 'error', // VITE_SSR_DEBUG 환경변수로 로깅 조절
  });

  // Vite의 미들웨어 사용
  app.use(vite.middlewares);
  
  // 모든 요청 처리
  app.use('*', async (req, res, next) => {
    const url = req.originalUrl;
    // SSR 경로인지 확인 (페이지 새로고침 시에도 SSR 수행)

    try {
      // index.html 읽기
      let template = fs.readFileSync(
        path.resolve(__dirname, 'index.html'),
        'utf-8'
      );
      
      // Vite를 통해 HTML 변환 (HMR, ESM 및 기타 변환 적용)
      template = await vite.transformIndexHtml(url, template);

      // entry-server 모듈 로드
      const { render, router } = await vite.ssrLoadModule('/src/entry-server.ts');

      // 라우터를 URL로 이동
      await router.push(url);
      await router.isReady();

      const shouldSSR = router.currentRoute.value.meta.ssr === true;

      // SSR이 필요한 경로만 서버 렌더링 수행
      if (shouldSSR) {
        try {
          // 쿠키 및 요청 헤더 전달
          const cookieHeader = req.headers.cookie || '';
          
          // 디버깅 로그
          if (isDev && isSsrDebugMode) {
            console.log(`SSR 쿠키 헤더: ${cookieHeader}`);
            console.log(`SSR 요청 URL: ${url}`);
            console.log(`SSR 요청 헤더:`, req.headers);
          }
          
          // 렌더링된 HTML 가져오기 (쿠키 및 헤더 포함)
          const { appHtml, preloadLinks, initialState } = await render(url, { 
            cookie: cookieHeader,
            headers: req.headers
          });
          
          // HTML에 렌더링된 앱 삽입
          const html = template
            .replace('<!--preload-links-->', preloadLinks || '')
            .replace('<!--ssr-outlet-->', appHtml)
            .replace(
              '<!--initial-state-->',
              `<script>window.__INITIAL_STATE__=${JSON.stringify(initialState || {})}</script>`
            );
          
          // 로그 출력 (개발 환경에서만)
          if (isDev && isSsrDebugMode) {
            console.log(`SSR 렌더링: ${url}`);
          }
          
          res.status(200).set({ 'Content-Type': 'text/html' }).end(html);
        } catch (e) {
          // 렌더링 오류 처리
          vite.ssrFixStacktrace(e);
          if (isSsrDebugMode) {
            console.error('SSR 렌더링 오류:', e);
          }
          
          // 오류 발생 시 CSR로 폴백
          res.status(200).set({ 'Content-Type': 'text/html' }).end(template);
        }
      } else {
        // CSR 경로는 클라이언트에서 렌더링
        if (isDev && isSsrDebugMode) {
          console.log(`CSR 렌더링: ${url}`);
        }
        
        // 초기 상태 없이 빈 앱 컨테이너만 제공
        res.status(200).set({ 'Content-Type': 'text/html' }).end(template);
      }
    } catch (e) {
      // 기타 오류 처리
      vite.ssrFixStacktrace(e);
      if (isSsrDebugMode) {
        console.error('서버 오류:', e);
      }
      next(e);
    }
  });
  
  // 서버 시작
  const port = env.VITE_BASE_PORT;
  app.listen(port, () => {
    console.log(`\n  🚀 하이브리드 CSR/SSR 서버가 ${env.VITE_BASE_URL} 에서 실행 중입니다`);
    console.log(`\n  🚀 서버 포트: ${port}`);
    if (isDev && isSsrDebugMode) {
      console.log(`\n  🚀 렌더링타입 디버그 모드`);
    }
    if (!isSsrDebugMode) {
      console.log(`  👀 자세한 로그는 VITE_SSR_DEBUG=true 환경변수를 설정하세요`);
    }
    console.log('');
  });
}

// 서버 실행
createDevSsrServer();
