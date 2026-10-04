package com.alec.InnovateX.spring.javaconfig;

/** 由 ImportSelector 的返回值"批量选中"后注册的 Bean */
public class SelectorPickedBean {

    public String describe() {
        return "我是被 ImportSelector 选中的 Bean";
    }
}
