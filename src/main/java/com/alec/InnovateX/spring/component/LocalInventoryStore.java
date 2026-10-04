package com.alec.InnovateX.spring.component;

import org.springframework.stereotype.Repository;

/** 本地内存库存：@Repository 同样派生自 @Component，语义上标记持久层组件。 */
@Repository
public class LocalInventoryStore {

    public int stockOf(String sku) {
        return 100;
    }
}
