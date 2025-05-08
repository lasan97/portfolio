# Enum 패턴 가이드

이 문서는 프로젝트에서 TypeScript Enum을 활용하는 방법과 Namespace를 통한 확장 패턴에 대해 설명합니다.

## 목차

1. [Enum 기본 개념](#enum-기본-개념)
2. [Namespace를 활용한 Enum 확장](#namespace를-활용한-enum-확장)
3. [실제 사용 예시](#실제-사용-예시)
4. [모범 사례](#모범-사례)

## Enum 기본 개념

TypeScript에서 Enum은 관련된 상수 값들의 집합을 정의할 때 사용합니다. 주로 상태, 카테고리, 유형 등을 표현할 때 활용합니다.

```typescript
// 기본적인 Enum 정의 방법
export enum ProductStatus {
  ACTIVE = 'ACTIVE',     // 판매중
  SOLD_OUT = 'SOLD_OUT', // 품절
  DELETED = 'DELETED'    // 삭제됨
}
```

## Namespace를 활용한 Enum 확장

TypeScript에서는 Enum과 동일한 이름의 Namespace를 선언하여 Enum을 확장할 수 있습니다. 이를 통해 Enum 값에 관련된 유틸리티 함수와 추가 데이터를 함께 제공할 수 있습니다.

### 기본 패턴

```typescript
// 1. Enum 정의
export enum ExampleStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE'
}

// 2. 동일한 이름의 네임스페이스 선언 (확장)
export namespace ExampleStatus {
  // 설명 매핑
  export const descriptions: Record<ExampleStatus, string> = {
    [ExampleStatus.ACTIVE]: '활성화',
    [ExampleStatus.INACTIVE]: '비활성화'
  };

  // getDescription 메서드
  export function getDescription(status: ExampleStatus): string {
    return descriptions[status] || status;
  }

  // entries 메서드 - key-value 쌍 배열 반환
  export function entries(): Array<{ code: ExampleStatus; description: string }> {
    return Object.values(ExampleStatus)
      .filter(value => typeof value === 'string') // enum에 추가되는 숫자 값 필터링
      .map(code => ({
        code: code as ExampleStatus,
        description: descriptions[code as ExampleStatus] || code as string
      }));
  }

  // values 메서드 - 모든 enum 값 반환
  export function values(): ExampleStatus[] {
    return Object.values(ExampleStatus)
      .filter(value => typeof value === 'string') as ExampleStatus[];
  }
}
```

## 실제 사용 예시

프로젝트에서는 다음과 같은 방식으로 Enum과 Namespace를 활용합니다:

### ProductCategory Enum

상품 카테고리를 표현하는 Enum과 Namespace 예제:

```typescript
// entities/product/model/constants.ts

// 상품 카테고리 Enum
export enum ProductCategory {
  ELECTRONICS = 'ELECTRONICS',
  FURNITURE = 'FURNITURE',
  HOME_APPLIANCE = 'HOME_APPLIANCE',
  CLOTHING = 'CLOTHING',
  SHOES = 'SHOES',
  ACCESSORIES = 'ACCESSORIES',
  BEAUTY = 'BEAUTY',
  HEALTH = 'HEALTH',
  SPORTS = 'SPORTS',
  BABY = 'BABY',
  FOOD = 'FOOD',
  BEVERAGE = 'BEVERAGE',
  BOOKS = 'BOOKS',
  STATIONERY = 'STATIONERY',
  KITCHEN = 'KITCHEN',
  BATHROOM = 'BATHROOM',
  BEDDING = 'BEDDING',
  PETS = 'PETS',
  PLANTS = 'PLANTS',
  DIGITAL_CONTENT = 'DIGITAL_CONTENT',
  TOYS = 'TOYS',
  AUTOMOTIVE = 'AUTOMOTIVE',
  OUTDOOR = 'OUTDOOR',
  TRAVEL = 'TRAVEL'
}

// 동일한 이름의 네임스페이스 선언 (확장)
export namespace ProductCategory {
  // 설명 매핑
  export const descriptions: Record<ProductCategory, string> = {
    [ProductCategory.ELECTRONICS]: '전자제품',
    [ProductCategory.FURNITURE]: '가구',
    [ProductCategory.HOME_APPLIANCE]: '가전제품',
    [ProductCategory.CLOTHING]: '의류',
    [ProductCategory.SHOES]: '신발',
    [ProductCategory.ACCESSORIES]: '액세서리',
    [ProductCategory.BEAUTY]: '뷰티/화장품',
    [ProductCategory.HEALTH]: '건강/의료용품',
    [ProductCategory.SPORTS]: '스포츠/레저',
    [ProductCategory.BABY]: '유아용품',
    [ProductCategory.FOOD]: '식품',
    [ProductCategory.BEVERAGE]: '음료',
    [ProductCategory.BOOKS]: '도서',
    [ProductCategory.STATIONERY]: '문구/사무용품',
    [ProductCategory.KITCHEN]: '주방용품',
    [ProductCategory.BATHROOM]: '욕실용품',
    [ProductCategory.BEDDING]: '침구류',
    [ProductCategory.PETS]: '반려동물용품',
    [ProductCategory.PLANTS]: '식물/원예용품',
    [ProductCategory.DIGITAL_CONTENT]: '디지털 콘텐츠',
    [ProductCategory.TOYS]: '장난감/취미용품',
    [ProductCategory.AUTOMOTIVE]: '자동차용품',
    [ProductCategory.OUTDOOR]: '아웃도어/캠핑',
    [ProductCategory.TRAVEL]: '여행용품'
  };

  // getDescription 메서드
  export function getDescription(category: ProductCategory): string {
    return descriptions[category] || category;
  }

  // entries 메서드 - key-value 쌍 배열 반환
  export function entries(): Array<{ code: ProductCategory; description: string }> {
    return Object.values(ProductCategory)
      .filter(value => typeof value === 'string') // enum에 추가되는 숫자 값 필터링
      .map(code => ({
        code: code as ProductCategory,
        description: descriptions[code as ProductCategory] || code as string
      }));
  }

  // values 메서드 - 모든 enum 값 반환
  export function values(): ProductCategory[] {
    return Object.values(ProductCategory)
      .filter(value => typeof value === 'string') as ProductCategory[];
  }
}
```

### 컴포넌트에서의 사용 예시

```vue
<!-- entities/product/ui/ProductCard.vue -->
<template>
  <div class="product-card">
    <!-- ... -->
    <span class="category">{{ getCategoryDescription(product.category) }}</span>
    <!-- ... -->
  </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue';
import { Product, ProductCategory } from '@entities/product';

export default defineComponent({
  name: 'ProductCard',
  props: {
    product: {
      type: Object as PropType<Product>,
      required: true
    }
  },
  setup() {
    return {
      // Namespace에서 제공하는 메서드 사용
      getCategoryDescription: ProductCategory.getDescription
    };
  }
});
</script>
```

## 모범 사례

### 1. 일관된 패턴 유지

모든 Enum에 대해 일관된 패턴을 유지하세요:

- 기본 설명 매핑 (`descriptions`)
- 설명 조회 메서드 (`getDescription`)
- 항목 목록 반환 메서드 (`entries`)
- 값 목록 반환 메서드 (`values`)

### 2. 확장 가능한 설계

필요에 따라 추가 속성이나 메서드를 확장할 수 있도록 설계하세요:

```typescript
export namespace OrderStatus {
  // 기본 설명 매핑
  export const descriptions: Record<OrderStatus, string> = { /* ... */ };
  
  // 추가 속성: 상태별 스타일 클래스
  export const statusClasses: Record<OrderStatus, string> = {
    [OrderStatus.PENDING]: 'bg-blue-100 text-blue-800',
    [OrderStatus.PAID]: 'bg-green-100 text-green-800',
    // ...
  };
  
  // 기본 메서드
  export function getDescription(status: OrderStatus): string { /* ... */ }
  
  // 추가 메서드: 스타일 클래스 가져오기
  export function getStatusClass(status: OrderStatus): string {
    return statusClasses[status] || 'bg-gray-100 text-gray-800';
  }
}
```

### 3. 주의사항

1. Enum 값은 가능한 변경되지 않도록 유지하세요. 백엔드 API와 매핑되는 경우가 많습니다.
2. 모든 Enum 케이스에 대한 매핑을 완전하게 제공하세요. 누락된 케이스가 없어야 합니다.
3. 기본 폴백 값을 제공하여 예외 상황에 대비하세요.
4. UI와 관련된 매핑(색상, 스타일 등)은 가능한 UI 계층과 분리하세요.
5. 열거형 이름과 네임스페이스 이름이 동일하므로 이름 충돌에 주의하세요.