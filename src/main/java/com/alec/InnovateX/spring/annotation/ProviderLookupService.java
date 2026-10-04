package com.alec.InnovateX.spring.annotation;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ObjectProvider：可选依赖的现代方案（取代 @Autowired(required=false)）。
 * 注入的是一个"按需取 Bean 的句柄"，容器启动时不要求目标 Bean 存在，也就不会启动报错：
 * - getIfAvailable()：按需取一个（多候选时走 @Primary）；没有则返回 null 而非抛异常
 * - stream()：同类型全部 Bean 的流（顺序不保证）；orderedStream() 才按 @Order/@Priority 排序
 * - getIfAvailable(Supplier)：目标缺失时直接给出兜底实现，业务代码里不用写 null 判断
 * ObjectProvider 继承自 ObjectFactory（getObject()），是注入"延迟/可选依赖"的官方推荐
 */
@Service
public class ProviderLookupService {

    private final ObjectProvider<NotificationChannel> channelProvider;

    /** 容器中并不存在 MetricsBinder 的实现——句柄照样能注入，用时再判 */
    private final ObjectProvider<MetricsBinder> metricsProvider;

    public ProviderLookupService(ObjectProvider<NotificationChannel> channelProvider,
                                 ObjectProvider<MetricsBinder> metricsProvider) {
        this.channelProvider = channelProvider;
        this.metricsProvider = metricsProvider;
    }

    /** 按需取一个：多候选时走 @Primary（email） */
    public String primaryChannel() {
        return channelProvider.getIfAvailable().name();
    }

    /** orderedStream：按 @Order 排序遍历全部实现（email -> sms） */
    public List<String> orderedChannels() {
        return channelProvider.orderedStream().map(NotificationChannel::name).toList();
    }

    /** 普通流：只保证拿到全部实现，不保证顺序 */
    public long streamCount() {
        return channelProvider.stream().count();
    }

    /** 缺失类型：getIfAvailable 返回 null 而非抛 NoSuchBeanDefinitionException */
    public String missingOrNull() {
        return String.valueOf(metricsProvider.getIfAvailable());
    }

    /** 缺失类型：getIfAvailable(Supplier) 提供兜底实现 */
    public String missingWithFallback() {
        MetricsBinder binder = metricsProvider.getIfAvailable(() -> () -> "兜底 MetricsBinder");
        return binder.bind();
    }
}
