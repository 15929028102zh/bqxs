import request from '@/utils/request';

// 管理员登录
export function login(data) {
  return request({
    url: '/admin/login',
    method: 'post',
    data,
  });
}

// 管理员登出
export function logout() {
  return request({
    url: '/admin/logout',
    method: 'post',
  });
}

// 获取管理员信息
export function getUserInfo() {
  return request({
    url: '/admin/info',
    method: 'get',
  });
}

// 获取用户列表
export function getUserList(params) {
  return request({
    url: '/admin/user/list',
    method: 'get',
    params,
  });
}

// 更新用户状态
export function updateUserStatus(id, status) {
  return request({
    url: `/admin/user/${id}/status`,
    method: 'put',
    data: { status },
  });
}

// 删除用户
export function deleteUser(id) {
  return request({
    url: `/admin/user/${id}`,
    method: 'delete',
  });
}
