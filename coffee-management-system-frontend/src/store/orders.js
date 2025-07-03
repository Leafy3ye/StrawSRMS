import { defineStore } from 'pinia';

export const useOrderStore = defineStore('orders', {
  state: () => ({
    // 模拟的订单数据
    orders: [
      {
        id: 1,
        date: '2025-05-01',
        amount: 200.50,
      },
      {
        id: 2,
        date: '2025-05-01',
        amount: 150.75,
      },
      {
        id: 3,
        date: '2025-05-02',
        amount: 120.30,
      },
      {
        id: 4,
        date: '2025-05-02',
        amount: 300.40,
      },
      {
        id: 5,
        date: '2025-05-02',
        amount: 180.60,
      },
    ],
  }),
  getters: {
    // 你可以创建计算属性来方便访问状态
    getTotalRevenue: (state) => {
      return state.orders.reduce((sum, order) => sum + order.amount, 0).toFixed(2);
    },
    getTotalOrders: (state) => {
      return state.orders.length;
    },
  },
  actions: {
    // 假设有添加新订单的方法
    addOrder(order) {
      this.orders.push(order);
    },
    // 可以用来模拟从后端获取订单数据的异步方法
    async fetchOrders() {
      // 模拟异步获取订单
      setTimeout(() => {
        this.orders = [
          ...this.orders,
          {
            id: 6,
            date: '2025-05-03',
            amount: 250.00,
          },
        ];
      }, 1000);
    },
  },
});
