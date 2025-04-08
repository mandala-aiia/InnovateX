package com.alec.InnovateX.spring;

import org.springframework.beans.factory.FactoryBean;

public class AppFactoryBean implements FactoryBean<AppFaBean> {
    @Override
    public boolean isSingleton() {
        return FactoryBean.super.isSingleton();
    }

    @Override
    public AppFaBean getObject()  {
        AppFaBean appFaBean = new AppFaBean();
        appFaBean.setAppFaBeanName("源生");
        return appFaBean;
    }

    @Override
    public Class<?> getObjectType() {
        return AppFaBean.class;
    }
}
