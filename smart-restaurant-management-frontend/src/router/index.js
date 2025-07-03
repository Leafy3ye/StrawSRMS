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
        path: 'settings', 
        name: 'Settings',
        children: [
          { 
            path: 'email', 
            name: 'EmailSettings', 
            component: () => import('../views/settings/EmailSettings.vue') 
          },
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
  { path: "/login", name: "Login", component: () => import("../views/Login.vue") },
  { path: "/register", name: "Register", component: () => import("../views/Register.vue") },
  // 客户点餐页面独立，不使用布局
  { path: "/customer/:tableId", name: "CustomerOrder", component: () => import("../views/CustomerOrder.vue") },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to, from, next) => {
  const userStore = useUserStore();
  const authRequired = ["/list", "/add", "/cart", "/stats", "/members", "/member-policy", "/profile", "/settings"];

  if (authRequired.some(path => to.path.startsWith(path)) && !userStore.user) {
    next("/login");
  } else {
    next();
  }
});

export default router;
