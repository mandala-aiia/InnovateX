package com.alec.InnovateX.spring.event;

/** 固化泛型的具体子类：ResolvableType 能解析出 T = UserPayload */
public class UserChangedEvent extends EntityChangedEvent<UserPayload> {

    public UserChangedEvent(Object source, UserPayload payload) {
        super(source, payload);
    }
}
