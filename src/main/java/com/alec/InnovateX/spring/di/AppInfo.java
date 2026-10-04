package com.alec.InnovateX.spring.di;

import org.springframework.beans.factory.annotation.Value;

/**
 * @Value 五种姿势：
 * - ${key}：从 Environment 的 PropertySource 取属性（@PropertySource / 系统属性 / 环境变量）
 * - ${key:默认值}：属性缺失时的兜底
 * - #{表达式}：SpEL，字面量运算
 * - #{${key} + 1}：占位符先解析成字面量、再交给 SpEL 计算
 * 注意：properties 文件按 ISO-8859-1 读取，值里避免直接写中文。
 */
public class AppInfo {

    @Value("${app.name}")
    private String name;

    @Value("${app.version}")
    private String version;

    @Value("${app.zone:UTC}")
    private String zone;

    @Value("#{2 * 21}")
    private int magic;

    @Value("#{${app.threadPool} + 1}")
    private int poolPlusOne;

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getZone() {
        return zone;
    }

    public int getMagic() {
        return magic;
    }

    public int getPoolPlusOne() {
        return poolPlusOne;
    }
}
