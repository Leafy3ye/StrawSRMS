import { defineStore } from 'pinia';
import api from '../utils/api'; // 确保导入 api 工具

export const useUserStore = defineStore('user', {
  state: () => ({
    user: null,
    token: null
  }),
  
  actions: {
    async login(credentials) {
      try {
        const response = await api.post('/api/users/login', credentials);
        const { token, user } = response.data;
        
        // 保存 Token 和用户信息
        this.token = token;
        this.user = user;
        localStorage.setItem('token', token);
        localStorage.setItem('user', JSON.stringify(user));
        
        return { success: true };
      } catch (error) {
        return { success: false, error: error.response?.data?.error || '登录失败' };
      }
    },
    
    logout() {
      this.token = null;
      this.user = null;
      localStorage.removeItem('token');
      localStorage.removeItem('user');
    },
    
    loadFromStorage() {
      const token = localStorage.getItem('token');
      const user = localStorage.getItem('user');
      
      if (token && user) {
        this.token = token;
        this.user = JSON.parse(user);
      }
    }
  }
});
