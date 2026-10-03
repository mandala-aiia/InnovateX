package com.alec.InnovateX.netty.codec;

import java.util.Objects;

/**
 * 自定义协议的 POJO 载荷：pipeline 里直接进出 ProtocolMessage 对象，
 * 编解码细节被封装在 MessageEncoder/MessageDecoder 里（对业务 handler 透明）
 *
 * 报文格式：| 魔数 1B | 类型 1B | 长度 4B | 业务体 NB |
 */
public class ProtocolMessage {

    /** 协议魔数：快速识别非法报文 */
    public static final byte MAGIC = 0x59;

    public static final byte TYPE_HEARTBEAT = 0;

    public static final byte TYPE_BUSINESS = 1;

    private final byte type;

    private final String body;

    public ProtocolMessage(byte type, String body) {
        this.type = type;
        this.body = body;
    }

    public byte getType() {
        return type;
    }

    public String getBody() {
        return body;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProtocolMessage that)) return false;
        return type == that.type && Objects.equals(body, that.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, body);
    }

    @Override
    public String toString() {
        return "ProtocolMessage{type=" + type + ", body='" + body + "'}";
    }
}
