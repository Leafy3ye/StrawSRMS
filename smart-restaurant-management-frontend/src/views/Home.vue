<template>
  <el-card class="home-card">
    <!-- 欢迎语 -->
    <div class="welcome">
      <h2>{{ welcomeMessage }}</h2>
      <p class="current-time">{{ currentTimeMessage }}</p>
    </div>

    <!-- 统计数据卡片 -->
    <el-row :gutter="20" class="summary">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card" @click="goToStatDetail('today-orders')">
         <h3>今日新增订单数</h3>
          <p>{{ isLoggedIn ? todayOrderCount : "0" }} (笔)</p>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card" @click="goToStatDetail('total-orders')">
          <h3>总订单数</h3>
          <p>{{ isLoggedIn ? totalOrderCount : "0" }} (笔)</p>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card" @click="goToStatDetail('today-revenue')">
          <h3>今日交易金额</h3>
          <p>{{ isLoggedIn ? todayRevenue.toFixed(2) : "0.00" }}(元)</p>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card" @click="goToStatDetail('total-revenue')">
          <h3>总交易金额</h3>
          <p>{{ isLoggedIn ? totalRevenue.toFixed(2) : "0.00" }}(元)</p>
        </el-card>
      </el-col>
    </el-row>

    <!-- 统计图表区域 -->
    <div v-if="isLoggedIn && totalOrderCount > 0" class="charts-section">
      <!-- 上方：缩小的趋势图 -->
      <el-row :gutter="20" class="trend-charts">
        <el-col :span="12">
          <el-card class="chart-card small-chart" shadow="hover">
            <template #header>
              <div class="chart-header">
                <h3>订单数变化趋势</h3>
                <el-radio-group v-model="orderChartPeriod" @change="updateOrderChart" size="small">
                  <el-radio-button label="week">周视图</el-radio-button>
                  <el-radio-button label="month">月视图</el-radio-button>
                </el-radio-group>
              </div>
            </template>
            <div ref="orderChart" class="chart small"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card class="chart-card small-chart" shadow="hover">
            <template #header>
              <div class="chart-header">
                <h3>交易金额变化趋势</h3>
                <el-radio-group v-model="revenueChartPeriod" @change="updateRevenueChart" size="small">
                  <el-radio-button label="week">周视图</el-radio-button>
                  <el-radio-button label="month">月视图</el-radio-button>
                </el-radio-group>
              </div>
            </template>
            <div ref="revenueChart" class="chart small"></div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 下方：新增的分析图表 -->
      <el-row :gutter="20" class="analysis-charts">
        <el-col :span="12">
          <el-card class="chart-card" shadow="hover">
            <template #header>
              <div class="chart-header">
                <h3>订单时间分析</h3>
                <el-radio-group v-model="timeAnalysisPeriod" @change="updateTimeAnalysisChart" size="small">
                  <el-radio-button label="week">周视图</el-radio-button>
                  <el-radio-button label="month">月视图</el-radio-button>
                </el-radio-group>
              </div>
            </template>
            <div v-if="timeAnalysisData.hasData" ref="timeAnalysisChart" class="chart"></div>
            <div v-else class="no-data-message">
              <el-empty description="缺少数据样本，先营业一段时间看看吧" :image-size="100" />
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card class="chart-card" shadow="hover">
            <template #header>
              <div class="chart-header">
                <h3>最受欢迎菜品 TOP5</h3>
                <el-button @click="refreshPopularDishes" size="small" type="primary">刷新</el-button>
              </div>
            </template>
            <div v-if="popularDishesData.length > 0" ref="popularDishesChart" class="chart"></div>
            <div v-else class="no-data-message">
              <el-empty description="缺少数据样本，先营业一段时间看看吧" :image-size="100" />
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 未登录时的提示信息 -->
    <div v-if="!isLoggedIn" class="login-prompt">
      <el-alert type="info" center show-icon>
        <template #title>
          请先
          <el-link type="primary" @click="goToLogin">登录</el-link>
          以查看营业统计数据！
        </template>
      </el-alert>
    </div>
    <!-- 登录但无数据时提示信息 -->
    <div v-if="isLoggedIn && totalOrderCount === 0" class="no-data-prompt">
      <el-alert
        title="哎呀！一条记录都没有！"
        type="info"
        show-icon
        center
      />
    </div>
  </el-card>
