# 코드 컨벤션 가이드

## 목차

1. [개요](#개요)
2. [파일 및 폴더 구조](#파일-및-폴더-구조)
3. [명명 규칙](#명명-규칙)
4. [TypeScript 사용 가이드](#typescript-사용-가이드)
5. [Vue 컴포넌트 작성 가이드](#vue-컴포넌트-작성-가이드)
6. [API 통신 패턴](#api-통신-패턴)
7. [상태 관리 패턴](#상태-관리-패턴)
8. [스타일링 가이드](#스타일링-가이드)
9. [코드 포맷팅 및 린팅](#코드-포맷팅-및-린팅)
10. [문서화](#문서화)

## 개요

이 문서는 프론트엔드 코드베이스의 일관성을 유지하기 위한 코드 컨벤션 가이드를 제공합니다. 이 가이드를 따르면 코드의 가독성, 유지보수성, 확장성을 향상시킬 수 있습니다.

우리 프로젝트는 다음과 같은 핵심 기술 스택을 사용합니다:

- **프레임워크**: Vue 3
- **상태 관리**: Pinia
- **타입 시스템**: TypeScript
- **스타일링**: Tailwind CSS
- **아키텍처**: FSD (Feature-Sliced Design)

## 파일 및 폴더 구조

### 레이어 구조

프로젝트는 FSD 아키텍처에 따라 다음과 같은 레이어로 구성됩니다:

```
src/
├── app/          # 애플리케이션 설정, 진입점
├── processes/    # 비즈니스 프로세스
├── pages/        # 페이지 컴포넌트
├── widgets/      # 위젯 (독립적인 UI 블록)
├── features/     # 사용자 기능
├── entities/     # 비즈니스 엔티티
└── shared/       # 공유 유틸리티, 타입, UI
```

레이어 간 의존성 방향은 항상 위에서 아래로 흐릅니다. (예: `features`는 `entities`에 의존할 수 있지만, `entities`는 `features`에 의존할 수 없습니다.)

### 슬라이스 구조

각 레이어 내부에서는 도메인이나 기능에 따라 슬라이스로 구성됩니다. 각 슬라이스는 다음과 같은 세그먼트를 포함합니다:

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

## 명명 규칙

### 파일 및 폴더 명명

- **폴더 이름**:
  - 레이어 폴더: 복수형, camelCase (예: `entities`, `features`)
  - 슬라이스 폴더: 단수형, camelCase 또는 kebab-case (예: `product`, `product-management`)
  - 세그먼트 폴더: camelCase (예: `api`, `model`, `ui`)

- **파일 이름**:
  - Vue 컴포넌트: PascalCase (예: `ProductCard.vue`, `LoginForm.vue`)
  - TypeScript 파일: camelCase (예: `repository.ts`, `types.ts`)
  - 상수/타입 전용 파일: camelCase (예: `constants.ts`, `types.ts`)

### 변수, 함수, 클래스 명명

- **변수 및 함수**: camelCase
  ```typescript
  const productList = [];
  function fetchProducts() {}
  ```

- **클래스 및 컴포넌트**: PascalCase
  ```typescript
  class ProductService {}
  const ProductCard = defineComponent({});
  ```

- **상수**: 대문자 스네이크 케이스
  ```typescript
  const MAX_RETRY_COUNT = 3;
  ```

- **타입 및 인터페이스**: PascalCase
  ```typescript
  interface User {}
  type ProductId = number;
  ```

- **Enum**: PascalCase
  ```typescript
  enum ProductStatus {}
  ```

## TypeScript 사용 가이드

### 타입 정의

- 모든 변수, 함수, 매개변수에 명시적인 타입을 지정합니다.
- `any` 타입 사용을 지양하고, 대신 구체적인 타입을 사용합니다.
- 유니온 타입을 활용하여 여러 타입 중 하나를 명시합니다.

```typescript
// 좋은 예시
function getProduct(id: number): Product | undefined {
  return products.find(product => product.id === id);
}

// 나쁜 예시
function getProduct(id: any): any {
  return products.find(product => product.id === id);
}
```

### 인터페이스와 타입

- 객체 구조를 정의할 때는 `interface`를 사용합니다.
- 유니온 타입, 교차 타입 등 복잡한 타입 정의에는 `type`을 사용합니다.

```typescript
// 인터페이스 사용
interface Product {
  id: number;
  name: string;
  price: number;
}

// 타입 사용
type ProductId = number;
type ProductStatus = 'active' | 'soldOut' | 'deleted';
```

### Enum 패턴

Enum은 네임스페이스 패턴을 사용하여 확장합니다:

```typescript
// Enum 정의
export enum ProductCategory {
  ELECTRONICS = 'ELECTRONICS',
  FURNITURE = 'FURNITURE',
  // ...
}

// 네임스페이스로 확장
export namespace ProductCategory {
  // 설명 매핑
  export const descriptions: Record<ProductCategory, string> = {
    [ProductCategory.ELECTRONICS]: '전자제품',
    // ...
  };
  
  // 설명 가져오기
  export function getDescription(category: ProductCategory): string {
    return descriptions[category] || category;
  }
  
  // 모든 항목 가져오기
  export function entries(): Array<{ code: ProductCategory; description: string }> {
    return Object.values(ProductCategory)
      .filter(value => typeof value === 'string')
      .map(code => ({
        code: code as ProductCategory,
        description: descriptions[code as ProductCategory] || code as string
      }));
  }
}
```

## Vue 컴포넌트 작성 가이드

### 컴포넌트 스타일

- Vue 3 Composition API와 `<script setup>` 문법을 사용합니다.
- 타입스크립트를 활용하여 props와 emit을 명시적으로 정의합니다.

```vue
<template>
  <div class="product-card">
    <h2>{{ product.name }}</h2>
    <p>{{ formatPrice(product.price) }}</p>
    <button @click="addToCart">장바구니에 추가</button>
  </div>
</template>

<script setup lang="ts">
import { defineProps, defineEmits } from 'vue';
import type { Product } from '@entities/product';
import { formatPrice } from '@shared/lib';

// Props 정의
const props = defineProps<{
  product: Product;
}>();

// Emits 정의
const emit = defineEmits<{
  (e: 'add-to-cart', productId: number): void;
}>();

// 메서드
const addToCart = () => {
  emit('add-to-cart', props.product.id);
};
</script>

<style scoped>
.product-card {
  /* 스타일링 */
}
</style>
```

### Props 및 Emits 정의

- Props에는 타입과 필수 여부를 명시합니다.
- Emits에는 이벤트 이름과 매개변수 타입을 명시합니다.

```typescript
// Props 정의
const props = defineProps<{
  product: Product;
  showPrice?: boolean;
}>();

// 기본값 설정
withDefaults(defineProps<{
  product: Product;
  showPrice?: boolean;
}>(), {
  showPrice: true
});

// Emits 정의
const emit = defineEmits<{
  (e: 'add-to-cart', productId: number): void;
  (e: 'select-product', productId: number): void;
}>();
```

### 컴포넌트 구성

- 단일 책임 원칙을 따라 각 컴포넌트는 하나의 역할만 수행하도록 합니다.
- 재사용 가능한 컴포넌트는 `shared/ui` 폴더에 배치합니다.
- 엔티티별 컴포넌트는 해당 엔티티의 `ui` 폴더에 배치합니다.

## API 통신 패턴

### 리포지토리 패턴

API 통신은 리포지토리 패턴을 사용하여 구현합니다:

```typescript
// entities/product/api/repository.ts
export const productRepository = {
  async getProducts(): Promise<Product[]> {
    const response = await apiInstance.get<ProductResponse.List[]>('/api/products');
    return mapToProducts(response.data);
  },
  
  async getProduct(id: number): Promise<Product> {
    const response = await apiInstance.get<ProductResponse.Detail>(`/api/products/${id}`);
    return mapToProduct(response.data);
  },
  
  // ... 기타 메서드
};
```

### 매퍼 패턴

API 응답을 도메인 모델로 변환하는 매퍼 함수를 구현합니다:

```typescript
// entities/product/api/mappers.ts
export const mapToProduct = (response: ProductResponse.Detail): Product => ({
  id: response.id,
  name: response.name,
  price: response.price,
  // ... 기타 속성 매핑
});

export const mapToProducts = (responses: ProductResponse.List[]): Product[] => 
  responses.map(response => ({
    id: response.id,
    name: response.name,
    // ... 기타 속성 매핑
  }));
```

### 에러 처리

API 에러 처리는 일관된 방식으로 구현합니다:

```typescript
try {
  const products = await productRepository.getProducts();
  // 성공 처리
} catch (err) {
  const apiError = handleApiError(err);
  // 에러 처리
  console.error('API Error:', apiError.message);
}
```

## 상태 관리 패턴

### Pinia 스토어

상태 관리는 Pinia를 사용하여 구현합니다:

```typescript
// entities/product/model/productStore.ts
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
  },
  
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
    // ... 기타 액션
  }
});
```

### 컴포넌트에서의 스토어 사용

컴포넌트에서는 `storeToRefs`를 사용하여 반응성을 유지합니다:

```vue
<script setup>
import { storeToRefs } from 'pinia';
import { useProductStore } from '@entities/product';

const productStore = useProductStore();

// storeToRefs로 반응성 유지
const { products, isLoading, error } = storeToRefs(productStore);

// 액션 직접 사용
const { fetchProducts } = productStore;

onMounted(() => {
  fetchProducts();
});
</script>
```

## 스타일링 가이드

### Tailwind CSS 사용

- 기본적으로 Tailwind CSS를 사용하여 스타일링합니다.
- 복잡한 컴포넌트는 `scoped` 스타일을 사용할 수 있습니다.

```vue
<template>
  <div class="bg-white rounded-lg shadow-md p-4 hover:shadow-lg transition-shadow">
    <h2 class="text-xl font-semibold text-gray-800">{{ product.name }}</h2>
    <p class="text-gray-600 mt-2">{{ formatPrice(product.price) }}</p>
    <button class="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded mt-4">
      장바구니에 추가
    </button>
  </div>
</template>

<style scoped>
/* 복잡한 스타일은 여기에 작성 */
</style>
```

### 컬러 시스템

- Tailwind CSS의 기본 색상 팔레트를 활용합니다.
- 프로젝트 전용 색상은 `tailwind.config.js`에 정의합니다.

```javascript
// tailwind.config.js
module.exports = {
  theme: {
    extend: {
      colors: {
        primary: {
          light: '#4da6ff',
          DEFAULT: '#0080ff',
          dark: '#0066cc',
        },
        // ... 기타 커스텀 색상
      },
    },
  },
};
```

## 코드 포맷팅 및 린팅

### ESLint 및 Prettier

- ESLint를 사용하여 코드 품질을 검사합니다.
- Prettier를 사용하여 코드 형식을 통일합니다.

### Import 순서

import 문은 다음 순서로 정렬합니다:

1. 외부 라이브러리 (Vue, Pinia 등)
2. 절대 경로 임포트 (@로 시작하는 경로)
3. 상대 경로 임포트 (./로 시작하는 경로)

```typescript
// 외부 라이브러리
import { defineComponent, ref, computed } from 'vue';
import { storeToRefs } from 'pinia';

// 절대 경로 임포트
import { useProductStore } from '@entities/product';
import { formatPrice } from '@shared/lib';

// 상대 경로 임포트
import { mapToProduct } from './mappers';
```

## 문서화

### 주석 작성

- 복잡한 로직은 주석으로 설명합니다.
- 공개 API 및 컴포넌트는 JSDoc 형식의 주석을 사용합니다.

```typescript
/**
 * 상품 카드 컴포넌트
 * 
 * @component ProductCard
 * @description 상품 정보를 카드 형태로 표시하는 컴포넌트
 * 
 * @example
 * <product-card
 *   :product="product"
 *   @add-to-cart="handleAddToCart"
 * />
 */
```

### README 및 문서화

- 각 슬라이스는 간단한 README.md 파일을 포함할 수 있습니다.
- 복잡한 기능은 별도의 문서를 작성합니다.

이상의 컨벤션을 따르면 코드베이스의 일관성을 유지하고, 새로운 개발자가 쉽게 적응할 수 있는 환경을 만들 수 있습니다.