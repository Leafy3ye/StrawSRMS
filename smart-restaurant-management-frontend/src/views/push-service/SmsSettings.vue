<template>
  <div class="sms-settings">
    <div class="page-header">
      <h2>短信设置</h2>
      <p>配置您的短信服务，用于发送订单通知、会员消息等</p>
    </div>

    <el-card class="settings-card">
      <template #header>
        <div class="card-header">
          <span>短信服务配置</span>
          <el-tag type="warning">功能开发中</el-tag>
        </div>
      </template>

      <el-form :model="smsConfig" label-width="120px" ref="smsFormRef">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="服务商" prop="provider">
              <el-select v-model="smsConfig.provider" placeholder="选择短信服务商" disabled>
                <el-option label="阿里云短信" value="aliyun" />
                <el-option label="腾讯云短信" value="tencent" />
                <el-option label="华为云短信" value="huawei" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="AccessKey" prop="accessKey">
              <el-input 
                v-model="smsConfig.accessKey" 
                placeholder="请输入AccessKey"
                disabled
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="SecretKey" prop="secretKey">
              <el-input 
                v-model="smsConfig.secretKey" 
                type="password"
                placeholder="请输入SecretKey"
                disabled
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="签名" prop="signature">
              <el-input 
                v-model="smsConfig.signature" 
                placeholder="短信签名"
                disabled
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item>
          <el-button type="primary" disabled>保存配置</el-button>
          <el-button disabled>测试连接</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 开发计划 -->
    <el-card class="development-plan">
      <template #header>
        <span>开发计划</span>
      </template>
      <el-timeline>
        <el-timeline-item timestamp="计划中" type="primary">
          <h4>短信服务商集成</h4>
          <p>集成阿里云、腾讯云等主流短信服务商</p>
        </el-timeline-item>
        <el-timeline-item timestamp="计划中" type="primary">
          <h4>短信模板管理</h4>
          <p>支持自定义短信模板，包括订单通知、会员营销等</p>
        </el-timeline-item>
        <el-timeline-item timestamp="计划中" type="primary">
          <h4>发送记录</h4>
          <p>短信发送历史记录和统计分析</p>
        </el-timeline-item>
      </el-timeline>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'

const smsFormRef = ref()

const smsConfig = reactive({
  provider: '',
  accessKey: '',
  secretKey: '',
  signature: ''
})
</script>

<style scoped>
.sms-settings {
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

.settings-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.development-plan {
  margin-top: 20px;
}
</style>