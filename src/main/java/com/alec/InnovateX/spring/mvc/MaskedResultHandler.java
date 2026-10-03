package com.alec.InnovateX.spring.mvc;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * 自定义返回值处理器（与 HandlerMethodArgumentResolver 对称的另一半）：
 * 内置处理器（@ResponseBody/ModelAndView/HttpEntity...）都不认识 @MaskedResult，
 * 轮到 WebMvcConfigurer.addReturnValueHandlers 追加的它接管——直接写响应并声明"请求已处理"
 */
public class MaskedResultHandler implements HandlerMethodReturnValueHandler {

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return returnType.hasMethodAnnotation(MaskedResult.class);
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType,
                                  ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws Exception {
        // 关键一步：声明请求已处理，后续的视图渲染/ResponseBody 处理不再介入
        mavContainer.setRequestHandled(true);
        HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
        response.setContentType("text/plain;charset=UTF-8");
        String raw = returnValue instanceof MaskedPayload payload ? payload.value() : String.valueOf(returnValue);
        String masked = raw.length() > 4 ? "****" + raw.substring(raw.length() - 4) : raw;
        response.getWriter().write(masked);
    }
}
