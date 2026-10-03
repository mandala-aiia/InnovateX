package com.alec.InnovateX.spring.resource;

/** 类型转换/数据绑定的目标对象 */
public class OrderRecord {

    private String orderNo;

    private Integer amount;

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "OrderRecord{orderNo='" + orderNo + "', amount=" + amount + "}";
    }
}
