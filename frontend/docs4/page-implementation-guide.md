# 페이지 구현 가이드

## 목차

1. [페이지 역할과 책임](#페이지-역할과-책임)
2. [페이지 구조](#페이지-구조)
3. [페이지 구현 패턴](#페이지-구현-패턴)
4. [라우팅 통합](#라우팅-통합)
5. [데이터 페칭](#데이터-페칭)
6. [페이지 레이아웃](#페이지-레이아웃)
7. [페이지 간 통신](#페이지-간-통신)
8. [성능 최적화](#성능-최적화)
9. [모범 사례](#모범-사례)
10. [실제 구현 예시](#실제-구현-예시)

## 페이지 역할과 책임

FSD 아키텍처에서 페이지는 다음과 같은 역할과 책임을 가집니다:

1. **페이지 레이아웃 정의**: 페이지의 전체 구조와 레이아웃을 정의합니다.
2. **위젯 조합**: 여러 위젯을 조합하여 완전한 페이지를 구성합니다.
3. **라우팅 연결**: URL 경로와 페이지 컴포넌트를 연결합니다.
4. **페이지별 상태 관리**: 페이지 수준의 상태를 관리합니다.
5. **데이터 조율**: 여러 엔티티와 기능의 데이터를 조율합니다.

페이지는 직접적인 비즈니스 로직이나 API 호출을 포함하지 않아야 합니다. 대신, 엔티티, 기능, 위젯의 기능을 조합하여 사용합니다.

## 페이지 구조

FSD 아키텍처에서 페이지는 다음과 같은 구조를 가집니다:

```
pages/
├── {page-name}/
│   ├── index.ts         # 페이지 내보내기
│   ├── model/           # 페이지 모델 (필요한 경우)
│   │   ├── hooks/       # 페이지 수준 훅
│   │   └── types.ts     # 페이지 관련 타입
│   └── ui/              # 페이지 UI 컴포넌트
│       └── {Page}Page.vue # 페이지 컴포넌트
```

예를 들어, 상품 상세 페이지의 구조는 다음과 같습니다:

```
pages/
├── product-detail/
│   ├── index.ts
│   ├── model/
│   │   ├── hooks/
│   │   │   └── useProductDetail.ts
│   │   └── types.ts
│   └── ui/
│       └── ProductDetailPage.vue
```

## 페이지 구현 패턴

페이지 구현에는 다음과 같은 패턴을 사용합니다:

### 1. 컴포지션 패턴

페이지는 여러 위젯, 기능, 엔티티를 조합하여 구성합니다:

```vue
<template>
  <main class="product-detail-page">
    <!-- 네비게이션 위젯 -->
    <app-header />
    
    <!-- 페이지 내용 -->
    <div class="container mx-auto px-4 py-8">
      <div class="flex flex-col md:flex-row gap-8">
        <!-- 상품 상세 정보 위젯 -->
        <product-detail-widget
          v-if="product"
          :product="product"
          @add-to-cart="handleAddToCart"
        />
        
        <!-- 관련 상품 위젯 -->
        <related-products-widget
          v-if="product"
          :category="product.category"
          :current-product-id="product.id"
        />
      </div>
      
      <!-- 상품 리뷰 위젯 -->
      <product-reviews-widget
        v-if="product"
        :product-id="product.id"
      />
    </div>
    
    <!-- 푸터 위젯 -->
    <app-footer />
  </main>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { useProductStore, type Product } from '@entities/product';
import { useCartWithAuth } from '@features/cart';
import { AppHeader, AppFooter } from '@widgets/layout';
import { ProductDetailWidget } from '@widgets/productDetail';
import { RelatedProductsWidget } from '@widgets/relatedProducts';
import { ProductReviewsWidget } from '@widgets/productReviews';

const route = useRoute();
const productStore = useProductStore();
const { addToCart } = useCartWithAuth();

const product = ref<Product | null>(null);
const loading = ref(true);
const error = ref<string | null>(null);

// 상품 ID 파라미터 추출
const productId = computed(() => {
  const id = route.params.id;
  return typeof id === 'string' ? parseInt(id, 10) : -1;
});

// 상품 데이터 로드
onMounted(async () => {
  try {
    loading.value = true;
    error.value = null;
    
    product.value = await productStore.fetchProductById(productId.value);
  } catch (err: any) {
    error.value = err.message || '상품 정보를 불러오는 중 오류가 발생했습니다.';
    console.error('Error loading product:', err);
  } finally {
    loading.value = false;
  }
});

// 이벤트 핸들러
const handleAddToCart = async () => {
  if (product.value) {
    try {
      await addToCart(product.value, 1);
    } catch (err) {
      console.error('Failed to add to cart:', err);
    }
  }
};
</script>
```

### 2. 페이지 훅 패턴

페이지 관련 로직을 훅으로 분리하여 재사용성과 테스트 용이성을 높일 수 있습니다:

```typescript
// pages/product-detail/model/hooks/useProductDetail.ts
import { ref, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { useProductStore, type Product } from '@entities/product';
import { useCartWithAuth } from '@features/cart';

export function useProductDetail() {
  const route = useRoute();
  const productStore = useProductStore();
  const { addToCart } = useCartWithAuth();
  
  const product = ref<Product | null>(null);
  const loading = ref(true);
  const error = ref<string | null>(null);
  
  // 상품 ID 파라미터 추출
  const productId = computed(() => {
    const id = route.params.id;
    return typeof id === 'string' ? parseInt(id, 10) : -1;
  });
  
  // 상품 데이터 로드
  const fetchProduct = async () => {
    try {
      loading.value = true;
      error.value = null;
      
      product.value = await productStore.fetchProductById(productId.value);
    } catch (err: any) {
      error.value = err.message || '상품 정보를 불러오는 중 오류가 발생했습니다.';
      console.error('Error loading product:', err);
    } finally {
      loading.value = false;
    }
  };
  
  // 장바구니에 추가
  const handleAddToCart = async (quantity = 1) => {
    if (product.value) {
      try {
        await addToCart(product.value, quantity);
      } catch (err) {
        console.error('Failed to add to cart:', err);
      }
    }
  };
  
  onMounted(fetchProduct);
  
  return {
    product,
    loading,
    error,
    productId,
    fetchProduct,
    handleAddToCart
  };
}
```

페이지 컴포넌트에서 사용:

```vue
<script setup lang="ts">
import { useProductDetail } from '../model/hooks/useProductDetail';
import { AppHeader, AppFooter } from '@widgets/layout';
import { ProductDetailWidget } from '@widgets/productDetail';
import { RelatedProductsWidget } from '@widgets/relatedProducts';
import { ProductReviewsWidget } from '@widgets/productReviews';

const {
  product,
  loading,
  error,
  handleAddToCart
} = useProductDetail();
</script>
```

## 라우팅 통합

페이지는 Vue Router를 통해 URL 경로와 연결됩니다:

```typescript
// app/router/routeConfig.ts
import { RouteRecordRaw } from 'vue-router';

// 페이지 컴포넌트 가져오기
import HomePage from '@pages/home/ui/HomePage.vue';
import ProductsPage from '@pages/products/ui/ProductsPage.vue';
import ProductDetailPage from '@pages/product-detail/ui/ProductDetailPage.vue';
import CartPage from '@pages/cart/ui/CartPage.vue';
import CheckoutPage from '@pages/checkout/ui/CheckoutPage.vue';
import OrderCompletePage from '@pages/order-complete/ui/OrderCompletePage.vue';
import OrderHistoryPage from '@pages/order-history/ui/OrderHistoryPage.vue';
import LoginPage from '@pages/auth/ui/LoginPage.vue';
import OAuthCallbackPage from '@pages/auth/ui/OAuthCallbackPage.vue';
import NotFoundPage from '@pages/not-found/ui/NotFoundPage.vue';

// 라우트 설정
export const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'home',
    component: HomePage,
    meta: {
      title: '홈'
    }
  },
  {
    path: '/products',
    name: 'products',
    component: ProductsPage,
    meta: {
      title: '상품 목록'
    }
  },
  {
    path: '/products/:id',
    name: 'product-detail',
    component: ProductDetailPage,
    props: true,
    meta: {
      title: '상품 상세 정보'
    }
  },
  {
    path: '/cart',
    name: 'cart',
    component: CartPage,
    meta: {
      title: '장바구니',
      requiresAuth: true
    }
  },
  {
    path: '/checkout',
    name: 'checkout',
    component: CheckoutPage,
    meta: {
      title: '결제',
      requiresAuth: true
    }
  },
  {
    path: '/order-complete/:orderId',
    name: 'order-complete',
    component: OrderCompletePage,
    props: true,
    meta: {
      title: '주문 완료',
      requiresAuth: true
    }
  },
  {
    path: '/order-history',
    name: 'order-history',
    component: OrderHistoryPage,
    meta: {
      title: '주문 내역',
      requiresAuth: true
    }
  },
  {
    path: '/login',
    name: 'login',
    component: LoginPage,
    meta: {
      title: '로그인',
      guestOnly: true
    }
  },
  {
    path: '/oauth/callback',
    name: 'oauth-callback',
    component: OAuthCallbackPage,
    meta: {
      title: '로그인 처리 중',
      guestOnly: true
    }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: NotFoundPage,
    meta: {
      title: '페이지를 찾을 수 없습니다'
    }
  }
];
```

## 데이터 페칭

페이지에서 데이터를 로드하는 패턴:

### 1. onMounted에서 데이터 로드

```typescript
onMounted(async () => {
  try {
    loading.value = true;
    error.value = null;
    
    // 스토어를 통해 데이터 로드
    await productStore.fetchProducts();
  } catch (err: any) {
    error.value = err.message || '데이터를 불러오는 중 오류가 발생했습니다.';
    console.error('Error fetching data:', err);
  } finally {
    loading.value = false;
  }
});
```

### 2. 라우터 파라미터 변경 감지

```typescript
// URL 파라미터 변경 감지
watch(() => route.params.id, async (newId) => {
  if (newId) {
    await fetchProduct();
  }
}, { immediate: true });
```

### 3. 페이지 간 데이터 공유

페이지 간 데이터 공유가 필요한 경우, Pinia 스토어를 사용합니다:

```typescript
// 현재 페이지에서 데이터 저장
const saveDataForNextPage = () => {
  // 스토어에 데이터 저장
  checkoutStore.setOrderData(orderData.value);
  
  // 다음 페이지로 이동
  router.push('/review-order');
};

// 다음 페이지에서 데이터 로드
const loadDataFromPreviousPage = () => {
  // 스토어에서 데이터 로드
  orderData.value = checkoutStore.orderData;
  
  // 데이터가 없으면 이전 페이지로 리다이렉트
  if (!orderData.value) {
    router.replace('/checkout');
  }
};
```

## 페이지 레이아웃

페이지 레이아웃을 구성하는 패턴:

### 1. 기본 레이아웃 컴포넌트

```vue
<!-- widgets/layout/ui/DefaultLayout.vue -->
<template>
  <div class="default-layout">
    <app-header />
    
    <main class="main-content">
      <slot></slot>
    </main>
    
    <app-footer />
  </div>
</template>

<script setup lang="ts">
import AppHeader from './AppHeader.vue';
import AppFooter from './AppFooter.vue';
</script>
```

### 2. 페이지에서 레이아웃 활용

```vue
<!-- pages/products/ui/ProductsPage.vue -->
<template>
  <default-layout>
    <div class="container mx-auto px-4 py-6">
      <!-- 페이지 내용 -->
    </div>
  </default-layout>
</template>

<script setup lang="ts">
import { DefaultLayout } from '@widgets/layout';
</script>
```

### 3. 라우터 뷰를 활용한 레이아웃

```vue
<!-- app/App.vue -->
<template>
  <div id="app">
    <router-view v-slot="{ Component }">
      <component :is="Component" />
    </router-view>
  </div>
</template>
```

## 페이지 간 통신

페이지 간 통신에는 다음과 같은 방법을 사용합니다:

### 1. URL 파라미터 및 쿼리 문자열

```typescript
// 페이지 A: 데이터를 URL에 포함하여 전달
router.push({
  name: 'product-detail',
  params: { id: product.id },
  query: { referrer: 'search', keyword: searchQuery.value }
});

// 페이지 B: URL에서 데이터 추출
const productId = computed(() => {
  const id = route.params.id;
  return typeof id === 'string' ? parseInt(id, 10) : -1;
});

const referrer = computed(() => route.query.referrer as string || '');
const keyword = computed(() => route.query.keyword as string || '');
```

### 2. Pinia 스토어

```typescript
// 페이지 A: 스토어에 데이터 저장
const navigateToCheckout = () => {
  checkoutStore.setCartItems(cartItems.value);
  router.push('/checkout');
};

// 페이지 B: 스토어에서 데이터 로드
const cartItems = computed(() => checkoutStore.cartItems);
```

### 3. LocalStorage/SessionStorage

```typescript
// 페이지 A: 데이터 저장
const saveToStorage = (key: string, data: any) => {
  localStorage.setItem(key, JSON.stringify(data));
};

// 페이지 B: 데이터 로드
const loadFromStorage = (key: string) => {
  const data = localStorage.getItem(key);
  return data ? JSON.parse(data) : null;
};
```

## 성능 최적화

페이지 성능을 최적화하는 방법:

### 1. 컴포넌트 지연 로딩

```typescript
// app/router/routeConfig.ts
const ProductDetailPage = () => import('@pages/product-detail/ui/ProductDetailPage.vue');
```

### 2. 중요한 데이터 우선 로드

```typescript
onMounted(async () => {
  // 중요한 데이터 먼저 로드
  await loadCriticalData();
  
  // 비중요 데이터는 병렬로 로드
  Promise.all([
    loadNonCriticalData1(),
    loadNonCriticalData2(),
    loadNonCriticalData3()
  ]);
});
```

### 3. 이미지 최적화

```vue
<template>
  <img
    loading="lazy"
    :src="product.thumbnailImageUrl"
    :alt="product.name"
    class="product-image"
    width="300"
    height="300"
  />
</template>
```

### 4. 가상 스크롤링

```vue
<template>
  <virtual-list
    :data-key="'id'"
    :data-sources="products"
    :item-height="80"
    :visible-count="10"
  >
    <template #item="{ item: product }">
      <product-card :product="product" />
    </template>
  </virtual-list>
</template>
```

## 모범 사례

### 1. 페이지 모듈화

페이지 컴포넌트가 복잡해지면 더 작은 컴포넌트로 분리합니다:

```
pages/
├── product-detail/
│   ├── ui/
│   │   ├── ProductDetailPage.vue             # 메인 페이지 컴포넌트
│   │   ├── ProductDetailHeader.vue           # 헤더 섹션
│   │   ├── ProductDetailGallery.vue          # 갤러리 섹션
│   │   ├── ProductDetailInfo.vue             # 정보 섹션
│   │   └── ProductDetailRelated.vue          # 관련 상품 섹션
```

### 2. 페이지 상태 캐싱

자주 방문하는 페이지의 상태를 캐싱하여 사용자 경험을 향상시킵니다:

```typescript
// pages/products/model/hooks/useProductsCache.ts
import { ref, watch } from 'vue';
import { useProductStore } from '@entities/product';

export function useProductsCache() {
  const productStore = useProductStore();
  const hasLoaded = ref(false);
  
  // 데이터 로드
  const loadProducts = async () => {
    if (!hasLoaded.value || productStore.lastUpdated < Date.now() - 5 * 60 * 1000) {
      await productStore.fetchProducts();
      hasLoaded.value = true;
    }
  };
  
  // 캐시 무효화
  const invalidateCache = () => {
    hasLoaded.value = false;
  };
  
  return {
    loadProducts,
    invalidateCache,
    hasLoaded
  };
}
```

### 3. 페이지 권한 제어

페이지 접근 권한을 제어합니다:

```typescript
// app/router/index.ts
import { useAuthStore } from '@features/auth';

// 라우터 네비게이션 가드
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore();
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth);
  const guestOnly = to.matched.some(record => record.meta.guestOnly);
  
  // 인증이 필요한 페이지
  if (requiresAuth && !authStore.isAuthenticated) {
    next({
      name: 'login',
      query: { redirect: to.fullPath }
    });
    return;
  }
  
  // 비로그인 사용자만 접근 가능한 페이지
  if (guestOnly && authStore.isAuthenticated) {
    next({ name: 'home' });
    return;
  }
  
  next();
});
```

### 4. 미리 가져오기(Prefetching)

사용자가 방문할 가능성이 높은 페이지의 데이터를 미리 가져옵니다:

```typescript
// 링크에 마우스를 올렸을 때 데이터 미리 가져오기
const prefetchProductData = (productId: number) => {
  productStore.fetchProductById(productId);
};
```

```vue
<router-link 
  :to="`/products/${product.id}`"
  @mouseenter="prefetchProductData(product.id)"
>
  {{ product.name }}
</router-link>
```

## 실제 구현 예시

다음은 실제 프로젝트에서 구현된 페이지 컴포넌트 예시입니다:

### 상품 목록 페이지

```vue
<!-- pages/products/ui/ProductsPage.vue -->
<template>
  <div class="min-h-screen bg-gray-50">
    <div class="bg-white shadow">
      <div class="container mx-auto px-4 py-6">
        <div class="flex justify-between items-center">
          <div>
            <h1 class="text-3xl font-bold text-gray-800">상품 목록</h1>
            <p class="text-gray-600 mt-2">다양한 상품을 살펴보세요</p>
          </div>
          <div v-if="isAuthenticated">
            <router-link 
              to="/products/manage" 
              class="px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500"
            >
              상품 관리
            </router-link>
          </div>
        </div>
      </div>
    </div>
    
    <div class="container mx-auto px-4 py-6">
      <div class="flex flex-col md:flex-row gap-6">
        <!-- 상품 목록 영역 -->
        <div class="md:w-3/4">
          <product-list @select-product="handleProductSelect" />
        </div>
        
        <!-- 장바구니 요약 영역 -->
        <div class="md:w-1/4 mb-6">
          <div class="sticky top-6">
            <cart-summary @checkout="goToCheckout" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { defineComponent, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ProductList } from '@widgets/productList';
import { CartSummary } from '@widgets/cartSummary';
import { useAuthStore } from '@features/auth';

const router = useRouter();
const authStore = useAuthStore();

// 인증 상태 확인
const isAuthenticated = computed(() => authStore.isAuthenticated);

const handleProductSelect = (productId: number) => {
  // 상품 상세 페이지로 이동
  router.push(`/products/${productId}`);
};

const goToCheckout = () => {
  router.push('/cart');
};
</script>
```

### 상품 상세 페이지

```vue
<!-- pages/product-detail/ui/ProductDetailPage.vue -->
<template>
  <div class="product-detail-container">
    <div v-if="loading" class="flex justify-center py-12">
      <div class="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-indigo-600"></div>
    </div>
    
    <div v-else-if="error" class="bg-red-100 border border-red-200 text-red-700 px-6 py-4 rounded-lg shadow-sm mb-6">
      {{ error }}
    </div>
    
    <template v-else-if="product">
      <!-- 상품 상세 컨텐츠 -->
      <div class="bg-white rounded-xl shadow-lg overflow-hidden">
        <div class="md:flex">
          <!-- 상품 이미지 -->
          <div class="md:w-1/2 relative">
            <img 
              :src="product.thumbnailImageUrl"
              :alt="product.name" 
              class="w-full h-full object-cover object-center"
              @error="handleImageError"
            />
          </div>
          
          <!-- 상품 정보 -->
          <div class="md:w-1/2 p-8">
            <div class="uppercase tracking-wide text-sm text-indigo-600 font-semibold">
              {{ getCategoryDescription(product.category) }}
            </div>
            <h1 class="mt-2 text-3xl font-bold text-gray-900">{{ product.name }}</h1>
            
            <!-- 상품 가격 정보 -->
            <div class="mt-4 space-y-1">
              <!-- 원가 표시 (할인이 있는 경우) -->
              <div v-if="product.originalPrice > product.price" class="text-lg text-gray-500">
                <span class="line-through">{{ formatPrice(product.originalPrice) }}</span>
                <span class="ml-2 text-red-500 font-medium">
                  {{ calculateDiscountRate(product.originalPrice, product.price) }}% 할인
                </span>
              </div>
              
              <!-- 판매가 표시 -->
              <div>
                <span class="text-3xl font-bold text-gray-900">{{ formatPrice(product.price) }}</span>
              </div>
            </div>
            
            <!-- 재고 상태 -->
            <div class="mt-4">
              <span 
                :class="[
                  'inline-flex items-center px-3 py-1 rounded-full text-sm font-medium',
                  product.inStock 
                    ? 'bg-green-100 text-green-800' 
                    : 'bg-red-100 text-red-800'
                ]"
              >
                {{ product.inStock ? '재고 있음' : '품절' }}
              </span>
            </div>
            
            <!-- 설명 -->
            <div class="mt-6">
              <h3 class="text-lg font-medium text-gray-900">상품 설명</h3>
              <p class="mt-2 text-gray-600">{{ product.description }}</p>
            </div>
            
            <!-- 구매 버튼 -->
            <div class="mt-8">
              <button 
                class="w-full bg-indigo-600 text-white py-3 px-6 rounded-md font-medium hover:bg-indigo-700 transition-colors disabled:bg-gray-400 disabled:cursor-not-allowed"
                :disabled="!product.inStock"
                @click="addToCart"
              >
                {{ product.inStock ? '장바구니에 담기' : '품절' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </template>
    
    <div v-else class="text-center py-12">
      <p class="text-gray-500 text-lg">상품을 찾을 수 없습니다.</p>
    </div>
    
    <!-- 뒤로가기 버튼 -->
    <div class="mt-8">
      <button 
        @click="goBack" 
        class="inline-flex items-center px-4 py-2 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50"
      >
        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
        </svg>
        상품 목록으로 돌아가기
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useProductStore, type Product, ProductCategory } from '@entities/product';
import { useCartWithAuth } from '@features/cart';

const route = useRoute();
const router = useRouter();
const productStore = useProductStore();
const { addToCart: addProductToCart } = useCartWithAuth();

const loading = ref(true);
const error = ref<string | null>(null);
const product = ref<Product | null>(null);
const imageError = ref(false);

const productId = computed(() => {
  const id = route.params.id;
  return typeof id === 'string' ? parseInt(id, 10) : -1;
});

onMounted(async () => {
  try {
    loading.value = true;
    error.value = null;
    
    // 상품 ID로 상세 정보 조회 (API 호출)
    const foundProduct = await productStore.fetchProductById(productId.value);
    
    if (foundProduct) {
      product.value = foundProduct;
    } else {
      error.value = '상품을 찾을 수 없습니다.';
    }
  } catch (err: any) {
    error.value = err.message || '상품 정보를 불러오는 중 오류가 발생했습니다.';
  } finally {
    loading.value = false;
  }
});

// 이미지 오류 처리 함수
const handleImageError = (e: Event) => {
  imageError.value = true;
  e.stopPropagation();
};

const formatPrice = (price: number): string => {
  return new Intl.NumberFormat('ko-KR', {
    style: 'currency',
    currency: 'KRW'
  }).format(price);
};

const goBack = () => {
  router.push('/products');
};

const addToCart = async () => {
  if (product.value) {
    try {
      // useCartWithAuth를 사용하여 인증 체크와 토스트 메시지 표시
      await addProductToCart(product.value, 1);
      // 토스트 메시지는 useCartWithAuth 내부에서 처리하므로 여기서는 별도 처리 필요 없음
    } catch (error) {
      console.error('장바구니 추가 실패:', error);
    }
  }
};

const calculateDiscountRate = (originalPrice: number, currentPrice: number): number => {
  const discountRate = ((originalPrice - currentPrice) / originalPrice) * 100;
  return Math.round(discountRate);
};
</script>
```

이러한 패턴을 따르면 페이지를 더 체계적으로 구현하고 유지보수할 수 있으며, 사용자 경험을 향상시킬 수 있습니다.
