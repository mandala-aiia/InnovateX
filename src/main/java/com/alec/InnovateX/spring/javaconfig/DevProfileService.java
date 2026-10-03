package com.alec.InnovateX.spring.javaconfig;

/** dev profile 激活时生效 */
public class DevProfileService implements ProfileService {

    @Override
    public String env() {
        return "dev";
    }
}
