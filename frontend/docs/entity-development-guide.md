# 엔티티 개발 가이드

이 문서는 Portfolio 프로젝트에서 엔티티(Entity)를 개발하는 방법에 대한 상세 가이드를 제공합니다. 엔티티는 FSD 아키텍처에서 핵심 비즈니스 도메인 모델을 나타내는 중요한 레이어입니다.

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
│   └── mappers.ts      # API ↔ 도메인 변환 함수
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

// 핵심 도메인 모델
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
// 상품 카테고리 Enum
export enum ProductCategory {
  ELECTRONICS = 'ELECTRONICS',
  FURNITURE = 'FURNITURE',
  HOME_APPLIANCE = 'HOME_APPLIANCE',
  // 기타 카테고리...
}

// 네임스페이스를 사용한 확장 기능
export namespace ProductCategory {
  // 설명 매핑
  export const descriptions: Record<ProductCategory, string> = {
    [ProductCategory.ELECTRONICS]: '전자제품',
    [ProductCategory.FURNITURE]: '가구',
    [ProductCategory.HOME_APPLIANCE]: '가전제품',
    // 기타 설명...
  };

  // 설명 가져오기 함수
  export function getDescription(category: ProductCategory): string {
    return descriptions[category] || category;
  }
  
  // 기타 유틸리티 함수...
}

// 상품 상태 Enum
export enum ProductStatus {
  ACTIVE = 'ACTIVE',
  SOLD_OUT = 'SOLD_OUT',
  DELETED = 'DELETED'
}
```

### UI 관련 타입 (`ui-types.ts`)

```typescript
// entities/product/model/ui-types.ts
import { ProductCategory } from './constants';

// UI 모델 - 컴포넌트용 타입
export interface ProductDisplayProps {
  id: number;
  name: string;
  price: number;
  originalPrice: number;
  thumbnailImageUrl?: string;
  category: ProductCategory;
  inStock: boolean;
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
    originalPrice: number;
    price: number;
    description: string;
    thumbnailImageUrl: string;
    category: ProductCategory;
    stock: number;
  }

  export interface Update {
    name: string;
    originalPrice: number;
    price: number;
    description: string;
    thumbnailImageUrl: string;
    category: ProductCategory;
  }
  
  // 기타 요청 타입...
}

// API 응답 타입
export namespace ProductResponse {
  export interface Detail {
    id: number;
    name: string;
    originalPrice: number;
    price: number;
    description: string;
    thumbnailImageUrl: string;
    category: ProductCategory;
    status: string;
    stock: number;
    // 기타 필드...
  }

  export interface List {
    id: number;
    name: string;
    price: number;
    // 기타 필드...
  }
}
```

### 리포지토리 (`api/repository.ts`)

```typescript
// entities/product/api/repository.ts
import { apiInstance } from '@shared/api';
import { ProductRequest, ProductResponse } from './types';
import { mapToProduct, mapToProducts } from './mappers';

export const productRepository = {
  // 상품 목록 조회
  async getProducts() {
    const response = await apiInstance.get<ProductResponse.List[]>('/api/products');
    return mapToProducts(response.data);
  },

  // 단일 상품 조회
  async getProduct(id: number) {
    const response = await apiInstance.get<ProductResponse.Detail>(`/api/products/${id}`);
    return mapToProduct(response.data);
  },

  // 상품 생성
  async createProduct(productData: ProductRequest.Create) {
    const response = await apiInstance.post<number>('/api/products', productData);
    return response.data;
  },

  // 기타 API 요청 함수...
};
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
  originalPrice: response.originalPrice,
  price: response.price,
  description: response.description,
  thumbnailImageUrl: response.thumbnailImageUrl,
  category: response.category,
  inStock: response.stock > 0,
  status: response.status as ProductStatus,
  stock: {
    quantity: response.stock
  }
});

// API 목록 응답 → 도메인 모델 매핑
export const mapToProducts = (responses: ProductResponse.List[]): Product[] => 
  responses.map(response => ({
    // 매핑 로직...
  }));

// 도메인 모델 → UI 프롭스 매핑
export const mapToProductDisplayProps = (product: Product): ProductDisplayProps => ({
  id: product.id,
  name: product.name,
  price: product.price,
  originalPrice: product.originalPrice,
  thumbnailImageUrl: product.thumbnailImageUrl,
  category: product.category,
  inStock: product.inStock
});
```

## 상태 관리 구현

엔티티의 상태 관리는 Pinia 스토어를 사용하여 구현합니다.

```typescript
// entities/product/model/productStore.ts
import { defineStore } from 'pinia';
import { Product } from './types';
import { productRepository } from '../api/repository';

