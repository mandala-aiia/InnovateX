# InnovateX

Java 技术学习实验场（**纯 Spring Framework 7.0 + Java 21**）

## Spring 基础学习主题（core / bean / context）

所有容器都在测试里手动装配（`AnnotationConfigApplicationContext` 为主，try-with-resources 管理），
支撑类放 `src/main/java/com/alec/InnovateX/spring/<主题>/`，一个主题一个测试类。

| 主题 | 子包 | 测试类 | 知识点 |
|---|---|---|---|
| IoC 容器 | `spring.ioc` | `IocContainerTest` | @Configuration/@Bean、registerBean、BeanDefinitionBuilder、getBean 重载与异常、ObjectProvider、getBeanNamesForType 不实例化 vs getBeansOfType 实例化、FactoryBean（& 前缀）、别名、BFPP 在 AC 与裸 BeanFactory 的执行差异、XML 一瞥 |
| 组件扫描 | `spring.component` | `ComponentScanTest` | @Component 派生与自定义组合注解、过滤器（ANNOTATION/ASSIGNABLE_TYPE/REGEX/CUSTOM/白名单）、full vs lite 配置类、@Import 与 ImportSelector、@Conditional、@Profile、lazyInit |
| 依赖注入 | `spring.di` | `DependencyInjectionTest` | 构造器/Setter/字段注入、@Primary/@Qualifier/@Resource、可选依赖四写法、List/Map/泛型注入、@Value（占位符/默认值/SpEL）、构造器与 Setter 循环依赖及 allowCircularReferences |
| 生命周期 | `spring.lifecycle` | `LifecycleTest` | 初始化/销毁全链路顺序、initMethod/destroyMethod、BeanPostProcessor（含替换 bean）、DestructionAwareBPP、BFPP 改定义、@Lazy、@DependsOn、SmartLifecycle phase 顺序 |
| 作用域 | `spring.scope` | `ScopeTest` | singleton/prototype、单例注入原型的冻结陷阱与三种解法（ObjectProvider/ObjectFactory/@Lookup）、SimpleThreadScope 自定义作用域、scoped proxy |
| Aware 回调 | `spring.aware` | `AwareTest` | 8 个 Aware 接口的触发顺序与注入能力、构造器注入容器能力的替代写法 |

运行单个主题：

```bash
./mvnw test -Dtest=LifecycleTest
```

运行全部：`./mvnw test`

### 已遇到的 Spring 7 API 变化（学习笔记）

- `ComponentScan.FilterType` 在 Spring 7 提升为顶层枚举 `org.springframework.context.annotation.FilterType`
- `EmbeddedValueResolverAware` 位于 `org.springframework.context` 包（不在 beans.factory.config）
- `allowCircularReferences` 默认值在裸框架仍是 `true`；`false` 是 Spring Boot 2.6+ 的装配默认
- `@PostConstruct/@PreDestroy/@Resource` 需要 `jakarta.annotation-api`（Spring 7 的 Jakarta EE 11 基线）

## 后续主题（计划）

event 事件机制、environment 属性环境、resource 资源加载与 MessageSource、SpEL、
extension 扩展点深入（BeanDefinitionRegistryPostProcessor / ImportBeanDefinitionRegistrar /
InstantiationAwareBeanPostProcessor）、spring-test 声明式上下文、AOP、事务。
