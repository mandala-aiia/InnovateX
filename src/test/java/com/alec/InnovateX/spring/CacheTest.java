package com.alec.InnovateX.spring;

import com.alec.InnovateX.spring.cache.CacheConfig;
import com.alec.InnovateX.spring.cache.CacheOrderService;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 主题⑫缓存抽象：@Cacheable/@CachePut/@CacheEvict/@Caching + ConcurrentHashMapCacheManager
 */
public class CacheTest {

    @Test
    public void cacheablePutEvict() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(CacheConfig.class)) {
            CacheOrderService service = ctx.getBean(CacheOrderService.class);

            // 第一次：未命中，真实执行；第二次：命中缓存，方法不再执行
            assertEquals("order-1", service.loadOrder(1));
            assertEquals("order-1", service.loadOrder(1));
            assertEquals(1, service.getInvocationCount());
            System.out.println("两次 loadOrder(1) 只执行了一次真实查询（invocationCount=1）");

            // @CachePut：方法一定执行，缓存被刷新为 v2
            assertEquals("order-1-v2", service.refreshOrder(1));
            assertEquals("order-1-v2", service.loadOrder(1));
            assertEquals(2, service.getInvocationCount());
            System.out.println("CachePut 刷新后，loadOrder 读到新值且未再执行查询");

            // @CacheEvict：清除后再次 load 需要真实执行
            service.evictOrder(1);
            assertEquals("order-1", service.loadOrder(1));
            assertEquals(3, service.getInvocationCount());
            System.out.println("CacheEvict 后缓存失效，重新真实查询（invocationCount=3）");

            // @Caching 组合操作
            service.evictWithCombo(1);
            assertEquals("order-1", service.loadOrder(1));
            assertEquals(4, service.getInvocationCount());
            System.out.println("@Caching 组合清除同样生效（invocationCount=4）");
        }
    }
}
