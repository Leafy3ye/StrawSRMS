// 油耗记录存储
import { defineStore } from "pinia";
import { useUserStore } from "./user"; // 引入用户存储
import data from "../assets/data.json"; // 引入 data.json 演示数据

export const useRecordStore = defineStore("records", {
  state: () => ({
    records: [], // 当前用户的油耗记录
  }),

  actions: {
    // 初始化数据
    loadRecords() {
      const userStore = useUserStore();
      if (userStore.user) {
        const username = userStore.user.username;

        // 如果是 admin 用户，加载演示数据
        if (username === "admin") {
          this.records = JSON.parse(localStorage.getItem(`records_admin`)) || data.records;
        } else {
          // 对于其他用户，加载用户自己的记录
          const storedRecords = localStorage.getItem(`records_${username}`);
          this.records = storedRecords ? JSON.parse(storedRecords) : [];
        }
      }
    },

    // 添加记录
    addRecord(record) {
      this.records.push(record); // 添加新记录
      this.saveRecords(); // 保存数据
    },

    // 保存当前用户的数据到 localStorage
    saveRecords() {
      const userStore = useUserStore();
      if (userStore.user) {
        const username = userStore.user.username;

        // 保存数据到 localStorage
        localStorage.setItem(`records_${username}`, JSON.stringify(this.records));
      }
    },

    // 清空当前用户的记录
    clearRecords() {
      this.records = [];
      this.saveRecords();
    },
  },
});
