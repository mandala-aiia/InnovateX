package com.alec.InnovateX.spring.mvc;

/**
 * 脱敏载荷：特意用一个"内置返回值处理器都不认识"的自定义类型——
 * String 会被 ViewName 处理器吃掉、@RestController 的一切都会被 RequestResponseBody 处理器吃掉，
 * 只有自定义类型才能轮到 WebMvcConfigurer.addReturnValueHandlers 追加的处理器
 */
public record MaskedPayload(String value) {
}
