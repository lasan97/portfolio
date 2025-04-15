export interface Product {
  id: number;
  name: string;
  originalPrice: number; // 원가
  price: number; // 판매가
  description: string;
  imageUrl: string;
  category: ProductCategory;
  inStock: boolean;
}

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
    return descriptions[category];
  }

  // entries 메서드 - key-value 쌍 배열 반환
  export function entries(): Array<{ code: ProductCategory; description: string }> {
    return Object.values(ProductCategory)
        .filter(value => typeof value === 'string') // enum에 추가되는 숫자 값 필터링
        .map(code => ({
          code: code as ProductCategory,
          description: descriptions[code as ProductCategory]
        }));
  }

  // values 메서드 - 모든 enum 값 반환
  export function values(): ProductCategory[] {
    return Object.values(ProductCategory)
        .filter(value => typeof value === 'string') as ProductCategory[];
  }
}