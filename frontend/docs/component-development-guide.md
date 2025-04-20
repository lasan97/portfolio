# 컴포넌트 개발 가이드

이 문서는 Portfolio 프로젝트에서 UI 컴포넌트를 개발하는 방법에 대한 상세 가이드를 제공합니다. Vue 3와 Composition API를 사용한 컴포넌트 개발 방법과 모범 사례를 설명합니다.

## 목차

1. [컴포넌트 개요](#컴포넌트-개요)
2. [컴포넌트 계층 구조](#컴포넌트-계층-구조)
3. [컴포넌트 개발 프로세스](#컴포넌트-개발-프로세스)
4. [Composition API 활용](#composition-api-활용)
5. [Props 및 Emits 설계](#props-및-emits-설계)
6. [컴포넌트 스타일링](#컴포넌트-스타일링)
7. [재사용 가능한 컴포넌트 설계](#재사용-가능한-컴포넌트-설계)
8. [컴포넌트 테스트](#컴포넌트-테스트)
9. [접근성 고려사항](#접근성-고려사항)
10. [모범 사례 및 패턴](#모범-사례-및-패턴)

## 컴포넌트 개요

Vue 컴포넌트는 UI를 구성하는 재사용 가능한 독립적인 단위입니다. 이 프로젝트에서는 Vue 3의 Composition API를 사용하여 컴포넌트를 개발합니다.

### 컴포넌트의 특징

- **재사용성**: 여러 곳에서 재사용할 수 있는 독립적인 UI 단위
- **캡슐화**: 내부 상태와 로직을 캡슐화하여 외부와의 인터페이스를 명확히 정의
- **조합 가능성**: 작은 컴포넌트를 조합하여 복잡한 UI를 구성
- **유지보수성**: 관심사 분리를 통해 코드의 유지보수성 향상

## 컴포넌트 계층 구조

프로젝트에서 컴포넌트는 FSD 아키텍처에 따라 다음과 같은 계층으로 구분됩니다:

1. **공유 컴포넌트** (`shared/ui`): 도메인에 종속되지 않는 기본 UI 컴포넌트
2. **엔티티 컴포넌트** (`entities/{entity}/ui`): 특정 도메인 모델과 관련된 기본 UI 컴포넌트
3. **피처 컴포넌트** (`features/{feature}/ui`): 특정 기능과 관련된 UI 컴포넌트
4. **위젯 컴포넌트** (`widgets/{widget}/ui`): 여러 기능과 엔티티를 조합한 복합 UI 블록
5. **페이지 컴포넌트** (`pages/{page}/ui`): 특정 경로에 매핑되는 전체 페이지 컴포넌트

### 컴포넌트 유형별 책임

- **공유 컴포넌트**: 버튼, 입력 필드, 카드 등 기본 UI 요소
- **엔티티 컴포넌트**: 도메인 모델을 시각적으로 표현 (예: ProductCard, UserProfile)
- **피처 컴포넌트**: 사용자 인터랙션 기능 구현 (예: LoginForm, ProductFilter)
- **위젯 컴포넌트**: 여러 기능을 조합한 복합 UI 블록 (예: Header, Sidebar)
- **페이지 컴포넌트**: 전체 페이지 레이아웃 및 구성 (예: HomePage, ProfilePage)

## 컴포넌트 개발 프로세스

컴포넌트 개발은 다음 단계로 진행됩니다:

1. **요구사항 분석**: 컴포넌트의 목적과 기능 요구사항을 명확히 이해합니다.
2. **인터페이스 설계**: Props, Events, Slots 등 컴포넌트의 인터페이스를 설계합니다.
3. **상태 및 로직 구현**: 컴포넌트의 내부 상태와 로직을 구현합니다.
4. **UI 마크업 및 스타일링**: 컴포넌트의 UI를 구현하고 스타일링합니다.
5. **테스트 및 검증**: 컴포넌트가 요구사항을 충족하는지 확인합니다.
6. **문서화**: 컴포넌트의 사용 방법과 예시를 문서화합니다.

## Composition API 활용

이 프로젝트에서는 Vue 3의 Composition API를 사용하여 컴포넌트를 개발합니다. Composition API는 관련 로직을 함께 그룹화하여 코드의 가독성과 재사용성을 높입니다.

### `<script setup>` 문법

간단한 컴포넌트는 `<script setup>` 문법을 사용하여 개발합니다:

```vue
<script setup lang="ts">
import { ref, computed } from 'vue';

// Props 정의
interface Props {
  initialCount: number;
}

const props = withDefaults(defineProps<Props>(), {
  initialCount: 0
});

// Emits 정의
const emit = defineEmits<{
  (e: 'increment', value: number): void;
  (e: 'decrement', value: number): void;
}>();

// 상태
const count = ref(props.initialCount);

// 계산된 속성
const doubleCount = computed(() => count.value * 2);

// 메서드
function increment() {
  count.value++;
  emit('increment', count.value);
}

function decrement() {
  count.value--;
  emit('decrement', count.value);
}
</script>

<template>
  <div class="counter">
    <button @click="decrement">-</button>
    <span>{{ count }}</span>
    <span class="double">({{ doubleCount }})</span>
    <button @click="increment">+</button>
  </div>
</template>
```

### `defineComponent`와 `setup` 함수

복잡한 컴포넌트는 `defineComponent`와 `setup` 함수를 사용하여 개발합니다:

```vue
<script lang="ts">
import { defineComponent, ref, computed, watch, onMounted } from 'vue';
import type { PropType } from 'vue';
import { Product } from '@entities/product';

export default defineComponent({
  name: 'ProductDetail',
  
  props: {
    product: {
      type: Object as PropType<Product>,
      required: true
    },
    showDescription: {
      type: Boolean,
      default: true
    }
  },
  
  emits: ['add-to-cart', 'view-details'],
  
  setup(props, { emit }) {
    // 상태
    const quantity = ref(1);
    
    // 계산된 속성
    const totalPrice = computed(() => props.product.price * quantity.value);
    const isInStock = computed(() => props.product.inStock);
    
    // 감시자
    watch(() => props.product, (newProduct) => {
      // 상품이 변경되면 수량 초기화
      quantity.value = 1;
    });
    
    // 라이프사이클 훅
    onMounted(() => {
      console.log('ProductDetail 컴포넌트가 마운트되었습니다.');
    });
    
    // 메서드
    function incrementQuantity() {
      quantity.value++;
    }
    
    function decrementQuantity() {
      if (quantity.value > 1) {
        quantity.value--;
      }
    }
    
    function addToCart() {
      emit('add-to-cart', {
        product: props.product,
        quantity: quantity.value
      });
    }
    
    function viewDetails() {
      emit('view-details', props.product.id);
    }
    
    // 노출할 상태와 메서드 반환
    return {
      quantity,
      totalPrice,
      isInStock,
      incrementQuantity,
      decrementQuantity,
      addToCart,
      viewDetails
    };
  }
});
</script>
```

## Props 및 Emits 설계

컴포넌트의 인터페이스는 Props와 Emits를 통해 정의됩니다. 이는 컴포넌트의 API를 구성하는 중요한 요소입니다.

### Props 설계 원칙

1. **타입 안전성**: TypeScript를 사용하여 Props의 타입을 명확히 정의합니다.
2. **기본값 제공**: 가능한 경우 Props에 기본값을 제공합니다.
3. **필수 여부 명시**: 필수 Props는 `required: true`로 명시합니다.
4. **유효성 검사**: 필요한 경우 Props에 유효성 검사 함수를 추가합니다.

#### Props 정의 예시

```typescript
// <script setup> 문법 사용
interface ButtonProps {
  variant: 'primary' | 'secondary' | 'danger';
  size?: 'small' | 'medium' | 'large';
  disabled?: boolean;
  loading?: boolean;
}

const props = withDefaults(defineProps<ButtonProps>(), {
  size: 'medium',
  disabled: false,
  loading: false
});

// defineComponent 사용
props: {
  variant: {
    type: String as PropType<'primary' | 'secondary' | 'danger'>,
    required: true
  },
  size: {
    type: String as PropType<'small' | 'medium' | 'large'>,
    default: 'medium'
  },
  disabled: {
    type: Boolean,
    default: false
  },
  loading: {
    type: Boolean,
    default: false
  }
}
```

### Emits 설계 원칙

1. **타입 안전성**: TypeScript를 사용하여 Emits의 타입을 명확히 정의합니다.
2. **이벤트 페이로드**: 이벤트와 함께 전달할 데이터의 타입을 명시합니다.
3. **명확한 이벤트 이름**: 이벤트 이름은 명확하고 일관되게 지정합니다.

#### Emits 정의 예시

```typescript
// <script setup> 문법 사용
const emit = defineEmits<{
  (e: 'click', payload: { id: number }): void;
  (e: 'focus'): void;
  (e: 'blur'): void;
}>();

// defineComponent 사용
emits: {
  click: (payload: { id: number }) => true,
  focus: null,
  blur: null
}
```

## 컴포넌트 스타일링

이 프로젝트에서는 TailwindCSS를 사용하여 컴포넌트를 스타일링합니다. 또한 Vue의 스코프 스타일을 활용하여 컴포넌트별 스타일을 캡슐화합니다.

### TailwindCSS 활용

TailwindCSS는 유틸리티 클래스를 사용하여 스타일링하는 방식을 제공합니다:

```vue
<template>
  <button 
    class="px-4 py-2 rounded-md font-medium focus:outline-none focus:ring-2 focus:ring-offset-2"
    :class="[
      variant === 'primary' ? 'bg-blue-600 text-white hover:bg-blue-700' :
      variant === 'secondary' ? 'bg-gray-200 text-gray-800 hover:bg-gray-300' :
      'bg-red-600 text-white hover:bg-red-700',
      size === 'small' ? 'text-sm' :
      size === 'large' ? 'text-lg' :
      'text-base',
      disabled ? 'opacity-50 cursor-not-allowed' : 'cursor-pointer'
    ]"
    :disabled="disabled || loading"
  >
    <span v-if="loading" class="mr-2">
      <svg class="animate-spin h-4 w-4 inline" viewBox="0 0 24 24">
        <!-- 로딩 아이콘 SVG -->
      </svg>
    </span>
    <slot></slot>
  </button>
</template>
```

### 스코프 스타일

컴포넌트별 스타일은 `<style scoped>` 태그를 사용하여 캡슐화합니다:

```vue
<style scoped>
.custom-button {
  transition: all 0.2s ease;
}

.custom-button:active {
  transform: scale(0.98);
}
</style>
```

### 스타일 변수 및 테마

전역 스타일 변수와 테마는 `app/styles` 디렉토리에 정의합니다:

```css
/* app/styles/variables.css */
:root {
  --primary-color: #3b82f6;
  --secondary-color: #e5e7eb;
  --danger-color: #ef4444;
  --text-color: #1f2937;
  --background-color: #ffffff;
}

/* 다크 모드 */
@media (prefers-color-scheme: dark) {
  :root {
    --primary-color: #60a5fa;
    --secondary-color: #4b5563;
    --danger-color: #f87171;
    --text-color: #f3f4f6;
    --background-color: #1f2937;
  }
}
```

## 재사용 가능한 컴포넌트 설계

재사용 가능한 컴포넌트를 설계할 때는 다음 원칙을 고려합니다:

### 1. 단일 책임 원칙

각 컴포넌트는 하나의 책임만 가져야 합니다. 여러 책임을 가진 컴포넌트는 작은 컴포넌트로 분리합니다.

### 2. 구성 가능성

컴포넌트는 다양한 상황에서 재사용할 수 있도록 구성 가능해야 합니다. 이를 위해 Props, Slots, Scoped Slots 등을 활용합니다.

#### Slots 활용 예시

```vue
<template>
  <div class="card">
    <div v-if="$slots.header" class="card-header">
      <slot name="header"></slot>
    </div>
    <div class="card-body">
      <slot></slot>
    </div>
    <div v-if="$slots.footer" class="card-footer">
      <slot name="footer"></slot>
    </div>
  </div>
</template>

<!-- 사용 예시 -->
<Card>
  <template #header>
    <h2>카드 제목</h2>
  </template>
  
  <p>카드 내용입니다.</p>
  
  <template #footer>
    <button>확인</button>
  </template>
</Card>
```

#### Scoped Slots 활용 예시

```vue
<template>
  <div class="list">
    <div v-for="(item, index) in items" :key="item.id" class="list-item">
      <slot :item="item" :index="index"></slot>
    </div>
  </div>
</template>

<!-- 사용 예시 -->
<List :items="products">
  <template #default="{ item, index }">
    <div class="product-item">
      <span>{{ index + 1 }}. {{ item.name }}</span>
      <span>{{ formatPrice(item.price) }}</span>
    </div>
  </template>
</List>
```

### 3. 컴포넌트 조합

복잡한 UI는 작은 컴포넌트를 조합하여 구성합니다. 이를 통해 코드의 재사용성과 유지보수성을 높입니다.

```vue
<template>
  <Card>
    <template #header>
      <div class="flex justify-between items-center">
        <h2>{{ product.name }}</h2>
        <Badge :variant="product.inStock ? 'success' : 'danger'">
          {{ product.inStock ? '재고 있음' : '품절' }}
        </Badge>
      </div>
    </template>
    
    <div class="product-details">
      <ProductImage :src="product.thumbnailImageUrl" :alt="product.name" />
      <div class="product-info">
        <PriceDisplay 
          :price="product.price" 
          :originalPrice="product.originalPrice" 
        />
        <p>{{ product.description }}</p>
      </div>
    </div>
    
    <template #footer>
      <div class="flex justify-end">
        <Button 
          variant="secondary" 
          class="mr-2"
          @click="$emit('view-details', product.id)"
        >
          상세 보기
        </Button>
        <Button 
          variant="primary" 
          :disabled="!product.inStock"
          @click="$emit('add-to-cart', product)"
        >
          장바구니에 추가
        </Button>
      </div>
    </template>
  </Card>
</template>
```

## 컴포넌트 테스트

컴포넌트 테스트는 컴포넌트의 동작을 검증하고 버그를 방지하는 데 중요합니다. 이 프로젝트에서는 Vitest와 Vue Test Utils를 사용하여 컴포넌트를 테스트합니다.

### 단위 테스트 작성 예시

```typescript
import { describe, it, expect, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import Button from './Button.vue';

describe('Button 컴포넌트', () => {
  it('기본 렌더링이 올바르게 동작한다', () => {
    const wrapper = mount(Button, {
      props: {
        variant: 'primary'
      },
      slots: {
        default: '버튼 텍스트'
      }
    });
    
    expect(wrapper.text()).toBe('버튼 텍스트');
    expect(wrapper.classes()).toContain('bg-blue-600');
  });
  
  it('클릭 이벤트가 올바르게 발생한다', async () => {
    const wrapper = mount(Button, {
      props: {
        variant: 'primary'
      }
    });
    
    await wrapper.trigger('click');
    expect(wrapper.emitted('click')).toBeTruthy();
  });
  
  it('disabled 상태일 때 클릭 이벤트가 발생하지 않는다', async () => {
    const wrapper = mount(Button, {
      props: {
        variant: 'primary',
        disabled: true
      }
    });
    
    await wrapper.trigger('click');
    expect(wrapper.emitted('click')).toBeFalsy();
  });
  
  it('loading 상태일 때 로딩 아이콘이 표시된다', () => {
    const wrapper = mount(Button, {
      props: {
        variant: 'primary',
        loading: true
      }
    });
    
    expect(wrapper.find('svg').exists()).toBe(true);
  });
});
```

## 접근성 고려사항

접근성(A11y)은 모든 사용자가 웹 애플리케이션을 사용할 수 있도록 하는 중요한 요소입니다. 컴포넌트 개발 시 다음과 같은 접근성 고려사항을 염두에 둡니다:

### 1. 시맨틱 HTML

적절한 HTML 요소를 사용하여 콘텐츠의 의미를 명확히 전달합니다:

```vue
<!-- 잘못된 예 -->
<div class="button" @click="handleClick">버튼</div>

<!-- 올바른 예 -->
<button type="button" @click="handleClick">버튼</button>
```

### 2. ARIA 속성

필요한 경우 ARIA 속성을 사용하여 접근성을 향상시킵니다:

```vue
<div 
  role="tablist" 
  aria-orientation="horizontal"
>
  <button 
    v-for="(tab, index) in tabs" 
    :key="tab.id"
    role="tab"
    :id="`tab-${tab.id}`"
    :aria-selected="activeTab === index"
    :aria-controls="`panel-${tab.id}`"
    @click="setActiveTab(index)"
  >
    {{ tab.label }}
  </button>
</div>

<div 
  v-for="(tab, index) in tabs" 
  :key="tab.id"
  role="tabpanel"
  :id="`panel-${tab.id}`"
  :aria-labelledby="`tab-${tab.id}`"
  v-show="activeTab === index"
>
  {{ tab.content }}
</div>
```

### 3. 키보드 접근성

키보드만으로도 모든 기능을 사용할 수 있도록 합니다:

```vue
<script setup lang="ts">
function handleKeyDown(event: KeyboardEvent) {
  if (event.key === 'Enter' || event.key === ' ') {
    event.preventDefault();
    handleClick();
  }
}
</script>

<template>
  <div 
    class="custom-checkbox" 
    tabindex="0"
    role="checkbox"
    :aria-checked="checked"
    @click="handleClick"
    @keydown="handleKeyDown"
  >
    <svg v-if="checked" class="check-icon" viewBox="0 0 24 24">
      <!-- 체크 아이콘 SVG -->
    </svg>
    <span class="checkbox-label">
      <slot></slot>
    </span>
  </div>
</template>
```

### 4. 색상 대비

텍스트와 배경 간의 충분한 색상 대비를 제공합니다:

```vue
<!-- 낮은 대비 (피해야 함) -->
<p class="text-gray-400 bg-gray-200">낮은 대비 텍스트</p>

<!-- 높은 대비 (권장) -->
<p class="text-gray-900 bg-gray-100">높은 대비 텍스트</p>
```

### 5. 포커스 관리

사용자가 현재 포커스된 요소를 쉽게 식별할 수 있도록 합니다:

```vue
<style>
/* 기본 포커스 스타일 */
:focus {
  outline: 2px solid var(--primary-color);
  outline-offset: 2px;
}

/* 마우스 사용자를 위한 포커스 스타일 숨김 (선택적) */
:focus:not(:focus-visible) {
  outline: none;
}

/* 키보드 사용자를 위한 포커스 스타일 */
:focus-visible {
  outline: 2px solid var(--primary-color);
  outline-offset: 2px;
}
</style>
```

## 모범 사례 및 패턴

### 1. 컴포넌트 설계 원칙

- **단일 책임**: 각 컴포넌트는 하나의 책임만 가져야 합니다.
- **낮은 결합도**: 컴포넌트 간의 결합도를 최소화합니다.
- **높은 응집도**: 관련된 기능은 함께 그룹화합니다.
- **명확한 인터페이스**: Props, Events, Slots를 통해 명확한 인터페이스를 제공합니다.

### 2. 성능 최적화

- **메모이제이션**: `computed`, `watch` 등을 사용하여 계산 비용이 큰 작업을 최적화합니다.
- **지연 로딩**: 필요한 경우 컴포넌트를 지연 로딩합니다.
- **가상 스크롤링**: 대량의 데이터를 표시할 때 가상 스크롤링을 사용합니다.

```typescript
// 지연 로딩 예시
const HeavyComponent = defineAsyncComponent(() => 
  import('./HeavyComponent.vue')
);
```

### 3. 에러 처리

- **폴백 UI**: 컴포넌트 로딩 실패 시 폴백 UI를 제공합니다.
- **에러 경계**: 컴포넌트 에러가 전체 애플리케이션에 영향을 미치지 않도록 합니다.

```vue
<script setup lang="ts">
import { ref, onErrorCaptured } from 'vue';

const error = ref<Error | null>(null);

onErrorCaptured((err) => {
  error.value = err;
  return false; // 에러가 상위로 전파되지 않도록 함
});
</script>

<template>
  <div>
    <div v-if="error" class="error-boundary">
      <h3>오류가 발생했습니다</h3>
      <p>{{ error.message }}</p>
      <button @click="error = null">다시 시도</button>
    </div>
    <slot v-else></slot>
  </div>
</template>
```

### 4. 상태 관리

- **로컬 상태**: 컴포넌트 내부에서만 사용되는 상태는 `ref`나 `reactive`를 사용합니다.
- **공유 상태**: 여러 컴포넌트에서 공유되는 상태는 Pinia 스토어를 사용합니다.
- **상태 끌어올리기**: 필요한 경우 상태를 상위 컴포넌트로 끌어올립니다.

### 5. 컴포넌트 문서화

컴포넌트의 사용 방법과 예시를 문서화하여 다른 개발자가 쉽게 이해하고 사용할 수 있도록 합니다:

```typescript
/**
 * 사용자 정보를 표시하는 카드 컴포넌트
 * 
 * @example
 *