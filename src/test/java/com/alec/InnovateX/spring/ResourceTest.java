package com.alec.InnovateX.spring;

import com.alec.InnovateX.spring.resource.ClusterRegistry;
import com.alec.InnovateX.spring.resource.ConversionConfig;
import com.alec.InnovateX.spring.resource.ServerNode;
import com.alec.InnovateX.spring.resource.ServerNodePropertyEditor;
import com.alec.InnovateX.spring.resource.ServerNodeValidator;
import com.alec.InnovateX.spring.resource.StringToServerNodeConverter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.DataBinder;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 主题：资源加载（Resource/ResourceLoader 三前缀统一抽象 + Ant 通配）、
 * 类型转换（容器级 ConversionService + 自定义 Converter vs 老式 PropertyEditor + BeanWrapper）、
 * 数据绑定（DataBinder + MutablePropertyValues + Validator）。
 */
public class ResourceTest {

    @Test
    public void resourceLoaderUnifiedPrefixes() throws Exception {
        ResourceLoader loader = new DefaultResourceLoader();

        // 前缀一：classpath: —— 从类路径根定位（现有教学资源 annotation/annotation-app.properties）
        Resource classpathRes = loader.getResource("classpath:annotation/annotation-app.properties");
        assertTrue(classpathRes.exists(), "classpath: 资源应存在");
        String classpathContent = new String(classpathRes.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertTrue(classpathContent.contains("annotation.app.name=InnovateX"));
        System.out.println("[测试] classpath: → " + classpathRes.getFilename() + "，内容: "
                + classpathContent.replace("\n", " | "));

        // 前缀二：file: —— 文件系统绝对定位（用临时文件演示协议本身，断言与内容一致）
        Path temp = Files.createTempFile("innovatex-resource", ".properties");
        Files.writeString(temp, "demo.key=fromFileSystem\n", StandardCharsets.UTF_8);
        try {
            Resource fileRes = loader.getResource(temp.toUri().toURL().toExternalForm());
            assertTrue(fileRes.exists(), "file: 资源应存在");
            String fileContent = new String(fileRes.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(fileContent.contains("demo.key=fromFileSystem"));
            System.out.println("[测试] file: → " + fileRes.getFilename() + "，内容: " + fileContent.trim());
        } finally {
            Files.deleteIfExists(temp);
        }

        // 前缀三：无前缀 —— DefaultResourceLoader 退化为类路径语义（等价相对 classpath 根）
        Resource noPrefix = loader.getResource("message_zh_CN.properties");
        assertTrue(noPrefix.exists(), "无前缀应按 classpath 根解析到根路径资源");
        String noPrefixContent = new String(noPrefix.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertTrue(noPrefixContent.contains("app.message"));
        System.out.println("[测试] 无前缀 → " + noPrefix.getFilename() + "，内容: " + noPrefixContent.trim());
        // 三种定位方式拿到的是同一个 Resource 抽象（exists/getInputStream/getFilename 统一 API）
        assertEquals("message_zh_CN.properties", noPrefix.getFilename());
    }

    @Test
    public void antPatternAcrossClasspath() throws Exception {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        // classpath*：聚合所有类路径根（多模块 jar 也能合并）；*.properties：Ant 通配只匹配"根目录一层"
        Resource[] matched = resolver.getResources("classpath*:*.properties");
        List<String> names = Arrays.stream(matched)
                .map(Resource::getFilename)
                .collect(Collectors.toList());
        System.out.println("[测试] classpath*:*.properties 匹配到: " + names);
        // 根路径下的两个国际化资源必须被通配命中
        assertTrue(names.contains("message_en_US.properties"), "应匹配 message_en_US.properties");
        assertTrue(names.contains("message_zh_CN.properties"), "应匹配 message_zh_CN.properties");
        // 对比点：annotation/ 子目录里的 annotation-app.properties 不在 "*.properties"（单层）命中范围内
        assertFalse(names.contains("annotation-app.properties"),
                "单层通配不应命中子目录资源；想命中需 classpath*:annotation/*.properties");
    }

    @Test
    public void containerLevelConversionService() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(ConversionConfig.class)) {
            // 容器级 ConversionService：bean 名必须是 conversionService，容器按名字识别并全局启用
            ConversionService service = ctx.getBean(ConversionService.class);
            ServerNode node = service.convert("db.innovatex:3306", ServerNode.class);
            assertEquals("db.innovatex", node.getHost());
            assertEquals(3306, node.getPort());
            System.out.println("[测试] 自定义 Converter: 'db.innovatex:3306' → " + node);

            // 证据二：@Value 注入时容器自动用它完成 String → ServerNode（业务代码零感知）
            ClusterRegistry registry = ctx.getBean(ClusterRegistry.class);
            assertEquals("redis.innovatex", registry.getRedisNode().getHost());
            assertEquals(6379, registry.getRedisNode().getPort());
            System.out.println("[测试] @Value 自动转换: " + registry.report());

            // 对照：DefaultConversionService 自带的内置转换器（String → List<Integer> 按逗号拆分）
            DefaultConversionService defaults = new DefaultConversionService();
            defaults.addConverter(new StringToServerNodeConverter()); // 定制项仍可叠加
            @SuppressWarnings("unchecked")
            List<Integer> numbers = (List<Integer>) defaults.convert("1,2,3",
                    TypeDescriptor.valueOf(String.class),
                    TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(Integer.class)));
            assertEquals(List.of(1, 2, 3), numbers);
            System.out.println("[测试] 内置 StringToCollection: '1,2,3' → " + numbers);
        }
    }

