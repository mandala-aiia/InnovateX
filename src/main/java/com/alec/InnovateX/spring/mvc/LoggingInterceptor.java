package com.alec.InnovateX.spring.mvc;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * HandlerInterceptor 三个回调的时机（都在 DispatcherServlet 内部）：
 * - preHandle：HandlerAdapter 调用处理器之前，返回 false 中断后续
 * - postHandle：处理器执行完之后、视图渲染/响应提交之前（@ResponseBody 的响应体在处理器阶段已写出）
 * - afterCompletion：请求完全结束（渲染后），无论成功失败都执行——能拿到处理器抛出的异常
 */
public class LoggingInterceptor implements HandlerInterceptor {

    /** 与 TimingFilter 共用一份事件列表，测试断言"Filter 与 Interceptor 的执行顺序" */
    public static final List<String> EVENTS = new CopyOnWriteArrayList<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        EVENTS.add("interceptor-preHandle:" + request.getRequestURI());
        System.out.println("[LoggingInterceptor] preHandle -> " + request.getMethod() + " " + request.getRequestURI());
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           @Nullable ModelAndView modelAndView) {
        EVENTS.add("interceptor-postHandle:status=" + response.getStatus());
        System.out.println("[LoggingInterceptor] postHandle（响应体已写出，状态=" + response.getStatus() + "）");
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                @Nullable Exception ex) {
        EVENTS.add("interceptor-afterCompletion:" + (ex == null ? "无异常" : ex.getClass().getSimpleName()));
        System.out.println("[LoggingInterceptor] afterCompletion，处理器异常=" + ex);
    }
}
