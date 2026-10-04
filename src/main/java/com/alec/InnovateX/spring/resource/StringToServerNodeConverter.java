package com.alec.InnovateX.spring.resource;

import org.springframework.core.convert.converter.Converter;

/**
 * 新转换体系的自定义转换器：String → ServerNode，约定格式 "host:port"（如 "redis.local:6379"）。
 * <p>
 * 与老式 PropertyEditor 的三点本质差异：
 * <ol>
 *   <li><b>无状态</b>：不持有字段、每次 convert 独立，天然线程安全，可全局单例复用；</li>
 *   <li><b>按"源类型→目标类型"成对注册</b>：而不是"挂在某个目标类的 PropertyEditor 上"；</li>
 *   <li><b>支持泛型与条件匹配</b>（ConditionalConverter/ConverterFactory 可扩展出集合、数组等转换）。</li>
 * </ol>
 */
public class StringToServerNodeConverter implements Converter<String, ServerNode> {

    @Override
    public ServerNode convert(String source) {
        // Converter 约定 source 不会是 null（null 由 ConversionService 外层处理）
        String[] parts = source.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("格式应为 host:port，收到: " + source);
        }
        ServerNode node = new ServerNode();
        node.setHost(parts[0].trim());
        node.setPort(Integer.parseInt(parts[1].trim()));
        return node;
    }
}
