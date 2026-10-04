package com.alec.InnovateX.spring;

import com.alec.InnovateX.spring.resource.ConversionConfig;
import com.alec.InnovateX.spring.resource.OrderPropertyEditor;
import com.alec.InnovateX.spring.resource.OrderRecord;
import com.alec.InnovateX.spring.resource.OrderValidator;
import com.alec.InnovateX.spring.resource.StringToOrderConverter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.DataBinder;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 主题⑪资源与类型转换：Resource/ResourceLoader/ResourcePatternResolver、
 * ConversionService + 自定义 Converter、PropertyEditor、DataBinder + Validator
 */
public class ResourceTest {

    @Test
    public void resourceAbstraction() throws Exception {
        ResourceLoader loader = new DefaultResourceLoader();
        // 三种资源定位前缀走同一个 Resource 抽象
        Resource classpathResource = loader.getResource("classpath:annotation/annotation-app.properties");
        assertTrue(classpathResource.exists());
        String content = new String(classpathResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertTrue(content.contains("annotation.app.name=InnovateX"));
        System.out.println("classpath 资源内容: " + content.replace("\n", " | "));

        // Ant 通配 + classpath*：把根路径下所有 properties 一网打尽
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath*:*.properties");
        List<String> names = resources.length > 0
                ? java.util.Arrays.stream(resources).map(Resource::getFilename).toList()
                : List.of();
        System.out.println("classpath*:*.properties 匹配到: " + names);
        assertTrue(names.contains("message_en_US.properties"));
        assertTrue(names.contains("message_zh_CN.properties"));
    }

    @Test
    public void conversionService() {
        // 容器级 ConversionService：注册了自定义 StringToOrderConverter
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(ConversionConfig.class)) {
            org.springframework.core.convert.ConversionService service =
                    ctx.getBean(org.springframework.core.convert.ConversionService.class);
            OrderRecord record = service.convert("SO-9001,88", OrderRecord.class);
            assertEquals("SO-9001", record.getOrderNo());
            assertEquals(88, record.getAmount());
            System.out.println("自定义 Converter: 'SO-9001,88' -> " + record);

            // DefaultConversionService 内置转换器：String -> List<Integer>（逗号分隔自动拆分）
            DefaultConversionService defaultService = new DefaultConversionService();
            defaultService.addConverter(new StringToOrderConverter());
            @SuppressWarnings("unchecked")
            List<Integer> numbers = (List<Integer>) defaultService.convert("1,2,3",
                    org.springframework.core.convert.TypeDescriptor.valueOf(String.class),
                    org.springframework.core.convert.TypeDescriptor.collection(List.class,
                            org.springframework.core.convert.TypeDescriptor.valueOf(Integer.class)));
            assertEquals(List.of(1, 2, 3), numbers);
            System.out.println("内置转换器: '1,2,3' -> " + numbers);
        }
    }

    @Test
    public void propertyEditorAndBeanWrapper() {
        // BeanWrapper + PropertyEditor：老式属性注入的底层机制
        OrderRecord target = new OrderRecord();
        BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(target);
        wrapper.setAutoGrowNestedPaths(false);
        // 直接演示自定义编辑器把字符串转对象
        OrderPropertyEditor editor = new OrderPropertyEditor();
        editor.setAsText("SO-777@66");
        OrderRecord converted = (OrderRecord) editor.getValue();
        assertEquals("SO-777", converted.getOrderNo());
        assertEquals(66, converted.getAmount());
        System.out.println("PropertyEditor: 'SO-777-66' -> " + converted);
        // BeanWrapper 对基本类型的自动转换（内置编辑器）
        wrapper.setPropertyValue("orderNo", "SO-100");
        wrapper.setPropertyValue("amount", "123");
        assertEquals(123, target.getAmount());
        System.out.println("BeanWrapper 自动类型转换: \"123\" -> Integer 123");
    }

    @Test
    public void dataBinderAndValidator() {
        // DataBinder：MutablePropertyValues 绑定 + Validator 校验
        OrderRecord target = new OrderRecord();
        DataBinder binder = new DataBinder(target, "order");
        binder.setValidator(new OrderValidator());
        binder.bind(new org.springframework.beans.MutablePropertyValues(
                java.util.Map.of("orderNo", "SO-9500", "amount", "-5")));
        binder.validate();
        BeanPropertyBindingResult result = (BeanPropertyBindingResult) binder.getBindingResult();
        // 绑定成功但校验失败：金额为负
        assertEquals("SO-9500", target.getOrderNo());
        assertEquals(-5, target.getAmount());
        assertEquals(1, result.getErrorCount());
        System.out.println("校验错误: " + result.getAllErrors().get(0).getDefaultMessage());
        assertTrue(result.getAllErrors().get(0).getDefaultMessage().contains("金额不能为负数"));
    }
}
