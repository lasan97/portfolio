# SSR 구현

이 디렉토리는 SSR(Server-Side Rendering) 구현과 관련된 코드를 포함합니다.

SSR에 대한 자세한 가이드와 구현 방법은 `/docs/ssr-guide.md` 문서를 참조하세요.

## 주요 파일 구조

- `index.ts`: SSR 페이지 모듈 진입점
- `ui/`: SSR 관련 UI 컴포넌트

## 참고사항

SSR 구현 시 주의해야 할 사항:

1. 브라우저 API에 접근할 때는 `typeof window !== 'undefined'` 확인
2. 컴포넌트에서 데이터 페칭 시 `onServerPrefetch` 훅 사용
3. Pinia 스토어 상태가 서버에서 클라이언트로 전달되도록 설정
