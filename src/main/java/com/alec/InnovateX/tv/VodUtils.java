package com.alec.InnovateX.tv;


import cn.hutool.json.JSONUtil;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class VodUtils {

    @Value("classpath:config.json")
    private Resource jsonFile;

    @Getter
    private List<VodConfig> configArray;

    @PostConstruct
    public void init() throws Exception {
        String jsonStr = new String(jsonFile.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        configArray = JSONUtil.toList(jsonStr, VodConfig.class);
    }
}
