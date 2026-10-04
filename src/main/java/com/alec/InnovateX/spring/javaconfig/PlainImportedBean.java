package com.alec.InnovateX.spring.javaconfig;

/** @Import 第一种形式——普通类直接导入：没有任何注解修饰也会被注册，bean 名默认是全限定类名 */
public class PlainImportedBean {

    public String describe() {
        return "我是被 @Import(普通类) 直接导入的，bean 名 = 全限定类名";
    }
}
