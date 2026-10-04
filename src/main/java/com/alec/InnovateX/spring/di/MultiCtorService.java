package com.alec.InnovateX.spring.di;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * 多构造器场景：容器无法替你选择时，必须在目标构造器上标 @Autowired；
 * 只有无参构造兜底存在且无标注时才会退回无参构造。
 */
public class MultiCtorService {

    private final String label;

    public MultiCtorService() {
        this.label = "无参构造";
    }

    @Autowired
    public MultiCtorService(CatalogService catalogService) {
        this.label = "目录构造:" + catalogService.priceOf("书");
    }

    public String label() {
        return label;
    }
}
