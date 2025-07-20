<template>
  <el-card class="main-card">
    <div class="header-section">
      <h2>桌位管理</h2>
      <div class="header-actions">
        <el-button 
          v-if="!editMode" 
          type="primary" 
          @click="toggleEditMode"
          :icon="Edit"
        >
          编辑模式
        </el-button>
        <div v-else class="edit-actions">
          <el-button 
            type="success" 
            @click="showAddTableDialog"
            :icon="Plus"
          >
            新增桌位
          </el-button>
          <el-button 
            type="info" 
            @click="toggleEditMode"
            :icon="Check"
          >
            完成编辑
          </el-button>
        </div>
      </div>
    </div>

    <!-- 桌位管理 -->
    <el-row :gutter="20" class="table-row">
      <el-col :span="5" v-for="table in tables" :key="table.id">
        <el-card 
          class="table-card" 
          @click="editMode ? null : goToTableDetail(table.id)"
          :style="{ backgroundColor: getTableStatus(table.id).color }"
          :draggable="!editMode"
          @dragstart="handleDragStart($event, table.id)"
          @dragover="handleDragOver($event)"
          @drop="handleDrop($event, table.id)"
          @dragenter="handleDragEnter($event)"
          @dragleave="handleDragLeave($event)"
        >
          <div class="table-card-content">
            <h3>{{ table.name || `桌位 ${table.id}` }}</h3>
            <p>{{ getTableStatus(table.id).status }}</p>
          </div>
          
          <!-- 编辑模式下的操作按钮 -->
          <div v-if="editMode" class="edit-buttons">
            <el-button 
              size="small" 
              type="primary" 
              @click="showEditTableDialog(table)"
              :icon="Edit"
              circle
            />
            <el-button 
              size="small" 
              type="danger" 
              @click="deleteTable(table.id)"
              :icon="Delete"
              circle
            />
          </div>
          
          <!-- 拖拽提示 -->
          <div v-if="dragOverTableId === table.id && !editMode" class="drag-overlay">
            <span>松开以转移订单</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 新增桌位对话框 -->
    <el-dialog v-model="addTableDialogVisible" title="新增桌位" width="400px">
      <el-form :model="newTableForm" :rules="tableRules" ref="addTableFormRef">
        <el-form-item label="桌位名称" prop="name">
          <el-input v-model="newTableForm.name" placeholder="请输入桌位名称" />
        </el-form-item>
        <el-form-item label="桌位状态" prop="status">
          <el-select v-model="newTableForm.status" placeholder="请选择状态">
            <el-option label="未开桌" value="未开桌" />
            <el-option label="使用中" value="使用中" />
            <el-option label="维护中" value="维护中" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addTableDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="addTable">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 编辑桌位对话框 -->
    <el-dialog v-model="editTableDialogVisible" title="编辑桌位" width="400px">
      <el-form :model="editTableForm" :rules="tableRules" ref="editTableFormRef">
        <el-form-item label="桌位名称" prop="name">
          <el-input v-model="editTableForm.name" placeholder="请输入桌位名称" />
        </el-form-item>
        <el-form-item label="桌位状态" prop="status">
          <el-select v-model="editTableForm.status" placeholder="请选择状态">
            <el-option label="未开桌" value="未开桌" />
            <el-option label="使用中" value="使用中" />
            <el-option label="维护中" value="维护中" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editTableDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="updateTable">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import api from "../utils/api";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox, ElLoading } from "element-plus";
import { Edit, Delete, Plus, Check } from "@element-plus/icons-vue";

const router = useRouter();

// 用于存储桌位数据
const tables = ref([]);

// 编辑模式状态
const editMode = ref(false);

// 拖拽相关状态
const draggedTableId = ref(null);
const dragOverTableId = ref(null);

// 对话框状态
const addTableDialogVisible = ref(false);
const editTableDialogVisible = ref(false);

// 表单引用
const addTableFormRef = ref(null);
const editTableFormRef = ref(null);

// 新增桌位表单
const newTableForm = ref({
  name: '',
  status: '未开桌'
});

// 编辑桌位表单
const editTableForm = ref({
  id: null,
  name: '',
  status: ''
});

// 表单验证规则
const tableRules = {
  name: [
    { required: true, message: '请输入桌位名称', trigger: 'blur' },
    { min: 1, max: 50, message: '桌位名称长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择桌位状态', trigger: 'change' }
  ]
};

// 获取桌位数据
// 添加加载状态
const loading = ref(false);

