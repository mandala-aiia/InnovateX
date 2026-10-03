package com.alec.InnovateX.spring.mvc;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 自定义 HttpMessageConverter：支持 text/csv 格式的 Person 读写。
 * 内容协商（Accept 头）命中 text/csv 时，@ResponseBody 的返回值走这里而不是 Jackson；
 * 测试里 Accept: application/json 与 text/csv 会分别产出 JSON 和 CSV 两种响应体
 */
public class PersonCsvHttpMessageConverter extends AbstractHttpMessageConverter<Person> {

    public PersonCsvHttpMessageConverter() {
        super(new MediaType("text", "csv"), new MediaType("application", "csv"));
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return Person.class.isAssignableFrom(clazz);
    }

    @Override
    protected Person readInternal(Class<? extends Person> clazz, HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {
        String body = new String(inputMessage.getBody().readAllBytes(), StandardCharsets.UTF_8).trim();
        String[] parts = body.split(",");
        return new Person(Long.parseLong(parts[0]), parts[1]);
    }

    @Override
    protected void writeInternal(Person person, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {
        outputMessage.getBody().write((person.id() + "," + person.name()).getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected Long getContentLength(Person person, MediaType contentType) {
        return (long) (person.id() + "," + person.name()).length();
    }
}
