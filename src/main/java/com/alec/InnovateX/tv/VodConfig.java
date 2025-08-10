package com.alec.InnovateX.tv;

import lombok.Data;

@Data
public class VodConfig {
    private String api;
    private String name;
    private Boolean primary;
    private ParseRule playUrlParseRule;
}
