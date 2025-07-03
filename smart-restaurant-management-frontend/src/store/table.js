import { defineStore } from 'pinia';

export const useTableStore = defineStore('tables', {
  state: () => ({
    tables: {
      1: [],
      2: [],
      3: [],
      4: [],
      5: [],
      6: [],
      7: [],
      8: [],
      9: [],
      10: [],
      11: [],
      12: [],
    }
  }),

  actions: {
    addOrderToTable(tableId, order) {
      if (!this.tables[tableId]) {
        this.tables[tableId] = [];
      }
      this.tables[tableId].push(order);
    },

    getOrdersByTableId(tableId) {
      return this.tables[tableId] || [];
    }
  }
});
