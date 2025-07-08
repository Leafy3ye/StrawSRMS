<template>
  <div class="email-settings">
    <div class="page-header">
      <h2>邮箱设置</h2>
      <p>配置您的邮件服务，用于发送订单通知、会员消息等</p>
    </div>

    <el-card class="development-card">
      <div class="development-content">
        <el-icon class="development-icon" size="80">
          <Tools />
        </el-icon>
        <h3>功能开发中</h3>
        <p>邮箱设置功能正在开发中，敬请期待...</p>
        <div class="feature-list">
          <h4>即将支持的功能：</h4>
          <ul>
            <li>SMTP邮件服务器配置</li>
            <li>邮件模板自定义</li>
            <li>邮件发送记录查看</li>
            <li>邮件测试功能</li>
            <li>多种邮箱服务商支持</li>
          </ul>
        </div>
        <el-button type="primary" disabled>
          <el-icon><Setting /></el-icon>
          配置邮箱服务
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { Tools, Setting } from '@element-plus/icons-vue'
</script>

<style scoped>
.email-settings {
  padding: 20px;
  max-width: 1200px;
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

.development-card {
  min-height: 500px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.development-content {
  text-align: center;
  padding: 40px;
}

.development-icon {
  color: #909399;
  margin-bottom: 20px;
}

.development-content h3 {
  font-size: 24px;
  color: #303133;
  margin: 0 0 10px 0;
}

.development-content > p {
  color: #909399;
  font-size: 16px;
  margin-bottom: 30px;
}

.feature-list {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 20px;
  margin: 30px 0;
  text-align: left;
  max-width: 400px;
  margin-left: auto;
  margin-right: auto;
}

.feature-list h4 {
  color: #606266;
  margin: 0 0 15px 0;
  font-size: 16px;
  text-align: center;
}

.feature-list ul {
  margin: 0;
  padding-left: 20px;
}

.feature-list li {
  color: #909399;
  margin: 8px 0;
  font-size: 14px;
}

.el-button {
  margin-top: 20px;
}
</style>