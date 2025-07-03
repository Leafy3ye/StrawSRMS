<template>
  <el-card class="table-detail-card">
    <!-- 修改标题和返回按钮的布局 -->
    <div class="header-section">
      <h2>{{ tableInfo?.name || `桌位 ${tableId}` }} 订单</h2>
      <el-button 
        type="primary" 
        size="small"
        @click="goBack" 
        :icon="ArrowLeft"
      >
        返回
      </el-button>
    </div>

    <!-- 菜品列表 - 添加滚动容器 -->
    <div class="orders-container">
      <el-table :data="tableOrders" style="width: 100%" height="400">
        <el-table-column prop="name" label="菜品名称" width="180" />
        <el-table-column prop="price" label="单价" width="120" />
        <el-table-column label="数量" width="150">
          <template #default="scope">
            <el-button size="small" @click="updateQuantity(scope.row.uniqueId, -1)">-</el-button>
            <span style="margin: 0 10px">{{ scope.row.quantity }}</span>
            <el-button size="small" @click="updateQuantity(scope.row.uniqueId, 1)">+</el-button>
          </template>
        </el-table-column>
        <el-table-column label="小计">
          <template #default="scope">
            ¥{{ (scope.row.price * scope.row.quantity).toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column label="备注" width="200">
          <template #default="scope">
            <el-input
              v-model="scope.row.remark"
              placeholder="请输入备注"
              clearable
              @change="updateRemark(scope.row.uniqueId, scope.row.remark)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button
              type="success"
              size="small"
              @click="markAsCompleted(scope.row.uniqueId)"
              v-if="!scope.row.completed"
            >
              完成
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="removeFromCart(scope.row.uniqueId)"
            >
              删除
            </el-button>
            <el-icon v-if="scope.row.completed" style="color: #67c23a; margin-left: 10px;">
              <Check />
            </el-icon>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 总价与结算 -->
    <div class="cart-footer">
      <p>总金额：¥{{ totalAmount }}</p>
      <el-input
        v-model="searchQuery"
        placeholder="搜索菜品（支持中文、拼音、首字母）"
        clearable
        style="width: 250px; margin-right: 10px;"
      />
      <!-- 移除搜索按钮，因为现在是实时搜索 -->
      
      <!-- 结算方式选择 -->
      <el-select v-model="paymentType" placeholder="选择结算方式" style="width: 120px; margin-right: 10px;">
        <el-option label="正常结算" value="cash" />
        <el-option label="会员结算" value="member" />
      </el-select>
      
      <!-- 会员选择（仅在会员结算时显示） -->
      <el-select 
        v-if="paymentType === 'member'" 
        v-model="selectedMemberPhone" 
        placeholder="请输入会员手机号" 
        filterable
        remote
        :remote-method="searchMemberByPhone"
        style="width: 180px; margin-right: 10px;"
        @focus="loadMembers"
        clearable
      >
        <el-option 
          v-for="member in filteredMembers" 
          :key="member.id" 
          :label="`${member.phone} - ${member.name} (余额¥${member.balance.toFixed(2)})`" 
          :value="member.phone"
        />
      </el-select>
      
      <el-button type="success" @click="checkout">结算</el-button>
      <el-button type="danger" @click="clearCart">清空所有菜品</el-button>
    </div>

    <!-- 菜单管理 -->
    <el-divider>菜单</el-divider>
    <el-row :gutter="20">
      <el-col :span="6" v-for="item in menuItems" :key="item.id">
        <el-card class="menu-card" @click="addToCart(item)">
          <div class="menu-card-content">
            <h3>{{ item.name }}</h3>
            <p>¥{{ item.price }}</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </el-card>
</template>

<script setup>
import { ref, computed, onMounted, watch } from "vue";
import { useCartStore } from "../store/cart";
import { ElMessage, ElMessageBox } from 'element-plus';
import { useRoute, useRouter } from "vue-router";
import { Check, ArrowLeft } from '@element-plus/icons-vue';
import api from '../utils/api';
import pinyin from 'pinyin';

// 获取路由中的桌位ID
const route = useRoute();
const router = useRouter();
const tableId = route.params.id;

// 获取购物车 store
const cartStore = useCartStore();

// 菜单数据 (使用 ref 来使其成为响应式)
const menuItems = ref([]);
const allMenuItems = ref([]); // 保存所有菜品数据
const searchQuery = ref('');

// 从后端获取菜单数据
const loadMenu = async () => {
  try {
    const response = await api.get("/api/dishes");  // 调用后端的 /api/dishes 接口
    allMenuItems.value = response.data;  // 保存所有菜品数据
    menuItems.value = response.data;  // 更新菜单数据
  } catch (error) {
    console.error("无法加载菜单数据", error);
    ElMessage.error("无法加载菜单数据");
  }
};

// 实时搜索计算属性
const filteredMenuItems = computed(() => {
  if (!searchQuery.value.trim()) {
    return allMenuItems.value;
  }
  
  const query = searchQuery.value.toLowerCase().trim();
  
  return allMenuItems.value.filter(item => {
    try {
      // 1. 直接匹配菜品名称
      if (item.name.toLowerCase().includes(query)) {
        return true;
      }
      
      // 2. 拼音匹配（全拼）
      const pinyinResult = pinyin(item.name, {
        style: pinyin.STYLE_NORMAL,
        heteronym: false
      });
      
      // 将二维数组转为一维数组，然后连接成字符串
      const fullPinyin = pinyinResult.map(arr => arr[0]).join('').toLowerCase();
      
      if (fullPinyin.includes(query)) {
        return true;
      }
      
      // 3. 拼音首字母匹配
      const firstLetterResult = pinyin(item.name, {
        style: pinyin.STYLE_FIRST_LETTER,
        heteronym: false
      });
      
      const firstLetters = firstLetterResult.map(arr => arr[0]).join('').toLowerCase();
      
      if (firstLetters.includes(query)) {
        return true;
      }
      
      // 4. 支持部分拼音匹配（如"bing"匹配"冰美式"）
      const pinyinArray = pinyinResult.map(arr => arr[0]);
      
      // 检查是否有任何一个拼音以查询字符串开头
      const hasMatchingPinyin = pinyinArray.some(py => 
        py.toLowerCase().startsWith(query)
      );
      
      if (hasMatchingPinyin) {
        return true;
      }
      
      return false;
    } catch (error) {
      console.error('拼音转换错误:', error, '菜品名称:', item.name);
      // 如果拼音转换出错，至少保证中文名称匹配还能工作
      return item.name.toLowerCase().includes(query);
    }
  });
});

// 监听搜索框变化，实现实时搜索
watch(searchQuery, () => {
  menuItems.value = filteredMenuItems.value;
}, { immediate: true });

// 移除原来的 handleSearch 函数，因为现在是实时搜索
// const handleSearch = () => {
//   if (!searchQuery.value) {
//     loadMenu();
//     return;
//   }
//   // 过滤菜单数据
//   menuItems.value = menuItems.value.filter(item =>
//     item.name.toLowerCase().includes(searchQuery.value.toLowerCase())
//   );
// };

// 获取当前桌位的订单数据
const tableOrders = computed(() => cartStore.getCart(tableId));

// 总金额
const totalAmount = computed(() => {
  return tableOrders.value.reduce((sum, item) => sum + (item.price * item.quantity), 0).toFixed(2);
});

// 添加菜品到购物车
const addToCart = async (item) => {
  try {
    // 向后端发送添加菜品请求
    const response = await api.post("/api/orders", {
      tableId: tableId,    // 当前桌位ID
      dishId: item.id,     // 菜品ID
      quantity: 1,         // 默认数量为1
      price: item.price,   // 菜品价格
      completed: false,    // 订单状态，默认未完成
      remark: ''           // 默认没有备注
    });
    // 添加菜品成功后，显示提示
    ElMessage.success(`${item.name} 已添加到桌位 ${tableId} 的订单中`);
    loadTableOrders(); // 重新加载桌位订单，确保前端显示更新
  } catch (error) {
    console.error("添加菜品失败", error);
    ElMessage.error("添加菜品失败，请稍后再试");
  }
};

// 更新桌位订单数据 - 修改为使用详细订单接口
const loadTableOrders = async () => {
  try {
    const response = await api.get(`/api/orders/table/${tableId}/details`);
    
    const ordersWithName = response.data.map(order => ({
      ...order,
      name: order.dishName,
      price: order.dishPrice,
      uniqueId: order.id
    }));
    
    // 恢复完成状态
    const completedKey = `completed_${tableId}`;
    const completed = JSON.parse(localStorage.getItem(completedKey) || '[]');
    ordersWithName.forEach(order => {
      if (completed.includes(order.uniqueId)) {
        order.completed = true;
      }
    });
    
    cartStore.setCart(tableId, ordersWithName);
  } catch (error) {
    console.error("无法加载桌位订单数据", error);
  }
};

// 更新菜品数量
const updateQuantity = async (uniqueId, amount) => {
  const order = tableOrders.value.find(o => o.uniqueId === uniqueId);
  if (!order) return;
  
  const newQuantity = order.quantity + amount;
  if (newQuantity <= 0) {
    // 如果数量为0或负数，删除该订单
    await removeFromCart(uniqueId);
    return;
  }
  
  try {
    // 向后端发送更新请求
    await api.put(`/api/orders/${uniqueId}`, {
      ...order,
      quantity: newQuantity
    });
    cartStore.updateQuantity(tableId, uniqueId, amount);
    loadTableOrders(); // 重新加载订单数据
  } catch (error) {
    console.error("更新数量失败", error);
    ElMessage.error("更新数量失败，请稍后再试");
  }
};

// 删除菜品
const removeFromCart = async (uniqueId) => {
  try {
    // 向后端发送删除请求
    await api.delete(`/api/orders/${uniqueId}`);
    cartStore.removeFromCart(tableId, uniqueId);
    ElMessage.success("菜品已删除！");
    loadTableOrders(); // 重新加载订单数据
  } catch (error) {
    console.error("删除菜品失败", error);
    ElMessage.error("删除菜品失败，请稍后再试");
  }
};

// 标记菜品为已完成
const markAsCompleted = async (uniqueId) => {
  try {
    const order = tableOrders.value.find(o => o.uniqueId === uniqueId);
    if (!order) return;
    
    // 调用后端API更新prepared状态，而不是completed
    await api.put(`/api/orders/${uniqueId}`, {
      ...order,
      prepared: true  // 使用prepared字段而不是completed
    });
    
    // 更新本地状态
    cartStore.markAsCompleted(tableId, uniqueId);
    ElMessage.success("菜品已完成！");
  } catch (error) {
    console.error("标记完成失败", error);
    ElMessage.error("标记完成失败，请稍后再试");
  }
};

// 修改相关的ref定义
const paymentType = ref('cash'); // 默认正常结算
const selectedMemberPhone = ref(''); // 改为手机号
const members = ref([]);
const filteredMembers = ref([]);

// 加载会员列表
const loadMembers = async () => {
  try {
    const response = await api.get('/api/members');
    members.value = response.data;
    filteredMembers.value = response.data;
  } catch (error) {
    console.error('加载会员列表失败:', error);
    ElMessage.error('加载会员列表失败');
  }
};

// 根据手机号搜索会员
const searchMemberByPhone = (query) => {
  if (query) {
    filteredMembers.value = members.value.filter(member => 
      member.phone.includes(query)
    );
  } else {
    filteredMembers.value = members.value;
  }
};

// 修改结算方法中的余额不足处理部分
const checkout = async () => {
  if (tableOrders.value.length === 0) {
    ElMessage.warning("未选择任何菜品，请添加菜品！");
    return;
  }
  
  // 会员结算时的验证
  if (paymentType.value === 'member') {
    if (!selectedMemberPhone.value) {
      ElMessage.warning("请输入会员手机号！");
      return;
    }
    
    // 根据手机号查找会员
    const selectedMember = members.value.find(m => m.phone === selectedMemberPhone.value);
    if (!selectedMember) {
      ElMessage.error("未找到该手机号对应的会员，请检查手机号是否正确！");
      return;
    }
    
    // 检查会员余额是否足够 - 使用弹窗提示
    if (selectedMember.balance < totalAmount.value) {
      await ElMessageBox.alert(
        `会员余额不足，无法完成结算！\n\n会员姓名：${selectedMember.name}\n手机号码：${selectedMember.phone}\n当前余额：¥${selectedMember.balance.toFixed(2)}\n订单金额：¥${totalAmount.value.toFixed(2)}\n还需充值：¥${(totalAmount.value - selectedMember.balance).toFixed(2)}`,
        '余额不足',
        {
          confirmButtonText: '确定',
          type: 'error',
          center: true
        }
      );
      return;
    }
  }
  
  try {
    let response;
    
    if (paymentType.value === 'member') {
      // 根据手机号获取会员ID
      const selectedMember = members.value.find(m => m.phone === selectedMemberPhone.value);
      
      // 会员结算：先正常结算，再扣除会员余额
      response = await api.post(`/api/orders/checkout/${tableId}`);
      
      // 构建消费项目列表
      const consumeItems = tableOrders.value.map(order => 
        `${order.name} x${order.quantity} (¥${(order.price * order.quantity).toFixed(2)})`
      );
      
      // 扣除会员余额
      await api.post('/api/members/deduct', {
        memberId: selectedMember.id,
        amount: response.data.totalAmount,
        tableName: response.data.tableName || `桌位${tableId}`,
        consumeItems: consumeItems,
        totalItems: tableOrders.value.length
      });
      
      // 使用弹窗显示成功信息
      await ElMessageBox.alert(
        `会员结算成功！\n\n会员姓名：${selectedMember.name}\n手机号码：${selectedMember.phone}\n消费金额：¥${response.data.totalAmount}\n剩余余额：¥${(selectedMember.balance - response.data.totalAmount).toFixed(2)}`,
        '结算成功',
        {
          confirmButtonText: '确定',
          type: 'success',
          center: true
        }
      );
    } else {
      // 正常结算
      response = await api.post(`/api/orders/checkout/${tableId}`);
      ElMessage.success(`结算成功！总金额：¥${response.data.totalAmount}`);
    }
    
    // 清空前端购物车
    cartStore.clearCart(tableId);
    
    // 重新加载订单数据
    loadTableOrders();
    
    // 重置结算选项
    paymentType.value = 'cash';
    selectedMemberPhone.value = '';
    
  } catch (error) {
    console.error("结算失败", error);
    if (error.response && error.response.data && error.response.data.message) {
      ElMessage.error(`结算失败：${error.response.data.message}`);
    } else {
      ElMessage.error("结算失败，请稍后再试");
    }
  }
};

// 清空所有菜品
const clearCart = async () => {
  try {
    // 向后端发送请求，删除当前桌位的所有订单
    await api.delete(`/api/orders/table/${tableId}`);
    cartStore.clearCart(tableId);  // 清空前端购物车
    ElMessage.success("所选菜品已清空！");
    loadTableOrders();  // 重新加载桌位订单数据，确保前端显示更新
  } catch (error) {
    console.error("清空菜品失败", error);
    ElMessage.error("清空菜品失败，请稍后再试");
  }
};

// 更新备注
const updateRemark = (uniqueId, remark) => {
  const item = tableOrders.value.find(order => order.uniqueId === uniqueId);
  if (item) {
    item.remark = remark;
    ElMessage.success("备注已更新！");
  }
};

// 添加桌位信息
const tableInfo = ref(null);

// 获取桌位信息
const loadTableInfo = async () => {
  try {
    const response = await api.get(`/api/tables/${tableId}`);
    tableInfo.value = response.data;
  } catch (error) {
    console.error("无法获取桌位信息", error);
  }
};

// 在组件挂载时调用
onMounted(() => {
  loadMenu();
  loadTableOrders();
  loadTableInfo(); // 添加这行
});
// 添加返回方法
const goBack = () => {
  router.push('/main');
};
</script>

<style scoped>
.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-section h2 {
  margin: 0;
  font-size: 24px;
}

.table-detail-card {
  max-width: 1200px;
  margin: 20px auto;
  padding: 20px;
  background-color: #ffffff;
  border-radius: 12px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

/* 新增：订单容器样式 */
.orders-container {
  margin: 20px 0;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
}

.cart-footer {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e4e7ed;
}

.cart-footer p {
  font-size: 18px;
  font-weight: bold;
}

.menu-card {
  cursor: pointer;
  text-align: center;
  height: 150px;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f5f5;
  border-radius: 8px;
  transition: transform 0.3s;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.menu-card:hover {
  transform: scale(1.05);
}

.menu-card-content h3 {
  font-size: 18px;
  font-weight: bold;
}
</style>