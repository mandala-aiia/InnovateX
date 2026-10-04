package com.alec.InnovateX.spring.scope;

import java.util.ArrayList;
import java.util.List;

/** 会话购物车：配合 scoped proxy 演示「注入的是代理，每次调用按当前线程路由到不同实例」。 */
public class SessionCart {

    private final List<String> items = new ArrayList<>();

    public void add(String item) {
        items.add(item);
    }

    public int size() {
        return items.size();
    }
}
