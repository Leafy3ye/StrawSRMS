<template>
  <div class="admin-console">
    <!-- 顶部导航栏 -->
    <div class="admin-header">
      <div class="header-left">
        <el-icon class="logo-icon"><Setting /></el-icon>
        <h1 class="console-title">管理员控制台</h1>
      </div>
      <div class="header-right">
        <el-dropdown @command="handleCommand">
          <span class="admin-info">
            <el-icon><UserFilled /></el-icon>
            <span>系统管理员</span>
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">
                <el-icon><SwitchButton /></el-icon>
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="admin-content">
      <!-- 系统概览 -->
      <div class="overview-section">
        <h2 class="section-title">系统概览</h2>
        <div class="overview-cards">
          <el-row :gutter="20">
            <el-col :xs="12" :sm="6" :md="6" :lg="6">
              <el-card class="overview-card">
                <div class="card-content">
                  <div class="card-icon tenant-icon">
                    <el-icon><OfficeBuilding /></el-icon>
                  </div>
                  <div class="card-info">
                    <div class="card-number">{{ statistics.totalTenants }}</div>
                    <div class="card-label">租户总数</div>
                  </div>
                </div>
              </el-card>
            </el-col>
            <el-col :xs="12" :sm="6" :md="6" :lg="6">
              <el-card class="overview-card">
                <div class="card-content">
                  <div class="card-icon store-icon">
                    <el-icon><Shop /></el-icon>
                  </div>
                  <div class="card-info">
                    <div class="card-number">{{ statistics.totalStores }}</div>
                    <div class="card-label">店铺总数</div>
                  </div>
                </div>
              </el-card>
            </el-col>
            <el-col :xs="12" :sm="6" :md="6" :lg="6">
              <el-card class="overview-card">
                <div class="card-content">
                  <div class="card-icon user-icon">
                    <el-icon><User /></el-icon>
                  </div>
                  <div class="card-info">
                    <div class="card-number">{{ statistics.totalUsers }}</div>
                    <div class="card-label">用户总数</div>
                  </div>
                </div>
              </el-card>
            </el-col>
            <el-col :xs="12" :sm="6" :md="6" :lg="6">
              <el-card class="overview-card">
                <div class="card-content">
                  <div class="card-icon active-icon">
                    <el-icon><CircleCheck /></el-icon>
                  </div>
                  <div class="card-info">
                    <div class="card-number">{{ statistics.activeTenants }}</div>
                    <div class="card-label">活跃租户</div>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </div>

      <!-- 租户管理 -->
      <div class="tenant-section">
        <div class="section-header">
          <h2 class="section-title">租户管理</h2>
          <div class="section-actions">
            <el-button type="primary" @click="refreshData" :loading="loading">
              <el-icon><Refresh /></el-icon>
              刷新数据
            </el-button>
          </div>
        </div>

        <el-card class="tenant-table-card">
          <el-table :data="tenants" style="width: 100%" v-loading="loading">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column label="品牌名称" width="200">
              <template #default="scope">
                {{ scope.row.tenantName || '未设置' }}
              </template>
            </el-table-column>
            <el-table-column prop="email" label="邮箱" width="200" />
            <el-table-column prop="phone" label="电话" width="150" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.status === 'ACTIVE' ? 'success' : 'danger'">
                  {{ scope.row.status === 'ACTIVE' ? '活跃' : '停用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="storeCount" label="店铺数" width="100" />
            <el-table-column prop="userCount" label="用户数" width="100" />
            <el-table-column prop="subscriptionPlan" label="订阅计划" width="120" />
            <el-table-column prop="createdAt" label="注册时间" width="180">
              <template #default="scope">
                {{ formatDate(scope.row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="350">
              <template #default="scope">
                <el-button type="primary" size="small" @click="viewTenantDetail(scope.row.id)">
                  查看详情
                </el-button>
                <el-button type="success" size="small" @click="viewTenantData(scope.row.id)" style="margin-left: 5px;">
                  查看数据
                </el-button>
                <el-button type="danger" size="small" @click="deleteTenant(scope.row)" style="margin-left: 5px;">
                  删除租户
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </div>

      <!-- 店铺管理 -->
      <div class="store-section">
        <div class="section-header">
          <h2 class="section-title">店铺管理</h2>
        </div>

        <el-card class="store-table-card">
          <el-table :data="stores" style="width: 100%">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="storeName" label="店铺名称" width="200" />
            <el-table-column label="所属品牌" width="150">
              <template #default="scope">
                {{ scope.row.tenantName || '未设置' }}
              </template>
            </el-table-column>
            <el-table-column prop="address" label="地址" width="250" />
            <el-table-column prop="phone" label="电话" width="150" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.status === 'ACTIVE' ? 'success' : 'danger'">
                  {{ scope.row.status === 'ACTIVE' ? '营业' : '停业' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="isDefault" label="默认店铺" width="100">
              <template #default="scope">
                <el-tag v-if="scope.row.isDefault" type="warning">是</el-tag>
                <span v-else>否</span>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="创建时间" width="180">
              <template #default="scope">
                {{ formatDate(scope.row.createdAt) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </div>
    </div>

    <!-- 租户详情对话框 -->
    <el-dialog v-model="showTenantDetail" title="租户详情" width="80%">
      <div v-if="selectedTenantDetail && selectedTenantDetail.tenant" v-loading="!selectedTenantDetail">
        <h3>基本信息</h3>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="品牌名称">{{ selectedTenantDetail.tenant?.tenantName || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ selectedTenantDetail.tenant?.email || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="电话">{{ selectedTenantDetail.tenant?.phone || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ selectedTenantDetail.tenant?.status || '未知' }}</el-descriptions-item>
          <el-descriptions-item label="订阅计划">{{ selectedTenantDetail.tenant?.subscriptionPlan || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ selectedTenantDetail.tenant?.createdAt ? formatDate(selectedTenantDetail.tenant.createdAt) : '未知' }}</el-descriptions-item>
        </el-descriptions>

        <h3 style="margin-top: 20px;">店铺列表</h3>
        <el-table :data="selectedTenantDetail.stores || []" style="width: 100%">
          <el-table-column prop="storeName" label="店铺名称" />
          <el-table-column prop="address" label="地址" />
          <el-table-column prop="phone" label="电话" />
          <el-table-column prop="status" label="状态" />
        </el-table>

        <h3 style="margin-top: 20px;">用户列表</h3>
        <el-table :data="selectedTenantDetail.users || []" style="width: 100%">
          <el-table-column prop="username" label="用户名" />
          <el-table-column prop="email" label="邮箱" />
          <el-table-column prop="phone" label="电话" />
          <el-table-column prop="setupCompleted" label="设置完成">
            <template #default="scope">
              {{ scope.row.setupCompleted ? '是' : '否' }}
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="注册时间">
            <template #default="scope">
              {{ scope.row.createdAt ? formatDate(scope.row.createdAt) : '未知' }}
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>

    <!-- 租户数据查看对话框 -->
    <el-dialog v-model="showTenantData" title="租户数据查看" width="90%" :close-on-click-modal="false">
      <div v-if="selectedTenantData">
        <el-tabs v-model="activeDataTab" type="card">
          <!-- 会员数据 -->
          <el-tab-pane label="会员数据" name="members">
            <el-table :data="selectedTenantData.members" style="width: 100%" v-loading="dataLoading">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="name" label="姓名" width="120" />
              <el-table-column prop="phone" label="电话" width="150" />
              <el-table-column prop="email" label="邮箱" width="200" />
              <el-table-column prop="balance" label="余额" width="100">
                <template #default="scope">
                  ¥{{ scope.row.balance }}
                </template>
              </el-table-column>
              <el-table-column prop="points" label="积分" width="100" />
              <el-table-column prop="createdAt" label="注册时间" width="180">
                <template #default="scope">
                  {{ formatDate(scope.row.createdAt) }}
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <!-- 订单数据 -->
          <el-tab-pane label="订单数据" name="orders">
            <el-table :data="selectedTenantData.orders" style="width: 100%" v-loading="dataLoading">
              <el-table-column prop="id" label="订单ID" width="100" />
              <el-table-column prop="tableId" label="桌号" width="80" />
              <el-table-column prop="dishId" label="菜品ID" width="100" />
              <el-table-column prop="quantity" label="数量" width="80" />
              <el-table-column prop="price" label="价格" width="100">
                <template #default="scope">
                  ¥{{ scope.row.price }}
                </template>
              </el-table-column>
              <el-table-column prop="completed" label="状态" width="100">
                <template #default="scope">
                  <el-tag :type="scope.row.completed ? 'success' : 'warning'">
                    {{ scope.row.completed ? '已完成' : '进行中' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createdAt" label="下单时间" width="180">
                <template #default="scope">
                  {{ formatDate(scope.row.createdAt) }}
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <!-- 菜品数据 -->
          <el-tab-pane label="菜品数据" name="dishes">
            <el-table :data="selectedTenantData.dishes" style="width: 100%" v-loading="dataLoading">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="name" label="菜品名称" width="200" />
              <el-table-column prop="category" label="分类" width="120" />
              <el-table-column prop="price" label="价格" width="100">
                <template #default="scope">
                  ¥{{ scope.row.price }}
                </template>
              </el-table-column>
              <el-table-column prop="description" label="描述" width="300" />
              <el-table-column prop="available" label="状态" width="100">
                <template #default="scope">
                  <el-tag :type="scope.row.available ? 'success' : 'danger'">
                    {{ scope.row.available ? '可用' : '不可用' }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  OfficeBuilding,
  Shop,
  User,
  CircleCheck,
  Setting,
  UserFilled,
  ArrowDown,
  SwitchButton,
  Refresh
} from '@element-plus/icons-vue'
import api, { createTenantApi } from '../utils/api'
import { useUserStore } from '../store/user'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const tenants = ref([])
const stores = ref([])
const statistics = ref({
  totalTenants: 0,
  totalStores: 0,
  totalUsers: 0,
  activeTenants: 0,
  activeStores: 0
})

const showTenantDetail = ref(false)
const selectedTenantDetail = ref(null)

// 租户数据查看相关
const showTenantData = ref(false)
const selectedTenantData = ref(null)
const activeDataTab = ref('members')
const dataLoading = ref(false)

// 获取看板数据
const fetchDashboardData = async () => {
  loading.value = true
  try {
    const response = await api.get('/api/admin/dashboard')
    const data = response.data
    
    tenants.value = data.tenants
    stores.value = data.stores
    statistics.value = data.statistics
    
  } catch (error) {
    ElMessage.error('获取看板数据失败: ' + (error.response?.data || error.message))
  } finally {
    loading.value = false
  }
}

// 查看租户详情
const viewTenantDetail = async (tenantId) => {
  try {
    const response = await api.get(`/api/admin/tenants/${tenantId}`)
    console.log('租户详情响应:', response.data) // 添加调试日志
    selectedTenantDetail.value = response.data
    showTenantDetail.value = true
  } catch (error) {
    console.error('获取租户详情失败:', error) // 添加调试日志
    ElMessage.error('获取租户详情失败: ' + (error.response?.data || error.message))
  }
}

// 查看租户数据
const viewTenantData = async (tenantId) => {
  try {
    dataLoading.value = true
    showTenantData.value = true
    activeDataTab.value = 'members'

    // 获取租户的会员、订单、菜品数据
    const [membersResponse, ordersResponse, dishesResponse] = await Promise.all([
      api.get(`/api/admin/tenants/${tenantId}/members`),
      api.get(`/api/admin/tenants/${tenantId}/orders`),
      api.get(`/api/admin/tenants/${tenantId}/dishes`)
    ])

    selectedTenantData.value = {
      members: membersResponse.data || [],
      orders: ordersResponse.data || [],
      dishes: dishesResponse.data || []
    }

  } catch (error) {
    ElMessage.error('获取租户数据失败: ' + (error.response?.data || error.message))
    showTenantData.value = false
  } finally {
    dataLoading.value = false
  }
}

// 刷新数据
const refreshData = () => {
  fetchDashboardData()
}

// 删除租户
const deleteTenant = async (tenant) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除租户 "${tenant.tenantName}" 吗？此操作将删除该租户下的所有数据，包括店铺、用户、订单等，且无法恢复！`,
      '危险操作确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'error',
        dangerouslyUseHTMLString: true
      }
    )

    // 二次确认
    await ElMessageBox.confirm(
      `请再次确认：您即将删除租户 "${tenant.tenantName}" 及其所有相关数据。<br><br><strong style="color: red;">此操作不可逆转！</strong>`,
      '最终确认',
      {
        confirmButtonText: '我确定要删除',
        cancelButtonText: '取消',
        type: 'error',
        dangerouslyUseHTMLString: true
      }
    )

    loading.value = true

    await api.delete(`/api/admin/tenants/${tenant.id}`)

    ElMessage.success('租户删除成功')

    // 刷新数据
    await fetchDashboardData()

  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除租户失败: ' + (error.response?.data || error.message))
    }
  } finally {
    loading.value = false
  }
}

// 处理下拉菜单命令
const handleCommand = async (command) => {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm(
        '确定要退出登录吗？',
        '退出确认',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        }
      )

      // 使用用户状态管理的退出方法
      userStore.logout()

      // 清除额外的本地存储
      localStorage.removeItem('userInfo')
      localStorage.removeItem('tenantId')
      localStorage.removeItem('storeId')

      // 显示退出消息
      ElMessage.success('已退出登录')

      // 立即跳转到登录页面
      await router.replace('/login')

    } catch {
      // 用户取消退出
    }
  }
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  return new Date(dateString).toLocaleString('zh-CN')
}

onMounted(() => {
  fetchDashboardData()
})
</script>

<style scoped>
/* 整体布局 */
.admin-console {
  min-height: 100vh;
  background: linear-gradient(135deg, #f6f9fc 0%, #e9f1f7 100%);
  padding: 0;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

/* 头部 */
.admin-header {
  background: white;
  padding: 0 24px;
  height: 64px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.logo-icon {
  font-size: 32px;
  color: #409eff;
}

.console-title {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  color: #303133;
}

.admin-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border-radius: 20px;
  transition: background 0.3s;
  color: #606266;
}

.admin-info:hover {
  background: #f5f7fa;
}

/* 内容区域 */
.admin-content {
  padding: 32px 24px;
  max-width: 1600px;
  margin: 0 auto;
}

.section-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 24px;
}

/* 概览卡片 */
.overview-cards .el-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  transition: transform 0.3s;
}

.overview-cards .el-card:hover {
  transform: translateY(-4px);
}

.card-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.card-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.tenant-icon { background: #e6f7ff; color: #1890ff; }
.store-icon { background: #f6ffed; color: #52c41a; }
.user-icon { background: #fff7e6; color: #fa8c16; }
.active-icon { background: #f0f5ff; color: #2f54eb; }

.card-number {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.card-label {
  font-size: 14px;
  color: #909399;
}

/* 表格卡片 */
.tenant-table-card, .store-table-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  margin-bottom: 32px;
}

.el-table {
  border-radius: 12px;
  overflow: hidden;
}

.el-table th {
  background: #fafafa;
  color: #606266;
}

/* 对话框 */
.el-dialog {
  border-radius: 12px;
}

/* 响应式 */
@media (max-width: 768px) {
  .admin-content {
    padding: 16px;
  }

  .overview-cards .el-col {
    margin-bottom: 16px;
  }
}
</style>