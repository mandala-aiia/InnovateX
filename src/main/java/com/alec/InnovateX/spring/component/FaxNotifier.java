package com.alec.InnovateX.spring.component;

/** 传真通知器：没有任何注解，只能被 ImportSelector 显式导入成 bean。 */
public class FaxNotifier {

    public String send() {
        return "传真已发送";
    }
}
