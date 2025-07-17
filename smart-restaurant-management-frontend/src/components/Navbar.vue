<template>
  <div class="sidebar-container" :class="{ collapsed: isCollapsed }" :style="{
    backgroundColor: currentNavbarTheme.primary
  }">
    <!-- Logo区域 -->
    <div class="logo-container" :style="{
      backgroundColor: currentNavbarTheme.primary,
      borderBottomColor: currentNavbarTheme.border || '#434a5a'
    }">
      <div class="logo-content">
        <img :src="logoUrl" alt="logo" class="logo-img" />
        <div v-if="!isCollapsed" class="logo-info">
          <span class="logo-title" :style="{ color: currentNavbarTheme.text }">{{ tenantName || userStore.shopName || 'StrawSRMS' }}</span>
          <!-- 店铺切换下拉菜单 -->
          <el-dropdown v-if="isMultiStoreMode && stores.length > 0" @command="switchStore" class="store-switcher">
            <span class="store-name" :style="{ color: currentNavbarTheme.text }">
              {{ currentStoreName }}
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item
                  v-for="store in stores"
                  :key="store.id"
                  :command="store"
                  :class="{ 'is-active': store.id === currentStoreId }"
                >
                  <el-icon v-if="store.isDefault"><Star /></el-icon>
                  {{ store.storeName }}
                  <span v-if="store.isDefault" class="store-badge">总店</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <!-- 收缩按钮 -->
      <el-button
        class="collapse-btn"
        :icon="isCollapsed ? Expand : Fold"
        @click="toggleCollapse"
        :style="{
          color: currentNavbarTheme.text,
          backgroundColor: 'transparent',
          border: 'none'
        }"
        size="small"
      />
    </div>
    
    <!-- 自定义导航菜单 -->
    <div class="custom-menu">
      <div
        class="menu-item"
        :class="{ active: activeMenu === '/' }"
        @click="navigateTo('/')"
      >
        <el-icon class="menu-icon" :style="{ color: activeMenu === '/' ? '#409EFF' : currentNavbarTheme.text }"><House /></el-icon>
        <span v-if="!isCollapsed" class="menu-text" :style="{ color: activeMenu === '/' ? '#409EFF' : currentNavbarTheme.text }">首页</span>
        <el-tooltip v-if="isCollapsed" content="首页" placement="right">
          <div class="tooltip-trigger"></div>
        </el-tooltip>
      </div>

      <div
        class="menu-item"
        :class="{ active: activeMenu === '/list' }"
        @click="checkAuth('/list')"
      >
        <el-icon class="menu-icon" :style="{ color: activeMenu === '/list' ? '#409EFF' : currentNavbarTheme.text }"><Document /></el-icon>
        <span v-if="!isCollapsed" class="menu-text" :style="{ color: activeMenu === '/list' ? '#409EFF' : currentNavbarTheme.text }">菜单管理</span>
        <el-tooltip v-if="isCollapsed" content="菜单管理" placement="right">
          <div class="tooltip-trigger"></div>
        </el-tooltip>
      </div>

      <div
        class="menu-item"
        :class="{ active: activeMenu === '/main' }"
        @click="checkAuth('/main')"
      >
        <el-icon class="menu-icon" :style="{ color: activeMenu === '/main' ? '#409EFF' : currentNavbarTheme.text }"><Edit /></el-icon>
        <span v-if="!isCollapsed" class="menu-text" :style="{ color: activeMenu === '/main' ? '#409EFF' : currentNavbarTheme.text }">桌位管理</span>
        <el-tooltip v-if="isCollapsed" content="桌位管理" placement="right">
          <div class="tooltip-trigger"></div>
        </el-tooltip>
      </div>
      
      <!-- 会员管理下拉菜单 -->
      <div class="submenu-container">
        <div
          class="menu-item submenu-title"
          :class="{ active: activeMenu.startsWith('/members') || activeMenu === '/member-policy' }"
          @click="toggleSubmenu('members')"
        >
          <el-icon class="menu-icon" :style="{ color: (activeMenu.startsWith('/members') || activeMenu === '/member-policy') ? '#409EFF' : currentNavbarTheme.text }"><User /></el-icon>
          <span v-if="!isCollapsed" class="menu-text" :style="{ color: (activeMenu.startsWith('/members') || activeMenu === '/member-policy') ? '#409EFF' : currentNavbarTheme.text }">会员管理</span>
          <el-icon v-if="!isCollapsed" class="submenu-arrow" :class="{ expanded: expandedMenus.members }" :style="{ color: currentNavbarTheme.text }"><ArrowDown /></el-icon>
          <el-tooltip v-if="isCollapsed" content="会员管理" placement="right">
            <div class="tooltip-trigger"></div>
          </el-tooltip>
        </div>
        <div v-if="!isCollapsed && expandedMenus.members" class="submenu-items">
          <div
            class="submenu-item"
            :class="{ active: activeMenu === '/members' }"
            @click="checkAuth('/members')"
          >
            <span class="submenu-text" :style="{ color: activeMenu === '/members' ? '#409EFF' : currentNavbarTheme.text }">会员</span>
          </div>
          <div
            class="submenu-item"
            :class="{ active: activeMenu === '/member-policy' }"
            @click="checkAuth('/member-policy')"
          >
            <span class="submenu-text" :style="{ color: activeMenu === '/member-policy' ? '#409EFF' : currentNavbarTheme.text }">会员等级管理</span>
          </div>
        </div>
      </div>

      <!-- 推送服务下拉菜单 -->
      <div class="submenu-container">
        <div
          class="menu-item submenu-title"
          :class="{ active: activeMenu.startsWith('/push-service') }"
          @click="toggleSubmenu('pushService')"
        >
          <el-icon class="menu-icon" :style="{ color: activeMenu.startsWith('/push-service') ? '#409EFF' : currentNavbarTheme.text }"><Bell /></el-icon>
          <span v-if="!isCollapsed" class="menu-text" :style="{ color: activeMenu.startsWith('/push-service') ? '#409EFF' : currentNavbarTheme.text }">推送服务</span>
          <el-icon v-if="!isCollapsed" class="submenu-arrow" :class="{ expanded: expandedMenus.pushService }" :style="{ color: currentNavbarTheme.text }"><ArrowDown /></el-icon>
          <el-tooltip v-if="isCollapsed" content="推送服务" placement="right">
            <div class="tooltip-trigger"></div>
          </el-tooltip>
        </div>
        <div v-if="!isCollapsed && expandedMenus.pushService" class="submenu-items">
          <div
            class="submenu-item"
            :class="{ active: activeMenu === '/push-service/email-settings' }"
            @click="checkAuth('/push-service/email-settings')"
          >
            <el-icon class="submenu-icon" :style="{ color: activeMenu === '/push-service/email-settings' ? '#409EFF' : currentNavbarTheme.text }"><Message /></el-icon>
            <span class="submenu-text" :style="{ color: activeMenu === '/push-service/email-settings' ? '#409EFF' : currentNavbarTheme.text }">邮箱设置</span>
          </div>
          <div
            class="submenu-item"
            :class="{ active: activeMenu === '/push-service/sms-settings' }"
            @click="checkAuth('/push-service/sms-settings')"
          >
            <el-icon class="submenu-icon" :style="{ color: activeMenu === '/push-service/sms-settings' ? '#409EFF' : currentNavbarTheme.text }"><ChatDotRound /></el-icon>
            <span class="submenu-text" :style="{ color: activeMenu === '/push-service/sms-settings' ? '#409EFF' : currentNavbarTheme.text }">短信设置</span>
          </div>
          <div
            class="submenu-item"
            :class="{ active: activeMenu === '/push-service/email-templates' }"
            @click="checkAuth('/push-service/email-templates')"
          >
            <el-icon class="submenu-icon" :style="{ color: activeMenu === '/push-service/email-templates' ? '#409EFF' : currentNavbarTheme.text }"><Document /></el-icon>
            <span class="submenu-text" :style="{ color: activeMenu === '/push-service/email-templates' ? '#409EFF' : currentNavbarTheme.text }">邮箱模板</span>
          </div>
          <div
            class="submenu-item"
            :class="{ active: activeMenu === '/push-service/sms-templates' }"
            @click="checkAuth('/push-service/sms-templates')"
          >
            <el-icon class="submenu-icon" :style="{ color: activeMenu === '/push-service/sms-templates' ? '#409EFF' : currentNavbarTheme.text }"><EditPen /></el-icon>
            <span class="submenu-text" :style="{ color: activeMenu === '/push-service/sms-templates' ? '#409EFF' : currentNavbarTheme.text }">短信模板</span>
          </div>
        </div>
      </div>

      <!-- 系统设置下拉菜单 -->
      <div class="submenu-container">
        <div
          class="menu-item submenu-title"
          :class="{ active: activeMenu.startsWith('/settings') }"
          @click="toggleSubmenu('settings')"
        >
          <el-icon class="menu-icon" :style="{ color: activeMenu.startsWith('/settings') ? '#409EFF' : currentNavbarTheme.text }"><Tools /></el-icon>
          <span v-if="!isCollapsed" class="menu-text" :style="{ color: activeMenu.startsWith('/settings') ? '#409EFF' : currentNavbarTheme.text }">系统设置</span>
          <el-icon v-if="!isCollapsed" class="submenu-arrow" :class="{ expanded: expandedMenus.settings }" :style="{ color: currentNavbarTheme.text }"><ArrowDown /></el-icon>
          <el-tooltip v-if="isCollapsed" content="系统设置" placement="right">
            <div class="tooltip-trigger"></div>
          </el-tooltip>
        </div>
        <div v-if="!isCollapsed && expandedMenus.settings" class="submenu-items">
          <div
            class="submenu-item"
            :class="{ active: activeMenu === '/settings/shop' }"
            @click="checkAuth('/settings/shop')"
          >
            <el-icon class="submenu-icon" :style="{ color: activeMenu === '/settings/shop' ? '#409EFF' : currentNavbarTheme.text }"><Shop /></el-icon>
            <span class="submenu-text" :style="{ color: activeMenu === '/settings/shop' ? '#409EFF' : currentNavbarTheme.text }">店铺设置</span>
          </div>
          <div
            class="submenu-item"
            :class="{ active: activeMenu === '/settings/theme' }"
            @click="checkAuth('/settings/theme')"
          >
            <el-icon class="submenu-icon" :style="{ color: activeMenu === '/settings/theme' ? '#409EFF' : currentNavbarTheme.text }"><Brush /></el-icon>
            <span class="submenu-text" :style="{ color: activeMenu === '/settings/theme' ? '#409EFF' : currentNavbarTheme.text }">主题设置</span>
          </div>
        </div>
      </div>

      <!-- 登录菜单项（未登录时显示） - 商业化改造：移除此项 -->
      <!-- <el-menu-item v-if="!isLoggedIn" index="/login" @click="navigateTo('/login')">
        <el-icon><User /></el-icon>
        <span>登录</span>
      </el-menu-item> -->
    </div>

    <!-- 用户信息区域（已登录时显示） -->
    <div v-if="isLoggedIn" class="user-info" :style="{
      backgroundColor: currentNavbarTheme.primary,
      borderTopColor: currentNavbarTheme.border || '#434a5a'
    }">
      <el-dropdown trigger="click" @command="handleCommand">
        <div class="user-avatar-container">
          <div class="user-avatar">
            <el-avatar :size="isCollapsed ? 32 : 40" :src="userStore.user?.avatarUrl">
              <el-icon><User /></el-icon>
            </el-avatar>
          </div>
          <div v-if="!isCollapsed" class="user-details">
            <div class="username" :style="{ color: currentNavbarTheme.text }">{{ userStore.user.username }}</div>
          </div>
          <el-icon v-if="!isCollapsed" class="dropdown-icon" :style="{ color: currentNavbarTheme.text }"><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">
              <el-icon><Setting /></el-icon> 个人信息
            </el-dropdown-item>
            <el-dropdown-item command="logout">
              <el-icon><SwitchButton /></el-icon> 退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <!-- 未登录弹窗提示 -->
    <el-dialog v-model="showLoginPrompt" title="提示" width="30%">
      <p>您需要登录才能使用该功能！</p>
      <template #footer>
        <span class="dialog-footer">
          <el-button type="primary" @click="goToLogin">好的</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from "vue";
