package com.alec.InnovateX.spring.mvc;

import org.springframework.web.client.RestTemplate;

/** RestTemplate 客户端演示：测试里用 MockRestServiceServer 绑定它，不发出真实网络请求 */
public class PersonRestClient {

    private final RestTemplate restTemplate;

    public PersonRestClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Person fetchPerson(String url) {
        // getForObject 内部：请求执行 -> HttpMessageConverter 反序列化 -> Person
        return restTemplate.getForObject(url, Person.class);
    }
}
