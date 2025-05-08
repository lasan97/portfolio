# FSD (Feature-Sliced Design) 아키텍처 가이드

## 목차

1. [아키텍처 개요](#아키텍처-개요)
2. [핵심 원칙](#핵심-원칙)
3. [레이어 설명](#레이어-설명)
4. [슬라이스 구조](#슬라이스-구조)
5. [의존성 규칙](#의존성-규칙)
6. [공개 API 설계](#공개-api-설계)
7. [실제 구현 예시: Product 도메인](#실제-구현-예시-product-도메인)

## 아키텍처 개요

Feature-Sliced Design(FSD)은 프론트엔드 애플리케이션을 구조화하기 위한 아키텍처 방법론으로, 코드를 기능적 관심사에 따라 레이어와 슬라이스로 조직화합니다. 이 방법론은 확장성, 유지보수성, 재사용성, 개발 효율성을 향상시키는 것을 목표로 합니다.

FSD는 애플리케이션을 **레이어**, **슬라이스**, **세그먼트**로 구성합니다:

- **레이어**: 추상화 수준과 책임에 따라 코드를 수직적으로 나눈 것 (app, pages, widgets 등)
- **슬라이스**: 비즈니스 도메인에 따라 코드를 수평적으로 나눈 것 (user, product, order 등)
- **세그먼트**: 기술적 관심사에 따라 슬라이스 내부를 나눈 것 (api, model, ui 등)

## 핵심 원칙

FSD는 다음과 같은 핵심 원칙을 따릅니다:

1. **단방향 의존성**: 상위 레이어는 하위 레이어에만 의존할 수 있으며, 동일 레이어 내에서는 다른 슬라이스에 의존하지 않아야 합니다.
2. **명시적 공개 API**: 각 슬라이스는 명확한 공개 API를 제공해야 합니다. 내부 구현 세부 사항은 외부에 노출되지 않아야 합니다.
3. **레이어 기반 조직화**: 코드는 추상화 수준과 책임에 따라 레이어로 구성됩니다.
4. **슬라이스 독립성**: 각 슬라이스는 가능한 독립적으로 작동해야 합니다.
5. **적응성**: 아키텍처는 프로젝트의 규모와 복잡성에 따라 적응할 수 있어야 합니다.

## 레이어 설명

우리 애플리케이션은 다음과 같은 레이어로 구성됩니다:

### 1. app

애플리케이션의 진입점으로, 전역 설정, 스타일, 라우팅, 프로바이더 등을 포함합니다.

**역할**:
- 애플리케이션 초기화
- 전역 스타일 및 테마
- 라우팅 설정
- 전역 상태 프로바이더 설정

**예시 파일**:
- `app/App.vue`
- `app/styles/main.css`
- `app/router/index.ts`
- `app/store/index.ts`

### 2. processes

여러 기능과 엔티티에 걸친 비즈니스 프로세스를 구현합니다.

**역할**:
- 복잡한 비즈니스 프로세스 구현
- 여러 기능 간의 조정
- 장기 실행 작업 관리

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
- 상품 목록 위젯 (`widgets/productList`)

### 5. features

사용자 상호작용과 관련된 기능을 구현합니다. 엔티티를 사용하여 비즈니스 로직을 구현합니다.

**역할**:
- 사용자 상호작용 처리
- 비즈니스 로직 구현
- UI 컴포넌트 제공

**예시**:
- 상품 관리 기능 (`features/productManagement`)
- 장바구니 기능 (`features/cart`)
- 인증 기능 (`features/auth`)

### 6. entities

비즈니스 엔티티를 정의하고 관리합니다. 도메인 모델, 상태 관리, API 통신을 포함합니다.

**역할**:
- 도메인 모델 정의
- 엔티티 관련 상태 관리
- 엔티티 관련 API 통신
- 기본 UI 컴포넌트 제공

**예시**:
- 상품 엔티티 (`entities/product`)
- 주문 엔티티 (`entities/order`)
- 사용자 엔티티 (`entities/user`)

### 7. shared

애플리케이션 전체에서 공유되는 코드를 포함합니다. 유틸리티, 상수, 타입, API 클라이언트 등이 여기에 속합니다.

**역할**:
- 공통 유틸리티 함수
- 공통 타입 정의
- API 클라이언트
- UI 키트

**예시**:
- `shared/api/instance.ts`
- `shared/types/index.ts`
- `shared/ui/Button.vue`

## 슬라이스 구조

각 슬라이스(예: `entities/product`, `features/productManagement`)는 일반적으로 다음과 같은 세그먼트 구조를 가집니다:

```
slice/
├── api/        # API 통신 관련
│   ├── repository.ts
│   ├── types.ts
│   └── mappers.ts
├── model/      # 비즈니스 로직, 타입, 상태
│   ├── types.ts
│   ├── constants.ts
│   └── store.ts
├── ui/         # UI 컴포넌트
│   └── Component.vue
└── index.ts    # 공개 API
```

각 세그먼트의 역할:

- **api**: 서버와의 통신을 담당하는 코드
- **model**: 비즈니스 로직, 타입 정의, 상태 관리 코드
- **ui**: 슬라이스에 특화된 UI 컴포넌트
- **lib**: 유틸리티 함수 (필요시)
- **config**: 설정 정보 (필요시)

## 의존성 규칙

FSD에서는 다음과 같은 의존성 규칙을 따릅니다:

1. **상위 레이어에서 하위 레이어로만 의존성 허용**:
   - 상위 레이어는 하위 레이어에만 의존할 수 있습니다.
   - 하위 레이어는 상위 레이어에 의존할 수 없습니다.
   - 의존성 방향: `app → processes → pages → widgets → features → entities → shared`

2. **동일 레이어 내에서는 다른 슬라이스에 의존하지 않음**:
   - 예: `entities/product`는 `entities/user`에 직접 의존해서는 안 됩니다.
   - 예외: shared 레이어는 어디서든 사용될 수 있습니다.

3. **세그먼트 간 의존성**:
   - ui → model → api (ui는 model과 api에 의존할 수 있음)
   - api → model (api는 model에 의존할 수 있음)
   - model은 api나 ui에 의존할 수 없음

## 공개 API 설계

각 슬라이스는 `index.ts` 파일을 통해 공개 API를 제공해야 합니다. 내부 구현 세부 사항은 외부에 노출되지 않아야 합니다.

**좋은 공개 API 예시**:

```typescript
// entities/product/index.ts

// 도메인 모델
export type { Product, ProductStock } from './model/types';
export { ProductCategory, ProductStatus } from './model/constants';

// API 모델 및 함수
export type { ProductRequest, ProductResponse } from './api/types';
export { productRepository } from './api/repository';

// UI 컴포넌트
export { default as ProductCard } from './ui/ProductCard.vue';

// 스토어 (상태 관리)
export { useProductStore } from './model/productStore';
```

## 실제 구현 예시: Product 도메인

`entities/product` 슬라이스는 FSD 아키텍처를 잘 구현한 예시입니다:

### 1. model 세그먼트

**types.ts**: 핵심 도메인 모델 정의
```typescript
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

export interface ProductStock {
  id?: number;
  quantity: number;
  version?: number;
}
```

**constants.ts**: Enum 및 관련 상수 정의 (Namespace 패턴 사용)
```typescript
export enum ProductCategory {
  ELECTRONICS = 'ELECTRONICS',
  FURNITURE = 'FURNITURE',
  // ...
}

export namespace ProductCategory {
  export const descriptions: Record<ProductCategory, string> = {
    [ProductCategory.ELECTRONICS]: '전자제품',
    // ...
  };
  
  export function getDescription(category: ProductCategory): string {
    return descriptions[category] || category;
  }
  
  export function entries(): Array<{ code: ProductCategory; description: string }> {
    // ...
  }
}
```

**productStore.ts**: 상태 관리 (Pinia 사용)
```typescript
export const useProductStore = defineStore('product', {
  state: () => ({
    products: [] as Product[],
    isLoading: false,
    error: null as string | null,
  }),
  
  getters: {
    getProductById: (state) => (id: number) => 
      state.products.find(product => product.id === id),
    // ...
  },
  
  actions: {
    async fetchProducts() {
      // ...
    },
    // ...
  }
});
```

### 2. api 세그먼트

**types.ts**: API 요청/응답 타입 정의
```typescript
export namespace ProductRequest {
  export interface Create {
    name: string;
    originalPrice: number;
    price: number;
    // ...
  }
  // ...
}

export namespace ProductResponse {
  export interface Detail {
    id: number;
    name: string;
    // ...
  }
  // ...
}
```

**repository.ts**: API 통신 함수
```typescript
export const productRepository = {
  async getProducts(): Promise<Product[]> {
    const response = await apiInstance.get<ProductResponse.List[]>('/api/products');
    return mapToProducts(response.data);
  },
  // ...
};
```

**mappers.ts**: API 데이터와 도메인 모델 간 매핑
```typescript
export const mapToProduct = (response: ProductResponse.Detail): Product => ({
  id: response.id,
  name: response.name,
  // ...
});
```

### 3. ui 세그먼트

**ProductCard.vue**: 엔티티 UI 컴포넌트
```vue
<template>
  <div class="product-card">
    <!-- ... -->
  </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue';
import { Product, ProductCategory } from '@entities/product';

export default defineComponent({
  name: 'ProductCard',
  props: {
    product: {
      type: Object as PropType<Product>,
      required: true
    }
  },
  // ...
});
</script>
```

이러한 구조를 통해 Product 도메인 로직과 관련 기능들이 명확하게 조직화되어 있어, 유지보수가 용이하고 확장성이 높아집니다.
