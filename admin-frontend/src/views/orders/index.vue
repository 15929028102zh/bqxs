<template>
  <div class="app-container">
    <div class="page-header">
      <h1>订单管理</h1>
      <el-button type="primary" @click="handleExport">
        <el-icon><Download /></el-icon>
        导出订单
      </el-button>
    </div>

    <!-- 搜索筛选 -->
    <div class="filter-container">
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="订单号">
          <el-input
            v-model="queryParams.orderNo"
            placeholder="请输入订单号"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input
            v-model="queryParams.userName"
            placeholder="请输入用户名"
            clearable
            style="width: 150px"
          />
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select
            v-model="queryParams.status"
            placeholder="请选择状态"
            clearable
            style="width: 120px"
          >
            <el-option label="待付款" :value="1" />
            <el-option label="待发货" :value="2" />
            <el-option label="已发货" :value="3" />
            <el-option label="已完成" :value="4" />
            <el-option label="已取消" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="下单时间">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            @change="handleDateChange"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="resetQuery">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 订单统计 -->
    <div class="stats-container">
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-number">{{ orderStats.total }}</div>
            <div class="stat-label">总订单数</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-number">{{ orderStats.pending }}</div>
            <div class="stat-label">待处理</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-number">{{ orderStats.completed }}</div>
            <div class="stat-label">已完成</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-number">
              ¥{{ formatNumber(orderStats.totalAmount) }}
            </div>
            <div class="stat-label">总金额</div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 订单列表 -->
    <div class="table-container">
      <el-table
        v-loading="loading"
        :data="orderList"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="orderNo" label="订单号" width="160" />
        <el-table-column prop="userName" label="用户" width="120" />
        <el-table-column prop="userPhone" label="联系电话" width="130" />
        <el-table-column prop="totalAmount" label="订单金额" width="120">
          <template #default="{ row }">
            <span class="price">¥{{ row.totalAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getOrderStatusType(row.status)">
              {{ getOrderStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="paymentMethod" label="支付方式" width="100">
          <template #default="{ row }">
            <span v-if="row.paymentMethod === 1">微信支付</span>
            <span v-else-if="row.paymentMethod === 2">现金支付</span>
            <span v-else-if="row.paymentMethod === 3">货到付款</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="deliveryAddress"
          label="收货地址"
          width="200"
          show-overflow-tooltip
        />
        <el-table-column prop="createTime" label="下单时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)">
              查看
            </el-button>
            <el-button
              v-if="row.status === 2"
              type="success"
              size="small"
              @click="handleShip(row)"
            >
              发货
            </el-button>
            <el-button
              v-if="row.status === 1"
              type="danger"
              size="small"
              @click="handleCancel(row)"
            >
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 订单详情对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="订单详情"
      width="900px"
      :before-close="handleClose"
    >
      <div v-if="currentOrder" class="order-detail">
        <!-- 订单基本信息 -->
        <div class="detail-section">
          <h3>订单信息</h3>
          <el-descriptions :column="3" border>
            <el-descriptions-item label="订单号">{{
              currentOrder.orderNo
            }}</el-descriptions-item>
            <el-descriptions-item label="订单状态">
              <el-tag :type="getOrderStatusType(currentOrder.status)">
                {{ getOrderStatusText(currentOrder.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="订单金额">
              <span class="price">¥{{ currentOrder.totalAmount }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="支付方式">
              <span v-if="currentOrder.paymentMethod === 1">微信支付</span>
              <span v-else-if="currentOrder.paymentMethod === 2">支付宝</span>
              <span v-else-if="currentOrder.paymentMethod === 3">货到付款</span>
              <span v-else>-</span>
            </el-descriptions-item>
            <el-descriptions-item label="下单时间">{{
              formatTime(currentOrder.createTime)
            }}</el-descriptions-item>
            <el-descriptions-item label="支付时间">{{
              formatTime(currentOrder.payTime)
            }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 用户信息 -->
        <div class="detail-section">
          <h3>用户信息</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="用户名">{{
              currentOrder.userName
            }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{
              currentOrder.userPhone
            }}</el-descriptions-item>
            <el-descriptions-item label="收货地址" :span="2">{{
              currentOrder.deliveryAddress
            }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 商品信息 -->
        <div class="detail-section">
          <h3>商品信息</h3>
          <el-table :data="currentOrder.items" style="width: 100%">
            <el-table-column prop="productImage" label="商品图片" width="100">
              <template #default="{ row }">
                <el-image
                  :src="row.productImage"
                  fit="cover"
                  style="width: 60px; height: 60px; border-radius: 4px"
                />
              </template>
            </el-table-column>
            <el-table-column prop="productName" label="商品名称" />
            <el-table-column prop="price" label="单价" width="100">
              <template #default="{ row }"> ¥{{ row.price }} </template>
            </el-table-column>
            <el-table-column prop="quantity" label="数量" width="80" />
            <el-table-column prop="subtotal" label="小计" width="100">
              <template #default="{ row }">
                <span class="price">¥{{ row.subtotal }}</span>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 物流信息 -->
        <div v-if="currentOrder.logistics" class="detail-section">
          <h3>物流信息</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="物流公司">{{
              currentOrder.logistics.company
            }}</el-descriptions-item>
            <el-descriptions-item label="快递单号">{{
              currentOrder.logistics.trackingNo
            }}</el-descriptions-item>
            <el-descriptions-item label="发货时间">{{
              formatTime(currentOrder.logistics.shipTime)
            }}</el-descriptions-item>
            <el-descriptions-item label="预计到达">{{
              formatTime(currentOrder.logistics.estimatedTime)
            }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 发货对话框 -->
    <el-dialog v-model="shipDialogVisible" title="订单发货" width="500px">
      <el-form
        ref="shipFormRef"
        :model="shipForm"
        :rules="shipRules"
        label-width="100px"
      >
        <el-form-item label="物流公司" prop="company">
          <el-select
            v-model="shipForm.company"
            placeholder="请选择物流公司"
            style="width: 100%"
          >
            <el-option label="顺丰速运" value="顺丰速运" />
            <el-option label="圆通速递" value="圆通速递" />
            <el-option label="中通快递" value="中通快递" />
            <el-option label="申通快递" value="申通快递" />
            <el-option label="韵达速递" value="韵达速递" />
            <el-option label="百世快递" value="百世快递" />
          </el-select>
        </el-form-item>
        <el-form-item label="快递单号" prop="trackingNo">
          <el-input
            v-model="shipForm.trackingNo"
            placeholder="请输入快递单号"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="shipForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="shipDialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            @click="handleConfirmShip"
            :loading="shipLoading"
          >
            确认发货
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  getOrderList,
  getOrderDetail,
  cancelOrder,
  shipOrder,
  getOrderStats,
  exportOrders,
} from '@/api/order';
import dayjs from 'dayjs';

// 响应式数据
const loading = ref(false);
const shipLoading = ref(false);
const orderList = ref([]);
const total = ref(0);
const selectedOrders = ref([]);
const dialogVisible = ref(false);
const shipDialogVisible = ref(false);
const currentOrder = ref(null);
const dateRange = ref([]);
const shipFormRef = ref();

// 订单统计
const orderStats = reactive({
  total: 0,
  pending: 0,
  completed: 0,
  totalAmount: 0,
});

// 查询参数
const queryParams = reactive({
  page: 1,
  size: 10,
  orderNo: '',
  userName: '',
  status: null,
  startDate: '',
  endDate: '',
});

// 发货表单
const shipForm = reactive({
  company: '',
  trackingNo: '',
  remark: '',
});

// 发货表单验证规则
const shipRules = {
  company: [{ required: true, message: '请选择物流公司', trigger: 'change' }],
  trackingNo: [{ required: true, message: '请输入快递单号', trigger: 'blur' }],
};

// 获取订单列表
const getOrderListData = async () => {
  loading.value = true;
  try {
    const response = await getOrderList(queryParams);
    orderList.value = response.data.records || [];
    total.value = response.data.total || 0;
  } catch (error) {
    ElMessage.error('获取订单列表失败');
  } finally {
    loading.value = false;
  }
};

// 获取订单统计
const getOrderStatsData = async () => {
  try {
    const response = await getOrderStats();
    Object.assign(orderStats, response.data);
  } catch (error) {
    console.error('获取订单统计失败:', error);
  }
};

// 搜索
const handleQuery = () => {
  queryParams.page = 1;
  getOrderListData();
};

// 重置
const resetQuery = () => {
  Object.assign(queryParams, {
    page: 1,
    size: 10,
    orderNo: '',
    userName: '',
    status: null,
    startDate: '',
    endDate: '',
  });
  dateRange.value = [];
  getOrderListData();
};

// 日期范围改变
const handleDateChange = (dates) => {
  if (dates && dates.length === 2) {
    queryParams.startDate = dates[0];
    queryParams.endDate = dates[1];
  } else {
    queryParams.startDate = '';
    queryParams.endDate = '';
  }
};

// 分页大小改变
const handleSizeChange = (val) => {
  queryParams.size = val;
  getOrderListData();
};

// 当前页改变
const handleCurrentChange = (val) => {
  queryParams.page = val;
  getOrderListData();
};

// 选择改变
const handleSelectionChange = (val) => {
  selectedOrders.value = val;
};

// 查看订单详情
const handleView = async (row) => {
  try {
    const response = await getOrderDetail(row.id);
    currentOrder.value = response.data;
    dialogVisible.value = true;
  } catch (error) {
    ElMessage.error('获取订单详情失败');
  }
};

// 发货
const handleShip = (row) => {
  currentOrder.value = row;
  resetShipForm();
  shipDialogVisible.value = true;
};

// 确认发货
const handleConfirmShip = async () => {
  try {
    const valid = await shipFormRef.value.validate();
    if (!valid) return;

    shipLoading.value = true;

    await shipOrder(currentOrder.value.id, shipForm);
    ElMessage.success('发货成功');

    shipDialogVisible.value = false;
    getOrderListData();
  } catch (error) {
    ElMessage.error('发货失败');
  } finally {
    shipLoading.value = false;
  }
};

// 取消订单
const handleCancel = async (row) => {
  try {
    const { value: reason } = await ElMessageBox.prompt(
      '请输入取消原因',
      '取消订单',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPattern: /.+/,
        inputErrorMessage: '取消原因不能为空',
      }
    );

    await cancelOrder(row.id, reason);
    ElMessage.success('订单已取消');
    getOrderListData();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消订单失败');
    }
  }
};

// 导出订单
const handleExport = async () => {
  try {
    await exportOrders(queryParams);
    ElMessage.success('导出成功');
  } catch (error) {
    ElMessage.error('导出失败');
  }
};

// 关闭对话框
const handleClose = () => {
  dialogVisible.value = false;
  currentOrder.value = null;
};

// 重置发货表单
const resetShipForm = () => {
  Object.assign(shipForm, {
    company: '',
    trackingNo: '',
    remark: '',
  });
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

// 格式化数字
const formatNumber = (num) => {
  return num.toLocaleString();
};

// 格式化时间
const formatTime = (time) => {
  return time ? dayjs(time).format('YYYY-MM-DD HH:mm:ss') : '-';
};

// 页面加载时获取数据
onMounted(() => {
  getOrderListData();
  getOrderStatsData();
});
</script>

<style lang="scss" scoped>
.stats-container {
  margin-bottom: 20px;

  .stat-card {
    text-align: center;
    padding: 20px;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);

    .stat-number {
      font-size: 24px;
      font-weight: 600;
      color: #2c3e50;
      margin-bottom: 8px;
    }

    .stat-label {
      font-size: 14px;
      color: #7f8c8d;
    }
  }
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding: 20px;
}

.price {
  color: #f56c6c;
  font-weight: 600;
}

.order-detail {
  .detail-section {
    margin-bottom: 30px;

    &:last-child {
      margin-bottom: 0;
    }

    h3 {
      margin-bottom: 15px;
      font-size: 16px;
      color: #2c3e50;
      border-left: 4px solid #409eff;
      padding-left: 10px;
    }
  }

  .el-descriptions {
    :deep(.el-descriptions__body) {
      .el-descriptions__table {
        .el-descriptions__cell {
          padding: 12px;
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .filter-container {
    .el-form {
      .el-form-item {
        margin-bottom: 10px;

        .el-input,
        .el-select,
        .el-date-picker {
          width: 100% !important;
        }
      }
    }
  }

  .stats-container {
    .el-col {
      margin-bottom: 10px;
    }
  }

  .table-container {
    overflow-x: auto;
  }
}
</style>
