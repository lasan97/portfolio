<template>
  <Teleport to="body">
    <transition name="toast-fade">
      <div 
        v-if="isVisible" 
        :class="[
          'fixed z-50 px-4 py-3 rounded-lg shadow-lg transform transition-all',
          'flex items-center justify-between',
          positionClass,
          typeClass
        ]"
      >
        <div class="flex items-center">
          <div v-if="iconType" class="mr-3">
            <span v-if="iconType === 'success'" class="text-xl">✓</span>
            <span v-else-if="iconType === 'error'" class="text-xl">✕</span>
            <span v-else-if="iconType === 'warning'" class="text-xl">⚠</span>
            <span v-else-if="iconType === 'info'" class="text-xl">ℹ</span>
          </div>
          <span class="text-sm md:text-base">{{ message }}</span>
        </div>
        <button 
          v-if="showClose" 
          @click="close"
          class="ml-3 flex-shrink-0 text-gray-300 hover:text-white"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
          </svg>
        </button>
      </div>
    </transition>
  </Teleport>
</template>

<script lang="ts">
import { defineComponent, ref, toRefs, watch, onMounted, onUnmounted } from 'vue';

export default defineComponent({
  name: 'ToastNotification',
  
  props: {
    message: {
      type: String,
      required: true
    },
    type: {
      type: String,
      default: 'info',
      validator: (value: string) => ['success', 'error', 'warning', 'info'].includes(value)
    },
    position: {
      type: String,
      default: 'top-center',
      validator: (value: string) => [
        'top-left', 'top-center', 'top-right',
        'bottom-left', 'bottom-center', 'bottom-right'
      ].includes(value)
    },
    duration: {
      type: Number,
      default: 3000
    },
    showClose: {
      type: Boolean,
      default: true
    },
    autoClose: {
      type: Boolean,
      default: true
    }
  },
  
  emits: ['close'],
  
  setup(props, { emit }) {
    const { duration, autoClose } = toRefs(props);
    const isVisible = ref(false);
    let timeout: ReturnType<typeof setTimeout> | null = null;
    
    const close = () => {
      isVisible.value = false;
      emit('close');
    };
    
    const startTimer = () => {
      if (autoClose.value && duration.value > 0) {
        timeout = setTimeout(() => {
          close();
        }, duration.value);
      }
    };
    
    const clearTimer = () => {
      if (timeout) {
        clearTimeout(timeout);
        timeout = null;
      }
    };
    
    watch(duration, () => {
      clearTimer();
      if (isVisible.value) {
        startTimer();
      }
    });
    
    onMounted(() => {
      // 마운트 후 약간의 지연을 두고 표시 (애니메이션 효과를 위함)
      setTimeout(() => {
        isVisible.value = true;
        startTimer();
      }, 100);
    });
    
    onUnmounted(() => {
      clearTimer();
    });
    
    const iconType = toRefs(props).type.value;
    
    const getPositionClass = () => {
      switch (props.position) {
        case 'top-left':
          return 'top-4 left-4';
        case 'top-center':
          return 'top-4 left-1/2 -translate-x-1/2';
        case 'top-right':
          return 'top-4 right-4';
        case 'bottom-left':
          return 'bottom-4 left-4';
        case 'bottom-center':
          return 'bottom-4 left-1/2 -translate-x-1/2';
        case 'bottom-right':
          return 'bottom-4 right-4';
        default:
          return 'top-4 right-4';
      }
    };
    
    const getTypeClass = () => {
      switch (props.type) {
        case 'success':
          return 'bg-green-500 text-white';
        case 'error':
          return 'bg-red-500 text-white';
        case 'warning':
          return 'bg-yellow-500 text-white';
        case 'info':
        default:
          return 'bg-blue-500 text-white';
      }
    };
    
    return {
      isVisible,
      close,
      iconType,
      positionClass: getPositionClass(),
      typeClass: getTypeClass()
    };
  }
});
</script>

<style scoped>
.toast-fade-enter-active,
.toast-fade-leave-active {
  transition: opacity 0.3s, transform 0.3s;
}

.toast-fade-enter-from,
.toast-fade-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}
</style>
