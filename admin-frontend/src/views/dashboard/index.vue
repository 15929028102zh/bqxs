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
                @change="handlePeriodChange"
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
                <el-table-column prop="sales" label="销量" width="60" />
                <el-table-column prop="price" label="价格" width="70">
                  <template #default="{ row }"> ¥{{ row.price }} </template>
                </el-table-column>
                <el-table-column prop="revenue" label="销售额" width="80">
                  <template #default="{ row }"> ¥{{ formatNumber(row.revenue || 0) }} </template>
                </el-table-column>
                <el-table-column prop="stock" label="库存" width="60">
                  <template #default="{ row }">
                    <span :class="{ 'low-stock': row.stock < 10 }">{{ row.stock }}</span>
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
import { ref, reactive, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import request from '@/utils/request';
import dayjs from 'dayjs';

// 图表引用
const salesChartRef = ref();
const orderChartRef = ref();

// 销售趋势时间段
const salesPeriod = ref('week');

// 统计数据
const stats = reactive({
  totalUsers: 0,
  totalProducts: 0,
  totalOrders: 0,
  totalRevenue: 0,
});

// 最新订单数据
const recentOrders = ref([]);

// 热门商品数据
const hotProducts = ref([]);

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

// API调用函数
const fetchDashboardStats = async () => {
  try {
    const response = await request.get('/admin/statistics/overview');
    const data = response.data;
    
    Object.assign(stats, {
      totalUsers: data.totalUsers || 0,
      totalProducts: data.totalProducts || 0,
      totalOrders: data.totalOrders || 0,
      totalRevenue: data.totalRevenue || 0
    });
    
    // 更新订单状态分布数据
    if (data.orderStatusStats) {
      orderStatusData.value = [
        { value: data.orderStatusStats.pending || 0, name: '待支付', itemStyle: { color: '#F56C6C' } },
        { value: data.orderStatusStats.paid || 0, name: '待发货', itemStyle: { color: '#E6A23C' } },
        { value: data.orderStatusStats.shipped || 0, name: '已发货', itemStyle: { color: '#409EFF' } },
        { value: data.orderStatusStats.completed || 0, name: '已完成', itemStyle: { color: '#67C23A' } },
        { value: data.orderStatusStats.cancelled || 0, name: '已取消', itemStyle: { color: '#909399' } }
      ];
    }
  } catch (error) {
    console.error('获取统计数据失败:', error);
    // 使用默认值
    Object.assign(stats, {
      totalUsers: 0,
      totalProducts: 0,
      totalOrders: 0,
      totalRevenue: 0
    });
  }
};

const fetchSalesTrend = async () => {
  try {
    const response = await request.get('/admin/statistics/sales-trend', {
      params: {
        period: salesPeriod.value
      }
    });
    const data = response.data;
    
    // 处理后端返回的salesData数组
    if (data && data.salesData && Array.isArray(data.salesData)) {
      const dates = data.salesData.map(item => item.date);
      const orders = data.salesData.map(item => item.orders || 0);
      const amounts = data.salesData.map(item => item.amount || 0);
      
      return { dates, orders, amounts };
    }
    
    // 如果数据格式不正确，返回空数组
    return {
      dates: [],
      orders: [],
      amounts: []
    };
  } catch (error) {
    console.error('获取销售趋势数据失败:', error);
    // 返回空数据，不使用模拟数据
    return {
      dates: [],
      orders: [],
      amounts: []
    };
  }
};

const fetchRecentOrders = async () => {
  try {
    const response = await request.get('/admin/statistics/recent-orders', {
      params: {
        limit: 5
      }
    });
    const data = response.data;
    
    recentOrders.value = data || [];
  } catch (error) {
    console.error('获取最新订单数据失败:', error);
    // 不使用模拟数据，显示空列表
    recentOrders.value = [];
  }
};

const fetchHotProducts = async () => {
  try {
    const response = await request.get('/admin/statistics/hot-products', {
      params: {
        limit: 5
      }
    });
    const data = response.data;
    
    hotProducts.value = data || [];
  } catch (error) {
    console.error('获取热门商品数据失败:', error);
    // 不使用模拟数据，显示空列表
    hotProducts.value = [];
  }
};

// 加载销售趋势图
const loadSalesChart = async () => {
  const salesData = await fetchSalesTrend();
  
  nextTick(() => {
    const chart = echarts.init(salesChartRef.value);

    const option = {
      title: {
        text: '销售趋势',
        left: 'center',
        textStyle: {
          fontSize: 16,
          fontWeight: 'bold'
        }
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'cross'
        }
      },
      legend: {
        data: ['订单数', '销售额'],
        top: 30
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: salesData.dates
      },
      yAxis: [
        {
          type: 'value',
          name: '订单数',
          position: 'left'
        },
        {
          type: 'value',
          name: '销售额',
          position: 'right'
        }
      ],
      series: [
        {
          name: '订单数',
          type: 'line',
          data: salesData.orders,
          smooth: true,
          itemStyle: {
            color: '#409EFF'
          }
        },
        {
          name: '销售额',
          type: 'line',
          yAxisIndex: 1,
          data: salesData.amounts,
          smooth: true,
          itemStyle: {
            color: '#67C23A'
          }
        }
      ]
    };

    chart.setOption(option);

    // 响应式
    window.addEventListener('resize', () => {
      chart.resize();
    });
  });
};

// 订单状态分布数据
const orderStatusData = ref([
  { value: 0, name: '待支付', itemStyle: { color: '#F56C6C' } },
  { value: 0, name: '待发货', itemStyle: { color: '#E6A23C' } },
  { value: 0, name: '已发货', itemStyle: { color: '#409EFF' } },
  { value: 0, name: '已完成', itemStyle: { color: '#67C23A' } },
  { value: 0, name: '已取消', itemStyle: { color: '#909399' } }
]);

// 获取订单状态分布数据（已在fetchDashboardStats中处理）
const fetchOrderStatusData = async () => {
  // 订单状态数据已在fetchDashboardStats中获取和更新
  // 这里不需要额外的API调用
};

// 加载订单状态分布图
const loadOrderChart = () => {
  nextTick(() => {
    const chart = echarts.init(orderChartRef.value);

    const option = {
      title: {
        text: '订单状态分布',
        left: 'center',
        textStyle: {
          fontSize: 16,
          fontWeight: 'bold'
        }
      },
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)'
      },
      legend: {
        orient: 'vertical',
        left: 'left',
        top: 'middle'
      },
      series: [
        {
          name: '订单状态',
          type: 'pie',
          radius: ['40%', '70%'],
          center: ['60%', '50%'],
          avoidLabelOverlap: false,
          label: {
            show: false,
            position: 'center'
          },
          emphasis: {
            label: {
              show: true,
              fontSize: '18',
              fontWeight: 'bold'
            }
          },
          labelLine: {
            show: false
          },
          data: orderStatusData.value
        }
      ]
    };

    chart.setOption(option);

    // 响应式
    window.addEventListener('resize', () => {
      chart.resize();
    });
  });
};

// 监听销售周期变化
const handlePeriodChange = async () => {
  await loadSalesChart();
};

// 页面加载完成后初始化数据和图表
onMounted(async () => {
  // 并行加载所有数据
  await Promise.all([
    fetchDashboardStats(),
    fetchRecentOrders(),
    fetchHotProducts(),
    fetchOrderStatusData()
  ]);
  
  // 初始化图表
  await loadSalesChart();
  loadOrderChart();
});

// 暴露给模板使用的方法
defineExpose({
  handlePeriodChange
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
