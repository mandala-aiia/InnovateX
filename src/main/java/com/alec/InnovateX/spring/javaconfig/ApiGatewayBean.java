package com.alec.InnovateX.spring.javaconfig;

/**
 * 依赖网络设施却不注入它的 Bean：两者之间没有依赖关系可被容器感知，
 * @DependsOn 是它表达"networkInfra 必须先就绪"的唯一途径
 */
public class ApiGatewayBean {

    public ApiGatewayBean() {
        NetworkInfraBean.STARTUP_LOG.add("apiGateway");
        System.out.println("[ApiGatewayBean] 初始化（第 " + NetworkInfraBean.STARTUP_LOG.size() + " 个）");
    }
}
