package com.alec.InnovateX.spring.mvc;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * 自定义参数解析器：让控制器方法直接声明 ClientContext 类型参数。
 * Spring MVC 内置了几十个解析器（@PathVariable/@RequestBody/@RequestParam...），
 * supportsParameter 匹配不上时才轮到我们 addArgumentResolvers 追加的解析器
 */
public class ClientContextArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return ClientContext.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        String clientId = webRequest.getHeader("X-Client-Id");
        String forwardedFor = webRequest.getHeader("X-Forwarded-For");
        return new ClientContext(clientId == null ? "anonymous" : clientId,
                forwardedFor == null ? "127.0.0.1" : forwardedFor);
    }
}
