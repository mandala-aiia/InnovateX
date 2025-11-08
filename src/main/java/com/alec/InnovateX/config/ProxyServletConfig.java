package com.alec.InnovateX.config;

import org.mitre.dsmiley.httpproxy.ProxyServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProxyServletConfig {
    @Bean
    public ServletRegistrationBean<ProxyServlet> servletRegistrationBean() {
        ServletRegistrationBean<ProxyServlet> bean =
                new ServletRegistrationBean<>(new ProxyServlet(), "/proxy/hls/*");
        bean.setName("hlsProxy");
        bean.addInitParameter("targetUri", "https://t33.cdn2020.com");
        bean.addInitParameter("log", "true");
        return bean;
    }
}