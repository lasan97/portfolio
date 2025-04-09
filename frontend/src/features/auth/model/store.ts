import { Module } from 'vuex';
import { api } from '@/shared/api';
import { AuthState, TokenResponse } from './types';
import { processOAuthCallback } from '../api';

const authModule: Module<AuthState, any> = {
  namespaced: true,
  
  state: () => ({
    token: localStorage.getItem('token'),
    loading: false,
    error: null
  }),
  
  mutations: {
    setToken(state, token: string) {
      state.token = token;
      localStorage.setItem('token', token);
    },
    setLoading(state, loading: boolean) {
      state.loading = loading;
    },
    setError(state, error: string | null) {
      state.error = error;
    },
    clearAuth(state) {
      state.token = null;
      localStorage.removeItem('token');
    }
  },
  
  actions: {
    // GitHub 인증 코드로 로그인/회원가입 처리
    async handleOAuthCallback({ commit }, code: string) {
      commit('setLoading', true);
      commit('setError', null);
      
      try {
        // 백엔드에 OAuth 콜백 코드 전송하여 로그인/회원가입 처리
        const response = await processOAuthCallback(code);
        
        // 토큰 저장
        commit('setToken', response.token);
        
        // 사용자 정보 저장
        commit('user/setUser', response.user, { root: true });
        
        return response;
      } catch (error: any) {
        const errorMessage = error.response?.data?.message || 'OAuth 로그인 처리 중 오류가 발생했습니다.';
        commit('setError', errorMessage);
        throw new Error(errorMessage);
      } finally {
        commit('setLoading', false);
      }
    },
    
    // 이전 handleTokenFromCallback 메서드 유지 (필요할 경우)
    async handleTokenFromCallback({ commit, dispatch }, token: string) {
      commit('setToken', token);
      await dispatch('fetchCurrentUser');
    },
    
    async fetchCurrentUser({ commit }) {
      commit('setLoading', true);
      commit('setError', null);
      
      try {
        const response = await api.get('/api/users/me');
        commit('user/setUser', response.data, { root: true });
      } catch (error: any) {
        commit('setError', error.response?.data?.message || '사용자 정보를 가져오는데 실패했습니다.');
        commit('clearAuth');
        commit('user/clearUser', null, { root: true });
      } finally {
        commit('setLoading', false);
      }
    },
    
    logout({ commit }) {
      commit('clearAuth');
      commit('user/clearUser', null, { root: true });
    }
  },
  
  getters: {
    isAuthenticated(state): boolean {
      return !!state.token;
    }
  }
};

export default authModule;
