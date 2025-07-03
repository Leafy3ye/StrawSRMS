<template>
  <div class="login-page">
    <el-card class="login-card">
      <!-- Logo 图片 -->
      <div class="logo-container">
        <img :src="logoUrl" alt="Logo" class="logo-image" />
      </div>

      <!-- 标题 -->
      <h2 class="login-title">智慧餐饮综合管理系统</h2>

      <!-- 登录表单 -->
      <el-form :model="form" label-width="0" ref="loginForm" class="login-form" :rules="rules">
        <!-- 用户名输入框 -->
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            clearable
          >
            <template #prefix>
              <el-icon class="input-icon">
                <User />
              </el-icon>
            </template>
          </el-input>
        </el-form-item>

        <!-- 密码输入框 -->
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            clearable
            show-password
          >
            <template #prefix>
              <el-icon class="input-icon">
                <Lock />
              </el-icon>
            </template>
          </el-input>
        </el-form-item>

        <!-- 登录按钮 -->
        <el-form-item>
          <el-button
            type="primary"
            @click="onLogin"
            style="width: 100%;"
            :loading="loading"
            :icon="Promotion"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { useUserStore } from "../store/user";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { User, Lock, Promotion } from "@element-plus/icons-vue";
import logoUrl from '../assets/logo.jpg';

const userStore = useUserStore();
const router = useRouter();
const loading = ref(false);

const form = reactive({
  username: "",
  password: "",
});

const rules = {
  username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }],
};

const loginForm = ref(null);

// 登录操作
const onLogin = () => {
  loginForm.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      try {
        await userStore.login(form);
        ElMessage.success("登录成功！");
        router.push("/"); // 跳转到主页面
      } catch (error) {
        ElMessage.error(error.message || "用户名或密码错误！");
      } finally {
        loading.value = false;
      }
    }
  });
};
</script>

<style scoped>
/* 页面整体布局 */
.login-page {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
}

/* 登录卡片样式 */
.login-card {
  width: 400px;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

/* Logo 容器 */
.logo-container {
  text-align: center;
  margin-bottom: 20px;
}

/* Logo 图片 */
.logo-image {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
}

/* 标题样式 */
.login-title {
  text-align: center;
  font-size: 24px;
  color: #303133;
  margin-bottom: 30px;
}

/* 登录表单 */
.login-form {
  margin-top: 20px;
}

/* 输入框图标 */
.input-icon {
  color: #909399;
}
</style>
