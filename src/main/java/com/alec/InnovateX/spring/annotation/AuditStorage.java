package com.alec.InnovateX.spring.annotation;

/**
 * 审计存储抽象类：与接口形成对照——抽象类同样可以有多个实现，
 * 同样走按类型装配的全套规则（@Primary/@Qualifier/自定义限定符/集合注入）。
 * write() 是模板方法（抽象类的价值所在）：公共前缀写死，落库引擎细节交给子类
 */
public abstract class AuditStorage {

    /** 存储引擎名：测试断言"注入了哪个子类"的依据 */
    public abstract String engine();

    /** 模板方法：子类只需实现 engine()，即可复用统一的落库格式 */
    public String write(String record) {
        return "[" + engine() + "] 审计落库: " + record;
    }
}
