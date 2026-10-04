package com.alec.InnovateX.spring.ioc;

/** FactoryBean 的「产品」：一张问候卡片。 */
public class GreetingCard {

    private final String message;

    public GreetingCard(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
