# Enum 패턴 가이드

## 목차

1. [TypeScript Enum 소개](#typescript-enum-소개)
2. [Namespace로 확장된 Enum 패턴](#namespace로-확장된-enum-패턴)
3. [기본 패턴 구현 방법](#기본-패턴-구현-방법)
4. [실제 구현 예시](#실제-구현-예시)
5. [모범 사례 및 권장사항](#모범-사례-및-권장사항)
6. [컴포넌트에서의 사용 예시](#컴포넌트에서의-사용-예시)
7. [확장 가능한 설계](#확장-가능한-설계)

## TypeScript Enum 소개

TypeScript의 Enum은 관련된 상수 값들의 집합을 정의할 때 사용합니다. 주로 상태, 카테고리, 유형 등을 표현할 때 활용되며, 코드의 가독성과 유지보수성을 높입니다.

```typescript
// 기본적인 Enum 정의
export enum ProductStatus {
  ACTIVE = 'ACTIVE',     // 판매중
  SOLD_OUT = 'SOLD_OUT', // 품절
  DELETED = 'DELETED'    // 삭제됨
}
```

기본 Enum의 한계:
- 단순 값 매핑만 가능
- 메타데이터(설명, 라벨 등) 추가 불가
- 헬퍼 함수를 Enum과 함께 제공할 수 없음

## Namespace로 확장된 Enum 패턴

TypeScript에서는 Enum과 동일한 이름의 Namespace를 선언하여 Enum을 확장할 수 있습니다. 이 패턴을 통해 Java의 Enum과 유사하게 사용할 수 있으며, 다음과 같은 이점이 있습니다:

- Enum 값에 추가 메타데이터(설명, 라벨 등) 연결 가능
- 헬퍼 메서드 제공 가능 (값 조회, 변환, 검증 등)
- 엔티티와 UI 레이어 간의 자연스러운 연결 구현 가능

이 패턴은 Java의 Enum 클래스와 유사한 기능을 TypeScript에서 구현하는 방법으로, 백엔드와의 일관된 도메인 모델 표현을 가능하게 합니다.

## 기본 패턴 구현 방법

Namespace로 확장된 Enum 패턴의 기본 구현 방법은 다음과 같습니다:

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

## 실제 구현 예시

프로젝트에서 실제로 사용 중인 ProductCategory Enum의 예시:

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

// 재고 변경 사유 Enum
export enum StockChangeReason {
  ADJUSTMENT = 'ADJUSTMENT', // 재고조정
  LOSS = 'LOSS'              // 손실
}

// 재고 변경 사유에 대한 설명을 제공하는 네임스페이스
export namespace StockChangeReason {
  export const descriptions: Record<StockChangeReason, string> = {
    [StockChangeReason.ADJUSTMENT]: '재고조정',
    [StockChangeReason.LOSS]: '손실'
  };

  export function getDescription(reason: StockChangeReason): string {
    return descriptions[reason];
  }

  // entries 메서드
  export function entries(): Array<{ code: StockChangeReason; description: string }> {
    return Object.values(StockChangeReason)
      .filter(value => typeof value === 'string')
      .map(code => ({
        code: code as StockChangeReason,
        description: descriptions[code as StockChangeReason] || code as string
      }));
  }

  // values 메서드
  export function values(): StockChangeReason[] {
    return Object.values(StockChangeReason)
      .filter(value => typeof value === 'string') as StockChangeReason[];
  }
}
```

## 모범 사례 및 권장사항

Enum과 Namespace를 사용할 때 다음과 같은 모범 사례를 따르는 것이 좋습니다:

1. **일관된 패턴 유지**:
   - 모든 Enum에 대해 동일한 구조와 API를 제공
   - 기본 설명 매핑 (`descriptions`)
   - 설명 조회 메서드 (`getDescription`)
   - 항목 목록 반환 메서드 (`entries`)
   - 값 목록 반환 메서드 (`values`)

2. **안전한 열거형 사용**:
   - Enum 값은 문자열 리터럴 타입으로 정의 (숫자 enum은 지양)
   - Enum 값을 API 응답과 매핑할 때 타입 안전성 확보
   - 필터링에 `typeof value === 'string'` 사용하여 숫자 값 제거

3. **조건부 로직 대신 객체 매핑 사용**:
   - 긴 if-else 또는 switch 문 대신 객체 매핑 사용
   ```typescript
   // 좋은 예
   const statusClasses = {
     [ProductStatus.ACTIVE]: 'text-green-600',
     [ProductStatus.SOLD_OUT]: 'text-red-600',
     [ProductStatus.DELETED]: 'text-gray-400'
   };
   
   // 나쁜 예
   let statusClass = '';
   if (status === ProductStatus.ACTIVE) {
     statusClass = 'text-green-600';
   } else if (status === ProductStatus.SOLD_OUT) {
     statusClass = 'text-red-600';
   } else if (status === ProductStatus.DELETED) {
     statusClass = 'text-gray-400';
   }
   ```

4. **완전한 매핑 제공**:
   - 모든 Enum 값에 대한 매핑을 완전하게 제공 (누락된 값 없도록)
   - 기본 폴백 값을 항상 제공하여 예외 상황 대비

5. **백엔드 API와의 일관성 유지**:
   - 백엔드 API와 일치하는 Enum 값 사용
   - API 응답 타입과 도메인 모델 사이의 명확한 매핑 구현

## 컴포넌트에서의 사용 예시

```vue
<!-- 컴포넌트 사용 예시 -->
<template>
  <div class="product-card">
    <div class="category">
      {{ getCategoryDescription(product.category) }}
    </div>
    <!-- ... -->
    <span 
      class="status"
      :class="getStatusClass(product.status)"
    >
      {{ getStatusDescription(product.status) }}
    </span>
  </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue';
import { 
  Product, 
  ProductCategory, 
  ProductStatus 
} from '@entities/product';

export default defineComponent({
  name: 'ProductCard',
  props: {
    product: {
      type: Object as PropType<Product>,
      required: true
    }
  },
  setup() {
    // Namespace에서 제공하는 메서드 활용
    const getStatusClass = (status?: ProductStatus): string => {
      if (!status) return '';
      
      const statusClasses = {
        [ProductStatus.ACTIVE]: 'text-green-600',
        [ProductStatus.SOLD_OUT]: 'text-red-600',
        [ProductStatus.DELETED]: 'text-gray-400'
      };
      
      return statusClasses[status] || '';
    };
    
    return {
      getCategoryDescription: ProductCategory.getDescription,
      getStatusDescription: ProductStatus.getDescription,
      getStatusClass
    };
  }
});
</script>
```

## 확장 가능한 설계

enum 패턴은 필요에 따라 추가 속성이나 메서드로 확장할 수 있습니다:

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
  
  // 추가 속성: 상태별 아이콘
  export const icons: Record<OrderStatus, string> = {
    [OrderStatus.PENDING]: 'clock',
    [OrderStatus.PAID]: 'check-circle',
    // ...
  };
  
  // 기본 메서드
  export function getDescription(status: OrderStatus): string { /* ... */ }
  
  // 추가 메서드: 스타일 클래스 가져오기
  export function getStatusClass(status: OrderStatus): string {
    return statusClasses[status] || 'bg-gray-100 text-gray-800';
  }
  
  // 추가 메서드: 아이콘 가져오기
  export function getIcon(status: OrderStatus): string {
    return icons[status] || 'question-mark';
  }
  
  // 추가 메서드: 다음 가능한 상태 조회
  export function getNextPossibleStatuses(currentStatus: OrderStatus): OrderStatus[] {
    const statusFlow = {
      [OrderStatus.PENDING]: [OrderStatus.PAID, OrderStatus.CANCELED],
      [OrderStatus.PAID]: [OrderStatus.SHIPPED, OrderStatus.REFUNDED],
      // ...
    };
    
    return statusFlow[currentStatus] || [];
  }
}
```

이런 방식으로 Enum을 확장하면 도메인 로직을 더 효과적으로 표현하고, UI와 비즈니스 로직 사이의 일관성을 유지할 수 있습니다.
