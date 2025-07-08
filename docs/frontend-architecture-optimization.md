# 前端架构优化指南 - Vue 3 生态系统最佳实践

## 🎯 前端架构现状与优化目标

### 当前技术栈
✅ **Vue 3 + Composition API**：现代化响应式框架  
✅ **Vite**：快速构建工具  
✅ **Element Plus**：成熟的UI组件库  
✅ **Pinia**：新一代状态管理  

### 优化目标
🚀 **性能提升**：首屏加载时间 < 1.5s，交互响应 < 100ms  
🚀 **开发效率**：组件复用率 > 80%，开发周期缩短 50%  
🚀 **代码质量**：TypeScript 覆盖率 > 90%，测试覆盖率 > 80%  
🚀 **用户体验**：PWA 支持，离线可用，响应式设计  

## 📋 架构优化方案

### 1. TypeScript 全面集成

#### 1.1 严格类型配置

**tsconfig.json 优化**
```json
{
  "compilerOptions": {
    "target": "ES2020",
    "lib": ["ES2020", "DOM", "DOM.Iterable"],
    "module": "ESNext",
    "skipLibCheck": true,
    "moduleResolution": "bundler",
    "allowImportingTsExtensions": true,
    "resolveJsonModule": true,
    "isolatedModules": true,
    "noEmit": true,
    "jsx": "preserve",
    "strict": true,
    "noUnusedLocals": true,
    "noUnusedParameters": true,
    "noFallthroughCasesInSwitch": true,
    "baseUrl": ".",
    "paths": {
      "@/*": ["./src/*"],
      "@/components/*": ["./src/components/*"],
      "@/views/*": ["./src/views/*"],
      "@/stores/*": ["./src/stores/*"],
      "@/utils/*": ["./src/utils/*"],
      "@/types/*": ["./src/types/*"]
    },
    "types": ["vite/client", "element-plus/global"]
  },
  "include": [
    "src/**/*.ts",
    "src/**/*.tsx",
    "src/**/*.vue",
    "tests/**/*.ts",
    "tests/**/*.tsx"
  ],
  "exclude": ["node_modules", "dist"]
}
```

#### 1.2 类型定义体系

**API 响应类型**
```typescript
// src/types/api.ts
export interface ApiResponse<T = any> {
  success: boolean
  code: string
  message: string
  data: T
  timestamp: number
}

export interface PaginationParams {
  page: number
  size: number
  sort?: string
  order?: 'asc' | 'desc'
}

export interface PaginationResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
}

// 业务实体类型
export interface User {
  id: number
  username: string
  email: string
  phone: string
  avatar?: string
  status: UserStatus
  roles: Role[]
  createdAt: string
  updatedAt: string
}

export enum UserStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  BANNED = 'BANNED'
}

export interface Product {
  id: number
  name: string
  description: string
  price: number
  originalPrice?: number
  images: string[]
  category: Category
  stock: number
  status: ProductStatus
  tags: string[]
  specifications: Record<string, any>
  createdAt: string
  updatedAt: string
}

export interface Order {
  id: number
  orderNo: string
  user: User
  items: OrderItem[]
  totalAmount: number
  discountAmount: number
  finalAmount: number
  status: OrderStatus
  paymentMethod: PaymentMethod
  deliveryAddress: Address
  createdAt: string
  updatedAt: string
}
```

**组件 Props 类型**
```typescript
// src/types/components.ts
export interface TableColumn<T = any> {
  key: keyof T
  label: string
  width?: number | string
  minWidth?: number | string
  fixed?: 'left' | 'right'
  sortable?: boolean
  formatter?: (row: T, column: TableColumn<T>, cellValue: any) => string
  render?: (row: T) => VNode
}

export interface FormField {
  key: string
  label: string
  type: 'input' | 'select' | 'date' | 'textarea' | 'upload' | 'switch'
  required?: boolean
  placeholder?: string
  options?: Array<{ label: string; value: any }>
  rules?: FormItemRule[]
  props?: Record<string, any>
}

export interface ModalProps {
  visible: boolean
  title: string
  width?: string | number
  destroyOnClose?: boolean
  maskClosable?: boolean
  loading?: boolean
}
```

