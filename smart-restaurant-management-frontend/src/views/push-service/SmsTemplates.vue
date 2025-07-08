<template>
  <div class="sms-templates">
    <div class="page-header">
      <h2>短信模板</h2>
      <p>管理短信模板，用于订单通知、会员营销等场景</p>
    </div>

    <el-card>
      <template #header>
        <div class="card-header">
          <span>模板列表</span>
          <el-button type="primary" @click="showAddDialog = true" disabled>
            <el-icon><Plus /></el-icon>
            新增模板
          </el-button>
        </div>
      </template>

      <el-empty description="短信模板功能开发中" />
    </el-card>

    <!-- 功能说明 -->
    <el-card class="feature-info">
      <template #header>
        <span>功能说明</span>
      </template>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="订单通知">
          订单状态变更时自动发送短信通知给客户
        </el-descriptions-item>
        <el-descriptions-item label="会员营销">
          生日祝福、优惠活动等营销短信推送
        </el-descriptions-item>
        <el-descriptions-item label="验证码">
          登录、注册等场景的验证码短信
        </el-descriptions-item>
        <el-descriptions-item label="预约提醒">
          餐桌预约、活动预约等提醒短信
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Plus } from '@element-plus/icons-vue'

const showAddDialog = ref(false)
</script>

<style scoped>
.sms-templates {
  padding: 20px;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.page-header p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.feature-info {
  margin-top: 20px;
}
</style>