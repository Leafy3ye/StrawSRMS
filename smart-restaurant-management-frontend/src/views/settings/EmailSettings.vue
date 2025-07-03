<template>
  <div class="email-settings">
    <div class="page-header">
      <h2>邮箱设置</h2>
      <p>配置您的邮件服务，用于发送订单通知、会员消息等</p>
    </div>

    <el-card class="settings-card">
      <template #header>
        <div class="card-header">
          <span>SMTP邮件配置</span>
          <el-button 
            v-if="emailConfig.id" 
            type="success" 
            size="small" 
            @click="testConnection"
            :loading="testing"
          >
            测试连接
          </el-button>
        </div>
      </template>

      <el-form 
        :model="emailConfig" 
        :rules="rules" 
        ref="emailFormRef" 
        label-width="120px"
        class="email-form"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="SMTP服务器" prop="smtpHost">
              <el-input 
                v-model="emailConfig.smtpHost" 
                placeholder="如：smtp.qq.com"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="端口" prop="smtpPort">
              <el-input-number 
                v-model="emailConfig.smtpPort" 
                :min="1" 
                :max="65535" 
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="邮箱账号" prop="username">
              <el-input 
                v-model="emailConfig.username" 
                placeholder="发送邮件的邮箱账号"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱授权码" prop="password">
              <div style="display: flex; align-items: center; gap: 8px;">
                <el-input 
                  v-model="emailConfig.password" 
                  type="password" 
                  placeholder="邮箱授权码" 
                  show-password 
                  clearable
                  style="flex: 1;"
                />
                <el-tooltip 
                  content="点击查看示例：QQ邮箱授权码获取教程" 
                  placement="top"
                >
                  <el-icon 
                    class="help-icon" 
                    @click="openHelpPage"
                    size="16"
                  >
                    <QuestionFilled />
                  </el-icon>
                </el-tooltip>
              </div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="发件人名称" prop="fromName">
              <el-input 
                v-model="emailConfig.fromName" 
                placeholder="如：XX餐厅"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="发件人邮箱" prop="fromEmail">
              <el-input 
                v-model="emailConfig.fromEmail" 
                placeholder="显示的发件人邮箱"
                clearable
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="安全设置">
          <el-checkbox v-model="emailConfig.enableSsl">启用SSL加密</el-checkbox>
          <el-checkbox v-model="emailConfig.enableTls" style="margin-left: 20px;">启用TLS加密</el-checkbox>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="saveConfig" :loading="saving">
            保存配置
          </el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 常用邮箱配置模板 -->
    <el-card class="templates-card" style="margin-top: 20px;">
      <template #header>
        <span>常用邮箱配置模板</span>
      </template>
      
      <el-row :gutter="20">
        <el-col :span="6" v-for="template in emailTemplates" :key="template.name">
          <div class="template-card" @click="applyTemplate(template)">
            <div class="template-icon">
              <el-icon size="24"><Message /></el-icon>
            </div>
            <h4>{{ template.name }}</h4>
            <p>{{ template.host }}:{{ template.port }}</p>
            <div class="template-features">
              <el-tag size="small" v-if="template.ssl">SSL</el-tag>
              <el-tag size="small" v-if="template.tls" type="success">TLS</el-tag>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 邮件发送记录 -->
    <el-card class="logs-card" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>最近邮件发送记录</span>
          <el-button size="small" @click="refreshLogs">刷新</el-button>
        </div>
      </template>
      
      <el-table :data="emailLogs" style="width: 100%" v-loading="loadingLogs">
        <el-table-column prop="recipient" label="收件人" width="200" />
        <el-table-column prop="subject" label="主题" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'success' ? 'success' : 'danger'">
              {{ scope.row.status === 'success' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sentAt" label="发送时间" width="180" />
        <el-table-column prop="errorMessage" label="错误信息" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Message, QuestionFilled } from '@element-plus/icons-vue'
import api from '../../utils/api'

const emailFormRef = ref()
const saving = ref(false)
const testing = ref(false)
const loadingLogs = ref(false)

// 邮箱配置数据
const emailConfig = reactive({
  id: null,
  smtpHost: '',
  smtpPort: 587,
  username: '',
  password: '',
  fromName: '',
  fromEmail: '',
  enableSsl: true,
  enableTls: true
})

// 邮件发送记录
const emailLogs = ref([])

// 表单验证规则
const rules = {
  smtpHost: [{ required: true, message: '请输入SMTP服务器', trigger: 'blur' }],
  smtpPort: [{ required: true, message: '请输入端口号', trigger: 'blur' }],
  username: [{ required: true, message: '请输入邮箱账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入邮箱密码', trigger: 'blur' }],
  fromName: [{ required: true, message: '请输入发件人名称', trigger: 'blur' }],
  fromEmail: [
    { required: true, message: '请输入发件人邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

// 邮箱模板
const emailTemplates = [
  { 
    name: 'QQ邮箱', 
    host: 'smtp.qq.com', 
    port: 465, 
    ssl: true, 
    tls: false,
    description: '腾讯QQ邮箱服务'
  },
  { 
    name: '163邮箱', 
    host: 'smtp.163.com', 
    port: 465, 
    ssl: true, 
    tls: false,
    description: '网易163邮箱服务'
  },
  { 
    name: 'Gmail', 
    host: 'smtp.gmail.com', 
    port: 587, 
    ssl: false, 
    tls: true,
    description: 'Google Gmail服务'
  },
  { 
    name: '企业邮箱', 
    host: 'smtp.exmail.qq.com', 
    port: 465, 
    ssl: true, 
    tls: false,
    description: '腾讯企业邮箱'
  }
]

// 应用邮箱模板
const applyTemplate = (template) => {
  emailConfig.smtpHost = template.host
  emailConfig.smtpPort = template.port
  emailConfig.enableSsl = template.ssl
  emailConfig.enableTls = template.tls
  ElMessage.success(`已应用${template.name}配置模板`)
}

// 保存配置
const saveConfig = async () => {
  try {
    await emailFormRef.value.validate()
    saving.value = true
    
    const response = await api.post('/api/email-config', emailConfig)
    emailConfig.id = response.data.id
    
    ElMessage.success('邮箱配置保存成功')
  } catch (error) {
    ElMessage.error('保存失败：' + (error.response?.data?.message || error.message))
  } finally {
    saving.value = false
  }
}

// 测试连接
const testConnection = async () => {
  try {
    await emailFormRef.value.validate()
    testing.value = true
    
    await api.post('/api/email-config/test', emailConfig)
    ElMessage.success('邮箱配置测试成功！')
  } catch (error) {
    ElMessage.error('测试失败：' + (error.response?.data?.message || error.message))
  } finally {
    testing.value = false
  }
}

// 重置表单
const resetForm = () => {
  emailFormRef.value.resetFields()
  Object.assign(emailConfig, {
    id: null,
    smtpHost: '',
    smtpPort: 587,
    username: '',
    password: '',
    fromName: '',
    fromEmail: '',
    enableSsl: true,
    enableTls: true
  })
}

// 加载邮箱配置
const loadEmailConfig = async () => {
  try {
    const response = await api.get('/api/email-config')
    if (response.data) {
      Object.assign(emailConfig, response.data)
    }
  } catch (error) {
    console.log('暂无邮箱配置')
  }
}
// 打开帮助页面
const openHelpPage = () => {
  window.open('https://service.mail.qq.com/detail/0/75', '_blank')
}

// 刷新邮件日志
const refreshLogs = async () => {
  try {
    loadingLogs.value = true
    const response = await api.get('/api/email-logs')
    emailLogs.value = response.data
  } catch (error) {
    ElMessage.error('加载邮件记录失败')
  } finally {
    loadingLogs.value = false
  }
}

// 页面加载时获取配置
onMounted(() => {
  loadEmailConfig()
  refreshLogs()
})
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

.settings-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.email-form {
  padding: 20px 0;
}

.templates-card .el-row {
  margin: 0;
}

.template-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  height: 140px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.template-card:hover {
  border-color: #409eff;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.template-icon {
  color: #409eff;
  margin-bottom: 8px;
}

.template-card h4 {
  margin: 8px 0 4px 0;
  color: #303133;
  font-size: 16px;
}

.template-card p {
  margin: 0 0 8px 0;
  color: #909399;
  font-size: 12px;
}

.template-features {
  display: flex;
  justify-content: center;
  gap: 4px;
}

.logs-card {
  margin-top: 20px;
}
.help-icon {
  color: #909399;
  cursor: pointer;
  transition: color 0.3s;
}

.help-icon:hover {
  color: #409eff;
}
</style>