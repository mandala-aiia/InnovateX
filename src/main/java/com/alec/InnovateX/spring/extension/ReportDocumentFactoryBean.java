package com.alec.InnovateX.spring.extension;

import org.springframework.beans.factory.FactoryBean;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * FactoryBean：一种特殊的 Bean——注册进容器的名字属于"工厂"，getBean 按名字取到的却是"产品"。
 * 这是对"实例化逻辑本身"的扩展点（Mybatis 的 SqlSessionFactoryBean、OpenFeign 的 FeignClientFactoryBean
 * 都靠它把第三方复杂对象伪装成普通 Bean）。
 *
 * 获取规则（测试逐一验证）：
 * - getBean("reportDocumentFactory") / getBean(ReportDocument.class)：拿到产品 getObject() 的产物；
 * - getBean("&reportDocumentFactory") / getBean(ReportDocumentFactoryBean.class)：拿到工厂本身。
 *
 * isSingleton() 返回 true 时产品由容器缓存：多次 getBean 只会触发一次 getObject()
 * （PRODUCT_CREATIONS 计数器即为此断言的物证）；返回 false 则每次 getBean 都重新生产。
 */
public class ReportDocumentFactoryBean implements FactoryBean<ReportDocument> {

    /** getObject() 的真实执行次数：验证单例产品的缓存行为 */
    public static final AtomicInteger PRODUCT_CREATIONS = new AtomicInteger();

    @Override
    public ReportDocument getObject() {
        PRODUCT_CREATIONS.incrementAndGet();
        System.out.println("[ReportDocumentFactoryBean] getObject() 第 " + PRODUCT_CREATIONS.get() + " 次被调用，生产产品");
        return new ReportDocument("InnovateX-年度报告");
    }

    @Override
    public Class<?> getObjectType() {
        return ReportDocument.class;
    }

    /** true：产品按单例缓存，getObject 只会被调用一次 */
    @Override
    public boolean isSingleton() {
        return true;
    }
}
