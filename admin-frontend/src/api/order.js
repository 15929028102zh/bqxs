import request from '@/utils/request';

// 获取订单列表
export function getOrderList(params) {
  return request({
    url: '/admin/order/list',
    method: 'get',
    params,
  });
}

// 获取订单详情
export function getOrderDetail(id) {
  return request({
    url: `/admin/order/${id}`,
    method: 'get',
  });
}

// 更新订单状态
export function updateOrderStatus(id, status) {
  return request({
    url: `/admin/order/${id}/status`,
    method: 'put',
    data: { status },
  });
}

// 取消订单
export function cancelOrder(id, reason) {
  return request({
    url: `/admin/order/${id}/cancel`,
    method: 'put',
    data: { reason },
  });
}

// 发货
export function shipOrder(id, data) {
  return request({
    url: `/admin/order/${id}/ship`,
    method: 'put',
    data,
  });
}

// 确认收货
export function confirmOrder(id) {
  return request({
    url: `/admin/order/${id}/confirm`,
    method: 'put',
  });
}

// 获取订单统计
export function getOrderStats(params) {
  return request({
    url: '/admin/order/stats',
    method: 'get',
    params,
  });
}

// 导出订单
export function exportOrders(params) {
  return request({
    url: '/admin/order/export',
    method: 'get',
    params,
    responseType: 'blob',
  });
}
