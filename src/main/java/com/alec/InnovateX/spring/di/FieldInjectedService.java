package com.alec.InnovateX.spring.di;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * 字段注入：反射直接写字段。演示用——实际不推荐：
 * 绕过构造器导致依赖不可见、脱离容器无法 new 出可测试对象、final 用不了。
 */
public class FieldInjectedService {

    @Autowired
    private CatalogService catalogService;

    public String quote(String item) {
        return catalogService.priceOf(item);
    }
}
