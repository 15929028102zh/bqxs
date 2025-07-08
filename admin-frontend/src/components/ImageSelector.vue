<template>
  <div class="image-selector">
    <!-- 搜索和筛选 -->
    <div class="selector-header">
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="搜索">
          <el-input
            v-model="queryParams.keyword"
            placeholder="搜索图片名称"
            clearable
            style="width: 200px"
            @input="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="类型">
          <el-select
            v-model="queryParams.type"
            placeholder="图片类型"
            clearable
            style="width: 150px"
            @change="handleSearch"
          >
            <el-option label="商品图片" value="product" />
            <el-option label="分类图片" value="category" />
            <el-option label="轮播图" value="banner" />
            <el-option label="系统图片" value="system" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间">
          <el-date-picker
            v-model="queryParams.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            @change="handleSearch"
          />
        </el-form-item>
      </el-form>
      
      <div class="selector-actions">
        <el-upload
          :action="uploadUrl"
          :headers="uploadHeaders"
          :show-file-list="false"
          :on-success="handleUploadSuccess"
          :before-upload="beforeUpload"
          multiple
          accept="image/*"
        >
          <el-button type="primary">
            <el-icon><Upload /></el-icon>
            上传图片
          </el-button>
        </el-upload>
        
        <el-button
          v-if="selectedImages.length > 0"
          type="danger"
          @click="handleBatchDelete"
        >
          <el-icon><Delete /></el-icon>
          删除选中 ({{ selectedImages.length }})
        </el-button>
      </div>
    </div>

    <!-- 图片列表 -->
    <div class="image-list" v-loading="loading">
      <div class="image-grid">
        <div
          v-for="image in imageList"
          :key="image.id"
          :class="[
            'image-item',
            { 'is-selected': isSelected(image.url) }
          ]"
          @click="handleImageClick(image)"
        >
          <div class="image-wrapper">
            <el-image
              :src="image.url"
              :preview-src-list="[image.url]"
              fit="cover"
              class="image"
              @click.stop="handlePreview(image)"
            />
            
            <!-- 选择标记 -->
            <div class="selection-mark" v-if="isSelected(image.url)">
              <el-icon><Check /></el-icon>
            </div>
            
            <!-- 图片信息覆盖层 -->
            <div class="image-overlay">
              <div class="image-info">
                <div class="image-name" :title="image.name">{{ image.name }}</div>
                <div class="image-meta">
                  <span class="image-size">{{ formatFileSize(image.size) }}</span>
                  <span class="image-date">{{ formatDate(image.createTime) }}</span>
                </div>
              </div>
              
              <div class="image-actions">
                <el-button
                  type="primary"
                  size="small"
                  circle
                  @click.stop="handlePreview()"
                >
                  <el-icon><ZoomIn /></el-icon>
                </el-button>
                
                <el-button
                  type="danger"
                  size="small"
                  circle
                  @click.stop="handleDelete(image)"
                >
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 空状态 -->
      <el-empty v-if="!loading && imageList.length === 0" description="暂无图片" />
    </div>

    <!-- 分页 -->
    <div class="pagination-container" v-if="total > 0">
      <el-pagination
        v-model:current-page="queryParams.page"
        v-model:page-size="queryParams.size"
        :page-sizes="[20, 40, 60, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 底部操作栏 -->
    <div class="selector-footer">
      <div class="selected-info">
        已选择 {{ selectedUrls.length }} 张图片
        <span v-if="maxCount > 0">（最多可选 {{ maxCount }} 张）</span>
      </div>
      
      <div class="footer-actions">
        <el-button @click="handleCancel">取消</el-button>
        <el-button
          type="primary"
          :disabled="selectedUrls.length === 0"
          @click="handleConfirm"
        >
          确定选择
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getImageList, deleteImage, batchDeleteImages } from '@/api/upload';
import { useUserStore } from '@/stores/user';
import dayjs from 'dayjs';

const props = defineProps({
  // 是否多选
  multiple: {
    type: Boolean,
    default: true
  },
  // 最大选择数量
  maxCount: {
    type: Number,
    default: 0 // 0表示不限制
  },
  // 已选择的图片URL
  selected: {
    type: Array,
    default: () => []
  }
});

const emit = defineEmits(['confirm', 'cancel']);

const userStore = useUserStore();
const loading = ref(false);
const imageList = ref([]);
const total = ref(0);
const selectedUrls = ref([...props.selected]);

// 查询参数
const queryParams = reactive({
  page: 1,
  size: 20,
  keyword: '',
  type: '',
  dateRange: []
});

// 计算属性
const uploadUrl = computed(() => '/api/upload/image');
const uploadHeaders = computed(() => ({
  Authorization: userStore.token
}));

const selectedImages = computed(() => {
  return imageList.value.filter(image => selectedUrls.value.includes(image.url));
});