</template>

<script setup>
import { useUserStore } from "../store/user";
import { computed, ref, onMounted, onUnmounted, nextTick, watch } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from 'element-plus';
import api from "../utils/api";
import * as echarts from "echarts";

const router = useRouter();
const userStore = useUserStore();
const isLoggedIn = computed(() => !!userStore.user);
const welcomeMessage = computed(() =>
  userStore.user ? `欢迎回来，${userStore.user.username}！` : "欢迎使用 智慧餐饮综合管理系统"
);

// 统计数据
const todayOrderCount = ref(0);
const todayRevenue = ref(0);
const totalOrderCount = ref(0);
const totalRevenue = ref(0);

// 添加当前时间显示
const currentTime = ref(new Date());
const currentTimeMessage = computed(() => {
  const now = currentTime.value;
  const weekdays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'];
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const date = String(now.getDate()).padStart(2, '0');
  const hours = String(now.getHours()).padStart(2, '0');
  const minutes = String(now.getMinutes()).padStart(2, '0');
  const seconds = String(now.getSeconds()).padStart(2, '0');
  const weekday = weekdays[now.getDay()];
  
  return `现在是北京时间 ${year}-${month}-${date} ${hours}:${minutes}:${seconds}，${weekday}`;
});

// 定时更新时间
let timeInterval = null;

// 图表引用和实例
const orderChart = ref(null);
const revenueChart = ref(null);
const timeAnalysisChart = ref(null);
const popularDishesChart = ref(null);

const orderChartPeriod = ref('week');
const revenueChartPeriod = ref('week');
const timeAnalysisPeriod = ref('week');

let orderChartInstance = null;
let revenueChartInstance = null;
let timeAnalysisChartInstance = null;
let popularDishesChartInstance = null;

// 图表数据
const chartData = ref({
  week: {
    orders: [],
    revenue: [],
    dates: []
  },
  month: {
    orders: [],
    revenue: [],
    dates: []
  }
});

const timeAnalysisData = ref({ hasData: false, data: {} });
const popularDishesData = ref([]);

