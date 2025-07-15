import { createRouter, createWebHistory } from "vue-router";
import { useUserStore } from '../store/user'
import AdminLayout from '../layouts/AdminLayout.vue'

const routes = [
  {
    path: "/",
    component: AdminLayout,
    children: [
      { path: "", name: "Home", component: () => import("../views/Home.vue") },
      { path: "list", name: "RecordList", component: () => import("../views/RecordList.vue") },
      { path: "main", name: "Main", component: () => import("../views/Main.vue") },
      { path: "table/:id", name: "TableDetail", component: () => import("../views/TableDetail.vue") },
      { path: "stats/:type", name: "StatDetail", component: () => import("../views/StatDetail.vue") },
      { path: 'members', name: 'Members', component: () => import('../views/Members.vue') },
      { path: 'member-policy', name: 'MemberPolicy', component: () => import('../views/MemberPolicy.vue') },
      { path: 'profile', name: 'UserProfile', component: () => import('../views/UserProfile.vue') },
      
      // 系统设置路由
      { 
        path: 'push-service', 
        name: 'PushService',
        children: [
          { 
            path: 'email-settings', 
            name: 'PushEmailSettings', 
            component: () => import('../views/push-service/EmailSettings.vue') 
          },
          { 
            path: 'sms-settings', 
            name: 'SmsSettings', 
            component: () => import('../views/push-service/SmsSettings.vue') 
          },
          { 
            path: 'email-templates', 
            name: 'EmailTemplates', 
            component: () => import('../views/push-service/EmailTemplates.vue') 
          },
          { 
            path: 'sms-templates', 
            name: 'SmsTemplates', 
            component: () => import('../views/push-service/SmsTemplates.vue') 
          }
        ]
      },
      
      // 系统设置路由（移除邮箱设置）
      { 
        path: 'settings', 
        name: 'Settings',
        children: [
          { 
            path: 'shop', 
            name: 'ShopSettings', 
            component: () => import('../views/settings/ShopSettings.vue') 
          },
          { 
            path: 'theme', 
            name: 'ThemeSettings', 
            component: () => import('../views/settings/ThemeSettings.vue') 
          }
        ]
      }
    ]
  },
  // 超级管理员看板路由
  {
    path: "/super-admin",
    name: "SuperAdminDashboard",
    component: () => import("../views/SuperAdminDashboard.vue"),
    meta: { requiresSuperAdmin: true }
  },
  { path: "/login", name: "Login", component: () => import("../views/Login.vue") },
  { path: "/register", name: "Register", component: () => import("../views/Register.vue") },
  { path: "/forgot-password", name: "ForgotPassword", component: () => import("../views/ForgotPassword.vue") },
  // 客户点餐页面独立，不使用布局
  { path: "/customer/:tableId", name: "CustomerOrder", component: () => import("../views/CustomerOrder.vue") },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to, from, next) => {
  const userStore = useUserStore();
  
  // 定义不需要认证的路径（白名单）
  const publicRoutes = ["/login", "/register", "/forgot-password"];
  // 定义客户端路径（不需要管理员认证）
  const customerRoutes = ["/customer"];
  
  // 检查是否为公开路由
  const isPublicRoute = publicRoutes.includes(to.path);
  // 检查是否为客户端路由
  const isCustomerRoute = customerRoutes.some(path => to.path.startsWith(path));
  
  // 添加延迟检查，确保状态已加载
  setTimeout(() => {
    // 如果用户已登录且试图访问登录或注册页面，根据用户类型重定向
    if (userStore.user && userStore.token && (to.path === "/login" || to.path === "/register")) {
      if (userStore.user.userType === 'SUPER_ADMIN') {
        next("/super-admin");
      } else {
        next("/");
      }
      return;
    }
    
    // 检查超级管理员权限
    if (to.meta.requiresSuperAdmin) {
      if (!userStore.user || !userStore.token) {
        next("/login");
        return;
      }
      if (userStore.user.userType !== 'SUPER_ADMIN') {
        next("/"); // 非超级管理员重定向到普通页面
        return;
      }
    }
    
    // 如果是公开路由或客户端路由，直接通过
    if (isPublicRoute || isCustomerRoute) {
      next();
      return;
    }
    
    // 其他所有路由都需要认证（包括根路径 "/"）
    if (!userStore.user || !userStore.token) {
      next("/login");
    } else {
      // 登录后根据用户类型重定向
      if (to.path === "/" && userStore.user.userType === 'SUPER_ADMIN') {
        next("/super-admin");
      } else {
        next();
      }
    }
  }, 0); // 使用 setTimeout 0 确保在下一个事件循环中执行
});

export default router;