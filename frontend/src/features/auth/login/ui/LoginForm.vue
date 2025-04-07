<template>
  <div class="max-w-md mx-auto p-5">
    <Card shadow>
      <template #header>
        <h2 class="text-2xl font-bold text-center">로그인</h2>
      </template>
      
      <form @submit.prevent="handleSubmit" class="space-y-4">
        <Input
          v-model="form.username"
          label="사용자 이름"
          placeholder="사용자 이름을 입력하세요"
          :error="errors.username"
          required
          autocomplete="username"
          @blur="validateUsername"
        />
        
        <Input
          v-model="form.password"
          type="password"
          label="비밀번호"
          placeholder="비밀번호를 입력하세요"
          :error="errors.password"
          required
          autocomplete="current-password"
          @blur="validatePassword"
        />
        
        <div class="flex justify-between items-center">
          <label class="flex items-center text-sm">
            <input type="checkbox" v-model="form.rememberMe" class="mr-2 h-4 w-4 text-blue-600" />
            <span>로그인 상태 유지</span>
          </label>
          
          <a href="#" class="text-sm text-blue-600 hover:underline">비밀번호 찾기</a>
        </div>
        
        <div class="flex flex-col items-center space-y-4">
          <Button
            type="submit"
            variant="primary"
            :disabled="loading"
            class="w-full"
          >
            {{ loading ? '로그인 중...' : '로그인' }}
          </Button>
          
          <div class="text-sm">
            계정이 없으신가요? <a href="#" @click.prevent="goToRegister" class="text-blue-600 hover:underline">회원가입</a>
          </div>
        </div>
        
        <div v-if="error" class="mt-4 p-3 bg-red-50 text-red-600 rounded-md text-sm">
          {{ error }}
        </div>
      </form>
    </Card>
  </div>
</template>

<script lang="ts">
import { defineComponent, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { Button, Card, Input } from '@/shared/ui';
import { isEmpty } from '@/shared/lib/utils';
import { ROUTES } from '@/shared/config';
import { useLogin } from '../model/useLogin';
import { LoginFormData, LoginFormErrors } from '../model/types';

export default defineComponent({
  name: 'LoginForm',
  components: {
    Button,
    Card,
    Input
  },
  setup() {
    const router = useRouter();
    const { login, loading, error } = useLogin();
    
    // 폼 상태
    const form = reactive<LoginFormData>({
      username: '',
      password: '',
      rememberMe: false
    });
    
    // 유효성 검사 에러
    const errors = reactive<LoginFormErrors>({});
    
    // 사용자 이름 유효성 검사
    const validateUsername = () => {
      if (isEmpty(form.username)) {
        errors.username = '사용자 이름을 입력해주세요';
        return false;
      }
      
      errors.username = undefined;
      return true;
    };
    
    // 비밀번호 유효성 검사
    const validatePassword = () => {
      if (isEmpty(form.password)) {
        errors.password = '비밀번호를 입력해주세요';
        return false;
      }
      
      errors.password = undefined;
      return true;
    };
    
    // 폼 전체 유효성 검사
    const validateForm = () => {
      return validateUsername() && validatePassword();
    };
    
    // 폼 제출 처리
    const handleSubmit = async () => {
      if (!validateForm()) {
        return;
      }
      
      try {
        await login(form);
      } catch (err) {
        // 에러는 useLogin에서 처리됨
      }
    };
    
    // 회원가입 페이지로 이동
    const goToRegister = () => {
      router.push(ROUTES.REGISTER);
    };
    
    return {
      form,
      errors,
      loading,
      error,
      validateUsername,
      validatePassword,
      handleSubmit,
      goToRegister
    };
  }
});
</script>
