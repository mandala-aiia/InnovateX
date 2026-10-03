package com.alec.InnovateX.spring.xmladvanced;

/** autowire=byName 演示：按 setter 名匹配 bean 名称注入（xmlFactoryProduct -> setXmlFactoryProduct） */
public class XmlAutowireClient {

    private XmlFactoryProduct xmlFactoryProduct;

    public void setXmlFactoryProduct(XmlFactoryProduct xmlFactoryProduct) {
        this.xmlFactoryProduct = xmlFactoryProduct;
    }

    public XmlFactoryProduct getXmlFactoryProduct() {
        return xmlFactoryProduct;
    }
}
