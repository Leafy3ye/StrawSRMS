<template>
  <div>
    <el-card class="policy-container">
      <template #header>
        <div class="policy-header">
          <h2>会员等级管理</h2>
          <el-button type="primary" @click="showAddDialog">
            <el-icon><Plus /></el-icon>
            添加等级
          </el-button>
        </div>
      </template>

      <!-- 会员等级列表 -->
      <el-table :data="memberLevels" style="width: 100%" stripe v-loading="loading">
        <el-table-column prop="levelName" label="等级名称" width="120" />
        <el-table-column prop="discountRate" label="折扣率" width="120">
          <template #default="scope">
            {{ (scope.row.discountRate * 10).toFixed(1) }}折
          </template>
        </el-table-column>
        <el-table-column prop="pointRate" label="积分倍率" width="120">
          <template #default="scope">
            {{ scope.row.pointRate }}倍
          </template>
        </el-table-column>
        <el-table-column prop="benefits" label="会员权益" />
        <el-table-column prop="upgradeCondition" label="升级条件" width="200" />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="状态" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.isEnabled ? 'success' : 'danger'">
              {{ scope.row.isEnabled ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="editMemberLevel(scope.row)">
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="deleteMemberLevel(scope.row.id)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 添加/编辑等级对话框 -->
      <el-dialog v-model="showDialog" :title="isEdit ? '编辑会员等级' : '添加会员等级'" width="600px">
        <el-form :model="levelForm" label-width="100px" :rules="levelRules" ref="levelFormRef">
          <el-form-item label="等级名称" prop="levelName">
            <el-input v-model="levelForm.levelName" placeholder="请输入等级名称" />
          </el-form-item>
          <el-form-item label="折扣率" prop="discountRate">
            <el-input-number 
              v-model="levelForm.discountRate" 
              :min="0.1" 
              :max="1" 
              :step="0.05"
              :precision="2"
              style="width: 200px"
            />
            <span class="form-tip">例如：0.9表示9折</span>
          </el-form-item>
          <el-form-item label="积分倍率" prop="pointRate">
            <el-input-number 
              v-model="levelForm.pointRate" 
              :min="1" 
              :max="10" 
              :step="1"
              style="width: 200px"
            />
            <span class="form-tip">例如：2表示积分双倍</span>
          </el-form-item>
          <el-form-item label="排序权重" prop="sortOrder">
            <el-input-number 
              v-model="levelForm.sortOrder" 
              :min="1" 
              :max="100" 
              :step="1"
              style="width: 200px"
            />
            <span class="form-tip">数字越小优先级越高</span>
          </el-form-item>
          <el-form-item label="会员权益" prop="benefits">
            <el-input 
              v-model="levelForm.benefits" 
              type="textarea" 
              rows="3"
              placeholder="请输入会员权益，如：每月免费饮品一杯"
            />
          </el-form-item>
          <el-form-item label="升级条件" prop="upgradeCondition">
            <el-input 
              v-model="levelForm.upgradeCondition" 
              placeholder="请输入升级条件，如：累计消费满1000元"
            />
          </el-form-item>
          <el-form-item label="状态" prop="isEnabled">
            <el-switch v-model="levelForm.isEnabled" active-text="启用" inactive-text="禁用" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showDialog = false">取消</el-button>
          <el-button type="primary" @click="saveMemberLevel" :loading="submitLoading">确定</el-button>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import api from '../utils/api';

// 响应式数据
const memberLevels = ref([]);
const loading = ref(false);
const submitLoading = ref(false);
const showDialog = ref(false);
const isEdit = ref(false);
const levelFormRef = ref();

const levelForm = ref({
  levelName: '',
  discountRate: 1,
  pointRate: 1,
  benefits: '',
  upgradeCondition: '',
  sortOrder: 1,
  isEnabled: true
});

// 表单验证规则
const levelRules = {
  levelName: [
    { required: true, message: '请输入等级名称', trigger: 'blur' }
  ],
  discountRate: [
    { required: true, message: '请输入折扣率', trigger: 'blur' }
  ],
  pointRate: [
    { required: true, message: '请输入积分倍率', trigger: 'blur' }
  ],
  benefits: [
    { required: true, message: '请输入会员权益', trigger: 'blur' }
  ],
  upgradeCondition: [
    { required: true, message: '请输入升级条件', trigger: 'blur' }
  ],
  sortOrder: [
    { required: true, message: '请输入排序权重', trigger: 'blur' }
  ]
};

// 加载会员等级列表
const loadMemberLevels = async () => {
  try {
    loading.value = true;
    const response = await api.get('/api/member-levels');
    memberLevels.value = response.data;
  } catch (error) {
    console.error('加载会员等级失败:', error);
    ElMessage.error('加载会员等级失败');
  } finally {
    loading.value = false;
  }
};

// 显示添加对话框
const showAddDialog = () => {
  isEdit.value = false;
  levelForm.value = {
    levelName: '',
    discountRate: 1,
    pointRate: 1,
    benefits: '',
    upgradeCondition: '',
    sortOrder: memberLevels.value.length + 1,
    isEnabled: true
  };
  showDialog.value = true;
};

// 显示编辑对话框
const editMemberLevel = (level) => {
  isEdit.value = true;
  levelForm.value = { ...level };
  showDialog.value = true;
};

// 保存会员等级
const saveMemberLevel = async () => {
  if (!levelFormRef.value) return;
  
  try {
    const valid = await levelFormRef.value.validate();
    if (!valid) return;
    
    submitLoading.value = true;
    
    if (isEdit.value) {
      await api.put(`/api/member-levels/${levelForm.value.id}`, levelForm.value);
      ElMessage.success('会员等级更新成功');
    } else {
      await api.post('/api/member-levels', levelForm.value);
      ElMessage.success('会员等级添加成功');
    }
    
    showDialog.value = false;
    loadMemberLevels();
  } catch (error) {
    console.error('保存会员等级失败:', error);
    ElMessage.error(error.response?.data || '保存会员等级失败');
  } finally {
    submitLoading.value = false;
  }
};

// 删除会员等级
const deleteMemberLevel = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个会员等级吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    await api.delete(`/api/member-levels/${id}`);
    ElMessage.success('删除成功');
    loadMemberLevels();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除会员等级失败:', error);
      ElMessage.error('删除失败');
    }
  }
};

// 组件挂载时加载数据
onMounted(() => {
  loadMemberLevels();
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

.el-table {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  margin-top: 20px;
}

.el-dialog {
  border-radius: 12px;
}

.el-dialog__header {
  background-color: #f8f9fa;
  border-bottom: 1px solid #ebeef5;
}

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