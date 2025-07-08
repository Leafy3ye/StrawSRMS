<template>
  <div class="email-templates">
    <div class="page-header">
      <h2>邮箱模板</h2>
      <p>管理邮件模板，用于订单通知、会员营销等场景</p>
    </div>

    <el-card>
      <template #header>
        <div class="card-header">
          <span>模板列表</span>
          <el-button type="primary" @click="showAddDialog = true">
            <el-icon><Plus /></el-icon>
            新增模板
          </el-button>
        </div>
      </template>

      <el-table :data="templates" style="width: 100%">
        <el-table-column prop="name" label="模板名称" width="200" />
        <el-table-column prop="type" label="模板类型" width="150">
          <template #default="scope">
            <el-tag :type="getTypeColor(scope.row.type)">{{ getTypeName(scope.row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="subject" label="邮件主题" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'active' ? 'success' : 'info'">
              {{ scope.row.status === 'active' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="更新时间" width="180" />
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button type="primary" size="small" @click="editTemplate(scope.row)">编辑</el-button>
            <el-button type="success" size="small" @click="previewTemplate(scope.row)">预览</el-button>
            <el-button type="danger" size="small" @click="deleteTemplate(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑模板对话框 -->
    <el-dialog v-model="showAddDialog" :title="editingTemplate ? '编辑模板' : '新增模板'" width="800px">
      <el-form :model="templateForm" label-width="100px" ref="templateFormRef">
        <el-form-item label="模板名称" prop="name">
          <el-input v-model="templateForm.name" placeholder="请输入模板名称" />
        </el-form-item>
        <el-form-item label="模板类型" prop="type">
          <el-select v-model="templateForm.type" placeholder="选择模板类型">
            <el-option label="订单通知" value="order" />
            <el-option label="会员营销" value="marketing" />
            <el-option label="系统通知" value="system" />
          </el-select>
        </el-form-item>
        <el-form-item label="邮件主题" prop="subject">
          <el-input v-model="templateForm.subject" placeholder="请输入邮件主题" />
        </el-form-item>
        <el-form-item label="邮件内容" prop="content">
          <el-input 
            v-model="templateForm.content" 
            type="textarea" 
            :rows="10"
            placeholder="请输入邮件内容（支持HTML）"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="templateForm.active" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="saveTemplate">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const showAddDialog = ref(false)
const editingTemplate = ref(null)
const templateFormRef = ref()
const templates = ref([
  {
    id: 1,
    name: '订单确认通知',
    type: 'order',
    subject: '您的订单已确认',
    status: 'active',
    updatedAt: '2024-01-15 10:30:00'
  },
  {
    id: 2,
    name: '会员生日祝福',
    type: 'marketing',
    subject: '生日快乐！专属优惠等您来领',
    status: 'active',
    updatedAt: '2024-01-14 15:20:00'
  }
])

const templateForm = reactive({
  name: '',
  type: '',
  subject: '',
  content: '',
  active: true
})

const getTypeName = (type) => {
  const typeMap = {
    order: '订单通知',
    marketing: '会员营销',
    system: '系统通知'
  }
  return typeMap[type] || type
}

const getTypeColor = (type) => {
  const colorMap = {
    order: 'primary',
    marketing: 'success',
    system: 'warning'
  }
  return colorMap[type] || 'info'
}

const editTemplate = (template) => {
  editingTemplate.value = template
  Object.assign(templateForm, template)
  templateForm.active = template.status === 'active'
  showAddDialog.value = true
}

const previewTemplate = (template) => {
  ElMessage.info('预览功能开发中')
}

const deleteTemplate = (id) => {
  ElMessage.success('删除成功')
}

const saveTemplate = () => {
  ElMessage.success('保存成功')
  showAddDialog.value = false
}
</script>

<style scoped>
.email-templates {
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
</style>