const getTables = async () => {
  loading.value = true;
  try {
    const response = await api.get("/api/tables");
    
    // 确保响应数据是数组
    if (Array.isArray(response.data)) {
      tables.value = response.data;
      
      // 只有当tables.value是数组且不为空时才获取订单
      if (tables.value.length > 0) {
        // 使用 Promise.all 替代 forEach，避免并发问题
        const orderPromises = tables.value.map(async (table) => {
          try {
            const orderResponse = await api.get(`/api/orders/table/${table.id}`);
            table.orders = Array.isArray(orderResponse.data) ? orderResponse.data : [];
            return table;
          } catch (error) {
            console.error(`获取桌位 ${table.id} 订单失败:`, error);
            table.orders = [];
            return table;
          }
        });
        
        await Promise.all(orderPromises);
      }
    } else {
      console.error("获取桌位数据格式错误:", response.data);
      tables.value = [];
      ElMessage.error("获取桌位数据格式错误，请联系管理员");
    }
    
  } catch (error) {
    console.error("获取桌位数据失败:", error);
    
    // 确保tables.value始终是数组
    tables.value = [];
    
    // 根据错误类型显示不同的错误信息
    if (error.response) {
      const status = error.response.status;
      const message = error.response.data?.message || error.response.data || '服务器错误';
      
      if (status === 401) {
        ElMessage.error("登录已过期，请重新登录");
        // 可以在这里跳转到登录页
        // router.push('/login');
      } else if (status === 500) {
        ElMessage.error(`服务器内部错误：${message}`);
      } else {
        ElMessage.error(`获取桌位数据失败：${message}`);
      }
    } else if (error.request) {
      ElMessage.error("网络连接失败，请检查网络连接");
    } else {
      ElMessage.error("获取桌位数据失败，请稍后再试");
    }
  }
};

// 计算桌位的总金额
const getTotalAmount = (orders) => {
  return orders.reduce((sum, item) => sum + (item.price * item.quantity), 0).toFixed(2);
};

// 获取桌位状态
const getTableStatus = (tableId) => {
  const table = tables.value.find(t => t.id === tableId);
  if (table && table.orders) {
    const totalAmount = getTotalAmount(table.orders);
    return {
      color: table.orders.length > 0 ? '#fdf6ec' : '#f0f9eb',
      status: table.orders.length > 0 ? `总金额：¥${totalAmount}` : '未开桌'
    };
  }
  return { color: '#f0f9eb', status: '未开桌' };
};

// 跳转到桌位详情页面
const goToTableDetail = (tableId) => {
  // 使用科技蓝色的圆圈转动加载
  const loading = ElLoading.service({
    lock: true,
    text: '正在加载桌位详情...',
    background: 'rgba(0, 0, 0, 0.7)',
    spinner: 'el-icon-loading',
    svgViewBox: '-10, -10, 50, 50',
    svgStyle: {
      color: '#409EFF',  // 科技蓝
      fontSize: '42px'
    },
    textStyle: {
      color: '#409EFF',  // 科技蓝
      fontSize: '16px',
      fontWeight: '600'
    }
  });

  // 设置一个最短显示时间，避免闪烁
  const minLoadTime = 300; // 最少显示300ms
  const startTime = Date.now();

  router.push(`/table/${tableId}`).then(() => {
    const elapsed = Date.now() - startTime;
    const remainingTime = Math.max(0, minLoadTime - elapsed);

    setTimeout(() => {
      loading.close();
    }, remainingTime);
  }).catch(() => {
    loading.close();
    ElMessage.error('页面跳转失败，请重试');
  });
};

// 组件挂载时调用 getTables 方法
onMounted(() => {
  getTables();
});

// 拖拽开始
const handleDragStart = (event, tableId) => {
  draggedTableId.value = tableId;
  event.dataTransfer.effectAllowed = 'move';
  event.dataTransfer.setData('text/plain', tableId.toString());
  
  // 检查是否有订单可以转移
  const table = tables.value.find(t => t.id === tableId);
  if (!table || !table.orders || table.orders.length === 0) {
    event.preventDefault();
    ElMessage.warning('该桌位没有订单，无法转移');
    return;
  }
};

// 拖拽经过
const handleDragOver = (event) => {
  event.preventDefault();
  event.dataTransfer.dropEffect = 'move';
};

// 拖拽进入
const handleDragEnter = (event, tableId) => {
  event.preventDefault();
  if (draggedTableId.value && draggedTableId.value !== tableId) {
    dragOverTableId.value = tableId;
  }
};

// 拖拽离开
const handleDragLeave = (event) => {
  // 检查是否真的离开了元素
  if (!event.currentTarget.contains(event.relatedTarget)) {
    dragOverTableId.value = null;
  }
};

// 拖拽放下
const handleDrop = async (event, targetTableId) => {
  event.preventDefault();
  dragOverTableId.value = null;
  
  const sourceTableId = draggedTableId.value;
  draggedTableId.value = null;
  
  if (!sourceTableId || sourceTableId === targetTableId) {
    return;
  }
  
  const sourceTable = tables.value.find(t => t.id === sourceTableId);
  const targetTable = tables.value.find(t => t.id === targetTableId);
  
  if (!sourceTable || !sourceTable.orders || sourceTable.orders.length === 0) {
    ElMessage.warning('源桌位没有订单，无法转移');
    return;
  }
  
  try {
    // 确认转移操作
    const action = await ElMessageBox.confirm(
      `确定要将桌位 ${sourceTableId} 的所有订单转移到桌位 ${targetTableId} 吗？`,
      '确认转移',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );
    
    if (action === 'confirm') {
      await transferOrders(sourceTableId, targetTableId);
    }
  } catch (error) {
    // 用户取消操作
    console.log('用户取消了转移操作');
  }
};

