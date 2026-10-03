package com.alec.InnovateX.spring.xmladvanced;

/** 实例工厂：先由容器创建工厂 Bean 本身，再用 factory-bean + factory-method 产出目标 Bean */
public class XmlInstanceFactory {

    public XmlFactoryProduct build() {
        return new XmlFactoryProduct("实例工厂方法");
    }
}
