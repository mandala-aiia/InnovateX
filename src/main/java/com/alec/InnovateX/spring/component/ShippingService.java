package com.alec.InnovateX.spring.component;

/** 用自定义组合注解 @BizComponent 声明的组件：类上没有直接的 @Component，扫描器靠元注解识别。 */
@BizComponent
public class ShippingService {

    public String deliver(String city) {
        return "已发货:" + city;
    }
}
