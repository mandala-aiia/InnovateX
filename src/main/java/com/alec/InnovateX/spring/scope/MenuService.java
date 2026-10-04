package com.alec.InnovateX.spring.scope;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * setter/字段注入循环依赖的一端：菜单服务要问库存，库存服务又要问菜单。
 * <p>
 * 字段注入（@Autowired）发生在"实例化之后、初始化之前"的属性填充阶段——
 * 这正是三级缓存能救的场景：A 可以先拿着"没填满属性的半成品引用"去解围。
 */
public class MenuService {

    @Autowired
    private StockService stockService;

    public StockService getStockService() {
        return stockService;
    }

    public String signature() {
        return "MenuService@" + Integer.toHexString(System.identityHashCode(this));
    }
}
