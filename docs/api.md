# 边墙鲜送 API 接口文档

## 接口概述

### 基础信息
- **接口地址**: `http://localhost:8080/api`
- **接口协议**: HTTP/HTTPS
- **数据格式**: JSON
- **字符编码**: UTF-8
- **认证方式**: JWT Token

### 通用响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1640995200000
}
```

### 状态码说明

| 状态码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 400 | 参数错误 |
| 401 | 未授权 |
| 403 | 禁止访问 |
| 404 | 资源不存在 |
| 500 | 系统异常 |
| 4001 | Token无效 |
| 4002 | Token已过期 |
| 5001 | 用户不存在 |
| 6001 | 商品不存在 |
| 7001 | 订单不存在 |

## 用户相关接口

### 1. 微信登录

**接口地址**: `POST /user/login`

**请求参数**:
```json
{
  "code": "微信登录code",
  "userInfo": {
    "nickName": "用户昵称",
    "avatarUrl": "用户头像",
    "gender": 1
  }
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userInfo": {
      "id": 1,
      "nickname": "用户昵称",
      "avatar": "用户头像",
      "phone": "13800138000"
    }
  }
}
```

### 2. 获取用户信息

**接口地址**: `GET /user/info`

**请求头**:
```
Authorization: Bearer {token}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": 1,
    "nickname": "用户昵称",
    "avatar": "用户头像",
    "phone": "13800138000",
    "gender": 1,
    "createTime": "2024-01-01 12:00:00"
  }
}
```

### 3. 更新用户信息

**接口地址**: `PUT /user/update`

**请求参数**:
```json
{
  "nickname": "新昵称",
  "avatar": "新头像"
}
```

### 4. 绑定手机号

**接口地址**: `POST /user/bind-phone`

**请求参数**:
```json
{
  "phone": "13800138000",
  "code": "验证码"
}
```

### 5. 发送验证码

**接口地址**: `POST /user/send-code`

**请求参数**:
```json
{
  "phone": "13800138000"
}
```

## 商品相关接口

### 1. 获取商品列表

**接口地址**: `GET /product/list`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页数量，默认10 |
| categoryId | Long | 否 | 分类ID |
| keyword | String | 否 | 搜索关键词 |
| sortBy | String | 否 | 排序字段：price/sales/createTime |
| sortOrder | String | 否 | 排序方式：asc/desc |

**响应数据**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "records": [
      {
        "id": 1,
        "name": "新鲜白菜",
        "description": "新鲜优质大白菜",
        "images": ["/images/products/baicai.jpg"],
        "price": 3.50,
        "originalPrice": 4.00,
        "specification": "500g/份",
        "origin": "山东",
        "stock": 100,
        "sales": 50,
        "isRecommend": 1
      }
    ],
    "total": 100,
    "size": 10,
    "current": 1,
    "pages": 10
  }
}
```

### 2. 获取商品详情

**接口地址**: `GET /product/{id}`

**响应数据**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": 1,
    "name": "新鲜白菜",
    "description": "新鲜优质大白菜，口感清脆甜美",
    "images": ["/images/products/baicai1.jpg", "/images/products/baicai2.jpg"],
    "price": 3.50,
    "originalPrice": 4.00,
    "specification": "500g/份",
    "origin": "山东",
    "categoryId": 3,
    "categoryName": "叶菜类",
    "stock": 100,
    "sales": 50,
    "reviews": [
      {
        "id": 1,
        "userId": 1,
        "userName": "用户昵称",
        "productRating": 5,
        "serviceRating": 5,
        "content": "商品很新鲜，服务很好",
        "createTime": "2024-01-01 12:00:00"
      }
    ]
  }
}
```

### 3. 搜索商品

**接口地址**: `GET /product/search`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| keyword | String | 是 | 搜索关键词 |
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页数量，默认10 |

### 4. 获取推荐商品

**接口地址**: `GET /product/recommend`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| limit | Integer | 否 | 数量限制，默认10 |

### 5. 获取热销商品

**接口地址**: `GET /product/hot`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| limit | Integer | 否 | 数量限制，默认10 |

## 分类相关接口

### 1. 获取分类列表

**接口地址**: `GET /category/list`

**响应数据**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "id": 1,
      "name": "蔬菜类",
      "parentId": 0,
      "icon": "/images/category/vegetable.png",
      "children": [
        {
          "id": 3,
          "name": "叶菜类",
          "parentId": 1,
          "icon": "/images/category/leafy.png"
        }
      ]
    }
  ]
}
```

