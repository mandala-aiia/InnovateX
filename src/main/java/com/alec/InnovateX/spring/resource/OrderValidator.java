package com.alec.InnovateX.spring.resource;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/** 数据校验器：配合 DataBinder 实现"绑定 + 校验"一步完成 */
public class OrderValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return OrderRecord.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ValidationUtils.rejectIfEmpty(errors, "orderNo", "orderNo.empty", "订单号不能为空");
        OrderRecord record = (OrderRecord) target;
        if (record.getAmount() != null && record.getAmount() < 0) {
            errors.rejectValue("amount", "amount.negative", "金额不能为负数");
        }
    }
}
