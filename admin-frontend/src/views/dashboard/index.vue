<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-row :gutter="20">
        <el-col :xs="12" :sm="6" :md="6" :lg="6" :xl="6">
          <div class="stats-card">
            <div class="stats-icon users">
              <el-icon><User /></el-icon>
            </div>
            <div class="stats-content">
              <div class="stats-number">{{ stats.totalUsers }}</div>
              <div class="stats-label">总用户数</div>
            </div>
          </div>
        </el-col>

        <el-col :xs="12" :sm="6" :md="6" :lg="6" :xl="6">
          <div class="stats-card">
            <div class="stats-icon products">
              <el-icon><Goods /></el-icon>
            </div>
            <div class="stats-content">
              <div class="stats-number">{{ stats.totalProducts }}</div>
              <div class="stats-label">商品总数</div>
            </div>
          </div>
        </el-col>

        <el-col :xs="12" :sm="6" :md="6" :lg="6" :xl="6">
          <div class="stats-card">
            <div class="stats-icon orders">
              <el-icon><List /></el-icon>
            </div>
            <div class="stats-content">
              <div class="stats-number">{{ stats.totalOrders }}</div>
              <div class="stats-label">订单总数</div>
            </div>
          </div>
        </el-col>

        <el-col :xs="12" :sm="6" :md="6" :lg="6" :xl="6">
          <div class="stats-card">
            <div class="stats-icon revenue">
              <el-icon><Money /></el-icon>
            </div>
            <div class="stats-content">
              <div class="stats-number">
                ¥{{ formatNumber(stats.totalRevenue) }}
              </div>
              <div class="stats-label">总收入</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <el-row :gutter="20">
        <!-- 销售趋势图 -->
        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
          <div class="chart-card">
            <div class="chart-header">
              <h3>销售趋势</h3>
              <el-radio-group
                v-model="salesPeriod"
                size="small"
                @change="loadSalesChart"
              >
                <el-radio-button label="week">近7天</el-radio-button>
                <el-radio-button label="month">近30天</el-radio-button>
              </el-radio-group>
            </div>
            <div ref="salesChartRef" class="chart-container"></div>
          </div>
        </el-col>

        <!-- 订单状态分布 -->
        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
          <div class="chart-card">
            <div class="chart-header">
              <h3>订单状态分布</h3>
            </div>
            <div ref="orderChartRef" class="chart-container"></div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 最新订单和热门商品 -->
    <div class="data-section">
      <el-row :gutter="20">
        <!-- 最新订单 -->
        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
          <div class="data-card">
            <div class="data-header">
              <h3>最新订单</h3>
              <el-button type="text" @click="$router.push('/orders')"
                >查看更多</el-button
              >
            </div>
            <div class="data-content">
              <el-table :data="recentOrders" style="width: 100%" size="small">
                <el-table-column prop="orderNo" label="订单号" width="120" />
                <el-table-column prop="userName" label="用户" width="100" />
                <el-table-column prop="totalAmount" label="金额" width="80">
                  <template #default="{ row }">
                    ¥{{ row.totalAmount }}
                  </template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="80">
                  <template #default="{ row }">
                    <el-tag :type="getOrderStatusType(row.status)" size="small">
                      {{ getOrderStatusText(row.status) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="createTime" label="时间">
                  <template #default="{ row }">
                    {{ formatTime(row.createTime) }}
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
        </el-col>

        <!-- 热门商品 -->
        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
          <div class="data-card">
            <div class="data-header">
              <h3>热门商品</h3>
              <el-button type="text" @click="$router.push('/products')"
                >查看更多</el-button
              >
            </div>
            <div class="data-content">
              <el-table :data="hotProducts" style="width: 100%" size="small">
                <el-table-column prop="name" label="商品名称" />
                <el-table-column prop="sales" label="销量" width="80" />
                <el-table-column prop="price" label="价格" width="80">
                  <template #default="{ row }"> ¥{{ row.price }} </template>
                </el-table-column>
                <el-table-column prop="stock" label="库存" width="80">
                  <template #default="{ row }">
                    <span :class="{ 'low-stock': row.stock < 10 }">{{
                      row.stock
                    }}</span>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue';
import * as echarts from 'echarts';
import dayjs from 'dayjs';

// 图表引用
const salesChartRef = ref();
const orderChartRef = ref();

// 销售趋势时间段
const salesPeriod = ref('week');

// 统计数据
const stats = reactive({
  totalUsers: 1234,
  totalProducts: 567,
  totalOrders: 890,
  totalRevenue: 123456.78,
});

// 最新订单
const recentOrders = ref([
  {
    orderNo: 'ORD20240101001',
    userName: '张三',
    totalAmount: 128.5,
    status: 1,
    createTime: new Date(),
  },
  {
    orderNo: 'ORD20240101002',
    userName: '李四',
    totalAmount: 89.9,
    status: 2,
    createTime: new Date(Date.now() - 3600000),
  },
  {
    orderNo: 'ORD20240101003',
    userName: '王五',
    totalAmount: 256.0,
    status: 3,
    createTime: new Date(Date.now() - 7200000),
  },
]);

// 热门商品
const hotProducts = ref([
  {
    name: '新鲜苹果',
    sales: 156,
    price: 12.8,
    stock: 89,
  },
  {
    name: '有机蔬菜',
    sales: 134,
    price: 8.5,
    stock: 5,
  },
  {
    name: '优质牛肉',
    sales: 98,
    price: 45.6,
    stock: 23,
  },
]);

// 格式化数字
const formatNumber = (num) => {
  return num.toLocaleString();
};

// 格式化时间
const formatTime = (time) => {
  return dayjs(time).format('MM-DD HH:mm');
};

// 获取订单状态类型
const getOrderStatusType = (status) => {
  const types = {
    1: 'warning',
    2: 'primary',
    3: 'success',
    4: 'info',
    5: 'danger',
  };
  return types[status] || 'info';
};

// 获取订单状态文本
const getOrderStatusText = (status) => {
  const texts = {
    1: '待付款',
    2: '待发货',
    3: '已发货',
    4: '已完成',
    5: '已取消',
  };
  return texts[status] || '未知';
};

// 加载销售趋势图
const loadSalesChart = () => {
  nextTick(() => {
    const chart = echarts.init(salesChartRef.value);

    const option = {
      tooltip: {
        trigger: 'axis',
      },
      xAxis: {
        type: 'category',
        data:
          salesPeriod.value === 'week'
            ? ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
            : Array.from({ length: 30 }, (_, i) => `${i + 1}日`),
      },
      yAxis: {
        type: 'value',
      },
      series: [
        {
          data:
            salesPeriod.value === 'week'
              ? [820, 932, 901, 934, 1290, 1330, 1320]
              : Array.from(
                  { length: 30 },
                  () => Math.floor(Math.random() * 1000) + 500
                ),
          type: 'line',
          smooth: true,
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
              { offset: 1, color: 'rgba(64, 158, 255, 0.1)' },
            ]),
          },
          lineStyle: {
            color: '#409eff',
          },
        },
      ],
    };

    chart.setOption(option);

    // 响应式
    window.addEventListener('resize', () => {
      chart.resize();
    });
  });
};

