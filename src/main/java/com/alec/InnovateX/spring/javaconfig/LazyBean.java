package com.alec.InnovateX.spring.javaconfig;

/** 懒加载 Bean：@Lazy 修饰时容器启动不实例化，第一次 getBean 才创建 */
public class LazyBean {

    public static volatile boolean instantiated = false;

    public LazyBean() {
        instantiated = true;
        System.out.println("[LazyBean] 实例化（只有第一次 getBean 才会走到这里）");
    }
}
