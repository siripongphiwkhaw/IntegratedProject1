import { createRouter, createWebHistory } from 'vue-router'

const history = createWebHistory(import.meta.env.VITE_BASE_PATH)
const routes = [
  {
    path: '/',
    name: 'Schedules',
    alias: '/schedules',
    component: () => import('../views/VSchedules.vue'),
  },
  {
    path: '/categories',
    name: 'Categories',
    component: () => import('../views/VCategories.vue'),
  },
  {
    path: '/categories/:categoryId',
    name: 'EditCategory',
    component: () => import('../views/VEditCategory.vue'),
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    alias: '/not-found',
    component: () => import('../views/VNotFound.vue'),
  },
]

const router = createRouter({ history, routes })
export default router
