package com.alec.InnovateX.spring.event;

/**
 * 泛型事件载荷：会员。
 */
public class MemberPayload {

    private final String username;
    private final boolean active;

    public MemberPayload(String username, boolean active) {
        this.username = username;
        this.active = active;
    }

    public String getUsername() {
        return username;
    }

    public boolean isActive() {
        return active;
    }
}
