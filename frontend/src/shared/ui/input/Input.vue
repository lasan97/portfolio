<template>
  <div class="mb-4">
    <label v-if="label" :for="id" class="block text-sm font-medium text-gray-700 mb-1">{{ label }}</label>
    <div class="relative">
      <input
        :id="id"
        :type="type"
        :value="modelValue"
        :placeholder="placeholder"
        :disabled="disabled"
        :required="required"
        :autocomplete="autocomplete"
        class="w-full px-3 py-2 bg-white border rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition"
        :class="{ 'border-red-500 focus:ring-red-500': error, 'border-gray-300': !error, 'bg-gray-100': disabled }"
        @input="handleInput"
        @blur="handleBlur"
      />
      <div v-if="error" class="mt-1 text-sm text-red-600">{{ error }}</div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { generateRandomId } from '../../lib/helpers';

export default defineComponent({
  name: 'BaseInput',
  props: {
    /**
     * 입력 필드 값
     */
    modelValue: {
      type: [String, Number],
      default: ''
    },
    /**
     * 입력 필드 라벨
     */
    label: {
      type: String,
      default: ''
    },
    /**
     * HTML input 타입
     */
    type: {
      type: String,
      default: 'text'
    },
    /**
     * 입력 필드 placeholder
     */
    placeholder: {
      type: String,
      default: ''
    },
    /**
     * 입력 필드 비활성화 여부
     */
    disabled: {
      type: Boolean,
      default: false
    },
    /**
     * 필수 입력 필드 여부
     */
    required: {
      type: Boolean,
      default: false
    },
    /**
     * 자동완성 속성
     */
    autocomplete: {
      type: String,
      default: 'off'
    },
    /**
     * 에러 메시지
     */
    error: {
      type: String,
      default: ''
    },
    /**
     * 입력 필드 ID
     */
    id: {
      type: String,
      default: () => `input-${generateRandomId()}`
    }
  },
  emits: ['update:modelValue', 'blur'],
  setup(props, { emit }) {
    const handleInput = (event: Event) => {
      const target = event.target as HTMLInputElement;
      emit('update:modelValue', target.value);
    };

    const handleBlur = (event: Event) => {
      emit('blur', event);
    };

    return {
      handleInput,
      handleBlur
    };
  }
});
</script>
