<template>
  <div class="theme-settings">
    <div class="page-header">
      <h2>主题设置</h2>
      <p>个性化您的系统界面主题和样式</p>
    </div>

    <!-- 导航栏配色 -->
    <el-card class="theme-card">
      <template #header>
        <span>导航栏配色</span>
      </template>
      
      <div class="color-section">
        <div class="color-options">
          <div 
            class="color-item" 
            v-for="navTheme in navbarThemes" 
            :key="navTheme.name"
            @click="selectNavbarTheme(navTheme)"
            :class="{ active: currentTheme.navbar === navTheme.value }"
          >
            <div class="color-preview navbar-preview" :style="{
              backgroundColor: navTheme.primary,
              borderColor: navTheme.border
            }">
              <div class="nav-item" :style="{ backgroundColor: navTheme.active }"></div>
            </div>
            <span>{{ navTheme.name }}</span>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 主题背景配色 - 暂时移除 -->
    <!-- 
    <el-card class="theme-card">
      <template #header>
        <span>主题背景配色</span>
      </template>
      
      <div class="color-section">
        <div class="color-options">
          <div 
            class="color-item" 
            v-for="bgTheme in backgroundThemes" 
            :key="bgTheme.name"
            @click="selectBackgroundTheme(bgTheme)"
            :class="{ active: currentTheme.background === bgTheme.value }"
          >
            <div class="color-preview bg-preview" :style="{
              backgroundColor: bgTheme.primary,
              color: bgTheme.text
            }">
              <div class="content-area" :style="{ backgroundColor: bgTheme.content }"></div>
            </div>
            <span>{{ bgTheme.name }}</span>
          </div>
        </div>
      </div>
    </el-card>
    -->

    <!-- 预览和保存 -->
    <el-card class="theme-card">
      <template #header>
        <span>预览效果</span>
      </template>
      
      <div class="preview-section">
        <div class="theme-preview-container">
          <div class="mini-layout" :style="getPreviewStyle()">
            <div class="mini-navbar" :style="getNavbarPreviewStyle()">
              <div class="mini-logo">LOGO</div>
              <div class="mini-menu-item active">首页</div>
              <div class="mini-menu-item">菜单</div>
            </div>
            <div class="mini-content" :style="getContentPreviewStyle()">
              <div class="mini-card">内容区域</div>
            </div>
          </div>
        </div>
        
        <div class="action-buttons">
          <el-button @click="resetTheme">重置默认</el-button>
          <el-button type="primary" @click="saveTheme" :loading="saving">保存主题</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../../store/user'
import api from '../../utils/api'

const userStore = useUserStore()
const saving = ref(false)

// 当前主题配置
const currentTheme = reactive({
  navbar: 'default',
  background: 'light' // 固定为默认白色背景
})

// 导航栏主题选项
const navbarThemes = [
  {
    name: '默认灰蓝',
    value: 'default',
    primary: '#304156',
    active: '#409EFF',
    border: '#434a5a',
    text: '#bfcbd9'
  },
  {
    name: '深邃黑',
    value: 'dark',
    primary: '#1a1a1a',
    active: '#409EFF',
    border: '#333333',
    text: '#ffffff'
  },
  {
    name: '温馨米白',
    value: 'cream',
    primary: '#faf9f7',
    active: '#409EFF',
    border: '#e8e6e3',
    text: '#5a5a5a'
  }
]

// 背景主题选项 - 固定为默认白色
const backgroundThemes = [
  {
    name: '正常白色',
    value: 'light',
    primary: '#f0f2f5',
    content: '#ffffff',
    text: '#2c3e50'
  }
]

// 选择导航栏主题
const selectNavbarTheme = (theme) => {
  currentTheme.navbar = theme.value
  applyThemePreview()
}

// 选择背景主题 - 暂时移除功能
// const selectBackgroundTheme = (theme) => {
//   currentTheme.background = theme.value
//   applyThemePreview()
// }

// 获取预览样式
const getPreviewStyle = () => {
  const bgTheme = backgroundThemes.find(t => t.value === currentTheme.background)
  return {
    backgroundColor: bgTheme.primary,
    color: bgTheme.text
  }
}

const getNavbarPreviewStyle = () => {
  const navTheme = navbarThemes.find(t => t.value === currentTheme.navbar)
  return {
    backgroundColor: navTheme.primary,
    borderColor: navTheme.border,
    color: navTheme.text
  }
}

const getContentPreviewStyle = () => {
  const bgTheme = backgroundThemes.find(t => t.value === currentTheme.background)
  return {
    backgroundColor: bgTheme.content,
    color: bgTheme.text
  }
}

// 应用主题预览
const applyThemePreview = () => {
  // 这里可以添加实时预览逻辑
}

// 保存主题设置
const saveTheme = async () => {
  try {
    saving.value = true
    
    const themeData = {
      navbarTheme: currentTheme.navbar,
      backgroundTheme: 'light' // 固定为默认白色背景
    }
    
    await api.put('/api/users/theme-settings', themeData)
    
    // 更新用户store中的主题信息
    if (userStore.user) {
      userStore.user.themeSettings = themeData
      localStorage.setItem('user', JSON.stringify(userStore.user))
    }
    
    // 应用主题到全局
    applyGlobalTheme(themeData)
    
    // 强制触发Navbar组件重新渲染
    window.dispatchEvent(new Event('theme-changed'))
    
    ElMessage.success('主题设置保存成功！')
  } catch (error) {
    console.error('保存主题设置失败:', error)
    ElMessage.error('保存主题设置失败，请重试')
  } finally {
    saving.value = false
  }
}

