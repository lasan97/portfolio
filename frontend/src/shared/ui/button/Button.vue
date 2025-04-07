<template>
  <button
    :class="[
      'inline-flex items-center justify-center px-4 py-2 font-medium text-sm rounded-md transition-colors focus:outline-none',
      variantClasses,
      { 'opacity-50 cursor-not-allowed': disabled }
    ]"
    :disabled="disabled"
    :type="type"
    @click="handleClick"
  >
    <slot></slot>
  </button>
</template>

<script lang="ts">
import { defineComponent, computed } from 'vue';

export default defineComponent({
  name: 'BaseButton',
  props: {
    /**
     * 버튼 스타일 variant
     */
    variant: {
      type: String,
      default: 'primary',
      validator: (value: string) => {
        return ['primary', 'secondary', 'danger', 'success', 'warning', 'text'].includes(value);
      }
    },
    /**
     * 버튼 비활성화 여부
     */
    disabled: {
      type: Boolean,
      default: false
    },
    /**
     * HTML 버튼 타입
     */
    type: {
      type: String,
      default: 'button',
      validator: (value: string) => {
        return ['button', 'submit', 'reset'].includes(value);
      }
    }
  },
  emits: ['click'],
  setup(props, { emit }) {
    // Variant에 따른 클래스 계산
    const variantClasses = computed(() => {
      switch (props.variant) {
        case 'primary':
          return 'bg-blue-600 hover:bg-blue-700 text-white';
        case 'secondary':
          return 'bg-gray-200 hover:bg-gray-300 text-gray-900';
        case 'danger':
          return 'bg-red-600 hover:bg-red-700 text-white';
        case 'success':
          return 'bg-green-600 hover:bg-green-700 text-white';
        case 'warning':
          return 'bg-yellow-500 hover:bg-yellow-600 text-gray-900';
        case 'text':
          return 'bg-transparent hover:bg-gray-100 text-blue-600 px-2 py-1';
        default:
          return 'bg-blue-600 hover:bg-blue-700 text-white';
      }
    });

    const handleClick = (event: MouseEvent) => {
      if (!props.disabled) {
        emit('click', event);
      }
    };

    return {
      variantClasses,
      handleClick
    };
  }
});
</script>
