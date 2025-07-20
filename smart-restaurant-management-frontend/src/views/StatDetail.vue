<template>
  <el-card class="stat-detail-card">
    <template #header>
      <div class="stat-header">
        <el-button type="primary" @click="goBack" style="margin-right: 10px;">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <h2>{{ getTitle() }}</h2>
      </div>
    </template>

    <!-- 统计信息概览 -->
    <div class="stat-overview">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-statistic title="总数量" :value="transactions.length" suffix="笔" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="总金额" :value="totalAmount" prefix="¥" :precision="2" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="平均金额" :value="averageAmount" prefix="¥" :precision="2" />
        </el-col>
        <el-col :span="6">
          <!-- 将 ElStatistic 改为普通的文本显示 -->
          <div class="time-range-display">
            <div class="time-range-title">时间范围</div>
            <div class="time-range-value">{{ getTimeRange() }}</div>
          </div>
        </el-col>
      </el-row>
    </div>

    <el-divider />

    <!-- 交易记录表格 -->
    <el-table :data="paginatedTransactions" style="width: 100%" stripe>
      <el-table-column prop="id" label="订单ID" width="100" />
      <el-table-column prop="tableName" label="桌位" width="120" />
      <el-table-column label="结算方式" width="120">
        <template #default="scope">
          <el-tag :type="scope.row.paymentType === 'member' ? 'success' : 'info'">
            {{ scope.row.paymentType === 'member' ? '会员结算' : '正常结算' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="会员信息" width="150">
        <template #default="scope">
          <div v-if="scope.row.paymentType === 'member'">
            <div>{{ scope.row.memberName }}</div>
            <div style="font-size: 12px; color: #999;">{{ scope.row.memberPhone }}</div>
          </div>
          <span v-else style="color: #999;">-</span>
        </template>
      </el-table-column>
      <el-table-column label="原始金额" width="120">
        <template #default="scope">
          ¥{{ parseFloat(scope.row.originalAmount || scope.row.totalAmount).toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column label="实际收款" width="120">
        <template #default="scope">
          <span :style="{ color: scope.row.paymentType === 'member' ? '#67C23A' : '#303133' }">
            ¥{{ parseFloat(scope.row.actualAmount || scope.row.totalAmount).toFixed(2) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="交易时间" width="180">
        <template #default="scope">
          {{ formatTime(scope.row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="scope">
          <el-button type="primary" size="small" @click="viewDetail(scope.row)">
            查看详情
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="transactions.length"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 订单详情弹窗 -->
    <el-dialog v-model="showDetailDialog" title="订单详情" width="900px">
      <div v-if="selectedTransaction">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单ID">{{ selectedTransaction.id }}</el-descriptions-item>
          <el-descriptions-item label="桌位">{{ selectedTransaction.tableName }}</el-descriptions-item>
          <el-descriptions-item label="菜品数量">{{ selectedTransaction.orderCount }} 份</el-descriptions-item>
          <el-descriptions-item label="结算方式">
            <el-tag :type="selectedTransaction.paymentType === 'member' ? 'success' : 'info'">
              {{ selectedTransaction.paymentType === 'member' ? '会员结算' : '正常结算' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="交易时间" :span="2">{{ formatTime(selectedTransaction.createdAt) }}</el-descriptions-item>
        </el-descriptions>

        <!-- 会员信息（仅在会员结算时显示） -->
        <div v-if="selectedTransaction.paymentType === 'member'">
          <el-divider>会员信息</el-divider>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="会员姓名">{{ selectedTransaction.memberName }}</el-descriptions-item>
            <el-descriptions-item label="手机号码">{{ selectedTransaction.memberPhone }}</el-descriptions-item>
            <el-descriptions-item label="会员等级">{{ selectedTransaction.memberLevel }}</el-descriptions-item>
            <el-descriptions-item label="折扣率">{{ (parseFloat(selectedTransaction.discountRate || 1) * 100).toFixed(0) }}%</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 金额信息 -->
        <el-divider>金额信息</el-divider>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="原始金额">¥{{ parseFloat(selectedTransaction.originalAmount || selectedTransaction.totalAmount).toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="折扣金额" v-if="selectedTransaction.paymentType === 'member'">
            ¥{{ parseFloat(selectedTransaction.discountAmount || 0).toFixed(2) }}
          </el-descriptions-item>
          <el-descriptions-item label="实际收款">
            <span :style="{ color: selectedTransaction.paymentType === 'member' ? '#67C23A' : '#303133', fontWeight: 'bold' }">
              ¥{{ parseFloat(selectedTransaction.actualAmount || selectedTransaction.totalAmount).toFixed(2) }}
            </span>
          </el-descriptions-item>
        </el-descriptions>

        <el-divider>菜品详情</el-divider>

        <div v-if="orderDetails && orderDetails.length > 0">
          <el-table :data="orderDetails" style="width: 100%" size="small">
            <el-table-column prop="dishName" label="菜品名称" />
            <el-table-column prop="quantity" label="数量" width="80">
              <template #default="scope">
                {{ scope.row.quantity }} 份
              </template>
            </el-table-column>
            <el-table-column prop="price" label="单价" width="100">
              <template #default="scope">
                ¥{{ parseFloat(scope.row.price).toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column label="小计" width="100">
              <template #default="scope">
                ¥{{ (parseFloat(scope.row.price) * scope.row.quantity).toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" />
          </el-table>
        </div>

        <div v-else>
          <el-alert
            title="暂无菜品详情"
            description="无法找到该交易对应的菜品信息"
            type="warning"
            :closable="false"
            show-icon
          />
        </div>
      </div>

      <template #footer>
        <el-button @click="showDetailDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ArrowLeft } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import api from '../utils/api';

const route = useRoute();
const router = useRouter();
const statType = route.params.type;

// 数据
const transactions = ref([]);
const currentPage = ref(1);
const pageSize = ref(20);
const showDetailDialog = ref(false);
const selectedTransaction = ref(null);
const orderDetails = ref([]);

// 计算属性
const totalAmount = computed(() => {
  return transactions.value.reduce((sum, t) => sum + parseFloat(t.actualAmount || t.totalAmount || 0), 0);
});

const averageAmount = computed(() => {
  return transactions.value.length > 0 ? totalAmount.value / transactions.value.length : 0;
});

const paginatedTransactions = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return transactions.value.slice(start, end);
});

// 方法
const getTitle = () => {
  const titles = {
    'today-orders': '今日新增订单详情',
    'total-orders': '总订单详情',
    'today-revenue': '今日交易金额详情',
    'total-revenue': '总交易金额详情'
  };
  return titles[statType] || '统计详情';
};

const getTimeRange = () => {
  if (statType.includes('today')) {
    return '今日';
  }
  if (transactions.value.length === 0) return '无数据';
  
  const dates = transactions.value.map(t => new Date(t.createdAt)).sort((a, b) => a - b);
  const start = dates[0].toLocaleDateString('zh-CN');
  const end = dates[dates.length - 1].toLocaleDateString('zh-CN');
  return `${start} - ${end}`;
};

const formatTime = (timeStr) => {
  return new Date(timeStr).toLocaleString('zh-CN');
};

const goBack = () => {
  router.push('/');
};

const viewDetail = async (transaction) => {
  try {
    selectedTransaction.value = transaction;
    orderDetails.value = [];
    showDetailDialog.value = true;

    // 调用API获取交易详情和菜品信息
    const response = await api.get(`/api/transactions/${transaction.id}/details`);
    if (response.data.orders) {
      orderDetails.value = response.data.orders;
    }
  } catch (error) {
    console.error('获取订单详情失败:', error);
    ElMessage.error('获取订单详情失败');
  }
};

const handleSizeChange = (val) => {
  pageSize.value = val;
  currentPage.value = 1;
};

const handleCurrentChange = (val) => {
  currentPage.value = val;
};

// 加载数据
const loadData = async () => {
  try {
    let response;
    
    if (statType === 'today-orders' || statType === 'today-revenue') {
      // 获取今日数据
      response = await api.get('/api/transactions');
      const today = new Date().toLocaleDateString('zh-CN');
      transactions.value = response.data.filter(t => {
        const transDate = new Date(t.createdAt).toLocaleDateString('zh-CN');
        return transDate === today;
      });
    } else {
      // 获取所有数据
      response = await api.get('/api/transactions');
      transactions.value = response.data;
    }
    
    // 按时间倒序排列
    transactions.value.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
    
  } catch (error) {
    console.error('加载数据失败:', error);
    ElMessage.error('加载数据失败');
  }
};

onMounted(() => {
  loadData();
});
</script>

<style scoped>
.stat-detail-card {
  max-width: 1200px;
  margin: 20px auto;
  padding: 20px;
}

.stat-header {
  display: flex;
  align-items: center;
}

.stat-header h2 {
  margin: 0;
  color: #303133;
}

.stat-overview {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

/* 新增：时间范围显示样式 */
.time-range-display {
  text-align: center;
}

.time-range-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.time-range-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  line-height: 1;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.el-table {
  margin-bottom: 20px;
}
</style>