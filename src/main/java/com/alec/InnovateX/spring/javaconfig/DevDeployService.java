package com.alec.InnovateX.spring.javaconfig;

/** dev profile 激活时才生效的实现 */
public class DevDeployService implements DeployService {

    public DevDeployService() {
        System.out.println("[DevDeployService] 创建（dev profile 激活）");
    }

    @Override
    public String environment() {
        return "dev";
    }
}
