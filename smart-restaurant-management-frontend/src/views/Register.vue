<template>
  <div class="register-page">
    <el-card class="register-card">
      <!-- Logo 图片 -->
      <div class="logo-container">
        <img :src="logoUrl" alt="Logo" class="logo-image" />
      </div>

      <!-- 标题 -->
      <h2 class="register-title">用户注册</h2>

      <!-- 注册表单 -->
      <el-form :model="form" label-width="0" ref="registerForm" class="register-form" :rules="rules">
        <!-- 用户名输入框 -->
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名（支持中文、英文、数字，3-20个字符）"
            clearable
          >
            <template #prefix>
              <el-icon class="input-icon">
                <User />
              </el-icon>
            </template>
          </el-input>
        </el-form-item>

        <!-- 邮箱输入框 -->
        <el-form-item prop="email">
          <el-input
            v-model="form.email"
            placeholder="请输入邮箱地址"
            clearable
          >
            <template #prefix>
              <el-icon class="input-icon">
                <Message />
              </el-icon>
            </template>
          </el-input>
        </el-form-item>

        <!-- 手机号输入框 -->
        <el-form-item prop="phone">
          <el-input
            v-model="form.phone"
            placeholder="请输入手机号"
            clearable
          >
            <template #prefix>
              <el-icon class="input-icon">
                <Phone />
              </el-icon>
            </template>
          </el-input>
        </el-form-item>

        <!-- 图形验证码 -->
        <el-form-item prop="captcha">
          <div class="captcha-container">
            <el-input
              v-model="form.captcha"
              placeholder="请输入图形验证码"
              style="flex: 1; margin-right: 10px;"
            >
              <template #prefix>
                <el-icon class="input-icon">
                  <Picture />
                </el-icon>
              </template>
            </el-input>
            <img 
              :src="captchaImage" 
              @click="refreshCaptcha" 
              class="captcha-image"
              title="点击刷新验证码"
            />
          </div>
        </el-form-item>

        <!-- 邮件验证码 -->
        <el-form-item prop="emailCode">
          <div class="email-code-container">
            <el-input
              v-model="form.emailCode"
              placeholder="请输入邮件验证码"
              style="flex: 1; margin-right: 10px;"
            >
              <template #prefix>
                <el-icon class="input-icon">
                  <Key />
                </el-icon>
              </template>
            </el-input>
            <el-button 
              @click="sendEmailCode" 
              :disabled="emailCodeSending || emailCodeCountdown > 0"
              type="primary"
            >
              {{ emailCodeCountdown > 0 ? `${emailCodeCountdown}s后重发` : '发送验证码' }}
            </el-button>
          </div>
        </el-form-item>

        <!-- 密码输入框 -->
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码（6-20个字符）"
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

        <!-- 确认密码输入框 -->
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
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

        <!-- 注册按钮 -->
        <el-form-item>
          <el-button
            type="primary"
            @click="onRegister"
            style="width: 100%;"
            :loading="loading"
            :icon="UserFilled"
          >
            注册
          </el-button>
        </el-form-item>
        
        <!-- 返回登录 -->
        <el-form-item>
          <div class="login-link">
            <span class="login-text">已有账号？</span>
            <el-link type="primary" @click="goToLogin">返回登录</el-link>
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
import { User, Lock, UserFilled, Message, Phone, Picture, Key } from "@element-plus/icons-vue";
import api from "../utils/api";
import logoUrl from '../assets/logo.jpg';

const userStore = useUserStore();
const router = useRouter();
const loading = ref(false);
const emailCodeSending = ref(false);
const emailCodeCountdown = ref(0);
const captchaImage = ref('');
const captchaKey = ref('');

const form = reactive({
  username: "",
  email: "",
  phone: "",
  captcha: "",
  emailCode: "",
  password: "",
  confirmPassword: "",
});

// 自定义验证规则
const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'));
  } else {
    callback();
  }
};

