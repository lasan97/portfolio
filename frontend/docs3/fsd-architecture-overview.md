# FSD (Feature-Sliced Design) 아키텍처 개요

## 목차
1. [FSD 아키텍처 개요](#fsd-아키텍처-개요)
2. [프로젝트 구조](#프로젝트-구조)
3. [레이어 설명](#레이어-설명)
4. [개발 가이드라인](#개발-가이드라인)
5. [예시: Product 도메인](#예시-product-도메인)
6. [테스트 전략](#테스트-전략)
7. [자주 묻는 질문](#자주-묻는-질문)

## FSD 아키텍처 개요

FSD(Feature-Sliced Design)는 프론트엔드 애플리케이션을 구성하기 위한 아키텍처 방법론으로, 코드를 기능적 관심사에 따라 레이어와 슬라이스로 구성합니다. 이 방법론은 다음과 같은 이점을 제공합니다:

- **확장성**: 애플리케이션이 커져도 구조가 명확하게 유지됩니다.
- **유지보수성**: 관련 코드가 함께 그룹화되어 변경 사항을 적용하기 쉽습니다.
- **재사용성**: 컴포넌트와 로직을 쉽게 재사용할 수 있습니다.
- **개발 효율성**: 팀원들이 코드베이스를 쉽게 이해하고 협업할 수 있습니다.

### 핵심 원칙

1. **레이어 기반 구조**: 코드는 추상화 수준에 따라 레이어로 구성됩니다.
2. **단방향 의존성**: 상위 레이어는 하위 레이어에만 의존할 수 있습니다.
3. **슬라이스 독립성**: 각 슬라이스는 독립적으로 작동해야 합니다.
4. **공개 API**: 각 슬라이스는 명확한 공개 API를 제공해야 합니다.

## 프로젝트 구조

우리 프로젝트는 다음과 같은 FSD 레이어로 구성되어 있습니다:

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

## 레이어 설명

### 1. app

애플리케이션의 진입점으로, 전역 설정, 스타일, 라우팅, 프로바이더 등을 포함합니다.

**역할**:
- 애플리케이션 초기화
- 전역 스타일 및 테마
- 라우팅 설정
- 전역 프로바이더 설정

**예시 파일**:
- `App.vue`
- `styles/main.css`
- `router/index.ts`

### 2. processes

여러 기능과 엔티티에 걸친 비즈니스 프로세스를 구현합니다.

**역할**:
- 복잡한 비즈니스 프로세스 구현
- 여러 기능 간의 조정

**예시**:
- 결제 프로세스
- 인증 흐름

### 3. pages

애플리케이션의 페이지를 정의합니다. 위젯, 기능, 엔티티를 조합하여 완전한 페이지를 구성합니다.

**역할**:
- 페이지 레이아웃 정의
- 라우팅 연결
- 페이지별 로직 구현

**구조**:
```
pages/
├── products/
│   ├── ui/
│   │   ├── ProductsPage.vue
│   │   ├── ProductDetailPage.vue
│   │   └── ProductManagementPage.vue
│   └── index.ts
```

### 4. widgets

독립적인 UI 블록으로, 여러 기능과 엔티티를 조합하여 재사용 가능한 인터페이스 요소를 만듭니다.

**역할**:
- 복잡한 UI 컴포넌트 구성
- 여러 기능 조합

**예시**:
- 헤더
- 푸터
- 사이드바
- 상품 목록 위젯

### 5. features

사용자 상호작용과 관련된 기능을 구현합니다. 엔티티를 사용하여 비즈니스 로직을 구현합니다.

**역할**:
- 사용자 상호작용 처리
- 비즈니스 로직 구현
- UI 컴포넌트 제공

**구조**:
```
features/
├── productManagement/
│   ├── api/
│   ├── model/
│   ├── ui/
│   └── index.ts
```

### 6. entities

비즈니스 엔티티를 정의하고 관리합니다. 도메인 모델, 상태 관리, API 통신을 포함합니다.

**역할**:
- 도메인 모델 정의
- 엔티티 관련 상태 관리
- 엔티티 관련 API 통신
- 기본 UI 컴포넌트 제공

**구조**:
```
entities/
├── product/
│   ├── api/
│   │   ├── repository.ts
│   │   ├── types.ts
│   │   └── mappers.ts
│   ├── model/
│   │   ├── types.ts
│   │   ├── constants.ts
│   │   └── productStore.ts
│   ├── ui/
│   │   └── ProductCard.vue
│   └── index.ts
```

### 7. shared

애플리케이션 전체에서 공유되는 코드를 포함합니다. 유틸리티, 상수, 타입, API 클라이언트 등이 여기에 속합니다.

**역할**:
- 공통 유틸리티 함수
- 공통 타입 정의
- API 클라이언트
- UI 키트

## 개발 가이드라인

### 새로운 기능 개발 흐름

1. **엔티티 정의**: 필요한 비즈니스 엔티티를 정의합니다.
2. **기능 구현**: 엔티티를 사용하여 사용자 상호작용 기능을 구현합니다.
3. **위젯 구성**: 필요한 경우 여러 기능을 조합하여 위젯을 만듭니다.
4. **페이지 구성**: 위젯, 기능, 엔티티를 조합하여 페이지를 구성합니다.

### 레이어 간 의존성 규칙

- 상위 레이어는 하위 레이어에만 의존할 수 있습니다.
- 같은 레이어 내에서는 다른 슬라이스에 의존하지 않아야 합니다.
- 의존성 방향: `app → processes → pages → widgets → features → entities → shared`

### 슬라이스 구조

각 슬라이스는 일반적으로 다음과 같은 구조를 가집니다:

```
slice/
├── api/        # API 통신
├── model/      # 비즈니스 로직, 타입, 상태
├── ui/         # UI 컴포넌트
└── index.ts    # 공개 API
```

### 공개 API 설계

각 슬라이스는 `index.ts` 파일을 통해 공개 API를 제공해야 합니다. 내부 구현 세부 사항은 외부에 노출되지 않아야 합니다.

```typescript
// 도메인 모델
export type { Product } from './model/types';
export { ProductCategory } from './model/constants';

// API 모델 및 함수
export { productRepository } from './api/repository';

// UI 컴포넌트
export { default as ProductCard } from './ui/ProductCard.vue';
```

## 예시: Product 도메인

Product 도메인은 FSD 아키텍처를 잘 구현한 예시입니다. 다음은 각 레이어에서 Product 관련 코드가 어떻게 구성되어 있는지 보여줍니다.

### 1. entities/product

**역할**: Product 엔티티의 핵심 도메인 모델, 상태 관리, API 통신을 담당합니다.

**주요 파일**:
- `model/types.ts`: Product 인터페이스 정의
- `model/constants.ts`: 상품 카테고리, 상태 등의 상수 정의
- `model/productStore.ts`: Pinia를 사용한 상태 관리
- `api/repository.ts`: API 통신 함수
- `api/types.ts`: API 요청/응답 타입
- `api/mappers.ts`: API 데이터와 도메인 모델 간 변환
- `ui/ProductCard.vue`: 기본 상품 카드 컴포넌트

**예시 코드**:
```typescript
// model/types.ts
export interface Product {
  id: number;
  name: string;
  originalPrice: number;
  price: number;
  description: string;
  thumbnailImageUrl?: string;
  category: ProductCategory;
  inStock: boolean;
  status?: ProductStatus;
  stock?: ProductStock;
}
```

### 2. features/productManagement

**역할**: 상품 관리 기능을 구현합니다. 상품 생성, 수정, 삭제, 재고 조정 등의 기능을 제공합니다.

**주요 파일**:
- `model/types.ts`: 상품 관리 관련 타입 정의
- `model/mappers.ts`: 폼 데이터와 API 요청 간 변환
- `api/requests.ts`: 상품 관리 API 요청 함수
- `ui/ProductFormModal.vue`: 상품 생성/수정 모달
- `ui/StockAdjustmentModal.vue`: 재고 조정 모달

### 3. pages/products

**역할**: 상품 관련 페이지를 구성합니다.

**주요 파일**:
- `ui/ProductsPage.vue`: 상품 목록 페이지
- `ui/ProductDetailPage.vue`: 상품 상세 페이지
- `ui/ProductManagementPage.vue`: 상품 관리 페이지

**예시 코드**:
```vue
<!-- ProductDetailPage.vue (일부) -->
<template>
  <div class="product-detail-container">
    <!-- 상품 상세 정보 표시 -->
  </div>
</template>

<script lang="ts">
// 참고: 아래 코드는 예시이며, 실제 프로젝트의 경로와 다를 수 있습니다.
import { defineComponent, ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { useProductStore, Product, ProductCategory } from '../../../entities/product';
import { useCartWithAuth } from '../../../features/cart';

export default defineComponent({
  setup() {
    const route = useRoute();
    const productStore = useProductStore();
    const { addToCart: addProductToCart } = useCartWithAuth();
    
    // 상품 정보 로드 및 UI 로직 구현
  }
});
</script>
```

## 테스트 전략

FSD 아키텍처에서는 각 레이어와 슬라이스에 맞는 테스트 전략을 적용해야 합니다.

### 1. 단위 테스트

- **shared**: 유틸리티 함수, 헬퍼 등의 단위 테스트
- **entities**: 도메인 모델, 매퍼, 스토어 등의 단위 테스트
- **features**: 비즈니스 로직, 상태 변경 등의 단위 테스트

### 2. 통합 테스트

- **features**: 여러 엔티티와의 상호작용 테스트
- **widgets**: 여러 기능과의 통합 테스트

### 3. E2E 테스트

- **pages**: 전체 페이지 흐름 테스트
- **processes**: 전체 비즈니스 프로세스 테스트

## 자주 묻는 질문

### Q: 새로운 기능을 어느 레이어에 추가해야 하나요?

A: 기능의 성격에 따라 다릅니다:
- 비즈니스 엔티티와 관련된 기본 기능: `entities`
- 사용자 상호작용 기능: `features`
- 여러 기능을 조합한 UI 블록: `widgets`
- 전체 페이지: `pages`
- 여러 기능에 걸친 비즈니스 프로세스: `processes`

### Q: 레이어 간 의존성 규칙을 어떻게 강제할 수 있나요?

A: ESLint 규칙을 설정하여 의존성 방향을 강제할 수 있습니다. 예를 들어, `eslint-plugin-import`와 `eslint-plugin-boundaries`를 사용할 수 있습니다.

### Q: 상태 관리는 어느 레이어에서 처리해야 하나요?

A: 상태의 성격에 따라 다릅니다:
- 엔티티 관련 상태: `entities` 레이어의 스토어
- 기능 관련 상태: `features` 레이어의 스토어
- 전역 애플리케이션 상태: `app` 레이어의 스토어

### Q: API 통신은 어느 레이어에서 처리해야 하나요?

A: API 통신의 성격에 따라 다릅니다:
- 기본 API 클라이언트: `shared/api`
- 엔티티 관련 API: `entities/{entity}/api`
- 기능 관련 API: `features/{feature}/api`