# 프론트엔드 아키텍처 가이드라인

이 문서는 Portfolio 프로젝트의 프론트엔드 아키텍처 가이드라인을 설명합니다. FSD(Feature-Sliced Design) 아키텍처를 기반으로 하며, 특히 `entities`와 `features` 레이어 간의 구분과 타입 시스템 구조에 중점을 둡니다.

## 목차

1. [아키텍처 개요](#아키텍처-개요)
2. [레이어 구조](#레이어-구조)
3. [타입 시스템 구조](#타입-시스템-구조)
4. [모듈 간 참조 규칙](#모듈-간-참조-규칙)
5. [Import 패턴 가이드라인](#import-패턴-가이드라인)
6. [코드 품질 및 타입 안전성 가이드라인](#코드-품질-및-타입-안전성-가이드라인)

## 아키텍처 개요

본 프로젝트는 FSD(Feature-Sliced Design) 아키텍처를 채택하여 코드를 체계적으로 구성합니다. FSD는 다음과 같은 레이어로 구성됩니다:

**레이어 구성 (상위 -> 하위):**
  * `app`: 애플리케이션 진입점, 전역 설정, 라우터/스토어 초기화.
  * `processes`: 여러 페이지에 걸친 사용자 시나리오 및 복합 비즈니스 로직 (예: 온보딩 프로세스, 다단계 결제).
  * `pages`: 특정 경로에 매핑되는 UI 컴포넌트. `widgets`, `features`, `entities`를 조합하여 페이지 구성.
  * `widgets`: 독립적인 UI 블록. 여러 `features` 및 `entities`를 조합하여 특정 UI 영역 구성 (예: 헤더, 사이드바, 대시보드 패널). **비즈니스 로직 포함 최소화.**
  * `features`: 사용자 인터랙션 기반의 독립적인 기능 단위. 특정 비즈니스 가치를 제공 (예: 사용자 인증, 상품 검색, 장바구니 담기). **상태 관리 및 API 호출 포함 가능.**
  * `entities`: 핵심 비즈니스 도메인 모델 및 관련 로직/UI. (예: User, Product, Order). **재사용 가능한 최소 단위.**
  * `shared`: 특정 도메인/기능에 종속되지 않는 공통 유틸리티, UI 키트, 설정 등. **애플리케이션의 기반.**

각 레이어는 자신보다 아래에 있는 레이어만 의존할 수 있습니다(단방향 의존성).

## 레이어 구조

### 1. Entities 레이어

엔티티 레이어는 비즈니스 도메인 객체를 정의합니다. 각 엔티티는 다음과 같은 구조를 가집니다:

```
/entities/{도메인}
  ├── model/
  │   ├── types.ts         # 도메인 모델 타입 정의
  │   ├── constants.ts     # 열거형 및 상수
  │   └── store.ts         # 도메인 상태 관리 (선택적)
  ├── api/
  │   ├── types.ts         # API 요청/응답 타입
  │   ├── repository.ts    # API 통신 함수
  │   └── mappers.ts       # API ↔ 도메인 변환 함수
  ├── ui/
  │   └── {Component}.vue  # 도메인 관련 기본 UI 컴포넌트
  └── index.ts             # 공개 API 정의
```

### 2. Features 레이어

피처 레이어는 사용자 인터랙션과 비즈니스 기능을 정의합니다:

```
/features/{기능명}
  ├── model/
  │   ├── types.ts         # 기능별 타입 (폼 데이터, UI 상태 등)
  │   └── store.ts         # 기능별 상태 관리
  ├── api/
  │   ├── types.ts         # 기능별 API 타입
  │   └── requests.ts      # 기능별 API 요청
  ├── ui/
  │   └── {Component}.vue  # 기능별 UI 컴포넌트
  └── index.ts             # 공개 API 정의
```

### 3. 기타 레이어

- `shared` - 공통 컴포넌트, 유틸리티, API 인스턴스 등
- `widgets` - 복잡한 UI 블록(대시보드, 탭 패널 등)
- `pages` - 라우팅 및 페이지 컴포넌트
- `app` - 애플리케이션 설정, 전역 스타일, 플러그인 등

## 타입 시스템 구조

### 1. Entities 레이어의 타입

#### 도메인 모델 타입 (`model/types.ts`)

```typescript
// 핵심 도메인 모델 타입
export interface Product {
  id: number;
  name: string;
  price: number;
  // 기타 속성...
}

// 타입으로 export하는 경우 (인터페이스)
export type { Product };

// 값으로 export하는 경우 (열거형, 클래스 등)
export { ProductStatus };
```

#### API 타입 (`api/types.ts`)

```typescript
// API 요청 타입
export namespace ProductRequest {
  export interface Create {
    name: string;
    price: number;
    // 기타 속성...
  }
  
  export interface Update {
    name?: string;
    price?: number;
    // 기타 속성...
  }
}

// API 응답 타입
export namespace ProductResponse {
  export interface Detail {
    id: number;
    name: string;
    price: number;
    // 기타 속성...
  }
  
  export interface List {
    // 목록 속성...
  }
}
```

#### API 매핑 함수 (`api/mappers.ts`)

```typescript
import { Product } from '../model/types';
import { ProductResponse } from './types';

export const mapToProduct = (response: ProductResponse.Detail): Product => ({
  id: response.id,
  name: response.name,
  price: response.price,
  // 기타 매핑...
});

export const mapToProducts = (response: ProductResponse.List[]): Product[] => 
  response.map(mapToProduct);
```

### 2. Features 레이어의 타입

#### 기능별 타입 (`model/types.ts`)

```typescript
import { Product } from '@entities/product';

// 폼 데이터 타입
export interface ProductFormData {
  name: string;
  price: number;
  // 기타 속성...
}

// UI 상태 타입
export interface ProductFilterState {
  category: string | null;
  priceRange: [number, number];
  // 기타 속성...
}

// 타입으로 export
export type { ProductFormData, ProductFilterState };
```

## 모듈 간 참조 규칙

타입 참조 방향에 관한 규칙:

1. `features`는 `entities`의 타입과 repository를 직접 참조할 수 있습니다.
2. `entities`는 다른 `entities`를 참조할 수 있습니다.
3. `entities`는 `features`를 참조할 수 없습니다.
4. 모든 레이어는 `shared`를 참조할 수 있습니다.
5. **중요**: 엔티티의 리포지토리나 스토어는 직접 참조해야 합니다. 불필요한 중간 계층은 피하세요.

### 타입 배치 결정 기준

타입을 어디에 배치할지 결정할 때는 다음 질문을 고려하세요:

1. 이 타입이 비즈니스 도메인의 핵심 개념인가? → `entities`
2. 이 타입이 특정 기능이나 사용자 인터랙션에만 관련된 것인가? → `features`
3. 이 타입이 여러 기능에서 공유되는가? → `entities` 또는 `shared`
4. 이 타입이 UI 상태나 폼 데이터와 관련이 있는가? → `features`

## Import 패턴 가이드라인

### 올바른 Import 패턴

모듈을 import할 때는 항상 `index.ts`를 통해 공개된 API만 사용해야 합니다:

```typescript
// 올바른 방식 (권장)
import { Product, useProductStore, productRepository } from '@entities/product';
import { ProductFormModal } from '@features/product-management';

// 잘못된 방식 (사용하지 말 것)
import ProductFormModal from '@features/product-management/ui/ProductFormModal.vue';
import { useProductStore } from '@entities/product/model/store';
```

### 이유

1. **캡슐화**: 내부 구현 세부 사항을 숨기고 공개 API만 노출합니다.
2. **리팩터링 유연성**: 내부 구조를 변경하더라도 공개 API가 유지되면 상위 모듈에 영향을 주지 않습니다.
3. **의존성 관리**: 필요한 것만 명시적으로 내보내어 모듈 간 의존성을 명확히 합니다.

### Index.ts 작성 예시

```typescript
// entities/product/index.ts
// 도메인 모델
export type { Product, ProductVariant } from './model/types';
export { ProductStatus, ProductCategory } from './model/constants';

// API 타입 및 함수
export type { ProductRequest, ProductResponse } from './api/types';
export { productRepository } from './api/repository';

// 스토어
export { useProductStore } from './model/store';

// UI 컴포넌트
export { default as ProductCard } from './ui/ProductCard.vue';
export { default as ProductImage } from './ui/ProductImage.vue';
```

## 코드 품질 및 타입 안전성 가이드라인

### 1. 타입 정의 규칙

- `any` 타입 사용을 최소화합니다.
- 함수의 매개변수와 반환 타입을 항상 명시합니다.
- 인터페이스의 경우 `export type`을 사용합니다.
- 열거형, 클래스, 상수 등 실제 값이 있는 경우 `export`를 사용합니다.
- 타입 단언(`as`)은 최소한으로 사용합니다.

### 2. 네이밍 컨벤션

- 인터페이스와 타입: PascalCase (예: `ProductDetail`)
- 변수와 함수: camelCase (예: `getProductById`)
- 열거형: PascalCase (예: `ProductStatus`)
- 상수: UPPER_SNAKE_CASE 또는 camelCase

### 3. 널 안전성

- 선택적 속성(`?`) 및 널 체크 연산자(`?.`)를 적극 활용합니다.
- 기본값을 제공하여 널 또는 undefined 상태를 처리합니다.

```typescript
// 안전한 접근
const productName = product?.name || '제품명 없음';

// 조건부 렌더링
<div v-if="product && product.inStock">
  <!-- 컴포넌트 -->
</div>
```

### 4. 매핑 패턴

항상 명시적인 매핑 함수를 사용하여 타입 간 변환을 수행합니다:

```typescript
// API → 도메인
const product = mapToProduct(apiResponse);

// 도메인 → UI
const formData = mapToFormData(product);

// UI → API
const requestData = mapToCreateRequest(formData);
```

---

이 가이드라인을 따르면 코드베이스의 일관성을 유지하고, 타입 안전성을 강화하며, 레이어 간 책임을 명확히 구분할 수 있습니다.
