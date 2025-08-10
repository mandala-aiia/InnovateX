package com.alec.InnovateX.tv;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParseRule {
    private String lineSeparator;     // 线路分隔符
    private String episodeSeparator;  // 集数分隔符
    private String partSeparator;     // 标签和地址分隔符
    private boolean m3u8Only;         // 是否只保留 m3u8 链接
    private int targetLineIndex;      // 取第几条线路
    private int labelIndex;           // 标签所在下标
    private int urlIndex;             // 地址所在下标
}
