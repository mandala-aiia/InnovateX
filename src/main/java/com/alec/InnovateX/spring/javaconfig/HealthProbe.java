package com.alec.InnovateX.spring.javaconfig;

/**
 * record 作为"消费者"Bean：唯一一个规范构造器就是注入点，
 * 不写 @Autowired 也按构造器隐式绑定（单构造器规则对 record 同样生效）
 */
public record HealthProbe(ServerEndpoint endpoint) {

    public String report() {
        return "http://" + endpoint.url() + "/up";
    }
}
