import { createRouter, createWebHistory } from 'vue-router';
import { useUserStore } from '@/stores/user';
import NProgress from 'nprogress';
import 'nprogress/nprogress.css';

// 配置NProgress
NProgress.configure({ showSpinner: false });

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: {
      title: '登录',
      requiresAuth: false,
    },
  },
  {
    path: '/test-api',
    name: 'TestApi',
    component: () => import('@/views/test-api.vue'),
    meta: {
      title: 'API测试',
      requiresAuth: false,
    },
  },
  {
    path: '/',
    component: () => import('@/layout/index.vue'),
    redirect: '/dashboard',
    meta: {
      requiresAuth: true,
    },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: {
          title: '仪表盘',
          icon: 'DataBoard',
        },
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('@/views/users/index.vue'),
        meta: {
          title: '用户管理',
          icon: 'User',
        },
      },
      {
        path: 'products',
        name: 'Products',
        component: () => import('@/views/products/index.vue'),
        meta: {
          title: '商品管理',
          icon: 'Goods',
        },
      },
      {
        path: 'orders',
        name: 'Orders',
        component: () => import('@/views/orders/index.vue'),
        meta: {
          title: '订单管理',
          icon: 'List',
        },
      },
      {
        path: 'categories',
        name: 'Categories',
        component: () => import('@/views/categories/index.vue'),
        meta: {
          title: '分类管理',
          icon: 'Menu',
        },
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/settings/index.vue'),
        meta: {
          title: '系统设置',
          icon: 'Setting',
        },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/404.vue'),
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

// 路由守卫
router.beforeEach(async (to, from, next) => {
  NProgress.start();

  const userStore = useUserStore();
  const token = userStore.token;

  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 边墙鲜送管理系统`;
  }

  // 如果访问登录页面且已登录，重定向到首页
  if (to.path === '/login' && token) {
    next('/');
    return;
  }

  // 如果需要认证但未登录，重定向到登录页
  if (to.meta.requiresAuth && !token) {
    next('/login');
    return;
  }

  // 如果已登录但没有用户信息，获取用户信息
  if (token && !userStore.userInfo && to.meta.requiresAuth) {
    try {
      await userStore.getUserInfo();
    } catch (error) {
      console.error('获取用户信息失败:', error);
      // 如果获取用户信息失败，清除token并重定向到登录页
      userStore.resetState();
      next('/login');
      return;
    }
  }

  next();
});

router.afterEach(() => {
  NProgress.done();
});

export default router;
