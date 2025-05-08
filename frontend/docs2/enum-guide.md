# Enum 사용 가이드

이 문서는 프로젝트에서 Enum을 활용하는 방법과 표준화된 패턴에 대해 설명합니다.

## 목차

1. [Enum 기본 구조](#enum-기본-구조)
2. [Namespace 활용](#namespace-활용)
3. [Enum 사용 예제](#enum-사용-예제)
4. [Enum 확장 방법](#enum-확장-방법)

## Enum 기본 구조

Enum은 관련된 상수 값들의 집합을 정의할 때 사용합니다. 주로 상태, 카테고리, 유형 등을 표현할 때 활용합니다.

```typescript
// 기본적인 Enum 정의 방법
export enum ExampleStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  PENDING = 'PENDING'
}
```

## Namespace 활용

Enum에 Namespace를 활용하면 관련 유틸리티 함수와 추가 데이터를 함께 제공할 수 있습니다. 이를 통해 코드의 가독성과 재사용성을 높일 수 있습니다.

### 기본 패턴

```typescript
// Enum과 Namespace 결합 패턴
export enum SampleCategory {
  TYPE_A = 'TYPE_A',
  TYPE_B = 'TYPE_B',
  TYPE_C = 'TYPE_C'
}

// SampleCategory Namespace
export namespace SampleCategory {
  // 설명 매핑
  export const descriptions: Record<SampleCategory, string> = {
    [SampleCategory.TYPE_A]: 'A 타입',
    [SampleCategory.TYPE_B]: 'B 타입',
    [SampleCategory.TYPE_C]: 'C 타입'
  };

  // getDescription 메서드
  export function getDescription(category: SampleCategory): string {
    return descriptions[category] || category;
  }

  // entries 메서드 - key-value 쌍 배열 반환
  export function entries(): Array<{ code: SampleCategory; description: string }> {
    return Object.values(SampleCategory)
      .filter(value => typeof value === 'string') // enum에 추가되는 숫자 값 필터링
      .map(code => ({
        code: code as SampleCategory,
        description: descriptions[code as SampleCategory] || code as string
      }));
  }

  // values 메서드 - 모든 enum 값 반환
  export function values(): SampleCategory[] {
    return Object.values(SampleCategory)
      .filter(value => typeof value === 'string') as SampleCategory[];
  }
}
```

## Enum 사용 예제

프로젝트에서는 다음과 같은 방식으로 Enum을 활용합니다:

### 1. ProductCategory Enum

상품 카테고리를 표현하는 Enum 예제:

```typescript
export enum ProductCategory {
  ELECTRONICS = 'ELECTRONICS',
  FURNITURE = 'FURNITURE',
  HOME_APPLIANCE = 'HOME_APPLIANCE',
  // ... 기타 카테고리
}

// ProductCategory Namespace
export namespace ProductCategory {
  // 설명 매핑
  export const descriptions: Record<ProductCategory, string> = {
    [ProductCategory.ELECTRONICS]: '전자제품',
    [ProductCategory.FURNITURE]: '가구',
    [ProductCategory.HOME_APPLIANCE]: '가전제품',
    // ... 기타 카테고리 설명
  };

  // getDescription 메서드
  export function getDescription(category: ProductCategory): string {
    return descriptions[category] || category;
  }

  // entries 메서드
  export function entries(): Array<{ code: ProductCategory; description: string }> {
    return Object.values(ProductCategory)
      .filter(value => typeof value === 'string')
      .map(code => ({
        code: code as ProductCategory,
        description: descriptions[code as ProductCategory] || code as string
      }));
  }

  // values 메서드
  export function values(): ProductCategory[] {
    return Object.values(ProductCategory)
      .filter(value => typeof value === 'string') as ProductCategory[];
  }
}
```

### 2. OrderStatus Enum

주문 상태를 표현하는 Enum 예제:

```typescript
export enum OrderStatus {
  PENDING = 'PENDING',         // 대기
  PAID = 'PAID',               // 결제완료
  FAILED = 'FAILED',           // 주문 실패
  CANCELLED = 'CANCELLED',     // 주문 취소
  CANCELING = 'CANCELING',     // 주문 취소 요청
  ORDERED = 'ORDERED'          // 주문 완료
}

// OrderStatus Namespace
export namespace OrderStatus {
  // 설명 매핑
  export const descriptions: Record<OrderStatus, string> = {
    [OrderStatus.PENDING]: '대기',
    [OrderStatus.PAID]: '결제완료',
    [OrderStatus.FAILED]: '주문 실패',
    [OrderStatus.CANCELLED]: '주문 취소',
    [OrderStatus.CANCELING]: '주문 취소 요청',
    [OrderStatus.ORDERED]: '주문 완료'
  };

  // 상태별 스타일 클래스 매핑
  export const statusClasses: Record<OrderStatus, string> = {
    [OrderStatus.PENDING]: 'bg-blue-100 text-blue-800',
    [OrderStatus.PAID]: 'bg-green-100 text-green-800',
    [OrderStatus.FAILED]: 'bg-red-100 text-red-800',
    [OrderStatus.CANCELLED]: 'bg-gray-100 text-gray-800',
    [OrderStatus.CANCELING]: 'bg-orange-100 text-orange-800',
    [OrderStatus.ORDERED]: 'bg-purple-100 text-purple-800'
  };

  // getDescription 메서드
  export function getDescription(status: OrderStatus): string {
    return descriptions[status] || status;
  }

  // getStatusClass 메서드
  export function getStatusClass(status: OrderStatus): string {
    return statusClasses[status] || 'bg-gray-100 text-gray-800';
  }

  // entries 메서드
  export function entries(): Array<{ code: OrderStatus; description: string }> {
    return Object.values(OrderStatus)
      .filter(value => typeof value === 'string')
      .map(code => ({
        code: code as OrderStatus,
        description: descriptions[code as OrderStatus] || code as string
      }));
  }

  // values 메서드
  export function values(): OrderStatus[] {
    return Object.values(OrderStatus)
      .filter(value => typeof value === 'string') as OrderStatus[];
  }
}
```

## Enum 확장 방법

새로운 속성이나 메서드가 필요할 때 Enum을 확장하는 방법을 설명합니다.

### 새로운 속성 추가하기

```typescript
// MyStatus Enum
export enum MyStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE'
}

// MyStatus Namespace
export namespace MyStatus {
  // 기본 설명 매핑
  export const descriptions: Record<MyStatus, string> = {
    [MyStatus.ACTIVE]: '활성화',
    [MyStatus.INACTIVE]: '비활성화'
  };
  
  // 새로운 속성: 아이콘 매핑
  export const icons: Record<MyStatus, string> = {
    [MyStatus.ACTIVE]: 'check-circle',
    [MyStatus.INACTIVE]: 'x-circle'
  };
  
  // 기본 메서드
  export function getDescription(status: MyStatus): string {
    return descriptions[status] || status;
  }
  
  // 새로운 메서드: 아이콘 가져오기
  export function getIcon(status: MyStatus): string {
    return icons[status] || 'question-mark';
  }
  
  // entries 메서드 (확장)
  export function entries(): Array<{ code: MyStatus; description: string; icon: string }> {
    return Object.values(MyStatus)
      .filter(value => typeof value === 'string')
      .map(code => ({
        code: code as MyStatus,
        description: descriptions[code as MyStatus] || code as string,
        icon: icons[code as MyStatus] || 'question-mark'
      }));
  }
}
```

### 주의사항

1. Enum 값은 가능한 변경되지 않도록 유지하세요. 백엔드 API와 매핑되는 경우가 많습니다.
2. 모든 Enum 케이스에 대한 매핑을 완전하게 제공하세요. 누락된 케이스가 없어야 합니다.
3. 기본 폴백 값을 제공하여 예외 상황에 대비하세요.
4. UI와 관련된 매핑(색상, 스타일 등)은 가능한 UI 계층과 분리하세요.
5. 열거형 이름과 네임스페이스 이름이 동일하므로 이름 충돌에 주의하세요.
