package com.alec.InnovateX.spring.component;

import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

import java.io.IOException;

/**
 * 自定义 TypeFilter（FilterType.CUSTOM）：类名含 "Trial" 的候选一律排除。
 * match 返回 true 的语义取决于用在 include 还是 exclude 过滤器上。
 */
public class CustomTypeFilter implements TypeFilter {

    @Override
    public boolean match(MetadataReader reader, MetadataReaderFactory factory) throws IOException {
        ClassMetadata metadata = reader.getClassMetadata();
        return metadata.getClassName().contains("Trial");
    }
}
