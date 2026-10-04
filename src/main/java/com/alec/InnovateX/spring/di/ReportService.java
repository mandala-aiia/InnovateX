package com.alec.InnovateX.spring.di;

import org.springframework.beans.factory.annotation.Autowired;

/** Setter 注入：可选/可变依赖的替代写法，容器在属性填充阶段调用 @Autowired 标注的 setter。 */
public class ReportService {

    private CatalogService catalogService;

    @Autowired
    public void setCatalogService(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    public String report(String item) {
        return "报表[" + catalogService.priceOf(item) + "]";
    }
}
