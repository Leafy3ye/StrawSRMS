<template>
  <div class="tenant-layout">
    <!-- 租户布局容器 -->
    <div class="tenant-header">
      <!-- 租户头部导航 -->
      <h1>{{ restaurantName || '餐厅管理系统' }}</h1>
    </div>
    
    <div class="tenant-main">
      <!-- 子路由内容 -->
      <router-view />
    </div>
  </div>
</template>

<script>
export default {
  name: 'TenantLayout',
  data() {
    return {
      restaurantName: ''
    }
  },
  mounted() {
    // 可以从路由参数或用户信息中获取餐厅名称
    this.restaurantName = this.$route.params.tenantUuid || '餐厅'
  }
}
</script>

<style scoped>
.tenant-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.tenant-header {
  background-color: #409eff;
  color: white;
  padding: 1rem;
  text-align: center;
}

.tenant-main {
  flex: 1;
  padding: 1rem;
}
</style>