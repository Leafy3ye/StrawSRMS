<template>
  <div id="app">
    <!-- 登录页面：显示导航栏和footer -->
    <div v-if="isLoginPage" class="layout-container">
      <div class="sidebar">
        <Navbar />
      </div>
      <div class="main-container">
        <div class="content">
          <router-view></router-view>
        </div>
        <Footer />
      </div>
    </div>
    
    <!-- 其他页面：直接显示router-view（AdminLayout会处理布局，客户页面独立） -->
    <router-view v-else></router-view>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import Navbar from './components/Navbar.vue'
import Footer from './components/Footer.vue'

const route = useRoute()

// 登录页面需要在App.vue中添加布局
const isLoginPage = computed(() => {
  return route.path === '/login' || route.name === 'Login'
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

.layout-container {
  display: flex;
  height: 100vh;
}

.sidebar {
  width: 240px;
  background-color: #304156;
  box-shadow: 2px 0 6px rgba(0, 21, 41, 0.35);
  z-index: 1001;
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.content {
  flex: 1;
  padding: 20px;
  background-color: #f0f2f5;
  overflow-y: auto;
}
</style>