package com.alec.InnovateX.spring.annotation;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ObjectProvider 注入（required=false 的现代升级版）：
 * - 注入的不是 Bean 本身，而是一个"按需取 Bean 的句柄"——容器启动时不要求 Bean 存在
 * - getIfAvailable()：可选获取（没有时返回 null 而不是抛 NoSuchBeanDefinitionException）
 * - stream()：把同类型的所有 Bean 当流处理（替代注入 List/Map）
 * - getIfAvailable(Supplier)：没有时给默认值
 * ObjectProvider 继承自 ObjectFactory（getObject），还支持 getIfUnique/orderedStream
 */
@Service
public class ProviderConsumerService {

    private final ObjectProvider<MessageRepository> repositoryProvider;

    /** 容器里并不存在这个类型的 Bean——ObjectProvider 让注入照样成立 */
    private final ObjectProvider<AnnotationCollectorService.OptionalMessageHandler> missingProvider;

    public ProviderConsumerService(ObjectProvider<MessageRepository> repositoryProvider,
                                   ObjectProvider<AnnotationCollectorService.OptionalMessageHandler> missingProvider) {
        this.repositoryProvider = repositoryProvider;
        this.missingProvider = missingProvider;
    }

    /** 按需取一个：多实现时走 @Primary */
    public String getIfAvailable() {
        return repositoryProvider.getIfAvailable().type();
    }

    /** stream()：同类型所有实现（等价于注入 List<MessageRepository>） */
    public List<String> streamAllTypes() {
        return repositoryProvider.stream().map(MessageRepository::type).sorted().toList();
    }

    /** 可选类型缺失：getIfAvailable 返回 null 而不抛异常 */
    public String getIfMissing() {
        return String.valueOf(missingProvider.getIfAvailable());
    }
}