// 转移订单
const transferOrders = async (fromTableId, toTableId) => {
  try {
    await api.post('/api/orders/transfer', {
      fromTableId: fromTableId,
      toTableId: toTableId
    });
    
    ElMessage.success(`订单已成功从桌位 ${fromTableId} 转移到桌位 ${toTableId}`);
    
    // 重新获取桌位数据
    await getTables();
  } catch (error) {
    console.error('转移订单失败', error);
    if (error.response && error.response.data) {
      ElMessage.error(`转移失败：${error.response.data}`);
    } else {
      ElMessage.error('转移订单失败，请稍后再试');
    }
  }
};

// 切换编辑模式
const toggleEditMode = () => {
  editMode.value = !editMode.value;
  if (editMode.value) {
    ElMessage.info('已进入编辑模式，可以对桌位进行增删改操作');
  } else {
    ElMessage.success('已退出编辑模式');
  }
};

// 显示新增桌位对话框
const showAddTableDialog = () => {
  newTableForm.value = {
    name: '',
    status: '未开桌'
  };
  addTableDialogVisible.value = true;
};

// 显示编辑桌位对话框
const showEditTableDialog = (table) => {
  editTableForm.value = {
    id: table.id,
    name: table.name,
    status: table.status
  };
  editTableDialogVisible.value = true;
};

// 新增桌位
const addTable = async () => {
  if (!addTableFormRef.value) return;
  
  try {
    await addTableFormRef.value.validate();
    
    const response = await api.post('/api/tables', newTableForm.value);
    
    ElMessage.success('桌位新增成功');
    addTableDialogVisible.value = false;
    await getTables(); // 重新获取桌位列表
  } catch (error) {
    if (error.response) {
      ElMessage.error('新增桌位失败：' + (error.response.data.message || '服务器错误'));
    } else if (error.errors) {
      // 表单验证错误
      return;
    } else {
      ElMessage.error('新增桌位失败，请稍后再试');
    }
  }
};

// 更新桌位
const updateTable = async () => {
  if (!editTableFormRef.value) return;
  
  try {
    await editTableFormRef.value.validate();
    
    const { id, ...updateData } = editTableForm.value;
    await api.put(`/api/tables/${id}`, updateData);
    
    ElMessage.success('桌位更新成功');
    editTableDialogVisible.value = false;
    await getTables(); // 重新获取桌位列表
  } catch (error) {
    if (error.response) {
      ElMessage.error('更新桌位失败：' + (error.response.data.message || '服务器错误'));
    } else if (error.errors) {
      // 表单验证错误
      return;
    } else {
      ElMessage.error('更新桌位失败，请稍后再试');
    }
  }
};

// 删除桌位
const deleteTable = async (tableId) => {
  try {
    // 检查桌位是否有未完成的订单
    const table = tables.value.find(t => t.id === tableId);
    if (table && table.orders && table.orders.length > 0) {
      ElMessage.warning('该桌位还有未完成的订单，无法删除');
      return;
    }
    
    await ElMessageBox.confirm(
      '确定要删除这个桌位吗？删除后无法恢复。',
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );
    
    await api.delete(`/api/tables/${tableId}`);
    
    ElMessage.success('桌位删除成功');
    await getTables(); // 重新获取桌位列表
  } catch (error) {
    if (error === 'cancel') {
      // 用户取消删除
      return;
    }
    
    if (error.response) {
      ElMessage.error('删除桌位失败：' + (error.response.data.message || '服务器错误'));
    } else {
      ElMessage.error('删除桌位失败，请稍后再试');
    }
  }
};
</script>

<style scoped>
.main-card {
  padding: 20px;
  background-color: #ffffff;
  border-radius: 12px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.table-row {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  gap: 20px;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.edit-actions {
  display: flex;
  gap: 10px;
}

.table-card {
  cursor: pointer;
  text-align: center;
  height: 150px;
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 8px;
  transition: transform 0.3s;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  position: relative;
  user-select: none;
}

.table-card:hover {
  transform: scale(1.05);
}

.table-card[draggable="true"] {
  cursor: grab;
}

.table-card[draggable="true"]:active {
  cursor: grabbing;
}

/* 编辑按钮样式 */
.edit-buttons {
  position: absolute;
  top: 10px;
  right: 10px;
  display: flex;
  gap: 5px;
  z-index: 20;
}

/* 编辑模式下禁用点击 */
.table-card.edit-mode {
  cursor: default;
}

/* 拖拽覆盖层样式 */
.drag-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(64, 158, 255, 0.8);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  border-radius: 8px;
  z-index: 10;
}

/* 对话框样式 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.table-card-content h3 {
  font-size: 18px;
  font-weight: bold;
}

.table-card-content p {
  margin-top: 10px;
  color: #666;
}

.table-card + .table-card {
  margin-top: 20px;
}
</style>