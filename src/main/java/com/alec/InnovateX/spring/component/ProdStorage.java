package com.alec.InnovateX.spring.component;

/** prod 环境存储实现：仅 prod profile 激活时注册。 */
public class ProdStorage implements Storage {

    @Override
    public String describe() {
        return "生产环境存储(专用集群)";
    }
}
