import { defineStore } from 'pinia';
import { Product } from './types';

// 더미 상품 데이터
const dummyProducts: Product[] = [
  {
    id: 1,
    name: '프리미엄 노트북',
    originalPrice: 1499000,
    price: 1299000,
    description: '고성능 프로세서와 그래픽이 탑재된 전문가용 노트북',
    imageUrl: 'https://placehold.co/300x200',
    category: '전자제품',
    inStock: true
  },
  {
    id: 2,
    name: '인체공학 사무용 의자',
    originalPrice: 429000,
    price: 349000,
    description: '장시간 앉아도 편안한 인체공학적 설계의 사무용 의자',
    imageUrl: 'https://placehold.co/300x200',
    category: '가구',
    inStock: true
  },
  {
    id: 3,
    name: '무선 이어버드',
    originalPrice: 199000,
    price: 159000,
    description: '노이즈 캔슬링 기능이 있는 고음질 무선 이어버드',
    imageUrl: 'https://placehold.co/300x200',
    category: '전자제품',
    inStock: true
  },
  {
    id: 4,
    name: '스마트 워치',
    originalPrice: 329000,
    price: 279000,
    description: '건강 모니터링과 알림 기능이 있는 스마트 워치',
    imageUrl: 'https://placehold.co/300x200',
    category: '전자제품',
    inStock: true
  },
  {
    id: 5,
    name: '블루투스 스피커',
    originalPrice: 119000,
    price: 89000,
    description: '강력한 음향과 휴대성이 뛰어난 블루투스 스피커',
    imageUrl: 'https://placehold.co/300x200',
    category: '전자제품',
    inStock: false
  },
  {
    id: 6,
    name: '원목 식탁',
    originalPrice: 650000,
    price: 589000,
    description: '내추럴한 원목 소재의 6인용 식탁',
    imageUrl: 'https://placehold.co/300x200',
    category: '가구',
    inStock: true
  },
  {
    id: 7,
    name: '태블릿 PC',
    originalPrice: 529000,
    price: 459000,
    description: '슬림한 디자인의 고성능 태블릿 PC',
    imageUrl: 'https://placehold.co/300x200',
    category: '전자제품',
    inStock: true
  },
  {
    id: 8,
    name: '다용도 선반',
    originalPrice: 159000,
    price: 129000,
    description: '모던한 디자인의 조립식 다용도 선반',
    imageUrl: 'https://placehold.co/300x200',
    category: '가구',
    inStock: true
  },
  {
    id: 9,
    name: '로봇 청소기',
    originalPrice: 459000,
    price: 399000,
    description: '스마트 맵핑 기능이 있는 자동 로봇 청소기',
    imageUrl: 'https://placehold.co/300x200',
    category: '가전제품',
    inStock: false
  },
  {
    id: 10,
    name: '게이밍 키보드',
    originalPrice: 179000,
    price: 149000,
    description: 'RGB 백라이트와 기계식 스위치의 게이밍 키보드',
    imageUrl: 'https://placehold.co/300x200',
    category: '전자제품',
    inStock: true
  }
];

export const useProductStore = defineStore('product', {
  state: () => ({
    products: [] as Product[],
    isLoading: false,
    error: null as string | null,
  }),
  getters: {
    getProducts: (state) => state.products,
    getProductById: (state) => (id: number) => 
      state.products.find(product => product.id === id),
    getProductsByCategory: (state) => (category: string) => 
      state.products.filter(product => product.category === category),
    getInStockProducts: (state) => 
      state.products.filter(product => product.inStock),
  },
  actions: {
    // 더미 데이터 로드 (실제로는 API 호출)
    async fetchProducts() {
      this.isLoading = true;
      this.error = null;
      
      try {
        // 비동기 작업 시뮬레이션
        await new Promise(resolve => setTimeout(resolve, 500));
        
        // 더미 데이터 설정
        this.products = dummyProducts;
      } catch (err) {
        this.error = '상품을 불러오는 중 오류가 발생했습니다.';
        console.error('Error fetching products:', err);
      } finally {
        this.isLoading = false;
      }
    }
  }
});
