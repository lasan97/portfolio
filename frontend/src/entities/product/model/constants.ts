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

// 2. 동일한 이름의 네임스페이스 선언 (확장)
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
  ADJUSTMENT = 'ADJUSTMENT', // 판매
  LOSS = 'LOSS'              // 구매
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

  // entries 메서드 - key-value 쌍 배열 반환
  export function entries(): Array<{ code: StockChangeReason; description: string }> {
    return Object.values(StockChangeReason)
        .filter(value => typeof value === 'string') // enum에 추가되는 숫자 값 필터링
        .map(code => ({
          code: code as StockChangeReason,
          description: descriptions[code as StockChangeReason] || code as string
        }));
  }

  // values 메서드 - 모든 enum 값 반환
  export function values(): StockChangeReason[] {
    return Object.values(StockChangeReason)
        .filter(value => typeof value === 'string') as StockChangeReason[];
  }
}

// 상품 상태 Enum
export enum ProductStatus {
  ACTIVE = 'ACTIVE',             // 판매중
  SOLD_OUT = 'SOLD_OUT',         // 품절
  DELETED = 'DELETED'            // 삭제됨
}

// 상품 상태에 대한 설명을 제공하는 네임스페이스
export namespace ProductStatus {
  export const descriptions: Record<ProductStatus, string> = {
    [ProductStatus.ACTIVE]: '판매중',
    [ProductStatus.SOLD_OUT]: '품절',
    [ProductStatus.DELETED]: '삭제됨'
  };

  export function getDescription(status: ProductStatus): string {
    return descriptions[status] || status;
  }
}
