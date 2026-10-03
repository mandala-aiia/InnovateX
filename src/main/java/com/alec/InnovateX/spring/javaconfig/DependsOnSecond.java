package com.alec.InnovateX.spring.javaconfig;

/** 通过 @DependsOn 声明对 dependsOnFirst 的隐式依赖 */
public class DependsOnSecond {

    public DependsOnSecond() {
        DependsOnFirst.INIT_ORDER.add("second");
        System.out.println("[DependsOnSecond] 初始化（第 " + DependsOnFirst.INIT_ORDER.size() + " 个）");
    }
}
