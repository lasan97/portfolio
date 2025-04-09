<template>
  <div class="login-page py-12">
    <div class="max-w-lg mx-auto">
      <Card>
        <LoginForm />
      </Card>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { Card } from '@shared/ui';
import { LoginForm } from '@features/auth';

export default defineComponent({
  name: 'LoginPage',
  components: {
    Card,
    LoginForm
  }
});
</script>
