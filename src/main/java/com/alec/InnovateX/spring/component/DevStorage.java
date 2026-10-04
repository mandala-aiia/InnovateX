package com.alec.InnovateX.spring.component;

/** dev 环境存储实现：仅 dev profile 激活时注册。 */
public class DevStorage implements Storage {

    @Override
    public String describe() {
        return "开发环境存储(本地内存)";
    }
}
