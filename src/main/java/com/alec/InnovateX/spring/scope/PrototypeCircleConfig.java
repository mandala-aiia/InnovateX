package com.alec.InnovateX.spring.scope;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * prototype 循环依赖：三级缓存只服务于 singleton。prototype 每次 getBean 都现场创建，
 * 创建 A 时需要 B、创建 B 时又需要 A，无限递归，Spring 检测后在第二次创建时抛异常终止
 */
@Configuration
public class PrototypeCircleConfig {

    @Bean
    @org.springframework.context.annotation.Scope("prototype")
    public PrototypeCircleA prototypeCircleA(PrototypeCircleB b) {
        PrototypeCircleA a = new PrototypeCircleA();
        a.setB(b);
        return a;
    }

    @Bean
    @org.springframework.context.annotation.Scope("prototype")
    public PrototypeCircleB prototypeCircleB(PrototypeCircleA a) {
        PrototypeCircleB b = new PrototypeCircleB();
        b.setA(a);
        return b;
    }
}
