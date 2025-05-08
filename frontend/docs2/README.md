# Portfolio 프론트엔드 개발 가이드

이 가이드는 Portfolio 프로젝트의 프론트엔드 개발에 대한 표준과 모범 사례를 제공합니다. 이 문서는 개발자와 AI 모두가 코드 품질을 유지하면서 프로젝트를 진행할 수 있도록 설계되었습니다.

## 목차

1. [프로젝트 아키텍처](#프로젝트-아키텍처)
2. [코드 구조](#코드-구조)
3. [상태 관리](#상태-관리)
4. [코딩 스타일](#코딩-스타일)
5. [컴포넌트 개발](#컴포넌트-개발)
6. [API 통신](#api-통신)
7. [오류 처리](#오류-처리)
8. [테스트](#테스트)
9. [빌드 및 배포](#빌드-및-배포)
10. [Enum 사용 가이드](enum-guide.md)
11. [참고 자료](#참고-자료)

## 프로젝트 아키텍처

이 프로젝트는 **Feature-Sliced Design(FSD)** 아키텍처를 사용합니다. FSD는 코드를 기능 중심으로 조직화하여 확장성과 유지보수성을 높이는 방법론입니다.

### 레이어 구조

프로젝트는 다음 레이어로 구성됩니다(상위 -> 하위):

1. **app**: 애플리케이션 진입점, 전역 설정, 라우터/스토어 초기화
2. **processes**: 여러 페이지에 걸친 사용자 시나리오 및 비즈니스 로직
3. **pages**: 특정 경로에 매핑되는 UI 컴포넌트
4. **widgets**: 독립적인 UI 블록, 여러 features와 entities를 조합
5. **features**: 사용자 인터랙션 기반의 독립적인 기능 단위
6. **entities**: 핵심 비즈니스 도메인 모델 및 관련 로직/UI
7. **shared**: 특정 도메인/기능에 종속되지 않는 공통 유틸리티, UI 키트, 설정 등

### 의존성 규칙

- **단방향 의존성**: 상위 레이어는 하위 레이어를 참조할 수 있지만, 그 반대는 불가능합니다.
- **동일 레이어 간 직접 참조 제한**: 동일 레이어 내의 슬라이스 간 직접 참조는 제한됩니다.
- **shared 레이어 참조**: 모든 레이어에서 `shared` 레이어를 참조할 수 있습니다.

## 코드 구조

### 모듈 구조

각 모듈(예: `entities/product`)은 다음과 같은 구조를 가집니다:

```
entities/product/
├── api/                  # API 관련 코드
│   ├── mappers.ts        # API 응답 매핑 함수
│   ├── repository.ts     # API 요청 함수
│   ├── types.ts          # API 요청/응답 타입
│   └── index.ts          # 공개 API
├── model/                # 도메인 모델 및 상태
│   ├── constants.ts      # 상수 및 열거형
│   ├── productStore.ts   # 상태 관리 스토어
│   └── types.ts          # 도메인 타입 정의
├── ui/                   # UI 컴포넌트
│   ├── ProductCard.vue   # 상품 카드 컴포넌트
│   └── ProductDetails.vue # 상품 상세 컴포넌트
└── index.ts              # 모듈 공개 API
```

### 공개 API 패턴

모든 모듈은 `index.ts` 파일을 통해 공개 API를 명확히 정의해야 합니다:

```typescript
// entities/product/index.ts 예시
// 도메인 모델
export type { Product, ProductStock } from './model/types';
export { ProductCategory, ProductStatus } from './model/constants';

// API
export { productRepository } from './api/repository';
export { mapToProduct, mapToProducts } from './api/mappers';

// 스토어
export { useProductStore } from './model/productStore';

// UI 컴포넌트
export { default as ProductCard } from './ui/ProductCard.vue';
export { default as ProductDetails } from './ui/ProductDetails.vue';
```

## 상태 관리

### Pinia 스토어 패턴

상태 관리는 Pinia를 사용하여 구현합니다. 스토어는 도메인/기능별로 분리해야 합니다.

```typescript
// 스토어 구현 예시 (product 엔티티)
import { defineStore } from 'pinia';
import { Product } from './types';
import { productRepository } from '../api';

export const useProductStore = defineStore('product', {
  // 상태 정의
  state: () => ({
    products: [] as Product[],
    isLoading: false,
    error: null as string | null,
  }),
  
  // 게터 정의
  getters: {
    getProducts: (state) => state.products,
    getProductById: (state) => (id: number) => 
      state.products.find(product => product.id === id),
  },
  
  // 액션 정의
  actions: {
    async fetchProducts() {
      this.isLoading = true;
      this.error = null;
      
      try {
        this.products = await productRepository.getProducts();
      } catch (err: any) {
        this.error = err.message || '상품을 불러오는 중 오류가 발생했습니다.';
        console.error('Error fetching products:', err);
      } finally {
        this.isLoading = false;
      }
    },
    
    // 다른 액션들...
  }
});
```

### 스토어 사용 패턴

컴포넌트에서 스토어를 사용할 때는 다음 패턴을 따릅니다:

```vue
<script setup lang="ts">
import { useProductStore } from '@entities/product';
import { onMounted, computed } from 'vue';

// 스토어 인스턴스화
const productStore = useProductStore();

// 스토어에서 상태 접근
const products = computed(() => productStore.getProducts);
const isLoading = computed(() => productStore.isLoading);
const error = computed(() => productStore.error);

// 컴포넌트 마운트시 데이터 로드
onMounted(async () => {
  await productStore.fetchProducts();
});
</script>
```

## 코딩 스타일

### TypeScript 사용

- 모든 코드는 TypeScript로 작성합니다.
- 타입 정의는 명시적이고 구체적이어야 합니다.
- `any` 타입 사용을 최소화하고, 구체적인 타입을 사용합니다.

### Vue 컴포넌트 작성 규칙

- Composition API와 `<script setup>` 구문을 사용합니다.
- 이벤트 핸들러와 메서드 이름은 `handle` 접두사를 사용합니다.
- 컴포넌트 Props와 Emits는 명시적으로 타입을 정의합니다.
- 간단한 컴포넌트는 단일 파일에 작성하고, 복잡한 컴포넌트는 로직과 UI를 분리합니다.

```vue
<template>
  <div class="product-card">
    <img :src="product.thumbnailImageUrl" :alt="product.name" />
    <h3>{{ product.name }}</h3>
    <p class="price">{{ formatPrice(product.price) }}</p>
    <button @click="handleAddToCart">장바구니에 추가</button>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { Product } from '@entities/product';

// Props 정의
interface Props {
  product: Product;
}

const props = defineProps<Props>();

// Emits 정의
const emit = defineEmits<{
  (e: 'addToCart', productId: number): void;
}>();

// 포맷팅 함수
const formatPrice = (price: number): string => {
  return `${price.toLocaleString()}원`;
};

// 이벤트 핸들러
const handleAddToCart = () => {
  emit('addToCart', props.product.id);
};
</script>
```

## 컴포넌트 개발

### 컴포넌트 설계 원칙

1. **단일 책임 원칙**: 각 컴포넌트는 하나의 책임만 가져야 합니다.
2. **재사용성**: 공통 UI 요소는 `shared/ui`에 배치합니다.
3. **컴포넌트 계층 구조**:
   - `entities` 레이어: 도메인 모델과 관련된 기본 UI (예: ProductCard)
   - `features` 레이어: 사용자 기능과 관련된 UI (예: AddToCartButton)
   - `widgets` 레이어: 여러 기능을 조합한 복합 UI (예: ProductCatalog)
   - `shared/ui`: 재사용 가능한 기본 UI 컴포넌트 (예: Button, Input)

### 컴포넌트 상태 관리

- 지역 상태는 `ref` 또는 `reactive`로 관리합니다.
- 전역 상태는 Pinia 스토어로 관리합니다.
- 컴포넌트 간 통신은 Props와 Events를 사용합니다.
- 복잡한 부모-자식 통신은 `provide`/`inject`를 사용할 수 있습니다.

## API 통신

### API 리포지토리 패턴

API 통신은 리포지토리 패턴을 사용합니다:

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
  
  // 다른 API 메서드들...
};
```

### 데이터 매핑

API 응답은 도메인 모델로 변환하기 위한 매퍼 함수를 사용합니다:

```typescript
// entities/product/api/mappers.ts
import { Product } from '../model/types';
import { ProductResponse } from './types';
import { ProductCategory, ProductStatus } from '../model/constants';

export const mapToProduct = (response: ProductResponse.Detail): Product => ({
  id: response.id,
  name: response.name,
  originalPrice: response.originalPrice,
  price: response.price,
  description: response.description,
  thumbnailImageUrl: response.thumbnailImageUrl,
  category: response.category as ProductCategory,
  inStock: response.stock?.quantity > 0,
  status: response.status as ProductStatus,
  stock: response.stock ? {
    id: response.stock.id,
    quantity: response.stock.quantity,
    version: response.stock.version
  } : undefined
});
```

## 오류 처리

### API 오류 처리

```typescript
// API 호출에서 오류 처리
async fetchProducts() {
  this.isLoading = true;
  this.error = null;
  
  try {
    this.products = await productRepository.getProducts();
  } catch (err: any) {
    // 구체적인 오류 메시지 설정
    this.error = err.response?.data?.message || err.message || '상품을 불러오는 중 오류가 발생했습니다.';
    console.error('Error fetching products:', err);
  } finally {
    this.isLoading = false;
  }
}
```

### 사용자 친화적인 오류 표시

```vue
<template>
  <div>
    <div v-if="isLoading">로딩 중...</div>
    <div v-else-if="error" class="error-message">
      <p>{{ error }}</p>
      <button @click="handleRetry">다시 시도</button>
    </div>
    <div v-else>
      <!-- 콘텐츠 표시 -->
    </div>
  </div>
</template>
```

## 테스트

현재 프로젝트에는 자동화된 테스트가 구현되어 있지 않지만, 다음과 같은 테스트 방법을 추가할 수 있습니다:

### 단위 테스트

- Vitest를 사용한 유닛 테스트
- 컴포넌트, 스토어, 유틸리티 함수에 대한 테스트 작성
- 테스트 파일은 테스트 대상 파일과 같은 디렉토리에 `*.test.ts` 형식으로 배치

### 컴포넌트 테스트

```typescript
// ProductCard.test.ts 예시
import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import ProductCard from './ProductCard.vue';
import { Product } from '../model/types';
import { ProductCategory, ProductStatus } from '../model/constants';

describe('ProductCard', () => {
  const mockProduct: Product = {
    id: 1,
    name: '테스트 상품',
    originalPrice: 10000,
    price: 8000,
    description: '테스트 상품 설명',
    category: ProductCategory.ELECTRONICS,
    inStock: true,
    status: ProductStatus.ACTIVE
  };

  it('renders product information correctly', () => {
    const wrapper = mount(ProductCard, {
      props: {
        product: mockProduct
      }
    });

    expect(wrapper.find('h3').text()).toBe('테스트 상품');
    expect(wrapper.find('.price').text()).toBe('8,000원');
  });

  it('emits addToCart event with product id when button is clicked', async () => {
    const wrapper = mount(ProductCard, {
      props: {
        product: mockProduct
      }
    });

    await wrapper.find('button').trigger('click');
    expect(wrapper.emitted('addToCart')).toBeTruthy();
    expect(wrapper.emitted('addToCart')![0]).toEqual([1]);
  });
});
```

## 빌드 및 배포

### 빌드 명령어

```bash
# 개발 서버 실행
npm run dev

# SSR 개발 서버 실행
npm run dev:ssr

# 프로덕션 빌드
npm run build

# SSR 프로덕션 빌드
npm run build:ssr
```

### 환경 변수 설정

- `.env.local`: 로컬 개발 환경
- `.env.production`: 프로덕션 환경

```
VITE_API_URL=http://api.example.com
VITE_BASE_URL=http://frontend.example.com
VITE_BASE_PORT=8080
```

## 참고 자료

- [Vue 3 공식 문서](https://v3.vuejs.org/)
- [Pinia 공식 문서](https://pinia.vuejs.org/)
- [Feature-Sliced Design](https://feature-sliced.design/)
- [TypeScript 공식 문서](https://www.typescriptlang.org/docs/)
