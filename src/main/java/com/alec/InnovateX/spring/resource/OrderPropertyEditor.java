package com.alec.InnovateX.spring.resource;

import java.beans.PropertyEditorSupport;

/**
 * 老式属性编辑器（java.beans.PropertyEditor）：String -> Object 的转换，
 * Spring 早期 @Value/XML 属性注入靠它（内置的 CustomNumberEditor、ClassEditor 等）。
 * 有状态、仅适合单线程使用，现已被 ConversionService 体系逐步替代
 */
public class OrderPropertyEditor extends PropertyEditorSupport {

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        // 格式约定：订单号@金额，例如 "SO-777@66"
        String[] parts = text.split("@");
        OrderRecord record = new OrderRecord();
        record.setOrderNo(parts[0]);
        record.setAmount(Integer.parseInt(parts[1]));
        setValue(record);
    }
}
