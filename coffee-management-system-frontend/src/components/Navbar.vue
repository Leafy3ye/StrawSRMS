<template>
  <div class="sidebar-container">
    <!-- Logo区域 -->
    <div class="logo-container">
      <img :src="logoUrl" alt="logo" class="logo-img" />
      <span class="logo-title">智慧餐饮解决方案</span>
    </div>
    
    <!-- 导航菜单 -->
    <el-menu
      mode="vertical"
      :default-active="activeMenu"
      class="sidebar-menu"
      background-color="#304156"
      text-color="#bfcbd9"
      active-text-color="#409EFF"
    >
      <el-menu-item index="/" @click="navigateTo('/')">
        <el-icon><House /></el-icon>
        <span>首页</span>
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
      
      <el-menu-item index="/list" @click="checkAuth('/list')">
        <el-icon><Document /></el-icon>
        <span>菜单管理</span>
      </el-menu-item>
      
      <el-menu-item index="/main" @click="checkAuth('/main')">
        <el-icon><Edit /></el-icon>
        <span>桌位管理</span>
      </el-menu-item>

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

      <!-- 登录菜单项（未登录时显示） -->
      <el-menu-item v-if="!isLoggedIn" index="/login" @click="navigateTo('/login')">
        <el-icon><User /></el-icon>
        <span>登录</span>
      </el-menu-item>
    </el-menu>

    <!-- 用户信息区域（已登录时显示） -->
    <div v-if="isLoggedIn" class="user-info">
      <el-dropdown trigger="click" @command="handleCommand">
        <div class="user-avatar-container">
          <div class="user-avatar">
            <el-avatar :size="40" :src="userStore.user?.avatarUrl">
              <el-icon><User /></el-icon>
            </el-avatar>
          </div>
          <div class="user-details">
            <div class="username">{{ userStore.user.username }}</div>
          </div>
          <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
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
import { ref, computed } from "vue";
import { useRouter } from "vue-router";
import { useUserStore } from "../store/user";
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
  background-color: #304156;
}

.logo-container {
  display: flex;
  align-items: center;
  padding: 20px 16px;
  background-color: #2b2f3a;
  border-bottom: 1px solid #434a5a;
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
  color: #ffffff;
  white-space: nowrap;
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

.sidebar-menu .el-menu-item:hover {
  background-color: #263445 !important;
}

.sidebar-menu .el-menu-item.is-active {
  background-color: #409EFF !important;
  border-right: 3px solid #409EFF;
  color: #ffffff !important;
}

.sidebar-menu .el-menu-item span {
  margin-left: 8px;
}

.user-info {
  padding: 16px;
  border-top: 1px solid #434a5a;
  background-color: #2b2f3a;
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
  color: #ffffff;
  font-size: 14px;
  font-weight: 500;
}

.dropdown-icon {
  color: #bfcbd9;
  margin-left: 8px;
}

/* 添加子菜单样式 */
.el-sub-menu .el-menu-item {
  padding-left: 40px !important;
  min-width: 200px;
}

.el-sub-menu .el-menu-item:hover {
  background-color: #263445 !important;
}

.el-sub-menu .el-menu-item.is-active {
  background-color: #409EFF !important;
  color: #ffffff !important;
}
</style>