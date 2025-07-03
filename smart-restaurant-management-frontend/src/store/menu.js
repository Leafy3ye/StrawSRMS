import { defineStore } from 'pinia';

export const useMenuStore = defineStore('menu', {
  state: () => ({
    menuList: JSON.parse(localStorage.getItem('menu')) || []
  }),
  getters: {
    getMenuList: (state) => state.menuList
  },
  actions: {
    loadMenu() {
      const storedMenu = localStorage.getItem('menu');
      if (storedMenu) {
        this.menuList = JSON.parse(storedMenu);
      }
    },
    addDish(dish) {
      this.menuList.push(dish);
      this.saveMenu();
    },
    deleteDish(index) {
      this.menuList.splice(index, 1);
      this.saveMenu();
    },
    saveMenu() {
      localStorage.setItem('menu', JSON.stringify(this.menuList));
    }
  }
});
