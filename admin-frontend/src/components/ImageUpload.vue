<template>
  <div class="image-upload">
    <el-upload
      ref="uploadRef"
      :class="uploadClass"
      :action="uploadUrl"
      :headers="uploadHeaders"
      :show-file-list="false"
      :on-success="handleSuccess"
      :on-error="handleError"
      :before-upload="beforeUpload"
      :disabled="disabled"
      :accept="accept"
    >
      <!-- 单图上传 -->
      <template v-if="!multiple">
        <div v-if="imageUrl" class="image-preview">
          <el-image
            :src="imageUrl"
            :preview-src-list="[imageUrl]"
            fit="cover"
            class="uploaded-image"
          />
          <div class="image-overlay">
            <el-icon class="preview-icon" @click.stop="handlePreview">
              <ZoomIn />
            </el-icon>
            <el-icon class="delete-icon" @click.stop="handleRemove">
              <Delete />
            </el-icon>
          </div>
        </div>
        <div v-else class="upload-placeholder">
          <el-icon class="upload-icon"><Plus /></el-icon>
          <div class="upload-text">{{ placeholder }}</div>
        </div>
      </template>

      <!-- 多图上传 -->
      <template v-else>
        <div class="multiple-upload">
          <div
            v-for="(url, index) in imageList"
            :key="index"
            class="image-item"
          >
            <el-image
              :src="url"
              :preview-src-list="imageList"
              fit="cover"
              class="uploaded-image"
            />
            <div class="image-overlay">
              <el-icon class="preview-icon" @click.stop="() => handlePreview(index)">
                <ZoomIn />
              </el-icon>
              <el-icon class="delete-icon" @click.stop="() => handleRemoveMultiple(index)">
                <Delete />
              </el-icon>
            </div>
          </div>
          <div v-if="imageList.length < maxCount" class="upload-placeholder">
            <el-icon class="upload-icon"><Plus /></el-icon>
            <div class="upload-text">{{ placeholder }}</div>
          </div>
        </div>
      </template>
    </el-upload>

    <!-- 上传提示 -->
    <div v-if="showTip" class="upload-tip">
      {{ tip }}
    </div>

    <!-- 图片选择器对话框 -->
    <el-dialog
      v-model="selectorVisible"
      title="选择图片"
      width="80%"
      :before-close="handleSelectorClose"
    >
      <ImageSelector
        v-if="selectorVisible"
        :multiple="multiple"
        :max-count="maxCount - (multiple ? imageList.length : 0)"
        @confirm="handleSelectorConfirm"
        @cancel="handleSelectorClose"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { useUserStore } from '@/stores/user';
import ImageSelector from './ImageSelector.vue';

const props = defineProps({
  // 图片URL或URL数组
  modelValue: {
    type: [String, Array],
    default: () => []
  },
  // 是否多选
  multiple: {
    type: Boolean,
    default: false
  },
  // 最大上传数量
  maxCount: {
    type: Number,
    default: 9
  },
  // 最大文件大小(MB)
  maxSize: {
    type: Number,
    default: 5
  },
  // 接受的文件类型
  accept: {
    type: String,
    default: 'image/*'
  },
  // 允许的文件格式
  allowedTypes: {
    type: Array,
    default: () => ['jpg', 'jpeg', 'png', 'gif', 'webp']
  },
  // 是否禁用
  disabled: {
    type: Boolean,
    default: false
  },
  // 占位符文本
  placeholder: {
    type: String,
    default: '点击上传'
  },
  // 提示文本
  tip: {
    type: String,
    default: ''
  },
  // 是否显示提示
  showTip: {
    type: Boolean,
    default: true
  },
  // 图片尺寸
  size: {
    type: String,
    default: 'default', // small, default, large
    validator: (value) => ['small', 'default', 'large'].includes(value)
  },
  // 是否显示图片选择器按钮
  showSelector: {
    type: Boolean,
    default: true
  }
});

const emit = defineEmits(['update:modelValue', 'change', 'success', 'error']);

const userStore = useUserStore();
const uploadRef = ref();
const selectorVisible = ref(false);