// 应用全局主题 - 移除滤镜效果
const applyGlobalTheme = (themeData) => {
  const navTheme = navbarThemes.find(t => t.value === themeData.navbarTheme)
  const bgTheme = backgroundThemes.find(t => t.value === 'light') // 固定使用默认白色背景
  
  if (navTheme && bgTheme) {
    // 设置CSS变量
    const root = document.documentElement
    root.style.setProperty('--navbar-bg-color', navTheme.primary)
    root.style.setProperty('--navbar-active-color', navTheme.active)
    root.style.setProperty('--navbar-border-color', navTheme.border || '#434a5a')
    root.style.setProperty('--navbar-text-color', navTheme.text)
    
    root.style.setProperty('--bg-primary-color', bgTheme.primary)
    root.style.setProperty('--bg-content-color', bgTheme.content)
    root.style.setProperty('--bg-text-color', bgTheme.text)
    
    // 设置根元素背景色
    root.style.backgroundColor = bgTheme.primary
    
    // 移除之前的滤镜效果
    const mainContent = document.querySelector('.main-container')
    if (mainContent) {
      mainContent.style.filter = 'none'
    }
    
    // 移除媒体元素的滤镜
    const mediaElements = document.querySelectorAll('img, video, iframe, canvas')
    mediaElements.forEach(el => {
      el.style.filter = 'none'
    })
  }
}

// 重置主题
const resetTheme = () => {
  currentTheme.navbar = 'default'
  currentTheme.background = 'light' // 固定为默认白色背景
  applyThemePreview()
}

// 加载用户主题设置
const loadUserTheme = () => {
  console.log('加载用户主题设置...', userStore.user?.themeSettings)

  if (userStore.user?.themeSettings) {
    try {
      // 处理字符串格式的主题设置
      const settings = typeof userStore.user.themeSettings === 'string'
        ? JSON.parse(userStore.user.themeSettings)
        : userStore.user.themeSettings

      console.log('解析后的主题设置:', settings)

      currentTheme.navbar = settings.navbarTheme || 'default'
      currentTheme.background = 'light' // 固定为默认白色背景

      // 应用主题时也固定使用白色背景
      applyGlobalTheme({
        navbarTheme: settings.navbarTheme || 'default',
        backgroundTheme: 'light'
      })

      console.log('当前主题设置为:', currentTheme)
    } catch (error) {
      console.error('解析主题设置失败:', error)
      // 如果解析失败，使用默认主题
      currentTheme.navbar = 'default'
      currentTheme.background = 'light'
    }
  } else {
    console.log('没有找到用户主题设置，使用默认主题')
    currentTheme.navbar = 'default'
    currentTheme.background = 'light'
  }
}

// 监听用户数据变化
watch(() => userStore.user, (newUser) => {
  if (newUser) {
    console.log('用户数据更新，重新加载主题设置')
    loadUserTheme()
  }
}, { immediate: true })

onMounted(() => {
  loadUserTheme()
})
</script>

<style scoped>
.theme-settings {
  padding: 20px;
  max-width: 1000px;
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

.theme-card {
  margin-bottom: 20px;
}

.color-section {
  padding: 10px 0;
}

.color-options {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 20px;
}

.color-item {
  text-align: center;
  cursor: pointer;
  padding: 10px;
  border-radius: 8px;
  transition: all 0.3s;
}

.color-item:hover {
  background-color: #f5f7fa;
}

.color-item.active {
  background-color: #e6f7ff;
  border: 2px solid #409EFF;
}

.color-preview {
  width: 100px;
  height: 70px;
  border-radius: 8px;
  margin: 0 auto 8px;
  position: relative;
  border: 2px solid #e4e7ed;
  transition: transform 0.3s;
}

.color-preview:hover {
  transform: scale(1.05);
}

.navbar-preview {
  display: flex;
  align-items: center;
  padding: 8px;
}

.nav-item {
  width: 20px;
  height: 4px;
  border-radius: 2px;
  margin-left: 8px;
}

.bg-preview {
  padding: 8px;
}

.content-area {
  width: 100%;
  height: 40px;
  border-radius: 4px;
  border: 1px solid rgba(0,0,0,0.1);
}

.preview-section {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.theme-preview-container {
  flex: 1;
}

.mini-layout {
  width: 300px;
  height: 200px;
  border: 2px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
  display: flex;
}

.mini-navbar {
  width: 80px;
  height: 100%;
  padding: 8px;
  border-right: 1px solid;
}

.mini-logo {
  font-size: 10px;
  font-weight: bold;
  margin-bottom: 8px;
  text-align: center;
}

.mini-menu-item {
  font-size: 8px;
  padding: 4px;
  margin-bottom: 4px;
  border-radius: 2px;
  text-align: center;
}

.mini-menu-item.active {
  background-color: var(--navbar-active-color, #409EFF);
  color: white;
}

.mini-content {
  flex: 1;
  padding: 8px;
}

.mini-card {
  width: 100%;
  height: 60px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  border: 1px solid rgba(0,0,0,0.1);
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
</style>