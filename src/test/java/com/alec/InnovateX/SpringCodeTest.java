package com.alec.InnovateX;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.util.Locale;

public class SpringCodeTest {

    @Test
    public void springCoding() {
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

        AppPointcut appPointcut = context.getBean(AppPointcut.class);
        appPointcut.appPointcutBefore();
        appPointcut.appPointcutReturning();
        // appPointcut.appPointcutThrowing();  //异常通知
        appPointcut.appPointcutAround();
        appPointcut.appPointcutAfter();
        appPointcut.appPointcutAnnotation();

        AppJdbcTemplate appJdbcTemplate = context.getBean(AppJdbcTemplate.class);
        System.out.println("通过jdbcTemplate查询的结果：" + appJdbcTemplate.query());
        // System.out.println("通过jdbcTemplate新增的数据：" + appJdbcTemplate.insert());
        // System.out.println("通过jdbcTemplate修改的数据：" + appJdbcTemplate.update()); //xml事务异常回滚
        // System.out.println("通过jdbcTemplate中的编程式事务修改的数据：" + appJdbcTemplate.programmatic()); //编程式事务异常回滚
        // System.out.println("通过namedParameterJdbcTemplate新增的数据：" + appJdbcTemplate.namedParameter());

        String appFace = "appInterface_";
        AppInterface appInterface = context.getBean(appFace+"02",AppInterface.class);
        appInterface.display();
        appInterface.submit();

        context.stop();
        context.close();
    }


}