### 2. 组件架构优化

#### 2.1 原子化设计系统

**基础原子组件**
```vue
<!-- src/components/atoms/BaseButton.vue -->
<template>
  <el-button
    :type="computedType"
    :size="size"
    :loading="loading"
    :disabled="disabled"
    :icon="icon"
    @click="handleClick"
    v-bind="$attrs"
  >
    <slot />
  </el-button>
</template>

<script setup lang="ts">
import type { ButtonType, ButtonSize } from 'element-plus'

interface Props {
  type?: ButtonType | 'gradient' | 'outline'
  size?: ButtonSize
  loading?: boolean
  disabled?: boolean
  icon?: string
}

interface Emits {
  click: [event: MouseEvent]
}

const props = withDefaults(defineProps<Props>(), {
  type: 'default',
  size: 'default',
  loading: false,
  disabled: false
})

const emit = defineEmits<Emits>()

const computedType = computed(() => {
  if (props.type === 'gradient' || props.type === 'outline') {
    return 'primary'
  }
  return props.type
})

const handleClick = (event: MouseEvent) => {
  if (!props.loading && !props.disabled) {
    emit('click', event)
  }
}
</script>

<style scoped>
.el-button.gradient {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
}

.el-button.outline {
  background: transparent;
  border: 2px solid var(--el-color-primary);
  color: var(--el-color-primary);
}

.el-button.outline:hover {
  background: var(--el-color-primary);
  color: white;
}
</style>
```

**复合分子组件**
```vue
<!-- src/components/molecules/SearchForm.vue -->
<template>
  <el-form
    ref="formRef"
    :model="formData"
    :inline="inline"
    class="search-form"
    @submit.prevent="handleSearch"
  >
    <el-form-item
      v-for="field in fields"
      :key="field.key"
      :label="field.label"
      :prop="field.key"
    >
      <component
        :is="getFieldComponent(field.type)"
        v-model="formData[field.key]"
        v-bind="field.props"
        :placeholder="field.placeholder"
        @keyup.enter="handleSearch"
      >
        <template v-if="field.type === 'select'">
          <el-option
            v-for="option in field.options"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </template>
      </component>
    </el-form-item>
    
    <el-form-item>
      <base-button type="primary" @click="handleSearch">
        <el-icon><Search /></el-icon>
        搜索
      </base-button>
      <base-button @click="handleReset">
        <el-icon><Refresh /></el-icon>
        重置
      </base-button>
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts" generic="T extends Record<string, any>">
import { Search, Refresh } from '@element-plus/icons-vue'
import type { FormInstance } from 'element-plus'
import type { FormField } from '@/types/components'

interface Props {
  fields: FormField[]
  modelValue: T
  inline?: boolean
}

interface Emits {
  'update:modelValue': [value: T]
  search: [params: T]
  reset: []
}

const props = withDefaults(defineProps<Props>(), {
  inline: true
})

const emit = defineEmits<Emits>()

const formRef = ref<FormInstance>()
const formData = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const getFieldComponent = (type: string) => {
  const componentMap = {
    input: 'el-input',
    select: 'el-select',
    date: 'el-date-picker',
    textarea: 'el-input'
  }
  return componentMap[type] || 'el-input'
}

const handleSearch = () => {
  emit('search', formData.value)
}

const handleReset = () => {
  formRef.value?.resetFields()
  emit('reset')
}
</script>
```

#### 2.2 高阶组件模式

