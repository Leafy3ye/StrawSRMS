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
    loadFromStorage() {
      const token = localStorage.getItem('token');
      const user = localStorage.getItem('user');
      
      if (token && user && user !== 'undefined' && user !== 'null') {
        try {
          this.token = token;
          this.user = JSON.parse(user);
          // 加载用户数据后立即应用主题设置
          this.applyUserTheme();
        } catch (error) {
          console.error('解析用户数据失败:', error);
          // 清除损坏的数据
          localStorage.removeItem('token');
          localStorage.removeItem('user');
        }
      }
    },

    async login(credentials) {
      try {
        const response = await api.post('/api/users/login', credentials);
        const { token, user } = response.data;
        
        // 保存 Token 和用户信息
        this.token = token;
        this.user = user;
        localStorage.setItem('token', token);
        localStorage.setItem('user', JSON.stringify(user));
        
        // 登录成功后立即应用主题设置
        this.applyUserTheme();
        
        return { success: true };
      } catch (error) {
        throw new Error(error.response?.data?.error || '登录失败');
      }
    },

    // 添加应用用户主题的方法
    applyUserTheme() {
      if (this.user?.themeSettings) {
        const settings = typeof this.user.themeSettings === 'string' 
          ? JSON.parse(this.user.themeSettings) 
          : this.user.themeSettings;
        
        // 查找对应的主题配置
        const navbarThemes = {
          'default': { primary: '#2c3e50', active: '#409EFF', border: '#434a5a', text: '#ffffff' },
          'blue': { primary: '#1e3a8a', active: '#3b82f6', border: '#1e40af', text: '#ffffff' },
          'green': { primary: '#166534', active: '#22c55e', border: '#15803d', text: '#ffffff' },
          'purple': { primary: '#7c3aed', active: '#a855f7', border: '#8b5cf6', text: '#ffffff' },
          'red': { primary: '#dc2626', active: '#ef4444', border: '#b91c1c', text: '#ffffff' },
          'orange': { primary: '#ea580c', active: '#f97316', border: '#c2410c', text: '#ffffff' }
        };
        
        // 更新背景主题配置，与ThemeSettings.vue保持一致
        const backgroundThemes = [
          { value: 'light', primary: '#f0f2f5', content: '#ffffff', text: '#2c3e50' },
          { value: 'cream', primary: '#faf9f7', content: '#ffffff', text: '#5a5a5a' },
          { value: 'mint', primary: '#f0f9f4', content: '#ffffff', text: '#065f46' },
          { value: 'powder-blue', primary: '#f0f9ff', content: '#ffffff', text: '#0c4a6e' },
          { value: 'lavender', primary: '#faf5ff', content: '#ffffff', text: '#581c87' },
          { value: 'soft-rose', primary: '#fdf2f8', content: '#ffffff', text: '#9d174d' }
        ];
        
        const navTheme = navbarThemes[settings.navbarTheme] || navbarThemes.default;
        const bgTheme = backgroundThemes.find(t => t.value === settings.backgroundTheme) || backgroundThemes[0];
        
        // 应用主题到CSS变量
        const root = document.documentElement;
        root.style.setProperty('--navbar-bg-color', navTheme.primary);
        root.style.setProperty('--navbar-active-color', navTheme.active);
        root.style.setProperty('--navbar-border-color', navTheme.border);
        root.style.setProperty('--navbar-text-color', navTheme.text);
        
        root.style.setProperty('--bg-primary-color', bgTheme.primary);
        root.style.setProperty('--bg-content-color', bgTheme.content);
        root.style.setProperty('--bg-text-color', bgTheme.text);
        
        // 设置根元素背景色
        root.style.backgroundColor = bgTheme.primary;
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
      
      // 重置主题到默认状态
      this.resetThemeToDefault();
    },
    
    // 添加重置主题的方法
    resetThemeToDefault() {
      const root = document.documentElement;
      
      // 重置导航栏主题到默认
      root.style.setProperty('--navbar-bg-color', '#2c3e50');
      root.style.setProperty('--navbar-active-color', '#409EFF');
      root.style.setProperty('--navbar-border-color', '#434a5a');
      root.style.setProperty('--navbar-text-color', '#ffffff');
      
      // 重置背景主题到默认（与ThemeSettings.vue中的light主题一致）
      root.style.setProperty('--bg-primary-color', '#f0f2f5');
      root.style.setProperty('--bg-content-color', '#ffffff');
      root.style.setProperty('--bg-text-color', '#2c3e50');
      
      // 重置根元素背景色
      root.style.backgroundColor = '#f0f2f5';
      
      // 清除任何滤镜效果
      const mainContent = document.querySelector('.main-container');
      if (mainContent) {
        mainContent.style.filter = 'none';
      }
      
      // 清除媒体元素的滤镜
      const mediaElements = document.querySelectorAll('img, video, iframe, canvas');
      mediaElements.forEach(el => {
        el.style.filter = 'none';
      });
    },
    
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