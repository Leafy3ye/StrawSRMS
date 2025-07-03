//入口文件
import { createApp } from "vue";
import App from "./App.vue";
import ElementPlus from "element-plus";
import "element-plus/dist/index.css";
import { createPinia } from "pinia";
import router from "./router";
import { useUserStore } from "./store/user"; // 引入 useUserStore

const app = createApp(App);

// 创建 Pinia 实例
const pinia = createPinia();

// 注册插件
app.use(ElementPlus);
app.use(pinia);
app.use(router);

// 在 Pinia 完成注册后，访问 Store
const userStore = useUserStore(); 
userStore.loadUser();

// 挂载应用
app.mount("#app");
