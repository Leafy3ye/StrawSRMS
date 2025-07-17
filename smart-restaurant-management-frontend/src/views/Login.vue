<template>
  <div class="login-page">
    <el-card class="login-card">
      <!-- Logo 图片 -->
      <div class="logo-container">
        <img :src="logoUrl" alt="Logo" class="logo-image" />
      </div>

      <!-- 标题 -->
      <h2 class="login-title">StrawSRMS</h2>
      <p class="login-subtitle">智慧餐饮综合管理系统</p>

      <!-- 登录类型提示 -->
      <div class="login-type-indicator">
        <span class="login-type-text">
          {{ loginType === 'EMPLOYEE' ? '员工登录' : '普通登录' }}
        </span>
      </div>

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
        
        <!-- 注册入口 - 仅普通登录时显示 -->
        <el-form-item v-if="loginType === 'TENANT'">
          <div class="register-link">
            <span class="register-text">还没有账号？</span>
            <el-link type="primary" @click="goToRegister">点击注册！</el-link>
          </div>
        </el-form-item>

        <!-- 员工注册提示 - 仅员工登录时显示 -->
        <el-form-item v-if="loginType === 'EMPLOYEE'">
          <div class="employee-register-tip">
            <span class="tip-text">如需注册店员账号，请联系店长注册</span>
          </div>
        </el-form-item>

        <!-- 登录类型切换链接 -->
        <el-form-item>
          <div class="switch-login-link">
            <el-link
              v-if="loginType === 'TENANT'"
              type="primary"
              @click="switchToEmployeeLogin"
            >
              切换为员工登录
            </el-link>
            <el-link
              v-else
              type="primary"
              @click="switchToTenantLogin"
            >
              切换为普通登录
            </el-link>
          </div>
        </el-form-item>

        <!-- 忘记密码链接 -->
        <el-form-item>
          <div class="forgot-password-link">
            <el-link type="info" @click="goToForgotPassword">忘记密码？</el-link>
          </div>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from "vue";
import { useUserStore } from "../store/user";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { User, Lock, Promotion } from "@element-plus/icons-vue";
import logoUrl from '../assets/logo.jpg';

const userStore = useUserStore();
const router = useRouter();
const loading = ref(false);
const loginType = ref('TENANT'); // 默认普通登录

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
        const result = await userStore.login({
          username: form.username,
          password: form.password,
          userType: loginType.value  // 添加用户类型
        });
        
        if (result.success) {
          ElMessage.success('登录成功');
          
          // 根据用户类型重定向
          if (result.isSuperAdmin) {
            router.push('/super-admin');
          } else {
            router.push('/');
          }
        }
      } catch (error) {
        ElMessage.error(error.message || '登录失败');
      } finally {
        loading.value = false;
      }
    }
  });
};

// 跳转到注册页面
const goToRegister = () => {
  router.push("/register");
};

// 跳转到找回密码页面
const goToForgotPassword = () => {
  router.push("/forgot-password");
};

// 切换到员工登录
const switchToEmployeeLogin = () => {
  loginType.value = 'EMPLOYEE';
};

// 切换到普通登录
const switchToTenantLogin = () => {
  loginType.value = 'TENANT';
};

// 页面加载时确保主题是默认状态
onMounted(() => {
  // 确保登录页面使用默认主题
  userStore.resetThemeToDefault();
});
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
  margin-bottom: 5px;
  margin-top: 20px;
}

/* Logo 图片 */
.logo-image {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  object-fit: cover;
}

/* 标题样式 */
.login-title {
  text-align: center;
  font-size: 24px;
  color: #303133;
  margin-bottom: 10px;
}

/* 副标题样式 */
.login-subtitle {
  text-align: center;
  font-size: 14px;
  color: #909399;
  margin-bottom: 30px;
  margin-top: 0;
}



/* 登录表单 */
.login-form {
  margin-top: 20px;
}

/* 输入框图标 */
.input-icon {
  color: #909399;
}

/* 注册链接样式 */
.register-link {
  text-align: center;
  margin-top: 10px;
}

.register-text {
  color: #909399;
  font-size: 14px;
  margin-right: 5px;
}

/* 登录类型指示器 */
.login-type-indicator {
  text-align: center;
  margin-bottom: 20px;
}

.login-type-text {
  color: #409EFF;
  font-size: 16px;
  font-weight: 500;
}

/* 员工注册提示样式 */
.employee-register-tip {
  text-align: center;
  margin-top: 10px;
}

.tip-text {
  color: #909399;
  font-size: 14px;
}

/* 切换登录类型链接 */
.switch-login-link {
  text-align: left;
  margin-top: 10px;
  margin-bottom: 5px;
}

.switch-login-link .el-link {
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap; /* 防止换行 */
}

/* 忘记密码链接 */
.forgot-password-link {
  text-align: right;
  margin-top: 5px;
  padding-right: 0 !important;
  margin-right: 0 !important;
  width: 100%;
}

.forgot-password-link .el-link {
  font-size: 14px;
  margin-right: 0 !important;
  padding-right: 0 !important;
  float: right;
}

/* 确保忘记密码的表单项没有多余边距 */
.login-form .el-form-item:last-child {
  margin-bottom: 0;
  padding-right: 0 !important;
}

.login-form .el-form-item:last-child .el-form-item__content {
  padding-right: 0 !important;
  margin-right: 0 !important;
}
</style>
