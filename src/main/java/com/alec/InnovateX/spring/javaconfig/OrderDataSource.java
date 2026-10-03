package com.alec.InnovateX.spring.javaconfig;

/** 简单数据源 POJO：用于演示 @Configuration full/lite 模式下 @Bean 方法互调的行为差异 */
public class OrderDataSource {

    private final String name;

    public OrderDataSource(String name) {
        this.name = name;
        System.out.println("[OrderDataSource] 创建实例: " + name + " @" + Integer.toHexString(System.identityHashCode(this)));
    }

    public String getName() {
        return name;
    }
}
