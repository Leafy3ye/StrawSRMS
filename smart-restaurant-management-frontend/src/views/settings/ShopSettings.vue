<template>
  <div class="shop-settings">
    <div class="page-header">
      <h2>店铺管理</h2>
      <p>{{ isMultiStoreMode ? '管理您的多家店铺' : '配置您的店铺基本信息' }}</p>
    </div>

    <!-- 多店铺模式 -->
    <div v-if="isMultiStoreMode">
      <!-- 店铺列表 -->
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
          <el-table-column prop="address" label="地址" width="300" />
          <el-table-column prop="phone" label="电话" width="150" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.status === 'ACTIVE' ? 'success' : 'danger'">
                {{ scope.row.status === 'ACTIVE' ? '营业中' : '暂停营业' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="isDefault" label="类型" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.isDefault ? 'warning' : 'info'">
                {{ scope.row.isDefault ? '总店' : '分店' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="250">
            <template #default="scope">
              <el-button type="primary" size="small" @click="editStore(scope.row)">
                编辑
              </el-button>
              <el-button type="success" size="small" @click="switchStore(scope.row)" style="margin-left: 5px;">
                切换
              </el-button>
              <el-button
                v-if="!scope.row.isDefault"
                type="danger"
                size="small"
                @click="deleteStore(scope.row)"
                style="margin-left: 5px;"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <!-- 单店铺模式 -->
    <div v-else>
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

    <!-- 租户名称设置（所有模式都显示） -->
    <el-card style="margin-top: 20px;">
      <template #header>
        <span>品牌名称设置</span>
      </template>

      <el-form :model="tenantForm" :rules="tenantRules" ref="tenantFormRef" label-width="120px">
        <el-form-item label="品牌名称" prop="tenantName">
          <el-input
            v-model="tenantForm.tenantName"
            placeholder="请输入品牌名称（显示在左上角logo旁边）"
            maxlength="50"
          />
          <div class="form-tip">这个名称将显示在系统左上角logo旁边</div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleTenantSave" :loading="tenantLoading">
            保存品牌名称
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 添加/编辑店铺对话框 -->
    <el-dialog
      v-model="showAddStoreDialog"
      :title="editingStore ? '编辑店铺' : '添加分店'"
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
        <el-button @click="cancelStoreEdit">取消</el-button>
        <el-button type="primary" @click="saveStore" :loading="storeLoading">
          {{ editingStore ? '更新' : '添加' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useUserStore } from '../../store/user';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import api from '../../utils/api';

const userStore = useUserStore();
const shopFormRef = ref();
const storeFormRef = ref();
const loading = ref(false);
const storeLoading = ref(false);

// 店铺列表
const stores = ref([]);
const showAddStoreDialog = ref(false);
const editingStore = ref(null);

// 判断是否为多店铺模式
const isMultiStoreMode = computed(() => {
  return userStore.user?.storeMode === 'multi';
});

// 单店铺模式表单
const shopForm = ref({
  shopName: ''
});

// 店铺表单
const storeForm = ref({
  storeName: '',
  address: '',
  phone: '',
  status: 'ACTIVE'
});

// 租户表单
const tenantForm = ref({
  tenantName: ''
});

const tenantFormRef = ref();
const tenantLoading = ref(false);

const rules = {
  shopName: [
    { required: true, message: '请输入店铺名称', trigger: 'blur' },
    { min: 2, max: 50, message: '店铺名称长度在 2 到 50 个字符', trigger: 'blur' }
  ]
};

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

const tenantRules = {
  tenantName: [
    { required: true, message: '请输入品牌名称', trigger: 'blur' },
    { min: 2, max: 50, message: '品牌名称长度在 2 到 50 个字符', trigger: 'blur' }
  ]
};

// 初始化数据
onMounted(async () => {
  if (isMultiStoreMode.value) {
    await fetchStores();
  } else {
    // 单店模式：获取默认店铺的名称
    await fetchDefaultStoreName();
  }

  // 初始化租户名称
  await fetchTenantInfo();
});

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

// 获取默认店铺名称（单店模式）
const fetchDefaultStoreName = async () => {
  try {
    const response = await api.get('/api/stores');
    const defaultStore = response.data.find(store => store.isDefault);
    if (defaultStore) {
      shopForm.value.shopName = defaultStore.storeName;
    }
  } catch (error) {
    console.error('获取默认店铺名称失败:', error);
    // 如果获取失败，使用用户的餐厅名称作为备选
    if (userStore.user?.restaurantName) {
      shopForm.value.shopName = userStore.user.restaurantName;
    }
  }
};

// 单店铺模式保存
const handleSave = async () => {
  try {
    await shopFormRef.value.validate();
    loading.value = true;

    // 获取默认店铺并更新其名称
    const storesResponse = await api.get('/api/stores');
    const defaultStore = storesResponse.data.find(store => store.isDefault);

    if (defaultStore) {
      await api.put(`/api/stores/${defaultStore.id}`, {
        storeName: shopForm.value.shopName
      });
    }

    // 同时更新用户的餐厅名称（保持兼容性）
    await userStore.updateShopInfo(shopForm.value);

    ElMessage.success('店铺信息更新成功！');
  } catch (error) {
    console.error('更新失败:', error);
    ElMessage.error('更新失败，请重试');
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
    address: store.address || '',
    phone: store.phone || '',
    status: store.status
  };
  showAddStoreDialog.value = true;
};

// 保存店铺
const saveStore = async () => {
  try {
    await storeFormRef.value.validate();
    storeLoading.value = true;

    if (editingStore.value) {
      // 更新店铺
      await api.put(`/api/stores/${editingStore.value.id}`, storeForm.value);
      ElMessage.success('店铺更新成功！');
    } else {
      // 添加新店铺
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

// 切换店铺
const switchStore = async (store) => {
  try {
    await api.post(`/api/stores/${store.id}/switch`);
    ElMessage.success(`已切换到 ${store.storeName}`);

    // 更新用户信息中的当前店铺ID
    if (userStore.user) {
      userStore.user.currentStoreId = store.id;
      localStorage.setItem('user', JSON.stringify(userStore.user));
    }

    // 刷新页面以更新数据
    window.location.reload();
  } catch (error) {
    ElMessage.error('切换店铺失败: ' + (error.response?.data || error.message));
  }
};

// 删除店铺
const deleteStore = async (store) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除店铺 "${store.storeName}" 吗？此操作不可恢复！`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
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

// 获取租户信息
const fetchTenantInfo = async () => {
  try {
    const response = await api.get('/api/tenants/current');
    tenantForm.value.tenantName = response.data.tenantName || '';
  } catch (error) {
    console.error('获取租户信息失败:', error);
  }
};

// 保存租户名称
const handleTenantSave = async () => {
  try {
    await tenantFormRef.value.validate();
    tenantLoading.value = true;

    await api.put('/api/tenants/current', {
      tenantName: tenantForm.value.tenantName
    });

    ElMessage.success('品牌名称保存成功！');

    // 更新用户store中的信息
    if (userStore.user) {
      userStore.user.tenantName = tenantForm.value.tenantName;
      localStorage.setItem('user', JSON.stringify(userStore.user));
    }

    // 触发导航栏更新
    window.dispatchEvent(new CustomEvent('tenant-updated'));

  } catch (error) {
    ElMessage.error('保存失败: ' + (error.response?.data || error.message));
  } finally {
    tenantLoading.value = false;
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

.store-list-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header span {
  font-weight: 600;
  color: #303133;
}

/* 表格样式优化 */
:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-table th) {
  background-color: #f8f9fa;
  color: #606266;
  font-weight: 600;
}

:deep(.el-table td) {
  border-bottom: 1px solid #f0f0f0;
}

:deep(.el-table tr:hover > td) {
  background-color: #f8f9fa;
}

/* 对话框样式 */
:deep(.el-dialog) {
  border-radius: 12px;
}

:deep(.el-dialog__header) {
  background-color: #f8f9fa;
  border-radius: 12px 12px 0 0;
  padding: 20px 24px 16px;
}

:deep(.el-dialog__title) {
  font-weight: 600;
  color: #303133;
}

:deep(.el-dialog__body) {
  padding: 24px;
}

/* 按钮样式 */
.el-button + .el-button {
  margin-left: 8px;
}

/* 标签样式 */
.el-tag {
  border-radius: 6px;
  font-weight: 500;
}

/* 表单提示样式 */
.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>