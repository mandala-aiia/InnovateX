package com.alec.InnovateX.spring.xmladvanced;

/** 工厂方法创建的产物 */
public class XmlFactoryProduct {

    private final String from;

    public XmlFactoryProduct(String from) {
        this.from = from;
        System.out.println("[XmlFactoryProduct] 由 " + from + " 创建 @" + Integer.toHexString(System.identityHashCode(this)));
    }

    public String getFrom() {
        return from;
    }
}
