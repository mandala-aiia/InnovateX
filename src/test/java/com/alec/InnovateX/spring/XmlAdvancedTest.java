package com.alec.InnovateX.spring;

import com.alec.InnovateX.spring.xmladvanced.XmlAutowireClient;
import com.alec.InnovateX.spring.xmladvanced.XmlChildBean;
import com.alec.InnovateX.spring.xmladvanced.XmlCommander;
import com.alec.InnovateX.spring.xmladvanced.XmlCollectionsBean;
import com.alec.InnovateX.spring.xmladvanced.XmlFactoryProduct;
import com.alec.InnovateX.spring.xmladvanced.XmlLazyBean;
import com.alec.InnovateX.spring.xmladvanced.XmlOriginalWorker;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 主题⑨Bean 定义进阶（XML）：bean 继承、alias、静态/实例工厂、
 * lookup-method、replaced-method、map/set/props 集合注入、util: 命名空间、
 * lazy-init、autowire=byName、depends-on
 */
public class XmlAdvancedTest {

    @Test
    public void xmlAdvancedFeatures() {
        XmlLazyBean.instantiated = false;
        try (ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("spring-advanced.xml")) {
            // 1) bean 继承：子定义继承父定义属性，同名属性被子定义覆盖
            XmlChildBean child = ctx.getBean("xmlChild", XmlChildBean.class);
            assertEquals("子类覆盖名", child.getName());
            assertEquals("父类公共属性", child.getShared());
            // alias 指向同一个 Bean
            assertSame(child, ctx.getBean("xmlChildAlias"));
            System.out.println("bean 继承: name=" + child.getName() + ", shared=" + child.getShared() + "；alias 解析到同一实例");

            // 2/3) 静态工厂与实例工厂
            assertEquals("静态工厂方法", ctx.getBean("fromStaticFactory", XmlFactoryProduct.class).getFrom());
            assertEquals("实例工厂方法", ctx.getBean("fromInstanceFactory", XmlFactoryProduct.class).getFrom());
            System.out.println("静态工厂/实例工厂均正常产出 Bean");

            // 4) lookup-method：单例里两次"征召"拿到不同的 prototype 实例
            XmlCommander commander = ctx.getBean(XmlCommander.class);
            String first = commander.recruit();
            String second = commander.recruit();
            assertNotEqualsString(first, second);
            System.out.println("lookup-method: " + first + " / " + second);

            // 5) replaced-method：方法实现被整体替换
            String work = ctx.getBean(XmlOriginalWorker.class).work("写周报");
            assertTrue(work.startsWith("替换后的实现处理"));
            System.out.println("replaced-method: " + work);

            // 6) 集合注入 + util 命名空间
            XmlCollectionsBean collections = ctx.getBean(XmlCollectionsBean.class);
            assertEquals(2, collections.getMap().size());
            assertSame(child, collections.getMap().get("child"));
            assertEquals(2, collections.getSet().size());
            assertEquals("v", collections.getProps().getProperty("k"));
            assertEquals(2, collections.getUtilList().size());
            assertEquals("value", collections.getUtilMap().get("key"));
            System.out.println("map/set/props/util 注入全部生效: " + collections.getMap().keySet());

            // 7) lazy-init：refresh 完成后仍未实例化
            assertTrue(ctx.containsBean("xmlLazyBean"));
            assertFalse(XmlLazyBean.instantiated);
            ctx.getBean("xmlLazyBean", XmlLazyBean.class);
            assertTrue(XmlLazyBean.instantiated);
            System.out.println("lazy-init: 第一次 getBean 才实例化");

            // 8) autowire=byName：setXmlFactoryProduct 自动装配了名为 xmlFactoryProduct 的 Bean
            XmlAutowireClient client = ctx.getBean(XmlAutowireClient.class);
            assertTrue(client.getXmlFactoryProduct() != null);
            assertEquals("实例工厂方法", client.getXmlFactoryProduct().getFrom());
            System.out.println("autowire=byName 注入: " + client.getXmlFactoryProduct().getFrom());
        }
    }

    private void assertNotEqualsString(String a, String b) {
        assertFalse(a.equals(b), "两次 lookup 应产生不同实例: " + a + " vs " + b);
    }
}
