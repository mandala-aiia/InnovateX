package com.alec.InnovateX.spring.annotation;

/**
 * 指标绑定器：整个容器中故意不提供任何实现——
 * 专门演示"可选依赖"两种姿势：@Autowired(required=false) 注入 null 不报错；
 * ObjectProvider 句柄注入后可优雅判空/兜底
 */
@FunctionalInterface
public interface MetricsBinder {

    String bind();
}
