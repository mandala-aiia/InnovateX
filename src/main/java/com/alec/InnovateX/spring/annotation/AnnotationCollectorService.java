package com.alec.InnovateX.spring.annotation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 集合注入 + @Value 占位符 + required=false：
 * - 注入 List<T>：容器会把 T 类型的所有 bean 收集成列表（按 @Order/bean 名称排序）
 * - 注入 Map<String, T>：key 为 bean 名称，value 为 bean 实例
 * - @Value("${key:默认值}")：冒号后是占位符缺失时的默认值
 * - @Autowired(required=false)：没有候选 bean 时注入 null 而不是启动报错
 */
@Service
public class AnnotationCollectorService {

    @Autowired
    private List<MessageRepository> repositories;

    @Autowired
    private Map<String, MessageRepository> repositoryMap;

    @Value("${annotation.app.name}")
    private String appName;

    @Value("${annotation.app.desc:默认描述}")
    private String desc;

    /** 容器中并不存在 OptionalMessageHandler 类型的 bean，required=false 使其注入 null */
    @Autowired(required = false)
    private OptionalMessageHandler optionalHandler;

    public List<MessageRepository> getRepositories() {
        return repositories;
    }

    public Map<String, MessageRepository> getRepositoryMap() {
        return repositoryMap;
    }

    public String getAppName() {
        return appName;
    }

    public String getDesc() {
        return desc;
    }

    public OptionalMessageHandler getOptionalHandler() {
        return optionalHandler;
    }

    /** 仅用于演示 required=false 的注入目标类型 */
    public interface OptionalMessageHandler {
    }
}
