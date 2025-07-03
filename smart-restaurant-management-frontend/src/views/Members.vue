<template>
  <div>
    <el-card class="members-container">
      <template #header>
        <div class="members-header">
          <h2>会员管理</h2>
          <el-button type="primary" @click="showAddDialog = true">
            <el-icon><Plus /></el-icon>
            添加会员
          </el-button>
        </div>
      </template>
  
      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input
          v-model="searchQuery"
          placeholder="搜索会员姓名或手机号（支持拼音）"
          clearable
          style="width: 300px; margin-right: 10px;"
        />
        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
      </div>
  
      <!-- 会员列表 -->
      <el-table :data="members" style="width: 100%" stripe v-loading="loading">
        <el-table-column prop="id" label="会员ID" width="100" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="150" />
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column label="余额" width="180" align="center">
          <template #default="scope">
            <div class="balance-cell">
              <span class="balance-amount">¥{{ scope.row.balance.toFixed(2) }}</span>
              <el-button 
                type="success" 
                size="small" 
                @click="showRechargeDialog(scope.row)"
                class="recharge-btn"
              >
                充值
              </el-button>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="level" label="会员等级" width="120" />
        <el-table-column prop="createdAt" label="注册时间" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button type="primary" size="small" @click="editMember(scope.row)">
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="deleteMember(scope.row.id)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
  
      <!-- 添加会员对话框 -->
      <el-dialog v-model="showAddDialog" title="添加会员" width="500px">
        <el-form :model="newMember" label-width="80px" :rules="memberRules" ref="memberFormRef">
          <el-form-item label="姓名" prop="name">
            <el-input v-model="newMember.name" placeholder="请输入会员姓名" />
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="newMember.phone" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="newMember.email" placeholder="请输入邮箱" />
          </el-form-item>
          <el-form-item label="会员等级" prop="level">
            <el-select v-model="newMember.level" placeholder="请选择会员等级">
              <el-option label="普通会员" value="普通会员" />
              <el-option label="银卡会员" value="银卡会员" />
              <el-option label="金卡会员" value="金卡会员" />
              <el-option label="钻石会员" value="钻石会员" />
            </el-select>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showAddDialog = false">取消</el-button>
          <el-button type="primary" @click="addMember" :loading="submitLoading">确定</el-button>
        </template>
      </el-dialog>
  
      <!-- 编辑会员对话框 -->
      <el-dialog v-model="showEditDialog" title="编辑会员" width="500px">
        <el-form :model="editMemberForm" label-width="80px" :rules="memberRules" ref="editFormRef">
          <el-form-item label="姓名" prop="name">
            <el-input v-model="editMemberForm.name" placeholder="请输入会员姓名" />
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="editMemberForm.phone" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="editMemberForm.email" placeholder="请输入邮箱" />
          </el-form-item>
          <el-form-item label="会员等级" prop="level">
            <el-select v-model="editMemberForm.level" placeholder="请选择会员等级">
              <el-option label="普通会员" value="普通会员" />
              <el-option label="银卡会员" value="银卡会员" />
              <el-option label="金卡会员" value="金卡会员" />
              <el-option label="钻石会员" value="钻石会员" />
            </el-select>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showEditDialog = false">取消</el-button>
          <el-button type="primary" @click="updateMember" :loading="submitLoading">确定</el-button>
        </template>
      </el-dialog>
  
      <!-- 充值对话框 -->
      <el-dialog v-model="showRechargeDialogVisible" title="会员充值" width="400px">
        <el-form :model="rechargeForm" label-width="80px" :rules="rechargeRules" ref="rechargeFormRef">
          <el-form-item label="会员姓名">
            <el-input v-model="currentMember.name" disabled />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="currentMember.phone" disabled />
          </el-form-item>
          <el-form-item label="当前余额">
            <el-input :value="'¥' + currentMember.balance?.toFixed(2)" disabled />
          </el-form-item>
          <el-form-item label="充值金额" prop="amount">
            <el-input 
              v-model="rechargeForm.amount" 
              placeholder="请输入充值金额"
              type="number"
              step="0.01"
            />
          </el-form-item>
          <el-form-item label="支付方式" prop="paymentMethod">
            <el-select v-model="rechargeForm.paymentMethod" placeholder="请选择支付方式">
              <el-option label="现金" value="现金" />
              <el-option label="支付宝" value="支付宝" />
              <el-option label="微信" value="微信" />
              <el-option label="银行卡" value="银行卡" />
            </el-select>
          </el-form-item>
          <el-form-item label="备注">
            <el-input 
              v-model="rechargeForm.remark" 
              placeholder="请输入备注信息（可选）"
              type="textarea"
              rows="2"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showRechargeDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleRecharge" :loading="rechargeLoading">确认充值</el-button>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Search } from '@element-plus/icons-vue';
