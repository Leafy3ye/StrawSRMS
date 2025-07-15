<template>
  <div class="super-admin-dashboard">
    <div class="dashboard-header">
      <h1>超级管理员看板</h1>
      <p>系统总览和租户管理</p>
    </div>

    <!-- 统计卡片 -->
    <div class="statistics-cards">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ statistics.totalTenants }}</div>
              <div class="stat-label">总租户数</div>
            </div>
            <el-icon class="stat-icon"><OfficeBuilding /></el-icon>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ statistics.totalStores }}</div>
              <div class="stat-label">总店铺数</div>
            </div>
            <el-icon class="stat-icon"><Shop /></el-icon>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ statistics.totalUsers }}</div>
              <div class="stat-label">总用户数</div>
            </div>
            <el-icon class="stat-icon"><User /></el-icon>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ statistics.activeTenants }}</div>
              <div class="stat-label">活跃租户</div>
            </div>
            <el-icon class="stat-icon"><CircleCheck /></el-icon>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 租户列表 -->
    <el-card class="data-card">
      <template #header>
        <div class="card-header">
          <span>租户管理</span>
          <el-button type="primary" @click="refreshData">刷新数据</el-button>
        </div>
      </template>
      
      <el-table :data="tenants" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="tenantName" label="租户名称" width="200" />
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
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewTenantDetail(scope.row.id)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 店铺列表 -->
    <el-card class="data-card">
      <template #header>
        <div class="card-header">
          <span>店铺管理</span>
        </div>
      </template>
      
      <el-table :data="stores" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="storeName" label="店铺名称" width="200" />
        <el-table-column prop="tenantName" label="所属租户" width="150" />
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

    <!-- 租户详情对话框 -->
    <el-dialog v-model="showTenantDetail" title="租户详情" width="80%">
      <div v-if="selectedTenantDetail">
        <h3>基本信息</h3>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="租户名称">{{ selectedTenantDetail.tenant.tenantName }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ selectedTenantDetail.tenant.email }}</el-descriptions-item>
          <el-descriptions-item label="电话">{{ selectedTenantDetail.tenant.phone }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ selectedTenantDetail.tenant.status }}</el-descriptions-item>
          <el-descriptions-item label="订阅计划">{{ selectedTenantDetail.tenant.subscriptionPlan }}</el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ formatDate(selectedTenantDetail.tenant.createdAt) }}</el-descriptions-item>
        </el-descriptions>

        <h3 style="margin-top: 20px;">店铺列表</h3>
        <el-table :data="selectedTenantDetail.stores" style="width: 100%">
          <el-table-column prop="storeName" label="店铺名称" />
          <el-table-column prop="address" label="地址" />
          <el-table-column prop="phone" label="电话" />
          <el-table-column prop="status" label="状态" />
        </el-table>

        <h3 style="margin-top: 20px;">用户列表</h3>
        <el-table :data="selectedTenantDetail.users" style="width: 100%">
          <el-table-column prop="username" label="用户名" />
          <el-table-column prop="email" label="邮箱" />
          <el-table-column prop="phone" label="电话" />
          <el-table-column prop="setupCompleted" label="设置完成" />
          <el-table-column prop="createdAt" label="注册时间">
            <template #default="scope">
              {{ formatDate(scope.row.createdAt) }}
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { OfficeBuilding, Shop, User, CircleCheck } from '@element-plus/icons-vue'
import api from '../utils/api'

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
    selectedTenantDetail.value = response.data
    showTenantDetail.value = true
  } catch (error) {
    ElMessage.error('获取租户详情失败: ' + (error.response?.data || error.message))
  }
}

// 刷新数据
const refreshData = () => {
  fetchDashboardData()
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
.super-admin-dashboard {
  padding: 20px;
}

.dashboard-header {
  margin-bottom: 30px;
}

.dashboard-header h1 {
  margin: 0;
  color: #2c3e50;
  font-size: 28px;
}

.dashboard-header p {
  margin: 5px 0 0 0;
  color: #7f8c8d;
  font-size: 16px;
}

.statistics-cards {
  margin-bottom: 30px;
}

.stat-card {
  position: relative;
  overflow: hidden;
}

.stat-card .el-card__body {
  padding: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 32px;
  font-weight: bold;
  color: #2c3e50;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #7f8c8d;
}

.stat-icon {
  font-size: 40px;
  color: #3498db;
  opacity: 0.3;
}

.data-card {
  margin-bottom: 30px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header span {
  font-size: 18px;
  font-weight: bold;
  color: #2c3e50;
}
</style>