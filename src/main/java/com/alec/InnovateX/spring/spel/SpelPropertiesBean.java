package com.alec.InnovateX.spring.spel;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** @Value 中的 SpEL：#{...} 里写表达式（与 ${} 占位符可混用） */
@Component
public class SpelPropertiesBean {

    @Value("#{2 * 21}")
    private int computed;

    @Value("#{T(java.lang.Math).max(10, 99)}")
    private int staticCall;

    @Value("#{systemProperties['user.language'] ?: 'unknown'}")
    private String systemProperty;

    @Value("#{@spelPropertiesBean.computed + 1}")
    private int selfReference;

    public int getComputed() {
        return computed;
    }

    public int getStaticCall() {
        return staticCall;
    }

    public String getSystemProperty() {
        return systemProperty;
    }

    public int getSelfReference() {
        return selfReference;
    }
}