import { useRouter } from "vue-router";
import { useUserStore } from '../store/user';
import {
  House, Document, Edit, User, SwitchButton, Setting, ArrowDown,
  Tools, Message, Shop, Brush, Bell, ChatDotRound, EditPen, Fold, Expand, Star
} from "@element-plus/icons-vue";
// 导入logo图片
import logoUrl from '../assets/logo.jpg';
import api from '../utils/api';
import { ElMessage } from 'element-plus';

const router = useRouter();
const userStore = useUserStore();
const activeMenu = computed(() => {
  const path = router.currentRoute.value.path;
  return path;
});
const isLoggedIn = computed(() => !!userStore.user);

const showLoginPrompt = ref(false);
let redirectPath = "";

// 收缩状态
const isCollapsed = ref(false);

// 子菜单展开状态
const expandedMenus = ref({
  members: false,
  pushService: false,
  settings: false
});

// 店铺相关数据
const stores = ref([]);
const currentStoreId = ref(null);
const tenantName = ref('');

// 判断是否为多店铺模式
const isMultiStoreMode = computed(() => {
  return userStore.user?.storeMode === 'multi';
});

// 当前店铺名称
const currentStoreName = computed(() => {
  if (!stores.value.length) return '加载中...';
  const currentStore = stores.value.find(store => store.id === currentStoreId.value);
  return currentStore ? currentStore.storeName : '未知店铺';
});

