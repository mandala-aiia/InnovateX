package com.alec.InnovateX.spring.di;

/** 定价客户端：故意不注册到容器，用于演示「依赖可能不存在」时的四种可选写法。 */
public class PricingClient {

    public String ping() {
        return "pricing-ok";
    }
}
