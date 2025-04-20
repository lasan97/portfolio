# 피처(Feature) 개발 가이드

이 문서는 Portfolio 프로젝트에서 피처(Feature)를 개발하는 방법에 대한 상세 가이드를 제공합니다. 피처는 FSD 아키텍처에서 사용자 인터랙션과 비즈니스 기능을 구현하는 중요한 레이어입니다.

## 목차

1. [피처란?](#피처란)
2. [피처 구조](#피처-구조)
3. [피처 개발 프로세스](#피처-개발-프로세스)
4. [모델 설계](#모델-설계)
5. [API 통신 구현](#api-통신-구현)
6. [상태 관리 구현](#상태-관리-구현)
7. [UI 컴포넌트 개발](#ui-컴포넌트-개발)
8. [공개 API 설계](#공개-api-설계)
9. [엔티티와의 상호작용](#엔티티와의-상호작용)
10. [모범 사례 및 패턴](#모범-사례-및-패턴)

## 피처란?

피처(Feature)는 사용자 인터랙션과 관련된 비즈니스 기능을 구현하는 레이어입니다. 엔티티가 도메인 모델을 정의한다면, 피처는 그 모델을 사용하여 실제 사용자에게 가치를 제공하는 기능을 구현합니다.

### 피처의 특징

- **사용자 중심**: 사용자가 직접 상호작용하는 기능을 구현합니다.
- **비즈니스 가치**: 특정 비즈니스 요구사항을 충족시킵니다.
- **엔티티 활용**: 하나 이상의 엔티티를 활용하여 기능을 구현합니다.
- **독립적**: 다른 피처에 의존하지 않고 독립적으로 작동합니다.

### 현재 프로젝트의 피처 예시

- **auth**: 사용자 인증 및 권한 관리
- **productManagement**: 상품 관리 기능
- **credit**: 크레딧 관리 기능
- **introduction**: 자기소개 관리 기능
- **file**: 파일 업로드 및 관리 기능

## 피처 구조

피처는 다음과 같은 구조로 조직됩니다:

```
features/{피처명}/
├── model/              # 기능별 모델 및 상태
│   ├── types.ts        # 타입 정의
│   ├── constants.ts    # 상수 및 열거형 (선택적)
│   └── store.ts        # 상태 관리
├── api/                # API 관련 코드 (선택적)
│   ├── types.ts        # API 요청/응답 타입
│   ├── index.ts        # API 요청 함수
│   └── mappers.ts      # 데이터 변환 함수 (선택적)
├── ui/                 # UI 컴포넌트
│   ├── {Component}.vue # 기능 관련 UI 컴포넌트
│   └── index.ts        # UI 컴포넌트 내보내기
├── lib/                # 유틸리티 함수 (선택적)
│   └── helpers.ts      # 헬퍼 함수
└── index.ts            # 공개 API
```

## 피처 개발 프로세스

피처 개발은 다음 단계로 진행됩니다:

1. **요구사항 분석**: 구현할 기능의 요구사항을 명확히 이해합니다.
2. **의존성 식별**: 필요한 엔티티와 공유 유틸리티를 식별합니다.
3. **모델 설계**: 기능별 타입과 상태를 정의합니다.
4. **API 통신 구현**: 필요한 API 요청 함수를 구현합니다.
5. **상태 관리 구현**: Pinia 스토어를 사용하여 상태 관리를 구현합니다.
6. **UI 컴포넌트 개발**: 사용자 인터페이스 컴포넌트를 개발합니다.
7. **공개 API 설계**: `index.ts` 파일을 통해 공개 API를 정의합니다.
8. **테스트 및 검증**: 기능이 요구사항을 충족하는지 확인합니다.

## 모델 설계

피처의 모델은 해당 기능에 특화된 타입과 상태를 정의합니다. 이는 엔티티 모델을 기반으로 하되, 사용자 인터랙션에 필요한 추가 정보를 포함합니다.

### 타입 정의 (`types.ts`)

```typescript
// features/productManagement/model/types.ts
import { ProductCategory } from '@entities/product';

// 상품 폼 데이터 타입
export interface ProductFormData {
  name: string;
  originalPrice: number;
  price: number;
  description: string;
  thumbnailImageUrl: string;
  category: ProductCategory;
  stock: number;
}

// 재고 조정 데이터 타입
export interface StockAdjustmentData {
  productId: number;
  quantity: number;
  reason: string;
  memo?: string;
}

// 상품 필터 타입
export interface ProductFilter {
  category?: ProductCategory;
  minPrice?: number;
  maxPrice?: number;
  inStock?: boolean;
  searchTerm?: string;
}
```

### 상수 정의 (`constants.ts`, 선택적)

```typescript
// features/productManagement/model/constants.ts
export enum StockAdjustmentReason {
  PURCHASE = 'PURCHASE',
  RETURN = 'RETURN',
  LOSS = 'LOSS',
  ADJUSTMENT = 'ADJUSTMENT'
}

export namespace StockAdjustmentReason {
  export const descriptions: Record<StockAdjustmentReason, string> = {
    [StockAdjustmentReason.PURCHASE]: '구매',
    [StockAdjustmentReason.RETURN]: '반품',
    [StockAdjustmentReason.LOSS]: '손실',
    [StockAdjustmentReason.ADJUSTMENT]: '재고조정'
  };
}
```

## API 통신 구현

피처의 API 통신은 해당 기능에 필요한 서버 요청을 처리합니다.

### API 타입 (`api/types.ts`)

```typescript
// features/productManagement/api/types.ts
import { ProductCategory } from '@entities/product';

// API 요청 타입
export namespace ProductManagementRequest {
  export interface CreateProduct {
    name: string;
    originalPrice: number;
    price: number;
    description: string;
    thumbnailImageUrl: string;
    category: ProductCategory;
    stock: number;
  }

  export interface UpdateProduct {
    id: number;
    name: string;
    originalPrice: number;
    price: number;
    description: string;
    thumbnailImageUrl: string;
    category: ProductCategory;
  }

  export interface AdjustStock {
    quantity: number;
    reason: string;
    memo?: string;
  }
}

// API 응답 타입
export namespace ProductManagementResponse {
  export interface ProductResult {
    id: number;
    success: boolean;
    message?: string;
  }
}
```

### API 요청 함수 (`api/index.ts`)

```typescript
// features/productManagement/api/index.ts
import { apiInstance } from '@shared/api';
import { ProductManagementRequest, ProductManagementResponse } from './types';
import { StockAdjustmentData, ProductFormData } from '../model/types';

export const productManagementApi = {
  // 상품 생성
  async createProduct(data: ProductFormData): Promise<number> {
    const request: ProductManagementRequest.CreateProduct = {
      name: data.name,
      originalPrice: data.originalPrice,
      price: data.price,
      description: data.description,
      thumbnailImageUrl: data.thumbnailImageUrl,
      category: data.category,
      stock: data.stock
    };

    const response = await apiInstance.post<ProductManagementResponse.ProductResult>(
      '/api/admin/products',
      request
    );
    
    return response.data.id;
  },

  // 상품 수정
  async updateProduct(id: number, data: ProductFormData): Promise<number> {
    const request: ProductManagementRequest.UpdateProduct = {
      id,
      name: data.name,
      originalPrice: data.originalPrice,
      price: data.price,
      description: data.description,
      thumbnailImageUrl: data.thumbnailImageUrl,
      category: data.category
    };

    const response = await apiInstance.put<ProductManagementResponse.ProductResult>(
      `/api/admin/products/${id}`,
      request
    );
    
    return response.data.id;
  },

  // 상품 삭제
  async deleteProduct(id: number): Promise<boolean> {
    const response = await apiInstance.delete<ProductManagementResponse.ProductResult>(
      `/api/admin/products/${id}`
    );
    
    return response.data.success;
  },

  // 재고 조정
  async adjustStock(productId: number, data: StockAdjustmentData): Promise<number> {
    const request: ProductManagementRequest.AdjustStock = {
      quantity: data.quantity,
      reason: data.reason,
      memo: data.memo
    };

    const response = await apiInstance.patch<ProductManagementResponse.ProductResult>(
      `/api/admin/products/${productId}/stock`,
      request
    );
    
    return response.data.id;
  }
};
```

## 상태 관리 구현

피처의 상태 관리는 해당 기능의 UI 상태와 비즈니스 로직을 관리합니다.

```typescript
// features/productManagement/model/store.ts
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { ProductFormData, StockAdjustmentData, ProductFilter } from './types';
import { productManagementApi } from '../api';
import { useProductStore, Product } from '@entities/product';

export const useProductManagementStore = defineStore('productManagement', () => {
  // 상태
  const isLoading = ref(false);
  const error = ref<string | null>(null);
  const currentFilter = ref<ProductFilter>({});
  
  // 다른 스토어 참조
  const productStore = useProductStore();
  
  // 게터
  const filteredProducts = computed(() => {
    return productStore.products.filter(product => {
      // 카테고리 필터
      if (currentFilter.value.category && 
          product.category !== currentFilter.value.category) {
        return false;
      }
      
      // 가격 필터
      if (currentFilter.value.minPrice !== undefined && 
          product.price < currentFilter.value.minPrice) {
        return false;
      }
      
      if (currentFilter.value.maxPrice !== undefined && 
          product.price > currentFilter.value.maxPrice) {
        return false;
      }
      
      // 재고 필터
      if (currentFilter.value.inStock !== undefined && 
          product.inStock !== currentFilter.value.inStock) {
        return false;
      }
      
      // 검색어 필터
      if (currentFilter.value.searchTerm && 
          !product.name.toLowerCase().includes(
            currentFilter.value.searchTerm.toLowerCase()
          )) {
        return false;
      }
      
      return true;
    });
  });
  
  // 액션
  async function createProduct(productData: ProductFormData) {
    isLoading.value = true;
    error.value = null;
    
    try {
      const productId = await productManagementApi.createProduct(productData);
      // 상품 목록 새로고침
      await productStore.fetchProducts();
      return productId;
    } catch (err: any) {
      error.value = err.message || '상품 생성 중 오류가 발생했습니다.';
      throw error.value;
    } finally {
      isLoading.value = false;
    }
  }
  
  async function updateProduct(id: number, productData: ProductFormData) {
    isLoading.value = true;
    error.value = null;
    
    try {
      const productId = await productManagementApi.updateProduct(id, productData);
      // 상품 목록 새로고침
      await productStore.fetchProducts();
      return productId;
    } catch (err: any) {
      error.value = err.message || '상품 수정 중 오류가 발생했습니다.';
      throw error.value;
    } finally {
      isLoading.value = false;
    }
  }
  
  async function deleteProduct(id: number) {
    isLoading.value = true;
    error.value = null;
    
    try {
      const success = await productManagementApi.deleteProduct(id);
      if (success) {
        // 상품 목록 새로고침
        await productStore.fetchProducts();
      }
      return success;
    } catch (err: any) {
      error.value = err.message || '상품 삭제 중 오류가 발생했습니다.';
      throw error.value;
    } finally {
      isLoading.value = false;
    }
  }
  
  async function adjustStock(productId: number, data: StockAdjustmentData) {
    isLoading.value = true;
    error.value = null;
    
    try {
      const result = await productManagementApi.adjustStock(productId, data);
      // 상품 목록 새로고침
      await productStore.fetchProducts();
      return result;
    } catch (err: any) {
      error.value = err.message || '재고 조정 중 오류가 발생했습니다.';
      throw error.value;
    } finally {
      isLoading.value = false;
    }
  }
  
  function setFilter(filter: Partial<ProductFilter>) {
    currentFilter.value = { ...currentFilter.value, ...filter };
  }
  
  function clearFilter() {
    currentFilter.value = {};
  }
  
  return {
    // 상태
    isLoading,
    error,
    currentFilter,
    
    // 게터
    filteredProducts,
    
    // 액션
    createProduct,
    updateProduct,
    deleteProduct,
    adjustStock,
    setFilter,
    clearFilter
  };
});
```

## UI 컴포넌트 개발

피처의 UI 컴포넌트는 사용자 인터페이스를 구현합니다. 이는 엔티티 컴포넌트를 활용하여 더 복잡한 UI를 구성합니다.

### 상품 폼 컴포넌트 예시

```vue
<!-- features/productManagement/ui/ProductFormModal.vue -->
<template>
  <Modal :title="isCreate ? '상품 등록' : '상품 수정'" @close="handleClose">
    <form @submit.prevent="handleSubmit" class="space-y-4">
      <div class="form-group">
        <label for="name" class="form-label">상품명</label>
        <input
          id="name"
          v-model="form.name"
          type="text"
          class="form-input"
          required
        />
      </div>
      
      <div class="form-group">
        <label for="originalPrice" class="form-label">원가</label>
        <input
          id="originalPrice"
          v-model.number="form.originalPrice"
          type="number"
          class="form-input"
          min="0"
          required
        />
      </div>
      
      <div class="form-group">
        <label for="price" class="form-label">판매가</label>
        <input
          id="price"
          v-model.number="form.price"
          type="number"
          class="form-input"
          min="0"
          required
        />
      </div>
      
      <div class="form-group">
        <label for="category" class="form-label">카테고리</label>
        <select
          id="category"
          v-model="form.category"
          class="form-select"
          required
        >
          <option v-for="category in categories" :key="category.code" :value="category.code">
            {{ category.description }}
          </option>
        </select>
      </div>
      
      <div class="form-group">
        <label for="description" class="form-label">상품 설명</label>
        <textarea
          id="description"
          v-model="form.description"
          class="form-textarea"
          rows="4"
          required
        ></textarea>
      </div>
      
      <div class="form-group">
        <label for="thumbnailImageUrl" class="form-label">이미지 URL</label>
        <input
          id="thumbnailImageUrl"
          v-model="form.thumbnailImageUrl"
          type="text"
          class="form-input"
          required
        />
      </div>
      
      <div v-if="isCreate" class="form-group">
        <label for="stock" class="form-label">초기 재고</label>
        <input
          id="stock"
          v-model.number="form.stock"
          type="number"
          class="form-input"
          min="0"
          required
        />
      </div>
      
      <div class="flex justify-end space-x-2">
        <Button type="button" variant="secondary" @click="handleClose">취소</Button>
        <Button type="submit" variant="primary" :loading="isLoading">
          {{ isCreate ? '등록' : '수정' }}
        </Button>
      </div>
    </form>
  </Modal>
</template>

<script lang="ts">
import { defineComponent, ref, computed, PropType, onMounted } from 'vue';
import { ProductCategory } from '@entities/product';
import { Button, Modal } from '@shared/ui';
import { useProductManagementStore } from '../model/store';
import { useProductStore } from '@entities/product';
import { ProductFormData } from '../model/types';

export default defineComponent({
  name: 'ProductFormModal',
  components: {
    Button,
    Modal
  },
  props: {
    productId: {
      type: Number as PropType<number | null>,
      default: null
    }
  },
  emits: ['close', 'submit'],
  setup(props, { emit }) {
    const productStore = useProductStore();
    const productManagementStore = useProductManagementStore();
    
    // 폼 상태
    const form = ref<ProductFormData>({
      name: '',
      originalPrice: 0,
      price: 0,
      description: '',
      thumbnailImageUrl: '',
      category: ProductCategory.ELECTRONICS,
      stock: 0
    });
    
    // 카테고리 목록
    const categories = computed(() => ProductCategory.entries());
    
    // 생성 모드인지 수정 모드인지 확인
    const isCreate = computed(() => !props.productId);
    
    // 로딩 상태
    const isLoading = computed(() => productManagementStore.isLoading);
    
    // 초기화
    onMounted(async () => {
      if (props.productId) {
        // 상품 정보 가져오기
        const product = productStore.getProductById(props.productId);
        
        if (product) {
          // 폼 초기화
          form.value = {
            name: product.name,
            originalPrice: product.originalPrice,
            price: product.price,
            description: product.description,
            thumbnailImageUrl: product.thumbnailImageUrl || '',
            category: product.category,
            stock: product.stock?.quantity || 0
          };
        } else {
          // 상품 정보가 없으면 상세 정보 가져오기
          try {
            const fetchedProduct = await productStore.fetchProductById(props.productId);
            
            if (fetchedProduct) {
              form.value = {
                name: fetchedProduct.name,
                originalPrice: fetchedProduct.originalPrice,
                price: fetchedProduct.price,
                description: fetchedProduct.description,
                thumbnailImageUrl: fetchedProduct.thumbnailImageUrl || '',
                category: fetchedProduct.category,
                stock: fetchedProduct.stock?.quantity || 0
              };
            }
          } catch (error) {
            console.error('상품 정보를 가져오는 중 오류가 발생했습니다:', error);
          }
        }
      }
    });
    
    // 폼 제출 처리
    const handleSubmit = async () => {
      try {
        if (isCreate.value) {
          await productManagementStore.createProduct(form.value);
        } else if (props.productId) {
          await productManagementStore.updateProduct(props.productId, form.value);
        }
        
        emit('submit', form.value);
        emit('close');
      } catch (error) {
        console.error('상품 저장 중 오류가 발생했습니다:', error);
      }
    };
    
    // 모달 닫기
    const handleClose = () => {
      emit('close');
    };
    
    return {
      form,
      categories,
      isCreate,
      isLoading,
      handleSubmit,
      handleClose,
      ProductCategory
    };
  }
});
</script>
```

## 공개 API 설계

피처의 공개 API는 `index.ts` 파일을 통해 정의됩니다. 이는 피처의 내부 구현을 캡슐화하고 외부에 노출할 기능을 명확히 합니다.

```typescript
// features/productManagement/index.ts
// 모델
export type { ProductFormData, StockAdjustmentData, ProductFilter } from './model/types';
export { StockAdjustmentReason } from './model/constants';

// API
export { productManagementApi } from './api';
export type { ProductManagementRequest, ProductManagementResponse } from './api/types';

// 스토어
export { useProductManagementStore } from './model/store';

// UI 컴포넌트
export { default as ProductFormModal } from './ui/ProductFormModal.vue';
export { default as StockAdjustmentModal } from './ui/StockAdjustmentModal.vue';
export { default as ProductFilterPanel } from './ui/ProductFilterPanel.vue';
```

## 엔티티와의 상호작용

피처는 엔티티와 상호작용하여 비즈니스 기능을 구현합니다. 이는 주로 다음과 같은 방식으로 이루어집니다:

1. **엔티티 임포트**: 피처는 필요한 엔티티를 임포트하여 사용합니다.
2. **엔티티 스토어 활용**: 피처 스토어는 엔티티 스토어를 활용하여 데이터를 관리합니다.
3. **엔티티 컴포넌트 활용**: 피처 UI는 엔티티 UI 컴포넌트를 활용하여 구성됩니다.

```typescript
// features/auth/model/authStore.ts
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { useUserStore } from '@entities/user';
import { processOAuthCallback } from '../api';

export const useAuthStore = defineStore('auth', () => {
  // 상태
  const token = ref<string | null>(null);
  const loading = ref(false);
  const error = ref<string | null>(null);
  
  // 다른 스토어 참조
  const userStore = useUserStore();
  
  // 게터
  const isAuthenticated = computed(() => !!token.value);
  
  // 액션
  async function login(credentials: LoginCredentials) {
    // 로그인 로직...
    
    // 사용자 정보 설정 (엔티티 스토어 활용)
    userStore.setUser(userData);
  }
  
  async function logout() {
    // 로그아웃 로직...
    
    // 사용자 정보 초기화 (엔티티 스토어 활용)
    userStore.clearUser();
  }
  
  // 기타 액션...
  
  return {
    // 상태, 게터, 액션...
  };
});
```

## 모범 사례 및 패턴

### 1. 피처 설계 원칙

- **단일 책임**: 각 피처는 명확한 비즈니스 기능에 집중합니다.
- **독립성**: 피처는 다른 피처에 의존하지 않고 독립적으로 작동해야 합니다.
- **재사용성**: 공통 로직은 공유 레이어로 추출하여 재사용성을 높입니다.
- **명확한 경계**: 피처의 책임 범위를 명확히 정의합니다.

### 2. 상태 관리 패턴

- **엔티티 스토어 활용**: 도메인 데이터는 엔티티 스토어에서 관리하고, 피처 스토어는 UI 상태와 비즈니스 로직에 집중합니다.
- **상태 정규화**: 중복 상태를 최소화하고 단일 진실 소스(Single Source of Truth)를 유지합니다.
- **부수 효과 관리**: API 호출과 같은 부수 효과는 액션에서 처리합니다.

### 3. UI 컴포넌트 패턴

- **컨테이너/프레젠테이션 패턴**: 상태 관리와 UI 표현을 분리합니다.
- **컴포지션**: 작은 컴포넌트를 조합하여 복잡한 UI를 구성합니다.
- **재사용 가능한 폼**: 폼 로직을 재사용 가능한 컴포넌트로 추출합니다.

### 4. 에러 처리 패턴

- **일관된 에러 처리**: 모든 피처에서 일관된 방식으로 에러를 처리합니다.
- **사용자 친화적 메시지**: 기술적 에러 메시지를 사용자 친화적인 메시지로 변환합니다.
- **로깅**: 디버깅을 위해 에러를 적절히 로깅합니다.

### 5. 테스트 패턴

- **단위 테스트**: 비즈니스 로직과 유틸리티 함수에 대한 단위 테스트를 작성합니다.
- **컴포넌트 테스트**: UI 컴포넌트의 동작을 검증하는 테스트를 작성합니다.
- **통합 테스트**: 피처의 여러 부분이 함께 작동하는지 확인하는 테스트를 작성합