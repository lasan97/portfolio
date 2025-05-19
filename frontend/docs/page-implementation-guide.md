# 페이지 구현 가이드

## 목차

1. [개요](#개요)
2. [페이지 레이어 역할](#페이지-레이어-역할)
3. [페이지 구조](#페이지-구조)
4. [페이지 구현 원칙](#페이지-구현-원칙)
5. [라우팅 연결](#라우팅-연결)
6. [데이터 흐름](#데이터-흐름)
7. [실제 구현 예시](#실제-구현-예시)

## 개요

페이지(pages)는 FSD 아키텍처에서 사용자에게 보여지는 최종 화면을 구성하는 레이어입니다. 위젯, 기능, 엔티티 등 하위 레이어의 컴포넌트들을 조합하여 완전한 페이지를 구성합니다. 이 문서는 페이지를 효과적으로 구현하기 위한 가이드라인을 제공합니다.

## 페이지 레이어 역할

페이지 레이어의 주요 역할은 다음과 같습니다:

1. **화면 구성**: 다양한 위젯과 기능을 조합하여 완전한 화면을 구성
2. **라우팅 연결**: 애플리케이션의 라우팅 시스템과 연결
3. **레이아웃 정의**: 페이지의 전체적인 레이아웃 구조 정의
4. **데이터 흐름 조정**: 위젯과 기능 간의 데이터 흐름 조정
5. **상태 관리**: 페이지 수준의 상태 관리 (필요한 경우)

## 페이지 구조

페이지 레이어는 다음과 같은 구조로 구성됩니다:

```
pages/
├── products/
│   ├── ui/
│   │   ├── ProductsPage.vue        # 상품 목록 페이지
│   │   ├── ProductDetailPage.vue   # 상품 상세 페이지
│   │   └── ProductManagementPage.vue # 상품 관리 페이지
│   └── index.ts                    # 페이지 모듈의 공개 API
├── order/
│   ├── ui/
│   │   ├── OrderPage.vue           # 주문 페이지
│   │   ├── OrderHistoryPage.vue    # 주문 내역 페이지
│   │   └── OrderDetailPage.vue     # 주문 상세 페이지
│   └── index.ts
└── ...
```

## 페이지 구현 원칙

### 1. 컴포넌트 조합 중심

페이지는 직접적인 비즈니스 로직을 최소화하고, 하위 레이어의 컴포넌트를 조합하는 데 집중합니다:

```vue
<template>
  <div class="product-detail-page">
    <Header />
    
    <main class="container mx-auto py-6">
      <!-- 위젯 사용 -->
      <ProductDetailWidget :product-id="productId" />
      
      <!-- 기능 사용 -->
      <AddToCartFeature :product-id="productId" />
    </main>
    
    <Footer />
  </div>
</template>
```

### 2. 최소한의 로직 유지

페이지 컴포넌트는 다음과 같은 최소한의 로직만 포함해야 합니다:

- 라우팅 파라미터 처리
- 페이지 수준의 상태 관리
- 하위 컴포넌트 간 데이터 전달
- 페이지 수준의 이벤트 처리

```vue
<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { Header, Footer } from '@widgets/layout';
import { ProductDetailWidget } from '@widgets/productDetail';
import { AddToCartFeature } from '@features/cart';

const route = useRoute();
const productId = Number(route.params.id);
const isLoading = ref(false);

// 페이지 수준의 이벤트 처리
const handleAddToCartSuccess = () => {
  // 페이지 수준에서 처리할 작업
};
</script>
```

### 3. 의존성 규칙 준수

페이지는 다음 레이어에만 의존할 수 있습니다:
- widgets
- features
- entities
- shared

페이지는 다른 페이지나 processes 레이어에 의존해서는 안 됩니다.

## 라우팅 연결

페이지는 애플리케이션의 라우팅 시스템과 연결됩니다:

```typescript
// app/router/routeConfig.ts
import { ProductsPage, ProductDetailPage } from '@pages/products';
import { OrderPage, OrderHistoryPage } from '@pages/order';

export const routes = [
  {
    path: '/products',
    name: 'ProductsPage',
    component: ProductsPage
  },
  {
    path: '/products/:id',
    name: 'ProductDetailPage',
    component: ProductDetailPage
  },
  // ... 기타 라우트
];
```

## 데이터 흐름

페이지에서의 데이터 흐름은 다음과 같은 패턴을 따릅니다:

1. **데이터 가져오기**: 필요한 경우 페이지 수준에서 초기 데이터를 가져옵니다.
2. **하위 컴포넌트로 데이터 전달**: props를 통해 하위 컴포넌트로 데이터를 전달합니다.
3. **이벤트 처리**: 하위 컴포넌트에서 발생한 이벤트를 처리합니다.

```vue
<template>
  <div class="order-page">
    <OrderForm 
      :initial-data="initialFormData" 
      @submit="handleOrderSubmit" 
    />
    
    <OrderSummary 
      :items="cartItems" 
      :total="cartTotal" 
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { OrderForm } from '@features/order';
import { OrderSummary } from '@widgets/order';
import { useCartStore } from '@entities/cart';

const router = useRouter();
const cartStore = useCartStore();

const initialFormData = ref({});
const cartItems = computed(() => cartStore.items);
const cartTotal = computed(() => cartStore.total);

onMounted(async () => {
  await cartStore.fetchCartItems();
});

const handleOrderSubmit = async (orderData) => {
  try {
    // 주문 처리 로직
    const orderId = await cartStore.createOrder(orderData);
    router.push({ name: 'OrderComplete', params: { id: orderId } });
  } catch (err) {
    // 에러 처리
  }
};
</script>
```

## 실제 구현 예시

### ProductDetailPage 구현 예시

```vue
<!-- pages/products/ui/ProductDetailPage.vue -->
<template>
  <div class="product-detail-page">
    <Header />
    
    <main class="container mx-auto py-6">
      <div v-if="isLoading" class="loading-spinner" />
      
      <div v-else-if="error" class="error-message">
        {{ error }}
      </div>
      
      <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-8">
        <!-- 상품 상세 정보 위젯 -->
        <ProductDetailWidget :product="product" />
        
        <!-- 상품 구매 기능 -->
        <div class="purchase-section">
          <ProductPriceDisplay :product="product" />
          <StockDisplay :stock="product?.stock" />
          <AddToCartFeature 
            :product-id="productId" 
            @add-success="handleAddToCartSuccess" 
          />
        </div>
      </div>
      
      <!-- 연관 상품 위젯 -->
      <RelatedProductsWidget 
        :category="product?.category" 
        :exclude-product-id="productId" 
      />
    </main>
    
    <Footer />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useToast } from '@shared/ui/toast';

// 위젯 및 UI 컴포넌트
import { Header, Footer } from '@widgets/layout';
import { ProductDetailWidget, RelatedProductsWidget } from '@widgets/product';
import { ProductPriceDisplay, StockDisplay } from '@entities/product/ui';

// 기능
import { AddToCartFeature } from '@features/cart';

// 상태 관리
import { useProductStore } from '@entities/product';

const route = useRoute();
const router = useRouter();
const toast = useToast();
const productStore = useProductStore();

// 상태
const productId = Number(route.params.id);
const isLoading = ref(true);
const error = ref<string | null>(null);

// 계산된 속성
const product = computed(() => productStore.getProductById(productId));

// 라이프사이클 훅
onMounted(async () => {
  isLoading.value = true;
  error.value = null;
  
  try {
    // 상품 정보 로드
    await productStore.fetchProductById(productId);
    
    // 상품이 존재하지 않는 경우
    if (!product.value) {
      error.value = '상품을 찾을 수 없습니다.';
    }
  } catch (err: any) {
    error.value = err.message || '상품 정보를 불러오는 중 오류가 발생했습니다.';
    console.error('Error fetching product:', err);
  } finally {
    isLoading.value = false;
  }
});

// 이벤트 핸들러
const handleAddToCartSuccess = () => {
  toast.success('상품이 장바구니에 추가되었습니다.');
};
</script>
```

### OrderPage 구현 예시

```vue
<!-- pages/order/ui/OrderPage.vue -->
<template>
  <div class="order-page">
    <Header />
    
    <main class="container mx-auto py-6">
      <h1 class="text-2xl font-bold mb-6">주문하기</h1>
      
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <!-- 주문 폼 (2/3 너비) -->
        <div class="lg:col-span-2">
          <OrderForm 
            :initial-data="initialFormData" 
            @submit="handleOrderSubmit" 
          />
        </div>
        
        <!-- 주문 요약 (1/3 너비) -->
        <div>
          <OrderSummary 
            :items="cartItems" 
            :total="cartTotal" 
          />
        </div>
      </div>
    </main>
    
    <Footer />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useToast } from '@shared/ui/toast';

// 위젯 및 UI 컴포넌트
import { Header, Footer } from '@widgets/layout';
import { OrderSummary } from '@widgets/order';

// 기능
import { OrderForm } from '@features/order';

// 상태 관리
import { useCartStore } from '@entities/cart';
import { useOrderStore } from '@entities/order';
import { useUserStore } from '@entities/user';

const router = useRouter();
const toast = useToast();
const cartStore = useCartStore();
const orderStore = useOrderStore();
const userStore = useUserStore();

// 상태
const initialFormData = ref({});
const isSubmitting = ref(false);

// 계산된 속성
const cartItems = computed(() => cartStore.items);
const cartTotal = computed(() => cartStore.total);
const userAddress = computed(() => userStore.userAddress);

// 라이프사이클 훅
onMounted(async () => {
  // 장바구니가 비어있으면 장바구니 페이지로 리다이렉트
  await cartStore.fetchCartItems();
  if (cartItems.value.length === 0) {
    toast.warning('장바구니가 비어있습니다.');
    router.push({ name: 'CartPage' });
    return;
  }
  
  // 사용자 정보 로드
  if (userStore.isAuthenticated) {
    await userStore.fetchUserProfile();
    
    // 초기 폼 데이터 설정
    initialFormData.value = {
      name: userStore.user?.name || '',
      email: userStore.user?.email || '',
      address: userAddress.value || {},
      phone: userStore.user?.phone || ''
    };
  }
});

// 이벤트 핸들러
const handleOrderSubmit = async (orderData) => {
  isSubmitting.value = true;
  
  try {
    // 주문 생성
    const order = await orderStore.createOrder({
      ...orderData,
      items: cartItems.value.map(item => ({
        productId: item.product.id,
        quantity: item.quantity,
        price: item.product.price
      }))
    });
    
    // 장바구니 비우기
    await cartStore.clearCart();
    
    // 주문 완료 페이지로 이동
    router.push({ 
      name: 'OrderCompletePage', 
      params: { id: order.id } 
    });
  } catch (err: any) {
    toast.error(err.message || '주문 처리 중 오류가 발생했습니다.');
    console.error('Error creating order:', err);
  } finally {
    isSubmitting.value = false;
  }
};
</script>
```

이 가이드를 따르면 명확하고 유지보수가 용이한 페이지 구현이 가능합니다. 페이지는 하위 레이어 컴포넌트를 조합하고 라우팅 연결 및 최소한의 페이지 수준 로직만 처리해야 합니다.