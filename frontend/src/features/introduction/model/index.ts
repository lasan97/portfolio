import { useIntroductionStore } from './introductionStore';
import { IntroductionUpdateRequest, IntroductionCreateRequest, IntroductionDto } from '@entities/introduction';

/**
 * 자기소개 관련 커스텀 훅
 * @returns 자기소개 관련 상태 및 API 함수
 */
export function useIntroduction() {
  const introductionStore = useIntroductionStore();
  
  /**
   * 자기소개 정보 조회
   */
  const fetchIntroduction = async (): Promise<IntroductionDto | null> => {
    return await introductionStore.fetchIntroduction();
  };
  
  /**
   * 자기소개 정보 업데이트
   */
  const updateIntroduction = async (data: IntroductionUpdateRequest): Promise<boolean> => {
    return await introductionStore.updateIntroductionInfo(data);
  };
  
  /**
   * 자기소개 정보 생성
   */
  const createIntroduction = async (data: IntroductionCreateRequest): Promise<boolean> => {
    return await introductionStore.createIntroductionInfo(data);
  };
  
  return {
    loading: introductionStore.loading,
    error: introductionStore.error,
    introduction: introductionStore.introduction,
    fetchIntroduction,
    updateIntroduction,
    createIntroduction
  };
}
