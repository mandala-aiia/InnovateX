package com.alec.InnovateX.spring.testctx;

/** TestContext 框架演示的被测 Bean：无状态、结果可断言 */
public class GreetingService {

    public String greet(String name) {
        return "hello " + name + " (from TestContext)";
    }
}
