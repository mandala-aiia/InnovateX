package com.alec.InnovateX.spring.cache;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Component;

/**
 * 缓存注解全家桶：
 * - @Cacheable：先查缓存，命中直接返回（方法不执行）；未命中执行方法并把返回值放入缓存
 * - @CachePut：方法一定执行，用返回值刷新缓存（适合更新后同步缓存）
 * - @CacheEvict：删除缓存条目（beforeInvocation 可选删除时机）
 * - @Caching：组合多个缓存操作
 * invocationCount 用于在测试中验证"方法体到底执行了几次"
 */
@Component
public class CacheOrderService {

    private int invocationCount = 0;

    public int getInvocationCount() {
        return invocationCount;
    }

    @Cacheable(cacheNames = "orders", key = "#id")
    public String loadOrder(long id) {
        invocationCount++;
        System.out.println("[CacheOrderService] 未命中缓存，执行真实查询: " + id);
        return "order-" + id;
    }

    @CachePut(cacheNames = "orders", key = "#id")
    public String refreshOrder(long id) {
        invocationCount++;
        System.out.println("[CacheOrderService] @CachePut 执行并刷新缓存: " + id);
        return "order-" + id + "-v2";
    }

    @CacheEvict(cacheNames = "orders", key = "#id")
    public void evictOrder(long id) {
        System.out.println("[CacheOrderService] 清除缓存: " + id);
    }

    @Caching(evict = {@CacheEvict(cacheNames = "orders", key = "#id")})
    public void evictWithCombo(long id) {
        System.out.println("[CacheOrderService] @Caching 组合操作清除: " + id);
    }
}
