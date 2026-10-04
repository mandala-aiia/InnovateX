package com.alec.InnovateX.spring.annotation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 集合注入 + 占位符 + 可选依赖（知识点都在"注入点注解"上，故沿用字段注入）：
 * - List<T>：容器收集 T 类型的全部实现，顺序由实现类上的 @Order 决定
 * - Map<String, T>：key 为 bean 名，value 为 bean 实例
 * - @Value("${key}") 解析 @PropertySource 引入的占位符；"${key:默认值}" 在 key 缺失时兜底
 * - @Autowired(required=false)：没有候选 bean 时注入 null，容器启动不失败
 */
@Service
public class CollectionInjectionService {

    @Autowired
    private List<NotificationChannel> channels;

    @Autowired
    private Map<String, NotificationChannel> channelMap;

    @Autowired
    private List<AuditStorage> storages;

    @Autowired
    private Map<String, AuditStorage> storageMap;

    @Value("${annotation.app.name}")
    private String appName;

    @Value("${annotation.app.version}")
    private String appVersion;

    /** properties 里没有 annotation.app.slogan 这个 key，冒号后是兜底默认值 */
    @Value("${annotation.app.slogan:离线亦可测}")
    private String sloganOrDefault;

    /** 容器中没有 MetricsBinder 的任何实现，required=false 使其注入 null 而不是启动报错 */
    @Autowired(required = false)
    private MetricsBinder metricsBinder;

    public List<NotificationChannel> getChannels() {
        return channels;
    }

    public Map<String, NotificationChannel> getChannelMap() {
        return channelMap;
    }

    public List<AuditStorage> getStorages() {
        return storages;
    }

    public Map<String, AuditStorage> getStorageMap() {
        return storageMap;
    }

    public String getAppName() {
        return appName;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public String getSloganOrDefault() {
        return sloganOrDefault;
    }

    public MetricsBinder getMetricsBinder() {
        return metricsBinder;
    }
}
