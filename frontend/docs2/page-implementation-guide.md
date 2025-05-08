# 실제 화면 구현 및 주의사항 가이드

이 문서는 Portfolio 프로젝트에서 실제 화면 구현 방법과 개발 시 주의해야 할 사항에 대한 가이드를 제공합니다. 실제 프로젝트 코드를 기반으로 작성되었습니다.

## 목차

1. [페이지 구현 패턴](#페이지-구현-패턴)
2. [상태 관리 통합](#상태-관리-통합)
3. [컴포넌트 간 통신](#컴포넌트-간-통신)
4. [비동기 데이터 처리](#비동기-데이터-처리)
5. [UI/UX 개선 기법](#uiux-개선-기법)
6. [개발 시 주의사항](#개발-시-주의사항)
   - [SSR 관련 주의사항](#ssr-관련-주의사항)
   - [타입 안전성](#타입-안전성)
   - [성능 최적화](#성능-최적화)
   - [브라우저 호환성](#브라우저-호환성)
   - [코드 품질](#코드-품질)

## 페이지 구현 패턴

### 페이지 구성 요소

페이지 컴포넌트는 다음과 같은 요소로 구성됩니다:

1. **레이아웃**: 페이지의 전체 구조를 정의합니다.
2. **데이터 로딩**: 페이지에 필요한 데이터를 로드합니다.
3. **에러 처리**: 로딩 실패 시 에러 메시지를 표시합니다.
4. **상호작용 핸들링**: 사용자 인터랙션을 처리합니다.

### 표준 페이지 구조

```vue
<template>
  <div class="page-container">
    <!-- 로딩 상태 표시 -->
    <div v-if="loading" class="loading-indicator">
      <!-- 로딩 스피너 또는 스켈레톤 UI -->
    </div>
    
    <!-- 에러 처리 -->
    <div v-else-if="error" class="error-message">
      {{ error }}
      <button @click="handleRetry">다시 시도</button>
    </div>
    
    <!-- 본문 컨텐츠 -->
    <template v-else>
      <!-- 페이지 타이틀 -->
      <h1 class="page-title">{{ pageTitle }}</h1>
      
      <!-- 컨텐츠 영역 -->
      <div class="page-content">
        <!-- 페이지별 컨텐츠 -->
      </div>
      
      <!-- 네비게이션 또는 액션 영역 -->
      <div class="page-actions">
        <!-- 버튼, 링크 등 -->
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';

// 라우트 및 라우터
const route = useRoute();
const router = useRouter();

// 상태
const loading = ref(true);
const error = ref<string | null>(null);
const pageTitle = ref('페이지 제목');

// 데이터 로딩 함수
const loadData = async () => {
  loading.value = true;
  error.value = null;
  
  try {
    // 필요한 데이터 로드
    // 예: await someStore.fetchData(route.params.id);
  } catch (err: any) {
    error.value = err.message || '데이터를 불러오는 중 오류가 발생했습니다.';
  } finally {
    loading.value = false;
  }
};

// 재시도 핸들러
const handleRetry = () => {
  loadData();
};

// 컴포넌트 마운트 시 데이터 로드
onMounted(() => {
  loadData();
});
</script>
```

### 실제 구현 예시: 제품 페이지

프로젝트에서 실제 구현된 제품 목록 페이지는 아래와 같은 구조를 가지고 있습니다:

```vue
<!-- pages/products/ui/ProductsPage.vue (약식) -->
<template>
  <div class="container mx-auto px-4 py-8">
    <h1 class="text-3xl font-bold mb-8">상품 목록</h1>
    
    <!-- 로딩 상태 -->
    <div v-if="loading" class="flex justify-center py-12">
      <div class="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-indigo-600"></div>
    </div>
    
    <!-- 에러 상태 -->
    <div v-else-if="error" class="bg-red-100 border border-red-200 text-red-700 px-6 py-4 rounded-lg shadow-sm mb-6">
      {{ error }}
      <button @click="fetchProducts" class="mt-2 text-indigo-600 hover:underline">다시 시도</button>
    </div>
    
    <!-- 상품 목록 -->
    <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
      <ProductCard 
        v-for="product in products" 
        :key="product.id" 
        :product="product"
        @select="navigateToProduct"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ProductCard } from '@entities/product';
import { useProductStore } from '@entities/product';
import { storeToRefs } from 'pinia';

const router = useRouter();
const productStore = useProductStore();

// storeToRefs를 사용하여 반응성 유지
const { products, isLoading: loading, error } = storeToRefs(productStore);

// 데이터 로드
const fetchProducts = async () => {
  await productStore.fetchProducts();
};

// 상품 선택 핸들러
const navigateToProduct = (productId: number) => {
  router.push(`/products/${productId}`);
};

// 컴포넌트 마운트 시 데이터 로드
onMounted(() => {
  if (products.value.length === 0) {
    fetchProducts();
  }
});
</script>
```

## 상태 관리 통합

### 페이지에서 스토어 사용 패턴

페이지 컴포넌트에서는 여러 스토어를 조합하여 사용하는 경우가 많습니다:

```typescript
// 예: 주문 상세 페이지
import { storeToRefs } from 'pinia';
import { useOrderStore } from '@entities/order';
import { useProductStore } from '@entities/product';
import { useUserStore } from '@entities/user';

const orderStore = useOrderStore();
const productStore = useProductStore();
const userStore = useUserStore();

// storeToRefs를 사용하여 반응성 유지
const { currentOrder, isLoading: orderLoading, error: orderError } = storeToRefs(orderStore);
const { products } = storeToRefs(productStore);

// 주문 정보 로드
const loadOrderDetails = async (orderId: string) => {
  try {
    // 1. 주문 정보 로드
    const order = await orderStore.fetchOrderById(orderId);
    
    // 2. 주문에 포함된 상품 정보 로드
    if (order.items) {
      const productIds = order.items.map(item => item.productId);
      await Promise.all(productIds.map(id => productStore.fetchProductById(id)));
    }
    
    // 3. 사용자 정보가 없는 경우 로드
    if (!userStore.currentUser) {
      await userStore.fetchCurrentUser();
    }
  } catch (error) {
    // 오류 처리는 각 스토어에서 담당
  }
};
```

### 주의사항: 상태 일관성 유지

상태 관리에서 중요한 점은 데이터 일관성을 유지하는 것입니다. 현재 프로젝트에서는 각 스토어가 자체 로딩 및 오류 상태를 관리하며, 필요에 따라 이를 조합해 사용합니다.

```typescript
// 예: 장바구니 페이지에서 여러 스토어의 상태 조합
import { computed } from 'vue';
import { storeToRefs } from 'pinia';
import { useCartStore } from '@entities/cart';
import { useProductStore } from '@entities/product';

const cartStore = useCartStore();
const productStore = useProductStore();

// 각 스토어의 상태 참조
const { items: cartItems, isLoading: cartLoading } = storeToRefs(cartStore);
const { products, isLoading: productsLoading } = storeToRefs(productStore);

// 통합 로딩 상태
const isLoading = computed(() => cartLoading.value || productsLoading.value);

// 장바구니 아이템에 상품 정보를 연결한 확장 데이터
const cartItemsWithProducts = computed(() => {
  return cartItems.value.map(item => {
    const product = products.value.find(p => p.id === item.productId);
    return {
      ...item,
      product,
    };
  });
});
```

## 컴포넌트 간 통신

### Props와 Emits

부모-자식 컴포넌트 간 통신은 props와 emits를 사용합니다:

```vue
<!-- 부모 컴포넌트 -->
<template>
  <ProductCard 
    :product="product"
    @select="handleProductSelect"
  />
</template>

<script setup>
import { ProductCard } from '@entities/product';

const handleProductSelect = (productId) => {
  // 상품 선택 처리 로직
  router.push(`/products/${productId}`);
};
</script>

<!-- 자식 컴포넌트 (ProductCard.vue) -->
<template>
  <div 
    class="product-card"
    @click="handleClick"
  >
    <!-- 제품 정보 표시 -->
  </div>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue';
import type { ProductDisplayProps } from '../model/ui-types';

// Props 정의
const props = defineProps<{
  product: ProductDisplayProps;
}>();

// Emits 정의
const emit = defineEmits<{
  (e: 'select', productId: number): void;
}>();

const handleClick = () => {
  emit('select', props.product.id);
};
</script>
```

### 제공/주입 패턴

깊은 중첩 컴포넌트 간의 통신에는 provide/inject를 사용합니다:

```typescript
// 페이지 컴포넌트
import { provide } from 'vue';
import { useCartStore } from '@entities/cart';

// 상위 컴포넌트에서 제공
const cartStore = useCartStore();

provide('cartContext', {
  addItem: (productId: number, quantity: number) => {
    return cartStore.addItem(productId, quantity);
  },
  removeItem: (itemId: number) => {
    return cartStore.removeItem(itemId);
  }
});

// 깊게 중첩된 하위 컴포넌트
import { inject } from 'vue';
import type { CartContext } from '@entities/cart';

// 하위 컴포넌트에서 주입받기
const cartContext = inject<CartContext>('cartContext');

const handleAddToCart = () => {
  if (cartContext) {
    cartContext.addItem(props.productId, 1);
  }
};
```

## 비동기 데이터 처리

### 데이터 로딩 패턴

비동기 데이터를 로드하고 표시하는 표준 패턴은 다음과 같습니다:

```vue
<template>
  <div>
    <!-- 로딩 상태 -->
    <loading-spinner v-if="isLoading" />
    
    <!-- 오류 상태 -->
    <error-message v-else-if="error" :message="error" @retry="loadData" />
    
    <!-- 데이터 표시 -->
    <template v-else-if="data">
      <!-- 데이터 기반 UI -->
    </template>
    
    <!-- 데이터 없음 -->
    <empty-state v-else />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';

// 상태
const isLoading = ref(false);
const error = ref(null);
const data = ref(null);

// 데이터 로딩 함수
const loadData = async () => {
  isLoading.value = true;
  error.value = null;
  
  try {
    // API 호출
    const result = await fetchData();
    data.value = result;
  } catch (err) {
    error.value = err.message || '데이터를 불러오는 중 오류가 발생했습니다.';
  } finally {
    isLoading.value = false;
  }
};

// 컴포넌트 마운트 시 데이터 로드
onMounted(() => {
  loadData();
});
</script>
```

### 비동기 컴포넌트

대형 컴포넌트는 비동기 로딩을 사용하여 성능을 개선합니다:

```typescript
// 라우터에서 비동기 컴포넌트 사용
import { RouteRecordRaw } from 'vue-router';

const routes: RouteRecordRaw[] = [
  {
    path: '/products',
    component: () => import('@pages/products')
  },
  {
    path: '/products/:id',
    component: () => import('@pages/products/[id]')
  }
];
```

### 낙관적 UI 업데이트

사용자 경험을 개선하기 위한 낙관적 UI 업데이트 패턴:

```typescript
// 장바구니 추가 예시
const addToCart = async (productId: number, quantity: number) => {
  const cartStore = useCartStore();
  
  // 1. 현재 상태 저장
  const previousCartItems = [...cartStore.items];
  
  try {
    // 2. API 호출
    await cartStore.addItem(productId, quantity);
    
    // 3. 성공 메시지
    showToast('장바구니에 추가되었습니다.');
  } catch (error) {
    // 4. 오류 발생 시 처리
    showToast('장바구니 추가 실패. 다시 시도해주세요.', 'error');
  }
};
```

## UI/UX 개선 기법

### TailwindCSS 사용

프로젝트는 TailwindCSS를 사용하며, 로딩 상태, 스켈레톤 UI 등을 구현할 때 유용합니다:

```vue
<!-- 로딩 스피너 -->
<div v-if="loading" class="flex justify-center py-12">
  <div class="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-indigo-600"></div>
</div>

<!-- 스켈레톤 UI -->
<div v-if="loading" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
  <div v-for="i in 4" :key="i" class="bg-white rounded-lg shadow-md p-4 animate-pulse">
    <div class="bg-gray-300 h-40 w-full rounded-md mb-4"></div>
    <div class="bg-gray-300 h-6 w-3/4 rounded mb-2"></div>
    <div class="bg-gray-300 h-4 w-1/2 rounded mb-4"></div>
    <div class="bg-gray-300 h-10 w-full rounded"></div>
  </div>
</div>
```

### 토스트 메시지

사용자 액션의 결과를 알려주는 토스트 메시지 구현:

```typescript
// shared/ui/toast 컴포넌트를 사용
import { useToast } from '@shared/ui/toast';

const toast = useToast();

// 성공 메시지 표시
toast.success('상품이 장바구니에 추가되었습니다.');

// 에러 메시지 표시
toast.error('장바구니에 추가하지 못했습니다.');

// 정보 메시지 표시
toast.info('결제 페이지로 이동합니다.');
```

## 개발 시 주의사항

### SSR 관련 주의사항

1. **브라우저 API 접근**: SSR에서는 `window`, `document` 등 브라우저 API에 직접 접근할 수 없습니다.

```typescript
// 문제가 되는 코드
const width = window.innerWidth; // SSR에서 오류 발생

// 올바른 해결책
import { ref, onMounted } from 'vue';

const width = ref(0);

// onMounted 훅은 클라이언트에서만 실행됨
onMounted(() => {
  width.value = window.innerWidth;
});
```

2. **클라이언트 전용 코드 처리**: 브라우저 환경에서만 실행되어야 하는 코드는 조건부로 처리해야 합니다.

```typescript
// entry-client.ts와 entry-server.ts 분리
// 클라이언트 전용 코드 entry-client.ts에 배치
// 서버에서도 실행되는 코드는 공통 로직으로 분리
```

3. **SSR에서 지원하지 않는 라이브러리 처리**:

```typescript
// 브라우저 환경 확인 후 처리
const isBrowser = typeof window !== 'undefined';

if (isBrowser) {
  // 브라우저 전용 코드
}
```

### 타입 안전성

1. **명시적 타입 선언**: 모든 변수, 함수 파라미터, 반환 값에 명시적인 타입을 선언합니다.

```typescript
// 모델 정의
export interface Product {
  id: number;
  name: string;
  price: number;
  description: string;
  thumbnailImageUrl?: string;
  category: ProductCategory;
  status: ProductStatus;
  inStock: boolean;
  stock?: ProductStock;
}

// UI 관련 타입 정의 (간소화된 모델)
export interface ProductDisplayProps {
  id: number;
  name: string;
  price: number;
  originalPrice: number;
  thumbnailImageUrl?: string;
  inStock: boolean;
}

// 함수 타입 선언
export function formatPrice(price: number): string {
  return `${price.toLocaleString()}원`;
}
```

2. **API 응답 타입 정의**:

```typescript
// API 응답 타입 정의
export namespace ProductResponse {
  export interface List {
    id: number;
    name: string;
    price: number;
    originalPrice: number;
    description?: string;
    thumbnailImageUrl?: string;
    category: string;
    inStock: boolean;
    status: string;
  }
  
  export interface Detail extends List {
    description: string;
    stock?: {
      id: number;
      quantity: number;
      version: number;
    };
  }
}
```

### 성능 최적화

1. **계산된 속성 활용**: 비용이 많이 드는 계산에는 `computed` 속성을 사용합니다.

```typescript
// computed 속성을 사용한 필터링
const filteredProducts = computed(() => {
  return products.value.filter(product => {
    if (selectedCategory.value && product.category !== selectedCategory.value) {
      return false;
    }
    
    if (searchQuery.value && !product.name.toLowerCase().includes(searchQuery.value.toLowerCase())) {
      return false;
    }
    
    return true;
  });
});
```

2. **이미지 최적화**: 이미지 로딩 최적화를 위한 기법을 사용합니다.

```vue
<template>
  <img 
    v-if="product.thumbnailImageUrl" 
    :src="product.thumbnailImageUrl" 
    :alt="product.name" 
    class="w-full h-48 object-cover rounded-md mb-3"
    loading="lazy"
  />
  <div v-else class="w-full h-48 bg-gray-200 rounded-md mb-3 flex items-center justify-center">
    <span class="text-gray-400">이미지 없음</span>
  </div>
</template>
```

3. **코드 스플리팅**: 라우트 기반 코드 스플리팅을 사용하여 초기 번들 크기를 줄입니다.

```typescript
// 라우트 정의에서 동적 임포트 사용
const routes = [
  {
    path: '/products',
    component: () => import('@pages/products')
  },
  {
    path: '/products/:id',
    component: () => import('@pages/products/[id]')
  }
];
```

### 브라우저 호환성

1. **반응형 디자인**: TailwindCSS를 사용한 반응형 디자인을 구현합니다.

```vue
<template>
  <!-- 화면 크기에 따라 그리드 컬럼 수 조정 -->
  <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
    <!-- 그리드 아이템 -->
  </div>
  
  <!-- 모바일에서는 숨기고 데스크톱에서만 표시 -->
  <div class="hidden md:block">
    <!-- 데스크톱 전용 콘텐츠 -->
  </div>
  
  <!-- 데스크톱에서는 숨기고 모바일에서만 표시 -->
  <div class="block md:hidden">
    <!-- 모바일 전용 콘텐츠 -->
  </div>
</template>
```

### 코드 품질

1. **컴포넌트 분리**: 각 컴포넌트는 단일 책임을 가지도록 설계합니다.

2. **일관된 네이밍**: 프로젝트 전반에 걸쳐 일관된 네이밍 규칙을 사용합니다.
   - 파일명: PascalCase (예: `ProductCard.vue`)
   - 컴포넌트명: PascalCase (예: `ProductCard`)
   - 함수명: camelCase (예: `handleClick`)
   - 변수명: camelCase (예: `productList`)
   - 상수명: UPPER_SNAKE_CASE (예: `MAX_ITEMS_PER_PAGE`)

3. **코드 재사용**: 공통 로직은 컴포지션 함수로 추출하여 재사용합니다.

```typescript
// shared/lib/hooks.ts
import { ref, onMounted, onUnmounted } from 'vue';

export function useWindowSize() {
  const width = ref(0);
  const height = ref(0);

  function updateSize() {
    width.value = window.innerWidth;
    height.value = window.innerHeight;
  }

  onMounted(() => {
    window.addEventListener('resize', updateSize);
    updateSize();
  });

  onUnmounted(() => {
    window.removeEventListener('resize', updateSize);
  });

  return { width, height };
}
```
