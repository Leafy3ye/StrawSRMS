<template>
  <el-card class="menu-card">
    <div class="header-section">
      <h2>菜单管理</h2>
      <div class="header-actions">
        <el-input
          v-model="searchQuery"
          placeholder="搜索菜品（支持中文、拼音、首字母）"
          clearable
          style="width: 300px; margin-right: 15px;"
          :prefix-icon="Search"
        />
        <el-button type="primary" @click="showAddDialog = true" :icon="Plus">
          添加菜品
        </el-button>
      </div>
    </div>

    <!-- 菜品列表 -->
    <el-table :data="filteredMenuList" border style="width: 100%" class="menu-table">
      <el-table-column label="菜品图片" width="120" align="center">
        <template #default="scope">
          <el-image
            v-if="scope.row.imageUrl"
            :src="scope.row.imageUrl + '?t=' + Date.now()"
            :preview-src-list="[scope.row.imageUrl]"
            class="dish-image"
            fit="cover"
            :lazy="false"
          />
          <div v-else class="no-image">
            <el-icon><Picture /></el-icon>
            <span>暂无图片</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="菜品名称" width="200" />
      <el-table-column prop="price" label="价格 (元)" width="150">
        <template #default="scope">
          ¥{{ scope.row.price.toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" show-overflow-tooltip />
      <el-table-column prop="updatedAt" label="修改时间" width="200">
        <template #default="scope">
          {{ formatDateTime(scope.row.updatedAt) }}
        </template>
      </el-table-column>

      <!-- 操作列 -->
      <el-table-column label="操作" align="center" width="180">
        <template #default="scope">
          <el-button type="warning" @click="editDish(scope.row)" size="small">
            编辑
          </el-button>
          <el-button type="danger" @click="deleteDish(scope.row.id)" size="small">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 空状态显示 -->
    <div v-if="filteredMenuList.length === 0 && searchQuery" class="empty-search">
      <el-empty description="未找到相关菜品" />
      <p class="search-tip">尝试使用其他关键词搜索，支持中文、拼音、首字母</p>
    </div>

    <!-- 添加/编辑菜品弹窗 -->
    <el-dialog
      v-model="showAddDialog"
      :title="isEditing ? '编辑菜品' : '添加菜品'"
      width="500px"
      @close="resetForm"
    >
      <el-form :model="dishForm" label-width="100px" ref="dishFormRef" :rules="formRules">
        <el-form-item label="菜品图片">
          <div class="image-upload-section">
            <el-upload
              class="dish-image-uploader"
              :action="uploadUrl"
              :show-file-list="false"
              :on-success="handleImageSuccess"
              :before-upload="beforeImageUpload"
              :headers="uploadHeaders"
            >
              <img v-if="dishForm.imageUrl" :src="dishForm.imageUrl + '?t=' + Date.now()" class="uploaded-image" />
              <div v-else class="upload-placeholder">
                <el-icon class="upload-icon"><Plus /></el-icon>
                <div class="upload-text">点击上传图片</div>
              </div>
            </el-upload>
            <div class="upload-tips">
              <p>支持 jpg、png、gif 格式，文件大小不超过 2MB</p>
            </div>
          </div>
        </el-form-item>
        
        <el-form-item label="菜品名称" prop="name">
          <el-input v-model="dishForm.name" placeholder="请输入菜品名称" />
        </el-form-item>

        <el-form-item label="价格" prop="price">
          <el-input-number 
            v-model="dishForm.price" 
            :min="0" 
            :precision="2"
            placeholder="请输入菜品价格"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="描述">
          <el-input 
            v-model="dishForm.description" 
            type="textarea" 
            :rows="3"
            placeholder="请输入菜品描述（可选）"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showAddDialog = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitting">
            {{ isEditing ? '更新' : '添加' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, onMounted, computed } from "vue";
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Picture, Search } from '@element-plus/icons-vue';
import api from "../utils/api";
import { pinyin } from 'pinyin-pro'

const menuList = ref([]);
const searchQuery = ref('');
const showAddDialog = ref(false);
const isEditing = ref(false);
const submitting = ref(false);
const dishFormRef = ref();

const dishForm = ref({
  id: null,
  name: "",
  price: 0,
  description: "",
  imageUrl: ""
});

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入菜品名称', trigger: 'blur' },
    { min: 1, max: 50, message: '菜品名称长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  price: [
    { required: true, message: '请输入菜品价格', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '价格必须大于0', trigger: 'blur' }
  ]
};

// 智能搜索过滤
const filteredMenuList = computed(() => {
  if (!searchQuery.value.trim()) {
    return menuList.value;
  }
  
  const query = searchQuery.value.toLowerCase().trim();
  
  return menuList.value.filter(item => {
    try {
      // 1. 直接匹配菜品名称
      if (item.name.toLowerCase().includes(query)) {
        return true;
      }
      
      // 2. 拼音匹配（全拼）
      const pinyinResult = pinyin(item.name, {
        style: pinyin.STYLE_NORMAL,
        heteronym: false
      });
      
      // 将二维数组转为一维数组，然后连接成字符串
      const fullPinyin = pinyinResult.map(arr => arr[0]).join('').toLowerCase();
      
      if (fullPinyin.includes(query)) {
        return true;
      }
      
      // 3. 拼音首字母匹配
      const firstLetterResult = pinyin(item.name, {
        style: pinyin.STYLE_FIRST_LETTER,
        heteronym: false
      });
      
      const firstLetters = firstLetterResult.map(arr => arr[0]).join('').toLowerCase();
      
      if (firstLetters.includes(query)) {
        return true;
      }
      
      // 4. 支持部分拼音匹配（如"bing"匹配"冰美式"）
      const pinyinArray = pinyinResult.map(arr => arr[0]);
      
      // 检查是否有任何一个拼音以查询字符串开头
      const hasMatchingPinyin = pinyinArray.some(py => 
        py.toLowerCase().startsWith(query)
      );
      
      if (hasMatchingPinyin) {
        return true;
      }
      
      // 5. 描述匹配
      if (item.description && item.description.toLowerCase().includes(query)) {
        return true;
      }
      
      return false;
    } catch (error) {
      console.error('拼音转换错误:', error, '菜品名称:', item.name);
      // 如果拼音转换出错，至少保证中文名称匹配还能工作
      return item.name.toLowerCase().includes(query);
    }
  });
});

// 上传相关配置
const uploadUrl = computed(() => {
  const baseURL = api.defaults.baseURL;
  // 确保URL格式正确，避免双斜杠
  return baseURL.endsWith('/')
    ? `${baseURL}api/files/upload/dish`
    : `${baseURL}/api/files/upload/dish`;
});

const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token');
  return token ? { 'Authorization': `Bearer ${token}` } : {};
});

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-';
  const date = new Date(dateTime);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

