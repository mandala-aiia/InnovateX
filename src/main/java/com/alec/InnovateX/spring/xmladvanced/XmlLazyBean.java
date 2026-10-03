package com.alec.InnovateX.spring.xmladvanced;

/** lazy-init 演示：refresh 后不实例化，第一次 getBean 才创建 */
public class XmlLazyBean {

    public static volatile boolean instantiated = false;

    public XmlLazyBean() {
        instantiated = true;
        System.out.println("[XmlLazyBean] 第一次 getBean 才实例化");
    }
}
