package com.alec.InnovateX.spring;

import com.alec.InnovateX.spring.cache.CacheConfig;
import com.alec.InnovateX.spring.cache.QuoteService;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 主题：缓存抽象。覆盖知识点：
 * 1. @EnableCaching + ConcurrentMapCacheManager 装配，CacheManager 可直查底层缓存；
 * 2. @Cacheable 命中不执行（invocationCount 断言）+ 默认 key（单参数即 key）；
 * 3. SpEL 复合 key（#symbol + ':' + #currency）区分多参数条目；
 * 4. @CachePut 必执行并刷新缓存；
 * 5. @CacheEvict 单条清除 + allEntries 清空；
 * 6. @Caching 组合多个缓存操作（跨缓存批量清除）。
 */
public class CacheTest {

    @Test
    public void cacheableSkipsMethodOnCacheHit() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(CacheConfig.class)) {
            QuoteService service = ctx.getBean(QuoteService.class);
            assertEquals("QUOTE[AAPL]", service.loadQuote("AAPL")); // 未命中：真实执行（第 1 次）
            assertEquals("QUOTE[AAPL]", service.loadQuote("AAPL")); // 命中：方法体不再执行
            assertEquals(1, service.getLoadQuoteCalls(), "第二次命中缓存，真实查询只执行一次");

            assertEquals("QUOTE[MSFT]", service.loadQuote("MSFT")); // 不同 key：再次真实执行
            assertEquals(2, service.getLoadQuoteCalls());
            System.out.println("[CacheTest] 两次 loadQuote(AAPL) 只执行一次真实查询，调用计数=1；"
                    + "换 key 后计数=2");

            // CacheManager 直查底层缓存：默认 key 即参数本身
            CacheManager cacheManager = ctx.getBean(CacheManager.class);
            assertInstanceOf(ConcurrentMapCacheManager.class, cacheManager);
            Cache quotes = cacheManager.getCache("quotes");
            assertNotNull(quotes);
            assertEquals("QUOTE[AAPL]", quotes.get("AAPL").get(), "默认 key：单参数方法的 key 就是参数");
            System.out.println("[CacheTest] 直查 quotes 缓存: get(\"AAPL\") = " + quotes.get("AAPL").get());
        }
    }

    @Test
    public void spelCompositeKeySeparatesEntries() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(CacheConfig.class)) {
            QuoteService service = ctx.getBean(QuoteService.class);
            assertEquals("AAPL/USD=100", service.loadPrice("AAPL", "USD")); // 未命中
            assertEquals("AAPL/USD=100", service.loadPrice("AAPL", "USD")); // 命中同一复合 key
            assertEquals("AAPL/EUR=100", service.loadPrice("AAPL", "EUR")); // 复合 key 不同：新条目
            assertEquals(2, service.getLoadPriceCalls(), "SpEL key 区分了不同参数组合");

            assertEquals("AAPL/USD=100", service.loadPrice("AAPL", "USD")); // 仍命中
            assertEquals(2, service.getLoadPriceCalls(), "重复读取全部命中");
            System.out.println("[CacheTest] SpEL 复合 key：AAPL:USD 与 AAPL:EUR 是两个独立条目，调用计数=2");
        }
    }

    @Test
    public void cachePutAlwaysExecutesAndRefreshesEntry() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(CacheConfig.class)) {
            QuoteService service = ctx.getBean(QuoteService.class);
            service.loadQuote("AAPL"); // 建立缓存条目
            assertEquals(1, service.getLoadQuoteCalls());

            // @CachePut：即使缓存已有条目，方法也一定执行，并用返回值覆盖同 key 条目
            assertEquals("QUOTE[AAPL]-已刷新", service.refreshQuote("AAPL"));
            assertEquals(1, service.getRefreshQuoteCalls(), "@CachePut 方法一定真实执行");

            // 再读：拿到的是刷新后的值，且方法不再执行
            assertEquals("QUOTE[AAPL]-已刷新", service.loadQuote("AAPL"));
            assertEquals(1, service.getLoadQuoteCalls(), "刷新后的条目直接命中");
            Cache quotes = ctx.getBean(CacheManager.class).getCache("quotes");
            assertEquals("QUOTE[AAPL]-已刷新", quotes.get("AAPL").get(), "显式 key=\"#symbol\" 与默认 key 落在同一键上");
            System.out.println("[CacheTest] @CachePut 刷新后 loadQuote 读到新值且未再执行查询");
        }
    }

    @Test
    public void cacheEvictCachingComboAndAllEntries() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(CacheConfig.class)) {
            QuoteService service = ctx.getBean(QuoteService.class);
            Cache quotes = ctx.getBean(CacheManager.class).getCache("quotes");

            // @CacheEvict 单条清除
            service.loadQuote("AAPL");
            service.invalidateQuote("AAPL");
            assertNull(quotes.get("AAPL"), "单条清除后缓存里没有该 key");
            service.loadQuote("AAPL"); // 重新真实执行
            assertEquals(2, service.getLoadQuoteCalls());

            // @Caching 组合：同时清 quotes:AAPL 与 prices:AAPL:USD
            service.loadPrice("AAPL", "USD");
            assertEquals(1, service.getLoadPriceCalls());
            service.purgeSymbolEverywhere("AAPL");
            service.loadQuote("AAPL"); // 又要真实执行
            service.loadPrice("AAPL", "USD");
            assertEquals(3, service.getLoadQuoteCalls(), "@Caching 清除后 loadQuote 需重新执行");
            assertEquals(2, service.getLoadPriceCalls(), "@Caching 清除后 loadPrice 需重新执行");

            // allEntries：整库清空
            service.loadQuote("MSFT");
            service.clearAllQuotes();
            assertNull(quotes.get("AAPL"));
            assertNull(quotes.get("MSFT"));
            System.out.println("[CacheTest] @CacheEvict 单条 / @Caching 跨缓存组合 / allEntries 全清 均生效");
        }
    }
}
