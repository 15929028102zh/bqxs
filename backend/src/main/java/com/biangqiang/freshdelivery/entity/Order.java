package com.biangqiang.freshdelivery.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类
 *
 * @author biangqiang
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_order")
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 订单编号
     */
    @TableField("order_no")
    private String orderNo;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 用户名
     */
    @TableField(exist = false)
    private String userName;

    /**
     * 用户电话
     */
    @TableField(exist = false)
    private String userPhone;

    /**
     * 订单状态：0-待付款，1-待发货，2-待收货，3-已完成，4-已取消，5-已退款
     */
    @TableField("status")
    private Integer status;

    /**
     * 商品总金额
     */
    @TableField("product_amount")
    private BigDecimal productAmount;

    /**
     * 配送费
     */
    @TableField("delivery_fee")
    private BigDecimal deliveryFee;

    /**
     * 订单总金额
     */
    @TableField("total_amount")
    private BigDecimal totalAmount;

    /**
     * 实付金额
     */
    @TableField("pay_amount")
    private BigDecimal payAmount;

    /**
     * 支付方式：1-微信支付，2-现金支付，3-货到付款
     */
    @TableField("pay_type")
    private Integer payType;

    /**
     * 支付方式（前端显示用）
     */
    @TableField(exist = false)
    private Integer paymentMethod;

    /**
     * 支付状态：0-未支付，1-已支付，2-已退款
     */
    @TableField("pay_status")
    private Integer payStatus;

    /**
     * 支付时间
     */
    @TableField("pay_time")
    private LocalDateTime payTime;

    /**
     * 配送方式：1-送货上门，2-到店自提
     */
    @TableField("delivery_type")
    private Integer deliveryType;

    /**
     * 收货人姓名
     */
    @TableField("receiver_name")
    private String receiverName;

    /**
     * 收货人电话
     */
    @TableField("receiver_phone")
    private String receiverPhone;

    /**
     * 收货地址
     */
    @TableField("receiver_address")
    private String receiverAddress;

    /**
     * 配送地址（前端显示用）
     */
    @TableField(exist = false)
    private String deliveryAddress;

    /**
     * 物流公司
     */
    @TableField(exist = false)
    private String shippingCompany;

    /**
     * 物流单号
     */
    @TableField(exist = false)
    private String trackingNumber;

    /**
     * 发货时间
     */
    @TableField(exist = false)
    private LocalDateTime shippingTime;

    /**
     * 确认收货时间
     */
    @TableField(exist = false)
    private LocalDateTime confirmTime;

    /**
     * 订单备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 完成时间
     */
    @TableField("finish_time")
    private LocalDateTime finishTime;

    /**
     * 取消时间
     */
    @TableField("cancel_time")
    private LocalDateTime cancelTime;

    /**
     * 取消原因
     */
    @TableField("cancel_reason")
    private String cancelReason;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 删除标记：0-未删除，1-已删除
     */
    @TableField("deleted")
    @TableLogic
    private Integer deleted;

    /**
     * 订单项列表（不存储在数据库中）
     */
    @TableField(exist = false)
    private java.util.List<OrderItem> items;

    // 前端显示字段映射
    public String getOrderNumber() {
        return orderNo;
    }

    // 手动添加setter方法以解决编译问题
    public void setId(Long id) {
        this.id = id;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setProductAmount(BigDecimal productAmount) {
        this.productAmount = productAmount;
    }

    public void setDeliveryFee(BigDecimal deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setPayAmount(BigDecimal payAmount) {
        this.payAmount = payAmount;
    }

    public void setPayType(Integer payType) {
        this.payType = payType;
    }

    public void setPaymentMethod(Integer paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public void setPayStatus(Integer payStatus) {
        this.payStatus = payStatus;
    }

    public void setPayTime(LocalDateTime payTime) {
        this.payTime = payTime;
    }

    public void setDeliveryType(Integer deliveryType) {
        this.deliveryType = deliveryType;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = receiverPhone;
    }

    public void setReceiverAddress(String receiverAddress) {
        this.receiverAddress = receiverAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public void setShippingCompany(String shippingCompany) {
        this.shippingCompany = shippingCompany;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public void setShippingTime(LocalDateTime shippingTime) {
        this.shippingTime = shippingTime;
    }

    public void setConfirmTime(LocalDateTime confirmTime) {
        this.confirmTime = confirmTime;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public void setFinishTime(LocalDateTime finishTime) {
        this.finishTime = finishTime;
    }

    public void setCancelTime(LocalDateTime cancelTime) {
        this.cancelTime = cancelTime;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

    public void setItems(java.util.List<OrderItem> items) {
        this.items = items;
    }

    // 手动添加getter方法以解决编译问题
    public Long getId() {
        return id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public Integer getStatus() {
        return status;
    }

    public BigDecimal getProductAmount() {
        return productAmount;
    }

    public BigDecimal getDeliveryFee() {
        return deliveryFee;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public BigDecimal getPayAmount() {
        return payAmount;
    }

    public Integer getPayType() {
        return payType;
    }

    public Integer getPaymentMethod() {
        return paymentMethod;
    }

    public Integer getPayStatus() {
        return payStatus;
    }

    public LocalDateTime getPayTime() {
        return payTime;
    }

    public Integer getDeliveryType() {
        return deliveryType;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public String getReceiverPhone() {
        return receiverPhone;
    }

    public String getReceiverAddress() {
        return receiverAddress;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public String getShippingCompany() {
        return shippingCompany;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public LocalDateTime getShippingTime() {
        return shippingTime;
    }

    public LocalDateTime getConfirmTime() {
        return confirmTime;
    }

    public String getRemark() {
        return remark;
    }

    public LocalDateTime getFinishTime() {
        return finishTime;
    }

    public LocalDateTime getCancelTime() {
        return cancelTime;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public java.util.List<OrderItem> getItems() {
        return items;
    }
}