package com.alec.InnovateX.spring.javaconfig;

/** prod profile 激活时才生效的实现 */
public class ProdDeployService implements DeployService {

    public ProdDeployService() {
        System.out.println("[ProdDeployService] 创建（prod profile 激活）");
    }

    @Override
    public String environment() {
        return "prod";
    }
}
