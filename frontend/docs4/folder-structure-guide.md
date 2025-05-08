# 폴더 및 파일 구조 가이드

## 목차

1. [개요](#개요)
2. [루트 폴더 구조](#루트-폴더-구조)
3. [FSD 레이어 폴더 구조](#fsd-레이어-폴더-구조)
4. [슬라이스 폴더 구조](#슬라이스-폴더-구조)
5. [세그먼트 폴더 구조](#세그먼트-폴더-구조)
6. [파일 명명 규칙](#파일-명명-규칙)
7. [파일 내용 구성](#파일-내용-구성)
8. [상대 경로 vs 절대 경로](#상대-경로-vs-절대-경로)
9. [실제 폴더 구조 예시](#실제-폴더-구조-예시)

## 개요

이 가이드는 프론트엔드 프로젝트의 폴더 및 파일 구조에 대한 지침을 제공합니다. FSD(Feature-Sliced Design) 아키텍처를 기반으로 하며, 코드의 구성, 재사용성, 유지보수성을 개선하기 위한 모범 사례를 포함합니다.

## 루트 폴더 구조

프로젝트 루트 폴더는 다음과 같은 구조를 가집니다:

```
frontend/
├── .env                  # 환경 변수 (개발)
├── .env.production       # 환경 변수 (프로덕션)
├── index.html            # 진입점 HTML
├── package.json          # 의존성 및 스크립트
├── tsconfig.json         # TypeScript 설정
├── vite.config.ts        # Vite 설정
├── public/               # 정적 파일
│   ├── favicon.ico
│   └── image/
├── docs/                 # 문서
│   └── ...
├── src/                  # 소스 코드
│   ├── app/
│   ├── processes/
│   ├── pages/
│   ├── widgets/
│   ├── features/
│   ├── entities/
│   └── shared/
└── dist/                 # 빌드 결과물
    ├── client/
    └── server/
```

## FSD 레이어 폴더 구조

FSD 아키텍처는 코드를 다음과 같은 레이어로 구성합니다:

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

각 레이어는 특정 책임과 역할을 가지며, 레이어 간 의존성 방향은 항상 위에서 아래로 흐릅니다.

### app/

애플리케이션의 진입점과 전역 설정을 포함합니다:

```
app/
├── App.vue           # 루트 컴포넌트
├── index.ts          # 진입점
├── router/           # 라우팅 설정
│   ├── index.ts
│   └── routeConfig.ts
├── store/            # 전역 스토어 설정
│   └── index.ts
├── providers/        # 전역 프로바이더
│   ├── index.ts
│   ├── theme.ts
│   └── i18n.ts
└── styles/           # 전역 스타일
    ├── main.css
    ├── variables.css
    └── tailwind.css
```

### processes/

비즈니스 프로세스를 구현합니다:

```
processes/
├── auth/             # 인증 프로세스
│   ├── index.ts
│   ├── model/
│   └── lib/
└── checkout/         # 결제 프로세스
    ├── index.ts
    ├── model/
    └── lib/
```

### pages/

애플리케이션의 페이지를 정의합니다:

```
pages/
├── home/             # 홈 페이지
│   ├── index.ts
│   └── ui/
│       └── HomePage.vue
├── products/         # 상품 목록 페이지
│   ├── index.ts
│   ├── model/
│   │   └── hooks/
│   │       └── useProductList.ts
│   └── ui/
│       └── ProductsPage.vue
└── product-detail/   # 상품 상세 페이지
    ├── index.ts
    ├── model/
    │   └── hooks/
    │       └── useProductDetail.ts
    └── ui/
        └── ProductDetailPage.vue
```

### widgets/

독립적인 UI 블록을 구현합니다:

```
widgets/
├── header/           # 헤더 위젯
│   ├── index.ts
│   └── ui/
│       └── Header.vue
├── footer/           # 푸터 위젯
│   ├── index.ts
│   └── ui/
│       └── Footer.vue
└── product-list/     # 상품 목록 위젯
    ├── index.ts
    ├── model/
    │   └── filters.ts
    └── ui/
        └── ProductList.vue
```

### features/

사용자 상호작용 기능을 구현합니다:

```
features/
├── auth/             # 인증 기능
│   ├── index.ts
│   ├── api/
│   ├── model/
│   └── ui/
│       ├── LoginForm.vue
│       └── RegisterForm.vue
├── cart/             # 장바구니 기능
│   ├── index.ts
│   ├── api/
│   ├── model/
│   └── ui/
│       ├── AddToCartButton.vue
│       └── CartItem.vue
└── product-management/ # 상품 관리 기능
    ├── index.ts
    ├── api/
    ├── model/
    └── ui/
        ├── ProductFormModal.vue
        └── StockAdjustmentModal.vue
```

### entities/

비즈니스 엔티티를 정의합니다:

```
entities/
├── product/          # 상품 엔티티
│   ├── index.ts
│   ├── api/
│   │   ├── repository.ts
│   │   ├── types.ts
│   │   └── mappers.ts
│   ├── model/
│   │   ├── types.ts
│   │   ├── constants.ts
│   │   └── productStore.ts
│   └── ui/
│       └── ProductCard.vue
├── user/             # 사용자 엔티티
│   ├── index.ts
│   ├── api/
│   ├── model/
│   └── ui/
└── order/            # 주문 엔티티
    ├── index.ts
    ├── api/
    ├── model/
    └── ui/
```

### shared/

공유 유틸리티, 타입, UI 컴포넌트를 포함합니다:

```
shared/
├── api/              # API 클라이언트
│   ├── index.ts
│   ├── instance.ts
│   └── error.ts
├── ui/               # 공통 UI 컴포넌트
│   ├── Button/
│   ├── Input/
│   └── Modal/
├── lib/              # 유틸리티 함수
│   ├── date/
│   ├── format/
│   └── validation/
├── hooks/            # 공통 훅
│   ├── useForm.ts
│   └── useAsync.ts
├── config/           # 설정 상수
│   ├── index.ts
│   └── endpoints.ts
└── types/            # 공통 타입 정의
    └── index.ts
```

## 슬라이스 폴더 구조

슬라이스(예: `entities/product`, `features/cart`)는 특정 도메인이나 기능에 초점을 맞춘 코드 단위입니다. 각 슬라이스는 다음과 같은 구조를 가집니다:

```
slice/
├── index.ts        # 공개 API
├── api/            # API 통신
├── model/          # 비즈니스 로직, 타입, 상태
├── ui/             # UI 컴포넌트
└── lib/            # 유틸리티 함수 (필요시)
```

### index.ts

슬라이스의 공개 API를 정의합니다:

```typescript
// entities/product/index.ts

// 도메인 모델
export type { Product, ProductStock } from './model/types';
export { ProductCategory, ProductStatus } from './model/constants';

// API 모델 및 함수
export type { ProductRequest, ProductResponse } from './api/types';
export { productRepository } from './api/repository';

// UI 컴포넌트
export { default as ProductCard } from './ui/ProductCard.vue';

// 스토어
export { useProductStore } from './model/productStore';
```

## 세그먼트 폴더 구조

각 슬라이스 내의 세그먼트는 특정 기술적 관심사에 따라 코드를 분류합니다:

### api/

API 통신 관련 코드:

```
api/
├── repository.ts      # API 요청 함수
├── types.ts           # API 요청/응답 타입
└── mappers.ts         # API 데이터 변환 함수
```

### model/

비즈니스 로직, 타입, 상태 관리:

```
model/
├── types.ts           # 도메인 타입 정의
├── constants.ts       # 상수 정의
├── productStore.ts    # 상태 관리 스토어
└── helpers.ts         # 도메인 특화 헬퍼 함수
```

### ui/

UI 컴포넌트:

```
ui/
├── ComponentName.vue  # 메인 컴포넌트
└── components/        # 내부 컴포넌트
    ├── SubComponent1.vue
    └── SubComponent2.vue
```

## 파일 명명 규칙

파일 및 폴더 이름에 대한 명명 규칙:

1. **폴더 이름**:
   - 레이어 폴더: 복수형, camelCase (예: `entities`, `features`)
   - 슬라이스 폴더: 단수형, camelCase 또는 kebab-case (예: `product`, `product-management`)
   - 세그먼트 폴더: camelCase (예: `api`, `model`, `ui`)

2. **파일 이름**:
   - Vue 컴포넌트: PascalCase (예: `ProductCard.vue`, `LoginForm.vue`)
   - 기타 TypeScript 파일: camelCase (예: `repository.ts`, `types.ts`)
   - 상수/타입 전용 파일: camelCase (예: `constants.ts`, `types.ts`)
   - 유틸리티 함수 파일: camelCase (예: `formatters.ts`, `validators.ts`)

3. **특수 파일**:
   - 내보내기 파일: `index.ts`
   - 스토어 파일: `{entityName}Store.ts` (예: `productStore.ts`)
   - 리포지토리 파일: `repository.ts`

## 파일 내용 구성

파일 내용 구성에 대한 지침:

### Vue 컴포넌트 파일 (.vue)

```vue
<template>
  <!-- 템플릿 코드 -->
</template>

<script setup lang="ts">
// 임포트
import { ref, computed } from 'vue';
import type { PropType } from 'vue';
import { SomeType } from './types';

// Props 정의
const props = defineProps<{
  // Props 타입
}>();

// Emits 정의
const emit = defineEmits<{
  // Emits 타입
}>();

// 상태 및 로직
// ...

// 이벤트 핸들러
// ...
</script>

<style scoped>
/* 스타일 */
</style>
```

### TypeScript 타입 파일 (types.ts)

```typescript
// 외부 타입 임포트
import { SomeExternalType } from 'some-package';

// 인터페이스 정의
export interface Entity {
  id: number;
  name: string;
  // ...
}

// 타입 에일리어스
export type EntityId = number;

// 유니언 타입
export type EntityStatus = 'active' | 'inactive' | 'deleted';

// 제네릭 타입
export interface Paginated<T> {
  items: T[];
  total: number;
  page: number;
  pageSize: number;
}
```

### 리포지토리 파일 (repository.ts)

```typescript
// API 클라이언트 및 타입 임포트
import { apiInstance } from '@shared/api';
import type { EntityRequest, EntityResponse } from './types';
import type { Entity } from '../model/types';
import { mapToEntity, mapToEntities } from './mappers';

// 리포지토리 객체
export const entityRepository = {
  // 목록 조회
  async getEntities(): Promise<Entity[]> {
    const response = await apiInstance.get<EntityResponse.List[]>('/entities');
    return mapToEntities(response.data);
  },
  
  // 단일 항목 조회
  async getEntity(id: number): Promise<Entity> {
    const response = await apiInstance.get<EntityResponse.Detail>(`/entities/${id}`);
    return mapToEntity(response.data);
  },
  
  // 생성
  async createEntity(data: EntityRequest.Create): Promise<number> {
    const response = await apiInstance.post<{ id: number }>('/entities', data);
    return response.data.id;
  },
  
  // 수정
  async updateEntity(id: number, data: EntityRequest.Update): Promise<void> {
    await apiInstance.put(`/entities/${id}`, data);
  },
  
  // 삭제
  async deleteEntity(id: number): Promise<void> {
    await apiInstance.delete(`/entities/${id}`);
  }
};
```

### 스토어 파일 ({entityName}Store.ts)

```typescript
// 임포트
import { defineStore } from 'pinia';
import type { Entity } from './types';
import { entityRepository } from '../api/repository';

// 스토어 정의
export const useEntityStore = defineStore('entity', {
  // 상태
  state: () => ({
    entities: [] as Entity[],
    isLoading: false,
    error: null as string | null,
  }),
  
  // 게터
  getters: {
    getEntityById: (state) => (id: number) => 
      state.entities.find(entity => entity.id === id),
    // ...
  },
  
  // 액션
  actions: {
    async fetchEntities() {
      this.isLoading = true;
      this.error = null;
      
      try {
        this.entities = await entityRepository.getEntities();
      } catch (err: any) {
        this.error = err.message || '데이터를 불러오는 중 오류가 발생했습니다.';
        console.error('Error fetching entities:', err);
      } finally {
        this.isLoading = false;
      }
    },
    // ...
  }
});
```

## 상대 경로 vs 절대 경로

파일 임포트에는 다음과 같은 경로 규칙을 사용합니다:

1. **레이어 간 임포트**: 절대 경로 사용
   ```typescript
   // 좋은 예
   import { Product } from '@entities/product';
   import { Button } from '@shared/ui';
   
   // 나쁜 예
   import { Product } from '../../entities/product';
   ```

2. **동일 슬라이스 내 임포트**: 상대 경로 사용
   ```typescript
   // 좋은 예
   import { Product } from '../model/types';
   import { mapToProduct } from './mappers';
   
   // 나쁜 예
   import { Product } from '@entities/product/model/types';
   ```

3. **paths 별칭 설정**:
   ```typescript
   // tsconfig.json
   {
     "compilerOptions": {
       "paths": {
         "@app/*": ["src/app/*"],
         "@processes/*": ["src/processes/*"],
         "@pages/*": ["src/pages/*"],
         "@widgets/*": ["src/widgets/*"],
         "@features/*": ["src/features/*"],
         "@entities/*": ["src/entities/*"],
         "@shared/*": ["src/shared/*"]
       }
     }
   }
   ```

## 실제 폴더 구조 예시

다음은 Product 도메인에 대한 실제 폴더 구조 예시입니다:

```
entities/
└── product/
    ├── index.ts
    ├── api/
    │   ├── repository.ts
    │   ├── types.ts
    │   └── mappers.ts
    ├── model/
    │   ├── types.ts
    │   ├── constants.ts
    │   ├── productStore.ts
    │   └── ui-types.ts
    └── ui/
        ├── ProductCard.vue
        └── components/
            ├── ProductPrice.vue
            └── ProductImage.vue

features/
└── productManagement/
    ├── index.ts
    ├── api/
    │   ├── requests.ts
    │   └── types.ts
    ├── model/
    │   ├── types.ts
    │   └── mappers.ts
    └── ui/
        ├── ProductFormModal.vue
        └── StockAdjustmentModal.vue

widgets/
└── productList/
    ├── index.ts
    ├── model/
    │   └── filters.ts
    └── ui/
        └── ProductList.vue

pages/
└── products/
    ├── index.ts
    ├── model/
    │   └── hooks/
    │       └── useProductsPage.ts
    └── ui/
        ├── ProductsPage.vue
        └── ProductDetailPage.vue
```

이런 구조를 따르면 코드의 구성, 재사용성, 유지보수성이 향상되며, 개발자들이 코드베이스를 더 쉽게 이해하고 탐색할 수 있습니다.
