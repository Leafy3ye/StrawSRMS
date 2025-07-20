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
              @input="updateRemark(scope.row.uniqueId, scope.row.remark)"
              @blur="updateRemark(scope.row.uniqueId, scope.row.remark)"
              @keyup.enter="updateRemark(scope.row.uniqueId, scope.row.remark)"
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

  <!-- 会员结算成功弹窗 -->
  <el-dialog
    v-model="showMemberSettlementSuccess"
    title=""
    width="500px"
    :show-close="false"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    center
  >
    <div class="settlement-success-content">
      <!-- 成功图标 -->
      <div class="success-icon">
        <el-icon size="60" color="#67C23A">
          <Check />
        </el-icon>
      </div>

      <!-- 标题 -->
      <h2 class="success-title">结算成功</h2>

      <!-- 会员信息 -->
      <div class="member-info">
        <p><strong>会员姓名：</strong>{{ settlementResult.memberName }}</p>
        <p><strong>手机号码：</strong>{{ settlementResult.memberPhone }}</p>
        <p><strong>会员等级：</strong>{{ settlementResult.memberLevel }}</p>
      </div>

      <!-- 金额信息 -->
      <div class="amount-info">
        <div class="amount-row">
          <span class="label">消费总额：</span>
          <span class="amount original">¥{{ settlementResult.originalAmount?.toFixed(2) }}</span>
        </div>
        <div class="amount-row" v-if="settlementResult.discountRate < 1">
          <span class="label">会员折扣：</span>
          <span class="discount">{{ (settlementResult.discountRate * 10).toFixed(1) }}折 (-¥{{ settlementResult.discountAmount?.toFixed(2) }})</span>
        </div>
        <div class="amount-row final">
          <span class="label">实际付款：</span>
          <span class="amount final">¥{{ settlementResult.actualAmount?.toFixed(2) }}</span>
        </div>
        <div class="amount-row">
          <span class="label">剩余余额：</span>
          <span class="amount">¥{{ settlementResult.remainingBalance }}</span>
        </div>
      </div>
    </div>

    <template #footer>
      <el-button type="primary" size="large" @click="closeMemberSettlementDialog">
        确定
      </el-button>
    </template>
  </el-dialog>

  <!-- 余额不足弹窗 -->
  <el-dialog
    v-model="showInsufficientBalanceDialog"
    title=""
    width="500px"
    :show-close="false"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    center
  >
    <div class="insufficient-balance-content">
      <!-- 错误图标 -->
      <div class="error-icon">
        <el-icon size="60" color="#F56C6C">
          <CircleClose />
        </el-icon>
      </div>

      <!-- 标题 -->
      <h2 class="error-title">余额不足</h2>

      <!-- 会员信息 -->
      <div class="member-info">
        <p><strong>会员姓名：</strong>{{ insufficientBalanceInfo.memberName }}</p>
        <p><strong>手机号码：</strong>{{ insufficientBalanceInfo.memberPhone }}</p>
        <p><strong>会员等级：</strong>{{ insufficientBalanceInfo.memberLevel }}</p>
      </div>

      <!-- 金额信息 -->
      <div class="amount-info">
        <div class="amount-row">
          <span class="label">当前余额：</span>
          <span class="amount">¥{{ insufficientBalanceInfo.currentBalance?.toFixed(2) }}</span>
        </div>
        <div class="amount-row">
          <span class="label">订单金额：</span>
          <span class="amount">¥{{ insufficientBalanceInfo.originalAmount?.toFixed(2) }}</span>
        </div>
        <div class="amount-row">
          <span class="label">折扣后金额：</span>
          <span class="amount">¥{{ insufficientBalanceInfo.discountedAmount?.toFixed(2) }}</span>
        </div>
        <div class="amount-row shortage">
          <span class="label">还需充值：</span>
          <span class="amount shortage">¥{{ insufficientBalanceInfo.shortfall?.toFixed(2) }}</span>
        </div>
      </div>
    </div>

    <template #footer>
      <el-button type="primary" size="large" @click="closeInsufficientBalanceDialog">
        确定
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, onMounted, watch } from "vue";
import { useCartStore } from "../store/cart";
import { ElMessage, ElMessageBox } from 'element-plus';
import { useRoute, useRouter } from "vue-router";
import { Check, ArrowLeft, CircleClose } from '@element-plus/icons-vue';
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
    console.log('开始加载菜单数据...');
    const startTime = Date.now();

    // 尝试从缓存中获取菜单数据
    const cacheKey = 'menu_cache';
    const cacheTimeKey = 'menu_cache_time';
    const cacheExpiry = 5 * 60 * 1000; // 5分钟缓存

    const cachedData = localStorage.getItem(cacheKey);
    const cacheTime = localStorage.getItem(cacheTimeKey);

    if (cachedData && cacheTime && (Date.now() - parseInt(cacheTime)) < cacheExpiry) {
      console.log('使用缓存的菜单数据');
      const parsedData = JSON.parse(cachedData);
      allMenuItems.value = parsedData;
      menuItems.value = parsedData;

      const loadTime = Date.now() - startTime;
      console.log(`菜单数据从缓存加载完成，耗时: ${loadTime}ms，菜品数量: ${parsedData.length}`);
      return;
    }

    // 缓存过期或不存在，从服务器获取
    const response = await api.get("/api/dishes");
    allMenuItems.value = response.data;
    menuItems.value = response.data;

    // 缓存数据
    localStorage.setItem(cacheKey, JSON.stringify(response.data));
    localStorage.setItem(cacheTimeKey, Date.now().toString());

    const loadTime = Date.now() - startTime;
    console.log(`菜单数据从服务器加载完成，耗时: ${loadTime}ms，菜品数量: ${response.data.length}`);
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

