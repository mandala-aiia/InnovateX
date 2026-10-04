package com.alec.InnovateX.spring.cache;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 缓存注解全家桶演示（行情查询域）：
 * - @Cacheable：先按 key 查缓存，命中直接返回、方法体完全不执行；未命中才执行并回填缓存；
 * - @CachePut：方法体一定执行，用返回值"刷新"缓存（适合写后同步）；
 * - @CacheEvict：按 key 删除条目，allEntries=true 清空整个缓存；
 * - @Caching：把多个缓存操作组合在一个方法上（比如同时清多个缓存）。
 *
 * key 的两种给法都有示范：
 * - loadQuote 不写 key：单参数方法默认 key 就是参数本身（SimpleKeyGenerator）；
 * - loadPrice 写 SpEL：多参数时必须显式拼 key，"#参数名 + ':' + #参数名" 组合出复合键。
 * 底层原理：@EnableCaching 注册的 CacheInterceptor（AOP 环绕通知）在方法前后拦截。
 */
public class QuoteService {

    /** 各方法真实执行次数：测试用它们证明"缓存命中时方法体没有跑" */
    private final AtomicInteger loadQuoteCalls = new AtomicInteger();

    private final AtomicInteger loadPriceCalls = new AtomicInteger();

    private final AtomicInteger refreshQuoteCalls = new AtomicInteger();

    /** 默认 key：单参数方法的 key 即参数本身（与显式写 key = "#symbol" 等价） */
    @Cacheable(cacheNames = "quotes")
    public String loadQuote(String symbol) {
        loadQuoteCalls.incrementAndGet();
        System.out.println("[QuoteService] 缓存未命中，真实查询行情: " + symbol);
        return "QUOTE[" + symbol + "]";
    }

    /** SpEL 复合 key：symbol + currency 组合成 "AAPL:USD" 形式，参数不同即不同条目 */
    @Cacheable(cacheNames = "prices", key = "#symbol + ':' + #currency")
    public String loadPrice(String symbol, String currency) {
        loadPriceCalls.incrementAndGet();
        System.out.println("[QuoteService] 缓存未命中，真实查询价格: " + symbol + "/" + currency);
        return symbol + "/" + currency + "=100";
    }

    /** 方法一定执行，返回值覆盖写入同 key 条目；注意 key 写法与 loadQuote 的默认 key 落在同一键上 */
    @CachePut(cacheNames = "quotes", key = "#symbol")
    public String refreshQuote(String symbol) {
        refreshQuoteCalls.incrementAndGet();
        System.out.println("[QuoteService] @CachePut 执行并刷新缓存: " + symbol);
        return "QUOTE[" + symbol + "]-已刷新";
    }

    @CacheEvict(cacheNames = "quotes", key = "#symbol")
    public void invalidateQuote(String symbol) {
        System.out.println("[QuoteService] @CacheEvict 清除单条: " + symbol);
    }

    /** @Caching 组合：一次调用同时清两个缓存里的相关条目（单注解做不到跨 cache 操作） */
    @Caching(evict = {
            @CacheEvict(cacheNames = "quotes", key = "#symbol"),
            @CacheEvict(cacheNames = "prices", key = "#symbol + ':USD'")
    })
    public void purgeSymbolEverywhere(String symbol) {
        System.out.println("[QuoteService] @Caching 组合清除 quotes+prices: " + symbol);
    }

    /** allEntries：不关心具体 key，整库清空（灰度/全量重置场景） */
    @CacheEvict(cacheNames = "quotes", allEntries = true)
    public void clearAllQuotes() {
        System.out.println("[QuoteService] @CacheEvict(allEntries) 清空整个 quotes 缓存");
    }

    public int getLoadQuoteCalls() {
        return loadQuoteCalls.get();
    }

    public int getLoadPriceCalls() {
        return loadPriceCalls.get();
    }

    public int getRefreshQuoteCalls() {
        return refreshQuoteCalls.get();
    }
}
