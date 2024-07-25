package com.alec.InnovateX;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
// import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

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

        context.stop();
        context.close();

	}

}