// 从transactions数据生成图表数据
const generateChartDataFromTransactions = (transactions) => {
  console.log('开始生成图表数据，交易记录数量:', transactions.length);
  
  const now = new Date();
  
  // 生成周数据（最近7天）
  const weekDates = [];
  const weekOrders = [];
  const weekRevenue = [];
  
  for (let i = 6; i >= 0; i--) {
    const date = new Date(now);
    date.setDate(date.getDate() - i);
    const dateStr = date.toISOString().split('T')[0];
    weekDates.push(date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' }));
    
    const dayTransactions = transactions.filter(t => {
      // 使用 createdAt 字段
      if (!t.createdAt) return false;
      const transDate = new Date(t.createdAt);
      return transDate.toISOString().split('T')[0] === dateStr;
    });
    
    weekOrders.push(dayTransactions.length);
    // 使用 totalAmount 字段
    weekRevenue.push(dayTransactions.reduce((sum, t) => sum + (parseFloat(t.totalAmount) || 0), 0));
  }
  
  // 生成月数据（最近30天，每5天一个点）
  const monthDates = [];
  const monthOrders = [];
  const monthRevenue = [];
  
  for (let i = 25; i >= 0; i -= 5) {
    const startDate = new Date(now);
    startDate.setDate(startDate.getDate() - i - 4);
    const endDate = new Date(now);
    endDate.setDate(endDate.getDate() - i);
    
    const startDateStr = startDate.toISOString().split('T')[0];
    const endDateStr = endDate.toISOString().split('T')[0];
    monthDates.push(endDate.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' }));
    
    const periodTransactions = transactions.filter(t => {
      // 使用 createdAt 字段
      if (!t.createdAt) return false;
      const transDate = new Date(t.createdAt);
      const transDateStr = transDate.toISOString().split('T')[0];
      return transDateStr >= startDateStr && transDateStr <= endDateStr;
    });
    
    monthOrders.push(periodTransactions.length);
    // 使用 totalAmount 字段
    monthRevenue.push(periodTransactions.reduce((sum, t) => sum + (parseFloat(t.totalAmount) || 0), 0));
  }
  
  chartData.value = {
    week: {
      orders: weekOrders,
      revenue: weekRevenue,
      dates: weekDates
    },
    month: {
      orders: monthOrders,
      revenue: monthRevenue,
      dates: monthDates
    }
  };
  
  console.log('图表数据生成完成:', chartData.value);
};

// 初始化订单图表
const initOrderChart = async () => {
  await nextTick();
  if (!orderChart.value) {
    console.error('订单图表容器未找到');
    return;
  }
  
  try {
    if (orderChartInstance) {
      orderChartInstance.dispose();
    }
    orderChartInstance = echarts.init(orderChart.value);
    console.log('订单图表初始化成功');
    updateOrderChart();
  } catch (error) {
    console.error('订单图表初始化失败:', error);
  }
};

// 初始化收入图表
const initRevenueChart = async () => {
  await nextTick();
  if (!revenueChart.value) {
    console.error('收入图表容器未找到');
    return;
  }
  
  try {
    if (revenueChartInstance) {
      revenueChartInstance.dispose();
    }
    revenueChartInstance = echarts.init(revenueChart.value);
    console.log('收入图表初始化成功');
    updateRevenueChart();
  } catch (error) {
    console.error('收入图表初始化失败:', error);
  }
};

// 初始化订单时间分析图表
const initTimeAnalysisChart = async () => {
  await nextTick();
  if (!timeAnalysisChart.value || !timeAnalysisData.value.hasData) {
    return;
  }
  
  try {
    if (timeAnalysisChartInstance) {
      timeAnalysisChartInstance.dispose();
    }
    timeAnalysisChartInstance = echarts.init(timeAnalysisChart.value);
    console.log('订单时间分析图表初始化成功');
    updateTimeAnalysisChart();
  } catch (error) {
    console.error('订单时间分析图表初始化失败:', error);
  }
};

// 初始化热门菜品图表
const initPopularDishesChart = async () => {
  await nextTick();
  if (!popularDishesChart.value || popularDishesData.value.length === 0) {
    return;
  }
  
  try {
    if (popularDishesChartInstance) {
      popularDishesChartInstance.dispose();
    }
    popularDishesChartInstance = echarts.init(popularDishesChart.value);
    console.log('热门菜品图表初始化成功');
    updatePopularDishesChart();
  } catch (error) {
    console.error('热门菜品图表初始化失败:', error);
  }
};

// 更新订单图表
const updateOrderChart = () => {
  if (!orderChartInstance) {
    console.error('订单图表实例不存在');
    return;
  }
  
  const data = chartData.value[orderChartPeriod.value];
  if (!data || !data.dates || data.dates.length === 0) {
    console.error('订单图表数据为空');
    return;
  }
  
  const option = {
    title: {
      text: orderChartPeriod.value === 'week' ? '最近7天订单数' : '最近30天订单数',
      left: 'center',
      textStyle: {
        fontSize: 14
      }
    },
    tooltip: {
      trigger: 'axis',
      formatter: '{b}<br/>订单数: {c}笔'
    },
    xAxis: {
      type: 'category',
      data: data.dates,
      axisLabel: {
        rotate: 45,
        fontSize: 10
      }
    },
    yAxis: {
      type: 'value',
      name: '订单数(笔)',
      nameTextStyle: {
        fontSize: 10
      },
      axisLabel: {
        fontSize: 10
      }
    },
    series: [{
      data: data.orders,
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 4,
      lineStyle: {
        color: '#409EFF',
        width: 2
      },
      itemStyle: {
        color: '#409EFF'
      },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [{
            offset: 0, color: 'rgba(64, 158, 255, 0.3)'
          }, {
            offset: 1, color: 'rgba(64, 158, 255, 0.1)'
          }]
        }
      }
    }],
    grid: {
      left: '10%',
      right: '5%',
      bottom: '20%',
      top: '20%',
      containLabel: true
    }
  };
  
  try {
    orderChartInstance.setOption(option);
    console.log('订单图表更新成功');
  } catch (error) {
    console.error('订单图表更新失败:', error);
  }
};

