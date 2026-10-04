package com.alec.InnovateX.spring.extension;

/**
 * FactoryBean 的"产品"：一个普通业务对象。
 * 它不由容器直接 new 出来，而是由 ReportDocumentFactoryBean.getObject() 生产——
 * 产品的创建逻辑（可以复杂到连接外部系统、读配置拼装）被封装在工厂里，
 * 使用方按普通 Bean 一样 getBean，完全无感背后是工厂。
 */
public class ReportDocument {

    private final String title;

    public ReportDocument(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return "ReportDocument{title='" + title + "'}";
    }
}
