package com.alec.InnovateX.tv;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;


@Service
@RequiredArgsConstructor
public class VodService {

    private final VodUtils vodUtils;
    private static final String acVideo = "?ac=videolist";

    public VodResponse vod() {
        List<VodConfig> configArray = vodUtils.getConfigArray();
        VodConfig vodConfig = configArray.stream().filter(VodConfig::getPrimary).findFirst().orElse(null);
        assert vodConfig != null;
        HttpRequest request = HttpRequest.get(vodConfig.getApi());
        try (HttpResponse response = request.execute()) {
            JSONObject jsonObject = JSONUtil.parseObj(response.body());
            // 把class字段重命名为clazz
            jsonObject.set("clazz", jsonObject.get("class"));
            jsonObject.remove("class");
            return JSONUtil.toBean(jsonObject, VodResponse.class);

        }
    }

    public VodResponse vodVideoList(Integer type_id, Integer page,Integer vod_id) {
        List<VodConfig> configArray = vodUtils.getConfigArray();
        VodConfig vodConfig = configArray.stream().filter(VodConfig::getPrimary).findFirst().orElse(null);
        assert vodConfig != null;
        StringBuilder url = new StringBuilder(vodConfig.getApi() + acVideo);
        url.append("&t=").append(type_id);
        url.append("&pg=").append(page);
        if(vod_id != null) {
            url.append("&ids=").append(vod_id);
        }
        HttpRequest request = HttpRequest.get(url.toString());
        try (HttpResponse responseClass = request.execute()) {
            VodResponse vodResponse = JSONUtil.toBean(responseClass.body(), VodResponse.class);
            for (VodInfo vodInfo : vodResponse.getList()) {
                vodInfo.setVod_play_url_list(ParseUtils.parseVodPlayUrl(vodInfo.getVod_play_url(), vodConfig.getPlayUrlParseRule()));
            }
            return vodResponse;
        }
    }
}