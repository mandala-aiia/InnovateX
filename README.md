# InnovateX

Java 技术学习实验场（**纯 Spring Framework 6.2 + Java 21**）

所有 Spring 主题的容器都是测试里手工装配的 `AnnotationConfigApplicationContext` / `ClassPathXmlApplicationContext` /
`DispatcherServlet`，数据全部使用 H2 内存库——整个仓库**离线可完整构建与测试**。

## 模块索引

### Netty 网络编程（`com.alec.InnovateX.netty`）

按主题子包组织。`codec`/`core` 中标"无端口"的 demo 基于 EmbeddedChannel（由 `CodecTest`/`NettyCoreTest` 自动验证），其余为独立 main 类，手动运行：

| 子包 | Demo | 说明 |
|---|---|---|
| `netty.baseline` | `EchoServer`/`EchoClient` | TCP Echo（FixedLengthFrameDecoder 定长拆包） |
| | `NIOServer`/`NIOClient` | JDK 原生 NIO Selector（对照组） |
| | `UdpServer`/`UdpClient` | UDP DatagramPacket |
| `netty.codec` | `LineBasedCodecDemo` | 换行符拆包（无端口） |
| | `DelimiterCodecDemo` | 自定义分隔符拆包（无端口） |
| | `LengthFieldCodecDemo` | 长度字段拆包：Prepender+Decoder 参数详解、粘包证明（无端口） |
| | `CustomCodecDemo` | 自定义协议编解码：魔数/半包重试/粘包 + `ReplayingDecoder` 对照（无端口） |
| `netty.core` | `PipelineOrderDemo` | 入站/出站传播顺序、ctx.write vs channel.write（无端口） |
| | `IdleStateDemo` | IdleStateHandler 心跳检测（无端口） |
| | `ByteBufDemo` | 堆/直接内存、slice/duplicate/copy、引用计数（无端口） |
| | `FuturePromiseDemo` | Future/Promise、sync vs await（无端口） |
| | `ReactorModelDemo` | 单线程/多线程/主从 Reactor 三模型对照（8101-8103） |
| | `BusinessThreadDemo` | addLast(businessGroup, handler) 业务线程隔离（8200） |
| | `TcpParamDemo` | SO_BACKLOG/TCP_NODELAY/水位线与背压（8400，限速演示） |
| | `SharableHandlerDemo` | @Sharable 单例 handler 复用 vs 非 @Sharable 拒绝复用（无端口） |
| `netty.protocol` | `WebSocketServer`/`WebSocketClient` | WebSocket（8080） |
| | `MqttServer`/`MqttServerHandler`/`MqttSessionManager` | 手写 MQTT Broker（1883）：CONNECT/SUBSCRIBE/PUBLISH/DISCONNECT、**+/# 通配符订阅、QoS1 PUBACK、RETAINED 保留消息、PINGREQ 心跳**、内存会话管理（由 `MqttBrokerTest` 全流程自动验证） |
| | `HttpFileServer` | HTTP 静态文件服务器（8080，零拷贝 DefaultFileRegion） |
| | `FileUploadServer`/`FileUploadClient` | TCP 文件上传（9000） |

### Spring 底层机制（`com.alec.InnovateX.spring`）

全部按主题子包组织，每个主题一个测试类，自包含、不依赖外部环境（事务主题用 H2 内存库）。
其中"XML 装配"一列的类是原始 26 个 XML 时代 demo，由各主题测试类中的 `xmlXxx` 方法验证（通过共享工具 `XmlContexts` 加载 `spring-context.xml` 系列）。