// 切换收缩状态
const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value;

  // 收缩时关闭所有子菜单
  if (isCollapsed.value) {
    expandedMenus.value = {
      members: false,
      pushService: false,
      settings: false
    };
  }

  // 触发事件通知主布局组件调整内容区域
  window.dispatchEvent(new CustomEvent('sidebar-collapse', {
    detail: { collapsed: isCollapsed.value }
  }));
};

// 切换子菜单展开状态
const toggleSubmenu = (menuKey) => {
  if (!isCollapsed.value) {
    expandedMenus.value[menuKey] = !expandedMenus.value[menuKey];
  }
};

// 导航栏主题选项
const navbarThemes = {
  default: {
    primary: '#304156',
    text: '#bfcbd9',
    active: '#409EFF',
    border: '#434a5a'
  },
  dark: {
    primary: '#1f2937',
    text: '#d1d5db',
    active: '#60a5fa',
    border: '#374151'
  },
  cream: {
    primary: '#faf9f7',
    text: '#5a5a5a',
    active: '#409EFF',
    border: '#e8e6e3'
  }
}

// 当前导航栏主题
const currentNavbarTheme = ref(navbarThemes.default)

// 加载用户主题设置
const loadUserTheme = () => {
  if (userStore.user?.themeSettings) {
    const themeSettings = typeof userStore.user.themeSettings === 'string' 
      ? JSON.parse(userStore.user.themeSettings) 
      : userStore.user.themeSettings
    
    const navbarTheme = themeSettings.navbarTheme || 'default'
    currentNavbarTheme.value = navbarThemes[navbarTheme] || navbarThemes.default
    
    // 应用CSS变量到全局
    nextTick(() => {
      const root = document.documentElement
      root.style.setProperty('--navbar-bg-color', currentNavbarTheme.value.primary)
      root.style.setProperty('--navbar-text-color', currentNavbarTheme.value.text)
      root.style.setProperty('--navbar-active-color', currentNavbarTheme.value.active)
      root.style.setProperty('--navbar-border-color', currentNavbarTheme.value.border)
    })
  } else {
    // 重置为默认主题
    currentNavbarTheme.value = navbarThemes.default
  }
}

