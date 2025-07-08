# 现金支付和货到付款实现方案

## 概述
本文档描述了在生鲜配送系统中实现现金支付和货到付款功能的完整方案，包括后台记账流程、支付状态管理和前端交互。

## 支付方式说明

### 1. 微信支付 (paymentMethod = 1)
- 在线支付，即时到账
- 支付成功后订单状态更新为已支付

### 2. 现金支付 (paymentMethod = 2)
- 系统预记账，配送时收取现金
- 订单创建时即记录收入，状态更新为已支付
- 适用于用户选择现金支付的场景

### 3. 货到付款 (paymentMethod = 3)
- 预记账模式，配送员确认收款后完成记账
- 订单创建时记录预收入（待确认状态）
- 配送员送达时收取现金并确认收款

## 后端实现

### 核心服务接口

#### CashPaymentService
```java
public interface CashPaymentService {
    // 处理现金支付记账
    boolean processCashPayment(Order order);
    
    // 处理货到付款预记账
    boolean processCashOnDelivery(Order order);
    
    // 确认货到付款收款
    boolean confirmCashOnDeliveryPayment(String orderNo, Double actualAmount);
    
    // 查询现金支付记录
    Object getCashPaymentRecord(String orderNo);
    
    // 撤销现金支付记账
    boolean reverseCashPayment(String orderNo);
}
```

### 支付流程处理

#### OrderController 支付接口
- **路径**: `POST /order/{id}/pay`
- **功能**: 根据订单的支付方式进行相应处理

**现金支付流程**:
1. 调用 `cashPaymentService.processCashPayment(order)`
2. 更新订单状态为已支付 (status = 2)
3. 返回支付成功信息

**货到付款流程**:
1. 调用 `cashPaymentService.processCashOnDelivery(order)`
2. 更新订单状态为待发货 (status = 3)
3. 返回确认信息，标记为配送时付款

#### 货到付款确认接口
- **路径**: `POST /order/{id}/confirm-cash-payment`
- **功能**: 配送员确认收款
- **参数**:
  - `actualAmount`: 实际收款金额
  - `deliveryPersonId`: 配送员ID

**确认流程**:
1. 验证订单为货到付款类型
2. 调用 `confirmCashOnDeliveryPayment(orderNo, actualAmount)`
3. 更新订单状态为已完成 (status = 5)
4. 返回确认结果

### 记账逻辑

#### 现金支付记账
```java
public boolean processCashPayment(Order order) {
    // 1. 记录现金收入
    recordCashIncome(order);
    
    // 2. 记录支付日志
    recordPaymentLog(order);
    
    return true;
}
```

#### 货到付款预记账
```java
public boolean processCashOnDelivery(Order order) {
    // 1. 记录货到付款预收入（待确认状态）
    recordCashOnDeliveryIncome(order);
    
    // 2. 记录货到付款日志
    recordCashOnDeliveryLog(order);
    
    return true;
}
```

#### 货到付款确认
```java
public boolean confirmCashOnDeliveryPayment(String orderNo, Double actualAmount) {
    // 1. 确认收款记录
    confirmCashOnDeliveryIncome(orderNo, actualAmount);
    
    // 2. 记录确认日志
    recordCashOnDeliveryConfirmLog(orderNo, actualAmount);
    
    return true;
}
```

## 前端实现

### 支付页面处理

#### 支付方式判断
```javascript
if (order.paymentMethod === 1) {
    // 微信支付
    await this.handleWechatPay();
} else if (order.paymentMethod === 2) {
    // 现金支付
    await this.handleCashPay();
} else if (order.paymentMethod === 3) {
    // 货到付款
    await this.handleCashOnDelivery();
}
```

#### 现金支付处理
```javascript
async handleCashPay() {
    const res = await app.request({
        url: `/order/${this.data.orderId}/pay`,
        method: 'POST',
        data: { payType: 2 }
    });
    
    if (res.data && res.data.status === 'success') {
        wx.showModal({
            title: '现金支付',
            content: res.data.message,
            showCancel: false,
            success: () => {
                wx.redirectTo({ url: '/pages/order/list' });
            }
        });
    }
}
```

#### 货到付款处理
```javascript
async handleCashOnDelivery() {
    const res = await app.request({
        url: `/order/${this.data.orderId}/pay`,
        method: 'POST',
        data: { payType: 3 }
    });
    
    if (res.data && res.data.status === 'success') {
        wx.showModal({
            title: '货到付款',
            content: res.data.message,
            showCancel: false,
            success: () => {
                wx.redirectTo({ url: '/pages/order/list' });
            }
        });
    }
}
```

## 订单状态流转

### 现金支付订单
1. **PENDING_PAYMENT** (待支付) → **PAID** (已支付)
2. **PAID** → **SHIPPED** (已发货)
3. **SHIPPED** → **DELIVERED** (已送达)
4. **DELIVERED** → **COMPLETED** (已完成)

### 货到付款订单
1. **PENDING_PAYMENT** (待支付) → **PENDING_SHIPMENT** (待发货)
2. **PENDING_SHIPMENT** → **SHIPPED** (已发货)
3. **SHIPPED** → **DELIVERED** (已送达)
4. **DELIVERED** → **COMPLETED** (已完成，确认收款后)

## 财务记录

### 日志记录内容
- 订单号
- 支付方式
- 金额信息
- 操作时间
- 操作状态
- 配送员信息（货到付款）

### 记账类型
1. **即时记账**: 现金支付
2. **预记账**: 货到付款预收入
3. **确认记账**: 货到付款实际收款
4. **撤销记账**: 订单取消或退款

## 安全考虑

1. **权限验证**: 确认收款接口需要配送员权限
2. **金额校验**: 实际收款金额与订单金额的差异处理
3. **状态检查**: 确保订单状态正确才能进行相应操作
4. **日志审计**: 完整记录所有财务操作

## 扩展功能

1. **找零处理**: 支持实际收款大于订单金额的情况
2. **部分收款**: 支持分期或部分付款
3. **退款处理**: 现金支付和货到付款的退款流程
4. **财务报表**: 现金收入统计和分析

## 测试建议

1. **单元测试**: 各个支付方法的逻辑测试
2. **集成测试**: 完整支付流程测试
3. **边界测试**: 异常金额、状态等边界情况
4. **压力测试**: 高并发支付场景测试

## 部署注意事项

1. 确保数据库支持事务处理
2. 配置适当的日志级别
3. 监控财务数据一致性
4. 定期备份财务记录

---

**实施状态**: ✅ 已完成
**最后更新**: 2024年当前时间
**负责人**: 系统开发团队