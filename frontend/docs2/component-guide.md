# 컴포넌트 개발 가이드

이 문서는 Portfolio 프로젝트에서 Vue 컴포넌트를 개발할 때 따라야 할 표준과 패턴을 설명합니다. 실제 프로젝트 구현을 기반으로 작성되었습니다.

## 목차

1. [컴포넌트 구조](#컴포넌트-구조)
2. [컴포넌트 설계 원칙](#컴포넌트-설계-원칙)
3. [컴포지션 API 사용법](#컴포지션-api-사용법)
4. [Props 및 Emits](#props-및-emits)
5. [스타일 가이드](#스타일-가이드)
6. [재사용 가능한 컴포넌트](#재사용-가능한-컴포넌트)
7. [성능 최적화](#성능-최적화)
8. [접근성 고려사항](#접근성-고려사항)

## 컴포넌트 구조

Vue 컴포넌트는 다음과 같은 구조를 따라야 합니다:

```vue
<template>
  <!-- 템플릿 코드 -->
</template>

<script setup lang="ts">
// 1. 임포트
import { ref, computed, onMounted } from 'vue';
import type { ComponentType } from './types';

// 2. Props 정의
interface Props {
  propName: string;
  optionalProp?: number;
}

const props = withDefaults(defineProps<Props>(), {
  optionalProp: 0
});

// 3. Emits 정의
const emit = defineEmits<{
  (e: 'update', value: string): void;
  (e: 'submit'): void;
}>();

// 4. 반응형 상태 정의
const localState = ref('');

// 5. 계산된 속성
const computedValue = computed(() => {
  return `Computed: ${props.propName} - ${localState.value}`;
});

// 6. 메서드 및 이벤트 핸들러
const handleClick = () => {
  emit('update', localState.value);
};

// 7. 라이프사이클 훅
onMounted(() => {
  console.log('컴포넌트가 마운트되었습니다.');
});
</script>

<style scoped>
/* 스타일 정의 */
</style>
```

## 컴포넌트 설계 원칙

### 1. 단일 책임 원칙

각 컴포넌트는 하나의 책임만 가져야 합니다. 컴포넌트가 너무 복잡해지면 더 작은 컴포넌트로 분리하세요.

```vue
<!-- 잘못된 예: 너무 많은 책임을 가진 컴포넌트 -->
<template>
  <div>
    <h2>상품 목록</h2>
    <!-- 필터링 UI -->
    <div class="filters">...</div>
    
    <!-- 정렬 UI -->
    <div class="sorts">...</div>
    
    <!-- 페이지네이션 UI -->
    <div class="pagination">...</div>
    
    <!-- 상품 목록 UI -->
    <div class="product-list">...</div>
  </div>
</template>

<!-- 좋은 예: 책임을 분리한 컴포넌트 -->
<template>
  <div>
    <h2>상품 목록</h2>
    <ProductFilters @filter="handleFilter" />
    <ProductSorts @sort="handleSort" />
    <ProductList :products="filteredProducts" />
    <Pagination :total="totalPages" @change="handlePageChange" />
  </div>
</template>
```

### 2. 컴포넌트 계층 구조

컴포넌트는 FSD 아키텍처에 따라 적절한 레이어에 배치해야 합니다:

- **entities/ui**: 도메인 모델과 관련된 기본 UI 컴포넌트
  ```
  entities/product/ui/ProductCard.vue
  entities/user/ui/UserProfile.vue
  ```

- **features/ui**: 사용자 기능과 관련된 UI 컴포넌트
  ```
  features/auth/ui/LoginForm.vue
  features/cart/ui/AddToCartButton.vue
  ```

- **widgets/ui**: 여러 기능을 조합한 복합 UI 블록
  ```
  widgets/header/ui/Header.vue
  widgets/productList/ui/ProductList.vue
  ```

- **shared/ui**: 도메인에 종속되지 않는 재사용 가능한 UI 컴포넌트
  ```
  shared/ui/button/Button.vue
  shared/ui/input/Input.vue
  shared/ui/toast/Toast.vue
  ```

## 컴포지션 API 사용법

### 1. Composition API와 `<script setup>` 사용

모든 컴포넌트는 Composition API와 `<script setup>` 구문을 사용해야 합니다.

```vue
<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';

// 코드...
</script>
```

### 2. 컴포지션 함수(Composables) 활용

재사용 가능한 로직은 컴포지션 함수로 추출하세요:

```typescript
// useProductFilter.ts
import { ref, computed } from 'vue';
import type { Product } from '@entities/product';

export function useProductFilter(products: Ref<Product[]>) {
  const searchQuery = ref('');
  const selectedCategory = ref<string | null>(null);
  
  const filteredProducts = computed(() => {
    return products.value.filter(product => {
      // 검색어 필터링
      const matchesSearch = !searchQuery.value || 
        product.name.toLowerCase().includes(searchQuery.value.toLowerCase());
      
      // 카테고리 필터링
      const matchesCategory = !selectedCategory.value || 
        product.category === selectedCategory.value;
      
      return matchesSearch && matchesCategory;
    });
  });
  
  return {
    searchQuery,
    selectedCategory,
    filteredProducts
  };
}
```

사용 예:

```vue
<script setup lang="ts">
import { useProductStore } from '@entities/product';
import { storeToRefs } from 'pinia';
import { useProductFilter } from './composables/useProductFilter';

const productStore = useProductStore();
const { products } = storeToRefs(productStore);

const { searchQuery, selectedCategory, filteredProducts } = useProductFilter(products);
</script>
```

## Props 및 Emits

### 1. Props 타입 정의

Props는 항상 TypeScript 인터페이스를 사용하여 정의해야 합니다:

```vue
<script setup lang="ts">
interface Props {
  product: Product;
  showDescription?: boolean;
  maxDescriptionLength?: number;
}

// 기본값 설정
const props = withDefaults(defineProps<Props>(), {
  showDescription: false,
  maxDescriptionLength: 100
});
</script>
```

### 2. Emits 타입 정의

Emits도 항상 타입을 명시해야 합니다:

```vue
<script setup lang="ts">
const emit = defineEmits<{
  (e: 'select', productId: number): void;
  (e: 'addToCart', product: Product, quantity: number): void;
}>();

// 사용 예
const handleSelect = () => {
  emit('select', props.product.id);
};

const handleAddToCart = (quantity: number) => {
  emit('addToCart', props.product, quantity);
};
</script>
```

## 스타일 가이드

### 1. TailwindCSS 사용

이 프로젝트는 TailwindCSS를 사용합니다:

```vue
<template>
  <div class="bg-white rounded-lg shadow-md p-4 hover:shadow-lg transition-shadow">
    <img :src="product.thumbnailImageUrl" :alt="product.name" class="w-full h-48 object-cover rounded-md" />
    <h3 class="text-lg font-semibold mt-2">{{ product.name }}</h3>
    <p class="text-gray-600">{{ product.price.toLocaleString() }}원</p>
    <button class="bg-blue-500 text-white px-4 py-2 rounded mt-2 hover:bg-blue-600">
      장바구니에 추가
    </button>
  </div>
</template>
```

### 2. 스코프 스타일

Tailwind로 해결할 수 없는 스타일은 `<style scoped>`를 사용합니다:

```vue
<style scoped>
.product-card {
  transition: transform 0.2s ease;
}

.product-card:hover {
  transform: translateY(-5px);
}
</style>
```

## 재사용 가능한 컴포넌트

### 1. 기본 UI 컴포넌트

재사용 가능한 기본 UI 컴포넌트는 `shared/ui`에 배치합니다:

```vue
<!-- shared/ui/button/Button.vue -->
<template>
  <button 
    :class="[
      'px-4 py-2 rounded transition-colors', 
      variantClasses[variant],
      { 'opacity-50 cursor-not-allowed': disabled }
    ]"
    :disabled="disabled"
    @click="handleClick"
  >
    <slot></slot>
  </button>
</template>

<script setup lang="ts">
const props = withDefaults(defineProps<{
  variant?: 'primary' | 'secondary' | 'danger';
  disabled?: boolean;
}>(), {
  variant: 'primary',
  disabled: false
});

const emit = defineEmits<{
  (e: 'click', event: MouseEvent): void;
}>();

const variantClasses = {
  primary: 'bg-blue-500 text-white hover:bg-blue-600',
  secondary: 'bg-gray-200 text-gray-800 hover:bg-gray-300',
  danger: 'bg-red-500 text-white hover:bg-red-600'
};

const handleClick = (event: MouseEvent) => {
  if (!props.disabled) {
    emit('click', event);
  }
};
</script>
```

### 2. 컴포넌트 사용 예

```vue
<template>
  <div>
    <Button variant="primary" @click="handleSubmit">저장</Button>
    <Button variant="secondary" @click="handleCancel">취소</Button>
    <Button variant="danger" @click="handleDelete" :disabled="!canDelete">삭제</Button>
  </div>
</template>

<script setup lang="ts">
import { Button } from '@shared/ui';

// 메서드 정의...
</script>
```

## 성능 최적화

### 1. 계산된 속성 활용

불필요한 재계산을 방지하기 위해 계산된 속성을 사용하세요:

```vue
<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{
  products: Product[];
}>();

// 좋은 예: 계산된 속성 사용
const totalPrice = computed(() => {
  return props.products.reduce((sum, product) => sum + product.price, 0);
});

// 재고가 있는 상품만 필터링
const inStockProducts = computed(() => {
  return props.products.filter(product => product.inStock);
});
</script>
```

### 2. 메모이제이션

비싼 계산이나 렌더링의 경우 `v-memo`를 사용하세요:

```vue
<template>
  <div>
    <div v-for="product in products" :key="product.id" v-memo="[product.id, product.price]">
      <!-- 복잡한 상품 렌더링 -->
    </div>
  </div>
</template>
```

### 3. 지연 로딩

대규모 컴포넌트는 지연 로딩을 사용하세요:

```typescript
// 라우터에서 지연 로딩
const routes = [
  {
    path: '/admin/products',
    component: () => import('@pages/admin/ProductManagementPage.vue')
  }
];
```

## 접근성 고려사항

### 1. 시맨틱 HTML 사용

```vue
<template>
  <article class="product-card">
    <header>
      <h3>{{ product.name }}</h3>
    </header>
    <div class="content">
      <p>{{ product.description }}</p>
    </div>
    <footer>
      <p class="price">{{ formatPrice(product.price) }}</p>
    </footer>
  </article>
</template>
```

### 2. ARIA 속성 사용

```vue
<template>
  <div role="dialog" aria-labelledby="modal-title" aria-describedby="modal-description">
    <h2 id="modal-title">상품 상세 정보</h2>
    <p id="modal-description">이 상품에 대한 상세 정보입니다.</p>
    <!-- 내용 -->
    <button aria-label="닫기" @click="closeModal">X</button>
  </div>
</template>
```

### 3. 키보드 접근성

```vue
<template>
  <div 
    class="product-card" 
    tabindex="0"
    @click="handleSelect"
    @keydown.enter="handleSelect"
    @keydown.space.prevent="handleSelect"
  >
    <!-- 상품 카드 내용 -->
  </div>
</template>

<script setup lang="ts">
const handleSelect = () => {
  // 상품 선택 로직
};
</script>
```

### 4. 실제 프로젝트의 예시

실제 프로젝트에서 사용되는 ProductCard 컴포넌트의 예:

```vue
<!-- entities/product/ui/ProductCard.vue -->
<template>
  <div 
    class="bg-white rounded-lg shadow-md p-4 hover:shadow-lg transition-all cursor-pointer"
    @click="handleClick"
    @keydown.enter="handleClick"
    tabindex="0"
  >
    <img 
      v-if="product.thumbnailImageUrl" 
      :src="product.thumbnailImageUrl" 
      :alt="product.name" 
      class="w-full h-48 object-cover rounded-md mb-3"
    />
    <div v-else class="w-full h-48 bg-gray-200 rounded-md mb-3 flex items-center justify-center">
      <span class="text-gray-400">이미지 없음</span>
    </div>
    
    <h3 class="text-lg font-semibold">{{ product.name }}</h3>
    
    <div class="mt-2 flex justify-between items-center">
      <div>
        <p v-if="product.originalPrice !== product.price" class="text-sm text-gray-500 line-through">
          {{ formatPrice(product.originalPrice) }}
        </p>
        <p class="text-lg font-bold text-blue-600">
          {{ formatPrice(product.price) }}
        </p>
      </div>
      
      <span v-if="product.inStock" class="text-green-500 text-sm">
        재고 있음
      </span>
      <span v-else class="text-red-500 text-sm">
        품절
      </span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ProductDisplayProps } from '../model/ui-types';

interface Props {
  product: ProductDisplayProps;
}

const props = defineProps<Props>();

const emit = defineEmits<{
  (e: 'select', productId: number): void;
}>();

const formatPrice = (price: number): string => {
  return `${price.toLocaleString()}원`;
};

const handleClick = () => {
  emit('select', props.product.id);
};
</script>
```
