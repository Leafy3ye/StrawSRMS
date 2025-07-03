<template>
  <div class="shop-settings">
    <div class="page-header">
      <h2>店铺设置</h2>
      <p>配置您的店铺基本信息和业务参数</p>
    </div>

    <el-card>
      <template #header>
        <span>店铺基本信息</span>
      </template>
      
      <el-form label-width="120px">
        <el-form-item label="店铺名称">
          <el-input placeholder="请输入店铺名称" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="店铺地址">
          <el-input placeholder="请输入店铺地址" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary">保存设置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
// 店铺设置逻辑
</script>

<style scoped>
.shop-settings {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
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
  color: #909399;
  font-size: 14px;
}
</style>