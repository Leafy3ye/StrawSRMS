import { defineStore } from "pinia";

export const useCartStore = defineStore("cart", {
  state: () => ({
    carts: {}, // 使用一个对象保存每个桌位的购物车
  }),
  actions: {
    // 获取当前桌位的购物车
    getCart(tableId) {
      if (!this.carts[tableId]) {
        this.carts[tableId] = []; // 如果没有此桌位的购物车，初始化一个
      }
      return this.carts[tableId];
    },

    // 设置桌位的购物车
    setCart(tableId, orders) {
      this.carts[tableId] = orders; // 更新桌位购物车
    },

    // 添加商品到购物车
    addToCart(tableId, item) {
      const cart = this.getCart(tableId);
      // 为每个菜品生成唯一ID
      const uniqueId = Date.now() + Math.random().toString(36).substr(2, 9);
      cart.push({ 
        ...item, 
        uniqueId, // 添加唯一标识
        quantity: 1,
        remark: '', 
        completed: false
      });
      this.setCart(tableId, cart);  // 重新设置更新后的购物车
    },

    // 删除购物车中的商品
    removeFromCart(tableId, uniqueId) {
      const cart = this.getCart(tableId);
      const updatedCart = cart.filter((item) => item.uniqueId !== uniqueId);
      this.setCart(tableId, updatedCart); // 重新设置更新后的购物车
    },

    // 更新数量
    updateQuantity(tableId, uniqueId, amount) {
      const cart = this.getCart(tableId);
      const item = cart.find((item) => item.uniqueId === uniqueId);
      if (item) {
        item.quantity = Math.max(item.quantity + amount, 1);
        this.setCart(tableId, cart);  // 重新设置更新后的购物车
      }
    },

    // 标记菜品为已完成
    markAsCompleted(tableId, uniqueId) {
      const cart = this.getCart(tableId);
      const item = cart.find(item => item.uniqueId === uniqueId);
      if (item) {
        item.completed = true;
        this.setCart(tableId, cart);
        
        // 保存完成状态到localStorage
        const completedKey = `completed_${tableId}`;
        const completed = JSON.parse(localStorage.getItem(completedKey) || '[]');
        if (!completed.includes(uniqueId)) {
          completed.push(uniqueId);
          localStorage.setItem(completedKey, JSON.stringify(completed));
        }
      }
    },

    // 清空购物车
    clearCart(tableId) {
      this.setCart(tableId, []);  // 清空购物车
    }
  },
  getters: {
    // 获取指定桌位的总价
    totalPrice() {
      let total = 0;
      for (const tableId in this.carts) {
        total += this.carts[tableId].reduce((sum, item) => sum + item.price * item.quantity, 0);
      }
      return total.toFixed(2);
    },
  },
});
