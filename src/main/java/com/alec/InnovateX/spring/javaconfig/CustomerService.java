package com.alec.InnovateX.spring.javaconfig;

/** 只在父容器注册的服务：origin 标识它来自哪个容器，验证子容器"向上委托"取 Bean */
public class CustomerService {

    private final String origin;

    public CustomerService(String origin) {
        this.origin = origin;
        System.out.println("[CustomerService] 创建，来自" + origin);
    }

    public String origin() {
        return origin;
    }
}
