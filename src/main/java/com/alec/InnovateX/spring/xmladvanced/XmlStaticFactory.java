package com.alec.InnovateX.spring.xmladvanced;

/** 静态工厂：XML 里 factory-method="create" 指向静态方法，Spring 调它来产出 Bean */
public class XmlStaticFactory {

    private XmlStaticFactory() {
    }

    public static XmlFactoryProduct create() {
        return new XmlFactoryProduct("静态工厂方法");
    }
}
