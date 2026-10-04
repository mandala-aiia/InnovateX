package com.alec.InnovateX.spring.component;

import org.springframework.stereotype.Component;

/** 类名含 "Trial"，供 FilterType.CUSTOM 自定义 TypeFilter 演示排除。 */
@Component
public class TrialFeatureService {

    public String feature() {
        return "试验特性";
    }
}
