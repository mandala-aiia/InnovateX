package com.alec.InnovateX.spring.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class AppEvent extends ApplicationEvent {

    private String message;

    public AppEvent(Object source, String message) {
        super(source);
        this.message = message;
    }
}
