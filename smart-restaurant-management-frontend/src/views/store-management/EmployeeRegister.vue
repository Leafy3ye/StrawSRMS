<template>
  <div class="employee-register">
    <div class="page-header">
      <h2>店员注册</h2>
      <p class="page-description">为您的分店注册员工账号</p>
    </div>

    <el-card class="register-card">
      <template #header>
        <div class="card-header">
          <span>注册新员工</span>
        </div>
      </template>

      <el-form :model="employeeForm" :rules="employeeRules" ref="employeeFormRef" label-width="120px">
        <el-form-item label="所属店铺" prop="storeId">
          <el-select v-model="employeeForm.storeId" placeholder="请选择员工所属的店铺" style="width: 100%">
            <el-option
              v-for="store in stores"
              :key="store.id"
              :label="store.storeName + (store.isDefault ? ' (总店)' : '')"
              :value="store.id"
            >
              <span>{{ store.storeName }}</span>
              <span v-if="store.isDefault" style="color: #f56c6c; margin-left: 8px;">(总店)</span>
            </el-option>
          </el-select>
          <div class="form-tip">员工将只能访问指定店铺的数据</div>
        </el-form-item>

        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="employeeForm.username"
            placeholder="请输入用户名"
            maxlength="20"
          />
          <div class="form-tip">用户名用于登录，建议使用员工姓名拼音</div>
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="employeeForm.email"
            placeholder="请输入邮箱地址"
            maxlength="50"
          />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="employeeForm.phone"
            placeholder="请输入手机号码"
            maxlength="11"
          />
        </el-form-item>

        <el-form-item label="登录密码" prop="password">
          <el-input
            v-model="employeeForm.password"
            type="password"
            placeholder="请输入登录密码"
            maxlength="20"
            show-password
          />
          <div class="form-tip">密码长度至少6位，建议包含字母和数字</div>
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="employeeForm.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            maxlength="20"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleRegister" :loading="loading">
            注册员工
          </el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 已注册员工列表 -->
    <el-card class="employee-list-card" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>已注册员工</span>
        </div>
      </template>

      <el-table :data="employees" style="width: 100%" v-loading="employeeLoading">
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column prop="phone" label="手机号" width="150" />
        <el-table-column label="所属店铺" width="150">
          <template #default="scope">
            {{ getStoreName(scope.row.storeId) }}
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="scope">
            <el-button size="small" type="danger" @click="deleteEmployee(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import api from '../../utils/api';

const employeeFormRef = ref();
const loading = ref(false);
const employeeLoading = ref(false);
const stores = ref([]);
const employees = ref([]);

const employeeForm = ref({
  storeId: '',
  username: '',
  email: '',
  phone: '',
  password: '',
  confirmPassword: ''
});

// 验证确认密码
const validateConfirmPassword = (rule, value, callback) => {
  if (value !== employeeForm.value.password) {
    callback(new Error('两次输入的密码不一致'));
  } else {
    callback();
  }
};

const employeeRules = {
  storeId: [
    { required: true, message: '请选择员工所属的店铺', trigger: 'change' }
  ],
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入登录密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
};

// 获取店铺列表
const fetchStores = async () => {
  try {
    const response = await api.get('/api/stores');
    stores.value = response.data;
  } catch (error) {
    ElMessage.error('获取店铺列表失败: ' + (error.response?.data || error.message));
  }
};

// 获取员工列表
const fetchEmployees = async () => {
  try {
    employeeLoading.value = true;
    const response = await api.get('/api/users/employees');
    employees.value = response.data;
  } catch (error) {
    ElMessage.error('获取员工列表失败: ' + (error.response?.data || error.message));
  } finally {
    employeeLoading.value = false;
  }
};

// 注册员工
const handleRegister = async () => {
  try {
    await employeeFormRef.value.validate();
    loading.value = true;

    const registerData = {
      storeId: employeeForm.value.storeId,
      username: employeeForm.value.username,
      email: employeeForm.value.email,
      phone: employeeForm.value.phone,
      password: employeeForm.value.password
    };

    await api.post('/api/users/register-employee', registerData);
    ElMessage.success('员工注册成功！');
    
    resetForm();
    await fetchEmployees();
  } catch (error) {
    ElMessage.error('注册失败: ' + (error.response?.data || error.message));
  } finally {
    loading.value = false;
  }
};

// 重置表单
const resetForm = () => {
  employeeFormRef.value?.resetFields();
  employeeForm.value = {
    storeId: '',
    username: '',
    email: '',
    phone: '',
    password: '',
    confirmPassword: ''
  };
};

// 删除员工
const deleteEmployee = async (employee) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除员工"${employee.username}"吗？此操作不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );

    await api.delete(`/api/users/employees/${employee.id}`);
    ElMessage.success('员工删除成功！');
    await fetchEmployees();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败: ' + (error.response?.data || error.message));
    }
  }
};

// 获取店铺名称
const getStoreName = (storeId) => {
  const store = stores.value.find(s => s.id === storeId);
  return store ? store.storeName + (store.isDefault ? ' (总店)' : '') : '未知店铺';
};

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '';
  return new Date(dateString).toLocaleString('zh-CN');
};

onMounted(() => {
  fetchStores();
  fetchEmployees();
});
</script>

<style scoped>
.employee-register {
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

.register-card,
.employee-list-card {
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
