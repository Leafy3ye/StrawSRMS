//用户状态
import { defineStore } from "pinia";
import api from "../utils/api"; // 使用统一的 api 配置

export const useUserStore = defineStore("user", {
  state: () => {
    return {
      user: JSON.parse(localStorage.getItem("user")) || null, // 当前登录用户信息
    };
  },

  getters: {
    // 获取用户UUID - 商业化系统的核心标识
    userUuid: (state) => state.user?.uuid,
    // 检查用户是否已登录
    isLoggedIn: (state) => !!state.user && !!state.user.uuid,
  },

  actions: {
    // 用户登录
    async login(credentials) {
      try {
        // 使用统一的 api 实例
        const response = await api.post(`/api/users/login`, credentials);
        const userData = response.data;
        
        // 验证返回的用户数据包含UUID
        if (!userData.uuid) {
          throw new Error("用户数据缺少UUID，请联系管理员");
        }
        
        // 保存用户信息到状态和localStorage
        this.user = userData;
        localStorage.setItem("user", JSON.stringify(userData));
        
        // 记录用户UUID到本地存储（用于离线识别）
        localStorage.setItem("userUuid", userData.uuid);
        
        return userData;
      } catch (error) {
        console.error("登录失败:", error);
        throw new Error(error.response?.data || "用户名或密码错误！");
      }
    },

    // 用户注销
    logout() {
      this.user = null;
      localStorage.removeItem("user"); // 移除当前用户信息
      localStorage.removeItem("userUuid"); // 移除UUID
    },

    // 获取用户信息（通过ID）
    async getUserInfo() {
      if (!this.user) return null;
      
      try {
        const response = await api.get(`/api/users/${this.user.id}`);
        const userData = response.data;
        
        // 更新用户信息
        this.user = userData;
        localStorage.setItem("user", JSON.stringify(userData));
        return userData;
      } catch (error) {
        console.error("获取用户信息失败:", error);
        return null;
      }
    },

    // 通过UUID获取用户信息 - 新增方法
    async getUserInfoByUuid(uuid) {
      try {
        const response = await api.get(`/api/users/uuid/${uuid}`);
        const userData = response.data;
        
        // 更新用户信息
        this.user = userData;
        localStorage.setItem("user", JSON.stringify(userData));
        return userData;
      } catch (error) {
        console.error("通过UUID获取用户信息失败:", error);
        return null;
      }
    },

    // 更新用户信息
    async updateUserInfo(updateData) {
      if (!this.user) return null;
      
      try {
        const response = await api.put(`/api/users/${this.user.id}`, updateData);
        const userData = response.data;
        
        // 更新用户信息
        this.user = userData;
        localStorage.setItem("user", JSON.stringify(userData));
        return userData;
      } catch (error) {
        console.error("更新用户信息失败:", error);
        throw new Error(error.response?.data || "更新用户信息失败！");
      }
    },

    // 加载用户状态
    loadUser() {
      const savedUser = localStorage.getItem("user");
      if (savedUser) {
        this.user = JSON.parse(savedUser);
        
        // 验证用户数据完整性
        if (!this.user.uuid) {
          console.warn("用户数据缺少UUID，清除本地数据");
          this.logout();
        }
      }
    },

    // 恢复用户会话（通过UUID）- 商业化功能
    async restoreUserSession() {
      const savedUuid = localStorage.getItem("userUuid");
      if (savedUuid && !this.user) {
        try {
          await this.getUserInfoByUuid(savedUuid);
        } catch (error) {
          console.error("恢复用户会话失败:", error);
          this.logout();
        }
      }
    },

    // 用户注册
    async register(registerData) {
      try {
        const response = await api.post(`/api/users/register`, registerData);
        const userData = response.data;
        
        // 注册成功后不自动登录，让用户手动登录
        return userData;
      } catch (error) {
        console.error("注册失败:", error);
        throw new Error(error.response?.data || "注册失败！");
      }
    },
  },
});