**表格增强组件**
```vue
<!-- src/components/organisms/EnhancedTable.vue -->
<template>
  <div class="enhanced-table">
    <!-- 搜索表单 -->
    <search-form
      v-if="searchFields.length > 0"
      v-model="searchParams"
      :fields="searchFields"
      @search="handleSearch"
      @reset="handleReset"
    />
    
    <!-- 操作栏 -->
    <div v-if="$slots.actions" class="table-actions">
      <slot name="actions" :selected="selectedRows" />
    </div>
    
    <!-- 数据表格 -->
    <el-table
      ref="tableRef"
      v-loading="loading"
      :data="data"
      :height="height"
      @selection-change="handleSelectionChange"
      @sort-change="handleSortChange"
    >
      <el-table-column
        v-if="selectable"
        type="selection"
        width="55"
      />
      
      <el-table-column
        v-for="column in columns"
        :key="column.key"
        :prop="column.key"
        :label="column.label"
        :width="column.width"
        :min-width="column.minWidth"
        :fixed="column.fixed"
        :sortable="column.sortable ? 'custom' : false"
      >
        <template #default="{ row, column: tableColumn, $index }">
          <component
            v-if="column.render"
            :is="column.render(row, $index)"
          />
          <span v-else-if="column.formatter">
            {{ column.formatter(row, column, row[column.key]) }}
          </span>
          <span v-else>
            {{ row[column.key] }}
          </span>
        </template>
      </el-table-column>
      
      <el-table-column
        v-if="$slots.actions"
        label="操作"
        fixed="right"
        width="200"
      >
        <template #default="{ row, $index }">
          <slot name="rowActions" :row="row" :index="$index" />
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 分页 -->
    <el-pagination
      v-if="pagination"
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :total="total"
      :page-sizes="[10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="handleSizeChange"
      @current-change="handlePageChange"
    />
  </div>
</template>

<script setup lang="ts" generic="T extends Record<string, any>">
import type { TableInstance } from 'element-plus'
import type { TableColumn, FormField } from '@/types/components'

interface Props {
  data: T[]
  columns: TableColumn<T>[]
  loading?: boolean
  height?: string | number
  selectable?: boolean
  pagination?: boolean
  searchFields?: FormField[]
  total?: number
}

interface Emits {
  search: [params: Record<string, any>]
  'page-change': [page: number]
  'size-change': [size: number]
  'sort-change': [sort: { prop: string; order: string }]
  'selection-change': [selection: T[]]
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
  selectable: false,
  pagination: true,
  searchFields: () => [],
  total: 0
})

const emit = defineEmits<Emits>()

const tableRef = ref<TableInstance>()
const selectedRows = ref<T[]>([])
const searchParams = ref<Record<string, any>>({})
const currentPage = ref(1)
const pageSize = ref(20)

const handleSearch = (params: Record<string, any>) => {
  currentPage.value = 1
  emit('search', params)
}

const handleReset = () => {
  searchParams.value = {}
  currentPage.value = 1
  emit('search', {})
}

const handleSelectionChange = (selection: T[]) => {
  selectedRows.value = selection
  emit('selection-change', selection)
}

const handleSortChange = ({ prop, order }: { prop: string; order: string }) => {
  emit('sort-change', { prop, order })
}

const handlePageChange = (page: number) => {
  emit('page-change', page)
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  emit('size-change', size)
}
</script>
```

### 3. 状态管理优化

#### 3.1 Pinia Store 模块化

