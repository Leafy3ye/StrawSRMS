<template>
  <div class="customer-order-container">
    <!-- 桌位不存在的错误页面 -->
    <div v-if="tableNotFound" class="error-page">
      <el-result
        icon="error"
        title="桌位不存在"
        :sub-title="`抱歉，找不到${tableId}号桌位，请检查桌位号码是否正确。`"
      >
        <template #extra>
          <el-button type="primary" @click="goBack">返回</el-button>
        </template>
      </el-result>
    </div>

    <!-- 正常的点餐页面 -->
    <div v-else-if="!loading">
      <!-- 头部信息 -->
      <div class="header">
        <h1>小杜的咖啡馆</h1>
        <h2>{{ tableInfo?.name || `${tableId}号桌` }} - 点餐</h2>
      </div>

      <!-- 订单状态切换 -->
      <div class="status-tabs" v-if="hasSubmittedOrders">
        <el-tabs v-model="activeTab" @tab-change="handleTabChange">
          <el-tab-pane label="继续点餐" name="order"></el-tab-pane>
          <el-tab-pane label="已点菜品" name="submitted"></el-tab-pane>
        </el-tabs>
      </div>

      <!-- 已提交订单列表 -->
      <div v-if="activeTab === 'submitted'" class="submitted-orders">
        <el-card class="submitted-card">
          <div class="submitted-header">
            <h3>已提交订单</h3>
            <span class="submitted-total">总计：¥{{ submittedTotal.toFixed(2) }}</span>
          </div>
          <div class="submitted-items">
            <div v-for="order in submittedOrders" :key="order.id" class="submitted-item">
              <span class="item-name">{{ order.dishName }}</span>
              <span class="item-quantity">x{{ order.quantity }}</span>
              <span class="item-price">¥{{ (order.price * order.quantity).toFixed(2) }}</span>
              <span class="item-status" :class="order.prepared ? 'prepared' : 'preparing'">
                {{ order.prepared ? '已完成' : '制作中' }}
              </span>
            </div>
          </div>
          <div class="submitted-actions">
            <el-button type="primary" @click="activeTab = 'order'">继续点餐</el-button>
            <el-button @click="refreshSubmittedOrders">刷新状态</el-button>
          </div>
        </el-card>
      </div>

      <!-- 点餐界面 -->
      <div v-if="activeTab === 'order'">
        <!-- 购物车摘要 - 桌面版 -->
        <div class="cart-summary" v-if="cart.length > 0">
          <el-card class="cart-card">
            <div class="cart-header">
              <h3>待提交菜品 ({{ totalItems }}件)</h3>
              <span class="total-price">总计：¥{{ totalPrice.toFixed(2) }}</span>
            </div>
            <div class="cart-items-container">
              <div class="cart-items">
                <div v-for="item in cart" :key="item.uniqueId" class="cart-item">
                  <span class="item-name">{{ item.name }}</span>
                  <div class="quantity-controls">
                    <el-button size="small" @click="updateQuantity(item.uniqueId, -1)">-</el-button>
                    <span class="quantity">{{ item.quantity }}</span>
                    <el-button size="small" @click="updateQuantity(item.uniqueId, 1)">+</el-button>
                  </div>
                  <span class="item-price">¥{{ (item.price * item.quantity).toFixed(2) }}</span>
                  <el-button size="small" type="danger" @click="removeFromCart(item.uniqueId)">删除</el-button>
                </div>
              </div>
            </div>
            <div class="cart-actions">
              <el-button type="primary" @click="submitOrder" :disabled="cart.length === 0" :loading="submitting">提交订单</el-button>
              <el-button @click="clearCart">清空</el-button>
            </div>
          </el-card>
        </div>

        <!-- 移动端购物车 - 底部固定栏 -->
        <div class="mobile-cart-summary" v-if="cart.length > 0">
          <!-- 底部固定的购物车栏 -->
          <div class="mobile-cart-bar" @click="toggleCartExpanded">
            <div class="cart-bar-content">
              <div class="cart-info">
                <span class="cart-count">{{ totalItems }}件</span>
                <span class="cart-total">¥{{ totalPrice.toFixed(2) }}</span>
              </div>
              <el-button type="primary" size="small" @click.stop="submitOrder" :disabled="cart.length === 0" :loading="submitting">
                提交订单
              </el-button>
            </div>
          </div>
          
          <!-- 购物车详情弹出层 -->
          <div class="cart-overlay" v-show="cartExpanded" @click="cartExpanded = false"></div>
          <div class="cart-details" :class="{ 'expanded': cartExpanded }">
            <div class="cart-details-header">
              <h3>待提交菜品 ({{ totalItems }}件)</h3>
              <el-button text @click="cartExpanded = false">
                <el-icon><Close /></el-icon>
              </el-button>
            </div>
            <div class="cart-items-container">
              <div class="cart-items">
                <div v-for="item in cart" :key="item.uniqueId" class="cart-item">
                  <span class="item-name">{{ item.name }}</span>
                  <div class="quantity-controls">
                    <el-button size="small" @click="updateQuantity(item.uniqueId, -1)">-</el-button>
                    <span class="quantity">{{ item.quantity }}</span>
                    <el-button size="small" @click="updateQuantity(item.uniqueId, 1)">+</el-button>
                  </div>
                  <span class="item-price">¥{{ (item.price * item.quantity).toFixed(2) }}</span>
                  <el-button size="small" type="danger" @click="removeFromCart(item.uniqueId)">删除</el-button>
                </div>
              </div>
            </div>
            <div class="cart-actions">
              <el-button type="primary" @click="submitOrder" :disabled="cart.length === 0" :loading="submitting">提交订单</el-button>
              <el-button @click="clearCart">清空</el-button>
            </div>
          </div>
        </div>

        <!-- 菜品搜索 -->
        <div class="search-section">
          <el-input
            v-model="searchQuery"
            placeholder="搜索菜品（支持中文、拼音、首字母）"
            clearable
            size="large"
            class="search-input"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>

        <!-- 菜品列表 -->
        <div class="menu-section">
          <el-row :gutter="16">
            <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="item in filteredMenu" :key="item.id">
              <el-card class="menu-item-card" @click="addToCart(item)" shadow="hover">
                <div class="menu-item-content">
                  <h3 class="menu-item-name">{{ item.name }}</h3>
                  <div class="menu-item-price">¥{{ item.price.toFixed(2) }}</div>
                  <el-button type="primary" size="small" class="add-btn">
                    <el-icon><Plus /></el-icon>
                    添加
                  </el-button>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>

        <!-- 空状态 -->
        <div v-if="filteredMenu.length === 0" class="empty-state">
          <el-empty description="暂无菜品" />
        </div>
      </div>

      <!-- 订单提交成功对话框 -->
      <el-dialog v-model="orderSubmitted" title="订单提交成功" width="400px" center>
        <div class="success-content">
          <el-icon class="success-icon"><SuccessFilled /></el-icon>
          <p>您的订单已提交成功！</p>
          <p>请耐心等待，我们会尽快为您准备。</p>
        </div>
        <template #footer>
          <div class="dialog-footer">
            <el-button type="primary" @click="continueOrdering">继续点餐</el-button>
            <el-button @click="viewSubmittedOrders">查看已点菜品</el-button>
          </div>
        </template>
      </el-dialog>
    </div>

    <!-- 加载状态 -->
    <div v-else class="loading-container">
      <el-loading-directive v-loading="loading" text="正在加载..." />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, SuccessFilled, Close } from '@element-plus/icons-vue'
