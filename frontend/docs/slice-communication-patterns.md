# 슬라이스 간 통신 패턴 가이드

## 목차

1. [개요](#개요)
2. [슬라이스 간 통신의 기본 원칙](#슬라이스-간-통신의-기본-원칙)
3. [허용되는 통신 패턴](#허용되는-통신-패턴)
4. [통신 방법](#통신-방법)
5. [어댑터 패턴](#어댑터-패턴)
6. [이벤트 버스 패턴](#이벤트-버스-패턴)
7. [공유 스토어 패턴](#공유-스토어-패턴)
8. [통신 패턴 결정 가이드](#통신-패턴-결정-가이드)
9. [실제 구현 예시](#실제-구현-예시)

## 개요

FSD 아키텍처에서 슬라이스 간 통신은 중요한 설계 고려사항입니다. 이 문서는 슬라이스(도메인) 간 효과적이고 안전한 통신 방법에 대한 가이드라인을 제공합니다.

슬라이스 간 통신은 다음과 같은 상황에서 필요합니다:
- 한 슬라이스의 기능이 다른 슬라이스의 데이터나 기능을 필요로 할 때
- 여러 슬라이스에 영향을 미치는 사용자 작업을 처리할 때
- 슬라이스 간 데이터 일관성을 유지해야 할 때

## 슬라이스 간 통신의 기본 원칙

슬라이스 간 통신에서 반드시 지켜야 할 원칙들:

1. **단방향 의존성**: 상위 레이어는 하위 레이어에만 의존할 수 있습니다.
2. **슬라이스 독립성**: 동일 레이어의 다른 슬라이스에 직접 의존해서는 안 됩니다.
3. **명시적 공개 API**: 슬라이스 간 통신은 명시적 공개 API를 통해서만 이루어져야 합니다.
4. **결합도 최소화**: 슬라이스 간 결합도는 가능한 낮게 유지해야 합니다.
5. **내부 구현 은닉**: 슬라이스 내부 구현 세부사항은 외부에 노출되지 않아야 합니다.

## 허용되는 통신 패턴

FSD 아키텍처에서 허용되는 통신 패턴:

1. **상위 레이어에서 하위 레이어로의 직접 통신**:
   - 상위 레이어는 하위 레이어의 공개 API를 직접 사용할 수 있습니다.
   - 예: `features/productManagement`는 `entities/product`의 공개 API를 직접 사용 가능

2. **동일 레이어 내 슬라이스 간 간접 통신**:
   - 동일 레이어의 다른 슬라이스와 통신해야 할 경우, 다음 방법 중 하나를 사용:
     - 상위 레이어에서 조정
     - 이벤트 버스를 통한 통신
     - 공유 스토어를 통한 상태 공유

3. **하위 레이어에서 상위 레이어로의 통신**:
   - 직접 참조는 금지됩니다.
   - 이벤트 버스, 콜백, 또는 다른 간접 메커니즘을 사용해야 합니다.

## 통신 방법

### 1. 상위 레이어에서의 조정

가장 권장되는 방법은 상위 레이어에서 하위 레이어의 슬라이스 간 통신을 조정하는 것입니다:

```vue
<!-- pages/products/ui/ProductManagementPage.vue -->
<template>
  <div>
    <!-- ProductForm은 entities/product의 API를 사용 -->
    <ProductForm 
      @submit="handleProductSubmit" 
    />
    
    <!-- StockManagement는 entities/product의 다른 API를 사용 -->
    <StockManagement 
      :product-id="selectedProductId" 
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { ProductForm } from '@features/productManagement';
import { StockManagement } from '@features/stockManagement';
import { productRepository } from '@entities/product';

const selectedProductId = ref<number | null>(null);

const handleProductSubmit = async (productData) => {
  // 상품 생성/수정 후 재고 관리 컴포넌트 업데이트
  const product = await productRepository.createProduct(productData);
  selectedProductId.value = product.id;
};
</script>
```

### 2. 공개 API를 통한 접근

모든 슬라이스는 `index.ts` 파일을 통해 명시적인 공개 API를 제공해야 합니다:

```typescript
// entities/product/index.ts
export type { Product, ProductStock } from './model/types';
export { ProductCategory, ProductStatus } from './model/constants';
export { productRepository } from './api/repository';
export { useProductStore } from './model/productStore';
export { default as ProductCard } from './ui/ProductCard.vue';

// 내부 구현 세부사항은 export하지 않음
```

## 어댑터 패턴

슬라이스 간 간접 통신을 위해 어댑터 패턴을 사용할 수 있습니다:

```typescript
// features/cart/model/cartFeature.ts
import { Product, productRepository } from '@entities/product';
import { User, userRepository } from '@entities/user';

// 어댑터 레이어 역할을 하는 기능 모듈
export const cartFeature = {
  async addToCart(productId: number, quantity: number): Promise<void> {
    // 상품 정보 가져오기
    const product = await productRepository.getProduct(productId);
    
    // 재고 확인
    if (!product.inStock || (product.stock && product.stock.quantity < quantity)) {
      throw new Error('재고가 부족합니다.');
    }
    
    // 사용자 정보 가져오기
    const user = await userRepository.getCurrentUser();
    
    // 장바구니에 추가하는 로직
    // ...
  }
};
```

## 이벤트 버스 패턴

Vue 3에서는 `mitt`와 같은 이벤트 이미터를 사용한 이벤트 버스 패턴을 구현할 수 있습니다:

```typescript
// shared/lib/eventBus.ts
import mitt from 'mitt';

export type Events = {
  'product:stock-changed': { productId: number; quantity: number };
  'cart:item-added': { productId: number; quantity: number };
  'order:created': { orderId: number; items: Array<{ productId: number; quantity: number }> };
};

export const eventBus = mitt<Events>();
```

이벤트 발행:

```typescript
// features/cart/model/cartStore.ts
import { eventBus } from '@shared/lib/eventBus';

export const useCartStore = defineStore('cart', {
  // ...
  actions: {
    async addToCart(productId: number, quantity: number) {
      // 장바구니에 추가하는 로직
      // ...
      
      // 이벤트 발행
      eventBus.emit('cart:item-added', { productId, quantity });
    }
  }
});
```

이벤트 구독:

```typescript
// features/notification/model/notificationService.ts
import { onMounted, onUnmounted } from 'vue';
import { eventBus } from '@shared/lib/eventBus';

export function useNotificationService() {
  const handleCartItemAdded = ({ productId, quantity }) => {
    // 알림 표시 로직
    showNotification(`${quantity}개 상품이 장바구니에 추가되었습니다.`);
  };
  
  onMounted(() => {
    // 이벤트 구독
    eventBus.on('cart:item-added', handleCartItemAdded);
  });
  
  onUnmounted(() => {
    // 이벤트 구독 해제
    eventBus.off('cart:item-added', handleCartItemAdded);
  });
}
```

## 공유 스토어 패턴

Pinia를 사용하여 슬라이스 간 상태를 공유할 수 있습니다:

```typescript
// entities/product/model/productStore.ts
export const useProductStore = defineStore('product', {
  state: () => ({
    products: [] as Product[],
    // ...
  }),
  
  getters: {
    getProductById: (state) => (id: number) => 
      state.products.find(product => product.id === id),
    // ...
  },
  
  actions: {
    async fetchProducts() {
      // ...
    },
    // ...
  }
});
```

다른 슬라이스에서 스토어 사용:

```typescript
// features/cart/model/cartStore.ts
import { storeToRefs } from 'pinia';
import { useProductStore } from '@entities/product';

export const useCartStore = defineStore('cart', {
  // ...
  actions: {
    async addToCart(productId: number, quantity: number) {
      const productStore = useProductStore();
      const product = productStore.getProductById(productId);
      
      if (!product) {
        throw new Error('상품을 찾을 수 없습니다.');
      }
      
      // 장바구니에 추가하는 로직
      // ...
    }
  }
});
```

## 통신 패턴 결정 가이드

최적의 통신 패턴을 선택하기 위한 가이드:

1. **상위 레이어에서 조정이 가능한가?**
   - 가능하다면 상위 레이어에서 하위 레이어의 슬라이스를 조정하는 방식 사용

2. **단방향 데이터 흐름인가?**
   - 단방향 데이터 흐름이라면 공개 API를 통한 직접 접근 사용

3. **양방향 또는 다방향 통신이 필요한가?**
   - 이벤트 버스 패턴 고려

4. **여러 슬라이스가 동일한 상태에 접근해야 하는가?**
   - 공유 스토어 패턴 고려

5. **결합도를 최소화해야 하는가?**
   - 어댑터 패턴 또는 이벤트 버스 패턴 고려

## 실제 구현 예시

### 상품 주문 프로세스에서의 슬라이스 통신

상품 주문 프로세스는 여러 슬라이스 간 통신이 필요한 좋은 예시입니다:

```typescript
// processes/order/orderProcess.ts
import { useProductStore } from '@entities/product';
import { useCartStore } from '@entities/cart';
import { useOrderStore } from '@entities/order';
import { useUserStore } from '@entities/user';
import { eventBus } from '@shared/lib/eventBus';

export const orderProcess = {
  async createOrder(orderData) {
    // 스토어 참조
    const productStore = useProductStore();
    const cartStore = useCartStore();
    const orderStore = useOrderStore();
    const userStore = useUserStore();
    
    // 1. 장바구니 아이템 가져오기
    const cartItems = cartStore.items;
    
    // 2. 재고 확인
    for (const item of cartItems) {
      const product = productStore.getProductById(item.productId);
      if (!product || !product.inStock || product.stock.quantity < item.quantity) {
        throw new Error(`${product?.name || '상품'} 재고가 부족합니다.`);
      }
    }
    
    // 3. 사용자 정보 확인
    if (!userStore.isAuthenticated) {
      throw new Error('로그인이 필요합니다.');
    }
    
    // 4. 주문 생성
    const order = await orderStore.createOrder({
      userId: userStore.user.id,
      items: cartItems.map(item => ({
        productId: item.productId,
        quantity: item.quantity,
        price: productStore.getProductById(item.productId)?.price || 0
      })),
      shippingAddress: orderData.shippingAddress,
      // ... 기타 주문 정보
    });
    
    // 5. 재고 업데이트
    for (const item of cartItems) {
      await productStore.updateStock(item.productId, -item.quantity);
    }
    
    // 6. 장바구니 비우기
    await cartStore.clearCart();
    
    // 7. 주문 생성 이벤트 발행
    eventBus.emit('order:created', { 
      orderId: order.id, 
      items: order.items 
    });
    
    return order;
  }
};
```

### 위젯에서의 슬라이스 통신

위젯은 여러 기능과 엔티티를 조합하므로 슬라이스 간 통신이 빈번합니다:

```vue
<!-- widgets/productList/ui/ProductList.vue -->
<template>
  <div class="product-list">
    <div v-if="isLoading" class="loading-spinner" />
    
    <div v-else-if="error" class="error-message">
      {{ error }}
    </div>
    
    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <div v-for="product in products" :key="product.id" class="product-item">
        <!-- entities/product에서 제공하는 UI 컴포넌트 사용 -->
        <ProductCard :product="product">
          <template #actions>
            <!-- features/cart에서 제공하는 기능 사용 -->
            <AddToCartButton 
              :product-id="product.id" 
              @add-success="handleAddToCartSuccess(product)" 
            />
          </template>
        </ProductCard>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { storeToRefs } from 'pinia';
import { ProductCard } from '@entities/product';
import { AddToCartButton } from '@features/cart';
import { useProductStore } from '@entities/product';
import { useToast } from '@shared/ui/toast';

const productStore = useProductStore();
const toast = useToast();

// storeToRefs로 반응성 유지
const { products, isLoading, error } = storeToRefs(productStore);

onMounted(async () => {
  await productStore.fetchProducts();
});

// 이벤트 핸들러
const handleAddToCartSuccess = (product) => {
  toast.success(`${product.name}이(가) 장바구니에 추가되었습니다.`);
};
</script>
```

이러한 패턴을 따르면 슬라이스 간 통신을 효과적으로 관리하면서, 각 슬라이스의 독립성과 캡슐화를 유지할 수 있습니다.