// 加载菜单数据
const loadMenu = async () => {
  try {
    const response = await api.get("/api/dishes");
    menuList.value = response.data;
  } catch (error) {
    console.error("Failed to load dishes", error);
    ElMessage.error('加载菜单失败');
  }
};

// 图片上传成功回调
const handleImageSuccess = async (response) => {
  try {
    // 确保图片URL是完整的访问路径
    const baseURL = api.defaults.baseURL;
    const imageUrl = response.url.startsWith('http')
      ? response.url
      : (baseURL.endsWith('/') ? `${baseURL.slice(0, -1)}${response.url}` : `${baseURL}${response.url}`);

    // 更新表单数据
    dishForm.value.imageUrl = imageUrl;

    // 如果是编辑模式，立即保存到数据库
    if (isEditing.value && dishForm.value.id) {
      console.log('正在保存菜品图片:', {
        dishId: dishForm.value.id,
        imageUrl: dishForm.value.imageUrl,
        dishData: dishForm.value
      });

      await api.put(`/api/dishes/${dishForm.value.id}`, dishForm.value);
      ElMessage.success('菜品图片更新成功');

      // 刷新菜单列表以显示最新图片
      await loadMenu();

      console.log('菜品图片保存完成，菜单已刷新');
    } else {
      ElMessage.success('图片上传成功');
    }
  } catch (error) {
    console.error('保存菜品图片失败:', error);
    ElMessage.error('图片上传成功，但保存失败，请稍后重试');
  }
};

