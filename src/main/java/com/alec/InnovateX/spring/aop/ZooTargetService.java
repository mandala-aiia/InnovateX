package com.alec.InnovateX.spring.aop;

/**
 * 切点表达式动物园的目标类：不同表达式从不同维度匹配方法
 * - within(类型)：限定在某个类型（及其子类）内的所有方法
 * - target(接口)：被代理对象实现某接口的所有方法
 * - args(参数类型列表)：按方法参数类型匹配
 * - bean(bean名称)：Spring 扩展的表达式，按 bean 名称匹配
 * - @annotation(注解类型)：按方法上标注的注解匹配（见 annotationDemo + AppAnnotationAspect）
 */
public class ZooTargetService {

    public String withinDemo() {
        return "withinDemo";
    }

    public String targetDemo() {
        return "targetDemo";
    }

    public String argsDemo(String name, Integer count) {
        return "argsDemo:" + name + "," + count;
    }

    public String beanDemo() {
        return "beanDemo";
    }

    /** @annotation 切点维度的目标：方法上标注 @AppAnnotation */
    @AppAnnotation("zoo-annotation-demo")
    public String annotationDemo() {
        return "annotationDemo";
    }
}
