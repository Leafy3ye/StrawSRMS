<template>
  <div class="profile-container">
    <el-card class="profile-card">
      <template #header>
        <div class="profile-header">
          <h2>个人信息</h2>
        </div>
      </template>

      <div class="avatar-container">
        <el-avatar :size="100" :src="avatarUrl">
          <el-icon><User /></el-icon>
        </el-avatar>
        <el-upload
          class="avatar-uploader"
          action="#"
          :auto-upload="false"
          :show-file-list="false"
          :on-change="handleAvatarChange"
        >
          <el-button size="small" type="primary">更换头像</el-button>
        </el-upload>
      </div>

      <el-form
        :model="form"
        label-width="100px"
        :rules="rules"
        ref="formRef"
        class="profile-form"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" />
        </el-form-item>

        <el-form-item label="当前密码" prop="currentPassword">
          <el-input
            v-model="form.currentPassword"
            type="password"
            show-password
            placeholder="修改信息需要验证当前密码"
          />
        </el-form-item>

        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="form.newPassword"
            type="password"
            show-password
            placeholder="不修改请留空"
          />
        </el-form-item>

        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            show-password
            placeholder="不修改请留空"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submitForm" :loading="loading">
            保存修改
          </el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from "vue";
import { useUserStore } from "../store/user";
import { ElMessage } from "element-plus";
import { User } from "@element-plus/icons-vue";

const userStore = useUserStore();
const formRef = ref(null);
const loading = ref(false);
const avatarFile = ref(null);

// 表单数据
const form = reactive({
  username: userStore.user?.username || "",
  currentPassword: "",
  newPassword: "",
  confirmPassword: "",
});

// 头像URL
const avatarUrl = computed(() => {
  return userStore.user?.avatarUrl || "";
});

// 表单验证规则
const rules = {
  username: [
    { required: true, message: "请输入用户名", trigger: "blur" },
    { min: 3, max: 20, message: "长度在 3 到 20 个字符", trigger: "blur" },
  ],
  currentPassword: [
    { required: true, message: "请输入当前密码", trigger: "blur" },
  ],
  newPassword: [
    { min: 6, message: "密码长度不能少于 6 个字符", trigger: "blur" },
  ],
  confirmPassword: [
    {
      validator: (rule, value, callback) => {
        if (form.newPassword && value !== form.newPassword) {
          callback(new Error("两次输入密码不一致"));
        } else {
          callback();
        }
      },
      trigger: "blur",
    },
  ],
};

// 处理头像变更
const handleAvatarChange = (file) => {
  avatarFile.value = file;
  
  // 这里可以实现头像预览
  const reader = new FileReader();
  reader.onload = (e) => {
    form.avatarUrl = e.target.result;
  };
  reader.readAsDataURL(file.raw);
};

// 提交表单
const submitForm = () => {
  formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      try {
        // 构建更新数据
        const updateData = {
          username: form.username,
          currentPassword: form.currentPassword,
        };

        // 如果有新密码，添加到更新数据中
        if (form.newPassword) {
          updateData.newPassword = form.newPassword;
        }

        // 如果有新头像，添加到更新数据中
        if (form.avatarUrl && form.avatarUrl !== userStore.user?.avatarUrl) {
          updateData.avatarUrl = form.avatarUrl;
        }

        // 调用更新API
        await userStore.updateUserInfo(updateData);
        ElMessage.success("个人信息更新成功");
        
        // 重置密码字段
        form.currentPassword = "";
        form.newPassword = "";
        form.confirmPassword = "";
      } catch (error) {
        ElMessage.error(error.message || "更新失败，请检查当前密码是否正确");
      } finally {
        loading.value = false;
      }
    }
  });
};

// 重置表单
const resetForm = () => {
  formRef.value.resetFields();
  form.username = userStore.user?.username || "";
};

// 组件挂载时获取最新的用户信息
onMounted(async () => {
  if (userStore.user) {
    await userStore.getUserInfo();
    form.username = userStore.user.username;
  }
});
</script>

<style scoped>
.profile-container {
  max-width: 800px;
  margin: 20px auto;
  padding: 0 20px;
}

.profile-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.avatar-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 30px;
}

.avatar-uploader {
  margin-top: 15px;
}

.profile-form {
  max-width: 500px;
  margin: 0 auto;
}
</style>