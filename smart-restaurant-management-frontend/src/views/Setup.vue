<template>
  <div class="setup-container">
    <div class="setup-card">
      <h2>完善餐厅信息</h2>
      <el-form :model="setupForm" :rules="rules" ref="setupFormRef" label-width="120px">
        <el-form-item label="餐厅名称" prop="restaurantName">
          <el-input v-model="setupForm.restaurantName" placeholder="请输入餐厅名称"></el-input>
        </el-form-item>
        
        <el-form-item label="餐厅地址" prop="restaurantAddress">
          <el-input v-model="setupForm.restaurantAddress" placeholder="请输入餐厅地址"></el-input>
        </el-form-item>
        
        <el-form-item label="联系电话" prop="restaurantPhone">
          <el-input v-model="setupForm.restaurantPhone" placeholder="请输入联系电话"></el-input>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="submitSetup" :loading="loading">完成设置</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Setup',
  data() {
    return {
      loading: false,
      setupForm: {
        restaurantName: '',
        restaurantAddress: '',
        restaurantPhone: ''
      },
      rules: {
        restaurantName: [
          { required: true, message: '请输入餐厅名称', trigger: 'blur' }
        ],
        restaurantAddress: [
          { required: true, message: '请输入餐厅地址', trigger: 'blur' }
        ],
        restaurantPhone: [
          { required: true, message: '请输入联系电话', trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    async submitSetup() {
      try {
        await this.$refs.setupFormRef.validate()
        this.loading = true
        
        // TODO: 调用后端API完成设置
        // const response = await this.$http.post('/api/setup', this.setupForm)
        
        this.$message.success('设置完成！')
        this.$router.push(`/${this.$route.params.tenantUuid}/dashboard`)
      } catch (error) {
        console.error('设置失败:', error)
        this.$message.error('设置失败，请重试')
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.setup-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80vh;
  padding: 2rem;
}

.setup-card {
  background: white;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 500px;
}

.setup-card h2 {
  text-align: center;
  margin-bottom: 2rem;
  color: #303133;
}
</style>