import axios from 'axios'
import {getAuthToken} from '../lib'

// Axios 인스턴스 생성
export const apiInstance = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  },
  withCredentials: true // 쿠키 전송을 위해 필요 (CORS 요청 시 중요)
})

// 요청 인터셉터
apiInstance.interceptors.request.use(
  (config) => {
    // 요청 전 처리 (토큰 추가)
    const token = getAuthToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
)

// 응답 인터셉터
apiInstance.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    // 401 Unauthorized 에러 처리
    if (error.response && error.response.status === 401) {
      // 인증 실패 처리 (예: 로그아웃)
      if (typeof window !== 'undefined') {
        // 현재 URL을 저장하여 로그인 후 리다이렉트
        const currentPath = window.location.pathname;
        sessionStorage.setItem('redirectAfterLogin', currentPath);
        
        import('@shared/lib').then(({ logout }) => {
          logout();
          window.location.href = '/login';
        });
      }
    }
    
    // 오류 메시지 추출 및 로깅
    const errorMessage = 
      error.response?.data?.message || 
      error.message || 
      '서버 통신 중 오류가 발생했습니다.';
    
    console.error('API 오류:', errorMessage);
    
    return Promise.reject(error);
  }
)

export default apiInstance