## 购物车相关接口

### 1. 添加商品到购物车

**接口地址**: `POST /cart/add`

**请求参数**:
```json
{
  "productId": 1,
  "quantity": 2
}
```

### 2. 获取购物车列表

**接口地址**: `GET /cart/list`

**响应数据**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "id": 1,
      "productId": 1,
      "productName": "新鲜白菜",
      "productImage": "/images/products/baicai.jpg",
      "productPrice": 3.50,
      "quantity": 2,
      "totalPrice": 7.00,
      "stock": 100
    }
  ]
}
```

### 3. 更新购物车商品数量

**接口地址**: `PUT /cart/update`

**请求参数**:
```json
{
  "id": 1,
  "quantity": 3
}
```

### 4. 删除购物车商品

**接口地址**: `DELETE /cart/delete/{id}`

### 5. 清空购物车

**接口地址**: `DELETE /cart/clear`

### 6. 获取购物车数量

**接口地址**: `GET /cart/count`

**响应数据**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": 5
}
```

## 订单相关接口

### 1. 创建订单

**接口地址**: `POST /order/create`

**请求参数**:
```json
{
  "items": [
    {
      "productId": 1,
      "quantity": 2
    }
  ],
  "addressId": 1,
  "deliveryType": 1,
  "remark": "订单备注"
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "orderId": 1,
    "orderNo": "202401010001",
    "totalAmount": 10.50,
    "payAmount": 10.50
  }
}
```

### 2. 获取订单列表

**接口地址**: `GET /order/list`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页数量，默认10 |
| status | Integer | 否 | 订单状态 |

**响应数据**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "records": [
      {
        "id": 1,
        "orderNo": "202401010001",
        "status": 1,
        "statusText": "待处理",
        "totalAmount": 10.50,
        "payAmount": 10.50,
        "createTime": "2024-01-01 12:00:00",
        "items": [
          {
            "productId": 1,
            "productName": "新鲜白菜",
            "productImage": "/images/products/baicai.jpg",
            "productPrice": 3.50,
            "quantity": 2,
            "totalPrice": 7.00
          }
        ]
      }
    ],
    "total": 50,
    "current": 1,
    "pages": 5
  }
}
```

### 3. 获取订单详情

**接口地址**: `GET /order/{id}`

### 4. 取消订单

**接口地址**: `PUT /order/{id}/cancel`

**请求参数**:
```json
{
  "reason": "取消原因"
}
```

### 5. 确认收货

**接口地址**: `PUT /order/{id}/confirm`

### 6. 订单支付

**接口地址**: `POST /order/{id}/pay`

**请求参数**:
```json
{
  "payType": 1
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "支付成功",
  "data": {
    "payInfo": {
      "appId": "微信小程序AppId",
      "timeStamp": "时间戳",
      "nonceStr": "随机字符串",
      "package": "prepay_id=xxx",
      "signType": "MD5",
      "paySign": "签名"
    }
  }
}
```

## 地址相关接口

### 1. 获取地址列表

**接口地址**: `GET /address/list`

**响应数据**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "id": 1,
      "receiverName": "张三",
      "receiverPhone": "13800138000",
      "province": "陕西省",
      "city": "榆林市",
      "district": "榆阳区",
      "detailAddress": "边墙小区1号楼101",
      "isDefault": 1
    }
  ]
}
```

### 2. 添加地址

**接口地址**: `POST /address/add`

**请求参数**:
```json
{
  "receiverName": "张三",
  "receiverPhone": "13800138000",
  "province": "陕西省",
  "city": "榆林市",
  "district": "榆阳区",
  "detailAddress": "边墙小区1号楼101",
  "isDefault": 1
}
```

### 3. 更新地址

**接口地址**: `PUT /address/{id}`

### 4. 删除地址

**接口地址**: `DELETE /address/{id}`

### 5. 设置默认地址

**接口地址**: `PUT /address/{id}/default`

## 评价相关接口

### 1. 添加商品评价

**接口地址**: `POST /review/add`

**请求参数**:
```json
{
  "orderId": 1,
  "productId": 1,
  "productRating": 5,
  "serviceRating": 5,
  "content": "商品很新鲜，服务很好",
  "images": ["/images/review/1.jpg"]
}
```

### 2. 获取商品评价列表

**接口地址**: `GET /review/list`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| productId | Long | 是 | 商品ID |
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页数量，默认10 |