| 子包 | 主题 | XML 装配的原始 demo | 测试类 |
|---|---|---|---|
| `spring.annotation` | 注解驱动装配：@ComponentScan/@Autowired/@Qualifier/@Primary/@Resource/@Value/@PostConstruct、自定义 @Qualifier 元注解、ObjectProvider 按需注入 | `AppInterface`/`AppAbsService*`（接口多实现按名注入） | `AnnotationTest` |
| `spring.javaconfig` | Java Config：full/lite 模式、@Import 三件套、@Conditional、@Profile/Environment、BeanDefinitionRegistryPostProcessor、@DependsOn/@Lazy、父子容器 | — | `JavaConfigTest` |
| `spring.extension` | 容器扩展点：BeanPostProcessor/InstantiationAwareBeanPostProcessor/BeanFactoryPostProcessor/ReaderEventListener/FactoryBean | `AppBeanPostProcessor`、`AppInstantiationAwareBeanPostProcessor`、`AppBeanFactoryPostProcessor`、`AppReaderEventListener`、`AppFactoryBean`+`AppFaBean` | `ExtensionTest` |
| `spring.scope` | 作用域：singleton/prototype、作用域代理、自定义 ThreadScope、循环依赖失败示例、@Lazy 打破循环、三级缓存早期引用验证 | `CircleA/B/C`（setter 循环依赖） | `ScopeTest` |
| `spring.aop` | AOP 深度：@AspectJ 五种通知、JDK vs CGLIB 对比、编程式 ProxyFactory、within/target/args/bean 切点、自调用失效与 currentProxy 修复、@DeclareParents 引介增强、Advised 动态增删通知、多切面 @Order 洋葱模型 | `AppAspect`、`AppPointcut`、`AppAnnotation`+`AppAnnotationAspect`（XML 五通知+注解切面） | `AopDeepTest` |
| `spring.transaction` | 事务深度（H2 内存库）：@Transactional、7 种传播行为、隔离级别与不可重复读、失效场景、@TransactionalEventListener、TransactionSynchronization 各阶段回调与事务-连接绑定、TransactionTemplate、JDBC 细节（batchUpdate/RowMapper/ResultSetExtractor/异常转译） | `AppJdbcTemplate`（XML `tx:advice` 声明式事务+编程式事务） | `TransactionDeepTest` |
| `spring.event` | 事件进阶：@EventListener、泛型事件、@Order、同步多播异常传播、@Async 异步事件、容器内置事件（refreshed/started/closed） | `AppEvent`+`AppEventListener`、`AppContextClosedListener` | `EventAdvancedTest` |
| `spring.aware` | Aware 全家桶：BeanFactory/Environment/ApplicationEventPublisher/MessageSource/ResourceLoader/EmbeddedValueResolver + ImportAware | `AppBeanNameAware`、`AppApplicationContextAware` | `AwareTest` |
| `spring.lifecycle` | 生命周期：SmartInitializingSingleton、SmartLifecycle（phase 启停顺序）、满配 Bean 全回调链（构造/@Autowired/Aware×3/@PostConstruct/afterPropertiesSet/initMethod/BPP 前后置/@PreDestroy/destroy/destroyMethod 共 13 步） | `App`（init/destroy 方法）、`AppDev`（InitializingBean）、`AppLifeCycleProcessor` | `SmartLifecycleTest` |
| `spring.xmladvanced` | Bean 定义进阶：bean 继承/alias/静态与实例工厂/lookup-method/replaced-method/集合注入/util:/lazy-init/autowire | — | `XmlAdvancedTest`（`spring-advanced.xml`） |
| `spring.spel` | SpEL：运算符/三元/Elvis/正则/方法/T()/集合投影筛选、@Value 中的 SpEL | — | `SpelTest` |
| `spring.resource` | 资源与类型转换：Resource/ResourceLoader/ResourcePatternResolver、ConversionService+自定义 Converter、PropertyEditor、DataBinder+Validator | — | `ResourceTest` |
| `spring.cache` | 缓存抽象：@EnableCaching + ConcurrentMapCacheManager、@Cacheable/@CachePut/@CacheEvict/@Caching | — | `CacheTest` |
| `spring.async` | 异步与调度：@EnableAsync/@Async（自定义线程池+异常兜底）、@EnableScheduling/@Scheduled（fixedDelay/cron） | — | `AsyncTest` |

## 数据源说明

所有数据库演示（`SpringCodeTest` 的 XML 数据源与 `TransactionDeepTest` 的 H2 建库）都使用 **H2 内存库**（PostgreSQL 兼容模式），
由 `transaction-context.xml` 的 `<jdbc:initialize-database>` / `TxConfig` 的 `EmbeddedDatabaseBuilder` 在容器刷新时自动建表，
不依赖任何外部数据库。XML 侧的连接池为 Druid（`spring-context.xml` 系列直接装配 `DruidDataSource`）。

## 运行

```bash
# 运行某个主题的验证测试
./mvnw test -Dtest=ScopeTest

# 运行全部主题测试（22 个测试类，完全离线）
./mvnw test

# Netty demo：直接在 IDE 里运行对应类的 main 方法
# 端口占用：8080 组（Echo/NIO/UDP/WebSocket/HttpFileServer，同时只能跑一个）、1883（MQTT）、
#          9000（文件上传）、8101-8103（Reactor 对照）、8200（业务线程隔离）、8400（TCP 参数）；
#          codec/core 中基于 EmbeddedChannel 的 demo 无端口，由 CodecTest/NettyCoreTest 自动验证
# 文件类 demo 说明：HttpFileServer 根目录默认 ~/Downloads（可用 args[0] 指定）；
#                  FileUploadClient 默认上传 ~/Downloads/netty-upload-demo.txt（不存在会自动生成）
```

另：`com.alec.InnovateX.thread.ThreadStateDemoDetailed` 为 JDK 线程状态演示（与 Netty 无关，独立成包）。
