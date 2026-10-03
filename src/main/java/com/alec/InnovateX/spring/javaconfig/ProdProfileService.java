package com.alec.InnovateX.spring.javaconfig;

/** prod profile 激活时生效 */
public class ProdProfileService implements ProfileService {

    @Override
    public String env() {
        return "prod";
    }
}