// 更新收入图表
const updateRevenueChart = () => {
  if (!revenueChartInstance) {
    console.error('收入图表实例不存在');
    return;
  }
  
  const data = chartData.value[revenueChartPeriod.value];
  if (!data || !data.dates || data.dates.length === 0) {
    console.error('收入图表数据为空');
    return;
  }
  
  const option = {
    title: {
      text: revenueChartPeriod.value === 'week' ? '最近7天交易金额' : '最近30天交易金额',
      left: 'center',
      textStyle: {
        fontSize: 14
      }
    },
    tooltip: {
      trigger: 'axis',
      formatter: '{b}<br/>交易金额: ¥{c}'
    },
    xAxis: {
      type: 'category',
      data: data.dates,
      axisLabel: {
        rotate: 45,
        fontSize: 10
      }
    },
    yAxis: {
      type: 'value',
      name: '金额(元)',
      nameTextStyle: {
        fontSize: 10
      },
      axisLabel: {
        formatter: '¥{value}',
        fontSize: 10
      }
    },
    series: [{
      data: data.revenue,
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 4,
      lineStyle: {
        color: '#67C23A',
        width: 2
      },
      itemStyle: {
        color: '#67C23A'
      },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [{
            offset: 0, color: 'rgba(103, 194, 58, 0.3)'
          }, {
            offset: 1, color: 'rgba(103, 194, 58, 0.1)'
          }]
        }
      }
    }],
    grid: {
      left: '10%',
      right: '5%',
      bottom: '20%',
      top: '20%',
      containLabel: true
    }
  };
  
  try {
    revenueChartInstance.setOption(option);
    console.log('收入图表更新成功');
  } catch (error) {
    console.error('收入图表更新失败:', error);
  }
};

// 更新订单时间分析图表
const updateTimeAnalysisChart = () => {
  if (!timeAnalysisChartInstance || !timeAnalysisData.value.hasData) {
    return;
  }
  
  const data = timeAnalysisData.value.data;
  const hours = [];
  const orderCounts = [];
  
  // 生成24小时数据
  for (let i = 0; i < 24; i++) {
    hours.push(i + ':00');
    orderCounts.push(data[i] || 0);
  }
  
  const option = {
    title: {
      text: `订单时间分析 (${timeAnalysisPeriod.value === 'week' ? '最近7天' : '最近30天'})`,
      left: 'center',
      textStyle: {
        fontSize: 16
      }
    },
    tooltip: {
      trigger: 'axis',
      formatter: '{b}点<br/>订单数: {c}笔'
    },
    xAxis: {
      type: 'category',
      data: hours,
      axisLabel: {
        rotate: 45
      }
    },
    yAxis: {
      type: 'value',
      name: '订单数(笔)'
    },
    series: [{
      data: orderCounts,
      type: 'bar',
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#83bff6' },
          { offset: 0.5, color: '#188df0' },
          { offset: 1, color: '#188df0' }
        ])
      },
      emphasis: {
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#2378f7' },
            { offset: 0.7, color: '#2378f7' },
            { offset: 1, color: '#83bff6' }
          ])
        }
      }
    }],
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      containLabel: true
    }
  };
  
  try {
    timeAnalysisChartInstance.setOption(option);
    console.log('订单时间分析图表更新成功');
  } catch (error) {
    console.error('订单时间分析图表更新失败:', error);
  }
};

// 更新热门菜品图表
const updatePopularDishesChart = () => {
  if (!popularDishesChartInstance || popularDishesData.value.length === 0) {
    return;
  }
  
  const dishNames = popularDishesData.value.map(item => item.dishName);
  const quantities = popularDishesData.value.map(item => item.totalQuantity);
  
  const option = {
    title: {
      text: '最受欢迎菜品 TOP5',
      left: 'center',
      textStyle: {
        fontSize: 16
      }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: '{b}<br/>销量: {c}份'
    },
    xAxis: {
      type: 'category',
      data: dishNames,
      axisLabel: {
        rotate: 45,
        interval: 0
      }
    },
    yAxis: {
      type: 'value',
      name: '销量(份)'
    },
    series: [{
      data: quantities,
      type: 'bar',
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#ffecd2' },
          { offset: 0.5, color: '#fcb69f' },
          { offset: 1, color: '#fcb69f' }
        ])
      },
      emphasis: {
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#fcb69f' },
            { offset: 0.7, color: '#fcb69f' },
            { offset: 1, color: '#ffecd2' }
          ])
        }
      }
    }],
    grid: {
      left: '3%',
      right: '4%',
      bottom: '20%',
      containLabel: true
    }
  };
  
  try {
    popularDishesChartInstance.setOption(option);
    console.log('热门菜品图表更新成功');
  } catch (error) {
    console.error('热门菜品图表更新失败:', error);
  }
};

