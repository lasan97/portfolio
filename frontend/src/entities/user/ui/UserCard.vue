<template>
  <Card>
    <div class="flex items-center">
      <div v-if="user.profileImageUrl" class="mr-4">
        <img 
          :src="user.profileImageUrl" 
          alt="Profile" 
          class="w-16 h-16 rounded-full object-cover"
        />
      </div>
      <div v-else class="mr-4">
        <div class="w-16 h-16 bg-gray-200 rounded-full flex items-center justify-center">
          <span class="text-gray-500 text-xl">{{ userInitials }}</span>
        </div>
      </div>
      <div>
        <h3 class="text-lg font-medium text-gray-900">{{ user.nickname }}</h3>
        <p class="text-sm text-gray-500">{{ user.email }}</p>
      </div>
    </div>
  </Card>
</template>

<script lang="ts">
import {defineComponent, computed, PropType} from 'vue';
import { Card } from '@shared/ui';
import { User } from '../model/types';

export default defineComponent({
  name: 'UserCard',
  components: {
    Card
  },
  props: {
    user: {
      type: Object as PropType<User>,
      required: true
    }
  },
  setup(props) {
    const userInitials = computed(() => {
      if (!props.user.nickname) return '?';
      return props.user.nickname.charAt(0).toUpperCase();
    });

    return {
      userInitials
    };
  }
});
</script>