// 监听用户数据变化
watch(() => userStore.user, (newUser) => {
  if (newUser) {
    loadUserTheme()
  } else {
    // 用户登出时重置为默认主题
    currentNavbarTheme.value = navbarThemes.default
  }
}, { deep: true, immediate: true })

// 组件挂载时加载主题
onMounted(() => {
  loadUserTheme()
  // 如果是多店铺模式，获取店铺列表
  if (isMultiStoreMode.value) {
    fetchStores()
  }
  // 获取租户名称
  fetchTenantName()

  // 监听租户更新事件
  window.addEventListener('tenant-updated', fetchTenantName)
})

// 组件卸载时清理事件监听
onUnmounted(() => {
  window.removeEventListener('tenant-updated', fetchTenantName)
})

// 直接导航的方法
const navigateTo = (path) => {
  router.push(path);
};

// 需要权限验证的导航
const checkAuth = (path) => {
  if (isLoggedIn.value) {
    router.push(path);
  } else {
    showLoginPrompt.value = true;
    redirectPath = path;
  }
};

const goToLogin = () => {
  showLoginPrompt.value = false;
  router.push("/login");
};

const handleCommand = async (command) => {
  if (command === 'logout') {
    userStore.logout();
    // 先跳转到登录页面
    await router.push("/login");
    // 跳转完成后再重置主题，避免闪烁
    setTimeout(() => {
      userStore.resetThemeToDefault();
    }, 100);
  } else if (command === 'profile') {
    router.push("/profile");
  }
};