**用户状态管理**
```typescript
// src/stores/modules/user.ts
import { defineStore } from 'pinia'
import type { User, LoginRequest, LoginResponse } from '@/types/api'
import { userApi } from '@/api/user'
import { storage } from '@/utils/storage'
import { TOKEN_KEY, USER_INFO_KEY } from '@/constants/storage'

interface UserState {
  token: string | null
  userInfo: User | null
  permissions: string[]
  roles: string[]
  isLoggedIn: boolean
}

export const useUserStore = defineStore('user', {
  state: (): UserState => ({
    token: storage.get(TOKEN_KEY),
    userInfo: storage.get(USER_INFO_KEY),
    permissions: [],
    roles: [],
    isLoggedIn: false
  }),
  
  getters: {
    isAdmin: (state) => state.roles.includes('ADMIN'),
    hasPermission: (state) => (permission: string) => 
      state.permissions.includes(permission),
    hasRole: (state) => (role: string) => 
      state.roles.includes(role),
    displayName: (state) => 
      state.userInfo?.username || '未知用户'
  },
  
  actions: {
    async login(loginData: LoginRequest): Promise<void> {
      try {
        const response = await userApi.login(loginData)
        const { token, user } = response.data
        
        this.setToken(token)
        this.setUserInfo(user)
        this.isLoggedIn = true
        
        // 获取用户权限
        await this.fetchUserPermissions()
      } catch (error) {
        this.logout()
        throw error
      }
    },
    
    async logout(): Promise<void> {
      try {
        if (this.token) {
          await userApi.logout()
        }
      } finally {
        this.clearUserData()
      }
    },
    
    async fetchUserInfo(): Promise<void> {
      if (!this.token) return
      
      try {
        const response = await userApi.getCurrentUser()
        this.setUserInfo(response.data)
      } catch (error) {
        this.logout()
        throw error
      }
    },
    
    async fetchUserPermissions(): Promise<void> {
      if (!this.token) return
      
      try {
        const response = await userApi.getUserPermissions()
        this.permissions = response.data.permissions
        this.roles = response.data.roles
      } catch (error) {
        console.error('获取用户权限失败:', error)
      }
    },
    
    setToken(token: string): void {
      this.token = token
      storage.set(TOKEN_KEY, token)
    },
    
    setUserInfo(userInfo: User): void {
      this.userInfo = userInfo
      storage.set(USER_INFO_KEY, userInfo)
    },
    
    clearUserData(): void {
      this.token = null
      this.userInfo = null
      this.permissions = []
      this.roles = []
      this.isLoggedIn = false
      
      storage.remove(TOKEN_KEY)
      storage.remove(USER_INFO_KEY)
    },
    
    async refreshToken(): Promise<void> {
      try {
        const response = await userApi.refreshToken()
        this.setToken(response.data.token)
      } catch (error) {
        this.logout()
        throw error
      }
    }
  }
})
```

**应用全局状态**
```typescript
// src/stores/modules/app.ts
import { defineStore } from 'pinia'
import type { RouteLocationNormalized } from 'vue-router'

interface AppState {
  collapsed: boolean
  device: 'desktop' | 'tablet' | 'mobile'
  theme: 'light' | 'dark' | 'auto'
  language: string
  loading: boolean
  visitedViews: RouteLocationNormalized[]
  cachedViews: string[]
}

export const useAppStore = defineStore('app', {
  state: (): AppState => ({
    collapsed: false,
    device: 'desktop',
    theme: 'light',
    language: 'zh-CN',
    loading: false,
    visitedViews: [],
    cachedViews: []
  }),
  
  getters: {
    isDark: (state) => state.theme === 'dark',
    isMobile: (state) => state.device === 'mobile'
  },
  
  actions: {
    toggleCollapsed(): void {
      this.collapsed = !this.collapsed
    },
    
    setDevice(device: AppState['device']): void {
      this.device = device
    },
    
    setTheme(theme: AppState['theme']): void {
      this.theme = theme
      document.documentElement.setAttribute('data-theme', theme)
    },
    
    setLanguage(language: string): void {
      this.language = language
    },
    
    setLoading(loading: boolean): void {
      this.loading = loading
    },
    
    addVisitedView(view: RouteLocationNormalized): void {
      if (this.visitedViews.some(v => v.path === view.path)) return
      
      this.visitedViews.push({
        ...view,
        title: view.meta?.title || 'Unknown'
      })
    },
    
    removeVisitedView(view: RouteLocationNormalized): void {
      const index = this.visitedViews.findIndex(v => v.path === view.path)
      if (index > -1) {
        this.visitedViews.splice(index, 1)
      }
    },
    
    addCachedView(viewName: string): void {
      if (!this.cachedViews.includes(viewName)) {
        this.cachedViews.push(viewName)
      }
    },
    
    removeCachedView(viewName: string): void {
      const index = this.cachedViews.indexOf(viewName)
      if (index > -1) {
        this.cachedViews.splice(index, 1)
      }
    }
  },
  
  persist: {
    key: 'app-store',
    storage: localStorage,
    paths: ['collapsed', 'theme', 'language']
  }
})
```

### 4. 路由和权限管理

#### 4.1 动态路由配置