// 获取图片列表
const getImageListData = async () => {
  loading.value = true;
  try {
    const params = { ...queryParams };
    if (params.dateRange && params.dateRange.length === 2) {
      params.startDate = params.dateRange[0];
      params.endDate = params.dateRange[1];
    }
    delete params.dateRange;
    
    const response = await getImageList(params);
    imageList.value = response.data.records || [];
    total.value = response.data.total || 0;
  } catch (error) {
    ElMessage.error('获取图片列表失败');
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  queryParams.page = 1;
  getImageListData();
};

// 分页大小改变
const handleSizeChange = (val) => {
  queryParams.size = val;
  getImageListData();
};

// 当前页改变
const handleCurrentChange = (val) => {
  queryParams.page = val;
  getImageListData();
};

// 判断图片是否被选中
const isSelected = (url) => {
  return selectedUrls.value.includes(url);
};

// 点击图片
const handleImageClick = (image) => {
  if (props.multiple) {
    const index = selectedUrls.value.indexOf(image.url);
    if (index > -1) {
      selectedUrls.value.splice(index, 1);
    } else {
      if (props.maxCount > 0 && selectedUrls.value.length >= props.maxCount) {
        ElMessage.warning(`最多只能选择 ${props.maxCount} 张图片`);
        return;
      }
      selectedUrls.value.push(image.url);
    }
  } else {
    selectedUrls.value = [image.url];
  }
};

// 预览图片
const handlePreview = () => {
  // Element Plus 的图片预览会自动处理
};

// 删除图片
const handleDelete = async (image) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除图片 "${image.name}" 吗？此操作不可恢复！`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );
    
    await deleteImage(image.url);
    ElMessage.success('删除成功');
    
    // 从选中列表中移除
    const index = selectedUrls.value.indexOf(image.url);
    if (index > -1) {
      selectedUrls.value.splice(index, 1);
    }
    
    getImageListData();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败');
    }
  }
};

// 批量删除
const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedImages.value.length} 张图片吗？此操作不可恢复！`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );
    
    const urls = selectedImages.value.map(img => img.url);
    await batchDeleteImages(urls);
    ElMessage.success('批量删除成功');
    
    // 清空选中列表
    selectedUrls.value = [];
    
    getImageListData();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败');
    }
  }
};

// 上传前验证
const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/');
  if (!isImage) {
    ElMessage.error('只能上传图片文件');
    return false;
  }
  
  const isLt5M = file.size / 1024 / 1024 < 5;
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB');
    return false;
  }
  
  return true;
};

// 上传成功
const handleUploadSuccess = (response) => {
  if (response.code === 200) {
    ElMessage.success('上传成功');
    getImageListData();
  } else {
    ElMessage.error(response.message || '上传失败');
  }
};

// 确认选择
const handleConfirm = () => {
  emit('confirm', selectedUrls.value);
};

// 取消选择
const handleCancel = () => {
  emit('cancel');
};

// 格式化文件大小
const formatFileSize = (size) => {
  if (!size) return '-';
  const units = ['B', 'KB', 'MB', 'GB'];
  let index = 0;
  while (size >= 1024 && index < units.length - 1) {
    size /= 1024;
    index++;
  }
  return `${size.toFixed(1)} ${units[index]}`;
};

// 格式化日期
const formatDate = (date) => {
  return date ? dayjs(date).format('MM-DD') : '-';
};

// 页面加载时获取数据
onMounted(() => {
  getImageListData();
});
</script>

<style lang="scss" scoped>
.image-selector {
  .selector-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 20px;
    padding-bottom: 16px;
    border-bottom: 1px solid #ebeef5;
    
    .search-form {
      flex: 1;
      margin-right: 20px;
    }
    
    .selector-actions {
      display: flex;
      gap: 12px;
    }
  }
  
  .image-list {
    min-height: 400px;
    margin-bottom: 20px;
    
    .image-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
      gap: 16px;
    }
    
    .image-item {
      position: relative;
      cursor: pointer;
      border-radius: 8px;
      overflow: hidden;
      transition: all 0.3s;
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        
        .image-overlay {
          opacity: 1;
        }
      }
      
      &.is-selected {
        border: 2px solid #409eff;
        
        .selection-mark {
          opacity: 1;
        }
      }
      
      .image-wrapper {
        position: relative;
        width: 100%;
        height: 160px;
        
        .image {
          width: 100%;
          height: 100%;
          border-radius: 6px;
        }
        
        .selection-mark {
          position: absolute;
          top: 8px;
          right: 8px;
          width: 24px;
          height: 24px;
          background: #409eff;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          color: white;
          font-size: 14px;
          opacity: 0;
          transition: opacity 0.3s;
        }
        
        .image-overlay {
          position: absolute;
          top: 0;
          left: 0;
          right: 0;
          bottom: 0;
          background: linear-gradient(
            to bottom,
            rgba(0, 0, 0, 0) 0%,
            rgba(0, 0, 0, 0.7) 100%
          );
          display: flex;
          flex-direction: column;
          justify-content: space-between;
          padding: 12px;
          opacity: 0;
          transition: opacity 0.3s;
          border-radius: 6px;
          
          .image-info {
            .image-name {
              color: white;
              font-size: 14px;
              font-weight: 500;
              margin-bottom: 4px;
              overflow: hidden;
              text-overflow: ellipsis;
              white-space: nowrap;
            }
            
            .image-meta {
              display: flex;
              justify-content: space-between;
              color: rgba(255, 255, 255, 0.8);
              font-size: 12px;
            }
          }
          
          .image-actions {
            display: flex;
            justify-content: center;
            gap: 8px;
          }
        }
      }
    }
  }
  
  .pagination-container {
    display: flex;
    justify-content: center;
    margin-bottom: 20px;
  }
  
  .selector-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 16px;
    border-top: 1px solid #ebeef5;
    
    .selected-info {
      color: #606266;
      font-size: 14px;
    }
    
    .footer-actions {
      display: flex;
      gap: 12px;
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .image-selector {
    .selector-header {
      flex-direction: column;
      
      .search-form {
        margin-right: 0;
        margin-bottom: 16px;
      }
    }
    
    .image-list {
      .image-grid {
        grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
        gap: 12px;
      }
      
      .image-item .image-wrapper {
        height: 120px;
      }
    }
    
    .selector-footer {
      flex-direction: column;
      gap: 12px;
      
      .selected-info {
        text-align: center;
      }
    }
  }
}
</style>