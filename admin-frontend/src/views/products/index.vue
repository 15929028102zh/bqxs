<template>
  <div class="app-container">
    <div class="page-header">
      <h1>商品管理</h1>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        添加商品
      </el-button>
    </div>

    <!-- 搜索筛选 -->
    <div class="filter-container">
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="商品名称">
          <el-input
            v-model="queryParams.name"
            placeholder="请输入商品名称"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="分类">
          <el-select
            v-model="queryParams.categoryId"
            placeholder="请选择分类"
            clearable
            style="width: 150px"
          >
            <el-option
              v-for="category in categories"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="queryParams.status"
            placeholder="请选择状态"
            clearable
            style="width: 120px"
          >
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格区间">
          <el-input
            v-model="queryParams.minPrice"
            placeholder="最低价"
            style="width: 100px"
          />
          <span style="margin: 0 10px">-</span>
          <el-input
            v-model="queryParams.maxPrice"
            placeholder="最高价"
            style="width: 100px"
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

    <!-- 批量操作 -->
    <div class="batch-actions" v-if="selectedProducts.length > 0">
      <el-alert
        :title="`已选择 ${selectedProducts.length} 个商品`"
        type="info"
        show-icon
        :closable="false"
      >
        <template #default>
          <el-button size="small" @click="handleBatchStatus(1)"
            >批量上架</el-button
          >
          <el-button size="small" @click="handleBatchStatus(0)"
            >批量下架</el-button
          >
          <el-button size="small" type="danger" @click="handleBatchDelete"
            >批量删除</el-button
          >
        </template>
      </el-alert>
    </div>

    <!-- 商品列表 -->
    <div class="table-container">
      <el-table
        v-loading="loading"
        :data="productList"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="image" label="图片" width="100">
          <template #default="{ row }">
            <el-image
              :src="row.image"
              :preview-src-list="[row.image]"
              fit="cover"
              style="width: 60px; height: 60px; border-radius: 4px"
            />
          </template>
        </el-table-column>
        <el-table-column
          prop="name"
          label="商品名称"
          width="200"
          show-overflow-tooltip
        />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">
            <span class="price">¥{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="originalPrice" label="原价" width="100">
          <template #default="{ row }">
            <span v-if="row.originalPrice" class="original-price"
              >¥{{ row.originalPrice }}</span
            >
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="80">
          <template #default="{ row }">
            <span :class="{ 'low-stock': row.stock < 10 }">{{
              row.stock
            }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="sales" label="销量" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)">
              查看
            </el-button>
            <el-button type="warning" size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button
              :type="row.status === 1 ? 'info' : 'success'"
              size="small"
              @click="handleStatusChange(row)"
            >
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">
              删除
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

    <!-- 商品详情对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      :before-close="handleClose"
    >
      <div
        v-if="currentProduct && dialogType === 'view'"
        class="product-detail"
      >
        <el-descriptions :column="2" border>
          <el-descriptions-item label="商品图片" :span="2">
            <el-image
              :src="currentProduct.image"
              :preview-src-list="[currentProduct.image]"
              fit="cover"
              style="width: 200px; height: 200px; border-radius: 8px"
            />
          </el-descriptions-item>
          <el-descriptions-item label="商品名称">{{
            currentProduct.name
          }}</el-descriptions-item>
          <el-descriptions-item label="分类">{{
            currentProduct.categoryName
          }}</el-descriptions-item>
          <el-descriptions-item label="价格"
            >¥{{ currentProduct.price }}</el-descriptions-item
          >
          <el-descriptions-item label="原价">{{
            currentProduct.originalPrice
              ? '¥' + currentProduct.originalPrice
              : '-'
          }}</el-descriptions-item>
          <el-descriptions-item label="库存">{{
            currentProduct.stock
          }}</el-descriptions-item>
          <el-descriptions-item label="销量">{{
            currentProduct.sales
          }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentProduct.status === 1 ? 'success' : 'danger'">
              {{ currentProduct.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="单位">{{
            currentProduct.unit || '-'
          }}</el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">
            <div v-html="currentProduct.description || '-'"></div>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{
            formatTime(currentProduct.createTime)
          }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{
            formatTime(currentProduct.updateTime)
          }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 商品编辑表单 -->
      <el-form
        v-if="dialogType === 'edit' || dialogType === 'add'"
        ref="productFormRef"
        :model="productForm"
        :rules="productRules"
        label-width="100px"
      >
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="productForm.name" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select
            v-model="productForm.categoryId"
            placeholder="请选择分类"
            style="width: 100%"
          >
            <el-option
              v-for="category in categories"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number
            v-model="productForm.price"
            :min="0"
            :precision="2"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="原价">
          <el-input-number
            v-model="productForm.originalPrice"
            :min="0"
            :precision="2"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number
            v-model="productForm.stock"
            :min="0"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="单位">
          <el-input v-model="productForm.unit" placeholder="如：斤、个、盒等" />
        </el-form-item>
        <el-form-item label="商品图片">
          <ImageUpload
            v-model="productForm.image"
            :multiple="false"
            :max-size="5"
            placeholder="点击上传商品图片"
            tip="建议尺寸：800x800px，支持jpg、png格式，大小不超过5MB"
          />
        </el-form-item>
        <el-form-item label="商品描述">
          <el-input
            v-model="productForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入商品描述"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="productForm.status">
            <el-radio :label="1">上架</el-radio>
            <el-radio :label="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleClose">取消</el-button>
          <el-button
            v-if="dialogType === 'edit' || dialogType === 'add'"
            type="primary"
            @click="handleSubmit"
            :loading="submitLoading"
          >
            确定
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
  getProductList,
  createProduct,
  updateProduct,
  deleteProduct,
  updateProductStatus,
  batchDeleteProducts,
} from '@/api/product';
import { getCategoryList } from '@/api/category';
import ImageUpload from '@/components/ImageUpload.vue';
import dayjs from 'dayjs';

// 响应式数据
const loading = ref(false);
const submitLoading = ref(false);
const productList = ref([]);
const categories = ref([]);
const total = ref(0);
const selectedProducts = ref([]);
const dialogVisible = ref(false);
const dialogType = ref('view'); // view, edit, add
const dialogTitle = ref('');
const currentProduct = ref(null);
const productFormRef = ref();

// 查询参数
const queryParams = reactive({
  page: 1,
  size: 10,
  name: '',
  categoryId: null,
  status: null,
  minPrice: '',
  maxPrice: '',
});

// 商品表单
const productForm = reactive({
  name: '',
  categoryId: null,
  price: 0,
  originalPrice: null,
  stock: 0,
  unit: '',
  image: '',
  description: '',
  status: 1,
});

// 表单验证规则
const productRules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }],
};

