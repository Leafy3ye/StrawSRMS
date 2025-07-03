<template>
  <el-card class="menu-card">
    <h2>菜单管理</h2>

    <!-- 添加菜品表单 -->
    <el-form :model="newDish" label-width="120px" class="add-dish-form" style="margin-bottom: 20px;">
      <el-form-item label="菜品名称" required>
        <el-input v-model="newDish.name" placeholder="请输入菜品名称"></el-input>
      </el-form-item>

      <el-form-item label="价格" required>
        <el-input-number v-model="newDish.price" :min="0" placeholder="请输入菜品价格"></el-input-number>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="addDish">添加菜品</el-button>
      </el-form-item>
    </el-form>

    <!-- 菜品列表 -->
    <el-table :data="menuList" border style="width: 100%">
      <el-table-column prop="name" label="菜品名称" width="200" />
      <el-table-column prop="price" label="价格 (元)" width="150" />
      <el-table-column prop="updatedAt" label="修改时间" width="200">
        <template #default="scope">
          {{ formatDateTime(scope.row.updatedAt) }}
        </template>
      </el-table-column>

      <!-- 操作列 -->
      <el-table-column label="操作" align="center">
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
  </el-card>
</template>

<script setup>
import { ref, onMounted } from "vue";
import api from "../utils/api";

const menuList = ref([]);
const newDish = ref({
  name: "",
  price: 0,
  description: "",
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
  }
};

// 添加菜品
const addDish = async () => {
  try {
    await api.post("/api/dishes", newDish.value);
    loadMenu(); // 刷新菜单列表
    newDish.value = { name: "", price: 0, description: "" }; // 清空表单
  } catch (error) {
    console.error("Failed to add dish", error);
  }
};

// 编辑菜品
const editDish = async (dish) => {
  newDish.value = { ...dish };  // 将菜品数据填充到表单
  // 这里你可以添加编辑功能的具体实现，例如打开编辑对话框等
};

// 删除菜品
const deleteDish = async (id) => {
  try {
    await api.delete(`/api/dishes/${id}`);
    loadMenu(); // 刷新菜单列表
  } catch (error) {
    console.error("Failed to delete dish", error);
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

.add-dish-form {
  margin-bottom: 30px;
}

.el-table {
  margin-top: 20px;
}
</style>