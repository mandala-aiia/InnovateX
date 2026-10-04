package com.alec.InnovateX.spring.event;

import org.springframework.context.ApplicationEventPublisher;

/**
 * 事件发布方（业务类）：显式构造器注入 ApplicationEventPublisher（final 字段 + 显式 getter）。
 *
 * 教学点：发布事件不需要拿整个 ApplicationContext——按最小接口注入 ApplicationEventPublisher 即可，
 * 它是 AbstractApplicationContext 启动时注册的"可解析依赖"，@Bean 方法参数 / @Autowired 都能直接注入。
 *
 * 一个业务动作发布两种事件：POJO 事件 + 固化泛型的具体子类事件。
 */
public class InventoryService {

    private final ApplicationEventPublisher publisher;

    public InventoryService(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public ApplicationEventPublisher getPublisher() {
        return publisher;
    }

    public void addProduct(String sku, double price) {
        System.out.println("[InventoryService] addProduct: sku=" + sku + ", price=" + price);
        publisher.publishEvent(new ProductAddedEvent(sku));
        publisher.publishEvent(new ProductChangedEvent(this, new ProductPayload(sku, price)));
    }
}
