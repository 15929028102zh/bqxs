import request from '@/utils/request';

// 获取商品列表
export function getProductList(params) {
  return request({
    url: '/product/list',
    method: 'get',
    params,
  });
}

// 获取商品详情
export function getProductDetail(id) {
  return request({
    url: `/product/${id}`,
    method: 'get',
  });
}

// 创建商品
export function createProduct(data) {
  return request({
    url: '/product',
    method: 'post',
    data,
  });
}

// 更新商品
export function updateProduct(id, data) {
  return request({
    url: `/product/${id}`,
    method: 'put',
    data,
  });
}

// 删除商品
export function deleteProduct(id) {
  return request({
    url: `/product/${id}`,
    method: 'delete',
  });
}

// 更新商品状态
export function updateProductStatus(id, status) {
  return request({
    url: `/product/${id}/status`,
    method: 'put',
    data: { status },
  });
}

// 批量删除商品
export function batchDeleteProducts(ids) {
  return request({
    url: '/product/batch',
    method: 'delete',
    data: { ids },
  });
}

// 搜索商品
export function searchProducts(params) {
  return request({
    url: '/product/search',
    method: 'get',
    params,
  });
}

// 获取热门商品
export function getHotProducts(params) {
  return request({
    url: '/product/hot',
    method: 'get',
    params,
  });
}

// 获取推荐商品
export function getRecommendProducts(params) {
  return request({
    url: '/product/recommend',
    method: 'get',
    params,
  });
}
