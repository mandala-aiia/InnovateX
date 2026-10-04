package com.alec.InnovateX.spring.scope;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * 循环依赖的另一端：注入回 MenuService，形成 A→B→A 闭环。
 * 默认（allowCircularReferences=true）容器能用三级缓存解开这个环。
 */
public class StockService {

    @Autowired
    private MenuService menuService;

    public MenuService getMenuService() {
        return menuService;
    }
}
