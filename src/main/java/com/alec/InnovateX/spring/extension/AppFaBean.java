package com.alec.InnovateX.spring.extension;


import lombok.Data;

@Data
public class AppFaBean {
    private String AppFaBeanName;

    @Override
    public String toString() {
        return "AppFaBean{" +
                "AppFaBeanName='" + AppFaBeanName + '\'' +
                '}';
    }
}