import api from '../utils/api'
import { pinyin } from 'pinyin-pro'

// 获取路由参数
const route = useRoute()
const router = useRouter()
const tableId = route.params.tableId

// 响应式数据
const menuItems = ref([])
const cart = ref([])
const submittedOrders = ref([])
const searchQuery = ref('')
const orderSubmitted = ref(false)
const loading = ref(true)
const submitting = ref(false)
const activeTab = ref('order')
const hasSubmittedOrders = ref(false)
const tableInfo = ref(null)
const tableNotFound = ref(false)
const cartExpanded = ref(false) // 购物车展开状态

// 计算属性
const filteredMenu = computed(() => {
  if (!searchQuery.value.trim()) return menuItems.value
  const query = searchQuery.value.toLowerCase().trim()
  return menuItems.value.filter(item => {
    // 1. 直接匹配菜品名称
    if (item.name.toLowerCase().includes(query)) return true
    // 2. 全拼匹配
    const fullPinyin = pinyin(item.name, { toneType: 'none', type: 'array' }).join('').toLowerCase()
    if (fullPinyin.includes(query)) return true
    // 3. 首字母匹配
    const firstLetters = firstLetter(item.name).replace(/\s+/g, '').toLowerCase()
    if (firstLetters.includes(query)) return true
    // 4. 部分拼音匹配
    const pinyinArray = pinyin(item.name, { toneType: 'none', type: 'array' })
    if (pinyinArray.some(py => py.toLowerCase().startsWith(query))) return true
    // 5. 连续拼音匹配
    if (pinyinArray.join('').toLowerCase().includes(query)) return true
    return false
  })
})