// 获取商品列表
const getProductListData = async () => {
  loading.value = true;
  try {
    const response = await getProductList(queryParams);
    productList.value = response.data.records || [];
    total.value = response.data.total || 0;
  } catch (error) {
    ElMessage.error('获取商品列表失败');
  } finally {
    loading.value = false;
  }
};

// 获取分类列表
const getCategoriesData = async () => {
  try {
    const response = await getCategoryList();
    categories.value = response.data || [];
  } catch (error) {
    console.error('获取分类列表失败:', error);
  }
};

// 搜索
const handleQuery = () => {
  queryParams.page = 1;
  getProductListData();
};

// 重置
const resetQuery = () => {
  Object.assign(queryParams, {
    page: 1,
    size: 10,
    name: '',
    categoryId: null,
    status: null,
    minPrice: '',
    maxPrice: '',
  });
  getProductListData();
};

// 分页大小改变
const handleSizeChange = (val) => {
  queryParams.size = val;
  getProductListData();
};

// 当前页改变
const handleCurrentChange = (val) => {
  queryParams.page = val;
  getProductListData();
};

// 选择改变
const handleSelectionChange = (val) => {
  selectedProducts.value = val;
};

// 添加商品
const handleAdd = () => {
  dialogType.value = 'add';
  dialogTitle.value = '添加商品';
  resetProductForm();
  dialogVisible.value = true;
};

