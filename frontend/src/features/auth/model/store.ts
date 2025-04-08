import { Module } from 'vuex';
import { api } from '@/shared/api';
import { AuthState, TokenResponse } from './types';

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
    async handleTokenFromCallback({ commit, dispatch }, token: string) {
      commit('setToken', token);
      await dispatch('fetchCurrentUser');
    },
    
    async fetchCurrentUser({ commit }) {
      commit('setLoading', true);
      commit('setError', null);
      
      try {
        const response = await api.get('/api/users/me');
        commit('user/setUser', response.data.data, { root: true });
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