const totalItems = computed(() => {
  return cart.value.reduce((total, item) => total + item.quantity, 0)
})

const totalPrice = computed(() => {
  return cart.value.reduce((total, item) => total + (item.price * item.quantity), 0)
})

const submittedTotal = computed(() => {
  return submittedOrders.value.reduce((total, order) => total + (order.price * order.quantity), 0)
})

// 切换购物车展开状态
const toggleCartExpanded = () => {
  cartExpanded.value = !cartExpanded.value
}

// 验证桌位是否存在
const validateTable = async () => {
  try {
    const response = await api.get(`/api/tables/${tableId}`)
    tableInfo.value = response.data
    return true
  } catch (error) {
    if (error.response && error.response.status === 404) {
      tableNotFound.value = true
      return false
    }
    console.error('验证桌位失败:', error)
    ElMessage.error('验证桌位信息失败，请稍后重试')
    return false
  }
}

// 返回上一页
const goBack = () => {
  router.go(-1)
}

// 加载菜单数据
const loadMenu = async () => {
  try {
    const response = await api.get('/api/dishes')
    menuItems.value = response.data
  } catch (error) {
    console.error('加载菜单失败:', error)
    ElMessage.error('加载菜单失败，请刷新页面重试')
  }
}

// 加载已提交订单
const loadSubmittedOrders = async () => {
  try {
    const response = await api.get(`/api/orders/table/${tableId}/details`)
    submittedOrders.value = response.data
    hasSubmittedOrders.value = response.data.length > 0
  } catch (error) {
    console.error('加载已提交订单失败:', error)
  }
}

// 刷新已提交订单状态
const refreshSubmittedOrders = async () => {
  await loadSubmittedOrders()
  ElMessage.success('订单状态已刷新')
}

// 添加到购物车
const addToCart = (item) => {
  cart.value.push({
    ...item,
    quantity: 1,
    uniqueId: `${item.id}_${Date.now()}_${Math.random()}`,
    remark: ''
  })
  
  ElMessage.success(`已添加 ${item.name} 到购物车`)
}

// 更新数量
const updateQuantity = (uniqueId, change) => {
  const item = cart.value.find(cartItem => cartItem.uniqueId === uniqueId)
  if (item) {
    item.quantity += change
    if (item.quantity <= 0) {
      removeFromCart(uniqueId)
    }
  }
}

// 从购物车删除单个条目
const removeFromCart = (uniqueId) => {
  cart.value = cart.value.filter(cartItem => cartItem.uniqueId !== uniqueId)
  ElMessage.success('已删除该菜品')
}

