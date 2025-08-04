package com.alec.InnovateX.tv;


import lombok.Data;

@Data
public class VodItem {
    private String vod_id;
    private String vod_name;
    private String type_id;
    private String type_name;
    private String vod_en;
    private String vod_time;
    private String vod_remarks;
    private String vod_play_from;
}