package com.alec.InnovateX.spring.aot;

/**
 * AOT 演示的"反射靶子"：老式框架（序列化器/RPC/ORM）大量靠反射调用这类类。
 * 构造器和核心方法都不是 public，GraalVM native-image 默认会把它们裁剪掉——
 * 必须提前用 RuntimeHints 声明，AOT 处理阶段才会保留 reachable 元数据
 */
public class LegacyService {

    private final String source;

    LegacyService(String source) {
        this.source = source;
    }

    public String work(String input) {
        return hiddenTransform(input);
    }

    private String hiddenTransform(String input) {
        return "[" + source + "] " + input.toUpperCase();
    }
}