    @Test
    public void propertyEditorAndBeanWrapper() {
        // 老式 PropertyEditor：setAsText 吃进字符串、getValue 吐出对象——状态就存在编辑器实例里
        ServerNodePropertyEditor editor = new ServerNodePropertyEditor();
        editor.setAsText("cache.innovatex#6380");
        ServerNode converted = (ServerNode) editor.getValue();
        assertEquals("cache.innovatex", converted.getHost());
        assertEquals(6380, converted.getPort());
        assertEquals("cache.innovatex#6380", editor.getAsText(), "getAsText 应能反向还原");
        System.out.println("[测试] PropertyEditor: 'cache.innovatex#6380' → " + converted);

        // BeanWrapper：Spring 属性访问/注入的底层引擎。自动类型转换走内置编辑器（String→Integer）
        ServerNode target = new ServerNode();
        BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(target);
        wrapper.setPropertyValue("host", "app.innovatex");
        wrapper.setPropertyValue("port", "8081"); // 字符串自动转 Integer
        assertEquals("app.innovatex", target.getHost());
        assertEquals(8081, target.getPort());
        System.out.println("[测试] BeanWrapper 自动转换: \"8081\" → Integer " + target.getPort());

        // 注册自定义编辑器后，BeanWrapper 也能做"字符串 → ServerNode"的嵌套属性转换
        wrapper.registerCustomEditor(ServerNode.class, new ServerNodePropertyEditor());
        wrapper.setPropertyValue("peer", "peer.innovatex#9042");
        assertEquals("peer.innovatex", target.getPeer().getHost());
        assertEquals(9042, target.getPeer().getPort());
        System.out.println("[测试] BeanWrapper + 自定义编辑器: peer=" + target.getPeer());
    }

    @Test
    public void dataBinderWithValidator() {
        // 反例：host 为空 + 端口越界 —— MutablePropertyValues 批量绑定后校验出两条错误
        ServerNode bad = new ServerNode();
        DataBinder badBinder = new DataBinder(bad, "serverNode");
        badBinder.setValidator(new ServerNodeValidator());
        badBinder.bind(new MutablePropertyValues(java.util.Map.of(
                "host", "   ",
                "port", "70000"))); // 字符串在绑定阶段被自动转换成 Integer
        badBinder.validate();
        BeanPropertyBindingResult badResult = (BeanPropertyBindingResult) badBinder.getBindingResult();
        assertEquals(2, badResult.getErrorCount(), "应恰好校验出两条错误");
        List<String> messages = badResult.getAllErrors().stream()
                .map(err -> "[" + err.getCode() + "] " + err.getDefaultMessage())
                .toList();
        messages.forEach(m -> System.out.println("[测试] 校验错误: " + m));
        assertTrue(messages.stream().anyMatch(m -> m.contains("主机名不能为空")));
        assertTrue(messages.stream().anyMatch(m -> m.contains("端口必须在 1~65535 之间")));

        // 正例：合法值 → 绑定成功且零校验错误（同一套 Validator 的另一面）
        ServerNode good = new ServerNode();
        DataBinder goodBinder = new DataBinder(good, "serverNode");
        goodBinder.setValidator(new ServerNodeValidator());
        goodBinder.bind(new MutablePropertyValues(java.util.Map.of("host", "api.innovatex", "port", "443")));
        goodBinder.validate();
        assertEquals(0, goodBinder.getBindingResult().getErrorCount());
        assertEquals("api.innovatex", good.getHost());
        assertEquals(443, good.getPort());
        System.out.println("[测试] 合法数据绑定: " + good + "，校验错误 0 条");
    }
}