// 图片上传前验证
const beforeImageUpload = (file) => {
  const isImage = file.type.startsWith('image/');
  const isLt2M = file.size / 1024 / 1024 < 2;

  if (!isImage) {
    ElMessage.error('只能上传图片文件!');
    return false;
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!');
    return false;
  }
  return true;
};

// 提交表单
const submitForm = async () => {
  if (!dishFormRef.value) return;
  
  try {
    await dishFormRef.value.validate();
    submitting.value = true;
    
    if (isEditing.value) {
      await api.put(`/api/dishes/${dishForm.value.id}`, dishForm.value);
      ElMessage.success('菜品更新成功');
    } else {
      await api.post("/api/dishes", dishForm.value);
      ElMessage.success('菜品添加成功');
    }
    
    showAddDialog.value = false;
    loadMenu();
  } catch (error) {
    console.error("Failed to save dish", error);
    ElMessage.error(isEditing.value ? '更新菜品失败' : '添加菜品失败');
  } finally {
    submitting.value = false;
  }
};

// 编辑菜品
const editDish = (dish) => {
  isEditing.value = true;
  dishForm.value = { ...dish };
  showAddDialog.value = true;
};

// 删除菜品
const deleteDish = async (id) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这个菜品吗？删除后不可恢复。',
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );
    
    await api.delete(`/api/dishes/${id}`);
    ElMessage.success('删除成功');
    loadMenu();
  } catch (error) {
    if (error !== 'cancel') {
      console.error("Failed to delete dish", error);
      ElMessage.error('删除失败');
    }
  }
};

// 重置表单
const resetForm = () => {
  isEditing.value = false;
  dishForm.value = {
    id: null,
    name: "",
    price: 0,
    description: "",
    imageUrl: ""
  };
  if (dishFormRef.value) {
    dishFormRef.value.clearValidate();
  }
};

// 组件加载时获取菜单数据
onMounted(loadMenu);
</script>

<style scoped>
.menu-card {
  max-width: 1200px;
  margin: 20px auto;
  padding: 20px;
  background-color: #ffffff;
  border-radius: 12px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-section h2 {
  margin: 0;
  color: #303133;
}

.header-actions {
  display: flex;
  align-items: center;
}

.menu-table {
  margin-top: 20px;
}

.dish-image {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  object-fit: cover;
  cursor: pointer;
}

.no-image {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 60px;
  height: 60px;
  background-color: #f5f7fa;
  border-radius: 8px;
  color: #909399;
  font-size: 12px;
}

.no-image .el-icon {
  font-size: 20px;
  margin-bottom: 4px;
}

.empty-search {
  text-align: center;
  padding: 40px 0;
}

.search-tip {
  color: #909399;
  font-size: 14px;
  margin-top: 10px;
}

.image-upload-section {
  text-align: center;
}

.dish-image-uploader {
  display: inline-block;
}

.dish-image-uploader :deep(.el-upload) {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
}

.dish-image-uploader :deep(.el-upload:hover) {
  border-color: #409eff;
}

.uploaded-image {
  width: 120px;
  height: 120px;
  display: block;
  object-fit: cover;
}

.upload-placeholder {
  width: 120px;
  height: 120px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #fafafa;
}

.upload-icon {
  font-size: 28px;
  color: #8c939d;
  margin-bottom: 8px;
}

.upload-text {
  color: #606266;
  font-size: 14px;
}

.upload-tips {
  margin-top: 10px;
  color: #909399;
  font-size: 12px;
}

.upload-tips p {
  margin: 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

:deep(.el-dialog__body) {
  padding: 20px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}
</style>