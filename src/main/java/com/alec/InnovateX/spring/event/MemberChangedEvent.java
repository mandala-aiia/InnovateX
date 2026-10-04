package com.alec.InnovateX.spring.event;

/** 固化泛型的具体子类：ResolvableType 能解析出 T = MemberPayload */
public class MemberChangedEvent extends EntityChangedEvent<MemberPayload> {

    public MemberChangedEvent(Object source, MemberPayload payload) {
        super(source, payload);
    }
}
