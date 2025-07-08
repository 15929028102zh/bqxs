<template>
  <div class="app-container">
    <div class="page-header">
      <h1>分类管理</h1>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增分类
      </el-button>
    </div>

    <!-- 搜索筛选 -->
    <div class="filter-container">
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="分类名称">
          <el-input
            v-model="queryParams.name"
            placeholder="请输入分类名称"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="queryParams.status"
            placeholder="请选择状态"
            clearable
            style="width: 120px"
          >
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
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

    <!-- 分类树形表格 -->
    <div class="table-container">
      <el-table
        v-loading="loading"
        :data="categoryList"
        style="width: 100%"
        row-key="id"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        :expand-row-keys="expandedKeys"
        @expand-change="handleExpandChange"
      >
        <el-table-column prop="name" label="分类名称" width="200">
          <template #default="{ row }">
            <div class="category-name">
              <el-image
                v-if="row.icon"
                :src="row.icon"
                fit="cover"
                style="
                  width: 24px;
                  height: 24px;
                  margin-right: 8px;
                  border-radius: 4px;
                "
              />
              <span>{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column
          prop="description"
          label="描述"
          show-overflow-tooltip
        />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="productCount" label="商品数量" width="100">
          <template #default="{ row }">
            <el-tag type="info" size="small">{{
              row.productCount || 0
            }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button
              v-if="!row.children || row.children.length === 0"
              type="success"
              size="small"
              @click="handleAddChild(row)"
            >
              添加子分类
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="handleDelete(row)"
              :disabled="row.productCount > 0"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 新增/编辑分类对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :before-close="handleClose"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="上级分类">
          <el-tree-select
            v-model="form.parentId"
            :data="categoryTreeData"
            :props="{ value: 'id', label: 'name', children: 'children' }"
            placeholder="请选择上级分类（不选择则为顶级分类）"
            clearable
            check-strictly
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="分类图标">
          <div class="upload-container">
            <el-upload
              class="icon-uploader"
              :action="uploadUrl"
              :show-file-list="false"
              :on-success="handleIconSuccess"
              :before-upload="beforeIconUpload"
            >
              <el-image
                v-if="form.icon"
                :src="form.icon"
                class="icon"
                fit="cover"
              />
              <el-icon v-else class="icon-uploader-icon"><Plus /></el-icon>
            </el-upload>
            <div class="upload-tip">建议尺寸：64x64px，支持jpg、png格式</div>
          </div>
        </el-form-item>
        <el-form-item label="分类描述">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入分类描述"
          />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number
            v-model="form.sort"
            :min="0"
            :max="999"
            placeholder="数值越小越靠前"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button
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
import { ref, reactive, onMounted, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Search, Refresh } from '@element-plus/icons-vue';
import {
  getCategoryList,
  getCategoryDetail,
  createCategory,
  updateCategory,
  deleteCategory,
  updateCategoryStatus,
  getCategoryTree,
} from '@/api/category';
import dayjs from 'dayjs';

// 响应式数据
const loading = ref(false);
const submitLoading = ref(false);
const categoryList = ref([]);
const categoryTreeData = ref([]);
const dialogVisible = ref(false);
const isEdit = ref(false);
const expandedKeys = ref([]);
const formRef = ref();

// 上传地址
const uploadUrl = ref('/api/upload/image');

// 查询参数
const queryParams = reactive({
  name: '',
  status: null,
});

// 表单数据
const form = reactive({
  id: null,
  parentId: null,
  name: '',
  icon: '',
  description: '',
  sort: 0,
  status: 1,
});

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' },
  ],
  sort: [{ required: true, message: '请输入排序值', trigger: 'blur' }],
};

// 计算属性
const dialogTitle = computed(() => {
  return isEdit.value ? '编辑分类' : '新增分类';
});

// 获取分类列表
const getCategoryListData = async () => {
  loading.value = true;
  try {
    const response = await getCategoryList(queryParams);
    
    // 确保数据是数组格式
    let data = response.data;
    if (data && typeof data === 'object') {
      // 如果是分页数据结构
      if (data.records && Array.isArray(data.records)) {
        categoryList.value = data.records;
      }
      // 如果是直接的数组
      else if (Array.isArray(data)) {
        categoryList.value = data;
      }
      // 如果是其他对象结构，尝试获取list或items属性
      else if (data.list && Array.isArray(data.list)) {
        categoryList.value = data.list;
      }
      else if (data.items && Array.isArray(data.items)) {
        categoryList.value = data.items;
      }
      else {
        console.warn('API返回的数据格式不是预期的数组:', data);
        categoryList.value = [];
      }
    } else {
      categoryList.value = [];
    }
  } catch (error) {
    console.error('获取分类列表失败:', error);
    categoryList.value = []; // 确保出错时也是数组
    ElMessage.error(`获取分类列表失败: ${error.message || error}`);
  } finally {
    loading.value = false;
  }
};

