package com.alec.InnovateX.spring;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

@Data
@NoArgsConstructor
public class AppDev implements InitializingBean, DisposableBean {
    private String appDevName;

    private String appDevOverride;

    private String appDevC;

    public AppDev(String appDevC) {
        this.appDevC = appDevC;
    }

    @Override
    public String toString() {
        return "AppDev{" +
                "appDevName='" + appDevName + '\'' +
                ", appDevOverride='" + appDevOverride + '\'' +
                ", appDevC='" + appDevC + '\'' +
                '}';
    }

/*    @PostConstruct
    public void initAppDev() {
        System.out.println("AppDev....init");
    }

    @PreDestroy
    public void destroyAppDev() {
        System.out.println("AppDev....destroy");
    }*/

    @Override
    public void afterPropertiesSet() {
        System.out.println("AppDev....init");
    }

    @Override
    public void destroy() {
        System.out.println("AppDev....destroy");
    }
}