export const useProductStore = defineStore('product', {
  state: () => ({
    products: [] as Product[],
    isLoading: false,
    error: null as string | null,
  }),
  
  getters: {
    getProducts: (state) => state.products,
    getProductById: (state) => (id: number) => 
      state.products.find(product => product.id === id),
    getProductsByCategory: (state) => (category: string) => 
      state.products.filter(product => product.category === category),
    // 기타 게터...
  },
  
  actions: {
    // 상품 목록 가져오기
    async fetchProducts() {
      this.isLoading = true;
      this.error = null;
      
      try {
        this.products = await productRepository.getProducts();
      } catch (err) {
        this.error = '상품을 불러오는 중 오류가 발생했습니다.';
        console.error('Error fetching products:', err);
      } finally {
        this.isLoading = false;
      }
    },
    
    // 단일 상품 가져오기
    async fetchProductById(id: number) {
      // 구현...
    },
    
    // 상품 생성
    async createProduct(productData) {
      // 구현...
    },
    
    // 기타 액션...
  }
});
```

## UI 컴포넌트 개발

엔티티는 자체 UI 컴포넌트를 가질 수 있습니다. 이는 도메인 모델을 시각적으로 표현하는 기본 컴포넌트입니다.

```vue
<!-- entities/product/ui/ProductCard.vue -->
<template>
  <div class="product-card">
    <img :src="product.thumbnailImageUrl" :alt="product.name" class="product-image" />
    <div class="product-info">
      <h3 class="product-name">{{ product.name }}</h3>
      <div class="product-price">
        <span class="current-price">{{ formatPrice(product.price) }}</span>
        <span v-if="product.originalPrice > product.price" class="original-price">
          {{ formatPrice(product.originalPrice) }}
        </span>
      </div>
      <div class="product-category">
        {{ getCategoryDescription(product.category) }}
      </div>
      <div class="product-stock" :class="{ 'out-of-stock': !product.inStock }">
        {{ product.inStock ? '재고 있음' : '품절' }}
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue';
import { Product, ProductCategory } from '../model/types';

export default defineComponent({
  name: 'ProductCard',
  props: {
    product: {
      type: Object as PropType<Product>,
      required: true
    }
  },
  setup(props) {
    // 가격 포맷팅 함수
    const formatPrice = (price: number): string => {
      return new Intl.NumberFormat('ko-KR', { 
        style: 'currency', 
        currency: 'KRW',
        maximumFractionDigits: 0
      }).format(price);
    };

    // 카테고리 설명 가져오기
    const getCategoryDescription = (category: ProductCategory): string => {
      return ProductCategory.getDescription(category);
    };

    return {
      formatPrice,
      getCategoryDescription
    };
  }
});
</script>

<style scoped>
/* 컴포넌트 스타일 */
</style>
```

## 공개 API 설계

엔티티의 공개 API는 `index.ts` 파일을 통해 정의됩니다. 이는 엔티티의 내부 구현을 캡슐화하고 외부에 노출할 기능을 명확히 합니다.

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
export { mapToProduct, mapToProducts, mapToProductDisplayProps } from './api/mappers';

// 스토어
export { useProductStore } from './model/productStore';

// UI 컴포넌트
export { default as ProductCard } from './ui/ProductCard.vue';
```

## 엔티티 간 관계 처리

엔티티 간에는 종종 관계가 있습니다. 이러한 관계는 다음과 같은 방법으로 처리할 수 있습니다:

### 참조를 통한 관계

```typescript
// entities/cart/model/types.ts
import { Product } from '@entities/product';

export interface CartItem {
  product: Product;
  quantity: number;
}

export interface CartState {
  items: CartItem[];
}
```

### 스토어 간 상호작용

```typescript
// entities/cart/model/cartStore.ts
import { defineStore } from 'pinia';
import { CartItem, CartState } from './types';
import { Product } from '@entities/product';

export const useCartStore = defineStore('cart', {
  state: (): CartState => ({
    items: [],
  }),

  actions: {
    // 장바구니에 상품 추가
    addToCart(product: Product, quantity: number = 1) {
      const existingItem = this.items.find(item => item.product.id === product.id);

      if (existingItem) {
        // 이미 장바구니에 있는 상품이면 수량만 증가
        existingItem.quantity += quantity;
      } else {
        // 새 상품이면 장바구니에 추가
        this.items.push({ product, quantity });
      }
    },
    
    // 기타 액션...
  }
});
```

## 모범 사례 및 패턴

### 1. 도메인 모델 설계 원칙

- **명확한 책임**: 각 엔티티는 명확한 책임을 가져야 합니다.
- **불변성 유지**: 가능한 한 불변 객체를 사용하여 예측 가능한 상태 변화를 유지합니다.
- **타입 안전성**: TypeScript의 타입 시스템을 최대한 활용하여 타입 안전성을 확보합니다.
- **명확한 네이밍**: 도메인 용어를 일관되게 사용하여 명확한 네이밍을 유지합니다.

### 2. API 통신 패턴

- **리포지토리 패턴**: API 요청은 리포지토리 패턴을 사용하여 구현합니다.
- **명시적 매핑**: API 응답은 항상 명시적으로 도메인 모델로 매핑합니다.
- **에러 처리**: API 오류를 일관되게 처리하고 사용자에게 적절한 피드백을 제공합니다.

### 3. 상태 관리 패턴

- **단일 책임**: 각 스토어는 하나의 엔티티 또는 관련 엔티티 그룹만 관리합니다.
- **캐싱 전략**: 자주 사용되는 데이터는 적절히 캐싱하여 불필요한 API 요청을 줄입니다.
- **상태 정규화**: 중복 데이터를 최소화하고 정규화된 상태 구조를 유지합니다.

### 4. UI 컴포넌트 패턴

- **프레젠테이션 컴포넌트**: 엔티티 UI 컴포넌트는 주로 프레젠테이션 역할에 집중합니다.
- **재사용성**: 컴포넌트는 다양한 컨텍스트에서 재사용 가능하도록 설계합니다.
- **접근성**: 모든 컴포넌트는 웹 접근성 표준을 준수해야 합니다.

### 5. 공개 API 설계 원칙

- **최소 노출**: 필요한 기능만 공개 API로 노출합니다.
- **안정적인 인터페이스**: 공개 API는 안정적이고 변경이 적어야 합니다.
- **명확한 문서화**: 공개 API는 명확하게 문서화되어야 합니다.