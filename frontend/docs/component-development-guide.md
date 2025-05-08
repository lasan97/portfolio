# 컴포넌트 개발 가이드

## 목차

1. [컴포넌트 구조 개요](#컴포넌트-구조-개요)
2. [Vue 3 컴포넌트 작성 스타일](#vue-3-컴포넌트-작성-스타일)
3. [FSD에서의 컴포넌트 배치](#fsd에서의-컴포넌트-배치)
4. [컴포넌트 설계 원칙](#컴포넌트-설계-원칙)
5. [재사용 가능한 컴포넌트 개발](#재사용-가능한-컴포넌트-개발)
6. [컴포넌트 간 통신](#컴포넌트-간-통신)
7. [UI 상태 관리](#ui-상태-관리)
8. [컴포넌트 테스트](#컴포넌트-테스트)
9. [모범 사례](#모범-사례)

## 컴포넌트 구조 개요

Vue 3 컴포넌트는 다음과 같은 구조로 구성됩니다:

1. **템플릿(Template)**: 컴포넌트의 UI 구조를 정의
2. **스크립트(Script)**: 컴포넌트의 로직을 정의
3. **스타일(Style)**: 컴포넌트의 스타일을 정의

컴포넌트 설계 시 다음 사항을 고려해야 합니다:

- **단일 책임 원칙**: 각 컴포넌트는 한 가지 책임만 가져야 함
- **재사용성**: 컴포넌트는 가능한 재사용 가능하게 설계
- **유지보수성**: 컴포넌트는 이해하기 쉽고 수정하기 쉽게 설계
- **테스트 용이성**: 컴포넌트는 쉽게 테스트할 수 있게 설계

## Vue 3 컴포넌트 작성 스타일

우리 프로젝트에서는 다음과 같은 Vue 3 컴포넌트 작성 스타일을 사용합니다:

### 1. Composition API - `<script setup>` 방식 (권장)

```vue
<template>
  <div class="product-card">
    <h2 class="product-name">{{ product.name }}</h2>
    <p class="product-price">{{ formatPrice(product.price) }}</p>
    <button @click="addToCart">장바구니에 추가</button>
  </div>
</template>

<script setup lang="ts">
import { defineProps, defineEmits, computed } from 'vue';
import type { Product } from '@entities/product';
import { formatPrice } from "@shared/lib";
// Props 정의
const props = defineProps<{
  product: Product;
}>();

// Emits 정의
const emit = defineEmits<{
  (e: 'add-to-cart', productId: number): void;
}>();

// 메서드
const addToCart = () => {
  emit('add-to-cart', props.product.id);
};
  
</script>

<style scoped>
.product-card {
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
}

.product-name {
  font-size: 18px;
  font-weight: 600;
}

.product-price {
  font-size: 16px;
  color: #666;
  margin: 8px 0;
}
</style>
```

### 2. Composition API - `defineComponent` 방식

```vue
<template>
  <div class="product-card">
    <!-- 템플릿 내용 -->
  </div>
</template>

<script lang="ts">
import { defineComponent, PropType, ref, computed } from 'vue';
import type { Product } from '@entities/product';
import { formatPrice } from "@shared/lib";

export default defineComponent({
  name: 'ProductCard',
  props: {
    product: {
      type: Object as PropType<Product>,
      required: true
    }
  },
  emits: ['add-to-cart'],
  setup(props, { emit }) {
    // 메서드
    const addToCart = () => {
      emit('add-to-cart', props.product.id);
    };
    
    return {
      addToCart,
      formatPrice
    };
  }
});
</script>

<style scoped>
/* 스타일 내용 */
</style>
```

## FSD에서의 컴포넌트 배치

FSD 아키텍처에서는 컴포넌트를 다음과 같이 배치합니다:

### 1. shared/ui

공통 UI 컴포넌트는 `shared/ui` 폴더에 배치합니다. 이 컴포넌트들은 애플리케이션 전체에서 재사용될 수 있습니다.

예시:
- `Button.vue`
- `Input.vue`
- `Modal.vue`
- `Alert.vue`

### 2. entities/{entity}/ui

엔티티와 관련된 기본 UI 컴포넌트는 `entities/{entity}/ui` 폴더에 배치합니다.

예시:
- `entities/product/ui/ProductCard.vue`
- `entities/user/ui/UserAvatar.vue`

### 3. features/{feature}/ui

특정 기능과 관련된 UI 컴포넌트는 `features/{feature}/ui` 폴더에 배치합니다.

예시:
- `features/productManagement/ui/ProductFormModal.vue`
- `features/cart/ui/CartItem.vue`

### 4. widgets/{widget}/ui

위젯 컴포넌트는 `widgets/{widget}/ui` 폴더에 배치합니다.

예시:
- `widgets/productList/ui/ProductList.vue`
- `widgets/header/ui/Header.vue`

### 5. pages/{page}/ui

페이지 컴포넌트는 `pages/{page}/ui` 폴더에 배치합니다.

예시:
- `pages/products/ui/ProductsPage.vue`
- `pages/productDetail/ui/ProductDetailPage.vue`

## 컴포넌트 설계 원칙

### 1. 단일 책임 원칙 (SRP)

각 컴포넌트는 한 가지 책임만 가져야 합니다. 컴포넌트가 너무 많은 일을 하고 있다면, 더 작은 컴포넌트로 분리하는 것이 좋습니다.

```vue
<!-- 나쁜 예: 한 컴포넌트가 너무 많은 일을 함 -->
<template>
  <div>
    <div class="search-bar"><!-- 검색 UI --></div>
    <div class="filter-panel"><!-- 필터 UI --></div>
    <div class="product-list"><!-- 상품 목록 UI --></div>
    <div class="pagination"><!-- 페이지네이션 UI --></div>
  </div>
</template>

<!-- 좋은 예: 기능별로 분리 -->
<template>
  <div>
    <search-bar @search="handleSearch" />
    <filter-panel @filter="handleFilter" />
    <product-list :products="filteredProducts" />
    <pagination
      :current-page="page"
      :total-pages="totalPages"
      @page-change="handlePageChange"
    />
  </div>
</template>
```

### 2. 속성(Props) 설계

속성은 컴포넌트의 인터페이스를 정의합니다. 다음 원칙을 따르세요:

- 명확한 타입 정의
- 필수 속성과 선택적 속성 구분
- 기본값 제공 (가능한 경우)
- 유효성 검사 추가 (필요한 경우)

```typescript
// 타입스크립트를 사용한 Props 정의
defineProps<{
  // 필수 속성
  product: Product;
  
  // 선택적 속성 (기본값 있음)
  showPrice?: boolean; // defineProps에서는 기본값 직접 지정 불가
  
  // 선택적 속성 (기본값 없음)
  customClass?: string;
}>();

// 또는 withDefaults 사용
withDefaults(defineProps<{
  product: Product;
  showPrice?: boolean;
  customClass?: string;
}>(), {
  showPrice: true
});

// defineComponent를 사용할 경우
props: {
  product: {
    type: Object as PropType<Product>,
    required: true
  },
  showPrice: {
    type: Boolean,
    default: true
  },
  customClass: {
    type: String,
    default: ''
  }
}
```

### 3. 이벤트(Emits) 설계

이벤트는 컴포넌트의 출력 인터페이스를 정의합니다. 다음 원칙을 따르세요:

- 이벤트 이름은 kebab-case 사용
- 이벤트 페이로드 명확하게 정의
- 이벤트 문서화

```typescript
// Composition API를 사용한 Emits 정의
defineEmits<{
  (e: 'add-to-cart', productId: number): void;
  (e: 'select-product', productId: number): void;
  (e: 'quantity-change', newQuantity: number): void;
}>();

// 또는 defineComponent를 사용할 경우
emits: {
  'add-to-cart': (productId: number) => typeof productId === 'number',
  'select-product': (productId: number) => typeof productId === 'number',
  'quantity-change': (newQuantity: number) => typeof newQuantity === 'number' && newQuantity > 0
}
```

### 4. 내부 상태 관리

컴포넌트 내부 상태는 다음 원칙을 따라 관리합니다:

- 로컬 상태는 `ref`나 `reactive`를 사용하여 관리
- 계산된 값은 `computed`를 사용
- 부수 효과는 `watch`나 `watchEffect`를 사용
- 라이프사이클 이벤트는 전용 훅을 사용

```vue
<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';

// 로컬 상태
const count = ref(0);
const isLoading = ref(false);

// 계산된 값
const doubledCount = computed(() => count.value * 2);

// 부수 효과
watch(count, (newValue, oldValue) => {
  console.log(`Count changed from ${oldValue} to ${newValue}`);
});

// 라이프사이클 훅
onMounted(() => {
  console.log('Component mounted');
});

// 메서드
const increment = () => {
  count.value++;
};
</script>
```

## 재사용 가능한 컴포넌트 개발

### 1. 일반적인 컴포넌트 구조

재사용 가능한 컴포넌트는 다음 구조를 따르는 것이 좋습니다:

```
ComponentName/
├── index.ts            # 컴포넌트 내보내기
├── ComponentName.vue   # 메인 컴포넌트
└── components/         # 내부 컴포넌트 (필요한 경우)
    ├── SubComponent1.vue
    └── SubComponent2.vue
```

```typescript
// index.ts
export { default } from './ComponentName.vue';
export * from './types';
```

### 2. 슬롯(Slots) 활용

슬롯을 활용하여 컴포넌트의 유연성을 높일 수 있습니다:

```vue
<template>
  <div class="card">
    <div class="card-header">
      <slot name="header">
        <!-- 기본 헤더 내용 -->
        <h3>{{ title }}</h3>
      </slot>
    </div>
    
    <div class="card-body">
      <slot>
        <!-- 기본 본문 내용 -->
        <p>{{ content }}</p>
      </slot>
    </div>
    
    <div class="card-footer">
      <slot name="footer">
        <!-- 기본 푸터 내용 -->
        <button @click="$emit('close')">닫기</button>
      </slot>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  title?: string;
  content?: string;
}>();

defineEmits<{
  (e: 'close'): void;
}>();
</script>
```

사용 예시:

```vue
<card title="알림">
  <template #header>
    <h2 class="custom-header">중요 알림</h2>
  </template>
  
  <p>이것은 중요한 알림 메시지입니다.</p>
  
  <template #footer>
    <div class="button-group">
      <button @click="confirm">확인</button>
      <button @click="cancel">취소</button>
    </div>
  </template>
</card>
```

### 3. 스코프드 슬롯(Scoped Slots) 활용

스코프드 슬롯을 사용하여 컴포넌트 내부 데이터를 슬롯에 노출할 수 있습니다:

```vue
<template>
  <div class="product-list">
    <div v-for="product in products" :key="product.id">
      <slot :product="product" :add-to-cart="() => addToCart(product.id)">
        <!-- 기본 렌더링 -->
        <div class="product-item">
          <h3>{{ product.name }}</h3>
          <p>{{ formatPrice(product.price) }}</p>
          <button @click="addToCart(product.id)">장바구니에 추가</button>
        </div>
      </slot>
    </div>
  </div>
</template>

<script setup lang="ts">
import { defineProps, defineEmits } from 'vue';
import type { Product } from '@entities/product';
import { formatPrice } from "@shared/lib";

defineProps<{
  products: Product[];
}>();

const emit = defineEmits<{
  (e: 'add-to-cart', productId: number): void;
}>();

const addToCart = (productId: number) => {
  emit('add-to-cart', productId);
};

</script>
```

사용 예시:

```vue
<product-list :products="products" @add-to-cart="handleAddToCart">
  <template #default="{ product, addToCart }">
    <div class="custom-product-card">
      <img :src="product.thumbnailImageUrl" :alt="product.name" />
      <div class="product-info">
        <h4>{{ product.name }}</h4>
        <p class="price">{{ formatPrice(product.price) }}</p>
        <button class="add-button" @click="addToCart">
          장바구니 추가
        </button>
      </div>
    </div>
  </template>
</product-list>
```

## 컴포넌트 간 통신

### 1. Props와 Events

부모-자식 컴포넌트 간 통신에는 Props와 Events를 사용합니다:

```vue
<!-- 부모 컴포넌트 -->
<template>
  <div>
    <product-card
      :product="product"
      @add-to-cart="handleAddToCart"
    />
  </div>
</template>

<!-- 자식 컴포넌트 (ProductCard.vue) -->
<template>
  <div class="product-card">
    <h3>{{ product.name }}</h3>
    <button @click="addToCart">장바구니에 추가</button>
  </div>
</template>

<script setup lang="ts">
import { defineProps, defineEmits } from 'vue';
import type { Product } from '@entities/product';

defineProps<{
  product: Product;
}>();

const emit = defineEmits<{
  (e: 'add-to-cart', productId: number): void;
}>();

const addToCart = () => {
  emit('add-to-cart', props.product.id);
};
</script>
```

### 2. Provide/Inject

계층 구조가 깊은 컴포넌트 간 통신에는 Provide/Inject를 사용합니다:

```vue
<!-- 상위 컴포넌트 -->
<script setup lang="ts">
import { provide, ref } from 'vue';
import { useTheme } from '@app/hooks';

const theme = useTheme();
const messages = ref({
  welcome: '환영합니다',
  goodbye: '안녕히 가세요'
});

// 값 제공
provide('theme', theme);
provide('messages', messages);
</script>

<!-- 하위 컴포넌트 (중간 컴포넌트들을 거치지 않고 직접 접근) -->
<script setup lang="ts">
import { inject } from 'vue';

// 값 주입
const theme = inject('theme');
const messages = inject('messages');
</script>
```

### 3. 상태 관리

컴포넌트 간 통신이 복잡해질 경우 Pinia 같은 상태 관리 라이브러리를 사용합니다:

```vue
<!-- ProductList.vue -->
<script setup lang="ts">
import { useProductStore } from '@entities/product';
import { useCartStore } from '@features/cart';

const productStore = useProductStore();
const cartStore = useCartStore();

// 상품 목록 로드
productStore.fetchProducts();

// 장바구니에 추가
const addToCart = (productId: number) => {
  const product = productStore.getProductById(productId);
  if (product) {
    cartStore.addToCart(product, 1);
  }
};
</script>

<!-- CartSummary.vue -->
<script setup lang="ts">
import { storeToRefs } from 'pinia';
import { useCartStore } from '@features/cart';

const cartStore = useCartStore();
// 반응성 유지를 위해 storeToRefs 사용
const { items, totalItems, totalPrice } = storeToRefs(cartStore);
</script>
```

## UI 상태 관리

### 1. 로컬 UI 상태

컴포넌트 내부에서만 사용되는 UI 상태는 로컬 상태로 관리합니다:

```vue
<script setup lang="ts">
import { ref } from 'vue';

// 로컬 UI 상태
const isModalOpen = ref(false);
const activeTab = ref('info');
const formData = ref({
  name: '',
  email: ''
});

// UI 상태 변경 메서드
const openModal = () => {
  isModalOpen.value = true;
};

const closeModal = () => {
  isModalOpen.value = false;
};

const setActiveTab = (tab: string) => {
  activeTab.value = tab;
};
</script>
```

### 2. 컴포저블(Composables)을 사용한 UI 상태

재사용 가능한 UI 로직은 컴포저블로 분리합니다:

```typescript
// shared/lib/hooks/useModal.ts
import { ref } from 'vue';

export function useModal() {
  const isOpen = ref(false);
  
  const open = () => {
    isOpen.value = true;
  };
  
  const close = () => {
    isOpen.value = false;
  };
  
  const toggle = () => {
    isOpen.value = !isOpen.value;
  };
  
  return {
    isOpen,
    open,
    close,
    toggle
  };
}

// 사용 예시
const { isOpen, open, close } = useModal();
```

### 3. 전역 UI 상태

여러 컴포넌트에서 공유해야 하는 UI 상태는 Pinia 스토어로 관리합니다:

```typescript
// app/store/uiStore.ts
import { defineStore } from 'pinia';

export const useUIStore = defineStore('ui', {
  state: () => ({
    theme: 'light',
    isPageLoading: false,
    toasts: [] as Toast[]
  }),
  
  actions: {
    setTheme(theme: 'light' | 'dark') {
      this.theme = theme;
      // 테마 변경 로직
    },
    
    setPageLoading(isLoading: boolean) {
      this.isPageLoading = isLoading;
    },
    
    addToast(toast: Toast) {
      this.toasts.push(toast);
      
      // 일정 시간 후 토스트 자동 제거
      setTimeout(() => {
        this.removeToast(toast.id);
      }, toast.duration || 3000);
    },
    
    removeToast(id: string) {
      const index = this.toasts.findIndex(t => t.id === id);
      if (index !== -1) {
        this.toasts.splice(index, 1);
      }
    }
  }
});

interface Toast {
  id: string;
  message: string;
  type: 'info' | 'success' | 'warning' | 'error';
  duration?: number;
}
```

## 컴포넌트 테스트

### 1. 단위 테스트

컴포넌트 단위 테스트는 다음 사항을 확인합니다:

- 속성(Props)에 따른 올바른 렌더링
- 이벤트(Events) 발생
- 사용자 상호작용

```typescript
// ProductCard.test.ts
import { mount } from '@vue/test-utils';
import ProductCard from '@entities/product/ui/ProductCard.vue';
import { createTestingPinia } from '@pinia/testing';

describe('ProductCard', () => {
  // 테스트 데이터
  const product = {
    id: 1,
    name: '테스트 상품',
    price: 10000,
    originalPrice: 12000,
    description: '테스트 설명',
    thumbnailImageUrl: 'test.jpg',
    category: 'ELECTRONICS',
    inStock: true
  };
  
  // 기본 마운트 함수
  const mountComponent = (props = {}) => {
    return mount(ProductCard, {
      props: {
        product,
        ...props
      },
      global: {
        plugins: [createTestingPinia()]
      }
    });
  };
  
  test('상품 이름과 가격이 올바르게 표시됨', () => {
    const wrapper = mountComponent();
    
    expect(wrapper.find('.product-name').text()).toBe('테스트 상품');
    expect(wrapper.find('.product-price').text()).toContain('10,000');
  });
  
  test('장바구니 버튼 클릭 시 이벤트 발생', async () => {
    const wrapper = mountComponent();
    
    await wrapper.find('.add-to-cart-button').trigger('click');
    
    expect(wrapper.emitted('add-to-cart')).toBeTruthy();
    expect(wrapper.emitted('add-to-cart')[0]).toEqual([1]); // 상품 ID가 전달됨
  });
  
  test('품절 상품은 장바구니 버튼이 비활성화됨', () => {
    const wrapper = mountComponent({
      product: { ...product, inStock: false }
    });
    
    const button = wrapper.find('.add-to-cart-button');
    expect(button.attributes('disabled')).toBeDefined();
    expect(button.text()).toBe('품절');
  });
});
```

### 2. 스냅샷 테스트

컴포넌트의 렌더링 결과가 예상대로인지 스냅샷 테스트로 확인합니다:

```typescript
test('컴포넌트 스냅샷이 일치함', () => {
  const wrapper = mountComponent();
  expect(wrapper.html()).toMatchSnapshot();
});
```

### 3. 통합 테스트

여러 컴포넌트 간 상호작용을 테스트합니다:

```typescript
// ProductDetail.test.ts
import { mount } from '@vue/test-utils';
import ProductDetailPage from '@pages/products/ui/ProductDetailPage.vue';
import { createTestingPinia } from '@pinia/testing';
import { useProductStore } from '@entities/product';
import { useCartStore } from '@features/cart';

describe('ProductDetailPage', () => {
  const mountComponent = () => {
    const pinia = createTestingPinia({
      stubActions: false
    });
    
    const wrapper = mount(ProductDetailPage, {
      global: {
        plugins: [pinia],
        stubs: {
          'router-link': true
        }
      }
    });
    
    const productStore = useProductStore(pinia);
    const cartStore = useCartStore(pinia);
    
    // 스토어 메서드 모킹
    productStore.fetchProductById = vi.fn().mockResolvedValue({
      id: 1,
      name: '테스트 상품',
      price: 10000,
      // ... 기타 속성
    });
    
    cartStore.addToCart = vi.fn().mockResolvedValue(undefined);
    
    return { wrapper, productStore, cartStore };
  };
  
  test('상품 데이터 로드 및 렌더링', async () => {
    const { wrapper, productStore } = mountComponent();
    
    // 컴포넌트 마운트 후 상품 데이터 로드 확인
    expect(productStore.fetchProductById).toHaveBeenCalled();
    
    // 비동기 업데이트 대기
    await wrapper.vm.$nextTick();
    
    // 상품 정보 렌더링 확인
    expect(wrapper.find('.product-name').text()).toBe('테스트 상품');
  });
  
  test('장바구니 추가 기능', async () => {
    const { wrapper, cartStore } = mountComponent();
    
    // 비동기 업데이트 대기
    await wrapper.vm.$nextTick();
    
    // 장바구니 버튼 클릭
    await wrapper.find('.add-to-cart-button').trigger('click');
    
    // 장바구니 추가 액션 호출 확인
    expect(cartStore.addToCart).toHaveBeenCalledWith(
      expect.objectContaining({ id: 1 }),
      1
    );
  });
});
```

## 모범 사례

### 1. 컴포넌트 크기 제한

컴포넌트가 너무 커지면 유지보수가 어려워집니다. 다음 기준으로 컴포넌트 크기를 제한하세요:

- 300줄 이하 권장
- 단일 책임 원칙 준수
- 복잡한 컴포넌트는 더 작은 컴포넌트로 분리

### 2. 명확한 네이밍 규칙

컴포넌트와 파일 이름은 명확하게 지정합니다:

- 컴포넌트 이름: PascalCase (예: `ProductCard`)
- 파일 이름: PascalCase (예: `ProductCard.vue`)
- 폴더 이름: camelCase 또는 kebab-case (예: `productList` 또는 `product-list`)

### 3. 스타일 캡슐화

컴포넌트 스타일은 캡슐화하여 전역 스타일 충돌을 방지합니다:

```vue
<style scoped>
/* 이 스타일은 컴포넌트 내부로 범위가 제한됨 */
.product-card {
  /* ... */
}
</style>

<!-- CSS 모듈을 사용할 수도 있음 -->
<style module>
.productCard {
  /* ... */
}
</style>
<template>
  <div :class="$style.productCard">
    <!-- ... -->
  </div>
</template>
```

### 4. 성능 최적화

성능을 최적화하기 위한 기법:

- **메모이제이션**: `computed`와 `v-memo` 활용
- **지연 로딩**: 필요할 때만 컴포넌트 로드
- **가상 스크롤**: 대량의 목록 표시 시 활용

```vue
<!-- v-memo를 사용한 최적화 -->
<div v-for="item in items" :key="item.id" v-memo="[item.id, item.updated]">
  <!-- 아이템이 변경될 때만 다시 렌더링 -->
  {{ item.name }}
</div>

<!-- 지연 로딩 -->
<script setup>
import { defineAsyncComponent } from 'vue';

const HeavyComponent = defineAsyncComponent(() => 
  import('./HeavyComponent.vue')
);
</script>
```

### 5. 문서화

컴포넌트 문서화를 위해 주석을 활용합니다:

```vue
<script lang="ts">
/**
 * 상품 카드 컴포넌트
 * 
 * @component ProductCard
 * @description 상품 정보를 카드 형태로 표시하는 컴포넌트
 * 
 * @example
 * <product-card
 *   :product="product"
 *   @add-to-cart="handleAddToCart"
 * />
 */
export default defineComponent({
  name: 'ProductCard',
  props: {
    /**
     * 상품 정보 객체
     */
    product: {
      type: Object as PropType<Product>,
      required: true
    }
  },
  // ...
});
</script>
```

이러한 모범 사례를 따르면 컴포넌트를 더 효과적으로 개발하고 유지보수할 수 있습니다.
