package com.alec.InnovateX.spring.xmladvanced;

/**
 * bean 继承的子类：parent="xmlParent" 让它"继承"父 bean 定义里的属性值，
 * 自己的 <property> 可以覆盖。注意这不是 Java 类继承，而是 BeanDefinition 的元数据继承
 */
public class XmlChildBean {

    private String name;

    private String shared;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShared() {
        return shared;
    }

    public void setShared(String shared) {
        this.shared = shared;
    }
}
