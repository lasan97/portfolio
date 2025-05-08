# API 통신 및 상태 관리 가이드

이 문서는 Portfolio 프로젝트에서 API 통신과 상태 관리를 구현하는 방법에 대한 표준과 패턴을 설명합니다. 실제 프로젝트의 구현을 기반으로 작성되었습니다.

## 목차

1. [API 통신 구조](#api-통신-구조)
2. [API 리포지토리 패턴](#api-리포지토리-패턴)
3. [데이터 매핑](#데이터-매핑)
4. [API 타입 정의](#api-타입-정의)
5. [Pinia 상태 관리](#pinia-상태-관리)
6. [오류 처리 전략](#오류-처리-전략)
7. [미들웨어 및 인터셉터](#미들웨어-및-인터셉터)
8. [레이어 간 통신](#레이어-간-통신)

## API 통신 구조

API 통신은 다음과 같은 구조로 구성됩니다:

```
entities/product/
├── api/
│   ├── mappers.ts      # API 응답 매핑 함수
│   ├── repository.ts   # API 요청 함수
│   ├── types.ts        # API 요청/응답 타입
│   └── index.ts        # 공개 API
└── model/
    ├── productStore.ts # 상태 관리 스토어
    └── types.ts        # 도메인 타입 정의
```

## API 리포지토리 패턴

백엔드 API와의 통신은 리포지토리 패턴을 사용하여 구현합니다. 각 도메인 엔티티는 자체 리포지토리를 가지며, 이 리포지토리는 해당 엔티티와 관련된 모든 API 요청을 담당합니다.

### 리포지토리 구현 예시

```typescript
// entities/product/api/repository.ts
import { apiInstance } from '@shared/api';
import { ProductRequest, ProductResponse } from './types';
import { Product } from '../model/types';
import { mapToProduct, mapToProducts } from './mappers';
import { StockChangeReason } from '../model/constants';

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

  // 관리자용 상품 추가
  async createProduct(productData: ProductRequest.Create): Promise<number> {
    const response = await apiInstance.post<number>('/api/products', productData);
    return response.data;
  },

  // 관리자용 상품 수정
  async updateProduct(id: number, productData: ProductRequest.Update): Promise<number> {
    const response = await apiInstance.put<number>(`/api/products/${id}`, productData);
    return response.data;
  },

  // 관리자용 상품 삭제
  async deleteProduct(id: number): Promise<void> {
    await apiInstance.delete(`/api/products/${id}`);
  },

  // 관리자용 재고 조정
  async adjustStock(id: number, adjustData: ProductRequest.StockAdjustment): Promise<number> {
    const response = await apiInstance.patch<number>(`/api/products/${id}/stocks`, adjustData);
    return response.data;
  }
};
```

### 리포지토리 모듈 내보내기

```typescript
// entities/product/api/index.ts
export { productRepository } from './repository';
export { mapToProduct, mapToProducts } from './mappers';
export type { ProductRequest, ProductResponse } from './types';
```

## 데이터 매핑

API 응답 데이터는 도메인 모델에 맞게 변환해야 합니다. 이를 위해 매퍼 함수를 사용합니다.

### 매퍼 구현 예시

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

export const mapToProducts = (responses: ProductResponse.List[]): Product[] => 
  responses.map(response => ({
    id: response.id,
    name: response.name,
    originalPrice: response.originalPrice,
    price: response.price,
    description: response.description || '',
    thumbnailImageUrl: response.thumbnailImageUrl,
    category: response.category as ProductCategory,
    inStock: !!response.inStock,
    status: response.status as ProductStatus
  }));
```

## API 타입 정의

API 요청 및 응답 타입은 명확하게 정의해야 합니다.

### API 타입 정의 예시

```typescript
// entities/product/api/types.ts
import { ProductCategory, ProductStatus, StockChangeReason } from '../model/constants';

// API 요청 타입
export namespace ProductRequest {
  // 상품 생성 요청
  export interface Create {
    name: string;
    originalPrice: number;
    price: number;
    description: string;
    thumbnailImageUrl?: string;
    category: ProductCategory;
    initialStock?: number;
  }

  // 상품 수정 요청
  export interface Update {
    name?: string;
    originalPrice?: number;
    price?: number;
    description?: string;
    thumbnailImageUrl?: string;
    category?: ProductCategory;
    status?: ProductStatus;
  }

  // 재고 조정 요청
  export interface StockAdjustment {
    quantity: number;
    reason: StockChangeReason;
    note?: string;
  }
}

// API 응답 타입
export namespace ProductResponse {
  // 상품 목록 응답
  export interface List {
    id: number;
    name: string;
    originalPrice: number;
    price: number;
    description?: string;
    thumbnailImageUrl?: string;
    category: string;
    inStock: boolean;
    status: string;
  }

  // 상품 상세 응답
  export interface Detail {
    id: number;
    name: string;
    originalPrice: number;
    price: number;
    description: string;
    thumbnailImageUrl?: string;
    category: string;
    status: string;
    stock?: {
      id: number;
      quantity: number;
      version: number;
    };
  }
}
```

## Pinia 상태 관리

상태 관리는 Pinia를 사용하여 구현합니다. 각 도메인 엔티티는 자체 스토어를 가집니다.

### 스토어 구현 예시

```typescript
// entities/product/model/productStore.ts
import { defineStore } from 'pinia';
import { Product } from './types';
import { productRepository, mapToProduct, mapToProducts } from '../api';
import { ProductFormData, StockAdjustmentData, productManagementApi } from '@features/productManagement';

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
    getProductsByCategory: (state) => (category: string) => 
      state.products.filter(product => product.category === category),
    getInStockProducts: (state) => 
      state.products.filter(product => product.inStock),
  },
  
  // 액션 정의
  actions: {
    // 상품 목록 가져오기
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
    
    // 단일 상품 상세 정보 가져오기
    async fetchProductById(id: number) {
      // 이미 상품 목록에 있는 경우 API 호출 스킵
      const existingProduct = this.getProductById(id);
      if (existingProduct && existingProduct.description) {
        return existingProduct;
      }
      
      this.isLoading = true;
      this.error = null;
      
      try {
        const product = await productRepository.getProduct(id);
        
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
    
    // 새 상품 생성 - features 레이어의 API 사용
    async createProduct(productData: ProductFormData) {
      this.isLoading = true;
      this.error = null;
      
      try {
        const createdProductId = await productManagementApi.createProduct(productData);
        
        // 성공적으로 생성된 후 상품 목록을 다시 불러옴
        await this.fetchProducts();
        
        return createdProductId;
      } catch (err: any) {
        this.error = err.message || '상품 등록 중 오류가 발생했습니다.';
        console.error('Error creating product:', err);
        throw err;
      } finally {
        this.isLoading = false;
      }
    },
    
    // 다른 액션들...
  }
});
```

### 스토어 사용 예시

```vue
<script setup lang="ts">
import { useProductStore } from '@entities/product';
import { storeToRefs } from 'pinia';
import { onMounted, computed } from 'vue';

// 스토어 인스턴스화
const productStore = useProductStore();

// 반응형으로 상태 참조하기 위해 storeToRefs 사용
const { products, isLoading, error } = storeToRefs(productStore);

// 컴포넌트 마운트시 데이터 로드
onMounted(async () => {
  await productStore.fetchProducts();
});

// 특정 카테고리의 상품만 필터링
const electronicsProducts = computed(() => 
  productStore.getProductsByCategory('ELECTRONICS')
);

// 단일 상품 로드
const loadProduct = async (id: number) => {
  try {
    const product = await productStore.fetchProductById(id);
    // 로직...
  } catch (error) {
    // 오류 처리...
  }
};
</script>
```

## 오류 처리 전략

API 통신 시 발생할 수 있는 오류를 체계적으로 관리하는 전략이 필요합니다.

### 중앙 집중식 오류 처리

```typescript
// shared/api/instance.ts (실제 구현 코드)
import axios from 'axios'
import {getAuthToken} from '../lib'

// Axios 인스턴스 생성
export const apiInstance = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  },
  withCredentials: true // 쿠키 전송을 위해 필요 (CORS 요청 시 중요)
})

// 요청 인터셉터
apiInstance.interceptors.request.use(
  (config) => {
    // 요청 전 처리 (토큰 추가)
    const token = getAuthToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
)

// 응답 인터셉터
apiInstance.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    // 401 Unauthorized 에러 처리
    if (error.response && error.response.status === 401) {
      // 인증 실패 처리 (예: 로그아웃)
      if (typeof window !== 'undefined') {
        // 현재 URL을 저장하여 로그인 후 리다이렉트
        const currentPath = window.location.pathname;
        sessionStorage.setItem('redirectAfterLogin', currentPath);
        
        import('@shared/lib').then(({ logout }) => {
          logout();
          window.location.href = '/login';
        });
      }
    }
    
    // 오류 메시지 추출 및 로깅
    const errorMessage = 
      error.response?.data?.message || 
      error.message || 
      '서버 통신 중 오류가 발생했습니다.';
    
    console.error('API 오류:', errorMessage);
    
    return Promise.reject(error);
  }
)
```

### 스토어에서의 오류 처리

```typescript
// 스토어 액션에서의 오류 처리
async fetchProducts() {
  this.isLoading = true;
  this.error = null;
  
  try {
    this.products = await productRepository.getProducts();
  } catch (err: any) {
    // 구체적인 오류 메시지 설정
    this.error = err.message || '상품을 불러오는 중 오류가 발생했습니다.';
    console.error('Error fetching products:', err);
  } finally {
    this.isLoading = false;
  }
}
```

## 미들웨어 및 인터셉터

API 요청 및 응답을 일관되게 처리하기 위해 인터셉터를 사용합니다.

### 요청 인터셉터

```typescript
// 요청 인터셉터
apiInstance.interceptors.request.use(
  (config) => {
    // 인증 토큰 추가
    const token = getAuthToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
)
```

### 응답 인터셉터

```typescript
// 응답 인터셉터
apiInstance.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    // 401 Unauthorized 에러 처리
    if (error.response && error.response.status === 401) {
      // 인증 실패 처리 (예: 로그아웃)
      if (typeof window !== 'undefined') {
        // 현재 URL을 저장하여 로그인 후 리다이렉트
        const currentPath = window.location.pathname;
        sessionStorage.setItem('redirectAfterLogin', currentPath);
        
        import('@shared/lib').then(({ logout }) => {
          logout();
          window.location.href = '/login';
        });
      }
    }
    
    // 오류 메시지 추출 및 로깅
    const errorMessage = 
      error.response?.data?.message || 
      error.message || 
      '서버 통신 중 오류가 발생했습니다.';
    
    console.error('API 오류:', errorMessage);
    
    return Promise.reject(error);
  }
)
```

## 레이어 간 통신

FSD 아키텍처에서 레이어 간의 통신 패턴을 설명합니다. 실제 코드에서는 다음과 같은 패턴을 사용하고 있습니다.

### 엔티티에서 기능 레이어 참조

엔티티의 스토어에서 기능 레이어의 API를 사용하는 케이스가 있습니다. 이는 FSD의 단방향 의존성 원칙에 부합하지 않는 패턴입니다. 이 패턴을 사용할 때는 주의가 필요합니다.

```typescript
// entities/product/model/productStore.ts
import { ProductFormData, StockAdjustmentData, productManagementApi } from '@features/productManagement';

// 새 상품 생성 - features 레이어의 API 사용
async createProduct(productData: ProductFormData) {
  this.isLoading = true;
  this.error = null;
  
  try {
    const createdProductId = await productManagementApi.createProduct(productData);
    
    // 성공적으로 생성된 후 상품 목록을 다시 불러옴
    await this.fetchProducts();
    
    return createdProductId;
  } catch (err: any) {
    this.error = err.message || '상품 등록 중 오류가 발생했습니다.';
    console.error('Error creating product:', err);
    throw err;
  } finally {
    this.isLoading = false;
  }
}
```

#### 개선된 패턴 제안

위 패턴을 개선하려면, 다음과 같이 수정할 수 있습니다:

1. **엔티티 레이어의 API에 모든 CRUD 작업 구현하기**
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

이 방식은 FSD의 단방향 의존성 원칙을 유지하면서 기능 레이어에서 비즈니스 로직을 추가할 수 있습니다.
