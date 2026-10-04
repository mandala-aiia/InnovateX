package com.alec.InnovateX.spring.lifecycle;

/** 原始实现：hello() 返回「原始问候」。 */
public class PlainGreeterService implements GreeterService {

    @Override
    public String hello() {
        return "原始问候";
    }
}
