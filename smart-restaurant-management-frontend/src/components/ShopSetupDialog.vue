<template>
  <el-dialog
    v-model="visible"
    title="店铺初始化设置"
    width="600px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    :show-close="false"
  >
    <div class="setup-content">
      <!-- 第一步：选择店铺模式 -->
      <div v-if="currentStep === 1" class="step-content">
        <h3 class="step-title">欢迎使用智慧餐饮管理系统！</h3>
        <p class="setup-description">请选择您的经营模式：</p>

        <div class="mode-selection">
          <el-card
            class="mode-card"
            :class="{ active: selectedMode === 'single' }"
            @click="selectedMode = 'single'"
            shadow="hover"
          >
            <div class="mode-icon">🏪</div>
            <h4>单店模式</h4>
            <p>适合单一店铺经营，简单快捷</p>
          </el-card>

          <el-card
            class="mode-card"
            :class="{ active: selectedMode === 'multi' }"
            @click="selectedMode = 'multi'"
            shadow="hover"
          >
            <div class="mode-icon">🏢</div>
            <h4>连锁模式</h4>
            <p>支持多分店管理，统一运营</p>
          </el-card>
        </div>
      </div>

      <!-- 第二步：品牌名称设置 -->
      <div v-if="currentStep === 2" class="step-content">
        <h3 class="step-title">设置品牌名称</h3>
        <p class="setup-description">请设置您的品牌名称，这将显示在系统左上角：</p>

        <el-form :model="brandForm" :rules="brandRules" ref="brandFormRef" label-width="100px">
          <el-form-item label="品牌名称" prop="brandName">
            <el-input
              v-model="brandForm.brandName"
              placeholder="请输入品牌名称（如：麦当劳、肯德基等）"
              maxlength="50"
            />
            <div class="form-tip">品牌名称将显示在系统左上角logo旁边</div>
          </el-form-item>
        </el-form>
      </div>

      <!-- 第三步：总店信息设置 -->
      <div v-if="currentStep === 3" class="step-content">
        <h3 class="step-title">{{ selectedMode === 'single' ? '店铺基本信息' : '总店基本信息' }}</h3>

        <el-form :model="setupForm" :rules="rules" ref="setupFormRef" label-width="100px">
          <el-form-item label="店铺名称" prop="shopName">
            <el-input
              v-model="setupForm.shopName"
              :placeholder="selectedMode === 'single' ? '请输入店铺名称' : '请输入总店名称'"
              maxlength="50"
            />
            <div class="form-tip">{{ selectedMode === 'single' ? '店铺名称' : '总店名称' }}，如：{{ brandForm.brandName || '品牌名称' }}{{ selectedMode === 'single' ? '餐厅' : '总店' }}</div>
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
    </div>

    <template #footer>
      <div class="dialog-footer">
        <div class="step-indicator">
          <span class="step-text">第 {{ currentStep }} 步，共 3 步</span>
        </div>
        <div class="button-group">
          <el-button v-if="currentStep > 1" @click="prevStep">上一步</el-button>
          <el-button
            v-if="currentStep === 1"
            type="primary"
            @click="nextStep"
            :disabled="!selectedMode"
          >
            下一步
          </el-button>
          <el-button
            v-if="currentStep === 2"
            type="primary"
            @click="nextStepBrand"
          >
            下一步
          </el-button>
          <el-button
            v-if="currentStep === 3"
            type="primary"
            @click="handleSetup"
            :loading="loading"
          >
            完成设置
          </el-button>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useUserStore } from '../store/user';
import { ElMessage } from 'element-plus';

const userStore = useUserStore();
const setupFormRef = ref();
const brandFormRef = ref();
const loading = ref(false);

// 当前步骤
const currentStep = ref(1);
// 选择的模式
const selectedMode = ref('');

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

const brandForm = ref({
  brandName: ''
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

const brandRules = {
  brandName: [
    { required: true, message: '请输入品牌名称', trigger: 'blur' },
    { min: 2, max: 50, message: '品牌名称长度在 2 到 50 个字符', trigger: 'blur' }
  ]
};

// 下一步（第一步到第二步）
const nextStep = () => {
  if (selectedMode.value) {
    currentStep.value = 2;
  }
};

// 品牌设置下一步（第二步到第三步）
const nextStepBrand = async () => {
  try {
    await brandFormRef.value.validate();
    currentStep.value = 3;
  } catch (error) {
    console.error('品牌名称验证失败:', error);
  }
};

// 上一步
const prevStep = () => {
  if (currentStep.value > 1) {
    currentStep.value--;
  }
};

const handleSetup = async () => {
  try {
    await setupFormRef.value.validate();
    loading.value = true;

    // 传入模式信息和品牌名称
    await userStore.setupShop({
      ...setupForm.value,
      mode: selectedMode.value,
      brandName: brandForm.value.brandName
    });

    ElMessage.success('店铺设置完成！');

    // 设置完成后，触发导航栏刷新
    window.dispatchEvent(new CustomEvent('tenant-updated'));

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

.step-content {
  min-height: 300px;
}

.step-title {
  color: #303133;
  margin-bottom: 10px;
  font-size: 18px;
  font-weight: 600;
}

.setup-description {
  margin-bottom: 30px;
  color: #606266;
  line-height: 1.6;
}

.mode-selection {
  display: flex;
  gap: 20px;
  justify-content: center;
  margin: 30px 0;
}

.mode-card {
  width: 200px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.mode-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.mode-card.active {
  border-color: #409EFF;
  box-shadow: 0 8px 25px rgba(64, 158, 255, 0.2);
}

.mode-icon {
  font-size: 48px;
  margin-bottom: 15px;
}

.mode-card h4 {
  color: #303133;
  margin: 10px 0;
  font-size: 16px;
  font-weight: 600;
}

.mode-card p {
  color: #666;
  font-size: 14px;
  margin: 0;
  line-height: 1.4;
}

.dialog-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.step-indicator {
  color: #606266;
  font-size: 14px;
}

.button-group {
  display: flex;
  gap: 10px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}
</style>