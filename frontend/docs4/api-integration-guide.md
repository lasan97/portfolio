# API 통합 가이드

## 목차

1. [API 통신 레이어 구조](#api-통신-레이어-구조)
2. [FSD에서의 API 통신 패턴](#fsd에서의-api-통신-패턴)
3. [API 클라이언트 설정](#api-클라이언트-설정)
4. [레이어별 API 구현 방법](#레이어별-api-구현-방법)
5. [데이터 매핑 패턴](#데이터-매핑-패턴)
6. [에러 처리](#에러-처리)
7. [비동기 상태 관리](#비동기-상태-관리)
8. [재사용 가능한 API 훅](#재사용-가능한-api-훅)
9. [모범 사례](#모범-사례)

## API 통신 레이어 구조

FSD 아키텍처에서 API 통신은 주로 다음 레이어에서 처리됩니다:

1. **shared/api**: 기본 API 클라이언트 및 공통 유틸리티
2. **entities/{entity}/api**: 엔티티 관련 API 통신
3. **features/{feature}/api**: 기능 관련 API 통신
4. **widgets/{widget}/api**: 위젯 관련 API 통신 (위젯 특화 데이터 요청)

각 레이어의 책임은 다음과 같습니다:

### shared/api

- 기본 HTTP 클라이언트 설정 (axios 등)
- 인증 토큰 관리
- 공통 에러 처리
- 요청/응답 인터셉터
- 재시도 로직

### entities/{entity}/api

- 엔티티 CRUD 작업
- 엔티티 데이터 조회
- API 응답을 도메인 모델로 변환 (매핑)

### features/{feature}/api

- 특정 기능과 관련된 API 요청
- 사용자 상호작용 관련 API 호출
- 여러 엔티티 API 조합

## FSD에서의 API 통신 패턴

FSD 아키텍처에서는 API 통신을 체계적으로 구성하기 위해 다음과 같은 패턴을 사용합니다:

### 1. 리포지토리 패턴

엔티티 레이어에서는 리포지토리 패턴을 사용하여 API 통신을 구현합니다. 리포지토리는 데이터 소스에 대한 추상화 계층을 제공하며, 데이터 액세스 로직을 캡슐화합니다.

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
  
  // ... 기타 CRUD 메서드
};
```

### 2. API 요청 모듈 패턴

기능 레이어에서는 API 요청 모듈 패턴을 사용하여 특정 기능과 관련된 API 요청을 구현합니다. 이 모듈은 종종 엔티티 리포지토리를 활용합니다.

```typescript
// features/productManagement/api/requests.ts
export const productManagementApi = {
  async createProduct(formData: ProductFormData): Promise<number> {
    const requestData = mapToCreateRequest(formData);
    return await productRepository.createProduct(requestData);
  },
  
  // ... 기타 메서드
};
```

### 3. 매퍼 패턴

API 응답 데이터를 도메인 모델로 변환하는 매퍼 패턴을 사용합니다. 이는 API 계층과 도메인 계층을 분리하는 데 도움이 됩니다.

```typescript
// entities/product/api/mappers.ts
export const mapToProduct = (response: ProductResponse.Detail): Product => ({
  id: response.id,
  name: response.name,
  // ... 기타 속성 매핑
});
```

## API 클라이언트 설정

### 기본 API 클라이언트 (shared/api)

```typescript
// shared/api/instance.ts
import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse, AxiosError } from 'axios';
import { getAuthToken } from '@shared/lib/auth';

// 기본 설정으로 Axios 인스턴스 생성
export const apiInstance: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
});

// 요청 인터셉터
apiInstance.interceptors.request.use(
  (config) => {
    const token = getAuthToken();
    
    // 토큰이 있으면 Authorization 헤더에 추가
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 응답 인터셉터
apiInstance.interceptors.response.use(
  (response) => {
    return response;
  },
  async (error) => {
    // 401 에러 처리 (인증 만료)
    if (error.response?.status === 401) {
      // 토큰 갱신 또는 로그아웃 처리
    }
    
    return Promise.reject(error);
  }
);
```

### API 요청 유틸리티 함수

```typescript
// shared/api/utils.ts
import { AxiosError } from 'axios';
import { apiInstance } from './instance';

// 재시도 로직이 포함된 API 요청 함수
export async function queryWithRetries<T>(
  url: string,
  options: {
    method?: 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH';
    data?: any;
    params?: Record<string, any>;
    maxRetries?: number;
    retryDelay?: number;
  } = {}
): Promise<T> {
  const {
    method = 'GET',
    data,
    params,
    maxRetries = 3,
    retryDelay = 1000
  } = options;
  
  let retries = 0;
  
  while (true) {
    try {
      const response = await apiInstance.request<T>({
        url,
        method,
        data,
        params
      });
      
      return response.data;
    } catch (error) {
      const axiosError = error as AxiosError;
      
      // 재시도 가능한 에러인지 확인 (네트워크 오류, 서버 오류)
      const isRetryable = !axiosError.response || axiosError.response.status >= 500;
      
      if (isRetryable && retries < maxRetries) {
        retries++;
        await new Promise(resolve => setTimeout(resolve, retryDelay * retries));
        continue;
      }
      
      throw error;
    }
  }
}
```

## 레이어별 API 구현 방법

### 1. entities/{entity}/api

엔티티 레이어에서는 리포지토리 패턴을 사용하여 API 통신을 구현합니다.

```typescript
// entities/product/api/types.ts
// API 요청/응답 타입 정의
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
  
  // 상품 생성
  async createProduct(data: ProductRequest.Create): Promise<number> {
    const response = await apiInstance.post<{ id: number }>('/api/products', data);
    return response.data.id;
  },
  
  // 상품 수정
  async updateProduct(id: number, data: ProductRequest.Update): Promise<number> {
    const response = await apiInstance.put<number>(`/api/products/${id}`, data);
    return response.data;
  },
  
  // 상품 삭제
  async deleteProduct(id: number): Promise<void> {
    await apiInstance.delete(`/api/products/${id}`);
  }
};
```

### 2. features/{feature}/api

기능 레이어에서는 특정 기능과 관련된 API 요청을 구현합니다.

```typescript
// features/productManagement/api/types.ts
// 기능 관련 API 요청/응답 타입
export interface ProductFormData {
  name: string;
  price: number;
  originalPrice: number;
  description: string;
  category: string;
  thumbnailImageUrl: string;
  stock: number;
}

export interface StockAdjustmentData {
  quantity: number;
  reason: string;
  memo?: string;
}

// features/productManagement/api/requests.ts
import { productRepository } from '@entities/product';
import { 
  mapToCreateRequest, 
  mapToUpdateRequest, 
  mapToStockAdjustmentRequest 
} from '../model/mappers';
import { ProductFormData, StockAdjustmentData } from '../model/types';

export const productManagementApi = {
  // 상품 생성
  async createProduct(formData: ProductFormData): Promise<number> {
    const requestData = mapToCreateRequest(formData);
    return await productRepository.createProduct(requestData);
  },
  
  // 상품 수정
  async updateProduct(id: number, formData: ProductFormData): Promise<number> {
    const requestData = mapToUpdateRequest(formData);
    return await productRepository.updateProduct(id, requestData);
  },
  
  // 상품 삭제
  async deleteProduct(id: number): Promise<void> {
    await productRepository.deleteProduct(id);
  },
  
  // 재고 조정
  async adjustStock(id: number, adjustmentData: StockAdjustmentData): Promise<number> {
    const requestData = mapToStockAdjustmentRequest(adjustmentData);
    return await productRepository.adjustStock(id, requestData);
  }
};
```

## 데이터 매핑 패턴

API 응답 데이터를 도메인 모델로 변환하는 매핑 함수를 구현합니다.

```typescript
// entities/product/api/mappers.ts
import { Product } from '../model/types';
import { ProductResponse } from './types';
import { ProductCategory, ProductStatus } from '../model/constants';

// API 응답 → 도메인 모델 매핑
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

// API 목록 응답 → 도메인 모델 배열 매핑
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

// features/productManagement/model/mappers.ts
import { ProductFormData, StockAdjustmentData } from '../model/types';
import { ProductRequest } from '@entities/product';

// 폼 데이터 → API 요청 매핑
export const mapToCreateRequest = (formData: ProductFormData): ProductRequest.Create => ({
  name: formData.name,
  originalPrice: formData.originalPrice,
  price: formData.price,
  description: formData.description,
  thumbnailImageUrl: formData.thumbnailImageUrl,
  category: formData.category,
  stock: formData.stock
});

export const mapToUpdateRequest = (formData: ProductFormData): ProductRequest.Update => ({
  name: formData.name,
  originalPrice: formData.originalPrice,
  price: formData.price,
  description: formData.description,
  thumbnailImageUrl: formData.thumbnailImageUrl,
  category: formData.category
});

export const mapToStockAdjustmentRequest = (data: StockAdjustmentData): ProductRequest.StockAdjustment => ({
  quantity: data.quantity,
  reason: data.reason,
  memo: data.memo
});
```

## 에러 처리

### 1. 공통 에러 처리

```typescript
// shared/api/error.ts
import { AxiosError } from 'axios';

// API 에러 클래스
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

// 에러 처리 함수
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

### 2. 컴포넌트에서의 에러 처리

```typescript
// features/productManagement/ui/ProductFormModal.vue
import { ref } from 'vue';
import { handleApiError } from '@shared/api/error';
import { productManagementApi } from '../api/requests';

export default {
  setup() {
    const error = ref<string | null>(null);
    const isSubmitting = ref(false);
    
    const handleSubmit = async (formData) => {
      isSubmitting.value = true;
      error.value = null;
      
      try {
        await productManagementApi.createProduct(formData);
        // 성공 처리
      } catch (err) {
        const apiError = handleApiError(err);
        error.value = apiError.message;
      } finally {
        isSubmitting.value = false;
      }
    };
    
    return {
      error,
      isSubmitting,
      handleSubmit
    };
  }
};
```

## 비동기 상태 관리

API 호출은 비동기 작업이므로 로딩 상태, 에러 상태, 데이터 상태를 관리해야 합니다.

### 1. Pinia 스토어에서의 비동기 상태 관리

```typescript
// entities/product/model/productStore.ts
export const useProductStore = defineStore('product', {
  state: () => ({
    products: [] as Product[],
    isLoading: false,
    error: null as string | null,
  }),
  
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
    
    // ... 기타 비동기 액션
  }
});
```

### 2. useAsync 훅을 사용한 비동기 상태 관리

```typescript
// shared/lib/hooks/useAsync.ts
import { ref, Ref } from 'vue';
import { handleApiError } from '@shared/api/error';

interface AsyncState<T> {
  data: Ref<T | null>;
  isLoading: Ref<boolean>;
  error: Ref<string | null>;
  execute: () => Promise<void>;
}

export function useAsync<T>(asyncFn: () => Promise<T>): AsyncState<T> {
  const data = ref<T | null>(null) as Ref<T | null>;
  const isLoading = ref(false);
  const error = ref<string | null>(null);
  
  const execute = async () => {
    isLoading.value = true;
    error.value = null;
    
    try {
      data.value = await asyncFn();
    } catch (err) {
      const apiError = handleApiError(err);
      error.value = apiError.message;
      console.error('API Error:', apiError);
    } finally {
      isLoading.value = false;
    }
  };
  
  return {
    data,
    isLoading,
    error,
    execute
  };
}
```

## 재사용 가능한 API 훅

컴포넌트에서 API 통신을 더 쉽게 사용할 수 있는 재사용 가능한 훅을 만들 수 있습니다.

### 1. useProducts 훅

```typescript
// entities/product/lib/hooks/useProducts.ts
import { ref, computed, ComputedRef } from 'vue';
import { productRepository } from '@entities/product/api';
import { Product } from '@entities/product/model/types';
import { handleApiError } from '@shared/api/error';

interface UseProductsReturn {
  products: ComputedRef<Product[]>;
  isLoading: ComputedRef<boolean>;
  error: ComputedRef<string | null>;
  fetchProducts: () => Promise<void>;
  getProductById: (id: number) => ComputedRef<Product | undefined>;
}

export function useProducts(): UseProductsReturn {
  const products = ref<Product[]>([]);
  const isLoading = ref(false);
  const error = ref<string | null>(null);
  
  const fetchProducts = async () => {
    isLoading.value = true;
    error.value = null;
    
    try {
      products.value = await productRepository.getProducts();
    } catch (err) {
      const apiError = handleApiError(err);
      error.value = apiError.message;
    } finally {
      isLoading.value = false;
    }
  };
  
  const getProductById = (id: number) => computed(() => 
    products.value.find(product => product.id === id)
  );
  
  return {
    products: computed(() => products.value),
    isLoading: computed(() => isLoading.value),
    error: computed(() => error.value),
    fetchProducts,
    getProductById
  };
}
```

### 2. 훅 사용 예시

```vue
<script setup>
import { onMounted } from 'vue';
import { useProducts } from '@entities/product';

const { products, isLoading, error, fetchProducts } = useProducts();

onMounted(() => {
  fetchProducts();
});
</script>

<template>
  <div>
    <div v-if="isLoading">로딩 중...</div>
    <div v-else-if="error">{{ error }}</div>
    <ul v-else>
      <li v-for="product in products" :key="product.id">
        {{ product.name }}
      </li>
    </ul>
  </div>
</template>
```

## 모범 사례

### 1. 레이어 책임 분리

- **shared/api**: 기본 HTTP 클라이언트, 인터셉터, 공통 유틸리티
- **entities/{entity}/api**: 엔티티 CRUD 작업, 데이터 매핑
- **features/{feature}/api**: 특정 기능 관련 API 요청, 비즈니스 로직

### 2. 일관된 에러 처리

- 모든 API 요청에서 일관된 에러 처리 패턴 사용
- 사용자 친화적인 에러 메시지 제공
- 개발자를 위한 상세 에러 로깅

### 3. 타입 안전성 유지

- API 요청/응답에 대한 명확한 타입 정의
- 도메인 모델과 API 모델 간의 명확한 매핑
- 타입 단언(type assertion) 최소화

### 4. 캐싱 전략

- 자주 사용되는 데이터 캐싱
- 캐시 무효화 전략 구현
- 낙관적 업데이트(Optimistic Updates) 구현

```typescript
// entities/product/model/productStore.ts
export const useProductStore = defineStore('product', {
  state: () => ({
    products: [] as Product[],
    productMap: new Map<number, Product>(),
    // ...
  }),
  
  actions: {
    // 캐싱을 활용한 상품 조회
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
    
    // 캐시 무효화
    invalidateProduct(id: number) {
      this.productMap.delete(id);
    }
  }
});
```

### 5. 요청 취소 처리

Axios의 CancelToken을 사용하여 요청 취소를 처리할 수 있습니다.

```typescript
// shared/api/utils.ts
import axios, { CancelTokenSource } from 'axios';
import { apiInstance } from './instance';

// 요청 취소 가능한 API 요청 함수
export function createCancellableRequest<T>(url: string, options: any = {}) {
  const cancelTokenSource = axios.CancelToken.source();
  
  const request = async (): Promise<T> => {
    const response = await apiInstance.request<T>({
      url,
      ...options,
      cancelToken: cancelTokenSource.token
    });
    
    return response.data;
  };
  
  const cancel = (message?: string) => {
    cancelTokenSource.cancel(message);
  };
  
  return { request, cancel };
}

// 사용 예시
const { request, cancel } = createCancellableRequest('/api/products');
const abortButton = document.getElementById('abort');
abortButton.addEventListener('click', () => cancel('User aborted request'));
const data = await request();
```

### 6. 모킹 및 테스트

API 통신을 모킹하고 테스트하는 방법:

```typescript
// tests/mocks/productRepository.ts
import { Product } from '@entities/product';
import { vi } from 'vitest';

export const mockProductRepository = {
  getProducts: vi.fn().mockResolvedValue([
    { id: 1, name: '테스트 상품1', /* ... */ },
    { id: 2, name: '테스트 상품2', /* ... */ }
  ] as Product[]),
  
  getProduct: vi.fn().mockImplementation((id: number) => {
    return Promise.resolve({
      id,
      name: `테스트 상품${id}`,
      // ... 기타 속성
    } as Product);
  }),
  
  // ... 기타 메서드 모킹
};

// tests/productStore.test.ts
import { setActivePinia, createPinia } from 'pinia';
import { useProductStore } from '@entities/product';
import { mockProductRepository } from '../mocks/productRepository';
import { vi, describe, it, expect, beforeEach } from 'vitest';

// productRepository 모듈 모킹
vi.mock('@entities/product/api/repository', () => ({
  productRepository: mockProductRepository
}));

describe('ProductStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });
  
  it('fetches products successfully', async () => {
    const store = useProductStore();
    await store.fetchProducts();
    
    expect(mockProductRepository.getProducts).toHaveBeenCalledTimes(1);
    expect(store.products.length).toBe(2);
    expect(store.isLoading).toBe(false);
    expect(store.error).toBeNull();
  });
  
  // ... 기타 테스트
});
```

이러한 모범 사례를 따르면 API 통신 코드를 더 효과적으로 구성하고 유지보수할 수 있습니다.
