# 모범 사례 및 패턴

## 목차

1. [FSD 구현 모범 사례](#fsd-구현-모범-사례)
2. [코드 스타일 및 일관성](#코드-스타일-및-일관성)
3. [성능 최적화 패턴](#성능-최적화-패턴)
4. [타입스크립트 활용 모범 사례](#타입스크립트-활용-모범-사례)
5. [컴포넌트 개발 모범 사례](#컴포넌트-개발-모범-사례)
6. [상태 관리 모범 사례](#상태-관리-모범-사례)
7. [API 통합 모범 사례](#api-통합-모범-사례)
8. [테스트 및 디버깅 전략](#테스트-및-디버깅-전략)
9. [패키지 및 의존성 관리](#패키지-및-의존성-관리)
10. [리팩토링 가이드라인](#리팩토링-가이드라인)

## FSD 구현 모범 사례

### 1. 레이어 간 의존성 규칙 준수

- 상위 레이어는 하위 레이어에만 의존할 수 있습니다.
- 하위 레이어는 상위 레이어에 의존할 수 없습니다.
- 의존성 방향: `app → processes → pages → widgets → features → entities → shared`

```typescript
// 좋은 예: 페이지가 위젯과 엔티티를 임포트
// pages/products/ui/ProductsPage.vue
import { ProductList } from '@widgets/productList';
import { useProductStore } from '@entities/product';

// 나쁜 예: 엔티티가 기능을 임포트
// entities/product/ui/ProductCard.vue
import { AddToCartButton } from '@features/cart'; // ❌ 의존성 규칙 위반
```

### 2. 슬라이스 독립성 유지

동일 레이어 내에서는 슬라이스 간 직접 의존하지 않아야 합니다:

```typescript
// 나쁜 예: 한 엔티티가 다른 엔티티를 직접 임포트
// entities/product/model/productStore.ts
import { useUserStore } from '@entities/user'; // ❌ 슬라이스 간 직접 의존

// 좋은 예: 필요한 데이터를 파라미터로 전달
// entities/product/model/productStore.ts
export const useProductStore = defineStore('product', {
  // ...
  actions: {
    async fetchProductsForUser(userId: number) { // ✅ 의존성 주입
      // ...
    }
  }
});
```

### 3. 공개 API로만 소통하기

각 슬라이스는 `index.ts` 파일을 통해 공개 API만 노출해야 합니다:

```typescript
// entities/product/index.ts
export type { Product } from './model/types';
export { ProductCategory } from './model/constants';
export { useProductStore } from './model/productStore';
export { default as ProductCard } from './ui/ProductCard.vue';

// 좋은 예: 공개 API를 통한 임포트
// pages/products/ui/ProductsPage.vue
import { useProductStore, ProductCard, ProductCategory } from '@entities/product';

// 나쁜 예: 내부 구현에 직접 접근
// pages/products/ui/ProductsPage.vue
import { useProductStore } from '@entities/product/model/productStore'; // ❌ 내부 구현에 의존
```

### 4. 책임 분리

각 레이어와 슬라이스는 명확한 책임을 가져야 합니다:

```typescript
// 좋은 예: 엔티티는 도메인 모델과 기본 CRUD 담당
// entities/product/api/repository.ts
export const productRepository = {
  async getProducts(): Promise<Product[]> {
    // ...
  },
  // ...
};

// 좋은 예: 기능은 사용자 상호작용 담당
// features/productManagement/api/requests.ts
export const productManagementApi = {
  async createProduct(formData: ProductFormData): Promise<number> {
    // ...
  },
  // ...
};
```

## 코드 스타일 및 일관성

### 1. 일관된 네이밍 컨벤션

```typescript
// 컴포넌트 이름: PascalCase
const ProductCard = defineComponent({
  // ...
});

// 변수, 함수: camelCase
const productList = ref<Product[]>([]);
const fetchProducts = async () => {
  // ...
};

// 타입, 인터페이스: PascalCase
interface ProductData {
  // ...
}

// 상수: UPPER_SNAKE_CASE (일반적인 경우)
const API_BASE_URL = 'https://api.example.com';
```

### 2. 일관된 코드 포맷팅

ESLint 및 Prettier를 사용하여 코드 스타일을 강제합니다:

```json
// .eslintrc.json
{
  "extends": [
    "plugin:vue/vue3-recommended",
    "@vue/typescript/recommended",
    "prettier"
  ],
  "rules": {
    "vue/component-name-in-template-casing": ["error", "PascalCase"],
    "vue/component-definition-name-casing": ["error", "PascalCase"],
    "vue/custom-event-name-casing": ["error", "kebab-case"],
    "vue/v-on-event-hyphenation": ["error", "always"]
  }
}

// .prettierrc
{
  "semi": true,
  "singleQuote": true,
  "tabWidth": 2,
  "printWidth": 100,
  "trailingComma": "es5"
}
```

### 3. 일관된 주석 스타일

```typescript
/**
 * 상품 데이터를 가져오는 함수
 * 
 * @param id - 상품 ID
 * @returns 상품 데이터
 * @throws 상품을 찾을 수 없는 경우 에러 발생
 */
async function getProduct(id: number): Promise<Product> {
  // ...
}

// 간단한 라인 주석은 이렇게 작성
const count = 0; // 초기값 0으로 설정
```

## 성능 최적화 패턴

### 1. 메모이제이션

```typescript
// computed를 사용한 메모이제이션
const filteredProducts = computed(() => {
  return products.value.filter(p => p.category === selectedCategory.value);
});

// useCallback 패턴 (커스텀 훅)
function useCallback<T extends Function>(callback: T, deps: any[]): T {
  const callbackRef = ref(callback);
  watch(deps, () => {
    callbackRef.value = callback;
  }, { immediate: true });
  
  return computed(() => callbackRef.value).value as T;
}

// v-memo 디렉티브 사용
<div v-for="item in items" :key="item.id" v-memo="[item.id, item.title]">
  <!-- 컴포넌트 내용 -->
</div>
```

### 2. 컴포넌트 지연 로딩

```typescript
// 지연 로딩 컴포넌트
import { defineAsyncComponent } from 'vue';

const HeavyComponent = defineAsyncComponent(() => 
  import('./HeavyComponent.vue')
);

// 지연 로딩 + 로딩/에러 처리
const HeavyComponent = defineAsyncComponent({
  loader: () => import('./HeavyComponent.vue'),
  loadingComponent: LoadingSpinner,
  errorComponent: ErrorDisplay,
  delay: 200,
  timeout: 3000
});
```

### 3. 가상 스크롤링

많은 항목을 렌더링할 때는 가상 스크롤링을 사용합니다:

```vue
<template>
  <virtual-scroller
    :items="items"
    :item-height="50"
    v-slot="{ item, index }"
  >
    <div class="item">
      {{ item.name }}
    </div>
  </virtual-scroller>
</template>

<script setup>
import { VirtualScroller } from '@vueuse/components';
// ...
</script>
```

### 4. 데이터 캐싱

```typescript
// 엔티티 스토어에서 캐싱 구현
export const useProductStore = defineStore('product', {
  state: () => ({
    products: [] as Product[],
    productMap: new Map<number, Product>(),
    lastFetchTime: 0,
    // ...
  }),
  
  actions: {
    async getProductById(id: number): Promise<Product> {
      // 캐시에 있으면 캐시된 데이터 반환
      if (this.productMap.has(id)) {
        return this.productMap.get(id)!;
      }
      
      // 캐시에 없으면 API 요청
      const product = await productRepository.getProduct(id);
      
      // 캐시에 저장
      this.productMap.set(id, product);
      
      return product;
    },
    
    async fetchProducts(force = false): Promise<Product[]> {
      const now = Date.now();
      const cacheAge = now - this.lastFetchTime;
      
      // 캐시가 5분 이내이고 강제 새로고침이 아니면 캐시된 데이터 반환
      if (!force && this.products.length > 0 && cacheAge < 5 * 60 * 1000) {
        return this.products;
      }
      
      // 새로운 데이터 가져오기
      this.products = await productRepository.getProducts();
      this.lastFetchTime = now;
      
      // 맵 업데이트
      this.productMap.clear();
      this.products.forEach(product => {
        this.productMap.set(product.id, product);
      });
      
      return this.products;
    }
  }
});
```

## 타입스크립트 활용 모범 사례

### 1. 엄격한 타입 체크

```typescript
// tsconfig.json
{
  "compilerOptions": {
    "strict": true,
    "noImplicitAny": true,
    "strictNullChecks": true,
    "noImplicitReturns": true,
    "noImplicitThis": true,
    // ...
  }
}
```

### 2. 유틸리티 타입 활용

```typescript
// 기존 타입에서 필드 일부만 선택
type ProductSummary = Pick<Product, 'id' | 'name' | 'price'>;

// 기존 타입에서 일부 필드를 제외
type ProductWithoutStock = Omit<Product, 'stock'>;

// 모든 필드 옵셔널로 만들기
type PartialProduct = Partial<Product>;

// 모든 필드 필수로 만들기
type RequiredProduct = Required<Product>;

// 읽기 전용 타입
type ReadonlyProduct = Readonly<Product>;

// 유니온 타입에서 null/undefined 제거
type NonNullableProductId = NonNullable<ProductId | null | undefined>;
```

### 3. 판별 유니온(Discriminated Unions) 활용

```typescript
// 상태에 따라 다른 데이터 구조를 가지는 타입
type AsyncState<T> =
  | { status: 'idle' }
  | { status: 'loading' }
  | { status: 'error'; error: Error }
  | { status: 'success'; data: T };

// 사용 예시
function renderContent<T>(state: AsyncState<T>) {
  switch (state.status) {
    case 'idle':
      return <div>시작되지 않음</div>;
    case 'loading':
      return <div>로딩 중...</div>;
    case 'error':
      return <div>에러: {state.error.message}</div>;
    case 'success':
      return <div>데이터: {JSON.stringify(state.data)}</div>;
  }
}
```

### 4. 제네릭 활용

```typescript
// 제네릭 함수
function fetchData<T>(url: string): Promise<T> {
  return fetch(url).then(res => res.json());
}

const product = await fetchData<Product>('/api/products/1');

// 제네릭 컴포넌트
interface ListProps<T> {
  items: T[];
  renderItem: (item: T) => any;
}

const List = <T,>(props: ListProps<T>) => {
  return (
    <div>
      {props.items.map((item, index) => (
        <div key={index}>{props.renderItem(item)}</div>
      ))}
    </div>
  );
};
```

## 컴포넌트 개발 모범 사례

### 1. 관심사 분리

각 컴포넌트는 한 가지 책임만 가져야 합니다:

```vue
<!-- 나쁜 예: 너무 많은 책임 -->
<template>
  <div>
    <div class="search-bar"><!-- 검색 UI --></div>
    <div class="filter-panel"><!-- 필터 UI --></div>
    <div class="product-list"><!-- 상품 목록 UI --></div>
    <div class="pagination"><!-- 페이지네이션 UI --></div>
  </div>
</template>

<!-- 좋은 예: 컴포넌트로 분리 -->
<template>
  <div>
    <search-bar @search="handleSearch" />
    <filter-panel @filter="handleFilter" />
    <product-list :products="filteredProducts" />
    <pagination 
      :current-page="page" 
      :total-pages="totalPages"
      @page-change="handlePageChange" 
    />
  </div>
</template>
```

### 2. 인터페이스 명확성

Props와 Events를 명확하게 정의합니다:

```typescript
// Props 인터페이스 명확하게 정의
defineProps<{
  product: Product;               // 필수 속성
  showPrice?: boolean;            // 선택적 속성
  variant?: 'default' | 'compact'; // 열거형 속성
}>();

// Events 인터페이스 명확하게 정의
defineEmits<{
  (e: 'select', productId: number): void;
  (e: 'add-to-cart', product: Product, quantity: number): void;
}>();
```

### 3. 컴포저블(Composables) 활용

재사용 가능한 로직은 컴포저블로 분리합니다:

```typescript
// shared/hooks/useAsyncData.ts
export function useAsyncData<T>(
  fetcher: () => Promise<T>,
  initial: T | null = null
) {
  const data = ref<T | null>(initial);
  const loading = ref(false);
  const error = ref<Error | null>(null);
  
  const execute = async () => {
    loading.value = true;
    error.value = null;
    
    try {
      data.value = await fetcher();
    } catch (err) {
      error.value = err instanceof Error ? err : new Error(String(err));
    } finally {
      loading.value = false;
    }
  };
  
  return {
    data,
    loading,
    error,
    execute
  };
}

// 사용 예시
const { data: products, loading, error, execute: fetchProducts } = 
  useAsyncData(() => productRepository.getProducts());

onMounted(fetchProducts);
```

### 4. 컴포넌트 문서화

```typescript
/**
 * 상품 카드 컴포넌트
 * 
 * @example
 * <product-card 
 *   :product="product" 
 *   @add-to-cart="handleAddToCart" 
 * />
 */
defineComponent({
  name: 'ProductCard',
  props: {
    /**
     * 표시할 상품 데이터
     */
    product: {
      type: Object as PropType<Product>,
      required: true
    },
    
    /**
     * 컴팩트 모드 활성화 여부
     * @default false
     */
    compact: {
      type: Boolean,
      default: false
    }
  },
  // ...
});
```

## 상태 관리 모범 사례

### 1. 상태 정규화

상태를 정규화하여 중복을 방지하고 업데이트를 효율적으로 만듭니다:

```typescript
// 나쁜 예: 중첩된 데이터 구조
const state = {
  products: [
    {
      id: 1,
      name: '상품 1',
      categories: [
        { id: 1, name: '카테고리 1' },
        { id: 2, name: '카테고리 2' }
      ]
    }
  ]
};

// 좋은 예: 정규화된 데이터 구조
const state = {
  products: {
    byId: {
      1: { id: 1, name: '상품 1', categoryIds: [1, 2] }
    },
    allIds: [1]
  },
  categories: {
    byId: {
      1: { id: 1, name: '카테고리 1' },
      2: { id: 2, name: '카테고리 2' }
    },
    allIds: [1, 2]
  }
};
```

### 2. 불변성 유지

상태 변경 시 불변성을 유지합니다:

```typescript
// 나쁜 예: 상태 직접 변경
state.products.push(newProduct); // ❌ 원본 배열 변경

// 좋은 예: 새 객체 생성
state.products = [...state.products, newProduct]; // ✅ 새 배열 생성
```

### 3. 비동기 액션 패턴

```typescript
// 비동기 액션 패턴
async fetchProducts() {
  // 1. 로딩 상태 설정
  this.isLoading = true;
  this.error = null;
  
  try {
    // 2. 비동기 작업 수행
    const products = await productRepository.getProducts();
    
    // 3. 성공 시 상태 업데이트
    this.products = products;
  } catch (err: any) {
    // 4. 실패 시 에러 상태 업데이트
    this.error = err.message || '데이터를 불러오는 중 오류가 발생했습니다.';
    console.error('Error fetching products:', err);
  } finally {
    // 5. 로딩 상태 해제
    this.isLoading = false;
  }
}
```

### 4. 선택자(Selector) 패턴

```typescript
// 스토어에 계산된 값 정의
export const useProductStore = defineStore('product', {
  state: () => ({
    products: [] as Product[],
    selectedCategory: '' as string,
  }),
  
  getters: {
    // 필터링된 상품 목록
    filteredProducts(): Product[] {
      if (!this.selectedCategory) return this.products;
      return this.products.filter(p => p.category === this.selectedCategory);
    },
    
    // 카테고리별 상품 수
    productCountByCategory(): Record<string, number> {
      const result: Record<string, number> = {};
      for (const product of this.products) {
        result[product.category] = (result[product.category] || 0) + 1;
      }
      return result;
    },
    
    // 페이지네이션 정보
    pagination(): PaginationInfo {
      return {
        totalItems: this.filteredProducts.length,
        // ...
      };
    }
  }
});
```

## API 통합 모범 사례

### 1. 리포지토리 패턴

엔티티 API 통신은 리포지토리 패턴을 사용합니다:

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

### 2. API 요청/응답 타입 정의

```typescript
// entities/product/api/types.ts
export namespace ProductRequest {
  export interface Create {
    name: string;
    price: number;
    description: string;
    category: string;
    // ...
  }
  
  export interface Update {
    name?: string;
    price?: number;
    description?: string;
    category?: string;
    // ...
  }
}

export namespace ProductResponse {
  export interface Detail {
    id: number;
    name: string;
    price: number;
    description: string;
    category: string;
    // ...
  }
  
  export interface List {
    id: number;
    name: string;
    price: number;
    category: string;
    // ...
  }
}
```

### 3. API 데이터 매핑

API 응답을 도메인 모델로 변환하는 매핑 함수를 구현합니다:

```typescript
// entities/product/api/mappers.ts
export const mapToProduct = (response: ProductResponse.Detail): Product => ({
  id: response.id,
  name: response.name,
  originalPrice: response.originalPrice,
  price: response.price,
  description: response.description,
  thumbnailImageUrl: response.thumbnailImageUrl,
  category: response.category as ProductCategory,
  inStock: response.stock > 0,
  status: response.status as ProductStatus,
  stock: {
    quantity: response.stock
  }
});

export const mapToProducts = (responses: ProductResponse.List[]): Product[] => 
  responses.map(response => ({
    id: response.id,
    name: response.name,
    originalPrice: response.originalPrice,
    price: response.price,
    description: '',
    thumbnailImageUrl: response.thumbnailImageUrl,
    category: response.category as ProductCategory,
    inStock: response.stock > 0,
    status: response.status as ProductStatus,
    stock: {
      quantity: response.stock
    }
  }));
```

### 4. API 에러 처리

```typescript
// shared/api/error.ts
export class ApiError extends Error {
  public readonly status: number;
  public readonly data: any;
  
  constructor(message: string, status: number, data?: any) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.data = data;
  }
  
  static fromAxiosError(error: AxiosError): ApiError {
    const status = error.response?.status || 0;
    const data = error.response?.data;
    const message = data?.message || error.message || '알 수 없는 오류가 발생했습니다.';
    
    return new ApiError(message, status, data);
  }
}

export function handleApiError(error: unknown): ApiError {
  if (error instanceof ApiError) {
    return error;
  }
  
  if (error instanceof AxiosError) {
    return ApiError.fromAxiosError(error);
  }
  
  return new ApiError('알 수 없는 오류가 발생했습니다.', 0);
}
```

## 테스트 및 디버깅 전략

### 1. 단위 테스트

```typescript
// entities/product/model/productStore.test.ts
import { setActivePinia, createPinia } from 'pinia';
import { useProductStore } from './productStore';
import { productRepository } from '../api/repository';

// 리포지토리 모킹
vi.mock('../api/repository', () => ({
  productRepository: {
    getProducts: vi.fn().mockResolvedValue([
      { id: 1, name: '테스트 상품', price: 1000, /* ... */ },
      { id: 2, name: '테스트 상품 2', price: 2000, /* ... */ }
    ]),
    getProduct: vi.fn().mockImplementation((id: number) => {
      return Promise.resolve({ id, name: `테스트 상품 ${id}`, /* ... */ });
    })
  }
}));

describe('ProductStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });
  
  test('fetchProducts 성공 시 상품 목록 업데이트', async () => {
    const store = useProductStore();
    await store.fetchProducts();
    
    expect(productRepository.getProducts).toHaveBeenCalledTimes(1);
    expect(store.products.length).toBe(2);
    expect(store.isLoading).toBe(false);
    expect(store.error).toBeNull();
  });
  
  test('fetchProductById 성공 시 상품 정보 반환', async () => {
    const store = useProductStore();
    const product = await store.fetchProductById(1);
    
    expect(productRepository.getProduct).toHaveBeenCalledWith(1);
    expect(product.id).toBe(1);
    expect(product.name).toBe('테스트 상품 1');
  });
  
  // ... 기타 테스트
});
```

### 2. 컴포넌트 테스트

```typescript
// entities/product/ui/ProductCard.test.ts
import { mount } from '@vue/test-utils';
import ProductCard from './ProductCard.vue';

describe('ProductCard', () => {
  const product = {
    id: 1,
    name: '테스트 상품',
    price: 10000,
    originalPrice: 12000,
    description: '테스트 설명',
    thumbnailImageUrl: 'test.jpg',
    category: 'ELECTRONICS',
    inStock: true
  };
  
  test('상품 이름과 가격이 올바르게 표시됨', () => {
    const wrapper = mount(ProductCard, {
      props: { product }
    });
    
    expect(wrapper.text()).toContain('테스트 상품');
    expect(wrapper.text()).toContain('10,000');
  });
  
  test('장바구니 버튼 클릭 시 이벤트 발생', async () => {
    const wrapper = mount(ProductCard, {
      props: { product }
    });
    
    await wrapper.find('.add-to-cart-button').trigger('click');
    
    expect(wrapper.emitted('add-to-cart')).toBeTruthy();
    expect(wrapper.emitted('add-to-cart')[0]).toEqual([product.id]);
  });
  
  // ... 기타 테스트
});
```

### 3. 디버깅 도구 설정

Vue Devtools 및 Pinia Devtools 설정:

```typescript
// app/index.ts
import { createApp } from 'vue';
import { createPinia } from 'pinia';
import App from './App.vue';
import router from './router';

const app = createApp(App);
const pinia = createPinia();

// 개발 환경에서만 실행
if (import.meta.env.DEV) {
  pinia.use(({ store }) => {
    store.$onAction(({
      name,
      args,
      after,
      onError
    }) => {
      console.log(`Action ${name} started with args:`, args);
      
      after((result) => {
        console.log(`Action ${name} completed with result:`, result);
      });
      
      onError((error) => {
        console.error(`Action ${name} failed with error:`, error);
      });
    });
  });
}

app.use(pinia);
app.use(router);
app.mount('#app');
```

## 패키지 및 의존성 관리

### 1. 의존성 최소화

```json
// package.json
{
  "dependencies": {
    "vue": "^3.3.0",
    "vue-router": "^4.2.0",
    "pinia": "^2.1.0",
    "axios": "^1.4.0"
  },
  "devDependencies": {
    "typescript": "^5.0.0",
    "vite": "^4.3.0",
    "@vitejs/plugin-vue": "^4.2.0",
    "vitest": "^0.31.0",
    "@vue/test-utils": "^2.3.0",
    "eslint": "^8.41.0",
    "prettier": "^2.8.0"
  }
}
```

### 2. 패키지 버전 고정

중요한 패키지의 버전을 고정하여 예상치 못한 업데이트로 인한 문제를 방지합니다:

```json
// package.json
{
  "dependencies": {
    "vue": "3.3.4",  // 버전 고정
    "vue-router": "4.2.2",
    "pinia": "2.1.3",
    "axios": "1.4.0"
  }
}
```

### 3. 번들 크기 최적화

```typescript
// vite.config.ts
import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import { visualizer } from 'rollup-plugin-visualizer';

export default defineConfig({
  plugins: [
    vue(),
    visualizer({
      open: true,
      gzipSize: true,
      brotliSize: true
    })
  ],
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          'vue-vendor': ['vue', 'vue-router', 'pinia'],
          'ui-lib': ['element-plus'],
          'utils': ['lodash-es', 'date-fns']
        }
      }
    }
  }
});
```

## 리팩토링 가이드라인

### 1. 점진적 리팩토링

- 한 번에 큰 변경을 시도하지 말고 작은 단계로 나누어 리팩토링
- 각 단계마다 테스트를 실행하여 기능이 제대로 작동하는지 확인
- 단계별 커밋을 통해 변경 사항 추적 가능성 유지

### 2. 코드 중복 제거

코드 중복을 발견하면 공통 함수, 컴포넌트, 훅 등으로 추출:

```typescript
// 중복 코드 예시
function formatPrice1(price: number) {
  return new Intl.NumberFormat('ko-KR', {
    style: 'currency',
    currency: 'KRW'
  }).format(price);
}

function formatPrice2(price: number) {
  return new Intl.NumberFormat('ko-KR', {
    style: 'currency',
    currency: 'KRW'
  }).format(price);
}

// 리팩토링: 공통 함수로 추출
function formatPrice(price: number) {
  return new Intl.NumberFormat('ko-KR', {
    style: 'currency',
    currency: 'KRW'
  }).format(price);
}
```

### 3. 복잡한 메서드 분리

긴 메서드나 복잡한 컴포넌트는 더 작은 단위로 분리:

```typescript
// 리팩토링 전: 복잡한 컴포넌트
<script setup>
// 100줄 이상의 복잡한 로직...
</script>

// 리팩토링 후: 로직을 여러 컴포넌트와 훅으로 분리
<script setup>
import { useProductFilters } from './hooks/useProductFilters';
import { useProductPagination } from './hooks/useProductPagination';
import { useProductSort } from './hooks/useProductSort';

const { filters, updateFilter, resetFilters } = useProductFilters();
const { page, pageSize, totalPages, setPage } = useProductPagination();
const { sortField, sortOrder, setSorting } = useProductSort();

// 더 간결해진 로직...
</script>
```

이러한 모범 사례와 패턴을 따르면 코드의 품질, 유지보수성, 확장성을 향상시킬 수 있으며, 개발 생산성을 높일 수 있습니다.
