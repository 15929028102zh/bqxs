<template>
  <div class="app-container">
    <div class="page-header">
      <h1>用户管理</h1>
    </div>

    <!-- 搜索筛选 -->
    <div class="filter-container">
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="用户名">
          <el-input
            v-model="queryParams.username"
            placeholder="请输入用户名"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input
            v-model="queryParams.phone"
            placeholder="请输入手机号"
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

    <!-- 用户列表 -->
    <div class="table-container">
      <el-table
        v-loading="loading"
        :data="userList"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="avatar" label="头像" width="80">
          <template #default="{ row }">
            <el-avatar :size="40" :src="row.avatar">
              <el-icon><User /></el-icon>
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="nickname" label="昵称" width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="gender" label="性别" width="80">
          <template #default="{ row }">
            <span v-if="row.gender === 1">男</span>
            <span v-else-if="row.gender === 2">女</span>
            <span v-else>未知</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" label="最后登录" width="180">
          <template #default="{ row }">
            {{ formatTime(row.lastLoginTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)">
              查看
            </el-button>
            <el-button
              :type="row.status === 1 ? 'warning' : 'success'"
              size="small"
              @click="handleStatusChange(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
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

    <!-- 用户详情对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="用户详情"
      width="600px"
      :before-close="handleClose"
    >
      <div v-if="currentUser" class="user-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="头像">
            <el-avatar :size="60" :src="currentUser.avatar">
              <el-icon><User /></el-icon>
            </el-avatar>
          </el-descriptions-item>
          <el-descriptions-item label="用户名">{{
            currentUser.username
          }}</el-descriptions-item>
          <el-descriptions-item label="昵称">{{
            currentUser.nickname || '-'
          }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{
            currentUser.phone || '-'
          }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{
            currentUser.email || '-'
          }}</el-descriptions-item>
          <el-descriptions-item label="性别">
            <span v-if="currentUser.gender === 1">男</span>
            <span v-else-if="currentUser.gender === 2">女</span>
            <span v-else>未知</span>
          </el-descriptions-item>
          <el-descriptions-item label="生日">{{
            currentUser.birthday || '-'
          }}</el-descriptions-item>
          <el-descriptions-item label="地址">{{
            currentUser.address || '-'
          }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentUser.status === 1 ? 'success' : 'danger'">
              {{ currentUser.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="注册时间">{{
            formatTime(currentUser.createTime)
          }}</el-descriptions-item>
          <el-descriptions-item label="最后登录">{{
            formatTime(currentUser.lastLoginTime)
          }}</el-descriptions-item>
          <el-descriptions-item label="登录次数">{{
            currentUser.loginCount || 0
          }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getUserList, updateUserStatus, deleteUser } from '@/api/user';
import dayjs from 'dayjs';

// 响应式数据
const loading = ref(false);
const userList = ref([]);
const total = ref(0);
const selectedUsers = ref([]);
const dialogVisible = ref(false);
const currentUser = ref(null);

// 查询参数
const queryParams = reactive({
  page: 1,
  size: 10,
  username: '',
  phone: '',
  status: null,
});

// 获取用户列表
const getUserListData = async () => {
  loading.value = true;
  try {
    const response = await getUserList(queryParams);
    userList.value = response.data.records || [];
    total.value = response.data.total || 0;
  } catch (error) {
    ElMessage.error('获取用户列表失败');
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleQuery = () => {
  queryParams.page = 1;
  getUserListData();
};

// 重置
const resetQuery = () => {
  queryParams.page = 1;
  queryParams.size = 10;
  queryParams.username = '';
  queryParams.phone = '';
  queryParams.status = null;
  getUserListData();
};

// 分页大小改变
const handleSizeChange = (val) => {
  queryParams.size = val;
  getUserListData();
};

// 当前页改变
const handleCurrentChange = (val) => {
  queryParams.page = val;
  getUserListData();
};

// 选择改变
const handleSelectionChange = (val) => {
  selectedUsers.value = val;
};

// 查看用户详情
const handleView = (row) => {
  currentUser.value = row;
  dialogVisible.value = true;
};

// 状态改变
const handleStatusChange = async (row) => {
  const action = row.status === 1 ? '禁用' : '启用';
  try {
    await ElMessageBox.confirm(
      `确定要${action}用户 "${row.username}" 吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );

    const newStatus = row.status === 1 ? 0 : 1;
    await updateUserStatus(row.id, newStatus);

    row.status = newStatus;
    ElMessage.success(`${action}成功`);
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`${action}失败`);
    }
  }
};

// 删除用户
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 "${row.username}" 吗？此操作不可恢复！`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );

    await deleteUser(row.id);
    ElMessage.success('删除成功');
    getUserListData();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败');
    }
  }
};

// 关闭对话框
const handleClose = () => {
  dialogVisible.value = false;
  currentUser.value = null;
};

// 格式化时间
const formatTime = (time) => {
  return time ? dayjs(time).format('YYYY-MM-DD HH:mm:ss') : '-';
};

// 页面加载时获取数据
onMounted(() => {
  getUserListData();
});
</script>

<style lang="scss" scoped>
.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding: 20px;
}

.user-detail {
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
