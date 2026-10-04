package com.alec.InnovateX.spring.javaconfig;

/** @Profile 演示的公共接口：同一接口的多个实现按激活的 profile 二选一注册 */
public interface DeployService {

    String environment();
}
