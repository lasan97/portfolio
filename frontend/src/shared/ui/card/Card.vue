<template>
  <div class="bg-white rounded-lg overflow-hidden" :class="{ 'shadow-md': shadow }">
    <div v-if="$slots.header" class="px-4 py-3 border-b border-gray-200 font-semibold">
      <slot name="header"></slot>
    </div>
    <div class="p-4" :style="customPadding">
      <slot></slot>
    </div>
    <div v-if="$slots.footer" class="px-4 py-3 border-t border-gray-200">
      <slot name="footer"></slot>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, computed } from 'vue';

export default defineComponent({
  name: 'BaseCard',
  props: {
    /**
     * 그림자 활성화 여부
     */
    shadow: {
      type: Boolean,
      default: true
    },
    /**
     * 카드 내부 패딩
     */
    padding: {
      type: String,
      default: '16px'
    }
  },
  setup(props) {
    // 커스텀 패딩이 있는 경우 적용
    const customPadding = computed(() => {
      return props.padding !== '16px' ? { padding: props.padding } : {};
    });

    return {
      customPadding
    };
  }
});
</script>
