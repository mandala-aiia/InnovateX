package com.alec.InnovateX.spring.di;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

import java.util.Optional;

/**
 * 可选依赖四写法（目标 bean 可能不存在）：
 * - Optional<T>：拿到 Optional.empty()
 * - @Nullable：直接注入 null
 * - required=false：跳过注入，字段保持 null
 * - ObjectProvider<T>：注入惰性句柄，用时再查，还可以给兜底默认值
 */
public class OptionalDepsService {

    @Autowired
    private Optional<PricingClient> optionalClient;

    @Autowired
    @Nullable
    private PricingClient nullableClient;

    @Autowired(required = false)
    private PricingClient absentClient;

    @Autowired
    private ObjectProvider<PricingClient> clientProvider;

    public Optional<PricingClient> optionalClient() {
        return optionalClient;
    }

    public PricingClient nullableClient() {
        return nullableClient;
    }

    public PricingClient absentClient() {
        return absentClient;
    }

    public ObjectProvider<PricingClient> clientProvider() {
        return clientProvider;
    }

    /** ObjectProvider 的兜底用法：不存在时给默认值，不抛异常。 */
    public String pingWithFallback() {
        PricingClient client = clientProvider.getIfAvailable(PricingClient::new);
        return client.ping();
    }
}
