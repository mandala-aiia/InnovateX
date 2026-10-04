package com.alec.InnovateX.spring.resource;

/**
 * 类型转换 / 数据绑定 / 校验三者共同的"目标对象"：一台服务器节点描述。
 * <p>
 * 同一个类分别喂给三套机制，形成清晰对照：
 * <ul>
 *   <li>ConversionService + 自定义 Converter（新体系，无状态）；</li>
 *   <li>PropertyEditor（老体系，有状态）；</li>
 *   <li>DataBinder + Validator（绑定 + 校验一条龙）。</li>
 * </ul>
 * peer 字段是 ServerNode 自身类型——专门留给 BeanWrapper 注册自定义 PropertyEditor 后做
 * "字符串 → ServerNode"的嵌套转换演示。
 */
public class ServerNode {

    private String host;

    private Integer port;

    private ServerNode peer;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public ServerNode getPeer() {
        return peer;
    }

    public void setPeer(ServerNode peer) {
        this.peer = peer;
    }

    @Override
    public String toString() {
        return "ServerNode{host='" + host + "', port=" + port
                + (peer != null ? ", peer=" + peer.host + ":" + peer.port : "") + "}";
    }
}
