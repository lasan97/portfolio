import axios from 'axios'
import { API_URL } from '../config'
import { getAuthToken } from '../lib'

// Axios 인스턴스 생성
export const apiInstance = axios.create({
  baseURL: API_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  },
  withCredentials: true // 쿠키 전송을 위해 필요
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
        import('@shared/lib').then(({ logout }) => {
          logout();
          window.location.href = '/login';
        });
      }
    }
    return Promise.reject(error);
  }
)

export default apiInstance