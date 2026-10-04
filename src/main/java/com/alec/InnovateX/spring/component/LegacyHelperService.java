package com.alec.InnovateX.spring.component;

import org.springframework.stereotype.Component;

/** 类名含 "Legacy"，供 FilterType.REGEX 正则过滤演示（正则匹配的是全限定类名）。 */
@Component
public class LegacyHelperService {

    public String help() {
        return "遗留工具";
    }
}
