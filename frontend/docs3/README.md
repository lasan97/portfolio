# FSD 아키텍처 개발 문서

이 문서는 Feature-Sliced Design(FSD) 아키텍처를 기반으로 한 프론트엔드 개발 가이드를 제공합니다. 개발자와 AI가 프로젝트를 일관성 있게 유지보수하고 추가 개발하는 데 참고할 수 있습니다.

## 문서 목록

1. [FSD 아키텍처 개요](./fsd-architecture-overview.md) - FSD 아키텍처의 상세 개요 및 구현 예시
2. [FSD 아키텍처 가이드](./fsd-architecture-guide.md) - FSD 아키텍처의 기본 개념과 구조
3. [Enum 패턴 가이드](./enum-patterns.md) - Enum과 Namespace를 활용한 개발 패턴
4. [컴포넌트 개발 가이드](./component-development.md) - 레이어별 컴포넌트 개발 방법
5. [API 통합 가이드](./api-integration.md) - FSD 아키텍처에서의 API 통합 방법
6. [개선 사항](./개선사항.md) - FSD 아키텍처 개선을 위한 제안

## 주요 레이어 구조

```
src/
├── app/         # 애플리케이션 설정, 스타일, 라우팅
├── processes/   # 비즈니스 프로세스
├── pages/       # 페이지 컴포넌트
├── widgets/     # 독립적인 UI 블록
├── features/    # 사용자 상호작용 기능
├── entities/    # 비즈니스 엔티티
└── shared/      # 공유 유틸리티, 타입, API
```

## 핵심 원칙

1. **레이어 기반 구조**: 코드는 추상화 수준에 따라 레이어로 구성됩니다.
2. **단방향 의존성**: 상위 레이어는 하위 레이어에만 의존할 수 있습니다.
3. **슬라이스 독립성**: 각 슬라이스는 독립적으로 작동해야 합니다.
4. **공개 API**: 각 슬라이스는 명확한 공개 API를 제공해야 합니다.

## 참고 자료

- [Feature-Sliced Design 공식 문서](https://feature-sliced.design/)
- [Vue.js 공식 문서](https://vuejs.org/)
- [Pinia 상태 관리 라이브러리](https://pinia.vuejs.org/)
