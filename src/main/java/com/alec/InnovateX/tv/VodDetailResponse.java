package com.alec.InnovateX.tv;

import lombok.Data;

import java.util.List;

@Data
public class VodDetailResponse {
    private Integer code;
    private String msg;
    private Integer page;
    private Integer pagecount;
    private String limit;
    private Integer total;
    private List<VodInfo> list;
}
