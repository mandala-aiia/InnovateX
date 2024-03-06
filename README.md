# InnovateX
===============================================================================================
创建型模式：用于描述“怎样创建对象”，它的主要特点是“将对象的创建与使用分离”。GoF 中提供了单例、原型、工厂方法、抽象工厂、建造者等 5 种创建型模式。
结构型模式：用于描述如何将类或对象按某种布局组成更大的结构，GoF 中提供了代理、适配器、桥接、装饰、外观、享元、组合等 7 种结构型模式。
行为型模式：用于描述类或对象之间怎样相互协作共同完成单个对象都无法单独完成的任务，以及怎样分配职责。GoF
中提供了模板方法、策略、命令、职责链、状态、观察者、中介者、迭代器、访问者、备忘录、解释器等 11 种行为型模式。
===============================================================================================
基本功：数据结构与算法，计算机网络，设计模式，操作系统
java基础：JVM，JDK(并发框架、IO/NIO框架(参考netty)、集合框架)
应用开发框架：spring系列，mybatis
web容器：tomcat，nginx
RPC框架：dubbo
消息中间件：kafka，rocketMQ
注册中心：zookeeper
配置中心框架：apollo/nacos
数据库：mysql，redis
分布式系列：分布式ID，分布式锁，分布式事务，分布式API网关
云原生：Kubernetes
开发工具：maven，git
===============================================================================================
pick：保留该commit（缩写:p）
reword：保留该commit，但我需要修改该commit的注释（缩写:r）
edit：保留该commit, 但我要停下来修改该提交(不仅仅修改注释)（缩写:e）
squash：将该commit和前一个commit合并（缩写:s）
fixup：将该commit和前一个commit合并，但我不要保留该提交的注释信息（缩写:f）
exec：执行shell命令（缩写:x）
drop：我要丢弃该commit（缩写:d）
===============================================================================================
id		         该SELECT标识符
select_type		 该SELECT类型
table		     输出行的表
partitions		 匹配的分区
type		     联接类型
possible_keys	 可供选择的可能索引
key		         实际选择的索引
key_len		     所选密钥的长度
ref		         与索引比较的列
rows		     要检查的行的估计
filtered		 按表条件过滤的行百分比
Extra		     附加信息
===============================================================================================