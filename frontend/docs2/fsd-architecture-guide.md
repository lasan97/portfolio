# Feature-Sliced Design 아키텍처 가이드

이 문서는 Portfolio 프로젝트에서 사용되는 Feature-Sliced Design(FSD) 아키텍처를 자세히 설명합니다. FSD는 Vue, React 등 UI 프레임워크에 구애받지 않는 아키텍처 방법론으로, 코드를 기능 중심으로 조직화하여 확장성과 유지보수성을 높입니다.

## 목차

1. [FSD 개요](#fsd-개요)
2. [레이어 구조](#레이어-구조)
3. [슬라이스 구성](#슬라이스-구성)
4. [세그먼트 구성](#세그먼트-구성)
5. [의존성 규칙](#의존성-규칙)
6. [컴포넌트 배치 및 참조](#컴포넌트-배치-및-참조)
7. [가이드라인 및 모범 사례](#가이드라인-및-모범-사례)
8. [문제 해결 패턴](#문제-해결-패턴)

## FSD 개요

Feature-Sliced Design(FSD)은 프론트엔드 애플리케이션을 개발할 때 코드를 조직화하는 방법론입니다. FSD는 다음과 같은 이점을 제공합니다:

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

현재 프로젝트에서는 processes 레이어가 활발히 사용되지 않는 것으로 보입니다.

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
├── auth/
│   ├── ui/        # 인증 페이지 UI
│   └── index.ts   # 공개 API
└── order/
    ├── ui/        # 주문 페이지 UI
    └── index.ts   # 공개 API
```

### 4. widgets

독립적인 복합 UI 블록으로, 여러 기능과 엔티티를 조합합니다.

```
widgets/
├── header/
│   ├── ui/        # 헤더 UI 컴포넌트
│   └── index.ts   # 공개 API
├── footer/
│   ├── ui/        # 푸터 UI 컴포넌트
│   └── index.ts   # 공개 API
└── productList/
    ├── ui/        # 상품 리스트 UI
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
├── cart/
│   ├── model/     # 장바구니 상태 및 로직
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
├── product/
│   ├── ui/        # 상품 UI 컴포넌트
│   ├── model/     # 상품 모델 및 상태
│   ├── api/       # 상품 API 통신
│   └── index.ts   # 공개 API
└── cart/
    ├── ui/        # 장바구니 UI 컴포넌트
    ├── model/     # 장바구니 모델 및 상태
    ├── api/       # 장바구니 API 통신
    └── index.ts   # 공개 API
```

### 7. shared

특정 도메인이나 기능에 종속되지 않는 공통 코드를 포함합니다.

```
shared/
├── ui/           # 기본 UI 컴포넌트
│   ├── button/
│   ├── input/
│   ├── card/
│   └── toast/
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
├── ui-types.ts        # UI 관련 타입 (실제 프로젝트에서 사용)
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

### 실제 프로젝트에서의 의존성 주의사항

현재 프로젝트에서는 일부 예외적인 의존성이 있습니다. 예를 들어:

```typescript
// entities/product/model/productStore.ts
import { ProductFormData, StockAdjustmentData, productManagementApi } from '@features/productManagement';
```

이러한 패턴은 단방향 의존성 원칙에 어긋나므로 주의해야 합니다. 더 좋은 방법은:

1. **엔티티 레이어에 모든 CRUD 작업 구현하기**
   ```typescript
   // entities/product/api/repository.ts
   export const productRepository = {
     // 기존 메서드...
     
     async createProduct(productData): Promise<number> {
       const response = await apiInstance.post<number>('/api/products', productData);
       return response.data;
     }
   };
   ```

2. **features 레이어에서 엔티티 레이어의 API 사용하기**
   ```typescript
   // features/productManagement/api/index.ts
   import { productRepository } from '@entities/product';
   
   export const productManagementApi = {
     createProduct: async (formData: ProductFormData) => {
       // 폼 데이터 유효성 검사 등 추가 로직
       return productRepository.createProduct(mapToProductRequest(formData));
     }
   };
   ```

### 동일 레이어 내 의존성

동일 레이어 내의 슬라이스 간 직접 참조는 제한됩니다. 대신 상위 레이어에서 조합하거나 공유 상태를 통해 간접적으로 통신해야 합니다.

금지되는 의존성:
- `entities/product`가 `entities/user`를 직접 참조하는 것
- `features/login`이 `features/registration`을 직접 참조하는 것

### 공유 레이어 의존성

모든 레이어는 `shared` 레이어를 참조할 수 있습니다.

## 컴포넌트 배치 및 참조

### 컴포넌트 배치 기준

컴포넌트는 다음 기준에 따라 레이어에 배치합니다:

1. **entities/ui**: 도메인 모델과 관련된 기본 UI 컴포넌트
   - 예: `ProductCard`, `UserProfile`
   - 특징: 도메인 모델을 직접 표현

2. **features/ui**: 사용자 인터랙션과 관련된 UI 컴포넌트
   - 예: `LoginForm`, `ProductFilterPanel`
   - 특징: 사용자 인터랙션을 캡슐화, 엔티티 컴포넌트 사용

3. **widgets/ui**: 여러 기능을 조합한 복합 UI 블록
   - 예: `Header`, `ProductList`
   - 특징: 여러 기능과 엔티티를 조합

4. **pages/ui**: 특정 라우트에 매핑되는 페이지 컴포넌트
   - 예: `HomePage`, `ProductDetailPage`
   - 특징: 위젯, 기능, 엔티티 컴포넌트를 조합

5. **shared/ui**: 재사용 가능한 기본 UI 컴포넌트
   - 예: `Button`, `Input`, `Modal`
   - 특징: 도메인 종속성 없음, 순수 UI

### 컴포넌트 참조 방식

컴포넌트 참조는 항상 공개 API를 통해 이루어져야 합니다.

```typescript
// 좋은 예
import { ProductCard } from '@entities/product';

// 나쁜 예
import ProductCard from '@entities/product/ui/ProductCard.vue';
```

### 앨리어스 설정

경로 앨리어스를 사용하여 가독성과 유지보수성을 높입니다.

```typescript
// vite.config.ts
export default defineConfig({
  resolve: {
    alias: {
      '@app': '/src/app',
      '@processes': '/src/processes',
      '@pages': '/src/pages',
      '@widgets': '/src/widgets',
      '@features': '/src/features',
      '@entities': '/src/entities',
      '@shared': '/src/shared',
    }
  }
});
```

## 가이드라인 및 모범 사례

### 1. 공개 API 패턴

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

### 2. 단일 책임 원칙

각 슬라이스, 세그먼트, 컴포넌트는 하나의 책임만 가져야 합니다.

```typescript
// 좋은 예: 책임 분리
// entities/product/model/types.ts - 타입 정의
export interface Product { /* ... */ }

// entities/product/model/constants.ts - 상수 정의
export enum ProductCategory { /* ... */ }

// entities/product/model/productStore.ts - 상태 관리
export const useProductStore = defineStore(/* ... */);

// 나쁜 예: 책임 혼합
// entities/product/model/index.ts - 여러 책임을 한 파일에 혼합
export interface Product { /* ... */ }
export enum ProductCategory { /* ... */ }
export const useProductStore = defineStore(/* ... */);
```

### 3. 엔티티 우선 모델링

애플리케이션 개발을 시작할 때는 엔티티부터 모델링하는 것이 좋습니다.

1. 도메인 모델 정의 (`entities/{entity}/model/types.ts`)
2. API 인터페이스 정의 (`entities/{entity}/api/types.ts`)
3. 리포지토리 구현 (`entities/{entity}/api/repository.ts`)
4. 기본 UI 컴포넌트 개발 (`entities/{entity}/ui/`)
5. 상위 레이어로 확장 (features -> widgets -> pages -> processes -> app)

### 4. 컴포넌트 명명 규칙

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

## 문제 해결 패턴

### 1. 크로스 엔티티 관계

여러 엔티티 간의 관계는 상위 레이어에서 해결합니다.

```typescript
// features/order/model/orderStore.ts
import { useProductStore } from '@entities/product';
import { useUserStore } from '@entities/user';

export const useOrderStore = defineStore('order', {
  // ...
  actions: {
    async createOrder(orderData) {
      const productStore = useProductStore();
      const userStore = useUserStore();
      
      // 여러 엔티티 간의 관계 처리
      const product = await productStore.fetchProductById(orderData.productId);
      const user = userStore.currentUser;
      
      // 주문 생성 로직
      // ...
    }
  }
});
```

### 2. 모듈 간 간접 통신

모듈 간 직접 참조 대신 이벤트 버스나 공유 상태를 사용할 수 있습니다. 하지만 현재 프로젝트에서는 Pinia 스토어를 통한 상태 공유가 주로 사용됩니다.

### 3. 모델-뷰 분리

복잡한 컴포넌트는 모델과 뷰를 분리합니다.

```vue
<!-- features/productFilter/ui/ProductFilterPanel.vue -->
<template>
  <div class="product-filter-panel">
    <!-- UI 표현 -->
    <input v-model="searchQuery" placeholder="검색어" />
    <select v-model="selectedCategory">
      <option v-for="category in categories" :key="category.code">
        {{ category.description }}
      </option>
    </select>
    <button @click="handleApplyFilter">필터 적용</button>
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

```typescript
// features/productFilter/model/useProductFilter.ts
import { ref, computed } from 'vue';
import { ProductCategory } from '@entities/product';

export function useProductFilter() {
  // 상태
  const searchQuery = ref('');
  const selectedCategory = ref<ProductCategory | null>(null);
  
  // 계산된 속성
  const categories = computed(() => ProductCategory.entries());
  
  // 메서드
  const applyFilter = () => {
    // 필터 적용 로직
    // ...
  };
  
  return {
    // 상태
    searchQuery,
    selectedCategory,
    
    // 계산된 속성
    categories,
    
    // 메서드
    applyFilter
  };
}
```
