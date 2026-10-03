package com.alec.InnovateX.spring.mvc;

/** 校验演示的可变 POJO：表单参数按 setter 绑定（record 走构造器绑定，这里特意用经典类） */
public class PersonRequest {

    private String name;

    private Integer age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}