// 加载订单状态分布图
const loadOrderChart = () => {
  nextTick(() => {
    const chart = echarts.init(orderChartRef.value);

    const option = {
      tooltip: {
        trigger: 'item',
      },
      series: [
        {
          type: 'pie',
          radius: '60%',
          data: [
            { value: 35, name: '待付款' },
            { value: 25, name: '待发货' },
            { value: 20, name: '已发货' },
            { value: 15, name: '已完成' },
            { value: 5, name: '已取消' },
          ],
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)',
            },
          },
        },
      ],
    };

    chart.setOption(option);

    // 响应式
    window.addEventListener('resize', () => {
      chart.resize();
    });
  });
};

// 页面加载完成后初始化图表
onMounted(() => {
  loadSalesChart();
  loadOrderChart();
});
</script>

<style lang="scss" scoped>
.dashboard {
  .stats-cards {
    margin-bottom: 20px;

    .stats-card {
      display: flex;
      align-items: center;
      padding: 20px;
      background: #fff;
      border-radius: 8px;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);

      .stats-icon {
        width: 60px;
        height: 60px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 16px;
        font-size: 24px;
        color: #fff;

        &.users {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        }

        &.products {
          background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
        }

        &.orders {
          background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
        }

        &.revenue {
          background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
        }
      }

      .stats-content {
        .stats-number {
          font-size: 24px;
          font-weight: 600;
          color: #2c3e50;
          margin-bottom: 4px;
        }

        .stats-label {
          font-size: 14px;
          color: #7f8c8d;
        }
      }
    }
  }

  .charts-section {
    margin-bottom: 20px;

    .chart-card {
      background: #fff;
      border-radius: 8px;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);

      .chart-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 20px 20px 0;

        h3 {
          margin: 0;
          font-size: 16px;
          color: #2c3e50;
        }
      }

      .chart-container {
        height: 300px;
        padding: 20px;
      }
    }
  }

  .data-section {
    .data-card {
      background: #fff;
      border-radius: 8px;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);

      .data-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 20px 20px 0;

        h3 {
          margin: 0;
          font-size: 16px;
          color: #2c3e50;
        }
      }

      .data-content {
        padding: 20px;

        .low-stock {
          color: #f56c6c;
          font-weight: 600;
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .dashboard {
    .stats-cards {
      .stats-card {
        padding: 15px;

        .stats-icon {
          width: 50px;
          height: 50px;
          font-size: 20px;
          margin-right: 12px;
        }

        .stats-content {
          .stats-number {
            font-size: 20px;
          }

          .stats-label {
            font-size: 12px;
          }
        }
      }
    }

    .charts-section,
    .data-section {
      .chart-container {
        height: 250px;
      }
    }
  }
}
</style>
