package com.alec.InnovateX.spring.di;

import org.springframework.beans.factory.annotation.Autowired;

/** Setter 循环依赖 B。 */
public class SetterB {

    private SetterA a;

    @Autowired
    public void setA(SetterA a) {
        this.a = a;
    }

    public SetterA getA() {
        return a;
    }
}
