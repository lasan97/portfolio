# 엔티티 개발 가이드

이 문서는 Portfolio 프로젝트에서 엔티티(Entity)를 개발하는 방법에 대한 가이드를 제공합니다.

## 목차

1. [엔티티란?](#엔티티란)
2. [엔티티 구조](#엔티티-구조)
3. [엔티티 개발 프로세스](#엔티티-개발-프로세스)
4. [도메인 모델 설계](#도메인-모델-설계)
5. [API 통신 구현](#api-통신-구현)
6. [상태 관리 구현](#상태-관리-구현)
7. [UI 컴포넌트 개발](#ui-컴포넌트-개발)
8. [공개 API 설계](#공개-api-설계)
9. [엔티티 간 관계 처리](#엔티티-간-관계-처리)
10. [모범 사례 및 패턴](#모범-사례-및-패턴)

## 엔티티란?

엔티티(Entity)는 비즈니스 도메인의 핵심 개념을 나타내는 모델입니다. 이는 애플리케이션의 기본 구성 요소로, 다른 레이어에서 사용되는 도메인 객체와 관련 로직을 포함합니다.

### 엔티티의 특징

- **도메인 중심**: 비즈니스 도메인의 핵심 개념을 표현합니다.
- **재사용 가능**: 여러 기능과 페이지에서 재사용될 수 있습니다.
- **자체 완결적**: 자체적인 상태 관리와 API 통신 기능을 가질 수 있습니다.
- **UI 독립적**: 핵심 로직은 UI에 의존하지 않습니다.

### 현재 프로젝트의 엔티티 예시

- **User**: 사용자 정보 및 인증 관련 데이터
- **Product**: 상품 정보 및 관리
- **Cart**: 장바구니 기능 및 상태

## 엔티티 구조

엔티티는 다음과 같은 구조로 조직됩니다:

```
entities/{엔티티명}/
├── model/              # 도메인 모델 및 상태
│   ├── types.ts        # 타입 정의
│   ├── constants.ts    # 상수 및 열거형
│   ├── ui-types.ts     # UI 관련 타입
│   └── {엔티티}Store.ts # 상태 관리
├── api/                # API 관련 코드 (선택적)
│   ├── types.ts        # API 요청/응답 타입
│   ├── repository.ts   # API 요청 함수
│   ├── mappers.ts      # API ↔ 도메인 변환 함수
│   └── index.ts        # API 레이어 엔트리포인트
├── ui/                 # UI 컴포넌트 (선택적)
│   └── {Component}.vue # 엔티티 관련 UI 컴포넌트
└── index.ts            # 공개 API
```

## 엔티티 개발 프로세스

엔티티 개발은 다음 단계로 진행됩니다:

1. **도메인 모델 정의**: 엔티티의 핵심 타입과 상수를 정의합니다.
2. **API 통신 구현**: 필요한 경우 API 요청 함수와 데이터 매핑을 구현합니다.
3. **상태 관리 구현**: Pinia 스토어를 사용하여 엔티티 상태 관리를 구현합니다.
4. **UI 컴포넌트 개발**: 엔티티와 관련된 기본 UI 컴포넌트를 개발합니다.
5. **공개 API 설계**: `index.ts` 파일을 통해 엔티티의 공개 API를 정의합니다.

## 도메인 모델 설계

도메인 모델은 엔티티의 핵심입니다. 이는 비즈니스 개념을 코드로 표현한 것입니다.

### 타입 정의 (`types.ts`)

```typescript
// entities/product/model/types.ts
import { ProductCategory, ProductStatus } from './constants';

export interface Product {
  id: number;
  name: string;
  originalPrice: number; // 원가
  price: number; // 판매가
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

### 상수 및 열거형 (`constants.ts`)

```typescript
// entities/product/model/constants.ts
export enum ProductCategory {
  ELECTRONICS = 'ELECTRONICS',
  FURNITURE = 'FURNITURE',
  // 기타 카테고리...
}

// 네임스페이스를 사용한 확장 기능
export namespace ProductCategory {
  export const descriptions: Record<ProductCategory, string> = {
    [ProductCategory.ELECTRONICS]: '전자제품',
    [ProductCategory.FURNITURE]: '가구',
    // 기타 설명...
  };

  export function getDescription(category: ProductCategory): string {
    return descriptions[category] || category;
  }
}
```

## API 통신 구현

엔티티는 자체 API 통신 기능을 가질 수 있습니다. 이는 리포지토리 패턴을 사용하여 구현됩니다.

### API 타입 (`api/types.ts`)

```typescript
// entities/product/api/types.ts
import { ProductCategory } from '../model/constants';

// API 요청 타입
export namespace ProductRequest {
  export interface Create {
    name: string;
    price: number;
    // 기타 필드...
  }
  
  // 기타 요청 타입...
}

// API 응답 타입
export namespace ProductResponse {
  export interface Detail {
    id: number;
    name: string;
    // 기타 필드...
  }
}
```

### 매퍼 (`api/mappers.ts`)

```typescript
// entities/product/api/mappers.ts
import { Product } from '../model/types';
import { ProductResponse } from './types';
import { ProductStatus } from '../model/constants';

// API 응답 → 도메인 모델 매핑
export const mapToProduct = (response: ProductResponse.Detail): Product => ({
  id: response.id,
  name: response.name,
  // 기타 매핑...
});

// API 목록 응답 → 도메인 모델 매핑
export const mapToProducts = (responses: ProductResponse.List[]): Product[] => 
  responses.map(response => ({
    id: response.id,
    name: response.name,
    // 기타 매핑...
  }));
```

### 리포지토리 (`api/repository.ts`)

```typescript
// entities/product/api/repository.ts
import { apiInstance } from '@shared/api';
import { ProductRequest, ProductResponse } from './types';
import { Product } from '../model/types';
import { mapToProduct, mapToProducts } from './mappers';

export const productRepository = {
  // 상품 목록 조회
  async getProducts(): Promise<Product[]> {
    const response = await apiInstance.get<ProductResponse.List[]>('/api/products');
    return mapToProducts(response.data);
  },

  // 단일 상품 조회
  async getProduct(id: number): Promise<Product> {
    const response = await apiInstance.get<ProductResponse.Detail>(`/api/products/${id}`);
    return mapToProduct(response.data);
  },
  
  // 기타 API 요청...
};
```

### API 레이어 엔트리포인트 (`api/index.ts`)

```typescript
// entities/product/api/index.ts
export type { ProductRequest, ProductResponse } from './types';
export { productRepository } from './repository';
export { mapToProduct, mapToProducts } from './mappers';
```

## 상태 관리 구현

엔티티의 상태 관리는 Pinia 스토어를 사용하여 구현합니다.

```typescript
// entities/product/model/productStore.ts
import { defineStore } from 'pinia';
import { Product } from './types';
import { productRepository } from '../api';

export const useProductStore = defineStore('product', {
  state: () => ({
    products: [] as Product[],
    isLoading: false,
    error: null as string | null,
  }),
  
  getters: {
    getProductById: (state) => (id: number) => 
      state.products.find(product => product.id === id),
    // 기타 게터...
  },
  
  actions: {
    // 상품 목록 가져오기
    async fetchProducts() {
      this.isLoading = true;
      try {
        this.products = await productRepository.getProducts();
      } catch (err: any) {
        this.error = err.message || '상품을 불러오는 중 오류가 발생했습니다.';
        console.error('Error:', err);
      } finally {
        this.isLoading = false;
      }
    },
    
    // 기타 액션...
  }
});
```

## UI 컴포넌트 개발

엔티티는 자체 UI 컴포넌트를 가질 수 있습니다. 이는 도메인 모델을 시각적으로 표현하는 기본 컴포넌트입니다.

```vue
<!-- entities/product/ui/ProductCard.vue (간략화) -->
<template>
  <div class="product-card">
    <img :src="product.thumbnailImageUrl" :alt="product.name" />
    <div class="product-info">
      <h3>{{ product.name }}</h3>
      <div>{{ formatPrice(product.price) }}</div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue';
import { Product, ProductCategory } from '@entities/product';

export default defineComponent({
  props: {
    product: {
      type: Object as PropType<Product>,
      required: true
    }
  },
  setup() {
    const formatPrice = (price: number): string => {
      return new Intl.NumberFormat('ko-KR', { 
        style: 'currency', currency: 'KRW'
      }).format(price);
    };

    return { formatPrice };
  }
});
</script>
```

## 공개 API 설계

엔티티의 공개 API는 `index.ts` 파일을 통해 정의됩니다.

```typescript
// entities/product/index.ts
// 도메인 모델
export type { Product, ProductStock } from './model/types';
export { ProductCategory, ProductStatus } from './model/constants';

// UI 모델
export type { ProductDisplayProps } from './model/ui-types';

// API 모델 및 함수
export type { ProductRequest, ProductResponse } from './api/types';
export { productRepository } from './api/repository';
export { mapToProduct, mapToProducts } from './api/mappers';

// 스토어
export { useProductStore } from './model/productStore';

// UI 컴포넌트
export { default as ProductCard } from './ui/ProductCard.vue';
```

## 엔티티 간 관계 처리

엔티티 간 관계는 다음과 같은 방법으로 처리합니다:

### 참조를 통한 관계

```typescript
// entities/cart/model/types.ts
import { Product } from '@entities/product';

export interface CartItem {
  product: Product;
  quantity: number;
}
```

### 스토어 간 상호작용

```typescript
// entities/cart/model/cartStore.ts
import { defineStore } from 'pinia';
import { CartItem, CartState } from './types';
import { Product } from '@entities/product';
import { cartRepository } from '../api';

export const useCartStore = defineStore('cart', {
  state: (): CartState => ({
    items: [],
  }),

  actions: {
    // 장바구니에 상품 추가
    async addToCart(product: Product, quantity: number = 1) {
      try {
        await cartRepository.addCartItem(product.id, quantity);
        
        // 로컬 상태 업데이트
        const existingItem = this.items.find(item => item.product.id === product.id);
        if (existingItem) {
          existingItem.quantity += quantity;
        } else {
          this.items.push({ product, quantity });
        }
      } catch (error) {
        console.error('오류:', error);
        throw error;
      }
    }
  }
});
```

## 모범 사례 및 패턴

### 1. 도메인 모델 설계 원칙
- **명확한 책임**: 각 엔티티는 명확한 책임을 가져야 합니다.
- **타입 안전성**: TypeScript의 타입 시스템을 최대한 활용합니다.
- **명확한 네이밍**: 도메인 용어를 일관되게 사용합니다.

### 2. API 통신 패턴
- **리포지토리 패턴**: API 요청은 리포지토리 패턴으로 구현합니다.
- **명시적 매핑**: API 응답은 항상 명시적으로 도메인 모델로 매핑합니다.
- **네임스페이스 활용**: 요청/응답 타입은 네임스페이스로 구분합니다.

### 3. 상태 관리 패턴
- **단일 책임**: 각 스토어는 하나의 엔티티만 관리합니다.
- **상태 정규화**: 중복 데이터를 최소화합니다.
- **비동기 액션**: API 호출은 액션으로 처리하고, 오류를 적절히 처리합니다.

### 4. UI 컴포넌트 패턴
- **프레젠테이션 컴포넌트**: 엔티티 UI 컴포넌트는 프레젠테이션에 집중합니다.
- **재사용성**: 컴포넌트는 재사용 가능하도록 설계합니다.

### 5. 폴더 및 파일 구조 컨벤션
- **일관된 구조**: 모든 엔티티는 동일한 구조를 따릅니다.
- **네이밍 컨벤션**: 파일 이름은 일관된 규칙을 따릅니다.
  - repository.ts (단수형)
  - types.ts
  - mappers.ts
  - constants.ts
  - {entity}Store.ts
