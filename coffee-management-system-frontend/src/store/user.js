//用户状态
import { defineStore } from "pinia";
import api from "../utils/api"; // 使用统一的 api 配置

// 删除这行硬编码的 API_URL
// const API_URL = "http://localhost:8080/api";

export const useUserStore = defineStore("user", {
  state: () => {
    return {
      user: JSON.parse(localStorage.getItem("user")) || null, // 当前登录用户信息
    };
  },

  actions: {
    // 用户登录
    async login(credentials) {
      try {
        // 使用统一的 api 实例
        const response = await api.post(`/api/users/login`, credentials);
        const userData = response.data;
        
        // 保存用户信息到状态和localStorage
        this.user = userData;
        localStorage.setItem("user", JSON.stringify(userData));
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
    },

    // 获取用户信息
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
      }
    },
  },
});
