<template>
  <el-dialog
    v-model="visible"
    title="店铺初始化设置"
    width="500px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    :show-close="false"
  >
    <div class="setup-content">
      <p class="setup-description">欢迎使用智慧餐饮管理系统！请完善您的店铺基本信息。</p>
      
      <el-form :model="setupForm" :rules="rules" ref="setupFormRef" label-width="100px">
        <el-form-item label="店铺名称" prop="shopName">
          <el-input 
            v-model="setupForm.shopName" 
            placeholder="请输入店铺名称，这将显示在系统左上角。"
            maxlength="50"
          />
        </el-form-item>
        
        <el-form-item label="桌位数量" prop="tableCount">
          <el-input-number 
            v-model="setupForm.tableCount" 
            :min="1" 
            :max="100"
            placeholder="请输入桌位数量"
            style="width: 100%"
          />
          <div class="form-tip">系统将自动为您创建相应数量的桌位，后续可在桌位管理中修改</div>
        </el-form-item>
      </el-form>
    </div>
    
    <template #footer>
      <el-button type="primary" @click="handleSetup" :loading="loading">
        完成设置
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useUserStore } from '../store/user';
import { ElMessage } from 'element-plus';

const userStore = useUserStore();
const setupFormRef = ref();
const loading = ref(false);

const visible = computed({
  get: () => userStore.showSetupDialog,
  set: (value) => {
    userStore.showSetupDialog = value;
  }
});

const setupForm = ref({
  shopName: '',
  tableCount: 10
});

const rules = {
  shopName: [
    { required: true, message: '请输入店铺名称', trigger: 'blur' },
    { min: 2, max: 50, message: '店铺名称长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  tableCount: [
    { required: true, message: '请输入桌位数量', trigger: 'blur' },
    { type: 'number', min: 1, max: 100, message: '桌位数量必须在 1 到 100 之间', trigger: 'blur' }
  ]
};

const handleSetup = async () => {
  try {
    await setupFormRef.value.validate();
    loading.value = true;
    
    await userStore.setupShop(setupForm.value);
    
    ElMessage.success('店铺设置完成！');
  } catch (error) {
    console.error('店铺设置失败:', error);
    ElMessage.error('店铺设置失败，请重试');
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.setup-content {
  padding: 20px 0;
}

.setup-description {
  margin-bottom: 20px;
  color: #606266;
  line-height: 1.6;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}
</style>