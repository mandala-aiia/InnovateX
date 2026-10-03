package com.alec.InnovateX.spring.scope;

import org.springframework.beans.factory.annotation.Autowired;

public class SetterCircleB {

    @Autowired
    private SetterCircleA a;

    public SetterCircleA getA() {
        return a;
    }
}
