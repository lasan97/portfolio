<template>
  <button 
    :class="[
      'px-4 py-2 rounded transition-colors duration-200 font-medium focus:outline-none focus:ring-2 focus:ring-opacity-50',
      variantClass,
      { 'opacity-50 cursor-not-allowed': disabled }
    ]" 
    :disabled="disabled"
    @click="$emit('click')"
  >
    <slot></slot>
  </button>
</template>

<script lang="ts">
import { defineComponent, computed } from 'vue';

export default defineComponent({
  name: 'Button',
  props: {
    variant: {
      type: String,
      default: 'primary',
      validator: (value: string) => ['primary', 'secondary', 'danger', 'ghost'].includes(value)
    },
    disabled: {
      type: Boolean,
      default: false
    }
  },
  setup(props) {
    const variantClass = computed(() => {
      switch (props.variant) {
        case 'primary':
          return 'bg-blue-600 hover:bg-blue-700 text-white focus:ring-blue-300';
        case 'secondary':
          return 'bg-gray-200 hover:bg-gray-300 text-gray-800 focus:ring-gray-300';
        case 'danger':
          return 'bg-red-600 hover:bg-red-700 text-white focus:ring-red-300';
        case 'ghost':
          return 'bg-transparent hover:bg-gray-100 text-gray-700 focus:ring-gray-200';
        default:
          return 'bg-blue-600 hover:bg-blue-700 text-white focus:ring-blue-300';
      }
    });

    return {
      variantClass
    };
  }
});
</script>
