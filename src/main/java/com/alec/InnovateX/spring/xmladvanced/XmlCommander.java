package com.alec.InnovateX.spring.xmladvanced;

/**
 * lookup-method 方法注入：单例 Commander 需要多次获取 prototype Soldier，
 * 用抽象方法 + <lookup-method> 让容器在运行时动态实现该方法（CGLIB 子类覆盖），
 * 每次调用都向容器 getBean 一次——解决"单例依赖多例"的经典手段
 */
public abstract class XmlCommander {

    public abstract XmlSoldier createSoldier();

    public String recruit() {
        return "征召: " + createSoldier().whoAmI();
    }
}
