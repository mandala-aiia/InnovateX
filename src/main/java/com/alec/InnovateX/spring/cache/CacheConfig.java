package com.alec.InnovateX.spring.cache;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 缓存抽象：@EnableCaching 开启注解驱动（原理是 CacheInterceptor 这个 AOP 环绕通知）。
 * CacheManager 接口对接不同实现——这里用 Spring 自带的 ConcurrentMapCacheManager（内存实现），
 * 换 Redis/Ehcache/Caffeine 只需换 CacheManager Bean，业务代码的 @Cacheable 不用动
 */
@Configuration
@EnableCaching
@ComponentScan
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("orders");
    }
}
