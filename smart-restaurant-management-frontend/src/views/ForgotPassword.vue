<template>
  <div class="forgot-password-page">
    <el-card class="forgot-password-card">
      <!-- Logo 图片 -->
      <div class="logo-container">
        <img :src="logoUrl" alt="Logo" class="logo-image" />
      </div>

      <!-- 标题 -->
      <h2 class="forgot-password-title">找回密码</h2>

      <!-- 找回密码表单 -->
      <el-form :model="form" label-width="0" ref="forgotPasswordForm" class="forgot-password-form" :rules="rules">
        <!-- 账户名或邮箱输入框 -->
        <el-form-item prop="account">
          <el-input
            v-model="form.account"
            placeholder="请输入用户名或邮箱地址"
            clearable
          >
            <template #prefix>
              <el-icon class="input-icon">
                <User />
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
              :disabled="emailCodeSending || emailCodeCountdown > 0 || !isCaptchaValid"
              type="primary"
            >
              {{ emailCodeCountdown > 0 ? `${emailCodeCountdown}s后重发` : '发送验证码' }}
            </el-button>
          </div>
        </el-form-item>

        <!-- 新密码输入框 -->
        <el-form-item prop="newPassword">
          <el-input
            v-model="form.newPassword"
            type="password"
            placeholder="请输入新密码（6-20个字符）"
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

        <!-- 确认新密码输入框 -->
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
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

        <!-- 重置密码按钮 -->
        <el-form-item>
          <el-button
            type="primary"
            @click="onResetPassword"
            style="width: 100%;"
            :loading="loading"
            :icon="Key"
          >
            重置密码
          </el-button>
        </el-form-item>
        
        <!-- 返回登录 -->
        <el-form-item>
          <div class="login-link">
            <el-link type="primary" @click="goToLogin">返回登录</el-link>
          </div>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, computed } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { User, Lock, Picture, Key } from "@element-plus/icons-vue";
import api from "../utils/api";
import logoUrl from '../assets/logo.jpg';

const router = useRouter();
const loading = ref(false);
const emailCodeSending = ref(false);
const emailCodeCountdown = ref(0);
const captchaImage = ref('');
const captchaKey = ref('');

const form = reactive({
  account: "",
  captcha: "",
  emailCode: "",
  newPassword: "",
  confirmPassword: "",
});

// 检查图形验证码是否有效（只检查长度）
const isCaptchaValid = computed(() => {
  return form.captcha.length >= 4;
});

// 自定义验证规则
const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.newPassword) {
    callback(new Error('两次输入的密码不一致'));
  } else {
    callback();
  }
};

const rules = {
  account: [
    { required: true, message: "请输入用户名或邮箱地址", trigger: "blur" }
  ],
  captcha: [
    { required: true, message: "请输入图形验证码", trigger: "blur" }
  ],
  emailCode: [
    { required: true, message: "请输入邮件验证码", trigger: "blur" }
  ],
  newPassword: [
    { required: true, message: "请输入新密码", trigger: "blur" },
    { min: 6, max: 20, message: "密码长度在 6 到 20 个字符", trigger: "blur" }
  ],
  confirmPassword: [
    { required: true, message: "请再次输入新密码", trigger: "blur" },
    { validator: validateConfirmPassword, trigger: "blur" }
  ]
};

const forgotPasswordForm = ref(null);

// 获取图形验证码
const refreshCaptcha = async () => {
  try {
    const response = await api.get('/api/users/captcha');
    captchaImage.value = response.data.image;
    captchaKey.value = response.data.key;
    form.captcha = '';
  } catch (error) {
    ElMessage.error('获取验证码失败');
  }
};

// 发送邮件验证码
const sendEmailCode = async () => {
  if (!form.account) {
    ElMessage.error('请先输入用户名或邮箱地址');
    return;
  }
  
  if (!form.captcha || form.captcha.length < 4) {
    ElMessage.error('请输入图形验证码');
    return;
  }
  
  emailCodeSending.value = true;
  try {
    await api.post('/api/users/send-reset-password-code', { 
      account: form.account,
      captchaKey: captchaKey.value,
      captcha: form.captcha
    });
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
    refreshCaptcha(); // 验证失败时刷新验证码
  } finally {
    emailCodeSending.value = false;
  }
};

// 重置密码操作
const onResetPassword = () => {
  forgotPasswordForm.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      try {
        await api.post('/api/users/reset-password', {
          account: form.account,
          emailCode: form.emailCode,
          newPassword: form.newPassword,
          captchaKey: captchaKey.value,
          captcha: form.captcha
        });
        ElMessage.success("密码重置成功！请使用新密码登录");
        router.push("/login");
      } catch (error) {
        ElMessage.error(error.response?.data || "密码重置失败！");
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
.forgot-password-page {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
}

/* 找回密码卡片样式 */
.forgot-password-card {
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
.forgot-password-title {
  text-align: center;
  font-size: 24px;
  color: #303133;
  margin-bottom: 30px;
}

/* 找回密码表单 */
.forgot-password-form {
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