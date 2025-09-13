package com.alec.InnovateX;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;


@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class InnovateXApplication {
    public static void main(String[] args) {
        SpringApplication.run(InnovateXApplication.class, args);
    }
}
