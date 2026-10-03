package com.alec.InnovateX.spring.javaconfig;

/**
 * @Import 的第一种形式——直接导入普通类：
 * 该类无需任何注解修饰，被导入后以"全限定类名"作为 bean 名称注册进容器
 */
public class ImportedBean {

    public String hello() {
        return "我是被 @Import(普通类) 直接导入的 Bean，bean 名称默认是全限定类名";
    }
}
