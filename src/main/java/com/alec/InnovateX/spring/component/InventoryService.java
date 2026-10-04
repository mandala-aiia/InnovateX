package com.alec.InnovateX.spring.component;

import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

/** 库存服务：@Service 派生自 @Component，扫描器一视同仁。静态计数器观察 lazyInit 扫描的实例化时机。 */
@Service
public class InventoryService {

    private static final AtomicInteger CREATED = new AtomicInteger();

    public InventoryService() {
        CREATED.incrementAndGet();
    }

    public static int createdTotal() {
        return CREATED.get();
    }

    public String check(String sku) {
        return "库存充足:" + sku;
    }
}
