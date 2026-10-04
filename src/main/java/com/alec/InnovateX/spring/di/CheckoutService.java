package com.alec.InnovateX.spring.di;

/**
 * 结账服务：唯一的构造器（带参数）——Spring 会自动选用它并从容器解析参数，无需 @Autowired。
 * 这就是官方推荐的构造器注入。
 */
public class CheckoutService {

    private final CatalogService catalogService;

    public CheckoutService(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    public String quote(String item) {
        return catalogService.priceOf(item);
    }
}
