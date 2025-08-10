package com.alec.InnovateX;

import com.alec.InnovateX.tv.ParseRule;
import com.alec.InnovateX.tv.ParseUtils;
import com.alec.InnovateX.tv.VodUrl;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

public class CodingTest {

    @Test
    public void codingTest() {
        String vod_play_url = "第01集$https://vodcnd11.rsfcxq.com/20250716/7X2JNTBP/index.m3u8#第02集$https://vodcnd11.rsfcxq.com/20250716/SUlD0QJD/index.m3u8#第03集$https://vodcnd11.rsfcxq.com/20250717/PQIf4miJ/index.m3u8#第04集$https://vodcnd11.rsfcxq.com/20250717/mS8w5afk/index.m3u8#第05集$https://vodcnd11.rsfcxq.com/20250718/urODMsvb/index.m3u8#第06集$https://vodcnd11.rsfcxq.com/20250718/KKFzDZOl/index.m3u8#第07集$https://vodcnd11.rsfcxq.com/20250722/ZXSZxcmK/index.m3u8#第08集$https://vodcnd11.rsfcxq.com/20250722/qEpVAAOI/index.m3u8#第09集$https://vodcnd11.rsfcxq.com/20250723/L9ansDQH/index.m3u8#第10集$https://vodcnd11.rsfcxq.com/20250723/RUIhlHBB/index.m3u8#第11集$https://vodcnd11.rsfcxq.com/20250725/C5DNYMdS/index.m3u8#第12集$https://vodcnd11.rsfcxq.com/20250725/anD7PvCz/index.m3u8#第13集$https://vodcnd11.rsfcxq.com/20250726/BoNKqBAb/index.m3u8#第14集$https://vodcnd11.rsfcxq.com/20250726/uw8eeyRh/index.m3u8#第15集$https://vodcnd11.rsfcxq.com/20250726/QC2vzsPN/index.m3u8#第16集$https://vodcnd11.rsfcxq.com/20250729/191FFFj8/index.m3u8#第17集$https://vodcnd11.rsfcxq.com/20250729/oTq7WhQz/index.m3u8#第18集$https://vodcnd10.rsfcxq.com/20250730/9Xp4Lj9X/index.m3u8#第19集$https://vodcnd10.rsfcxq.com/20250730/LTMGPhPR/index.m3u8#第20集$https://vodcnd10.rsfcxq.com/20250731/zcliP1yN/index.m3u8#第21集$https://vodcnd10.rsfcxq.com/20250731/pu1ZzrwM/index.m3u8#第22集$https://vodcnd10.rsfcxq.com/20250801/Z5AsvgKO/index.m3u8#第23集$https://vodcnd10.rsfcxq.com/20250801/wg7mXJlX/index.m3u8#第24集$https://vodcnd10.rsfcxq.com/20250801/eQYO1tB7/index.m3u8#第25集$https://vodcnd10.rsfcxq.com/20250801/5lZBixhK/index.m3u8#第26集$https://vodcnd11.rsfcxq.com/20250804/2dV64q9Y/index.m3u8#第27集$https://vodcnd11.rsfcxq.com/20250804/zJJyTsRF/index.m3u8#第28集$https://vodcnd10.rsfcxq.com/20250805/6CzWKtwj/index.m3u8#第29集$https://vodcnd10.rsfcxq.com/20250805/Vcaqzuvk/index.m3u8#第30集$https://vodcnd11.rsfcxq.com/20250806/m2pPDexU/index.m3u8#第31集$https://vodcnd11.rsfcxq.com/20250806/3iXT67in/index.m3u8#第32集$https://vodcnd10.rsfcxq.com/20250807/fRMZ5gcG/index.m3u8#第33集$https://vodcnd10.rsfcxq.com/20250808/76M8MLTv/index.m3u8#第34集$https://vodcnd10.rsfcxq.com/20250809/HaxNgTyQ/index.m3u8$$$第01集$https://vodcnd11.rsfcxq.com/share/apQuuVyEuS#第02集$https://vodcnd11.rsfcxq.com/share/xXVxvJ9GiB#第03集$https://vodcnd11.rsfcxq.com/share/4FAUQNUBv3#第04集$https://vodcnd11.rsfcxq.com/share/PEnKQn3yJm#第05集$https://vodcnd11.rsfcxq.com/share/fYE2Kwhn9N#第06集$https://vodcnd11.rsfcxq.com/share/7XZc9rDEqr#第07集$https://vodcnd11.rsfcxq.com/share/nbD1Fb2DHE#第08集$https://vodcnd11.rsfcxq.com/share/ToKytFQe88#第09集$https://vodcnd11.rsfcxq.com/share/NPs1XaBtL8#第10集$https://vodcnd11.rsfcxq.com/share/Ze4grU1nIG#第11集$https://vodcnd11.rsfcxq.com/share/YKhLOMK54a#第12集$https://vodcnd11.rsfcxq.com/share/iG2fdztlSD#第13集$https://vodcnd11.rsfcxq.com/share/wSdgv0hR7V#第14集$https://vodcnd11.rsfcxq.com/share/3OSNRr5SDj#第15集$https://vodcnd11.rsfcxq.com/share/5KZet50iKI#第16集$https://vodcnd11.rsfcxq.com/share/ACPW2T2TeX#第17集$https://vodcnd11.rsfcxq.com/share/FUcJWoJCyZ#第18集$https://vodcnd10.rsfcxq.com/share/ChpLvgmiFW#第19集$https://vodcnd10.rsfcxq.com/share/tWKmUB1gGb#第20集$https://vodcnd10.rsfcxq.com/share/4hd3KpjbZo#第21集$https://vodcnd10.rsfcxq.com/share/znJDMJhR3W#第22集$https://vodcnd10.rsfcxq.com/share/xwXBUVoVY1#第23集$https://vodcnd10.rsfcxq.com/share/jL0moEKmhc#第24集$https://vodcnd10.rsfcxq.com/share/sRALTBZJRv#第25集$https://vodcnd10.rsfcxq.com/share/N2Xj8j020d#第26集$https://vodcnd11.rsfcxq.com/share/IWgQFrzhVS#第27集$https://vodcnd11.rsfcxq.com/share/KdDXHMVzW2#第28集$https://vodcnd10.rsfcxq.com/share/T4ydMU7DHq#第29集$https://vodcnd10.rsfcxq.com/share/KVWjkXHrO2#第30集$https://vodcnd11.rsfcxq.com/share/ZOs8J2s38b#第31集$https://vodcnd11.rsfcxq.com/share/LHlmt5D4p7#第32集$https://vodcnd10.rsfcxq.com/share/siMKT2fc3h#第33集$https://vodcnd10.rsfcxq.com/share/vYdkQgbq79#第34集$https://vodcnd10.rsfcxq.com/share/8w8RzjJTs0"; // 省略长串

        ParseRule doubanRule = new ParseRule(
                "\\$\\$\\$", // lineSeparator，豆瓣用 $$$ 分隔线路
                "#",         // episodeSeparator，集数用 #
                "\\$",       // partSeparator，标题和地址用 $
                true,        // 只取 m3u8
                1,           // 取第0条线路（m3u8的线路）
                0,           // 标签下标
                1            // 地址下标
        );

        List<VodUrl> parsedList = ParseUtils.parseVodPlayUrl(vod_play_url, doubanRule);
        for (VodUrl vodUrl : parsedList) {
            System.out.println(vodUrl.getLabel() + " -> " + vodUrl.getUrl());
        }
    }
}
