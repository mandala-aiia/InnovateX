package com.alec.InnovateX.spring.di;

/** 泛型仓库基类：容器能按泛型参数区分不同实体类型的子类。 */
public class BaseRepo<T> {

    public String typeName() {
        return getClass().getSimpleName();
    }
}
