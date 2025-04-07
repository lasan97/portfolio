<template>
  <div class="register-form">
    <Card shadow>
      <template #header>
        <h2 class="text-2xl font-bold text-center">회원가입</h2>
      </template>
      
      <form @submit.prevent="handleSubmit">
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
          v-model="form.email"
          type="email"
          label="이메일"
          placeholder="이메일을 입력하세요"
          :error="errors.email"
          required
          autocomplete="email"
          @blur="validateEmail"
        />
        
        <Input
          v-model="form.name"
          label="이름"
          placeholder="이름을 입력하세요"
          :error="errors.name"
          autocomplete="name"
        />
        
        <Input
          v-model="form.password"
          type="password"
          label="비밀번호"
          placeholder="비밀번호를 입력하세요"
          :error="errors.password"
          required
          autocomplete="new-password"
          @blur="validatePassword"
        />
        
        <Input
          v-model="form.confirmPassword"
          type="password"
          label="비밀번호 확인"
          placeholder="비밀번호를 다시 입력하세요"
          :error="errors.confirmPassword"
          required
          autocomplete="new-password"
          @blur="validateConfirmPassword"
        />
        
        <div class="flex flex-col items-center">
          <Button
            type="submit"
            variant="primary"
            :disabled="loading"
            class="w-full mb-4"
          >
            {{ loading ? '처리 중...' : '회원가입' }}
          </Button>
          
          <div class="text-sm">
            이미 계정이 있으신가요? <a href="#" @click.prevent="goToLogin" class="text-blue-500 hover:underline">로그인</a>
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
import { useRegister } from '../model/useRegister';
import { RegisterFormData, RegisterFormErrors } from '../model/types';

export default defineComponent({
  name: 'RegisterForm',
  components: {
    Button,
    Card,
    Input
  },
  setup() {
    const router = useRouter();
    const { register, loading, error } = useRegister();
    
    // 폼 상태
    const form = reactive<RegisterFormData>({
      username: '',
      email: '',
      password: '',
      confirmPassword: '',
      name: ''
    });
    
    // 유효성 검사 에러
    const errors = reactive<RegisterFormErrors>({});
    
    // 사용자 이름 유효성 검사
    const validateUsername = () => {
      if (isEmpty(form.username)) {
        errors.username = '사용자 이름을 입력해주세요';
        return false;
      }
      
      if (form.username.length < 4) {
        errors.username = '사용자 이름은 4자 이상이어야 합니다';
        return false;
      }
      
      errors.username = undefined;
      return true;
    };
    
    // 이메일 유효성 검사
    const validateEmail = () => {
      if (isEmpty(form.email)) {
        errors.email = '이메일을 입력해주세요';
        return false;
      }
      
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!emailRegex.test(form.email)) {
        errors.email = '올바른 이메일 형식이 아닙니다';
        return false;
      }
      
      errors.email = undefined;
      return true;
    };
    
    // 비밀번호 유효성 검사
    const validatePassword = () => {
      if (isEmpty(form.password)) {
        errors.password = '비밀번호를 입력해주세요';
        return false;
      }
      
      if (form.password.length < 8) {
        errors.password = '비밀번호는 8자 이상이어야 합니다';
        return false;
      }
      
      errors.password = undefined;
      return true;
    };
    
    // 비밀번호 확인 유효성 검사
    const validateConfirmPassword = () => {
      if (isEmpty(form.confirmPassword)) {
        errors.confirmPassword = '비밀번호 확인을 입력해주세요';
        return false;
      }
      
      if (form.password !== form.confirmPassword) {
        errors.confirmPassword = '비밀번호가 일치하지 않습니다';
        return false;
      }
      
      errors.confirmPassword = undefined;
      return true;
    };
    
    // 폼 전체 유효성 검사
    const validateForm = () => {
      return (
        validateUsername() &&
        validateEmail() &&
        validatePassword() &&
        validateConfirmPassword()
      );
    };
    
    // 폼 제출 처리
    const handleSubmit = async () => {
      if (!validateForm()) {
        return;
      }
      
      try {
        await register(form);
      } catch (err) {
        // 에러는 useRegister에서 처리됨
      }
    };
    
    // 로그인 페이지로 이동
    const goToLogin = () => {
      router.push(ROUTES.LOGIN);
    };
    
    return {
      form,
      errors,
      loading,
      error,
      validateUsername,
      validateEmail,
      validatePassword,
      validateConfirmPassword,
      handleSubmit,
      goToLogin
    };
  }
});
</script>
