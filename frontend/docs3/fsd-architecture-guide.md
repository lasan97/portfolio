# Feature-Sliced Design 아키텍처 가이드

이 문서는 프로젝트에서 사용되는 Feature-Sliced Design(FSD) 아키텍처를 설명합니다. FSD는 프론트엔드 애플리케이션을 기능 중심으로 조직화하여 확장성과 유지보수성을 높이는 아키텍처 방법론입니다.

## 목차

1. [FSD 개요](#fsd-개요)
2. [레이어 구조](#레이어-구조)
3. [슬라이스 구성](#슬라이스-구성)
4. [세그먼트 구성](#세그먼트-구성)
5. [의존성 규칙](#의존성-규칙)
6. [공개 API 패턴](#공개-api-패턴)
7. [모범 사례](#모범-사례)

## FSD 개요

Feature-Sliced Design(FSD)은 프론트엔드 애플리케이션을 개발할 때 코드를 조직화하는 방법론으로, 다음과 같은 이점을 제공합니다:

- **확장성**: 애플리케이션이 커져도 코드 구조를 일관되게 유지할 수 있습니다.
- **명확한 경계**: 기능 간의 경계가 명확하여 책임 분리가 용이합니다.
- **개발 병렬화**: 팀 구성원이 서로 독립적으로 작업할 수 있습니다.
- **코드 재사용**: 공통 코드의 재사용이 용이합니다.
- **점진적 도입**: 기존 프로젝트에 점진적으로 도입할 수 있습니다.

## 레이어 구조

FSD는 다음과 같은 레이어 구조를 가집니다(상위 -> 하위):

### 1. app

애플리케이션의 핵심 설정과 진입점을 포함합니다.

```
app/
├── styles/        # 전역 스타일
├── store/         # 스토어 초기화
├── router/        # 라우터 설정
├── index.ts       # 애플리케이션 시작점
└── App.vue        # 루트 컴포넌트
```

### 2. processes

복잡한 비즈니스 프로세스와 전역 상태 관리를 담당합니다. 여러 페이지에 걸친 로직이나 워크플로우를 관리합니다.

```
processes/
└── index.ts      # 프로세스 내보내기
```

### 3. pages

라우트에 매핑되는 페이지 컴포넌트들을 포함합니다.

```
pages/
├── home/
│   ├── ui/        # 페이지 UI 컴포넌트
│   └── index.ts   # 공개 API
├── products/
│   ├── ui/        # 상품 페이지 UI
│   └── index.ts   # 공개 API
└── auth/
    ├── ui/        # 인증 페이지 UI
    └── index.ts   # 공개 API
```

### 4. widgets

독립적인 복합 UI 블록으로, 여러 기능과 엔티티를 조합합니다.

```
widgets/
├── header/
│   ├── ui/        # 헤더 UI 컴포넌트
│   └── index.ts   # 공개 API
└── footer/
    ├── ui/        # 푸터 UI 컴포넌트
    └── index.ts   # 공개 API
```

### 5. features

사용자 인터랙션과 관련된 기능을 구현합니다.

```
features/
├── auth/
│   ├── ui/        # 인증 UI 컴포넌트
│   ├── model/     # 인증 상태 및 로직
│   ├── api/       # 인증 API 요청
│   └── index.ts   # 공개 API
└── productManagement/
    ├── ui/        # 상품 관리 UI
    ├── model/     # 상태 및 로직
    ├── api/       # API 통신
    └── index.ts   # 공개 API
```

### 6. entities

비즈니스 도메인 모델과 관련 로직을 포함합니다.

```
entities/
├── user/
│   ├── ui/        # 사용자 UI 컴포넌트
│   ├── model/     # 사용자 모델 및 상태
│   ├── api/       # 사용자 API 통신
│   └── index.ts   # 공개 API
└── product/
    ├── ui/        # 상품 UI 컴포넌트
    ├── model/     # 상품 모델 및 상태
    ├── api/       # 상품 API 통신
    └── index.ts   # 공개 API
```

### 7. shared

특정 도메인이나 기능에 종속되지 않는 공통 코드를 포함합니다.

```
shared/
├── ui/           # 기본 UI 컴포넌트
├── api/          # API 클라이언트 및 유틸리티
├── lib/          # 유틸리티 함수
├── types/        # 공통 타입 정의
└── config/       # 상수 및 설정
```

## 슬라이스 구성

각 레이어는 슬라이스로 분할됩니다. 슬라이스는 특정 도메인 영역이나 기능을 담당합니다.

슬라이스 명명 규칙:
- 명사형: `user`, `product`, `order`
- 기능 설명: `auth`, `productManagement`, `checkout`

### 슬라이스 구조 예시 (product 엔티티)

```
entities/product/
├── ui/               # UI 컴포넌트
│   ├── ProductCard.vue
│   └── index.ts      # UI 컴포넌트 내보내기
├── model/            # 도메인 모델 및 상태
│   ├── types.ts      # 타입 정의
│   ├── constants.ts  # 상수 및 열거형
│   ├── ui-types.ts   # UI 관련 타입
│   ├── productStore.ts # 상태 관리 스토어
│   └── index.ts      # 모델 내보내기
├── api/              # API 통신
│   ├── repository.ts # API 요청 함수
│   ├── mappers.ts    # 데이터 변환 함수
│   ├── types.ts      # API 요청/응답 타입
│   └── index.ts      # API 내보내기
└── index.ts          # 공개 API
```

## 세그먼트 구성

각 슬라이스는 세그먼트로 분할됩니다. 일반적인 세그먼트는 다음과 같습니다:

### ui 세그먼트

UI 컴포넌트를 포함합니다.

```
ui/
├── ComponentName.vue   # 컴포넌트
└── index.ts            # 내보내기
```

### model 세그먼트

도메인 모델, 타입, 상태 관리를 포함합니다.

```
model/
├── types.ts           # 타입 정의
├── constants.ts       # 상수 및 열거형
├── ui-types.ts        # UI 관련 타입
├── store.ts           # 상태 관리 스토어
└── index.ts           # 내보내기
```

### api 세그먼트

API 통신 관련 코드를 포함합니다.

```
api/
├── repository.ts      # API 요청 함수
├── mappers.ts         # 데이터 변환 함수
├── types.ts           # API 요청/응답 타입
└── index.ts           # 내보내기
```

## 의존성 규칙

FSD의 핵심은 단방향 의존성입니다. 상위 레이어는 하위 레이어를 참조할 수 있지만, 그 반대는 불가능합니다.

### 의존성 규칙 예시

```
app -> processes -> pages -> widgets -> features -> entities -> shared
```

허용되는 의존성:
- `features/productManagement`은 `entities/product`를 참조할 수 있습니다.
- `widgets/productCatalog`은 `features/productFilter`와 `entities/product`를 참조할 수 있습니다.

금지되는 의존성:
- `entities/product`는 `features/productManagement`를 참조할 수 없습니다.
- `features/productFilter`는 `widgets/productCatalog`를 참조할 수 없습니다.

### 동일 레이어 내 의존성

동일 레이어 내의 슬라이스 간 직접 참조는 제한됩니다. 대신 상위 레이어에서 조합하거나 공유 상태를 통해 간접적으로 통신해야 합니다.

금지되는 의존성:
- `entities/product`가 `entities/user`를 직접 참조하는 것
- `features/login`이 `features/registration`을 직접 참조하는 것

### 공유 레이어 의존성

모든 레이어는 `shared` 레이어를 참조할 수 있습니다.

## 공개 API 패턴

모든 슬라이스는 `index.ts` 파일을 통해 공개 API를 명확히 정의해야 합니다.

```typescript
// entities/product/index.ts (실제 파일)
// 도메인 모델
export type { Product, ProductStock } from './model/types';
export { ProductCategory, ProductStatus, StockChangeReason } from './model/constants';

// UI 모델
export type { ProductDisplayProps } from './model/ui-types';

// API 모델 및 함수
export type { ProductRequest, ProductResponse } from './api/types';
export { productRepository } from './api/repository';
export { mapToProduct, mapToProducts, mapToProductDisplayProps } from './api/mappers';

// 스토어
export { useProductStore } from './model/productStore';

// UI 컴포넌트
export { default as ProductCard } from './ui/ProductCard.vue';
```

## 모범 사례

### 1. 단일 책임 원칙

각 슬라이스, 세그먼트, 컴포넌트는 하나의 책임만 가져야 합니다.

```typescript
// 좋은 예: 책임 분리
// entities/product/model/types.ts - 타입 정의
export interface Product { /* ... */ }

// entities/product/model/constants.ts - 상수 정의
export enum ProductCategory { /* ... */ }

// entities/product/model/productStore.ts - 상태 관리
export const useProductStore = defineStore(/* ... */);
```

### 2. 엔티티 우선 모델링

애플리케이션 개발을 시작할 때는 엔티티부터 모델링하는 것이 좋습니다.

1. 도메인 모델 정의 (`entities/{entity}/model/types.ts`)
2. API 인터페이스 정의 (`entities/{entity}/api/types.ts`)
3. 리포지토리 구현 (`entities/{entity}/api/repository.ts`)
4. 기본 UI 컴포넌트 개발 (`entities/{entity}/ui/`)
5. 상위 레이어로 확장 (features -> widgets -> pages -> processes -> app)

### 3. 컴포넌트 명명 규칙

- **엔티티 컴포넌트**: `{Entity}{ComponentPurpose}`
  - 예: `ProductCard`, `UserProfile`

- **기능 컴포넌트**: `{Action}{EntityOrPurpose}`
  - 예: `LoginForm`, `FilterProducts`

- **위젯 컴포넌트**: 목적이나 위치에 따라 명명
  - 예: `Header`, `Footer`, `ProductList`

- **페이지 컴포넌트**: `{Entity}Page` 또는 `{Purpose}Page`
  - 예: `HomePage`, `ProductDetailPage`, `LoginPage`

- **공유 컴포넌트**: 일반적인 UI 요소 이름
  - 예: `Button`, `Input`, `Modal`

### 4. 모델-뷰 분리

복잡한 컴포넌트는 모델과 뷰를 분리합니다.

```vue
<!-- features/productFilter/ui/ProductFilterPanel.vue -->
<template>
  <div class="product-filter-panel">
    <!-- UI 표현 -->
  </div>
</template>

<script setup lang="ts">
// 모델-뷰 분리를 위한 컴포지션 함수 사용
import { useProductFilter } from '../model/useProductFilter';

// 모델 로직
const {
  searchQuery,
  selectedCategory,
  categories,
  applyFilter
} = useProductFilter();

// 이벤트 핸들러
const handleApplyFilter = () => {
  applyFilter();
};
</script>
```