**路由配置**
```typescript
// src/router/routes.ts
import type { RouteRecordRaw } from 'vue-router'

export const constantRoutes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: {
      title: '登录',
      hidden: true,
      requiresAuth: false
    }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/layout/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: {
          title: '仪表盘',
          icon: 'dashboard',
          requiresAuth: true
        }
      }
    ]
  }
]

export const asyncRoutes: RouteRecordRaw[] = [
  {
    path: '/users',
    name: 'UserManagement',
    component: () => import('@/layout/index.vue'),
    meta: {
      title: '用户管理',
      icon: 'user',
      requiresAuth: true,
      permissions: ['user:read']
    },
    children: [
      {
        path: '',
        name: 'UserList',
        component: () => import('@/views/user/UserList.vue'),
        meta: {
          title: '用户列表',
          permissions: ['user:read']
        }
      },
      {
        path: 'create',
        name: 'UserCreate',
        component: () => import('@/views/user/UserForm.vue'),
        meta: {
          title: '创建用户',
          permissions: ['user:create']
        }
      }
    ]
  },
  {
    path: '/products',
    name: 'ProductManagement',
    component: () => import('@/layout/index.vue'),
    meta: {
      title: '商品管理',
      icon: 'product',
      requiresAuth: true,
      permissions: ['product:read']
    },
    children: [
      {
        path: '',
        name: 'ProductList',
        component: () => import('@/views/product/ProductList.vue'),
        meta: {
          title: '商品列表',
          permissions: ['product:read']
        }
      }
    ]
  }
]
```

**权限路由守卫**
```typescript
// src/router/guards.ts
import type { Router } from 'vue-router'
import { useUserStore } from '@/stores/modules/user'
import { useAppStore } from '@/stores/modules/app'
import { ElMessage } from 'element-plus'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

NProgress.configure({ showSpinner: false })

export function setupRouterGuards(router: Router) {
  // 全局前置守卫
  router.beforeEach(async (to, from, next) => {
    NProgress.start()
    
    const userStore = useUserStore()
    const appStore = useAppStore()
    
    // 设置页面标题
    document.title = to.meta?.title 
      ? `${to.meta.title} - 边墙鲜送管理系统` 
      : '边墙鲜送管理系统'
    
    // 添加访问记录
    if (to.meta?.title) {
      appStore.addVisitedView(to)
    }
    
    // 检查是否需要认证
    if (to.meta?.requiresAuth !== false) {
      if (!userStore.token) {
        ElMessage.warning('请先登录')
        next({ name: 'Login', query: { redirect: to.fullPath } })
        return
      }
      
      // 检查用户信息
      if (!userStore.userInfo) {
        try {
          await userStore.fetchUserInfo()
          await userStore.fetchUserPermissions()
        } catch (error) {
          ElMessage.error('获取用户信息失败')
          next({ name: 'Login' })
          return
        }
      }
      
      // 检查权限
      if (to.meta?.permissions) {
        const hasPermission = (to.meta.permissions as string[]).some(
          permission => userStore.hasPermission(permission)
        )
        
        if (!hasPermission) {
          ElMessage.error('权限不足')
          next({ name: 'Dashboard' })
          return
        }
      }
    }
    
    // 已登录用户访问登录页，重定向到首页
    if (to.name === 'Login' && userStore.token) {
      next({ name: 'Dashboard' })
      return
    }
    
    next()
  })
  
  // 全局后置守卫
  router.afterEach((to) => {
    NProgress.done()
    
    // 添加缓存视图
    if (to.meta?.keepAlive && to.name) {
      const appStore = useAppStore()
      appStore.addCachedView(to.name as string)
    }
  })
  
  // 路由错误处理
  router.onError((error) => {
    console.error('路由错误:', error)
    ElMessage.error('页面加载失败')
    NProgress.done()
  })
}
```

### 5. 性能优化策略

#### 5.1 代码分割和懒加载