// 计算属性
const uploadUrl = computed(() => '/api/upload/image');
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${userStore.token}`
}));

const uploadClass = computed(() => {
  const classes = ['image-uploader'];
  classes.push(`image-uploader--${props.size}`);
  if (props.disabled) classes.push('is-disabled');
  return classes.join(' ');
});

const imageUrl = computed({
  get: () => props.multiple ? '' : props.modelValue,
  set: (value) => emit('update:modelValue', value)
});

const imageList = computed({
  get: () => props.multiple ? (Array.isArray(props.modelValue) ? props.modelValue : []) : [],
  set: (value) => emit('update:modelValue', value)
});

// 上传前验证
const beforeUpload = (file) => {
  // 检查文件类型
  const fileType = file.name.split('.').pop().toLowerCase();
  if (!props.allowedTypes.includes(fileType)) {
    ElMessage.error(`只支持 ${props.allowedTypes.join('、')} 格式的图片`);
    return false;
  }

  // 检查文件大小
  const isLtMaxSize = file.size / 1024 / 1024 < props.maxSize;
  if (!isLtMaxSize) {
    ElMessage.error(`图片大小不能超过 ${props.maxSize}MB`);
    return false;
  }

  // 检查数量限制
  if (props.multiple && imageList.value.length >= props.maxCount) {
    ElMessage.error(`最多只能上传 ${props.maxCount} 张图片`);
    return false;
  }

  return true;
};

// 上传成功
const handleSuccess = (response) => {
  if (response.code === 200) {
    const url = response.data.url;
    
    if (props.multiple) {
      const newList = [...imageList.value, url];
      imageList.value = newList;
    } else {
      imageUrl.value = url;
    }
    
    emit('change', props.multiple ? imageList.value : imageUrl.value);
    emit('success', response);
    ElMessage.success('上传成功');
  } else {
    ElMessage.error(response.message || '上传失败');
    emit('error', response);
  }
};

// 上传失败
const handleError = (error) => {
  ElMessage.error('上传失败，请重试');
  emit('error', error);
};

// 预览图片
const handlePreview = () => {
  // Element Plus 的图片预览会自动处理
};

// 删除单张图片
const handleRemove = () => {
  imageUrl.value = '';
  emit('change', '');
};

// 删除多张图片中的一张
const handleRemoveMultiple = (idx) => {
  const newList = imageList.value.filter((_, i) => i !== idx);
  imageList.value = newList;
  emit('change', newList);
};



// 关闭图片选择器
const handleSelectorClose = () => {
  selectorVisible.value = false;
};

// 图片选择器确认
const handleSelectorConfirm = (selectedImages) => {
  if (props.multiple) {
    const newList = [...imageList.value, ...selectedImages];
    imageList.value = newList;
  } else {
    imageUrl.value = selectedImages[0] || '';
  }
  
  emit('change', props.multiple ? imageList.value : imageUrl.value);
  selectorVisible.value = false;
};
</script>

<style lang="scss" scoped>
.image-upload {
  .image-uploader {
    &--small {
      .upload-placeholder,
      .image-item {
        width: 80px;
        height: 80px;
      }
    }
    
    &--default {
      .upload-placeholder,
      .image-item {
        width: 120px;
        height: 120px;
      }
    }
    
    &--large {
      .upload-placeholder,
      .image-item {
        width: 160px;
        height: 160px;
      }
    }
    
    &.is-disabled {
      .upload-placeholder {
        background-color: #f5f7fa;
        cursor: not-allowed;
      }
    }
  }
  
  .upload-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    border: 2px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    transition: border-color 0.3s;
    
    &:hover {
      border-color: #409eff;
    }
    
    .upload-icon {
      font-size: 28px;
      color: #8c939d;
      margin-bottom: 8px;
    }
    
    .upload-text {
      color: #8c939d;
      font-size: 14px;
    }
  }
  
  .image-preview,
  .image-item {
    position: relative;
    display: inline-block;
    margin-right: 8px;
    margin-bottom: 8px;
    
    .uploaded-image {
      width: 100%;
      height: 100%;
      border-radius: 6px;
      border: 1px solid #d9d9d9;
    }
    
    .image-overlay {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0, 0, 0, 0.5);
      display: flex;
      align-items: center;
      justify-content: center;
      opacity: 0;
      transition: opacity 0.3s;
      border-radius: 6px;
      
      .preview-icon,
      .delete-icon {
        color: white;
        font-size: 20px;
        margin: 0 8px;
        cursor: pointer;
        
        &:hover {
          color: #409eff;
        }
      }
      
      .delete-icon:hover {
        color: #f56c6c;
      }
    }
    
    &:hover .image-overlay {
      opacity: 1;
    }
  }
  
  .multiple-upload {
    display: flex;
    flex-wrap: wrap;
    
    .image-item {
      margin-right: 8px;
      margin-bottom: 8px;
    }
  }
  
  .upload-tip {
    margin-top: 8px;
    color: #909399;
    font-size: 12px;
    line-height: 1.5;
  }
}
</style>