<template>
  <div class="test-upload-container">
    <el-card>
      <template #header>
        <h2>文件上传测试</h2>
      </template>
      
      <div class="upload-section">
        <h3>菜品图片上传测试</h3>
        <el-upload
          class="dish-uploader"
          :action="dishUploadUrl"
          :show-file-list="false"
          :on-success="handleDishSuccess"
          :before-upload="beforeUpload"
          :headers="uploadHeaders"
        >
          <img v-if="dishImageUrl" :src="dishImageUrl" class="uploaded-image" />
          <div v-else class="upload-placeholder">
            <el-icon><Plus /></el-icon>
            <div>点击上传菜品图片</div>
          </div>
        </el-upload>
        <p v-if="dishImageUrl">菜品图片URL: {{ dishImageUrl }}</p>
      </div>
      
      <div class="upload-section">
        <h3>头像上传测试</h3>
        <el-upload
          class="avatar-uploader"
          :action="avatarUploadUrl"
          :show-file-list="false"
          :on-success="handleAvatarSuccess"
          :before-upload="beforeUpload"
          :headers="uploadHeaders"
        >
          <img v-if="avatarImageUrl" :src="avatarImageUrl" class="uploaded-image" />
          <div v-else class="upload-placeholder">
            <el-icon><User /></el-icon>
            <div>点击上传头像</div>
          </div>
        </el-upload>
        <p v-if="avatarImageUrl">头像URL: {{ avatarImageUrl }}</p>
      </div>
      
      <div class="test-info">
        <h3>测试信息</h3>
        <p>菜品上传URL: {{ dishUploadUrl }}</p>
        <p>头像上传URL: {{ avatarUploadUrl }}</p>
        <p>API Base URL: {{ api.defaults.baseURL }}</p>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { Plus, User } from '@element-plus/icons-vue';
import api from '../utils/api';

const dishImageUrl = ref('');
const avatarImageUrl = ref('');

// 上传配置
const dishUploadUrl = computed(() => {
  const baseURL = api.defaults.baseURL;
  return baseURL.endsWith('/') 
    ? `${baseURL}api/files/upload/dish`
    : `${baseURL}/api/files/upload/dish`;
});

const avatarUploadUrl = computed(() => {
  const baseURL = api.defaults.baseURL;
  return baseURL.endsWith('/') 
    ? `${baseURL}api/files/upload/avatar`
    : `${baseURL}/api/files/upload/avatar`;
});

const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token');
  return token ? { 'Authorization': `Bearer ${token}` } : {};
});

// 上传成功回调
const handleDishSuccess = (response) => {
  const baseURL = api.defaults.baseURL;
  const imageUrl = response.url.startsWith('http') 
    ? response.url 
    : (baseURL.endsWith('/') ? `${baseURL.slice(0, -1)}${response.url}` : `${baseURL}${response.url}`);
  
  dishImageUrl.value = imageUrl;
  ElMessage.success('菜品图片上传成功');
  console.log('菜品图片上传响应:', response);
};

const handleAvatarSuccess = (response) => {
  const baseURL = api.defaults.baseURL;
  const imageUrl = response.url.startsWith('http') 
    ? response.url 
    : (baseURL.endsWith('/') ? `${baseURL.slice(0, -1)}${response.url}` : `${baseURL}${response.url}`);
  
  avatarImageUrl.value = imageUrl;
  ElMessage.success('头像上传成功');
  console.log('头像上传响应:', response);
};

// 上传前验证
const beforeUpload = (file) => {
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
  
  console.log('准备上传文件:', file.name, file.type, file.size);
  return true;
};
</script>

<style scoped>
.test-upload-container {
  padding: 20px;
}

.upload-section {
  margin-bottom: 30px;
  padding: 20px;
  border: 1px solid #eee;
  border-radius: 8px;
}

.upload-placeholder {
  width: 150px;
  height: 150px;
  border: 2px dashed #ddd;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 8px;
}

.upload-placeholder:hover {
  border-color: #409eff;
}

.uploaded-image {
  width: 150px;
  height: 150px;
  object-fit: cover;
  border-radius: 8px;
}

.test-info {
  background: #f5f5f5;
  padding: 15px;
  border-radius: 8px;
}

.test-info p {
  margin: 5px 0;
  font-family: monospace;
  font-size: 12px;
}
</style>
