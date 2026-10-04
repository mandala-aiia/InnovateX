package com.alec.InnovateX.spring.javaconfig;

/** 普通数据 record：@Value 解析出的占位符值固化进不可变载体 */
public record AppInfo(String appName, String version) {
}
