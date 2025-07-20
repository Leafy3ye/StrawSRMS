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
          :action="uploadUrl"
          :show-file-list="false"
          :on-success="handleAvatarSuccess"
          :before-upload="beforeAvatarUpload"
          :headers="uploadHeaders"
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

      <!-- 危险操作区域 -->
      <el-divider />
      <div class="danger-zone">
        <h3 class="danger-title">危险操作</h3>
        <p class="danger-description">
          注销账户将永久删除您的所有数据，包括店铺信息、菜品、订单、会员等，此操作无法恢复！
        </p>
        <el-button type="danger" @click="showDeleteDialog = true">
          注销账户
        </el-button>
      </div>
    </el-card>

    <!-- 删除账户确认对话框 -->
    <el-dialog
      v-model="showDeleteDialog"
      title="注销账户确认"
      width="500px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
    >
      <div class="delete-warning">
        <el-alert
          title="您正在进行注销账户操作！"
          description="注意，此操作将删除关于您和您店铺的一切数据，并且无法恢复！请谨慎操作！"
          type="error"
          :closable="false"
          show-icon
        />
      </div>
      
      <el-form :model="deleteForm" :rules="deleteRules" ref="deleteFormRef" class="delete-form">
        <el-form-item label="当前密码" prop="currentPassword">
          <el-input
            v-model="deleteForm.currentPassword"
            type="password"
            show-password
            placeholder="请输入当前密码以确认身份"
          />
        </el-form-item>
        
        <el-form-item label="确认删除" prop="confirmText">
          <el-input
            v-model="deleteForm.confirmText"
            placeholder="请输入 DELETE 以确认删除"
          />
          <div class="confirm-hint">请在上方输入框中输入 <strong>DELETE</strong> 以确认删除操作</div>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="cancelDelete">取消</el-button>
          <el-button 
            type="danger" 
            @click="confirmDelete" 
            :loading="deleteLoading"
            :disabled="deleteForm.confirmText !== 'DELETE'"
          >
            确认删除账户
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from "vue";
import { useUserStore } from "../store/user";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { User } from "@element-plus/icons-vue";
import api from "../utils/api";

const userStore = useUserStore();
const router = useRouter();
const formRef = ref(null);
const deleteFormRef = ref(null);
const loading = ref(false);
const deleteLoading = ref(false);
const avatarFile = ref(null);
const showDeleteDialog = ref(false);

// 表单数据
const form = reactive({
  username: userStore.user?.username || "",
  currentPassword: "",
  newPassword: "",
  confirmPassword: "",
});

// 删除账户表单
const deleteForm = reactive({
  currentPassword: "",
  confirmText: ""
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

// 删除表单验证规则
const deleteRules = {
  currentPassword: [
    { required: true, message: "请输入当前密码", trigger: "blur" },
  ],
  confirmText: [
    { required: true, message: "请输入 DELETE 以确认", trigger: "blur" },
    {
      validator: (rule, value, callback) => {
        if (value !== "DELETE") {
          callback(new Error("请输入 DELETE 以确认删除操作"));
        } else {
          callback();
        }
      },
      trigger: "blur",
    },
  ],
};

// 上传配置
const uploadUrl = computed(() => {
  const baseURL = api.defaults.baseURL;
  // 确保URL格式正确，避免双斜杠
  return baseURL.endsWith('/')
    ? `${baseURL}api/files/upload/avatar`
    : `${baseURL}/api/files/upload/avatar`;
});

const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token');
  return token ? { 'Authorization': `Bearer ${token}` } : {};
});

// 头像上传成功回调
const handleAvatarSuccess = async (response) => {
  try {
    // 确保头像URL是完整的访问路径
    const baseURL = api.defaults.baseURL;
    const imageUrl = response.url.startsWith('http')
      ? response.url
      : (baseURL.endsWith('/') ? `${baseURL.slice(0, -1)}${response.url}` : `${baseURL}${response.url}`);

    // 更新表单数据
    form.avatarUrl = imageUrl;

    // 立即更新用户信息到后端
    await userStore.updateUserInfo({ avatarUrl: imageUrl });

    ElMessage.success('头像上传并保存成功');
  } catch (error) {
    console.error('保存头像失败:', error);
    ElMessage.error('头像上传成功，但保存失败，请稍后重试');
  }
};

// 头像上传前验证
const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/');
  const isLt2M = file.size / 1024 / 1024 < 2;

  if (!isImage) {
    ElMessage.error('只能上传图片文件!');
    return false;
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!');
    return false;
  }
  return true;
};

// 处理头像变更（保留作为备用）
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

// 取消删除
const cancelDelete = () => {
  showDeleteDialog.value = false;
  deleteForm.currentPassword = "";
  deleteForm.confirmText = "";
};

// 确认删除账户
const confirmDelete = () => {
  deleteFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await ElMessageBox.confirm(
          '您确定要删除账户吗？此操作将永久删除所有数据且无法恢复！',
          '最终确认',
          {
            confirmButtonText: '确定删除',
            cancelButtonText: '取消',
            type: 'error',
            confirmButtonClass: 'el-button--danger'
          }
        );
        
        deleteLoading.value = true;
        
        const result = await userStore.deleteAccount({
          currentPassword: deleteForm.currentPassword,
          confirmText: deleteForm.confirmText
        });
        
        ElMessage.success(result.message || '账户删除成功');
        
        // 跳转到登录页
        router.push('/login');
        
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error(error.response?.data || error.message || '删除失败');
        }
      } finally {
        deleteLoading.value = false;
      }
    }
  });
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

.danger-zone {
  margin-top: 30px;
  padding: 20px;
  border: 1px solid #f56c6c;
  border-radius: 4px;
  background-color: #fef0f0;
}

.danger-title {
  color: #f56c6c;
  margin: 0 0 10px 0;
  font-size: 16px;
  font-weight: bold;
}

.danger-description {
  color: #606266;
  margin-bottom: 15px;
  line-height: 1.5;
}

.delete-warning {
  margin-bottom: 20px;
}

.delete-form {
  margin-top: 20px;
}

.confirm-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.confirm-hint strong {
  color: #f56c6c;
}
</style>