package com.alec.InnovateX.spring.mvc;

/** 自定义参数解析器的产物：控制器方法上直接声明它即可，无需任何注解 */
public record ClientContext(String clientId, String requestIp) {
}
