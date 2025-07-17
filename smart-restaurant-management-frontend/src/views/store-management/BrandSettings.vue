<template>
  <div class="brand-settings">
    <div class="page-header">
      <h2>品牌名称设置</h2>
      <p class="page-description">设置您的品牌名称，将显示在系统左上角</p>
    </div>

    <el-card class="settings-card">
      <template #header>
        <div class="card-header">
          <span>品牌信息</span>
        </div>
      </template>

      <el-form :model="brandForm" :rules="brandRules" ref="brandFormRef" label-width="120px">
        <el-form-item label="品牌名称" prop="brandName">
          <el-input
            v-model="brandForm.brandName"
            placeholder="请输入品牌名称（如：麦当劳、肯德基等）"
            maxlength="50"
            show-word-limit
          />
          <div class="form-tip">品牌名称将显示在系统左上角logo旁边</div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSave" :loading="loading">
            保存设置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import api from '../../utils/api';

const brandFormRef = ref();
const loading = ref(false);

const brandForm = ref({
  brandName: ''
});

const brandRules = {
  brandName: [
    { required: true, message: '请输入品牌名称', trigger: 'blur' },
    { min: 2, max: 50, message: '品牌名称长度在 2 到 50 个字符', trigger: 'blur' }
  ]
};

// 获取租户信息
const fetchTenantInfo = async () => {
  try {
    const response = await api.get('/api/tenants/current');
    brandForm.value.brandName = response.data.tenantName || '';
  } catch (error) {
    console.error('获取租户信息失败:', error);
  }
};

// 保存品牌名称
const handleSave = async () => {
  try {
    await brandFormRef.value.validate();
    loading.value = true;
    
    await api.put('/api/tenants/current', {
      tenantName: brandForm.value.brandName
    });
    
    ElMessage.success('品牌名称保存成功！');
    
    // 触发导航栏更新
    window.dispatchEvent(new CustomEvent('tenant-updated'));
    
  } catch (error) {
    ElMessage.error('保存失败: ' + (error.response?.data || error.message));
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchTenantInfo();
});
</script>

<style scoped>
.brand-settings {
  padding: 20px;
  max-width: 800px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.page-description {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.settings-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  font-weight: 600;
  color: #303133;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
