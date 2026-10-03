package com.alec.InnovateX.spring.javaconfig;

/**
 * 子容器独有的 Bean：构造参数 SharedService 只在父容器里有定义——
 * 依赖解析时子容器找不到会自动向上委托给父容器（传统 SSM 中 servlet 层注入 root 层服务的原理）
 */
public class ChildService {

    private final SharedService sharedService;

    public ChildService(SharedService sharedService) {
        this.sharedService = sharedService;
    }

    public String describe() {
        return "子容器 Bean 注入了来自" + sharedService.source() + "的共享服务";
    }
}
