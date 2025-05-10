// server.prod.js 내용
const fs = require('fs');
const path = require('path');
const express = require('express');

async function createProdServer() {
    const app = express();

    const PORT = process.env.VITE_BASE_PORT || 3000;
    const BASE_URL = process.env.VITE_BASE_URL || 'http://localhost:3000';

    // 정적 파일 제공
    app.use(express.static(path.resolve(__dirname, 'dist/client')));

    // SSR 처리
    app.use('*', async (req, res) => {
        try {
            const url = req.originalUrl;

            // 템플릿 HTML 읽기
            const template = fs.readFileSync(
                path.resolve(__dirname, 'dist/client/index.html'),
                'utf-8'
            );

            // 서버 엔트리 로드 (ESM으로 작성되어 있어 동적 import 사용)
            const serverEntryPath = path.resolve(__dirname, 'dist/server/entry-server.mjs');
            const serverEntry = await import(`file://${serverEntryPath}`);
            const { render, router } = serverEntry;

            // 라우터 초기화
            await router.push(url);
            await router.isReady();

            const shouldSSR = router?.currentRoute?.value?.meta?.ssr === true;

            if (shouldSSR) {
                try {
                    const cookieHeader = req.headers.cookie || '';

                    const { appHtml, preloadLinks, initialState } = await render(url, {
                        cookie: cookieHeader,
                        headers: req.headers
                    });

                    const html = template
                        .replace('<!--preload-links-->', preloadLinks || '')
                        .replace('<!--ssr-outlet-->', appHtml)
                        .replace(
                            '<!--initial-state-->',
                            `<script>window.__INITIAL_STATE__=${JSON.stringify(initialState || {})}</script>`
                        );

                    res.status(200).set({ 'Content-Type': 'text/html' }).end(html);
                } catch (e) {
                    console.error('SSR 렌더링 오류:', e, e.stack);
                    res.status(200).set({ 'Content-Type': 'text/html' }).end(template);
                }
            } else {
                res.status(200).set({ 'Content-Type': 'text/html' }).end(template);
            }
        } catch (e) {
            console.error('서버 오류:', e, e.stack);
            res.status(500).end('서버 오류');
        }
    });

    app.listen(PORT, () => {
        console.log(`\n  🚀 SSR 서버가 ${BASE_URL} 에서 실행 중입니다`);
        console.log(`\n  🚀 서버 포트: ${PORT}`);
    });
}

createProdServer().catch(err => console.error(err, err.stack));