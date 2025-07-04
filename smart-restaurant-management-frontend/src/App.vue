<template>
  <div id="app">
    <!-- 登录页面：不显示导航栏，只显示登录内容 -->
    <div v-if="isLoginPage" class="login-layout">
      <router-view></router-view>
    </div>
    
    <!-- 其他页面：使用AdminLayout布局 -->
    <router-view v-else></router-view>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

// 登录页面单独处理，不显示导航栏
const isLoginPage = computed(() => {
  return route.path === '/login' || route.name === 'Login' || 
         route.path === '/register' || route.name === 'Register' ||
         route.path === '/forgot-password' || route.name === 'ForgotPassword'
})
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  color: #2c3e50;
  margin: 0;
  height: 100vh;
  overflow: hidden;
}

.login-layout {
  height: 100vh;
  width: 100vw;
}
</style>