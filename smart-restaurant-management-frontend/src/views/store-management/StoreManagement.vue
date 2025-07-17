<template>
  <div class="store-management">
    <div class="page-header">
      <h2>分店管理</h2>
      <p class="page-description">管理您的所有分店信息</p>
    </div>

    <el-card class="store-list-card">
      <template #header>
        <div class="card-header">
          <span>店铺列表</span>
          <el-button type="primary" @click="addStore">
            <el-icon><Plus /></el-icon>
            添加分店
          </el-button>
        </div>
      </template>

      <el-table :data="stores" style="width: 100%" v-loading="loading">
        <el-table-column prop="storeName" label="店铺名称" width="200" />
        <el-table-column prop="address" label="地址" />
        <el-table-column prop="phone" label="电话" width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'ACTIVE' ? 'success' : 'danger'">
              {{ scope.row.status === 'ACTIVE' ? '营业中' : '暂停营业' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button size="small" @click="editStore(scope.row)">编辑</el-button>
            <el-button
              v-if="!scope.row.isDefault"
              size="small"
              type="danger"
              @click="deleteStore(scope.row)"
            >
              删除
            </el-button>
            <el-tooltip v-else content="总店不允许删除" placement="top">
              <el-button size="small" type="danger" disabled>删除</el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑店铺对话框 -->
    <el-dialog
      :title="editingStore ? '编辑店铺' : '添加分店'"
      v-model="showAddStoreDialog"
      width="500px"
    >
      <el-form :model="storeForm" :rules="storeRules" ref="storeFormRef" label-width="100px">
        <el-form-item label="店铺名称" prop="storeName">
          <el-input v-model="storeForm.storeName" placeholder="请输入店铺名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="店铺地址" prop="address">
          <el-input v-model="storeForm.address" placeholder="请输入店铺地址（可稍后设置）" maxlength="200" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="storeForm.phone" placeholder="请输入联系电话" maxlength="20" />
        </el-form-item>
        <el-form-item label="营业状态" prop="status">
          <el-select v-model="storeForm.status" placeholder="请选择营业状态">
            <el-option label="营业中" value="ACTIVE" />
            <el-option label="暂停营业" value="INACTIVE" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="cancelStoreEdit">取消</el-button>
          <el-button type="primary" @click="handleStoreSave" :loading="storeLoading">
            {{ editingStore ? '更新' : '添加' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import { useUserStore } from '../../store/user';
import api from '../../utils/api';

const userStore = useUserStore();
const stores = ref([]);
const loading = ref(false);
const showAddStoreDialog = ref(false);
const editingStore = ref(null);
const storeFormRef = ref();
const storeLoading = ref(false);

const storeForm = ref({
  storeName: '',
  address: '待设置',
  phone: userStore.user?.phone || '',
  status: 'ACTIVE'
});

const storeRules = {
  storeName: [
    { required: true, message: '请输入店铺名称', trigger: 'blur' },
    { min: 2, max: 50, message: '店铺名称长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  address: [
    { max: 200, message: '地址长度不能超过 200 个字符', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择营业状态', trigger: 'change' }
  ]
};

// 获取店铺列表
const fetchStores = async () => {
  try {
    loading.value = true;
    const response = await api.get('/api/stores');
    stores.value = response.data;
  } catch (error) {
    ElMessage.error('获取店铺列表失败: ' + (error.response?.data || error.message));
  } finally {
    loading.value = false;
  }
};

// 添加店铺
const addStore = () => {
  editingStore.value = null;
  storeForm.value = {
    storeName: '',
    address: '待设置',
    phone: userStore.user?.phone || '',
    status: 'ACTIVE'
  };
  showAddStoreDialog.value = true;
};

// 编辑店铺
const editStore = (store) => {
  editingStore.value = store;
  storeForm.value = {
    storeName: store.storeName,
    address: store.address || '待设置',
    phone: store.phone || '',
    status: store.status
  };
  showAddStoreDialog.value = true;
};

// 删除店铺
const deleteStore = async (store) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除店铺"${store.storeName}"吗？此操作不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );

    await api.delete(`/api/stores/${store.id}`);
    ElMessage.success('店铺删除成功！');
    await fetchStores();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败: ' + (error.response?.data || error.message));
    }
  }
};

// 保存店铺
const handleStoreSave = async () => {
  try {
    await storeFormRef.value.validate();
    storeLoading.value = true;

    if (editingStore.value) {
      // 更新店铺
      await api.put(`/api/stores/${editingStore.value.id}`, storeForm.value);
      ElMessage.success('店铺更新成功！');
    } else {
      // 添加店铺
      await api.post('/api/stores', storeForm.value);
      ElMessage.success('店铺添加成功！');
    }

    showAddStoreDialog.value = false;
    await fetchStores();
  } catch (error) {
    ElMessage.error('保存失败: ' + (error.response?.data || error.message));
  } finally {
    storeLoading.value = false;
  }
};

// 取消编辑
const cancelStoreEdit = () => {
  showAddStoreDialog.value = false;
  editingStore.value = null;
  storeForm.value = {
    storeName: '',
    address: '待设置',
    phone: userStore.user?.phone || '',
    status: 'ACTIVE'
  };
};

onMounted(() => {
  fetchStores();
});
</script>

<style scoped>
.store-management {
  padding: 20px;
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

.store-list-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  color: #303133;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
