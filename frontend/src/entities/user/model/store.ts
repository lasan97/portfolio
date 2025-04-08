import { Module } from 'vuex';
import { api } from '@/shared/api';
import { User, UserState } from './types';

const userModule: Module<UserState, any> = {
  namespaced: true,
  
  state: () => ({
    user: null,
    loading: false,
    error: null
  }),
  
  mutations: {
    setUser(state, user: User) {
      state.user = user;
    },
    setLoading(state, loading: boolean) {
      state.loading = loading;
    },
    setError(state, error: string | null) {
      state.error = error;
    },
    clearUser(state) {
      state.user = null;
    }
  },
  
  actions: {
    async fetchUser({ commit }, userId: number) {
      commit('setLoading', true);
      commit('setError', null);
      
      try {
        const response = await api.get(`/api/users/${userId}`);
        commit('setUser', response.data.data);
      } catch (error: any) {
        commit('setError', error.response?.data?.message || '사용자 정보를 가져오는데 실패했습니다.');
      } finally {
        commit('setLoading', false);
      }
    }
  },
  
  getters: {
    isAuthenticated(state): boolean {
      return !!state.user;
    }
  }
};

export default userModule;
