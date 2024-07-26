package com.alec.InnovateX;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
// import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Locale;

// @SpringBootTest
class InnovateXApplicationTests {

    @Test
    void contextLoads() {

        Resource resource = new ClassPathResource("spring-context.xml");
        GenericApplicationContext context = new GenericApplicationContext();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(context);
        reader.setEventListener(new AppReaderEventListener());
        reader.loadBeanDefinitions(resource);
        // context.setAllowCircularReferences(true); //是否启用循环依赖
        // context.registerBean("appListener",AppListener.class);//硬编码注册Bean
        context.refresh();
        context.start();

        App app = context.getBean(App.class);
        AppDev appDev = context.getBean(AppDev.class);
        context.publishEvent(new AppEvent(this, "spring event published"));
        String message = context.getMessage("app.message", null, "", Locale.US);
        AppFaBean appFaBean = context.getBean(AppFactoryBean.class).getObject();
        System.out.println(app);
        System.out.println(appDev);
        System.out.println(appFaBean);
        System.out.println(message);

        JdbcTemplate jdbcTemplate = context.getBean(JdbcTemplate.class);
        System.out.println("通过jdbcTemplate查询的结果：" + jdbcTemplate.queryForList("select uuid from innovatex limit 1", String.class));

        AppPointcut appPointcut = context.getBean(AppPointcut.class);
        appPointcut.appPointcutBefore();
        appPointcut.appPointcutReturning();
        // appPointcut.appPointcutThrowing();  //异常通知
        appPointcut.appPointcutAround();
        appPointcut.appPointcutAfter();
        appPointcut.appPointcutAnnotation();

        context.stop();
        context.close();
    }

}
