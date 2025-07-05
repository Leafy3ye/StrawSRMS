<template>
  <div class="layout-container">
    <div class="sidebar">
      <Navbar />
    </div>
    <div class="main-container">
      <div class="content">
        <router-view></router-view>
      </div>
      <Footer />
    </div>
    <!-- 店铺设置弹窗 -->
    <ShopSetupDialog />
  </div>
</template>

<script setup>
import Navbar from '../components/Navbar.vue';
import Footer from '../components/Footer.vue';
import ShopSetupDialog from '../components/ShopSetupDialog.vue';
import { useUserStore } from '../store/user';
import { onMounted } from 'vue';

const userStore = useUserStore();

// 页面加载时检查是否需要显示设置弹窗
onMounted(() => {
  userStore.checkSetupStatus();
});
</script>

<style scoped>
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
  background-color: var(--bg-content-color, #f0f2f5);
}

.content {
  flex: 1;
  padding: 20px;
  background-color: var(--bg-content-color, #f0f2f5);
  overflow-y: auto;
}
</style>