// 获取店铺列表
const fetchStores = async () => {
  try {
    const response = await api.get('/api/stores');
    stores.value = response.data;

    // 设置当前店铺ID（优先从用户的currentStoreId获取）
    if (userStore.user?.currentStoreId) {
      currentStoreId.value = userStore.user.currentStoreId;
    } else if (userStore.user?.storeId) {
      currentStoreId.value = userStore.user.storeId;
    } else if (stores.value.length > 0) {
      // 如果没有当前店铺ID，默认选择第一个店铺
      currentStoreId.value = stores.value[0].id;
    }
  } catch (error) {
    console.error('获取店铺列表失败:', error);
  }
};

// 切换店铺
const switchStore = async (store) => {
  try {
    await api.post(`/api/stores/${store.id}/switch`);
    ElMessage.success(`已切换到 ${store.storeName}`);

    // 更新当前店铺ID
    currentStoreId.value = store.id;

    // 更新用户信息中的当前店铺ID
    if (userStore.user) {
      userStore.user.currentStoreId = store.id;
      localStorage.setItem('user', JSON.stringify(userStore.user));
    }

    // 刷新页面以更新数据
    window.location.reload();
  } catch (error) {
    ElMessage.error('切换店铺失败: ' + (error.response?.data || error.message));
  }
};

// 获取租户名称
const fetchTenantName = async () => {
  try {
    const response = await api.get('/api/tenants/current');
    tenantName.value = response.data.tenantName || '';
  } catch (error) {
    console.error('获取租户名称失败:', error);
  }
};
</script>

<style scoped>
.sidebar-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  width: 250px;
  transition: width 0.3s ease;
}

.sidebar-container.collapsed {
  width: 64px;
}

.logo-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 12px;
  border-bottom: 1px solid;
}

.collapsed .logo-container {
  flex-direction: column;
  justify-content: center;
  padding: 12px 8px;
  gap: 6px;
}

.logo-content {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 0;
  max-width: calc(100% - 32px); /* 为收缩按钮留出空间 */
}

.logo-info {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  min-width: 0;
  flex: 1;
}

.collapsed .logo-content {
  flex-direction: column;
  align-items: center;
  gap: 4px;
  max-width: 100%;
}

.collapse-btn {
  flex-shrink: 0;
  padding: 4px;
  min-height: auto;
  width: 24px;
  height: 24px;
}

.collapsed .collapse-btn {
  margin: 0;
}

.logo-img {
  width: 50px;
  height: 50px;
  margin-right: 12px;
  border-radius: 4px;
  flex-shrink: 0;
  transition: all 0.3s ease;
}