// 获取订单时间分析数据
const loadTimeAnalysisData = async () => {
  try {
    const response = await api.get(`/api/orders/time-analysis?period=${timeAnalysisPeriod.value}`);
    const data = response.data;
    
    // 检查是否有足够的数据
    const totalOrders = Object.values(data.hourlyStats || {}).reduce((sum, count) => sum + count, 0);
    
    if (totalOrders < 5) { // 如果总订单数少于5个，认为数据不足
      timeAnalysisData.value = { hasData: false, data: {} };
    } else {
      timeAnalysisData.value = { hasData: true, data: data.hourlyStats || {} };
    }
    
    console.log('订单时间分析数据:', timeAnalysisData.value);
  } catch (error) {
    console.error('获取订单时间分析数据失败:', error);
    timeAnalysisData.value = { hasData: false, data: {} };
  }
};

// 获取热门菜品数据
const loadPopularDishesData = async () => {
  try {
    const response = await api.get('/api/orders/popular-dishes?limit=5');
    const data = response.data;
    
    // 检查是否有足够的数据
    if (!data || data.length < 3) { // 如果少于3个菜品，认为数据不足
      popularDishesData.value = [];
    } else {
      popularDishesData.value = data;
    }
    
    console.log('热门菜品数据:', popularDishesData.value);
  } catch (error) {
    console.error('获取热门菜品数据失败:', error);
    popularDishesData.value = [];
  }
};

// 刷新热门菜品数据
const refreshPopularDishes = async () => {
  await loadPopularDishesData();
  if (popularDishesData.value.length > 0) {
    await nextTick();
    await initPopularDishesChart();
  }
};

// 获取统计数据
const loadStats = async () => {
  console.log(' loadStats 开始执行...');
  console.log(' 当前登录状态:', isLoggedIn.value);
  
  if (!isLoggedIn.value) {
    console.log('用户未登录，跳过数据加载');
    return;
  }
  
  try {
    // 获取所有交易记录
    console.log('开始请求 /api/transactions...');
    const allTransactionsResponse = await api.get('/api/transactions');
    const allTransactions = allTransactionsResponse.data;
    
    console.log('获取到交易记录:', allTransactions.length, '条');
    
    // 计算今日数据
    const today = new Date();
    const todayStr = today.toISOString().split('T')[0]; // YYYY-MM-DD格式
    
    const todayTransactions = allTransactions.filter(transaction => {
      if (!transaction.createdAt) return false;
      const transactionDate = new Date(transaction.createdAt);
      const transactionDateStr = transactionDate.toISOString().split('T')[0];
      return transactionDateStr === todayStr;
    });
    
    console.log('今日交易记录:', todayTransactions.length, '条');
    
    // 计算今日统计数据
    todayOrderCount.value = todayTransactions.length;
    todayRevenue.value = Number(todayTransactions.reduce((sum, transaction) => {
      return sum + (parseFloat(transaction.totalAmount) || 0);
    }, 0));
    
    // 计算总统计数据
    totalOrderCount.value = allTransactions.length;
    totalRevenue.value = Number(allTransactions.reduce((sum, transaction) => {
      return sum + (parseFloat(transaction.totalAmount) || 0);
    }, 0));
    
    console.log('今日订单数:', todayOrderCount.value);
    console.log('今日营收:', todayRevenue.value);
    console.log('总订单数:', totalOrderCount.value);
    console.log('总营收:', totalRevenue.value);
    
    // 生成图表数据
    generateChartDataFromTransactions(allTransactions);
    
    // 加载新增的分析数据
    await loadTimeAnalysisData();
    await loadPopularDishesData();
    
    // 等待DOM更新后初始化图表
    await nextTick();
    if (totalOrderCount.value > 0) {
      console.log('开始初始化图表...');
      await initOrderChart();
      await initRevenueChart();
      await initTimeAnalysisChart();
      await initPopularDishesChart();
    }
    
    console.log('loadStats 执行完成！');
    
  } catch (error) {
    console.error('获取统计数据失败:', error);
    console.error('错误详情:', error.response?.data || error.message);
    
    // 发生错误时重置数据
    todayOrderCount.value = 0;
    todayRevenue.value = 0;
    totalOrderCount.value = 0;
    totalRevenue.value = 0;
  }
};

