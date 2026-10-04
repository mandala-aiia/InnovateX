package com.alec.InnovateX.spring.di;

/** 商品目录：被注入的普通服务，无注解，经 @Bean/registerBean 注册。 */
public class CatalogService {

    public String priceOf(String item) {
        return "价:" + item;
    }
}
