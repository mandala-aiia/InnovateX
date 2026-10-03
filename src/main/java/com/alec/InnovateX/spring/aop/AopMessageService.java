package com.alec.InnovateX.spring.aop;

/** 没有接口的目标类：只能走 CGLIB 子类代理 */
public class AopMessageService {

    public String send(String message) {
        System.out.println("[AopMessageService] send: " + message);
        return "已发送: " + message;
    }
}
