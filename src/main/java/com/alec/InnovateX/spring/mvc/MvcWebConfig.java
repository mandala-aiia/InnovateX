package com.alec.InnovateX.spring.mvc;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;

import java.util.List;

/**
 * 纯 Spring MVC 装配（不依赖 Boot）：
 * - @EnableWebMvc 导入 DelegatingWebMvcConfiguration，注册 DispatcherServlet 需要的全部基础设施
 *   （HandlerMapping/HandlerAdapter/异常解析器/内置 HttpMessageConverter 等）
 * - WebMvcConfigurer 回调是往基础设施里"加私货"的口子（Boot 的 WebMvcAutoConfiguration 也走同一机制）
 */
@Configuration
@EnableWebMvc
public class MvcWebConfig implements WebMvcConfigurer {

    @Bean
    public DemoController demoController() {
        return new DemoController();
    }

    @Bean
    public ValidationController validationController() {
        return new ValidationController();
    }

    @Bean
    public MaskedController maskedController() {
        return new MaskedController();
    }

    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    /**
     * Multipart 解析器：bean 名称必须是 multipartResolver（DispatcherServlet 按名检测），
     * multipart/form-data 请求会被预先解析成 MultipartFile 参数
     */
    @Bean(name = DispatcherServlet.MULTIPART_RESOLVER_BEAN_NAME)
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

    /** 追加自定义拦截器（也演示了路径过滤） */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoggingInterceptor()).addPathPatterns("/api/**");
    }

    /** 追加自定义参数解析器：内置解析器都不支持 ClientContext 时生效 */
    @Override
    public void addArgumentResolvers(List<org.springframework.web.method.support.HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new ClientContextArgumentResolver());
    }

    /** 追加自定义 HttpMessageConverter（保留 Jackson 等默认转换器，用 extend 而不是 configure） */
    @Override
    public void extendMessageConverters(List<org.springframework.http.converter.HttpMessageConverter<?>> converters) {
        converters.add(new PersonCsvHttpMessageConverter());
    }

    /** 追加自定义返回值处理器：@MaskedResult 标记的方法走脱敏写出 */
    @Override
    public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> handlers) {
        handlers.add(new MaskedResultHandler());
    }
}
