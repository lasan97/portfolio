# 피처(Feature) 개발 가이드

이 문서는 Portfolio 프로젝트에서 피처(Feature)를 개발하는 방법에 대한 가이드를 제공합니다.

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

피처의 모델은 해당 기능에 특화된 타입과 상태를 정의합니다.

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
    // 기타 필드...
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
import { ProductFormData } from '../model/types';

export const productManagementApi = {
  // 상품 생성
  async createProduct(data: ProductFormData): Promise<number> {
    const request: ProductManagementRequest.CreateProduct = {
      name: data.name,
      originalPrice: data.originalPrice,
      price: data.price,
      // 기타 매핑...
    };

    const response = await apiInstance.post<ProductManagementResponse.ProductResult>(
      '/api/admin/products',
      request
    );
    
    return response.data.id;
  },
  
  // 기타 API 함수...
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
import { useProductStore } from '@entities/product';

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
      // 필터링 로직...
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
  
  // 기타 액션...
  
  return {
    // 상태, 게터, 액션...
    isLoading,
    error,
    filteredProducts,
    createProduct,
    // ...
  };
});
```

## UI 컴포넌트 개발

피처의 UI 컴포넌트는 사용자 인터페이스를 구현합니다.

```vue
<!-- features/productManagement/ui/ProductFormModal.vue (간략화) -->
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
      
      <!-- 기타 폼 필드... -->
      
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
import { defineComponent, ref, computed, PropType } from 'vue';
import { ProductCategory } from '@entities/product';
import { Button, Modal } from '@shared/ui';
import { useProductManagementStore } from '../model/store';
import { ProductFormData } from '../model/types';

export default defineComponent({
  // 컴포넌트 정의...
});
</script>
```

## 공개 API 설계

피처의 공개 API는 `index.ts` 파일을 통해 정의됩니다.

```typescript
// features/productManagement/index.ts
// 모델
export type { ProductFormData, StockAdjustmentData } from './model/types';
export { StockAdjustmentReason } from './model/constants';

// API
export { productManagementApi } from './api';
export type { ProductManagementRequest, ProductManagementResponse } from './api/types';

// 스토어
export { useProductManagementStore } from './model/store';

// UI 컴포넌트
export { default as ProductFormModal } from './ui/ProductFormModal.vue';
export { default as StockAdjustmentModal } from './ui/StockAdjustmentModal.vue';
```

## 엔티티와의 상호작용

피처는 엔티티와 상호작용하여 비즈니스 기능을 구현합니다.

```typescript
// features/auth/model/authStore.ts
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { useUserStore } from '@entities/user';

export const useAuthStore = defineStore('auth', () => {
  // 상태
  const token = ref<string | null>(null);
  const loading = ref(false);
  
  // 다른 스토어 참조
  const userStore = useUserStore();
  
  // 게터
  const isAuthenticated = computed(() => !!token.value);
  
  // 액션
  async function login(credentials) {
    // 로그인 로직...
    
    // 사용자 정보 설정 (엔티티 스토어 활용)
    userStore.setUser(userData);
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

### 3. UI 컴포넌트 패턴

- **컨테이너/프레젠테이션 패턴**: 상태 관리와 UI 표현을 분리합니다.
- **컴포지션**: 작은 컴포넌트를 조합하여 복잡한 UI를 구성합니다.

### 4. 에러 처리 패턴

- **일관된 에러 처리**: 모든 피처에서 일관된 방식으로 에러를 처리합니다.
- **사용자 친화적 메시지**: 기술적 에러 메시지를 사용자 친화적인 메시지로 변환합니다.

### 5. 폴더 및 파일 구조 컨벤션

- **일관된 구조**: 모든 피처는 동일한 폴더 및 파일 구조를 따릅니다.
- **명확한 구분**: model, api, ui와 같은 하위 폴더로 관심사를 분리합니다.
- **인덱스 파일**: 각 폴더에는 내보내기를 관리하는 index.ts 파일이 있어야 합니다.