// 清空购物车
const clearCart = async () => {
  try {
    await ElMessageBox.confirm('确定要清空购物车吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    cart.value = []
    cartExpanded.value = false // 清空后关闭购物车详情
    ElMessage.success('购物车已清空')
  } catch {
    // 用户取消
  }
}

// 提交订单
const submitOrder = async () => {
  if (cart.value.length === 0) {
    ElMessage.warning('请先选择菜品')
    return
  }

  try {
    submitting.value = true
    
    const orderPromises = cart.value.map(async (item) => {
      const orderData = {
        tableId: parseInt(tableId),
        dishId: item.id,
        quantity: item.quantity,
        remark: item.remark || '',
        price: item.price,
        completed: false
      }
      
      return api.post('/api/orders', orderData)
    })
    
    await Promise.all(orderPromises)
    
    orderSubmitted.value = true
    cart.value = []
    cartExpanded.value = false // 提交后关闭购物车详情
    
    await loadSubmittedOrders()
    
  } catch (error) {
    console.error('提交订单失败:', error)
    ElMessage.error('提交订单失败，请重试')
  } finally {
    submitting.value = false
  }
}

// 继续点餐
const continueOrdering = () => {
  orderSubmitted.value = false
  activeTab.value = 'order'
}

// 查看已点菜品
const viewSubmittedOrders = () => {
  orderSubmitted.value = false
  activeTab.value = 'submitted'
}

// 标签页切换
const handleTabChange = (tabName) => {
  if (tabName === 'submitted') {
    loadSubmittedOrders()
  }
}

// 初始化数据加载
const initializeData = async () => {
  loading.value = true
  
  const tableExists = await validateTable()
  
  if (tableExists) {
    await Promise.all([
      loadMenu(),
      loadSubmittedOrders()
    ])
  }
  
  loading.value = false
}

// 组件挂载时加载数据
onMounted(() => {
  initializeData()
})

// 监听搜索框变化，实现实时搜索
watch(searchQuery, (newValue) => {
  // filteredMenu 计算属性会自动响应 searchQuery 的变化
}, { immediate: true })
</script>

<style scoped>
.customer-order-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.error-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 60vh;
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 60vh;
}

