import request from '@/utils/request';

// 上传单个图片
export function uploadImage(file) {
  const formData = new FormData();
  formData.append('file', file);
  
  return request({
    url: '/upload/image',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
}

// 批量上传图片
export function uploadImages(files) {
  const formData = new FormData();
  files.forEach(file => {
    formData.append('files', file);
  });
  
  return request({
    url: '/upload/images',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
}

// 获取图片列表
export function getImageList(params) {
  return request({
    url: '/upload/images/list',
    method: 'get',
    params
  });
}

// 删除图片
export function deleteImage(url) {
  return request({
    url: '/upload/images',
    method: 'delete',
    params: { url }
  });
}

// 批量删除图片
export function batchDeleteImages(urls) {
  return request({
    url: '/upload/images/batch',
    method: 'delete',
    data: { urls }
  });
}

// 获取系统配置图片
export function getSystemImages() {
  return request({
    url: '/system/images',
    method: 'get'
  });
}

// 更新系统配置图片
export function updateSystemImage(type, url) {
  return request({
    url: '/system/images',
    method: 'put',
    data: { type, url }
  });
}