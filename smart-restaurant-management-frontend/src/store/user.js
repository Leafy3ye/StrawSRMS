import { defineStore } from 'pinia';
import api from '../utils/api';

export const useUserStore = defineStore('user', {
  state: () => ({
    user: null,
    token: null,
    // 删除这行：isLoggedIn: false,
    showSetupDialog: false
  }),

  getters: {
    isLoggedIn: (state) => !!state.token && !!state.user,
    shopName: (state) => state.user?.restaurantName || '智慧餐饮解决方案',
    needsSetup: (state) => state.user && !state.user.setupCompleted
  },

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
        throw new Error(error.response?.data?.error || '登录失败');
      }
    },
    
    // 添加注册方法
    async register(registerData) {
      try {
        const response = await api.post('/api/users/register', registerData);
        return { success: true, data: response.data };
      } catch (error) {
        throw new Error(error.response?.data || '注册失败');
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
      
      if (token && user && user !== 'undefined' && user !== 'null') {
        try {
          this.token = token;
          this.user = JSON.parse(user);
        } catch (error) {
          console.error('解析用户数据失败:', error);
          // 清除损坏的数据
          localStorage.removeItem('token');
          localStorage.removeItem('user');
        }
      }
    },  // 添加了缺失的逗号
    
    // 店铺初始化设置
    async setupShop(shopData) {
      try {
        const response = await api.post('/api/users/shop-setup', shopData);
        if (response.data.user) {
          this.user = response.data.user;
          this.showSetupDialog = false;
          localStorage.setItem('user', JSON.stringify(this.user));
        }
        return response.data;
      } catch (error) {
        console.error('店铺设置失败:', error);
        throw error;
      }
    },
    
    // 更新店铺信息
    async updateShopInfo(shopData) {
      try {
        const response = await api.put('/api/users/shop-info', shopData);
        if (response.data) {
          this.user = response.data;
          localStorage.setItem('user', JSON.stringify(this.user));
        }
        return response.data;
      } catch (error) {
        console.error('更新店铺信息失败:', error);
        throw error;
      }
    },
    
    // 检查是否需要显示设置弹窗
    checkSetupStatus() {
      if (this.user && !this.user.setupCompleted) {
        this.showSetupDialog = true;
      }
    },
    
    // 删除账户
    async deleteAccount(deleteData) {
      try {
        const response = await api.delete('/api/users/delete-account', {
          data: deleteData
        });
        
        // 删除成功后清除本地数据
        this.logout();
        
        return { success: true, message: response.data.message };
      } catch (error) {
        console.error('删除账户失败:', error);
        throw error;
      }
    }
  }
});