.collapsed .logo-img {
  width: 36px;
  height: 36px;
  margin-right: 0;
}

.logo-title {
  font-size: 18px;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 150px; /* 限制最大宽度，防止与按钮重叠 */
  margin-bottom: 2px;
}

.store-switcher {
  margin-top: 2px;
}

.store-name {
  font-size: 12px;
  opacity: 0.8;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 2px 4px;
  border-radius: 4px;
  transition: all 0.2s ease;
  max-width: 150px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.store-name:hover {
  opacity: 1;
  background-color: rgba(255, 255, 255, 0.1);
}

.store-badge {
  font-size: 10px;
  background-color: #f56c6c;
  color: white;
  padding: 1px 4px;
  border-radius: 2px;
  margin-left: 4px;
  flex-shrink: 0;
}

:deep(.el-dropdown-menu__item.is-active) {
  background-color: #409eff;
  color: white;
}

/* 自定义菜单样式 */
.custom-menu {
  flex: 1;
  padding: 8px 0;
  overflow-y: auto;
  overflow-x: hidden;
}

/* 自定义滚动条样式 */
.custom-menu::-webkit-scrollbar {
  width: 6px;
}

.custom-menu::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 3px;
}

.custom-menu::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.3);
  border-radius: 3px;
}

.custom-menu::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.5);
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  cursor: pointer;
  transition: background-color 0.2s ease, color 0.2s ease;
  position: relative;
  min-height: 44px;
  background-color: transparent;
}

.menu-item:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.menu-item.active {
  background-color: transparent;
}

.menu-item.active .menu-icon,
.menu-item.active .menu-text {
  color: #409EFF !important;
}

.menu-icon {
  font-size: 18px;
  margin-right: 12px;
  flex-shrink: 0;
  width: 18px;
  text-align: center;
}

.collapsed .menu-icon {
  margin-right: 0;
}

.menu-text {
  font-size: 14px;
  white-space: nowrap;
  flex: 1;
}

.tooltip-trigger {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
}

/* 子菜单样式 */
.submenu-container {
  position: relative;
}

.submenu-title {
  justify-content: space-between;
}

.submenu-arrow {
  font-size: 12px;
  transition: transform 0.2s ease;
  margin-left: 8px;
}

.submenu-arrow.expanded {
  transform: rotate(180deg);
}

.submenu-items {
  background-color: rgba(255, 255, 255, 0.05);
  animation: slideDown 0.2s ease;
  overflow: hidden;
}

.submenu-item {
  display: flex;
  align-items: center;
  padding: 10px 16px 10px 48px;
  cursor: pointer;
  transition: background-color 0.2s ease, color 0.2s ease;
  min-height: 40px;
  background-color: transparent;
}

.submenu-item:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.submenu-item.active {
  background-color: transparent;
}

.submenu-item.active .submenu-icon,
.submenu-item.active .submenu-text {
  color: #409EFF !important;
}

.submenu-icon {
  font-size: 16px;
  margin-right: 8px;
  flex-shrink: 0;
  width: 16px;
  text-align: center;
}

.submenu-text {
  font-size: 13px;
  white-space: nowrap;
}

@keyframes slideDown {
  from {
    opacity: 0;
    max-height: 0;
  }
  to {
    opacity: 1;
    max-height: 300px;
  }
}

.user-info {
  padding: 16px;
  border-top: 1px solid;
}

.collapsed .user-info {
  padding: 16px 8px;
}

.user-avatar-container {
  display: flex;
  align-items: center;
  cursor: pointer;
  justify-content: flex-start;
}

.collapsed .user-avatar-container {
  justify-content: center;
}

.user-avatar {
  margin-right: 12px;
}

.collapsed .user-avatar {
  margin-right: 0;
}

.user-details {
  flex: 1;
  overflow: hidden;
}

.username {
  font-size: 14px;
  font-weight: 500;
}

.dropdown-icon {
  margin-left: 8px;
}

/* 添加子菜单样式 */
.el-sub-menu .el-menu-item {
  padding-left: 40px !important;
  min-width: 200px;
}
</style>