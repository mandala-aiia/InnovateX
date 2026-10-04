package com.alec.InnovateX.spring.di;

import org.springframework.beans.factory.annotation.Autowired;

/** Setter 循环依赖 A：对象先创建、再互相注入——理论上可以被三级缓存化解。 */
public class SetterA {

    private SetterB b;

    @Autowired
    public void setB(SetterB b) {
        this.b = b;
    }

    public SetterB getB() {
        return b;
    }
}
