package com.alec.InnovateX.spring.scope;

/**
 * prototype 循环依赖的一端：即使改成 setter 注入也救不了——
 * 三级缓存只服务于 singleton；prototype 不进任何缓存，
 * 创建 A 需要 B、创建 B 又需要 A……无限递归，Spring 靠"正在创建"标记及时止损。
 */
public class JobShardA {

    private JobShardB partner;

    public void setPartner(JobShardB partner) {
        this.partner = partner;
    }

    public JobShardB getPartner() {
        return partner;
    }
}