**Vite 配置优化**
```typescript
// vite.config.ts
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'
import { visualizer } from 'rollup-plugin-visualizer'
import { createSvgIconsPlugin } from 'vite-plugin-svg-icons'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

export default defineConfig({
  plugins: [
    vue(),
    
    // 自动导入
    AutoImport({
      imports: [
        'vue',
        'vue-router',
        'pinia',
        {
          '@/utils/request': ['request'],
          '@/utils/storage': ['storage'],
          '@/hooks/useTable': ['useTable'],
          '@/hooks/useForm': ['useForm']
        }
      ],
      resolvers: [ElementPlusResolver()],
      dts: true
    }),
    
    // 组件自动导入
    Components({
      resolvers: [ElementPlusResolver()],
      dts: true
    }),
    
    // SVG 图标
    createSvgIconsPlugin({
      iconDirs: [resolve(process.cwd(), 'src/assets/icons')],
      symbolId: 'icon-[dir]-[name]'
    }),
    
    // 打包分析
    visualizer({
      filename: 'dist/stats.html',
      open: true,
      gzipSize: true
    })
  ],
  
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  
  build: {
    target: 'es2015',
    cssTarget: 'chrome80',
    rollupOptions: {
      output: {
        manualChunks: {
          // 第三方库分包
          'element-plus': ['element-plus'],
          'vue-vendor': ['vue', 'vue-router', 'pinia'],
          'utils': [
            'axios',
            'dayjs',
            'lodash-es'
          ]
        },
        chunkFileNames: 'js/[name]-[hash].js',
        entryFileNames: 'js/[name]-[hash].js',
        assetFileNames: (assetInfo) => {
          const info = assetInfo.name.split('.')
          const extType = info[info.length - 1]
          if (/\.(mp4|webm|ogg|mp3|wav|flac|aac)(\?.*)?$/i.test(assetInfo.name)) {
            return `media/[name]-[hash].${extType}`
          }
          if (/\.(png|jpe?g|gif|svg)(\?.*)?$/.test(assetInfo.name)) {
            return `images/[name]-[hash].${extType}`
          }
          if (/\.(woff2?|eot|ttf|otf)(\?.*)?$/i.test(assetInfo.name)) {
            return `fonts/[name]-[hash].${extType}`
          }
          return `assets/[name]-[hash].${extType}`
        }
      }
    },
    
    // 压缩配置
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: true,
        drop_debugger: true
      }
    }
  },
  
  server: {
    port: 3000,
    open: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  }
})
```

#### 5.2 图片优化和懒加载

**图片懒加载指令**
```typescript
// src/directives/lazy.ts
import type { Directive, DirectiveBinding } from 'vue'

interface LazyElement extends HTMLElement {
  _lazy?: {
    observer: IntersectionObserver
    src: string
    loading: string
    error: string
  }
}

const lazy: Directive = {
  mounted(el: LazyElement, binding: DirectiveBinding) {
    const { value } = binding
    const src = typeof value === 'string' ? value : value.src
    const loading = value.loading || '/images/loading.gif'
    const error = value.error || '/images/error.png'
    
    // 设置默认加载图片
    el.src = loading
    
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            const img = entry.target as HTMLImageElement
            const tempImg = new Image()
            
            tempImg.onload = () => {
              img.src = src
              img.classList.add('lazy-loaded')
            }
            
            tempImg.onerror = () => {
              img.src = error
              img.classList.add('lazy-error')
            }
            
            tempImg.src = src
            observer.unobserve(img)
          }
        })
      },
      {
        rootMargin: '50px'
      }
    )
    
    observer.observe(el)
    
    el._lazy = {
      observer,
      src,
      loading,
      error
    }
  },
  
  updated(el: LazyElement, binding: DirectiveBinding) {
    const { value } = binding
    const newSrc = typeof value === 'string' ? value : value.src
    
    if (el._lazy && el._lazy.src !== newSrc) {
      el._lazy.observer.unobserve(el)
      el._lazy.src = newSrc
      el._lazy.observer.observe(el)
    }
  },
  
  unmounted(el: LazyElement) {
    if (el._lazy) {
      el._lazy.observer.unobserve(el)
      el._lazy.observer.disconnect()
      delete el._lazy
    }
  }
}

export default lazy
```

### 6. 测试策略

#### 6.1 单元测试配置

**Vitest 配置**
```typescript
// vitest.config.ts
import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: ['./tests/setup.ts'],
    coverage: {
      provider: 'v8',
      reporter: ['text', 'json', 'html'],
      exclude: [
        'node_modules/',
        'tests/',
        '**/*.d.ts',
        '**/*.config.*',
        'dist/'
      ]
    }
  },
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  }
})
```

