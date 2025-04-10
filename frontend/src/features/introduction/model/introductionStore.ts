import { defineStore } from 'pinia';
import { ref } from 'vue';
import { getIntroduction, updateIntroduction, createIntroduction } from '../api';
import type { IntroductionDto, IntroductionUpdateRequest, IntroductionCreateRequest } from '@entities/introduction';

export const useIntroductionStore = defineStore('introduction', () => {
  const loading = ref(false);
  const error = ref<string | null>(null);
  const introduction = ref<IntroductionDto | null>(null);

  /**
   * 자기소개 정보 조회
   */
  const fetchIntroduction = async (): Promise<IntroductionDto | null> => {
    loading.value = true;
    error.value = null;
    
    try {
      const response = await getIntroduction();
      introduction.value = response.data;
      return response.data;
    } catch (err: any) {
      error.value = err.response?.data?.message || '자기소개 정보를 가져오는 중 오류가 발생했습니다.';
      return null;
    } finally {
      loading.value = false;
    }
  };

  /**
   * 자기소개 정보 업데이트
   */
  const updateIntroductionInfo = async (data: IntroductionUpdateRequest): Promise<boolean> => {
    loading.value = true;
    error.value = null;
    
    try {
      await updateIntroduction(data);
      return true;
    } catch (err: any) {
      error.value = err.response?.data?.message || '자기소개 정보 업데이트 중 오류가 발생했습니다.';
      return false;
    } finally {
      loading.value = false;
    }
  };

  /**
   * 자기소개 정보 생성
   */
  const createIntroductionInfo = async (data: IntroductionCreateRequest): Promise<boolean> => {
    loading.value = true;
    error.value = null;
    
    try {
      await createIntroduction(data);
      return true;
    } catch (err: any) {
      error.value = err.response?.data?.message || '자기소개 정보 생성 중 오류가 발생했습니다.';
      return false;
    } finally {
      loading.value = false;
    }
  };

  return {
    loading,
    error,
    introduction,
    fetchIntroduction,
    updateIntroductionInfo,
    createIntroductionInfo
  };
});