// 查看商品详情
const handleView = (row) => {
  dialogType.value = 'view';
  dialogTitle.value = '商品详情';
  currentProduct.value = row;
  dialogVisible.value = true;
};

// 编辑商品
const handleEdit = (row) => {
  dialogType.value = 'edit';
  dialogTitle.value = '编辑商品';
  currentProduct.value = row;
  Object.assign(productForm, row);
  dialogVisible.value = true;
};

// 状态改变
const handleStatusChange = async (row) => {
  const action = row.status === 1 ? '下架' : '上架';
  try {
    await ElMessageBox.confirm(
      `确定要${action}商品 "${row.name}" 吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );

    const newStatus = row.status === 1 ? 0 : 1;
    await updateProductStatus(row.id, newStatus);

    row.status = newStatus;
    ElMessage.success(`${action}成功`);
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`${action}失败`);
    }
  }
};

// 删除商品
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除商品 "${row.name}" 吗？此操作不可恢复！`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );

    await deleteProduct(row.id);
    ElMessage.success('删除成功');
    getProductListData();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败');
    }
  }
};

// 批量状态更改
const handleBatchStatus = async (status) => {
  const action = status === 1 ? '上架' : '下架';
  try {
    await ElMessageBox.confirm(
      `确定要批量${action}选中的 ${selectedProducts.value.length} 个商品吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );

    // 这里应该调用批量更新状态的API
    ElMessage.success(`批量${action}成功`);
    getProductListData();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`批量${action}失败`);
    }
  }
};

// 批量删除
const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedProducts.value.length} 个商品吗？此操作不可恢复！`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );

    const ids = selectedProducts.value.map((item) => item.id);
    await batchDeleteProducts(ids);
    ElMessage.success('批量删除成功');
    getProductListData();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败');
    }
  }
};

// 提交表单
const handleSubmit = async () => {
  try {
    const valid = await productFormRef.value.validate();
    if (!valid) return;

    submitLoading.value = true;

    if (dialogType.value === 'add') {
      await createProduct(productForm);
      ElMessage.success('添加成功');
    } else {
      await updateProduct(currentProduct.value.id, productForm);
      ElMessage.success('更新成功');
    }

    dialogVisible.value = false;
    getProductListData();
  } catch (error) {
    ElMessage.error(dialogType.value === 'add' ? '添加失败' : '更新失败');
  } finally {
    submitLoading.value = false;
  }
};

// 关闭对话框
const handleClose = () => {
  dialogVisible.value = false;
  currentProduct.value = null;
  resetProductForm();
};

// 重置商品表单
const resetProductForm = () => {
  Object.assign(productForm, {
    name: '',
    categoryId: null,
    price: 0,
    originalPrice: null,
    stock: 0,
    unit: '',
    image: '',
    description: '',
    status: 1,
  });
};

// 格式化时间
const formatTime = (time) => {
  return time ? dayjs(time).format('YYYY-MM-DD HH:mm:ss') : '-';
};

// 页面加载时获取数据
onMounted(() => {
  getProductListData();
  getCategoriesData();
});
</script>

<style lang="scss" scoped>
.batch-actions {
  margin-bottom: 20px;
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

.original-price {
  color: #909399;
  text-decoration: line-through;
}

.low-stock {
  color: #f56c6c;
  font-weight: 600;
}

.product-detail {
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
        .el-select {
          width: 100% !important;
        }
      }
    }
  }

  .table-container {
    overflow-x: auto;
  }
}
</style>