**组件测试示例**
```typescript
// tests/components/BaseButton.spec.ts
import { mount } from '@vue/test-utils'
import { describe, it, expect, vi } from 'vitest'
import BaseButton from '@/components/atoms/BaseButton.vue'

describe('BaseButton', () => {
  it('渲染正确的文本', () => {
    const wrapper = mount(BaseButton, {
      slots: {
        default: '点击我'
      }
    })
    
    expect(wrapper.text()).toBe('点击我')
  })
  
  it('应用正确的类型样式', () => {
    const wrapper = mount(BaseButton, {
      props: {
        type: 'primary'
      }
    })
    
    expect(wrapper.find('.el-button--primary').exists()).toBe(true)
  })
  
  it('禁用状态下不触发点击事件', async () => {
    const handleClick = vi.fn()
    const wrapper = mount(BaseButton, {
      props: {
        disabled: true
      },
      attrs: {
        onClick: handleClick
      }
    })
    
    await wrapper.trigger('click')
    expect(handleClick).not.toHaveBeenCalled()
  })
  
  it('加载状态显示正确', () => {
    const wrapper = mount(BaseButton, {
      props: {
        loading: true
      }
    })
    
    expect(wrapper.find('.el-button').classes()).toContain('is-loading')
  })
})
```

#### 6.2 E2E 测试

**Playwright 配置**
```typescript
// playwright.config.ts
import { defineConfig, devices } from '@playwright/test'

export default defineConfig({
  testDir: './tests/e2e',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  reporter: 'html',
  use: {
    baseURL: 'http://localhost:3000',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure'
  },
  
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] }
    },
    {
      name: 'firefox',
      use: { ...devices['Desktop Firefox'] }
    },
    {
      name: 'webkit',
      use: { ...devices['Desktop Safari'] }
    },
    {
      name: 'Mobile Chrome',
      use: { ...devices['Pixel 5'] }
    }
  ],
  
  webServer: {
    command: 'npm run dev',
    url: 'http://localhost:3000',
    reuseExistingServer: !process.env.CI
  }
})
```

**E2E 测试示例**
```typescript
// tests/e2e/login.spec.ts
import { test, expect } from '@playwright/test'

test.describe('登录功能', () => {
  test('成功登录', async ({ page }) => {
    await page.goto('/login')
    
    // 填写登录表单
    await page.fill('[data-testid="username"]', 'admin')
    await page.fill('[data-testid="password"]', 'admin123')
    
    // 点击登录按钮
    await page.click('[data-testid="login-button"]')
    
    // 验证跳转到仪表盘
    await expect(page).toHaveURL('/dashboard')
    await expect(page.locator('[data-testid="user-avatar"]')).toBeVisible()
  })
  
  test('登录失败显示错误信息', async ({ page }) => {
    await page.goto('/login')
    
    await page.fill('[data-testid="username"]', 'admin')
    await page.fill('[data-testid="password"]', 'wrongpassword')
    await page.click('[data-testid="login-button"]')
    
    // 验证错误信息
    await expect(page.locator('.el-message--error')).toBeVisible()
    await expect(page.locator('.el-message--error')).toContainText('用户名或密码错误')
  })
})
```

## 🎯 实施计划和里程碑

### 第1周：TypeScript 集成
- [ ] 配置严格的 TypeScript 环境
- [ ] 定义完整的类型系统
- [ ] 重构现有组件为 TypeScript

### 第2周：组件架构优化
- [ ] 实现原子化设计系统
- [ ] 创建可复用的基础组件
- [ ] 开发高阶组件模式

### 第3周：状态管理和路由
- [ ] 优化 Pinia Store 结构
- [ ] 实现动态路由和权限控制
- [ ] 添加路由守卫和缓存机制

### 第4周：性能优化和测试
- [ ] 配置代码分割和懒加载
- [ ] 实现图片优化策略
- [ ] 建立完整的测试体系

---

**立即开始**：选择 TypeScript 集成作为第一步，为整个前端架构优化奠定坚实的类型安全基础！