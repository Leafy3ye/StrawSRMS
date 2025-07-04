<template>
  <div class="shop-settings">
    <div class="page-header">
      <h2>店铺设置</h2>
      <p>配置您的店铺基本信息</p>
    </div>

    <el-card>
      <template #header>
        <span>店铺基本信息</span>
      </template>
      
      <el-form :model="shopForm" :rules="rules" ref="shopFormRef" label-width="120px">
        <el-form-item label="店铺名称" prop="shopName">
          <el-input 
            v-model="shopForm.shopName" 
            placeholder="请输入店铺名称" 
            maxlength="50"
          />
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
import { useUserStore } from '../../store/user';
import { ElMessage } from 'element-plus';

const userStore = useUserStore();
const shopFormRef = ref();
const loading = ref(false);

const shopForm = ref({
  shopName: ''
});

const rules = {
  shopName: [
    { required: true, message: '请输入店铺名称', trigger: 'blur' },
    { min: 2, max: 50, message: '店铺名称长度在 2 到 50 个字符', trigger: 'blur' }
  ]
};

// 初始化表单数据
onMounted(() => {
  if (userStore.user?.restaurantName) {
    shopForm.value.shopName = userStore.user.restaurantName;
  }
});

const handleSave = async () => {
  try {
    await shopFormRef.value.validate();
    loading.value = true;
    
    await userStore.updateShopInfo(shopForm.value);
    
    ElMessage.success('店铺信息更新成功！');
  } catch (error) {
    console.error('更新失败:', error);
    ElMessage.error('更新失败，请重试');
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.shop-settings {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
}

.page-header p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}
</style>