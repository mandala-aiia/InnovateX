# InnovateX

Java 技术学习实验场（Spring Boot 3 + Java 21）。

## 模块索引

### Netty 网络编程（`com.alec.InnovateX.netty`）

| Demo | 说明 |
|---|---|
| `EchoServer`/`EchoClient` | TCP Echo（FixedLengthFrameDecoder 拆包） |
| `NIOServer`/`NIOClient` | JDK 原生 NIO Selector |
| `UdpServer`/`UdpClient` | UDP DatagramPacket |
| `WebSocketServer`/`WebSocketClient` | WebSocket（端口 8080） |
| `MqttServer`/`MqttServerHandler`/`MqttSessionManager` | 手写 MQTT Broker（端口 1883）：CONNECT/SUBSCRIBE/PUBLISH/DISCONNECT + 内存会话管理 |
| `HttpFileServer` | HTTP 静态文件服务器（零拷贝 DefaultFileRegion） |
| `FileUploadServer`/`FileUploadClient` | TCP 文件上传（端口 9000） |
| `AsyncController` | 长轮询 REST API（DeferredResult，随 Spring 启动） |

### Spring 底层机制（`com.alec.InnovateX.spring`）

全部按主题子包组织，每个主题一个测试类，自包含、不依赖外部环境（事务主题除外，用 H2 内存库）。
其中"XML 装配"一列的类是原始 26 个 XML 时代 demo，由 `SpringCodeTest` 配合 `spring-context.xml` 系列验证（依赖外网 PostgreSQL）。

| 子包 | 主题 | XML 装配的原始 demo | 测试类 |
|---|---|---|---|
| `spring.annotation` | 注解驱动装配：@ComponentScan/@Autowired/@Qualifier/@Primary/@Resource/@Value/@PostConstruct、自定义 @Qualifier 元注解、ObjectProvider 按需注入 | `AppInterface`/`AppAbsService*`（接口多实现按名注入） | `AnnotationTest` |
| `spring.javaconfig` | Java Config：full/lite 模式、@Import 三件套、@Conditional、@Profile/Environment、BeanDefinitionRegistryPostProcessor、@DependsOn/@Lazy、父子容器 | — | `JavaConfigTest` |
| `spring.extension` | 容器扩展点：BeanPostProcessor/InstantiationAwareBeanPostProcessor/BeanFactoryPostProcessor/ReaderEventListener/FactoryBean | `AppBeanPostProcessor`、`AppInstantiationAwareBeanPostProcessor`、`AppBeanFactoryPostProcessor`、`AppReaderEventListener`、`AppFactoryBean`+`AppFaBean` | `SpringCodeTest`（XML） |
| `spring.scope` | 作用域：singleton/prototype、作用域代理、自定义 ThreadScope、循环依赖失败示例、@Lazy 打破循环、三级缓存早期引用验证 | `CircleA/B/C`（setter 循环依赖） | `ScopeTest` |
| `spring.aop` | AOP 深度：@AspectJ 五种通知、JDK vs CGLIB 对比、编程式 ProxyFactory、within/target/args/bean 切点、自调用失效与 currentProxy 修复、@DeclareParents 引介增强、Advised 动态增删通知、多切面 @Order 洋葱模型 | `AppAspect`、`AppPointcut`、`AppAnnotation`+`AppAnnotationAspect`（XML 五通知+注解切面） | `AopDeepTest` |
| `spring.transaction` | 事务深度（H2 内存库）：@Transactional、7 种传播行为、隔离级别与不可重复读、失效场景、@TransactionalEventListener、TransactionSynchronization 各阶段回调与事务-连接绑定、TransactionTemplate、JDBC 细节（batchUpdate/RowMapper/ResultSetExtractor/异常转译） | `AppJdbcTemplate`（XML `tx:advice` 声明式事务+编程式事务，走外网 PG） | `TransactionDeepTest` |
| `spring.event` | 事件进阶：@EventListener、泛型事件、@Order、同步多播异常传播、@Async 异步事件、容器内置事件（refreshed/started/closed） | `AppEvent`+`AppEventListener`、`AppContextClosedListener` | `EventAdvancedTest` |
| `spring.aware` | Aware 全家桶：BeanFactory/Environment/ApplicationEventPublisher/MessageSource/ResourceLoader/EmbeddedValueResolver + ImportAware | `AppBeanNameAware`、`AppApplicationContextAware` | `AwareTest` |
| `spring.lifecycle` | 生命周期：SmartInitializingSingleton、SmartLifecycle（phase 启停顺序）、满配 Bean 全回调链（构造/@Autowired/Aware×3/@PostConstruct/afterPropertiesSet/initMethod/BPP 前后置/@PreDestroy/destroy/destroyMethod 共 13 步） | `App`（init/destroy 方法）、`AppDev`（InitializingBean）、`AppLifeCycleProcessor` | `SmartLifecycleTest` |
| `spring.xmladvanced` | Bean 定义进阶：bean 继承/alias/静态与实例工厂/lookup-method/replaced-method/集合注入/util:/lazy-init/autowire | — | `XmlAdvancedTest`（`spring-advanced.xml`） |
| `spring.spel` | SpEL：运算符/三元/Elvis/正则/方法/T()/集合投影筛选、@Value 中的 SpEL | — | `SpelTest` |
| `spring.resource` | 资源与类型转换：Resource/ResourceLoader/ResourcePatternResolver、ConversionService+自定义 Converter、PropertyEditor、DataBinder+Validator | — | `ResourceTest` |
| `spring.cache` | 缓存抽象：@EnableCaching + ConcurrentMapCacheManager、@Cacheable/@CachePut/@CacheEvict/@Caching | — | `CacheTest` |
| `spring.async` | 异步与调度：@EnableAsync/@Async（自定义线程池+异常兜底）、@EnableScheduling/@Scheduled（fixedDelay/cron） | — | `AsyncTest` |
| `spring.mvc` | Spring MVC（纯手工装配 DispatcherServlet，不依赖 Boot）：请求流程、Filter vs Interceptor 顺序、@RestControllerAdvice、内容协商+自定义 HttpMessageConverter、自定义 HandlerMethodArgumentResolver、@Validated+@InitBinder 校验集成、自定义返回值处理器、multipart 上传、Callable/SSE 异步、RestTemplate+MockRestServiceServer | — | `MvcDeepTest` |

事务主题使用 H2 内存数据库（test scope），其余主题无外部依赖。

## 运行

```bash
# 运行某个主题的验证测试
./mvnw test -Dtest=ScopeTest

# 运行全部新增主题测试（13 个测试类）
./mvnw test -Dtest='AnnotationTest,JavaConfigTest,ScopeTest,AopDeepTest,TransactionDeepTest,EventAdvancedTest,AwareTest,SmartLifecycleTest,XmlAdvancedTest,SpelTest,ResourceTest,CacheTest,AsyncTest'
```

注意：既有的 `SpringCodeTest` 依赖外部 PostgreSQL（`application-druid.yml`），需要网络连通才能通过。
