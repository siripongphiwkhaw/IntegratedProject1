import { useUsers } from '../stores/users'
import { createRouter, createWebHistory } from 'vue-router'
import { useUser } from '../stores/user'
import { Roles } from '../utils/roles'

const history = createWebHistory(import.meta.env.VITE_BASE_PATH)
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/VLogin.vue'),
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/VRegister.vue'),
  },
  {
    path: '/',
    name: 'Schedules',
    alias: '/schedules',
    meta: { requireAuth: true },
    component: () => import('../views/VSchedules.vue'),
  },
  {
    path: '/categories',
    name: 'Categories',
    meta: { requireAuth: true },
    component: () => import('../views/VCategories.vue'),
  },
  {
    path: '/categories/:categoryId',
    name: 'EditCategory',
    meta: { requireAuth: true, authorities: [Roles.ADMIN] },
    component: () => import('../views/VEditCategory.vue'),
  },
  {
    path: '/users',
    name: 'Users',
    meta: { requireAuth: true, authorities: [Roles.ADMIN] },
    component: () => import('../views/VUsers.vue'),
  },
  {
    path: '/users/:userId/edit',
    name: 'EditUser',
    props: true,
    meta: { requireAuth: true, authorities: [Roles.ADMIN] },
    component: () => import('../views/VEditUser.vue'),
    beforeEnter: async (to, from, next) => {
      const userId = parseInt(to.params.userId)

      if (isNaN(userId)) {
        return next('/not-found')
      }

      const userStore = useUsers()

      try {
        const user = await userStore.fetchUserByUserId(to.params.userId)
        to.params.user = user
        next()
      } catch (err) {
        return next('/not-found')
      }
    },
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    alias: '/not-found',
    component: () => import('../views/VNotFound.vue'),
  },
]

const router = createRouter({ history, routes })

router.beforeEach(async (to, from, next) => {
  const userStore = useUser()

  if (['Login', 'Register'].includes(to.name) && userStore.getToken()) {
    return next('/')
  }

  if (to.meta.requireAuth && (!userStore.isAuthenticated || !userStore.getToken())) {
    try {
      await userStore.loadUser()
    } catch (err) {
      if (err.response.status !== 200) {
        if (!userStore.getRefreshToken()) {
          return next('/login')
        }

        const res = await userStore.refreshToken()
        if (res.status !== 200) {
          userStore.logout()
          return next('/login')
        }
        await userStore.loadUser()
      }
    }
  }

  if (to.meta.authorities && !to.meta.authorities.includes(userStore.user.role.name.toUpperCase())) {
    return next('/')
  }

  next()
})
export default router
