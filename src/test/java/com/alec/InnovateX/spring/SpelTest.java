package com.alec.InnovateX.spring;

import com.alec.InnovateX.spring.spel.SpelConfig;
import com.alec.InnovateX.spring.spel.SpelDemo;
import com.alec.InnovateX.spring.spel.SpelPropertiesBean;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 主题⑩SpEL：表达式解析器（运算/三元/Elvis/正则/方法/T()/集合投影筛选）+ @Value 中的 SpEL
 */
public class SpelTest {

    @Test
    public void expressionParser() {
        System.out.println("运算符: " + SpelDemo.operators());
        assertEquals("7, hello spel, 奇, 默认值, true", SpelDemo.operators());
        System.out.println("属性/方法/T(): " + SpelDemo.propertyAndMethod());
        assertEquals("InnovateX, true, 9", SpelDemo.propertyAndMethod());
        System.out.println("集合投影/筛选: " + SpelDemo.collectionProjectionAndSelection());
        assertTrue(SpelDemo.collectionProjectionAndSelection().contains("投影翻倍=[2, 4, 6, 8, 10]"));
        assertTrue(SpelDemo.collectionProjectionAndSelection().contains("筛选偶数=[2, 4]"));
    }

    @Test
    public void valueWithSpel() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(SpelConfig.class)) {
            SpelPropertiesBean bean = ctx.getBean(SpelPropertiesBean.class);
            assertEquals(42, bean.getComputed());
            assertEquals(99, bean.getStaticCall());
            assertEquals(43, bean.getSelfReference());
            System.out.println("@Value SpEL: computed=" + bean.getComputed()
                    + ", staticCall=" + bean.getStaticCall()
                    + ", selfReference=" + bean.getSelfReference()
                    + ", systemProperty=" + bean.getSystemProperty());
        }
    }
}
