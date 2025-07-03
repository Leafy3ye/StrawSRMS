<template>
  <div class="theme-settings">
    <div class="page-header">
      <h2>主题设置</h2>
      <p>个性化您的系统界面主题和样式</p>
    </div>

    <el-card>
      <template #header>
        <span>主题配色</span>
      </template>
      
      <div class="theme-options">
        <div class="theme-item" v-for="theme in themes" :key="theme.name">
          <div class="theme-preview" :style="{backgroundColor: theme.primary}">
            <div class="theme-accent" :style="{backgroundColor: theme.accent}"></div>
          </div>
          <span>{{ theme.name }}</span>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
const themes = [
  { name: '默认蓝', primary: '#409EFF', accent: '#67C23A' },
  { name: '商务灰', primary: '#909399', accent: '#F56C6C' },
  { name: '活力橙', primary: '#E6A23C', accent: '#409EFF' },
  { name: '优雅紫', primary: '#9C27B0', accent: '#FF9800' }
]
</script>

<style scoped>
.theme-settings {
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

.theme-options {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 20px;
}

.theme-item {
  text-align: center;
  cursor: pointer;
}

.theme-preview {
  width: 80px;
  height: 60px;
  border-radius: 8px;
  margin: 0 auto 8px;
  position: relative;
  transition: transform 0.3s;
}

.theme-preview:hover {
  transform: scale(1.05);
}

.theme-accent {
  position: absolute;
  bottom: 4px;
  right: 4px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 2px solid white;
}
</style>