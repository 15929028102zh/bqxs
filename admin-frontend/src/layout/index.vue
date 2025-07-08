<template>
  <div class="app-wrapper">
    <!-- 侧边栏 -->
    <div class="sidebar-container" :class="{ 'sidebar-collapse': isCollapse }">
      <div class="sidebar-logo">
        <img src="/logo.png" alt="Logo" class="logo" v-if="!isCollapse" />
        <span class="title" v-if="!isCollapse">边墙鲜送</span>
        <img src="/logo.png" alt="Logo" class="logo-mini" v-else />
      </div>

      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :unique-opened="true"
        :collapse-transition="false"
        mode="vertical"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
        router
      >
        <template v-for="route in routes" :key="route.path">
          <el-menu-item
            v-if="!route.children || route.children.length === 1"
            :index="route.path === '/' ? route.children[0].path : route.path"
          >
            <el-icon
              ><component
                :is="
                  route.children ? route.children[0].meta.icon : route.meta.icon
                "
            /></el-icon>
            <template #title>{{
              route.children ? route.children[0].meta.title : route.meta.title
            }}</template>
          </el-menu-item>

          <el-sub-menu v-else :index="route.path">
            <template #title>
              <el-icon><component :is="route.meta.icon" /></el-icon>
              <span>{{ route.meta.title }}</span>
            </template>
            <el-menu-item
              v-for="child in route.children"
              :key="child.path"
              :index="child.path"
            >
              <el-icon><component :is="child.meta.icon" /></el-icon>
              <template #title>{{ child.meta.title }}</template>
            </el-menu-item>
          </el-sub-menu>
        </template>
      </el-menu>
    </div>

    <!-- 主内容区 -->
    <div class="main-container">
      <!-- 顶部导航 -->
      <div class="navbar">
        <div class="navbar-left">
          <el-button type="text" @click="toggleSidebar" class="sidebar-toggle">
            <el-icon><Expand v-if="isCollapse" /><Fold v-else /></el-icon>
          </el-button>

          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentRoute.meta.title">{{
              currentRoute.meta.title
            }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="navbar-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" :src="userStore.userAvatar">
                <el-icon><User /></el-icon>
              </el-avatar>
              <span class="username">{{ userStore.userName || '管理员' }}</span>
              <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="settings">设置</el-dropdown-item>
                <el-dropdown-item divided command="logout"
                  >退出登录</el-dropdown-item
                >
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <!-- 页面内容 -->
      <div class="app-main">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import { ElMessageBox, ElMessage } from 'element-plus';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

// 侧边栏折叠状态
const isCollapse = ref(false);

// 当前路由
const currentRoute = computed(() => route);

// 当前激活的菜单
const activeMenu = computed(() => {
  const { path } = route;
  return path;
});

// 菜单路由
const routes = computed(() => {
  return (
    router.getRoutes().filter((route) => {
      return route.path === '/' && route.children && route.children.length > 0;
    })[0]?.children || []
  );
});

// 切换侧边栏
const toggleSidebar = () => {
  isCollapse.value = !isCollapse.value;
};

// 处理用户下拉菜单命令
const handleCommand = async (command) => {
  switch (command) {
    case 'profile':
      ElMessage.info('个人中心功能开发中...');
      break;
    case 'settings':
      router.push('/settings');
      break;
    case 'logout':
      try {
        await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        });
        await userStore.logout();
        router.push('/login');
        ElMessage.success('退出登录成功');
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('退出登录失败');
        }
      }
      break;
  }
};
</script>

<style lang="scss" scoped>
.app-wrapper {
  display: flex;
  height: 100vh;
  width: 100%;
}

.sidebar-container {
  width: 210px;
  height: 100%;
  background-color: #304156;
  transition: width 0.28s;

  &.sidebar-collapse {
    width: 64px;
  }

  .sidebar-logo {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 60px;
    background-color: #2b2f3a;

    .logo {
      width: 32px;
      height: 32px;
      margin-right: 8px;
    }

    .logo-mini {
      width: 32px;
      height: 32px;
    }

    .title {
      color: #fff;
      font-size: 16px;
      font-weight: 600;
    }
  }

  .el-menu {
    border-right: none;
    height: calc(100% - 60px);
    overflow-y: auto;
  }
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 60px;
  padding: 0 20px;
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);

  .navbar-left {
    display: flex;
    align-items: center;

    .sidebar-toggle {
      font-size: 18px;
      margin-right: 20px;
    }

    .breadcrumb {
      font-size: 14px;
    }
  }

  .navbar-right {
    .user-info {
      display: flex;
      align-items: center;
      cursor: pointer;

      .username {
        margin: 0 8px;
        font-size: 14px;
      }
    }
  }
}

.app-main {
  flex: 1;
  padding: 20px;
  background-color: #f0f2f5;
  overflow-y: auto;
}

// 过渡动画
.fade-transform-leave-active,
.fade-transform-enter-active {
  transition: all 0.5s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style>