// 总金额（数字类型）
const totalAmountNumber = computed(() => {
  return tableOrders.value.reduce((sum, item) => sum + (item.price * item.quantity), 0);
});

// 总金额（字符串类型，用于显示）
const totalAmount = computed(() => {
  return totalAmountNumber.value.toFixed(2);
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
    console.log('开始加载桌位订单数据...');
    const startTime = Date.now();

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
      // 初始化备注缓存
      lastRemarkValues.set(order.uniqueId, order.remark || '');
    });

    cartStore.setCart(tableId, ordersWithName);

    const loadTime = Date.now() - startTime;
    console.log(`桌位订单数据加载完成，耗时: ${loadTime}ms，订单数量: ${ordersWithName.length}`);
  } catch (error) {
    console.error("无法加载桌位订单数据", error);
    ElMessage.error("无法获取桌位订单，请稍后再试");
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

// 会员结算成功弹窗相关
const showMemberSettlementSuccess = ref(false);
const settlementResult = ref({});

// 余额不足弹窗相关
const showInsufficientBalanceDialog = ref(false);
const insufficientBalanceInfo = ref({});

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
  
  // 会员结算时的基本验证
  if (paymentType.value === 'member') {
    if (!selectedMemberPhone.value) {
      ElMessage.warning("请输入会员手机号！");
      return;
    }
  }
  
  try {
    let response;
    
    if (paymentType.value === 'member') {
      // 根据手机号获取会员ID
      const selectedMember = members.value.find(m => m.phone === selectedMemberPhone.value);
      if (!selectedMember) {
        ElMessage.error('请选择有效的会员');
        return;
      }

      console.log('选中的会员:', selectedMember);

      // 获取会员等级信息和折扣率
      let discountRate = 1; // 默认无折扣
      let memberLevel = null;

      try {
        const levelResponse = await api.get('/api/member-levels');
        console.log('会员等级列表:', levelResponse.data);
        console.log('当前会员等级:', selectedMember.level); // 修正：使用 level 而不是 memberLevel

        // 查找匹配的会员等级（忽略大小写和空格）
        memberLevel = levelResponse.data.find(level => {
          const levelName = level.levelName?.trim();
          const memberLevelName = selectedMember.level?.trim(); // 修正：使用 level
          console.log(`比较等级: "${levelName}" === "${memberLevelName}", isEnabled: ${level.isEnabled}`);
          return levelName === memberLevelName && level.isEnabled;
        });

        console.log('匹配的会员等级:', memberLevel);

        if (memberLevel && memberLevel.discountRate) {
          discountRate = parseFloat(memberLevel.discountRate);
          console.log('应用折扣率:', discountRate);
        } else {
          console.log('未找到匹配的会员等级或折扣率，使用原价');
        }
      } catch (error) {
        console.warn('获取会员等级信息失败，使用原价结算:', error);
      }

      // 计算折扣后的金额
      const originalAmount = totalAmountNumber.value;
      const discountedAmount = originalAmount * discountRate;
      const discountAmount = originalAmount - discountedAmount;

      console.log('金额计算:', {
        originalAmount,
        discountRate,
        discountedAmount,
        discountAmount,
        memberBalance: selectedMember.balance
      });

      // 检查会员余额是否足够
      if (selectedMember.balance < discountedAmount) {
        // 显示余额不足弹窗
        showInsufficientBalanceDialog.value = true;
        insufficientBalanceInfo.value = {
          memberName: selectedMember.name,
          memberPhone: selectedMember.phone,
          memberLevel: selectedMember.level,
          currentBalance: selectedMember.balance,
          originalAmount: originalAmount,
          discountedAmount: discountedAmount,
          shortfall: discountedAmount - selectedMember.balance
        };
        return;
      }

      // 会员结算：使用专门的会员结算接口
      const memberSettlementInfo = {
        memberId: selectedMember.id,
        memberName: selectedMember.name,
        memberPhone: selectedMember.phone,
        memberLevel: selectedMember.level,
        originalAmount: originalAmount,
        discountRate: discountRate,
        discountAmount: discountAmount,
        actualAmount: discountedAmount
      };

      response = await api.post(`/api/orders/checkout/${tableId}/member`, memberSettlementInfo);

      // 构建消费项目列表
      const consumeItems = tableOrders.value.map(order =>
        `${order.name} x${order.quantity} (¥${(order.price * order.quantity).toFixed(2)})`
      );

      // 扣除会员余额（使用折扣后的金额）
      try {
        await api.post('/api/members/deduct', {
          memberId: selectedMember.id,
          amount: discountedAmount,
          tableName: response.data.tableName || `桌位${tableId}`,
          consumeItems: consumeItems,
          totalItems: tableOrders.value.length
        });
      } catch (deductError) {
        // 如果扣除余额失败，显示具体错误信息
        const errorMessage = deductError.response?.data || deductError.message || '扣除余额失败';
        ElMessage.error(errorMessage);
        return;
      }
      
      // 显示结算成功弹窗
      showMemberSettlementSuccess.value = true;
      settlementResult.value = {
        memberName: selectedMember.name,
        memberPhone: selectedMember.phone,
        memberLevel: selectedMember.level || '普通会员', // 修正：使用 level
        originalAmount: originalAmount,
        discountRate: discountRate,
        discountAmount: discountAmount,
        actualAmount: discountedAmount,
        remainingBalance: (selectedMember.balance - discountedAmount).toFixed(2)
      };

      console.log('结算成功，显示结果:', settlementResult.value);
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

// 防抖定时器
const remarkTimers = new Map();
const lastRemarkValues = new Map();

// 更新备注
const updateRemark = async (uniqueId, remark) => {
  // 如果备注值没有变化，不执行更新
  if (lastRemarkValues.get(uniqueId) === remark) {
    return;
  }

  // 清除之前的定时器
  if (remarkTimers.has(uniqueId)) {
    clearTimeout(remarkTimers.get(uniqueId));
  }

  // 设置新的定时器，1000ms后执行
  const timer = setTimeout(async () => {
    try {
      const item = tableOrders.value.find(order => order.uniqueId === uniqueId);
      if (!item) {
        ElMessage.error("找不到对应的订单");
        return;
      }

      // 调用后端API更新备注
      await api.put(`/api/orders/${uniqueId}`, {
        ...item,
        remark: remark
      });

      // 更新本地数据和缓存
      item.remark = remark;
      lastRemarkValues.set(uniqueId, remark);

      // 只在备注不为空时显示成功消息
      if (remark && remark.trim()) {
        ElMessage.success("备注已保存！");
      }
    } catch (error) {
      console.error("更新备注失败", error);
      ElMessage.error("备注保存失败，请稍后再试");
    } finally {
      // 清除定时器
      remarkTimers.delete(uniqueId);
    }
  }, 1000);

  remarkTimers.set(uniqueId, timer);
};

// 添加桌位信息
const tableInfo = ref(null);

// 获取桌位信息
const loadTableInfo = async () => {
  try {
    console.log('开始加载桌位信息...');
    const startTime = Date.now();

    const response = await api.get(`/api/tables/${tableId}`);
    tableInfo.value = response.data;

    const loadTime = Date.now() - startTime;
    console.log(`桌位信息加载完成，耗时: ${loadTime}ms`);
  } catch (error) {
    console.error("无法获取桌位信息", error);
  }
};

// 在组件挂载时调用
// 关闭会员结算成功弹窗
const closeMemberSettlementDialog = () => {
  showMemberSettlementSuccess.value = false;
  settlementResult.value = {};
};

// 关闭余额不足弹窗
const closeInsufficientBalanceDialog = () => {
  showInsufficientBalanceDialog.value = false;
  insufficientBalanceInfo.value = {};
};

onMounted(async () => {
  // 并行加载所有数据，提高页面加载速度
  console.log('桌位详情页面开始加载...');
  const pageStartTime = Date.now();

  try {
    await Promise.all([
      loadMenu(),
      loadTableOrders(),
      loadTableInfo()
    ]);

    const totalLoadTime = Date.now() - pageStartTime;
    console.log(`桌位详情页面加载完成，总耗时: ${totalLoadTime}ms`);
  } catch (error) {
    console.error('页面数据加载失败:', error);
    ElMessage.error('页面数据加载失败，请刷新重试');
  }
});

// 清除菜单缓存的函数（可以在需要时调用）
const clearMenuCache = () => {
  localStorage.removeItem('menu_cache');
  localStorage.removeItem('menu_cache_time');
  console.log('菜单缓存已清除');
};

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

/* 会员结算成功弹窗样式 */
.settlement-success-content {
  text-align: center;
  padding: 20px;
}

.success-icon {
  margin-bottom: 20px;
}

.success-title {
  font-size: 24px;
  font-weight: bold;
  color: #67C23A;
  margin-bottom: 30px;
}

.member-info {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 20px;
  text-align: left;
}

.member-info p {
  margin: 8px 0;
  font-size: 14px;
  color: #606266;
}

.amount-info {
  text-align: left;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 20px;
}

.amount-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  font-size: 16px;
}

.amount-row:last-child {
  margin-bottom: 0;
}

.amount-row.final {
  border-top: 1px solid #e4e7ed;
  padding-top: 12px;
  margin-top: 12px;
  font-weight: bold;
  font-size: 18px;
}

.label {
  color: #606266;
}

.amount {
  font-weight: bold;
  color: #303133;
}

.amount.original {
  color: #909399;
  text-decoration: line-through;
}

.amount.final {
  color: #67C23A;
  font-size: 20px;
}

.discount {
  color: #E6A23C;
  font-weight: bold;
}

/* 余额不足弹窗样式 */
.insufficient-balance-content {
  text-align: center;
  padding: 20px;
}

.error-icon {
  margin-bottom: 20px;
}

.error-title {
  font-size: 24px;
  font-weight: bold;
  color: #F56C6C;
  margin-bottom: 30px;
}

.insufficient-balance-content .member-info {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 20px;
  text-align: left;
}

.insufficient-balance-content .member-info p {
  margin: 8px 0;
  font-size: 14px;
  color: #606266;
}

.insufficient-balance-content .amount-info {
  text-align: left;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 20px;
}

.insufficient-balance-content .amount-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  font-size: 16px;
}

.insufficient-balance-content .amount-row:last-child {
  margin-bottom: 0;
}

.insufficient-balance-content .amount-row.shortage {
  border-top: 1px solid #e4e7ed;
  padding-top: 12px;
  margin-top: 12px;
  font-weight: bold;
  font-size: 18px;
}

.insufficient-balance-content .amount.shortage {
  color: #F56C6C;
  font-size: 20px;
}
</style>