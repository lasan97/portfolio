# 상태 관리 가이드

## 목차

1. [상태 관리 개요](#상태-관리-개요)
2. [Pinia를 사용한 상태 관리](#pinia를-사용한-상태-관리)
3. [FSD에서의 상태 관리 구조](#fsd에서의-상태-관리-구조)
4. [스토어 구현 패턴](#스토어-구현-패턴)
5. [비동기 액션 처리](#비동기-액션-처리)
6. [상태 정규화](#상태-정규화)
7. [스토어 간 상호작용](#스토어-간-상호작용)
8. [컴포넌트에서의 스토어 사용](#컴포넌트에서의-스토어-사용)
9. [모범 사례](#모범-사례)

## 상태 관리 개요

상태 관리는 애플리케이션의 데이터 흐름을 체계적으로 관리하는 방법입니다. 우리 프로젝트에서는 Pinia를 사용하여 상태를 관리합니다. 상태는 다음과 같이 분류됩니다:

1. **엔티티 상태**: 비즈니스 엔티티(Product, Order, User 등)의 데이터 상태
2. **UI 상태**: 모달 표시 여부, 로딩 상태, 선택된 탭 등의 UI 관련 상태
3. **세션 상태**: 인증 토큰, 사용자 정보 등의 세션 관련 상태
4. **애플리케이션 상태**: 전역 설정, 테마 등의 애플리케이션 수준 상태

## Pinia를 사용한 상태 관리

Pinia는 Vue 3 생태계에서 권장되는 상태 관리 라이브러리로, 다음과 같은 특징이 있습니다:

- TypeScript와의 우수한 통합
- Composition API와의 자연스러운 조합
- 개발자 도구 지원
- 모듈식 설계

Pinia 스토어는 세 가지 주요 개념으로 구성됩니다:

1. **State**: 애플리케이션 상태를 정의하는 객체
2. **Getters**: 상태를 기반으로 한 계산된 값 (Vue의 computed와 유사)
3. **Actions**: 상태를 변경하는 메서드 (비동기 작업 포함)

## FSD에서의 상태 관리 구조

FSD 아키텍처에서는 상태 관리를 다음과 같이 구성합니다:

### 1. entities/{entity}/model

엔티티 레이어에서는 해당 엔티티의 상태를 관리합니다. 예를 들어, `entities/product/model/productStore.ts`에서는 Product 엔티티의 상태를 관리합니다.

### 2. features/{feature}/model

기능 레이어에서는 특정 기능과 관련된 상태를 관리합니다. 예를 들어, `features/cart/model/cartStore.ts`에서는 장바구니 기능 관련 상태를 관리합니다.

### 3. widgets/{widget}/model

위젯 레이어에서는 특정 위젯과 관련된 UI 상태를 관리합니다.

### 4. app/store

애플리케이션 레이어에서는 전역 상태를 관리합니다. 예를 들어, 테마, 언어 설정 등이 여기에 해당합니다.

## 스토어 구현 패턴

### 1. 엔티티 스토어 패턴

```typescript
// entities/product/model/productStore.ts
import { defineStore } from 'pinia';
import { Product } from './types';
import { productRepository } from '../api';

export const useProductStore = defineStore('product', {
  state: () => ({
    products: [] as Product[],
    productMap: new Map<number, Product>(),
    isLoading: false,
    error: null as string | null,
  }),
  
  getters: {
    getProducts: (state) => state.products,
    
    getProductById: (state) => (id: number) => 
      state.products.find(product => product.id === id),
    
    getProductsByCategory: (state) => (category: string) => 
      state.products.filter(product => product.category === category),
    
    getInStockProducts: (state) => 
      state.products.filter(product => product.inStock),
  },
  
  actions: {
    async fetchProducts() {
      this.isLoading = true;
      this.error = null;
      
      try {
        this.products = await productRepository.getProducts();
        
        // 맵에 상품 인덱싱
        this.products.forEach(product => {
          this.productMap.set(product.id, product);
        });
      } catch (err: any) {
        this.error = err.message || '상품을 불러오는 중 오류가 발생했습니다.';
        console.error('Error fetching products:', err);
      } finally {
        this.isLoading = false;
      }
    },
    
    async fetchProductById(id: number) {
      // 이미 맵에 있는 상품인지 확인
      if (this.productMap.has(id)) {
        return this.productMap.get(id)!;
      }
      
      this.isLoading = true;
      this.error = null;
      
      try {
        const product = await productRepository.getProduct(id);
        
        // 상품 맵에 추가
        this.productMap.set(id, product);
        
        // 상품 목록에 이미 존재하는 상품인지 확인하고 업데이트 또는 추가
        const index = this.products.findIndex(p => p.id === id);
        if (index !== -1) {
          this.products[index] = product;
        } else {
          this.products.push(product);
        }
        
        return product;
      } catch (err: any) {
        this.error = err.message || '상품 정보를 불러오는 중 오류가 발생했습니다.';
        console.error('Error fetching product details:', err);
        throw err;
      } finally {
        this.isLoading = false;
      }
    },
    
    // ... 기타 상태 변경 액션
  }
});
```

### 2. 기능 스토어 패턴

```typescript
// features/cart/model/cartStore.ts
import { defineStore } from 'pinia';
import { Product, useProductStore } from '@entities/product';
import { CartItem } from './types';
import { cartRepository } from '../api';

export const useCartStore = defineStore('cart', {
  state: () => ({
    items: [] as CartItem[],
    isLoading: false,
    error: null as string | null,
  }),
  
  getters: {
    totalItems: (state) => 
      state.items.reduce((total, item) => total + item.quantity, 0),
    
    totalPrice: (state) => 
      state.items.reduce((total, item) => total + item.price * item.quantity, 0),
    
    isEmpty: (state) => state.items.length === 0,
  },
  
  actions: {
    async fetchCart() {
      this.isLoading = true;
      this.error = null;
      
      try {
        const cartItems = await cartRepository.getCart();
        this.items = cartItems;
      } catch (err: any) {
        this.error = err.message || '장바구니를 불러오는 중 오류가 발생했습니다.';
        console.error('Error fetching cart:', err);
      } finally {
        this.isLoading = false;
      }
    },
    
    async addToCart(product: Product, quantity: number) {
      this.isLoading = true;
      this.error = null;
      
      try {
        await cartRepository.addToCart(product.id, quantity);
        
        // 낙관적 업데이트
        const existingItem = this.items.find(item => item.productId === product.id);
        
        if (existingItem) {
          existingItem.quantity += quantity;
        } else {
          this.items.push({
            productId: product.id,
            name: product.name,
            price: product.price,
            quantity,
            thumbnailImageUrl: product.thumbnailImageUrl
          });
        }
      } catch (err: any) {
        this.error = err.message || '장바구니에 상품을 추가하는 중 오류가 발생했습니다.';
        console.error('Error adding to cart:', err);
        
        // 장바구니 다시 로드 (낙관적 업데이트 롤백)
        await this.fetchCart();
        
        throw err;
      } finally {
        this.isLoading = false;
      }
    },
    
    // ... 기타 장바구니 관련 액션
  }
});
```

### 3. UI 상태 스토어 패턴

```typescript
// widgets/productList/model/productListStore.ts
import { defineStore } from 'pinia';
import { ProductCategory } from '@entities/product';

export const useProductListStore = defineStore('productList', {
  state: () => ({
    searchQuery: '',
    selectedCategory: '' as string,
    sortOption: 'nameAsc' as SortOption,
    showInStockOnly: false,
    page: 1,
    pageSize: 12,
  }),
  
  getters: {
    paginationInfo: (state) => ({
      page: state.page,
      pageSize: state.pageSize
    }),
    
    filterParams: (state) => ({
      category: state.selectedCategory,
      inStock: state.showInStockOnly,
      search: state.searchQuery
    }),
  },
  
  actions: {
    setSearchQuery(query: string) {
      this.searchQuery = query;
      this.page = 1; // 검색어 변경 시 첫 페이지로 이동
    },
    
    setCategory(category: string) {
      this.selectedCategory = category;
      this.page = 1; // 카테고리 변경 시 첫 페이지로 이동
    },
    
    setSortOption(option: SortOption) {
      this.sortOption = option;
    },
    
    toggleInStockOnly() {
      this.showInStockOnly = !this.showInStockOnly;
      this.page = 1; // 필터 변경 시 첫 페이지로 이동
    },
    
    setPage(page: number) {
      this.page = page;
    },
    
    resetFilters() {
      this.searchQuery = '';
      this.selectedCategory = '';
      this.sortOption = 'nameAsc';
      this.showInStockOnly = false;
      this.page = 1;
    }
  }
});

type SortOption = 'nameAsc' | 'nameDesc' | 'priceAsc' | 'priceDesc';
```

## 비동기 액션 처리

비동기 작업은 주로 액션에서 처리합니다. 비동기 액션 처리 패턴은 다음과 같습니다:

1. 로딩 상태 설정
2. 에러 상태 초기화
3. try-catch 블록에서 비동기 작업 수행
4. 성공 시 상태 업데이트
5. 실패 시 에러 상태 업데이트
6. finally 블록에서 로딩 상태 해제

```typescript
async fetchData() {
  this.isLoading = true;
  this.error = null;
  
  try {
    const data = await repository.fetchData();
    this.data = data;
  } catch (err: any) {
    this.error = err.message || '데이터를 불러오는 중 오류가 발생했습니다.';
    console.error('Error fetching data:', err);
  } finally {
    this.isLoading = false;
  }
}
```

## 상태 정규화

대규모 애플리케이션에서는 상태를 정규화하여 관리하는 것이 좋습니다. 정규화된 상태는 다음과 같은 이점이 있습니다:

- 중복 데이터 방지
- 업데이트 효율성 향상
- 참조 무결성 유지

```typescript
// 정규화된 상태 예시
state: () => ({
  entities: {} as Record<number, Product>, // 키-값 맵
  ids: [] as number[], // ID 목록
  // ... 기타 상태
}),

getters: {
  getAllProducts: (state) => 
    state.ids.map(id => state.entities[id]),
  
  getProductById: (state) => (id: number) => 
    state.entities[id],
  
  // ... 기타 게터
},

actions: {
  setProducts(products: Product[]) {
    // 엔티티 맵 업데이트
    const entities = {...this.entities};
    const ids: number[] = [];
    
    products.forEach(product => {
      entities[product.id] = product;
      ids.push(product.id);
    });
    
    this.entities = entities;
    this.ids = ids;
  },
  
  updateProduct(product: Product) {
    // 단일 엔티티 업데이트
    this.entities[product.id] = product;
    
    // ID가 없는 경우 추가
    if (!this.ids.includes(product.id)) {
      this.ids.push(product.id);
    }
  },
  
  // ... 기타 액션
}
```

## 스토어 간 상호작용

스토어는 다른 스토어의 상태와 액션을 사용할 수 있습니다.

### 1. 스토어 컴포지션

```typescript
// features/productDetail/model/productDetailStore.ts
import { defineStore } from 'pinia';
import { useProductStore } from '@entities/product';
import { useCartStore } from '@features/cart';

export const useProductDetailStore = defineStore('productDetail', {
  state: () => ({
    selectedQuantity: 1,
    // ... 기타 상태
  }),
  
  actions: {
    async addToCart(productId: number) {
      const productStore = useProductStore();
      const cartStore = useCartStore();
      
      const product = await productStore.fetchProductById(productId);
      
      if (product) {
        await cartStore.addToCart(product, this.selectedQuantity);
        this.selectedQuantity = 1; // 수량 초기화
      }
    },
    
    // ... 기타 액션
  }
});
```

### 2. 액션 체이닝

```typescript
// features/checkout/model/checkoutStore.ts
import { defineStore } from 'pinia';
import { useCartStore } from '@features/cart';
import { useOrderStore } from '@entities/order';

export const useCheckoutStore = defineStore('checkout', {
  // ... 상태 및 게터
  
  actions: {
    async placeOrder() {
      const cartStore = useCartStore();
      const orderStore = useOrderStore();
      
      // 장바구니가 비어있는지 확인
      if (cartStore.isEmpty) {
        throw new Error('장바구니가 비어 있습니다.');
      }
      
      // 주문 생성
      const orderId = await orderStore.createOrder({
        items: cartStore.items,
        totalPrice: cartStore.totalPrice,
        // ... 기타 주문 정보
      });
      
      // 주문 성공 시 장바구니 비우기
      await cartStore.clearCart();
      
      return orderId;
    },
    
    // ... 기타 액션
  }
});
```

## 컴포넌트에서의 스토어 사용

### 1. Composition API를 사용한 스토어 사용

```vue
<script setup>
import { onMounted, computed } from 'vue';
import { useProductStore } from '@entities/product';
import { useCartStore } from '@features/cart';

const productStore = useProductStore();
const cartStore = useCartStore();

// 상태 및 게터 사용
const products = computed(() => productStore.getProducts);
const isLoading = computed(() => productStore.isLoading);
const error = computed(() => productStore.error);

// 액션 사용
const fetchProducts = () => productStore.fetchProducts();
const addToCart = (product) => cartStore.addToCart(product, 1);

onMounted(() => {
  fetchProducts();
});
</script>

<template>
  <div>
    <div v-if="isLoading">로딩 중...</div>
    <div v-else-if="error">{{ error }}</div>
    <div v-else>
      <div v-for="product in products" :key="product.id">
        {{ product.name }}
        <button @click="addToCart(product)">장바구니에 추가</button>
      </div>
    </div>
  </div>
</template>
```

### 2. 스토어 상태 분해

```vue
<script setup>
import { storeToRefs } from 'pinia';
import { useProductStore } from '@entities/product';

const productStore = useProductStore();

// storeToRefs를 사용하여 반응성 유지
const { products, isLoading, error } = storeToRefs(productStore);

// 액션은 분해하지 않고 직접 사용
const { fetchProducts } = productStore;

onMounted(() => {
  fetchProducts();
});
</script>
```

## 모범 사례

### 1. 상태 분리

상태를 적절한 레벨로 분리하여 관리합니다:

- **엔티티 상태**: 도메인 모델 데이터
- **기능 상태**: 특정 기능 관련 데이터
- **UI 상태**: UI 컴포넌트 관련 데이터
- **세션 상태**: 인증 정보 등 세션 관련 데이터

### 2. 단일 책임 원칙

각 스토어는 단일 책임을 가져야 합니다. 예를 들어, 상품 관리 스토어는 상품 관련 상태만 관리해야 합니다.

### 3. 불변성 유지

상태 변경 시 불변성을 유지하면 예측 가능한 상태 변화를 보장할 수 있습니다:

```typescript
// 좋은 예
this.items = [...this.items, newItem];

// 나쁜 예
this.items.push(newItem);
```

### 4. 액션 명명 규칙

액션 이름은 의도를 명확하게 표현해야 합니다:

- `fetch*`: 데이터 로드 (API 호출)
- `set*`: 단순 상태 설정
- `add*`: 컬렉션에 항목 추가
- `remove*`: 컬렉션에서 항목 제거
- `update*`: 기존 항목 업데이트
- `toggle*`: 불리언 상태 토글
- `reset*`: 상태 초기화

### 5. 게터 캐싱 활용

계산이 복잡한 게터는 캐싱을 활용하여 성능을 향상시킵니다:

```typescript
getters: {
  // 단순 게터
  allProducts: (state) => state.products,
  
  // 매개변수가 있는 게터 (자동 캐싱 안됨)
  getProductById: (state) => {
    // 캐싱을 위한 클로저 생성
    const cache = new Map<number, Product>();
    
    return (id: number) => {
      if (cache.has(id)) {
        return cache.get(id);
      }
      
      const product = state.products.find(p => p.id === id);
      if (product) {
        cache.set(id, product);
      }
      
      return product;
    };
  },
}
```

### 6. 디버깅 지원

개발 중 디버깅을 위해 로깅을 추가하는 것이 좋습니다:

```typescript
// 개발 환경에서만 상태 변경 로깅
if (import.meta.env.DEV) {
  const productStore = useProductStore();
  productStore.$subscribe((mutation, state) => {
    console.log('Product store changed:', mutation.type, mutation.payload);
    console.log('New state:', state);
  });
}
```

### 7. 스토어 모듈화

스토어가 커지면 논리적인 단위로 분리하는 것이 좋습니다:

```typescript
// useAuthStore.ts
export const useAuthStore = defineStore('auth', {
  // 로그인, 로그아웃 등 인증 관련 상태 및 액션
});

// useProfileStore.ts
export const useProfileStore = defineStore('profile', {
  // 사용자 프로필 관리 관련 상태 및 액션
});

// usePreferencesStore.ts
export const usePreferencesStore = defineStore('preferences', {
  // 사용자 설정 관련 상태 및 액션
});
```

이상의 모범 사례를 따르면 상태 관리 코드를 더 효과적으로 구성하고 유지보수할 수 있습니다.
