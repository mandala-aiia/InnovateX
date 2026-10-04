package com.alec.InnovateX.spring.event;

/**
 * POJO 事件：Spring 4.2 起事件对象不再要求继承 ApplicationEvent，任意类型都能发布。
 * 但"发布什么"和"监听器收到什么"在接口式写法里并不一致——
 * 容器会把 POJO 包装成 PayloadApplicationEvent&lt;ProductAddedEvent&gt; 再多播（见 LegacyInterfaceListener）。
 */
public class ProductAddedEvent {

    private final String sku;

    public ProductAddedEvent(String sku) {
        this.sku = sku;
    }

    public String getSku() {
        return sku;
    }
}
