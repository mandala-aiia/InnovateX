package com.alec.InnovateX.spring.aop;

import java.util.ArrayList;
import java.util.List;

/** 引介增强的目标类：注意它没有实现 Lockable，也不感知锁的存在 */
public class DocumentService {

    private final List<String> contents = new ArrayList<>();

    public void write(String content) {
        contents.add(content);
        System.out.println("[DocumentService] 写入: " + content);
    }

    public List<String> getContents() {
        return contents;
    }
}