// 新增：专门获取图表数据的函数
const loadChartData = async () => {
  try {
    // 从后端获取专门的图表数据
    const weekResponse = await api.get('/api/orders/chart/week');
    const monthResponse = await api.get('/api/orders/chart/month');
    
    chartData.value = {
      week: weekResponse.data,
      month: monthResponse.data
    };
    
    console.log('图表数据加载完成:', chartData.value);
  } catch (error) {
    console.error('获取图表数据失败:', error);
    // 如果专门的图表接口不可用，回退到原来的方式
    const allTransactionsResponse = await api.get('/api/transactions');
    generateChartDataFromTransactions(allTransactionsResponse.data);
  }
};

// 跳转到登录页面
const goToLogin = () => {
  router.push("/login");
};

// 跳转到统计详情页面
const goToStatDetail = (type) => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录');
    return;
  }
  router.push(`/stats/${type}`);
};

// 监听登录状态变化
watch(isLoggedIn, (newValue, oldValue) => {
  console.log('🔍 登录状态变化:', oldValue, '->', newValue);
  if (newValue) {
    console.log('用户已登录，开始加载数据...');
    loadStats();
  } else {
    console.log('用户已登出，重置数据...');
    // 未登录时重置数据和销毁图表
    todayOrderCount.value = 0;
    todayRevenue.value = 0;
    totalOrderCount.value = 0;
    totalRevenue.value = 0;
    timeAnalysisData.value = { hasData: false, data: {} };
    popularDishesData.value = [];
    
    // 销毁所有图表实例
    [orderChartInstance, revenueChartInstance, timeAnalysisChartInstance, popularDishesChartInstance]
      .forEach(instance => {
        if (instance) {
          instance.dispose();
        }
      });
    orderChartInstance = null;
    revenueChartInstance = null;
    timeAnalysisChartInstance = null;
    popularDishesChartInstance = null;
  }
});

// 监听时间分析周期变化
watch(timeAnalysisPeriod, async () => {
  await loadTimeAnalysisData();
  if (timeAnalysisData.value.hasData) {
    await nextTick();
    await initTimeAnalysisChart();
  }
});

// 组件挂载时加载数据
onMounted(() => {
  console.log('Home 组件已挂载');
  console.log('挂载时登录状态:', isLoggedIn.value);
  loadStats();
  // 启动时间更新定时器
  timeInterval = setInterval(() => {
    currentTime.value = new Date();
  }, 1000);
});

// 清理定时器
onUnmounted(() => {
  if (timeInterval) {
    clearInterval(timeInterval);
  }
  // 清理所有图表实例
  [orderChartInstance, revenueChartInstance, timeAnalysisChartInstance, popularDishesChartInstance]
    .forEach(instance => {
      if (instance) {
        instance.dispose();
      }
    });
});
</script>

<style scoped>
.home-card {
  max-width: 1200px;
  margin: 20px auto;
  padding: 20px;
  background-color: #ffffff;
  border-radius: 12px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.welcome {
  text-align: center;
  margin-bottom: 20px;
}

.welcome h2 {
  margin: 0 0 10px 0;
  font-size: 28px;
  font-weight: 600;
}

.current-time {
  margin: 0;
  font-size: 16px;
  color: #666;
  font-weight: 400;
}

.stat-card {
  cursor: pointer;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.15);
}

.summary .el-card {
  text-align: center;
}

.charts-section {
  margin-top: 30px;
}

.trend-charts {
  margin-bottom: 20px;
}

.analysis-charts {
  margin-top: 20px;
}

.chart-card {
  margin-bottom: 20px;
}

.small-chart .chart {
  height: 250px !important;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-header h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.chart {
  width: 100%;
  height: 400px;
}

.chart.small {
  height: 250px;
}

.no-data-message {
  height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-prompt {
  text-align: center;
  margin: 30px 0;
}

.no-data-prompt {
  margin: 30px 0;
}

.el-link {
  font-size: 16px;
  cursor: pointer;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chart-header {
    flex-direction: column;
    gap: 10px;
  }
  
  .chart {
    height: 300px;
  }
  
  .chart.small {
    height: 200px;
  }
}
</style>