// 获取分类树形数据
const getCategoryTreeData = async () => {
  try {
    const response = await getCategoryTree();
    // 确保树形数据也是数组格式
    let data = response.data;
    if (Array.isArray(data)) {
      categoryTreeData.value = data;
    } else if (data && typeof data === 'object' && data.list && Array.isArray(data.list)) {
      categoryTreeData.value = data.list;
    } else {
      categoryTreeData.value = [];
    }
  } catch (error) {
    console.error('获取分类树形数据失败:', error);
    categoryTreeData.value = []; // 确保出错时也是数组
  }
};

// 搜索
const handleQuery = () => {
  getCategoryListData();
};

// 重置
const resetQuery = () => {
  Object.assign(queryParams, {
    name: '',
    status: null,
  });
  getCategoryListData();
};

// 展开/收起
const handleExpandChange = (row, expanded) => {
  if (expanded) {
    expandedKeys.value.push(row.id);
  } else {
    const index = expandedKeys.value.indexOf(row.id);
    if (index > -1) {
      expandedKeys.value.splice(index, 1);
    }
  }
};

// 状态改变
const handleStatusChange = async (row) => {
  try {
    await updateCategoryStatus(row.id, row.status);
    ElMessage.success('状态更新成功');
  } catch (error) {
    // 恢复原状态
    row.status = row.status === 1 ? 0 : 1;
    ElMessage.error('状态更新失败');
  }
};

// 新增分类
const handleAdd = () => {
  resetForm();
  isEdit.value = false;
  dialogVisible.value = true;
  getCategoryTreeData();
};

// 添加子分类
const handleAddChild = (row) => {
  resetForm();
  form.parentId = row.id;
  isEdit.value = false;
  dialogVisible.value = true;
  getCategoryTreeData();
};

// 编辑分类
const handleEdit = async (row) => {
  try {
    const response = await getCategoryDetail(row.id);
    Object.assign(form, response.data);
    isEdit.value = true;
    dialogVisible.value = true;
    getCategoryTreeData();
  } catch (error) {
    ElMessage.error('获取分类详情失败');
  }
};

// 删除分类
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除分类"${row.name}"吗？`, '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    });

    await deleteCategory(row.id);
    ElMessage.success('删除成功');
    getCategoryListData();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败');
    }
  }
};

// 提交表单
const handleSubmit = async () => {
  try {
    const valid = await formRef.value.validate();
    if (!valid) return;

    submitLoading.value = true;

    if (isEdit.value) {
      await updateCategory(form.id, form);
      ElMessage.success('更新成功');
    } else {
      await createCategory(form);
      ElMessage.success('创建成功');
    }

    dialogVisible.value = false;
    getCategoryListData();
  } catch (error) {
    ElMessage.error(isEdit.value ? '更新失败' : '创建失败');
  } finally {
    submitLoading.value = false;
  }
};

// 关闭对话框
const handleClose = () => {
  dialogVisible.value = false;
  resetForm();
};

// 重置表单
const resetForm = () => {
  Object.assign(form, {
    id: null,
    parentId: null,
    name: '',
    icon: '',
    description: '',
    sort: 0,
    status: 1,
  });
  if (formRef.value) {
    formRef.value.clearValidate();
  }
};

// 图标上传成功
const handleIconSuccess = (response) => {
  if (response.code === 200) {
    form.icon = response.data.url;
    ElMessage.success('图标上传成功');
  } else {
    ElMessage.error('图标上传失败');
  }
};

// 图标上传前验证
const beforeIconUpload = (file) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png';
  const isLt2M = file.size / 1024 / 1024 < 2;

  if (!isJPG) {
    ElMessage.error('图标只能是 JPG/PNG 格式!');
  }
  if (!isLt2M) {
    ElMessage.error('图标大小不能超过 2MB!');
  }
  return isJPG && isLt2M;
};

// 格式化时间
const formatTime = (time) => {
  return time ? dayjs(time).format('YYYY-MM-DD HH:mm:ss') : '-';
};

// 页面加载时获取数据
onMounted(() => {
  getCategoryListData();
});
</script>

<style lang="scss" scoped>
.category-name {
  display: flex;
  align-items: center;
}

.upload-container {
  .icon-uploader {
    :deep(.el-upload) {
      border: 1px dashed #d9d9d9;
      border-radius: 6px;
      cursor: pointer;
      position: relative;
      overflow: hidden;
      transition: 0.2s;

      &:hover {
        border-color: #409eff;
      }
    }

    .icon {
      width: 64px;
      height: 64px;
      display: block;
    }

    .icon-uploader-icon {
      font-size: 28px;
      color: #8c939d;
      width: 64px;
      height: 64px;
      text-align: center;
      line-height: 64px;
    }
  }

  .upload-tip {
    margin-top: 8px;
    font-size: 12px;
    color: #999;
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
