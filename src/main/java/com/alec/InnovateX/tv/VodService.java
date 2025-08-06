package com.alec.InnovateX.tv;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


@Service
public class VodService {

    private static final String BASE_URL = "https://91md.me/api.php/provide/vod?ac=videolist";

    public VodResponse searchVod(String wd, Integer page) {
        StringBuilder urlBuilder = new StringBuilder(BASE_URL);
        if (wd != null && !wd.trim().isEmpty()) {
            urlBuilder.append("&wd=").append(URLEncoder.encode(wd, StandardCharsets.UTF_8));
        }
        urlBuilder.append("&pg=").append(page);
        HttpRequest request = HttpRequest
                .get(urlBuilder.toString())
                .setHttpProxy("127.0.0.1", 7894);
        try (HttpResponse response = request.execute()) {
            return JSONUtil.toBean(response.body(), VodResponse.class, false);
        }
    }

    public VodDetailResponse detailVod(Integer ids) {
        HttpRequest request = HttpRequest
                .get(BASE_URL + "&ids=" + ids)
                .setHttpProxy("127.0.0.1", 7894);
        try (HttpResponse response = request.execute()) {
            return JSONUtil.toBean(response.body(), VodDetailResponse.class, false);
        }
    }
}