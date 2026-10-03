package com.alec.InnovateX.spring.xmladvanced;

import org.springframework.beans.factory.support.MethodReplacer;

import java.lang.reflect.Method;

/** 方法替换器：replaced-method 指定的方法调用会转发到这里 */
public class XmlWorkerReplacer implements MethodReplacer {

    @Override
    public Object reimplement(Object obj, Method method, Object[] args) throws Throwable {
        return "替换后的实现处理: " + args[0] + "（原始对象被彻底绕过）";
    }
}
