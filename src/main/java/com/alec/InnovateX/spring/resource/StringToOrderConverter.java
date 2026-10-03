package com.alec.InnovateX.spring.resource;

import org.springframework.core.convert.converter.Converter;

/**
 * ConversionService 体系的自定义转换器：把 "SO-1001,88" 格式的字符串转成 OrderRecord。
 * 相比老 PropertyEditor：无状态、线程安全、按"源类型->目标类型"对注册，可全局复用
 */
public class StringToOrderConverter implements Converter<String, OrderRecord> {

    @Override
    public OrderRecord convert(String source) {
        String[] parts = source.split(",");
        OrderRecord record = new OrderRecord();
        record.setOrderNo(parts[0]);
        record.setAmount(Integer.parseInt(parts[1]));
        return record;
    }
}
