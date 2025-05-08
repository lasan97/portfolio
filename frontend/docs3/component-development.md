# 컴포넌트 개발 가이드

이 문서는 FSD 아키텍처에서 컴포넌트를 개발하는 방법과 각 레이어별 컴포넌트 구현 패턴에 대해 설명합니다.

## 목차

1. [컴포넌트 배치 원칙](#컴포넌트-배치-원칙)
2. [레이어별 컴포넌트 개발 가이드](#레이어별-컴포넌트-개발-가이드)
3. [컴포넌트 통신 패턴](#컴포넌트-통신-패턴)
4. [상태 관리](#상태-관리)
5. [모범 사례](#모범-사례)

## 컴포넌트 배치 원칙

컴포넌트는 그 역할과 책임에 따라 적절한 레이어에 배치해야 합니다. 다음은 컴포넌트를 배치하는 기본 원칙입니다:

### 1. 도메인 모델 중심 배치

- **entities**: 특정 도메인 모델을 표현하는 기본 컴포넌트
- **features**: 사용자 상호작용과 관련된 기능 컴포넌트
- **widgets**: 여러 기능을 조합한 복합 UI 블록
- **pages**: 라우트에 매핑되는 페이지 컴포넌트
- **shared**: 도메인에 독립적인 재사용 가능한 UI 컴포넌트

### 2. 의존성 방향 준수

컴포넌트는 FSD의 의존성 규칙을 준수해야 합니다:
- 상위 레이어 컴포넌트는 하위 레이어 컴포넌트를 사용할 수 있습니다.
- 하위 레이어 컴포넌트는 상위 레이어 컴포넌트를 사용할 수 없습니다.

## 레이어별 컴포넌트 개발 가이드

### 1. shared/ui 컴포넌트

**역할**: 도메인에 독립적인 기본 UI 컴포넌트

**특징**:
- 도메인 로직이 없음
- 순수하게 UI 표현에 집중
- 높은 재사용성

**명명 규칙**: 일반적인 UI 요소 이름 (Button, Input, Modal 등)

**예시**:
```vue
<!-- shared/ui/Button.vue -->
<template>
  <button 
    :class="[
      'btn', 
      `btn-${variant}`,
      { 'btn-block': block }
    ]"
    :disabled="disabled"
    @click="$emit('click', $event)"
  >
    <slot></slot>
  </button>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({
  name: 'Button',
  props: {
    variant: {
      type: String,
      default: 'primary',
      validator: (value: string) => ['primary', 'secondary', 'danger'].includes(value)
    },
    block: {
      type: Boolean,
      default: false
    },
    disabled: {
      type: Boolean,
      default: false
    }
  },
  emits: ['click']
});
</script>
```

### 2. entities/ui 컴포넌트

**역할**: 특정 도메인 모델을 표현하는 기본 컴포넌트

**특징**:
- 도메인 모델과 직접 연결
- 기본적인 표현 로직만 포함
- 사용자 상호작용은 이벤트로 위임

**명명 규칙**: `{Entity}{ComponentPurpose}` (ProductCard, UserProfile 등)

**예시**:
```vue
<!-- entities/product/ui/ProductCard.vue (간소화된 버전) -->
<template>
  <div class="product-card">
    <img :src="product.thumbnailImageUrl" :alt="product.name" />
    <h3>{{ product.name }}</h3>
    <p class="price">{{ formatPrice(product.price) }}</p>
    <span class="category">{{ getCategoryDescription(product.category) }}</span>
    
    <!-- 상호작용은 이벤트로 위임 -->
    <button @click="$emit('add-to-cart', product)">장바구니에 담기</button>
  </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue';
import { Product, ProductCategory } from '@entities/product';

export default defineComponent({
  name: 'ProductCard',
  props: {
    product: {
      type: Object as PropType<Product>,
      required: true
    }
  },
  emits: ['add-to-cart'],
  setup() {
    const formatPrice = (price: number): string => {
      return new Intl.NumberFormat('ko-KR', {
        style: 'currency',
        currency: 'KRW'
      }).format(price);
    };

    return {
      formatPrice,
      getCategoryDescription: ProductCategory.getDescription
    };
  }
});
</script>
```

### 3. features/ui 컴포넌트

**역할**: 사용자 상호작용과 관련된 기능 컴포넌트

**특징**:
- 사용자 상호작용 처리
- 비즈니스 로직 포함 또는 모델 계층 참조
- 엔티티 컴포넌트 사용

**명명 규칙**: `{Action}{EntityOrPurpose}` (LoginForm, FilterProducts 등)

**예시**:
```vue
<!-- features/productManagement/ui/ProductFormModal.vue (간소화된 버전) -->
<template>
  <Modal :visible="visible" @close="$emit('close')">
    <template #header>
      <h2>{{ isEdit ? '상품 수정' : '상품 등록' }}</h2>
    </template>
    
    <template #body>
      <form @submit.prevent="handleSubmit">
        <div class="form-group">
          <label for="name">상품명</label>
          <input id="name" v-model="form.name" required />
        </div>
        
        <div class="form-group">
          <label for="price">판매가</label>
          <input id="price" v-model.number="form.price" type="number" required />
        </div>
        
        <!-- 기타 폼 필드 -->
        
        <div class="form-actions">
          <Button type="submit" :disabled="isSubmitting">저장</Button>
          <Button variant="secondary" @click="$emit('close')">취소</Button>
        </div>
      </form>
    </template>
  </Modal>
</template>

<script lang="ts">
import { defineComponent, ref, computed } from 'vue';
import { Modal, Button } from '@shared/ui';
import { ProductCategory } from '@entities/product';
import { useProductForm } from '../model/useProductForm';

export default defineComponent({
  name: 'ProductFormModal',
  components: { Modal, Button },
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    productId: {
      type: Number,
      default: null
    }
  },
  emits: ['close', 'submit'],
  setup(props, { emit }) {
    // 모델 로직 분리
    const { form, isSubmitting, isEdit, initForm, submitForm } = useProductForm(props.productId);
    
    // 이벤트 핸들러
    const handleSubmit = async () => {
      try {
        const result = await submitForm();
        emit('submit', result);
      } catch (error) {
        // 에러 처리
      }
    };
    
    return {
      form,
      isSubmitting,
      isEdit,
      handleSubmit,
      categories: computed(() => ProductCategory.entries())
    };
  }
});
</script>
```

### 4. widgets/ui 컴포넌트

**역할**: 여러 기능을 조합한 복합 UI 블록

**특징**:
- 여러 기능과 엔티티 컴포넌트 조합
- 독립적인 UI 블록 형성
- 페이지의 일부로 사용 가능

**명명 규칙**: 목적이나 위치에 따라 명명 (Header, Footer, ProductList 등)

**예시**:
```vue
<!-- widgets/productList/ui/ProductList.vue (간소화된 버전) -->
<template>
  <div class="product-list-widget">
    <div class="filters">
      <!-- features/productFilter의 컴포넌트 사용 -->
      <ProductFilterPanel @filter="handleFilter" />
    </div>
    
    <div class="products-grid">
      <!-- entities/product의 컴포넌트 사용 -->
      <ProductCard 
        v-for="product in filteredProducts" 
        :key="product.id" 
        :product="product"
        @add-to-cart="handleAddToCart"
        @click="handleProductClick"
      />
    </div>
    
    <div class="pagination">
      <!-- shared/ui의 컴포넌트 사용 -->
      <Pagination 
        :total="totalProducts" 
        :current="currentPage"
        @change="handlePageChange" 
      />
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, computed } from 'vue';
import { Pagination } from '@shared/ui';
import { ProductCard, Product } from '@entities/product';
import { ProductFilterPanel } from '@features/productFilter';
import { useCartWithAuth } from '@features/cart';

export default defineComponent({
  name: 'ProductList',
  components: { ProductCard, ProductFilterPanel, Pagination },
  props: {
    products: {
      type: Array as PropType<Product[]>,
      required: true
    }
  },
  emits: ['product-click'],
  setup(props, { emit }) {
    const { addToCart } = useCartWithAuth();
    const currentFilter = ref({});
    const currentPage = ref(1);
    
    // 필터링된 상품 목록
    const filteredProducts = computed(() => {
      // 필터 로직 구현
      return props.products;
    });
    
    // 이벤트 핸들러
    const handleFilter = (filter) => {
      currentFilter.value = filter;
      currentPage.value = 1;
    };
    
    const handleAddToCart = async (product: Product) => {
      await addToCart(product, 1);
    };
    
    const handleProductClick = (product: Product) => {
      emit('product-click', product);
    };
    
    const handlePageChange = (page: number) => {
      currentPage.value = page;
    };
    
    return {
      filteredProducts,
      currentPage,
      totalProducts: computed(() => props.products.length),
      handleFilter,
      handleAddToCart,
      handleProductClick,
      handlePageChange
    };
  }
});
</script>
```

### 5. pages/ui 컴포넌트

**역할**: 라우트에 매핑되는 페이지 컴포넌트

**특징**:
- 위젯, 기능, 엔티티 컴포넌트 조합
- 라우팅 로직 처리
- 페이지 레이아웃 정의

**명명 규칙**: `{Entity}Page` 또는 `{Purpose}Page` (HomePage, ProductDetailPage 등)

**예시**:
```vue
<!-- pages/products/ui/ProductDetailPage.vue (간소화된 버전) -->
<template>
  <div class="product-detail-page">
    <div v-if="loading" class="loading">
      <Spinner />
    </div>
    
    <div v-else-if="error" class="error">
      {{ error }}
    </div>
    
    <template v-else-if="product">
      <div class="product-detail">
        <div class="product-image">
          <img :src="product.thumbnailImageUrl" :alt="product.name" />
        </div>
        
        <div class="product-info">
          <h1>{{ product.name }}</h1>
          <p class="price">{{ formatPrice(product.price) }}</p>
          <p class="description">{{ product.description }}</p>
          
          <!-- features/cart의 기능 사용 -->
          <Button @click="addToCart(product, 1)">장바구니에 담기</Button>
        </div>
      </div>
      
      <!-- widgets/relatedProducts의 위젯 사용 -->
      <RelatedProductsWidget :categoryId="product.category" />
    </template>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { Spinner, Button } from '@shared/ui';
import { useProductStore, Product } from '@entities/product';
import { useCartWithAuth } from '@features/cart';
import { RelatedProductsWidget } from '@widgets/relatedProducts';

export default defineComponent({
  name: 'ProductDetailPage',
  components: { Spinner, Button, RelatedProductsWidget },
  setup() {
    const route = useRoute();
    const productStore = useProductStore();
    const { addToCart } = useCartWithAuth();
    
    const product = ref<Product | null>(null);
    const loading = ref(true);
    const error = ref<string | null>(null);
    
    const formatPrice = (price: number): string => {
      return new Intl.NumberFormat('ko-KR', {
        style: 'currency',
        currency: 'KRW'
      }).format(price);
    };
    
    onMounted(async () => {
      try {
        const productId = Number(route.params.id);
        product.value = await productStore.fetchProductById(productId);
      } catch (err: any) {
        error.value = err.message || '상품을 불러오는 중 오류가 발생했습니다.';
      } finally {
        loading.value = false;
      }
    });
    
    return {
      product,
      loading,
      error,
      formatPrice,
      addToCart
    };
  }
});
</script>
```

## 컴포넌트 통신 패턴

### 1. Props Down, Events Up

기본적인 부모-자식 컴포넌트 간 통신 패턴:
- 부모 -> 자식: props를 통해 데이터 전달
- 자식 -> 부모: 이벤트를 통해 데이터 전달

```vue
<!-- 부모 컴포넌트 -->
<template>
  <ProductCard 
    :product="product" 
    @add-to-cart="handleAddToCart"
  />
</template>

<!-- 자식 컴포넌트 -->
<template>
  <button @click="$emit('add-to-cart', product)">
    장바구니에 담기
  </button>
</template>
```

### 2. 상태 관리를 통한 통신

컴포넌트 간 직접 통신이 어려운 경우 상태 관리를 활용:
- Pinia 스토어를 통한 상태 공유
- 컴포지션 API를 활용한 상태 관리

```typescript
// entities/product/model/productStore.ts
export const useProductStore = defineStore('product', {
  state: () => ({
    products: [] as Product[],
    // ...
  }),
  actions: {
    async fetchProducts() {
      // ...
    }
  }
});

// 컴포넌트에서 사용
import { useProductStore } from '@entities/product';

export default defineComponent({
  setup() {
    const productStore = useProductStore();
    
    onMounted(() => {
      productStore.fetchProducts();
    });
    
    return {
      products: computed(() => productStore.products)
    };
  }
});
```

### 3. 컴포지션 함수를 통한 로직 분리

복잡한 로직은 컴포지션 함수로 분리하여 재사용:

```typescript
// features/productFilter/model/useProductFilter.ts
export function useProductFilter() {
  const searchQuery = ref('');
  const selectedCategory = ref<ProductCategory | null>(null);
  
  const applyFilter = () => {
    // 필터 적용 로직
  };
  
  return {
    searchQuery,
    selectedCategory,
    applyFilter
  };
}

// 컴포넌트에서 사용
import { useProductFilter } from '../model/useProductFilter';

export default defineComponent({
  setup() {
    const { searchQuery, selectedCategory, applyFilter } = useProductFilter();
    
    return {
      searchQuery,
      selectedCategory,
      applyFilter
    };
  }
});
```

## 상태 관리

### 1. 레이어별 상태 관리 책임

- **entities**: 도메인 모델 관련 상태 (CRUD 작업, 기본 데이터 관리)
- **features**: 기능 관련 상태 (사용자 상호작용, 폼 상태 등)
- **widgets**: 위젯 내부 UI 상태 (필터링, 정렬, 페이지네이션 등)
- **pages**: 페이지 관련 상태 (라우팅 파라미터, 페이지 레벨 UI 상태)
- **app**: 전역 애플리케이션 상태 (인증, 테마, 전역 설정 등)

### 2. Pinia 스토어 구성

```typescript
// entities/product/model/productStore.ts
export const useProductStore = defineStore('product', {
  state: () => ({
    products: [] as Product[],
    isLoading: false,
    error: null as string | null,
  }),
  
  getters: {
    getProductById: (state) => (id: number) => 
      state.products.find(product => product.id === id),
  },
  
  actions: {
    async fetchProducts() {
      this.isLoading = true;
      this.error = null;
      
      try {
        this.products = await productRepository.getProducts();
      } catch (err: any) {
        this.error = err.message;
      } finally {
        this.isLoading = false;
      }
    },
    
    // 기타 액션...
  }
});
```

## 모범 사례

### 1. 컴포넌트 책임 분리

- 각 컴포넌트는 단일 책임 원칙을 따라야 합니다.
- 복잡한 컴포넌트는 더 작은 컴포넌트로 분해하세요.
- 표현 로직과 비즈니스 로직을 분리하세요.

### 2. 컴포넌트 재사용성 향상

- 도메인 특화 로직을 분리하여 컴포넌트 재사용성을 높이세요.
- 슬롯과 스코프드 슬롯을 활용하여 유연한 컴포넌트를 만드세요.
- 컴포지션 API를 활용하여 로직을 재사용 가능한 함수로 분리하세요.

### 3. 성능 최적화

- `computed` 속성을 활용하여 불필요한 재계산을 방지하세요.
- 대용량 목록에는 가상 스크롤링을 적용하세요.
- 컴포넌트 메모이제이션을 활용하여 불필요한 리렌더링을 방지하세요.

### 4. 접근성 고려

- 모든 상호작용 요소에 적절한 ARIA 속성을 추가하세요.
- 키보드 네비게이션을 지원하세요.
- 충분한 색상 대비를 제공하세요.