import api from '../utils/api';
import { pinyin } from 'pinyin-pro';

// 响应式数据
const allMembers = ref([]);
const members = ref([]);
const searchQuery = ref('');
const loading = ref(false);
const submitLoading = ref(false);
const rechargeLoading = ref(false);
const showAddDialog = ref(false);
const showEditDialog = ref(false);
const showRechargeDialogVisible = ref(false);
const currentMember = ref({});
const memberFormRef = ref();
const editFormRef = ref();
const rechargeFormRef = ref();

const newMember = ref({
  name: '',
  phone: '',
  email: '',
  level: '普通会员'
});

const editMemberForm = ref({
  id: null,
  name: '',
  phone: '',
  email: '',
  level: '普通会员'
});

const rechargeForm = ref({
  amount: '',
  paymentMethod: '现金',
  remark: ''
});

// 表单验证规则
const memberRules = {
  name: [
    { required: true, message: '请输入会员姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  level: [
    { required: true, message: '请选择会员等级', trigger: 'change' }
  ]
};

const rechargeRules = {
  amount: [
    { required: true, message: '请输入充值金额', trigger: 'blur' },
    { pattern: /^\d+(\.\d{1,2})?$/, message: '请输入正确的金额格式', trigger: 'blur' }
  ],
  paymentMethod: [
    { required: true, message: '请选择支付方式', trigger: 'change' }
  ]
};

// 过滤后的会员列表
const filteredMembers = computed(() => {
  if (!searchQuery.value.trim()) {
    return allMembers.value;
  }
  
  const query = searchQuery.value.toLowerCase().trim();
  
  return allMembers.value.filter(member => {
    // 直接匹配姓名或手机号
    if (member.name.toLowerCase().includes(query) || 
        member.phone.includes(query)) {
      return true;
    }
    
    // 拼音匹配
    try {
      const namePinyin = pinyin(member.name, { toneType: 'none', type: 'array' });
      
      // 全拼匹配
      const fullPinyin = namePinyin.join('').toLowerCase();
      if (fullPinyin.includes(query)) {
        return true;
      }
      
      // 首字母匹配
      const firstLetters = namePinyin.map(py => py.charAt(0)).join('').toLowerCase();
      if (firstLetters.includes(query)) {
        return true;
      }
      
      // 部分拼音匹配
      if (namePinyin.some(py => py.toLowerCase().startsWith(query))) {
        return true;
      }
      
      // 连续拼音匹配
      const continuousPinyin = namePinyin.join(' ').toLowerCase();
      if (continuousPinyin.includes(query)) {
        return true;
      }
    } catch (error) {
      console.warn('拼音转换失败:', error);
    }
    
    return false;
  });
});

// 监听搜索查询变化，实时过滤
watch(searchQuery, () => {
  members.value = filteredMembers.value;
});

// 加载会员列表
const loadMembers = async () => {
  try {
    loading.value = true;
    const response = await api.get('/api/members');
    allMembers.value = response.data;
    members.value = response.data;
  } catch (error) {
    console.error('加载会员列表失败:', error);
    ElMessage.error('加载会员列表失败');
  } finally {
    loading.value = false;
  }
};

// 删除原有的 handleSearch 函数，因为现在使用实时搜索

// 添加会员
const addMember = async () => {
  if (!memberFormRef.value) return;
  
  try {
    const valid = await memberFormRef.value.validate();
    if (!valid) return;
    
    submitLoading.value = true;
    const response = await api.post('/api/members', newMember.value);
    
    members.value.push(response.data);
    showAddDialog.value = false;
    
    // 重置表单
    newMember.value = {
      name: '',
      phone: '',
      email: '',
      level: '普通会员'
    };
    memberFormRef.value.resetFields();
    
    ElMessage.success('会员添加成功');
  } catch (error) {
    console.error('添加会员失败:', error);
    if (error.response?.status === 409) {
      ElMessage.error('手机号已存在');
    } else {
      ElMessage.error('添加会员失败');
    }
  } finally {
    submitLoading.value = false;
  }
};

// 显示编辑对话框
const editMember = (member) => {
  editMemberForm.value = { ...member };
  showEditDialog.value = true;
};

// 更新会员信息
const updateMember = async () => {
  if (!editFormRef.value) return;
  
  try {
    const valid = await editFormRef.value.validate();
    if (!valid) return;
    
    submitLoading.value = true;
    const response = await api.put(`/api/members/${editMemberForm.value.id}`, editMemberForm.value);
    
    // 更新本地数据
    const index = members.value.findIndex(m => m.id === editMemberForm.value.id);
    if (index !== -1) {
      members.value[index] = response.data;
    }
    
    showEditDialog.value = false;
    ElMessage.success('会员信息更新成功');
  } catch (error) {
    console.error('更新会员失败:', error);
    if (error.response?.status === 409) {
      ElMessage.error('手机号已存在');
    } else {
      ElMessage.error('更新会员失败');
    }
  } finally {
    submitLoading.value = false;
  }
};

// 删除会员
const deleteMember = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个会员吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    await api.delete(`/api/members/${id}`);
    members.value = members.value.filter(member => member.id !== id);
    ElMessage.success('删除成功');
  } catch (error) {
    if (error === 'cancel') {
      ElMessage.info('已取消删除');
    } else {
      console.error('删除会员失败:', error);
      ElMessage.error('删除会员失败');
    }
  }
};