const rules = {
  username: [
    { required: true, message: "请输入用户名", trigger: "blur" },
    { min: 3, max: 20, message: "用户名长度在 3 到 20 个字符", trigger: "blur" },
    // 修改这行以支持中文
    { pattern: /^[\u4e00-\u9fa5a-zA-Z0-9_]+$/, message: "用户名只能包含中文、字母、数字和下划线", trigger: "blur" }
  ],
  email: [
    { required: true, message: "请输入邮箱地址", trigger: "blur" },
    { type: 'email', message: "请输入正确的邮箱地址", trigger: "blur" }
  ],
  phone: [
    { required: true, message: "请输入手机号", trigger: "blur" },
    { pattern: /^1[3-9]\d{9}$/, message: "请输入正确的手机号", trigger: "blur" }
  ],
  captcha: [
    { required: true, message: "请输入图形验证码", trigger: "blur" }
  ],
  emailCode: [
    { required: true, message: "请输入邮件验证码", trigger: "blur" }
  ],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" },
    { min: 6, max: 20, message: "密码长度在 6 到 20 个字符", trigger: "blur" }
  ],
  confirmPassword: [
    { required: true, message: "请再次输入密码", trigger: "blur" },
    { validator: validateConfirmPassword, trigger: "blur" }
  ]
};

const registerForm = ref(null);

// 获取图形验证码
const refreshCaptcha = async () => {
  try {
    const response = await api.get('/api/users/captcha');
    captchaImage.value = response.data.image;
    captchaKey.value = response.data.key;
  } catch (error) {
    ElMessage.error('获取验证码失败');
  }
};

// 发送邮件验证码
const sendEmailCode = async () => {
  if (!form.email) {
    ElMessage.error('请先输入邮箱地址');
    return;
  }
  
  emailCodeSending.value = true;
  try {
    await api.post('/api/users/send-email-code', { email: form.email });
    ElMessage.success('验证码已发送到您的邮箱');
    
    // 开始倒计时
    emailCodeCountdown.value = 60;
    const timer = setInterval(() => {
      emailCodeCountdown.value--;
      if (emailCodeCountdown.value <= 0) {
        clearInterval(timer);
      }
    }, 1000);
  } catch (error) {
    ElMessage.error(error.response?.data || '发送验证码失败');
  } finally {
    emailCodeSending.value = false;
  }
};

// 注册操作
const onRegister = () => {
  registerForm.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      try {
        await userStore.register({
          username: form.username,
          email: form.email,
          phone: form.phone,
          password: form.password,
          captcha: form.captcha,
          captchaKey: captchaKey.value,
          emailCode: form.emailCode
        });
        ElMessage.success("注册成功！请登录");
        router.push("/login");
      } catch (error) {
        ElMessage.error(error.message || "注册失败！");
        // 注册失败后刷新验证码
        refreshCaptcha();
      } finally {
        loading.value = false;
      }
    }
  });
};

// 返回登录页面
const goToLogin = () => {
  router.push("/login");
};

// 页面加载时获取验证码
onMounted(() => {
  refreshCaptcha();
});
</script>

<style scoped>
/* 页面整体布局 */
.register-page {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
}

/* 注册卡片样式 */
.register-card {
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
.register-title {
  text-align: center;
  font-size: 24px;
  color: #303133;
  margin-bottom: 30px;
}

/* 注册表单 */
.register-form {
  margin-top: 20px;
}

/* 输入框图标 */
.input-icon {
  color: #909399;
}

/* 登录链接样式 */
.login-link {
  text-align: center;
  margin-top: 10px;
}

.login-text {
  color: #909399;
  font-size: 14px;
  margin-right: 5px;
}

.captcha-container {
  display: flex;
  align-items: center;
  width: 100%;
}

.captcha-image {
  height: 40px;
  cursor: pointer;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.email-code-container {
  display: flex;
  align-items: center;
  width: 100%;
}
</style>