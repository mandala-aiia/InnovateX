package com.alec.InnovateX.spring.mvc;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

/**
 * 原生 Servlet Filter（与 Spring MVC 无关的 Servlet 规范组件）：
 * 包在整个 DispatcherServlet 外面——所以"进入在所有拦截器之前，退出在 afterCompletion 之后"。
 * 与拦截器的三个区别：规范层（Servlet vs Spring）、能否拿到 handler、能否改 ModelAndView
 */
public class TimingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        long start = System.currentTimeMillis();
        LoggingInterceptor.EVENTS.add("filter-before:" + ((HttpServletRequest) request).getRequestURI());
        System.out.println("[TimingFilter] >>> 进入过滤器链");
        try {
            chain.doFilter(request, response);
        } finally {
            LoggingInterceptor.EVENTS.add("filter-after:耗时" + (System.currentTimeMillis() - start) + "ms");
            System.out.println("[TimingFilter] <<< 退出过滤器链，总耗时 " + (System.currentTimeMillis() - start) + "ms");
        }
    }
}