// 显示充值对话框
const showRechargeDialog = (member) => {
  currentMember.value = { ...member };
  rechargeForm.value = {
    amount: '',
    paymentMethod: '现金',
    remark: ''
  };
  showRechargeDialogVisible.value = true;
};

// 处理充值
const handleRecharge = async () => {
  if (!rechargeFormRef.value) return;
  
  try {
    const valid = await rechargeFormRef.value.validate();
    if (!valid) return;
    
    const amount = parseFloat(rechargeForm.value.amount);
    if (amount <= 0) {
      ElMessage.warning('充值金额必须大于0');
      return;
    }
    
    rechargeLoading.value = true;
    
    // 创建充值记录
    const rechargeData = {
      memberId: currentMember.value.id,
      amount: amount,
      paymentMethod: rechargeForm.value.paymentMethod,
      remark: rechargeForm.value.remark
    };
    
    await api.post('/api/members/recharge', rechargeData);
    
    // 更新会员余额
    const memberIndex = members.value.findIndex(m => m.id === currentMember.value.id);
    if (memberIndex !== -1) {
      members.value[memberIndex].balance += amount;
    }
    
    ElMessage.success(`充值成功！充值金额：¥${amount.toFixed(2)}`);
    showRechargeDialogVisible.value = false;
    
    // 重新加载会员数据以确保数据同步
    await loadMembers();
  } catch (error) {
    console.error('充值失败:', error);
    ElMessage.error('充值失败');
  } finally {
    rechargeLoading.value = false;
  }
};

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return '';
  const date = new Date(timeStr);
  return date.toLocaleString('zh-CN');
};

// 组件挂载时加载数据
onMounted(() => {
  loadMembers();
});
</script>

<style scoped>
/* 主容器样式 - 与其他页面保持统一 */
.members-container {
  margin: 20px;
  min-height: calc(100vh - 140px);
}

/* 头部样式 */
.members-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0;
}

.members-header h2 {
  margin: 0;
  color: #333;
  font-size: 24px;
  font-weight: 600;
}

/* 搜索栏样式 */
.search-bar {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  padding: 20px 0;
  border-bottom: 1px solid #ebeef5;
}

/* 余额单元格样式 - 解决充值按钮对齐问题 */
.balance-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
}

.balance-amount {
  color: #409EFF;
  font-weight: bold;
  font-size: 16px;
  min-height: 20px;
  display: flex;
  align-items: center;
}

.recharge-btn {
  width: 60px;
  height: 28px;
  font-size: 12px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 表格样式优化 */
.el-table {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
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
  .members-container {
    margin: 10px;
  }
  
  .members-header {
    flex-direction: column;
    gap: 15px;
    align-items: stretch;
  }
  
  .search-bar {
    flex-direction: column;
    gap: 10px;
    align-items: stretch;
  }
  
  .search-bar .el-input {
    width: 100% !important;
  }
}
</style>