.header {
  text-align: center;
  margin-bottom: 30px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 30px;
  border-radius: 15px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.header h1 {
  margin: 0 0 10px 0;
  font-size: 2.5em;
  font-weight: bold;
}

.header h2 {
  margin: 0;
  font-size: 1.5em;
  opacity: 0.9;
}

.status-tabs {
  margin-bottom: 20px;
  background: white;
  border-radius: 10px;
  padding: 10px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.submitted-orders {
  margin-bottom: 30px;
}

.submitted-card {
  border-radius: 15px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.submitted-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.submitted-header h3 {
  margin: 0;
  color: #333;
}

.submitted-total {
  font-size: 1.2em;
  font-weight: bold;
  color: #e74c3c;
}

.submitted-items {
  margin-bottom: 20px;
}

.submitted-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.submitted-item:last-child {
  border-bottom: none;
}

.item-status {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 0.9em;
  font-weight: bold;
}

.item-status.preparing {
  background-color: #fff3cd;
  color: #856404;
}

.item-status.prepared {
  background-color: #d4edda;
  color: #155724;
}

.submitted-actions {
  display: flex;
  gap: 10px;
  justify-content: center;
}

.cart-summary {
  margin-bottom: 30px;
}

.cart-card {
  border-radius: 15px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.cart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.cart-header h3 {
  margin: 0;
  color: #333;
}

.total-price {
  font-size: 1.2em;
  font-weight: bold;
  color: #e74c3c;
}

.cart-items-container {
  max-height: 300px;
  overflow-y: auto;
  margin: 15px 0;
}

.cart-items-container::-webkit-scrollbar {
  width: 6px;
}

.cart-items-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.cart-items-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.cart-items-container::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

.cart-items {
  margin-bottom: 20px;
}

.cart-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
  gap: 10px;
}

.cart-item:last-child {
  border-bottom: none;
}

.item-name {
  flex: 1;
  font-weight: 500;
}

.quantity-controls {
  display: flex;
  align-items: center;
  gap: 10px;
}

.quantity {
  min-width: 30px;
  text-align: center;
  font-weight: bold;
}

.item-quantity {
  font-weight: bold;
  color: #666;
  min-width: 50px;
  text-align: center;
}

.item-price {
  font-weight: bold;
  color: #e74c3c;
  min-width: 80px;
  text-align: right;
}

.cart-actions {
  display: flex;
  gap: 10px;
  justify-content: center;
}

.search-section {
  margin-bottom: 30px;
}

.search-input {
  border-radius: 25px;
}

.menu-section {
  margin-bottom: 30px;
}

.menu-item-card {
  margin-bottom: 20px;
  border-radius: 15px;
  transition: all 0.3s ease;
  cursor: pointer;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  height: 180px;
}

.menu-item-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.menu-item-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 20px;
  height: 100%;
}

.menu-item-name {
  margin: 0 0 10px 0;
  font-size: 1.2em;
  font-weight: bold;
  color: #333;
  line-height: 1.4;
}

.menu-item-price {
  margin: 0 0 15px 0;
  font-size: 1.3em;
  color: #e74c3c;
  font-weight: bold;
  line-height: 1;
}

.add-btn {
  border-radius: 20px;
  padding: 8px 20px;
  margin-top: auto;
}

.empty-state {
  text-align: center;
  padding: 50px;
}

.success-content {
  text-align: center;
  padding: 20px;
}

.success-icon {
  font-size: 3em;
  color: #67c23a;
  margin-bottom: 15px;
}

.success-content p {
  margin: 10px 0;
  font-size: 1.1em;
}

.dialog-footer {
  display: flex;
  gap: 10px;
  justify-content: center;
}

/* 移动端购物车样式 */
.mobile-cart-summary {
  display: none;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .customer-order-container {
    padding: 10px;
    padding-bottom: 80px; /* 为底部购物车栏留出空间 */
  }
  
  .header {
    padding: 20px;
  }
  
  .header h1 {
    font-size: 2em;
  }
  
  .header h2 {
    font-size: 1.2em;
  }
  
  /* 隐藏桌面版购物车 */
  .cart-summary {
    display: none;
  }
  
  /* 显示移动端购物车 */
  .mobile-cart-summary {
    display: block;
  }
  
  /* 移动端购物车栏 */
  .mobile-cart-bar {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    background: #fff;
    border-top: 1px solid #e4e7ed;
    padding: 15px 20px;
    box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.1);
    z-index: 1000;
    cursor: pointer;
  }
  
  .cart-bar-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .cart-info {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
  }
  
  .cart-count {
    font-size: 14px;
    color: #666;
    margin-bottom: 2px;
  }
  
  .cart-total {
    font-size: 18px;
    font-weight: bold;
    color: #e74c3c;
  }
  
  /* 购物车详情弹出层 */
  .cart-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    z-index: 1001;
  }
  
  .cart-details {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    background: #fff;
    border-radius: 20px 20px 0 0;
    max-height: 70vh;
    transform: translateY(100%);
    transition: transform 0.3s ease;
    z-index: 1002;
    overflow: hidden;
  }
  
  .cart-details.expanded {
    transform: translateY(0);
  }
  
  .cart-details-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px;
    border-bottom: 1px solid #e4e7ed;
    background: #f8f9fa;
  }
  
  .cart-details-header h3 {
    margin: 0;
    font-size: 18px;
  }
  
  .cart-details .cart-items-container {
    max-height: 40vh;
    overflow-y: auto;
    padding: 0 20px;
  }
  
  .cart-details .cart-actions {
    padding: 20px;
    border-top: 1px solid #e4e7ed;
    background: #f8f9fa;
  }
  
  .submitted-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .submitted-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .menu-item-card {
    height: 140px;
    margin-bottom: 15px;
  }
  
  .menu-item-content {
    padding: 12px;
  }
  
  .menu-item-name {
    font-size: 1em;
    margin-bottom: 8px;
  }
  
  .menu-item-price {
    font-size: 1.1em;
    margin-bottom: 10px;
  }
}

/* 桌面版保持原有样式 */
@media (min-width: 769px) {
  .mobile-cart-summary {
    display: none !important;
  }
}
</style>