## 管理后台接口

### 1. 管理员登录

**接口地址**: `POST /admin/login`

**请求参数**:
```json
{
  "username": "admin",
  "password": "123456"
}
```

### 2. 商品管理

#### 添加商品
**接口地址**: `POST /admin/product/add`

#### 更新商品
**接口地址**: `PUT /admin/product/{id}`

#### 删除商品
**接口地址**: `DELETE /admin/product/{id}`

#### 商品上架/下架
**接口地址**: `PUT /admin/product/{id}/status`

### 3. 订单管理

#### 获取订单列表
**接口地址**: `GET /admin/order/list`

#### 更新订单状态
**接口地址**: `PUT /admin/order/{id}/status`

**请求参数**:
```json
{
  "status": 2
}
```

### 4. 用户管理

#### 获取用户列表
**接口地址**: `GET /admin/user/list`

#### 禁用/启用用户
**接口地址**: `PUT /admin/user/{id}/status`

### 5. 数据统计

#### 获取统计概览
**接口地址**: `GET /admin/statistics/overview`

**响应数据**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "todayOrders": 100,
    "todayAmount": 5000.00,
    "totalUsers": 1000,
    "totalProducts": 200,
    "orderTrend": [
      {"date": "2024-01-01", "count": 50, "amount": 2500.00}
    ],
    "categoryStats": [
      {"categoryName": "蔬菜类", "count": 60, "amount": 3000.00}
    ]
  }
}
```

#### 获取销售报表
**接口地址**: `GET /admin/statistics/sales`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| startDate | String | 是 | 开始日期 |
| endDate | String | 是 | 结束日期 |
| type | String | 否 | 统计类型：day/week/month |

## 文件上传接口

### 1. 上传图片

**接口地址**: `POST /upload/image`

**请求参数**: multipart/form-data
- file: 图片文件

**响应数据**:
```json
{
  "code": 200,
  "message": "上传成功",
  "data": {
    "url": "/uploads/images/20240101/xxx.jpg",
    "fullUrl": "http://localhost:8080/uploads/images/20240101/xxx.jpg"
  }
}
```

## 错误处理

### 常见错误码

| 错误码 | 错误信息 | 解决方案 |
|--------|----------|----------|
| 4001 | Token无效 | 重新登录获取Token |
| 4002 | Token已过期 | 刷新Token或重新登录 |
| 5001 | 用户不存在 | 检查用户ID是否正确 |
| 6001 | 商品不存在 | 检查商品ID是否正确 |
| 6003 | 商品库存不足 | 减少购买数量或选择其他商品 |
| 7001 | 订单不存在 | 检查订单ID是否正确 |
| 7002 | 订单状态错误 | 检查订单当前状态 |

### 请求示例

```javascript
// 使用axios发送请求
const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000
});

// 请求拦截器 - 添加Token
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 响应拦截器 - 处理错误
api.interceptors.response.use(
  response => {
    const { code, message, data } = response.data;
    if (code === 200) {
      return data;
    } else {
      throw new Error(message);
    }
  },
  error => {
    if (error.response?.status === 401) {
      // Token失效，跳转到登录页
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// 使用示例
api.get('/product/list').then(data => {
  console.log('商品列表:', data);
}).catch(error => {
  console.error('请求失败:', error.message);
});
```

## 接口测试

### Postman集合

可以导入以下Postman集合进行接口测试：

```json
{
  "info": {
    "name": "边墙鲜送API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "variable": [
    {
      "key": "baseUrl",
      "value": "http://localhost:8080/api"
    },
    {
      "key": "token",
      "value": ""
    }
  ]
}
```

### 接口测试脚本

```bash
#!/bin/bash
# API接口测试脚本

BASE_URL="http://localhost:8080/api"

# 测试健康检查
echo "测试健康检查..."
curl -X GET "$BASE_URL/health"

# 测试商品列表
echo "测试商品列表..."
curl -X GET "$BASE_URL/product/list?page=1&size=10"

# 测试分类列表
echo "测试分类列表..."
curl -X GET "$BASE_URL/category/list"
```

## 版本更新记录

### v1.0.0 (2024-01-01)
- 初始版本发布
- 实现用户、商品、订单、购物车等基础功能
- 支持微信小程序登录
- 支持在线支付

### 后续版本规划
- v1.1.0: 增加优惠券功能
- v1.2.0: 增加积分系统
- v1.3.0: 增加配送跟踪功能
- v2.0.0: 支持多商户模式