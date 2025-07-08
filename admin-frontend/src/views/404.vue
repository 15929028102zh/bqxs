<template>
  <div class="error-page">
    <div class="error-container">
      <div class="error-content">
        <div class="error-image">
          <svg viewBox="0 0 404 300" class="error-svg">
            <!-- 404数字 -->
            <text
              x="50%"
              y="50%"
              text-anchor="middle"
              dominant-baseline="middle"
              class="error-text"
            >
              404
            </text>

            <!-- 装饰元素 -->
            <circle cx="100" cy="80" r="8" class="decoration-circle" />
            <circle cx="320" cy="220" r="6" class="decoration-circle" />
            <circle cx="80" cy="200" r="4" class="decoration-circle" />
            <circle cx="350" cy="100" r="5" class="decoration-circle" />

            <!-- 波浪线 -->
            <path
              d="M50 250 Q100 230 150 250 T250 250"
              stroke="#e0e6ed"
              stroke-width="2"
              fill="none"
              class="wave-line"
            />
            <path
              d="M200 80 Q250 60 300 80 T400 80"
              stroke="#e0e6ed"
              stroke-width="2"
              fill="none"
              class="wave-line"
            />
          </svg>
        </div>

        <div class="error-info">
          <h1 class="error-title">页面未找到</h1>
          <p class="error-description">抱歉，您访问的页面不存在或已被移除。</p>
          <p class="error-suggestion">
            请检查URL是否正确，或者返回首页重新开始。
          </p>

          <div class="error-actions">
            <el-button type="primary" size="large" @click="goHome">
              <el-icon><HomeFilled /></el-icon>
              返回首页
            </el-button>
            <el-button size="large" @click="goBack">
              <el-icon><ArrowLeft /></el-icon>
              返回上页
            </el-button>
          </div>
        </div>
      </div>

      <!-- 底部链接 -->
      <div class="error-footer">
        <div class="quick-links">
          <h3>快速链接</h3>
          <div class="links-grid">
            <router-link to="/dashboard" class="quick-link">
              <el-icon><DataBoard /></el-icon>
              <span>数据看板</span>
            </router-link>
            <router-link to="/users" class="quick-link">
              <el-icon><User /></el-icon>
              <span>用户管理</span>
            </router-link>
            <router-link to="/products" class="quick-link">
              <el-icon><Goods /></el-icon>
              <span>商品管理</span>
            </router-link>
            <router-link to="/orders" class="quick-link">
              <el-icon><Document /></el-icon>
              <span>订单管理</span>
            </router-link>
            <router-link to="/categories" class="quick-link">
              <el-icon><Menu /></el-icon>
              <span>分类管理</span>
            </router-link>
            <router-link to="/settings" class="quick-link">
              <el-icon><Setting /></el-icon>
              <span>系统设置</span>
            </router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';

const router = useRouter();

// 返回首页
const goHome = () => {
  router.push('/dashboard');
};

// 返回上一页
const goBack = () => {
  if (window.history.length > 1) {
    router.go(-1);
  } else {
    router.push('/dashboard');
  }
};
</script>

<style lang="scss" scoped>
.error-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.error-container {
  max-width: 1000px;
  width: 100%;
  background: white;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.error-content {
  display: flex;
  align-items: center;
  min-height: 500px;
  padding: 60px;

  @media (max-width: 768px) {
    flex-direction: column;
    padding: 40px 20px;
    text-align: center;
  }
}

.error-image {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;

  .error-svg {
    width: 100%;
    max-width: 400px;
    height: 300px;

    .error-text {
      font-size: 80px;
      font-weight: 900;
      fill: #f56c6c;
      font-family: 'Arial', sans-serif;

      @media (max-width: 768px) {
        font-size: 60px;
      }
    }

    .decoration-circle {
      fill: #409eff;
      opacity: 0.6;
      animation: float 3s ease-in-out infinite;

      &:nth-child(3) {
        animation-delay: 0.5s;
      }

      &:nth-child(4) {
        animation-delay: 1s;
      }

      &:nth-child(5) {
        animation-delay: 1.5s;
      }
    }

    .wave-line {
      opacity: 0.4;
      animation: wave 4s ease-in-out infinite;

      &:nth-child(7) {
        animation-delay: 2s;
      }
    }
  }
}

.error-info {
  flex: 1;
  padding-left: 40px;

  @media (max-width: 768px) {
    padding-left: 0;
    margin-top: 30px;
  }
}

.error-title {
  font-size: 48px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 20px 0;
  line-height: 1.2;

  @media (max-width: 768px) {
    font-size: 36px;
  }
}

.error-description {
  font-size: 18px;
  color: #5a6c7d;
  margin: 0 0 15px 0;
  line-height: 1.6;
}

.error-suggestion {
  font-size: 16px;
  color: #7f8c8d;
  margin: 0 0 40px 0;
  line-height: 1.6;
}

.error-actions {
  display: flex;
  gap: 15px;

  @media (max-width: 480px) {
    flex-direction: column;

    .el-button {
      width: 100%;
    }
  }
}

.error-footer {
  background: #f8f9fa;
  padding: 40px 60px;
  border-top: 1px solid #e9ecef;

  @media (max-width: 768px) {
    padding: 30px 20px;
  }
}

.quick-links {
  h3 {
    font-size: 20px;
    color: #2c3e50;
    margin: 0 0 25px 0;
    text-align: center;
  }
}

.links-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.quick-link {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 15px;
  background: white;
  border-radius: 12px;
  text-decoration: none;
  color: #5a6c7d;
  transition: all 0.3s ease;
  border: 2px solid transparent;

  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
    border-color: #409eff;
    color: #409eff;
  }

  .el-icon {
    font-size: 24px;
    margin-bottom: 8px;
  }

  span {
    font-size: 14px;
    font-weight: 500;
  }
}

// 动画
@keyframes float {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

@keyframes wave {
  0%,
  100% {
    opacity: 0.4;
  }
  50% {
    opacity: 0.8;
  }
}

// 响应式设计
@media (max-width: 480px) {
  .error-page {
    padding: 10px;
  }

  .error-container {
    border-radius: 15px;
  }

  .links-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 15px;
  }

  .quick-link {
    padding: 15px 10px;

    .el-icon {
      font-size: 20px;
    }

    span {
      font-size: 12px;
    }
  }
}
</style>
