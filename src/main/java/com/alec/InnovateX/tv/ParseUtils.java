package com.alec.InnovateX.tv;

import java.util.ArrayList;
import java.util.List;

public class ParseUtils {

    public static List<VodUrl> parseVodPlayUrl(String vodPlayUrl, ParseRule rule) {
        List<VodUrl> result = new ArrayList<>();
        if (vodPlayUrl == null || vodPlayUrl.isEmpty()) {
            return result;
        }
        String[] lines = vodPlayUrl.split(rule.getLineSeparator());
        if (rule.getTargetLineIndex() >= lines.length) {
            return result;
        }
        String targetLine = lines[rule.getTargetLineIndex()];
        String[] episodes = targetLine.split(rule.getEpisodeSeparator());
        for (String episode : episodes) {
            String[] parts = episode.split(rule.getPartSeparator());
            if (parts.length > Math.max(rule.getLabelIndex(), rule.getUrlIndex())) {
                String label = parts[rule.getLabelIndex()].trim();
                String url = parts[rule.getUrlIndex()].trim();
                if (rule.isM3u8Only()) {
                    if (!url.endsWith(".m3u8")) {
                        continue;
                    }
                }
                VodUrl vodUrl = new VodUrl();
                vodUrl.setLabel(label);
                vodUrl.setUrl(url);
                result.add(vodUrl);
            }
        }
        return result;
    }

}
