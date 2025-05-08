# 슬라이스 간 통신 패턴 가이드

## 목차

1. [슬라이스 간 통신의 문제](#슬라이스-간-통신의-문제)
2. [권장 통신 패턴](#권장-통신-패턴)
   - [상위 레이어를 통한 조합](#상위-레이어를-통한-조합)
   - [이벤트 버스 패턴](#이벤트-버스-패턴)
   - [의존성 주입 패턴](#의존성-주입-패턴)
   - [공유 모델 패턴](#공유-모델-패턴)
3. [안티 패턴](#안티-패턴)
4. [슬라이스 간 통신 예시](#슬라이스-간-통신-예시)
   - [상품과 장바구니 간 통신](#상품과-장바구니-간-통신)
   - [사용자와 주문 간 통신](#사용자와-주문-간-통신)
5. [슬라이스 간 의존성 관리](#슬라이스-간-의존성-관리)
6. [모범 사례](#모범-사례)

## 슬라이스 간 통신의 문제

FSD 아키텍처에서는 동일 레이어 내 슬라이스 간에 직접적인 의존성이 없어야 합니다. 그러나 실제 애플리케이션에서는 다른 슬라이스의 데이터나 기능을 사용해야 하는 경우가 빈번하게 발생합니다. 예를 들어:

- 상품(product) 엔티티와 장바구니(cart) 기능 간의 상호작용
- 사용자(user) 엔티티와 주문(order) 엔티티 간의 상호작용
- 인증(auth) 기능과 프로필(profile) 기능 간의 상호작용

이러한 상황에서 슬라이스 간 통신을 어떻게 구현할지에 대한 명확한 패턴과 지침이 필요합니다.

## 권장 통신 패턴

### 상위 레이어를 통한 조합

가장 권장되는 방법은 상위 레이어에서 여러 슬라이스를 조합하는 것입니다. 이 방식은 FSD의 의존성 규칙과 완벽하게 일치합니다.

#### 원칙

- 동일 레이어 내 슬라이스 간에는 직접적으로 통신하지 않음
- 상위 레이어(pages, widgets, processes)에서 여러 슬라이스를 조합하고 필요한 데이터를 전달함

#### 예시 코드

```typescript
// pages/product-detail/ui/ProductDetailPage.vue
<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { useProductStore, type Product } from '@entities/product';
import { useCartStore } from '@entities/cart';
import { AddToCartButton } from '@features/cart';

const route = useRoute();
const productStore = useProductStore();
const cartStore = useCartStore();
const product = ref<Product | null>(null);

// 상품 정보 로드
onMounted(async () => {
  const productId = Number(route.params.id);
  product.value = await productStore.fetchProductById(productId);
});

// 장바구니에 추가 - 페이지 레이어에서 두 슬라이스 간 통신 조정
const handleAddToCart = (quantity: number) => {
  if (product.value) {
    cartStore.addToCart(product.value.id, quantity);
  }
};
</script>

<template>
  <div v-if="product">
    <h1>{{ product.name }}</h1>
    <p>{{ product.price }}</p>
    
    <!-- 상위 레이어에서 필요한 데이터 전달 -->
    <AddToCartButton
      :product-id="product.id"
      :in-stock="product.inStock"
      @add-to-cart="handleAddToCart"
    />
  </div>
</template>
```

### 이벤트 버스 패턴

이벤트 기반 아키텍처는 슬라이스 간 느슨한 결합을 구현하는 효과적인 방법입니다. 이 패턴은 슬라이스가 서로에 대해 직접적으로 알 필요 없이 이벤트를 통해 통신할 수 있게 합니다.

#### 구현 방법

1. 이벤트 버스 생성:

```typescript
// shared/lib/eventBus.ts
import mitt from 'mitt';

type EventMap = {
  'product:selected': { productId: number };
  'cart:updated': { cartItems: number };
  'auth:logged-in': { userId: number };
  'auth:logged-out': void;
  // ... 기타 이벤트 타입
};

export const eventBus = mitt<EventMap>();
```

2. 이벤트 발생 및 구독:

```typescript
// features/product-selection/model/productSelection.ts
import { eventBus } from '@shared/lib/eventBus';

export function selectProduct(productId: number) {
  // ... 비즈니스 로직 ...
  
  // 이벤트 발생
  eventBus.emit('product:selected', { productId });
}

// features/product-recommendations/model/productRecommendations.ts
import { eventBus } from '@shared/lib/eventBus';

// 이벤트 구독
eventBus.on('product:selected', ({ productId }) => {
  fetchRecommendationsForProduct(productId);
});

function fetchRecommendationsForProduct(productId: number) {
  // ... 추천 상품 가져오기 ...
}
```

#### 장점과 한계

**장점:**
- 슬라이스 간 느슨한 결합 제공
- 비동기적인 통신에 적합
- 일대다 통신 패턴 지원

**한계:**
- 이벤트 흐름 추적이 어려울 수 있음
- 과도한 사용 시 스파게티 코드 발생 가능
- 타입 안전성 확보를 위한 추가 작업 필요

### 의존성 주입 패턴

의존성 주입 패턴은 슬라이스가 직접적으로 다른 슬라이스에 의존하지 않고, 필요한 기능을 외부에서 주입받는 방식입니다.

#### 구현 방법

1. 인터페이스 정의:

```typescript
// entities/product/model/types.ts
export interface ProductService {
  getProduct(id: number): Promise<Product>;
  getProducts(): Promise<Product[]>;
}

// entities/product/api/productService.ts
import { ProductService, Product } from '../model/types';
import { apiInstance } from '@shared/api';

export class ProductServiceImpl implements ProductService {
  async getProduct(id: number): Promise<Product> {
    const response = await apiInstance.get<ProductResponse.Detail>(`/api/products/${id}`);
    return mapToProduct(response.data);
  }
  
  async getProducts(): Promise<Product[]> {
    const response = await apiInstance.get<ProductResponse.List[]>('/api/products');
    return mapToProducts(response.data);
  }
}
```

2. 의존성 주입:

```typescript
// features/product-management/model/productManagement.ts
import { ProductService } from '@entities/product';

export class ProductManagement {
  private productService: ProductService;
  
  constructor(productService: ProductService) {
    this.productService = productService;
  }
  
  async addNewProduct(productData: ProductCreateData): Promise<number> {
    // productService를 사용하여 구현
  }
}

// 사용 예시
import { ProductServiceImpl } from '@entities/product/api/productService';

const productService = new ProductServiceImpl();
const productManagement = new ProductManagement(productService);
```

#### 장점과 한계

**장점:**
- 테스트 용이성 향상
- 슬라이스 간 결합도 감소
- 구현 세부 사항 숨김

**한계:**
- 복잡한 설정 필요
- 인터페이스 설계에 추가적인 노력 필요
- Vue의 컴포지션 API와 혼합 시 복잡도 증가 가능

### 공유 모델 패턴

공통 도메인 모델이나 타입을 `shared` 레이어에 정의하여 여러 슬라이스에서 사용하는 패턴입니다.

#### 구현 방법

1. 공유 도메인 모델 정의:

```typescript
// shared/model/domain/product.ts
export interface Product {
  id: number;
  name: string;
  price: number;
  // ... 공통 속성
}

// shared/model/domain/cart.ts
import { Product } from './product';

export interface CartItem {
  product: Product;
  quantity: number;
}
```

2. 여러 슬라이스에서 공유 모델 사용:

```typescript
// entities/product/model/types.ts
import { Product as SharedProduct } from '@shared/model/domain/product';

// 엔티티 특화 속성 추가
export interface Product extends SharedProduct {
  description: string;
  thumbnailImageUrl: string;
  // ... 추가 속성
}

// entities/cart/model/types.ts
import { CartItem as SharedCartItem } from '@shared/model/domain/cart';

export interface CartItem extends SharedCartItem {
  // ... 추가 속성
}
```

#### 장점과 한계

**장점:**
- 도메인 모델의 일관성 유지
- 여러 슬라이스에서 동일한 타입 참조 가능
- 타입 변환 작업 감소

**한계:**
- 공유 모델에 대한 변경이 여러 슬라이스에 영향을 줄 수 있음
- 올바른 추상화 수준을 결정하기 어려울 수 있음
- 슬라이스 간 간접적 결합 발생 가능

## 안티 패턴

FSD 아키텍처에서 피해야 할 슬라이스 간 통신 패턴:

### 1. 직접적인 슬라이스 간 임포트

```typescript
// entities/cart/model/cartStore.ts
import { useProductStore } from '@entities/product'; // ❌ 같은 레이어의 다른 슬라이스 직접 임포트

export const useCartStore = defineStore('cart', {
  // ...
  actions: {
    async addToCart(productId: number, quantity: number) {
      const productStore = useProductStore();
      const product = await productStore.getProductById(productId);
      // ...
    }
  }
});
```

### 2. 전역 상태 남용

```typescript
// 전역 변수를 통한 통신
window.__APP_STATE__ = {
  selectedProduct: null,
  cartItems: []
};

// 전역 변수 접근
function selectProduct(product) {
  window.__APP_STATE__.selectedProduct = product;
}

function getCartItems() {
  return window.__APP_STATE__.cartItems;
}
```

### 3. 레이어 무시

```typescript
// entities/product/ui/ProductCard.vue
<script setup>
import { AddToCartButton } from '@features/cart'; // ❌ 엔티티 레이어에서 기능 레이어 직접 임포트
</script>
```

## 슬라이스 간 통신 예시

### 상품과 장바구니 간 통신

상품 정보를 장바구니에 추가하는 기능은 여러 슬라이스 간 통신이 필요한 전형적인 예시입니다.

#### 상위 레이어 조합 패턴 사용

```typescript
// features/cart/model/types.ts
export interface AddToCartPayload {
  productId: number;
  quantity: number;
}

// features/cart/ui/AddToCartButton.vue
<script setup lang="ts">
import { ref } from 'vue';

const props = defineProps<{
  productId: number;
  inStock: boolean;
}>();

const emit = defineEmits<{
  (e: 'add-to-cart', quantity: number): void;
}>();

const quantity = ref(1);

const handleAddToCart = () => {
  emit('add-to-cart', quantity.value);
};
</script>

<template>
  <div>
    <input type="number" v-model="quantity" min="1" max="99" />
    <button 
      @click="handleAddToCart" 
      :disabled="!inStock"
    >
      장바구니에 추가
    </button>
  </div>
</template>

// pages/product-detail/ui/ProductDetailPage.vue
<script setup lang="ts">
import { useProductStore } from '@entities/product';
import { useCartStore } from '@entities/cart';
import { AddToCartButton } from '@features/cart';

const productStore = useProductStore();
const cartStore = useCartStore();
const product = ref(null);

// 상품 정보 로드
onMounted(async () => {
  const productId = Number(route.params.id);
  product.value = await productStore.fetchProductById(productId);
});

// 장바구니에 추가
const handleAddToCart = (quantity: number) => {
  if (product.value) {
    cartStore.addToCart({
      productId: product.value.id,
      quantity
    });
  }
};
</script>

<template>
  <div v-if="product">
    <!-- 상품 정보 표시 -->
    
    <!-- 장바구니 추가 버튼 -->
    <AddToCartButton
      :product-id="product.id"
      :in-stock="product.inStock"
      @add-to-cart="handleAddToCart"
    />
  </div>
</template>
```

### 사용자와 주문 간 통신

사용자 정보를 주문 생성에 활용하는 예시입니다.

#### 이벤트 버스 패턴 사용

```typescript
// shared/lib/eventBus.ts
import mitt from 'mitt';

type EventMap = {
  'user:profile-updated': { userId: number };
  'order:created': { orderId: number };
  // ... 기타 이벤트
};

export const eventBus = mitt<EventMap>();

// entities/user/model/userStore.ts
import { defineStore } from 'pinia';
import { eventBus } from '@shared/lib/eventBus';

export const useUserStore = defineStore('user', {
  // ... 상태, 게터 ...
  actions: {
    async updateProfile(profileData: UserProfileData) {
      try {
        const updatedUser = await userRepository.updateProfile(profileData);
        this.user = updatedUser;
        
        // 프로필 업데이트 이벤트 발생
        eventBus.emit('user:profile-updated', { userId: updatedUser.id });
        
        return updatedUser;
      } catch (error) {
        // ... 에러 처리 ...
      }
    }
  }
});

// entities/order/model/orderProcessor.ts
import { eventBus } from '@shared/lib/eventBus';

// 사용자 프로필 업데이트 이벤트 구독
eventBus.on('user:profile-updated', async ({ userId }) => {
  // 해당 사용자의 완료되지 않은 주문이 있는지 확인
  const pendingOrders = await orderRepository.getPendingOrdersByUserId(userId);
  
  // 주문 정보 업데이트
  pendingOrders.forEach(async (order) => {
    await orderRepository.updateOrderShippingInfo(order.id);
  });
});
```

#### 의존성 주입 패턴 사용

```typescript
// processes/checkout/model/checkoutProcess.ts
import { UserService } from '@entities/user';
import { OrderService } from '@entities/order';
import { CartService } from '@entities/cart';

export class CheckoutProcess {
  constructor(
    private userService: UserService,
    private orderService: OrderService,
    private cartService: CartService
  ) {}
  
  async processCheckout(): Promise<number> {
    // 1. 현재 사용자 정보 가져오기
    const user = await this.userService.getCurrentUser();
    
    // 2. 장바구니 아이템 가져오기
    const cartItems = await this.cartService.getCartItems();
    
    // 3. 주문 생성
    const orderId = await this.orderService.createOrder({
      userId: user.id,
      items: cartItems,
      shippingAddress: user.address,
      // ... 기타 주문 정보
    });
    
    // 4. 장바구니 비우기
    await this.cartService.clearCart();
    
    return orderId;
  }
}

// processes/checkout/index.ts
import { UserServiceImpl } from '@entities/user/api/userService';
import { OrderServiceImpl } from '@entities/order/api/orderService';
import { CartServiceImpl } from '@entities/cart/api/cartService';
import { CheckoutProcess } from './model/checkoutProcess';

// 서비스 인스턴스 생성
const userService = new UserServiceImpl();
const orderService = new OrderServiceImpl();
const cartService = new CartServiceImpl();

// 체크아웃 프로세스 인스턴스 생성 및 내보내기
export const checkoutProcess = new CheckoutProcess(
  userService,
  orderService,
  cartService
);
```

## 슬라이스 간 의존성 관리

슬라이스 간 통신을 효과적으로 관리하기 위한 전략:

### 1. 공개 API 설계

각 슬라이스는 명확하게 정의된 공개 API만 노출해야 합니다:

```typescript
// entities/product/index.ts
// 도메인 모델
export type { Product, ProductStock } from './model/types';
export { ProductCategory, ProductStatus } from './model/constants';

// 서비스 인터페이스
export type { ProductService } from './model/service';

// 구현체 (필요한 경우만)
export { ProductServiceImpl } from './api/productService';

// 저장소
export { useProductStore } from './model/productStore';

// UI 컴포넌트
export { default as ProductCard } from './ui/ProductCard.vue';
```

### 2. 타입 분리

구현 타입과 공개 타입을 분리하여 슬라이스 간 결합도를 낮출 수 있습니다:

```typescript
// entities/product/model/types.ts
// 내부 구현 타입 (슬라이스 내부에서만 사용)
export interface ProductImplementation {
  id: number;
  name: string;
  price: number;
  // ... 기타 내부 속성
  _rawData: any; // API 원시 데이터
}

// 공개 타입 (다른 슬라이스에서 사용)
export interface Product {
  id: number;
  name: string;
  price: number;
  // ... 공개 속성
}

// 내부 타입에서 공개 타입으로 변환하는 유틸리티 함수
export function toPublicProduct(impl: ProductImplementation): Product {
  return {
    id: impl.id,
    name: impl.name,
    price: impl.price,
    // ... 공개 속성만 포함
  };
}
```

### 3. 의존성 방향 제어

ESLint 규칙을 사용하여 슬라이스 간 의존성 방향을 강제할 수 있습니다:

```javascript
// .eslintrc.js
module.exports = {
  // ...
  rules: {
    // eslint-plugin-boundaries 사용
    'boundaries/element-types': [
      'error',
      {
        default: 'disallow',
        rules: [
          {
            from: 'app',
            allow: ['processes', 'pages', 'widgets', 'features', 'entities', 'shared']
          },
          {
            from: 'processes',
            allow: ['pages', 'widgets', 'features', 'entities', 'shared']
          },
          {
            from: 'pages',
            allow: ['widgets', 'features', 'entities', 'shared']
          },
          {
            from: 'widgets',
            allow: ['features', 'entities', 'shared']
          },
          {
            from: 'features',
            allow: ['entities', 'shared']
          },
          {
            from: 'entities',
            allow: ['shared']
          },
          {
            from: 'shared',
            allow: ['shared']
          }
        ]
      }
    ],
    'boundaries/entry-point': [
      'error',
      {
        default: 'disallow',
        rules: [
          {
            target: ['app', 'processes', 'pages', 'widgets', 'features', 'entities', 'shared'],
            allow: 'index.ts'
          }
        ]
      }
    ]
  }
};
```

## 모범 사례

슬라이스 간 통신에 대한 모범 사례:

### 1. 명확한 통신 방식 선택

- **간단한 데이터 전달**: Props와 이벤트 (컴포넌트 통신)
- **복잡한 비즈니스 로직**: 상위 레이어에서 조합
- **비동기 이벤트 처리**: 이벤트 버스
- **재사용 가능한 도메인 로직**: 의존성 주입

### 2. 명시적 의존성 선언

슬라이스가 의존하는 다른 슬라이스의 기능이나 데이터를 명시적으로 선언하세요:

```typescript
// features/cart/model/cartFeature.ts
export interface CartFeatureDependencies {
  getProduct: (id: number) => Promise<Product>;
  getCurrentUser: () => Promise<User>;
}

export function createCartFeature(deps: CartFeatureDependencies) {
  return {
    addToCart: async (productId: number, quantity: number) => {
      const product = await deps.getProduct(productId);
      const user = await deps.getCurrentUser();
      // ... 장바구니 추가 로직
    }
  };
}

// 사용 예시
import { useProductStore } from '@entities/product';
import { useUserStore } from '@entities/user';

const productStore = useProductStore();
const userStore = useUserStore();

const cartFeature = createCartFeature({
  getProduct: productStore.getProductById,
  getCurrentUser: userStore.getCurrentUser
});
```

### 3. 단일 출처 원칙

데이터는 항상 하나의 출처(source of truth)에서만 관리하고, 다른 슬라이스에서는 해당 출처에서 데이터를 가져와야 합니다:

```typescript
// entities/product/model/productStore.ts (출처)
export const useProductStore = defineStore('product', {
  // ... 상태, 게터, 액션
});

// features/cart/model/cartStore.ts
export const useCartStore = defineStore('cart', {
  state: () => ({
    items: [] as CartItem[]
  }),
  
  actions: {
    async addToCart(productId: number, quantity: number) {
      // 상품 ID만 저장하고, 필요할 때 productStore에서 데이터 가져오기
      this.items.push({
        productId,
        quantity
      });
    }
  }
});

// 데이터 조합은 상위 레이어에서 수행
// widgets/cart-widget/ui/CartWidget.vue
<script setup>
import { computed } from 'vue';
import { useCartStore } from '@entities/cart';
import { useProductStore } from '@entities/product';

const cartStore = useCartStore();
const productStore = useProductStore();

// 카트 아이템과 상품 정보 조합
const cartItemsWithProducts = computed(() => {
  return cartStore.items.map(item => ({
    ...item,
    product: productStore.getProductById(item.productId)
  }));
});
</script>
```

### 4. 캐싱 전략

자주 사용되는 데이터는 캐싱하여 불필요한 API 호출을 방지합니다:

```typescript
// entities/product/model/productStore.ts
export const useProductStore = defineStore('product', {
  state: () => ({
    products: [] as Product[],
    productMap: new Map<number, Product>(),
    lastFetchTime: 0
  }),
  
  actions: {
    async getProductById(id: number): Promise<Product> {
      // 캐시에 있으면 캐시된 데이터 반환
      if (this.productMap.has(id)) {
        return this.productMap.get(id)!;
      }
      
      // 캐시에 없으면 API 요청
      const product = await productRepository.getProduct(id);
      
      // 캐시에 저장
      this.productMap.set(id, product);
      
      return product;
    }
  }
});
```

효과적인 슬라이스 간 통신 패턴을 적용하면 FSD 아키텍처의 이점을 최대한 활용하면서도 실제 애플리케이션의 요구사항을 충족시킬 수 있습니다. 특히 상위 레이어를 통한 조합 패턴은 의존성 규칙을 지키면서도 유연한 통신을 가능하게 합니다.
