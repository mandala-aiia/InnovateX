package com.alec.InnovateX.spring.resource;

/**
 * 消费"容器级 ConversionService"的业务 Bean：
 * @Value 注入的是字符串 "redis.innovatex:6379"，而参数类型是 ServerNode——
 * 转换由容器自动完成（先查 PropertyEditor，没有则回落到名为
 * conversionService 的 ConversionService，命中我们注册的 StringToServerNodeConverter）。
 * <p>
 * 这一步的意义：Converter 不必在业务代码里手动调用，容器在属性注入时就会用它。
 */
public class ClusterRegistry {

    private final ServerNode redisNode;

    public ClusterRegistry(ServerNode redisNode) {
        this.redisNode = redisNode;
        System.out.println("[ClusterRegistry] 注入时已自动转换: 'redis.innovatex:6379' -> " + redisNode);
    }

    public ServerNode getRedisNode() {
        return redisNode;
    }

    public String report() {
        return "ClusterRegistry 追踪的 Redis 节点 = " + redisNode.getHost() + ":" + redisNode.getPort();
    }
}
