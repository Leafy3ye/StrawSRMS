<template>
  <div>
    <el-card class="policy-container">
      <template #header>
        <div class="policy-header">
          <h2>会员政策</h2>
          <el-button type="primary" @click="showAddDialog = true">
            <el-icon><Plus /></el-icon>
            添加政策
          </el-button>
        </div>
      </template>

      <!-- 会员政策列表 -->
      <el-table :data="policies" style="width: 100%" stripe v-loading="loading">
        <el-table-column prop="level" label="会员等级" width="120" />
        <el-table-column prop="discount" label="折扣率" width="120">
          <template #default="scope">
            {{ (scope.row.discount * 10).toFixed(1) }}折
          </template>
        </el-table-column>
        <el-table-column prop="pointRate" label="积分比例" width="120">
          <template #default="scope">
            {{ scope.row.pointRate }}倍
          </template>
        </el-table-column>
        <el-table-column prop="benefits" label="会员权益" />
        <el-table-column prop="upgradeCondition" label="升级条件" width="200" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="editPolicy(scope.row)">
              编辑
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 添加/编辑政策对话框 -->
      <el-dialog v-model="showDialog" :title="isEdit ? '编辑会员政策' : '添加会员政策'" width="500px">
        <el-form :model="policyForm" label-width="100px" :rules="policyRules" ref="policyFormRef">
          <el-form-item label="会员等级" prop="level">
            <el-select v-model="policyForm.level" placeholder="请选择会员等级" :disabled="isEdit">
              <el-option label="普通会员" value="普通会员" />
              <el-option label="银卡会员" value="银卡会员" />
              <el-option label="金卡会员" value="金卡会员" />
              <el-option label="钻石会员" value="钻石会员" />
            </el-select>
          </el-form-item>
          <el-form-item label="折扣率" prop="discount">
            <el-input-number 
              v-model="policyForm.discount" 
              :min="0.1" 
              :max="1" 
              :step="0.05"
              :precision="2"
            />
            <span class="form-tip">例如：0.9表示9折</span>
          </el-form-item>
          <el-form-item label="积分比例" prop="pointRate">
            <el-input-number 
              v-model="policyForm.pointRate" 
              :min="1" 
              :max="10" 
              :step="1"
            />
            <span class="form-tip">例如：2表示积分双倍</span>
          </el-form-item>
          <el-form-item label="会员权益" prop="benefits">
            <el-input 
              v-model="policyForm.benefits" 
              type="textarea" 
              rows="3"
              placeholder="请输入会员权益，如：每月免费饮品一杯"
            />
          </el-form-item>
          <el-form-item label="升级条件" prop="upgradeCondition">
            <el-input 
              v-model="policyForm.upgradeCondition" 
              placeholder="请输入升级条件，如：累计消费满1000元"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showDialog = false">取消</el-button>
          <el-button type="primary" @click="savePolicy" :loading="submitLoading">确定</el-button>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import api from '../utils/api';

// 响应式数据
const policies = ref([]);
const loading = ref(false);
const submitLoading = ref(false);
const showDialog = ref(false);
const isEdit = ref(false);
const policyFormRef = ref();

const policyForm = ref({
  level: '',
  discount: 1,
  pointRate: 1,
  benefits: '',
  upgradeCondition: ''
});

// 表单验证规则
const policyRules = {
  level: [
    { required: true, message: '请选择会员等级', trigger: 'change' }
  ],
  discount: [
    { required: true, message: '请输入折扣率', trigger: 'blur' }
  ],
  pointRate: [
    { required: true, message: '请输入积分比例', trigger: 'blur' }
  ],
  benefits: [
    { required: true, message: '请输入会员权益', trigger: 'blur' }
  ],
  upgradeCondition: [
    { required: true, message: '请输入升级条件', trigger: 'blur' }
  ]
};

// 加载会员政策列表
const loadPolicies = async () => {
  try {
    loading.value = true;
    // 这里假设后端有对应的API，如果没有，可以先使用模拟数据
    // const response = await api.get('/api/member-policies');
    // policies.value = response.data;
    
    // 模拟数据
    policies.value = [
      {
        id: 1,
        level: '待开发',
        discount: 1,
        pointRate: 1,
        benefits: '待开发',
        upgradeCondition: '待开发'
      },
    ];
  } catch (error) {
    console.error('加载会员政策失败:', error);
    ElMessage.error('加载会员政策失败');
  } finally {
    loading.value = false;
  }
};

// 显示添加对话框
const showAddDialog = () => {
  isEdit.value = false;
  policyForm.value = {
    level: '',
    discount: 1,
    pointRate: 1,
    benefits: '',
    upgradeCondition: ''
  };
  showDialog.value = true;
};

// 显示编辑对话框
const editPolicy = (policy) => {
  isEdit.value = true;
  policyForm.value = { ...policy };
  showDialog.value = true;
};

// 保存会员政策
const savePolicy = async () => {
  if (!policyFormRef.value) return;
  
  try {
    const valid = await policyFormRef.value.validate();
    if (!valid) return;
    
    submitLoading.value = true;
    
    if (isEdit.value) {
      // 编辑现有政策
      // await api.put(`/api/member-policies/${policyForm.value.id}`, policyForm.value);
      
      // 模拟更新
      const index = policies.value.findIndex(p => p.id === policyForm.value.id);
      if (index !== -1) {
        policies.value[index] = { ...policyForm.value };
      }
      
      ElMessage.success('会员政策更新成功');
    } else {
      // 添加新政策
      // const response = await api.post('/api/member-policies', policyForm.value);
      
      // 模拟添加
      const newPolicy = {
        ...policyForm.value,
        id: Math.max(...policies.value.map(p => p.id), 0) + 1
      };
      policies.value.push(newPolicy);
      
      ElMessage.success('会员政策添加成功');
    }
    
    showDialog.value = false;
  } catch (error) {
    console.error('保存会员政策失败:', error);
    ElMessage.error('保存会员政策失败');
  } finally {
    submitLoading.value = false;
  }
};

// 组件挂载时加载数据
onMounted(() => {
  loadPolicies();
});
</script>

<style scoped>
.policy-container {
  margin: 20px;
  min-height: calc(100vh - 140px);
}

.policy-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0;
}

.policy-header h2 {
  margin: 0;
  color: #333;
  font-size: 24px;
  font-weight: 600;
}

.form-tip {
  margin-left: 10px;
  color: #909399;
  font-size: 12px;
}

/* 表格样式优化 */
.el-table {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  margin-top: 20px;
}

/* 对话框样式 */
.el-dialog {
  border-radius: 12px;
}

.el-dialog__header {
  background-color: #f8f9fa;
  border-bottom: 1px solid #ebeef5;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .policy-container {
    margin: 10px;
  }
  
  .policy-header {
    flex-direction: column;
    gap: 15px;
    align-items: stretch;
  }
}
</style>