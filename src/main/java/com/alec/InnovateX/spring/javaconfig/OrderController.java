package com.alec.InnovateX.spring.javaconfig;

/**
 * 子容器独有的 Bean：构造参数类型 CustomerService 只在父容器里有定义——
 * 依赖解析时子容器找不到会自动向上委托父容器（传统 SSM 里 servlet 层注入 root 层服务的原理）
 */
public class OrderController {

    private final CustomerService customerService;

    public OrderController(CustomerService customerService) {
        this.customerService = customerService;
        System.out.println("[OrderController] 创建，注入了来自" + customerService.origin() + "的 customerService");
    }

    public String describe() {
        return "子容器的 orderController 注入了来自父容器的 customerService";
    }
}
