package com.alec.InnovateX.spring.cache;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 缓存抽象装配：
 * @EnableCaching 开启注解驱动（向容器注册 CacheInterceptor 所需的基础设施）；
 * CacheManager 是"缓存抽象"的核心——业务注解只面向抽象，存储实现可插拔：
 * 这里用 Spring 自带的 ConcurrentMapCacheManager（纯内存、零依赖，适合教学与单机小场景），
 * 换 Redis/Caffeine 只需替换本方法返回的实现，QuoteService 的注解一行不改。
 * （对比：不预声明缓存名时 ConcurrentMapCacheManager 默认动态创建，预声明可及早暴露拼写错误。）
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("quotes", "prices");
    }

    @Bean
    public QuoteService quoteService() {
        return new QuoteService();
    }
}
