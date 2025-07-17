<template>
  <div class="layout-container">
    <div class="sidebar" :class="{ collapsed: sidebarCollapsed }">
      <Navbar />
    </div>
    <div class="main-container" :class="{ 'sidebar-collapsed': sidebarCollapsed }">
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
import { onMounted, onUnmounted, ref } from 'vue';

const userStore = useUserStore();
const sidebarCollapsed = ref(false);

// 监听侧边栏收缩事件
const handleSidebarCollapse = (event) => {
  sidebarCollapsed.value = event.detail.collapsed;
};

// 页面加载时检查是否需要显示设置弹窗
onMounted(() => {
  userStore.checkSetupStatus();
  // 监听侧边栏收缩事件
  window.addEventListener('sidebar-collapse', handleSidebarCollapse);
});

onUnmounted(() => {
  // 清理事件监听器
  window.removeEventListener('sidebar-collapse', handleSidebarCollapse);
});
</script>

<style scoped>
.layout-container {
  position: relative;
  height: 100vh;
}

.sidebar {
  position: fixed;
  left: 0;
  top: 0;
  width: 250px;
  height: 100vh;
  background-color: #304156;
  box-shadow: 2px 0 6px rgba(0, 21, 41, 0.35);
  z-index: 1001;
  transition: width 0.3s ease;
}

.sidebar.collapsed {
  width: 64px;
}

.main-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
  background-color: var(--bg-content-color, #f0f2f5);
  margin-left: 250px;
  transition: margin-left 0.3s ease;
}

.main-container.sidebar-collapsed {
  margin-left: 64px;
}

.content {
  flex: 1;
  padding: 20px;
  background-color: var(--bg-content-color, #f0f2f5);
  overflow-y: auto;
}
</style>