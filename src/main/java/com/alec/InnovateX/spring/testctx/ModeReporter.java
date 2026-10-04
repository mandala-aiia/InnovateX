package com.alec.InnovateX.spring.testctx;

/** @Profile 演示载体：default / lab 两个装配版本，由测试类的 @ActiveProfiles 决定谁生效 */
public class ModeReporter {

    private final String mode;

    public ModeReporter(String mode) {
        this.mode = mode;
    }

    public String mode() {
        return mode;
    }
}
