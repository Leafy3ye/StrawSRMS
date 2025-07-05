<template>
  <div class="sidebar-container" :style="{
    backgroundColor: currentNavbarTheme.primary
  }">
    <!-- Logo区域 -->
    <div class="logo-container" :style="{
      backgroundColor: currentNavbarTheme.primary,
      borderBottomColor: currentNavbarTheme.border || '#434a5a'
    }">
      <img :src="logoUrl" alt="logo" class="logo-img" />
      <span class="logo-title" :style="{ color: currentNavbarTheme.text }">{{ userStore.shopName || '智慧餐饮解决方案' }}</span>
    </div>
    
    <!-- 导航菜单 -->
    <el-menu
      mode="vertical"
      :default-active="activeMenu"
      class="sidebar-menu"
      :background-color="currentNavbarTheme.primary"
      :text-color="currentNavbarTheme.text"
      :active-text-color="currentNavbarTheme.active"
    >
      <el-menu-item index="/" @click="navigateTo('/')">
        <el-icon><House /></el-icon>
        <span>首页</span>
      </el-menu-item>
      
      <el-menu-item index="/list" @click="checkAuth('/list')">
        <el-icon><Document /></el-icon>
        <span>菜单管理</span>
      </el-menu-item>
      
      <el-menu-item index="/main" @click="checkAuth('/main')">
        <el-icon><Edit /></el-icon>
        <span>桌位管理</span>
      </el-menu-item>
      
            <!-- 会员管理下拉菜单 -->
      <el-sub-menu index="/members">
        <template #title>
          <el-icon><User /></el-icon>
          <span>会员管理</span>
        </template>
        <el-menu-item index="/members" @click="checkAuth('/members')">
          <span>会员</span>
        </el-menu-item>
        <el-menu-item index="/member-policy" @click="checkAuth('/member-policy')">
          <span>会员政策</span>
        </el-menu-item>
      </el-sub-menu>

      <!-- 系统设置下拉菜单 -->
      <el-sub-menu index="/settings">
        <template #title>
          <el-icon><Tools /></el-icon>
          <span>系统设置</span>
        </template>
        <el-menu-item index="/settings/email" @click="checkAuth('/settings/email')">
          <el-icon><Message /></el-icon>
          <span>邮箱设置</span>
        </el-menu-item>
        <el-menu-item index="/settings/shop" @click="checkAuth('/settings/shop')">
          <el-icon><Shop /></el-icon>
          <span>店铺设置</span>
        </el-menu-item>
        <el-menu-item index="/settings/theme" @click="checkAuth('/settings/theme')">
          <el-icon><Brush /></el-icon>
          <span>主题设置</span>
        </el-menu-item>
      </el-sub-menu>

      <!-- 登录菜单项（未登录时显示） - 商业化改造：移除此项 -->
      <!-- <el-menu-item v-if="!isLoggedIn" index="/login" @click="navigateTo('/login')">
        <el-icon><User /></el-icon>
        <span>登录</span>
      </el-menu-item> -->
    </el-menu>

    <!-- 用户信息区域（已登录时显示） -->
    <div v-if="isLoggedIn" class="user-info" :style="{
      backgroundColor: currentNavbarTheme.primary,
      borderTopColor: currentNavbarTheme.border || '#434a5a'
    }">
      <el-dropdown trigger="click" @command="handleCommand">
        <div class="user-avatar-container">
          <div class="user-avatar">
            <el-avatar :size="40" :src="userStore.user?.avatarUrl">
              <el-icon><User /></el-icon>
            </el-avatar>
          </div>
          <div class="user-details">
            <div class="username" :style="{ color: currentNavbarTheme.text }">{{ userStore.user.username }}</div>
          </div>
          <el-icon class="dropdown-icon" :style="{ color: currentNavbarTheme.text }"><ArrowDown /></el-icon>
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
import { ref, computed, onMounted, watch, nextTick } from "vue";
import { useRouter } from "vue-router";
import { useUserStore } from '../store/user';
import { 
  House, Document, Edit, User, SwitchButton, Setting, ArrowDown,
  Tools, Message, Shop, Brush
} from "@element-plus/icons-vue";
// 导入logo图片
import logoUrl from '../assets/logo.jpg';

const router = useRouter();
const userStore = useUserStore();
const activeMenu = computed(() => {
  const path = router.currentRoute.value.path;
  return path;
});
const isLoggedIn = computed(() => !!userStore.user);

const showLoginPrompt = ref(false);
let redirectPath = "";

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

const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout();
    router.push("/login");
  } else if (command === 'profile') {
    router.push("/profile");
  }
};
</script>

<style scoped>
.sidebar-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  transition: background-color 0.3s ease;
}

.logo-container {
  display: flex;
  align-items: center;
  padding: 20px 16px;
  border-bottom: 1px solid;
  transition: all 0.3s ease;
}

.logo-img {
  width: 32px;
  height: 32px;
  margin-right: 12px;
  border-radius: 4px;
}

.logo-title {
  font-size: 18px;
  font-weight: 600;
  white-space: nowrap;
  transition: color 0.3s ease;
}

.sidebar-menu {
  flex: 1;
  border: none;
  width: 100%;
}

.sidebar-menu .el-menu-item {
  height: 50px;
  line-height: 50px;
  padding: 0 20px;
  margin: 0;
  border-radius: 0;
}

.sidebar-menu .el-menu-item span {
  margin-left: 8px;
}

.user-info {
  padding: 16px;
  border-top: 1px solid;
  transition: all 0.3s ease;
}

.user-avatar-container {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.user-avatar {
  margin-right: 12px;
}

.user-details {
  flex: 1;
}

.username {
  font-size: 14px;
  font-weight: 500;
  transition: color 0.3s ease;
}

.dropdown-icon {
  margin-left: 8px;
  transition: color 0.3s ease;
}

/* 添加子菜单样式 */
.el-sub-menu .el-menu-item {
  padding-left: 40px !important;
  min-width: 200px;
}
</style>