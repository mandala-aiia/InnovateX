package com.alec.InnovateX.spring;

public class AppPointcut {

    public void appPointcutBefore() {
        System.out.println(" 执行 App PointcutBefore");
    }

    public String appPointcutReturning() {
        System.out.println(" 执行 App appPointcutReturning");
        return "oh  这是一个正常返回！！！！！";
    }

    public void appPointcutThrowing() {
        System.out.println(" 执行 App PointcutThrowing");
        throw new RuntimeException("oh 这是一个异常！！！！！");
    }


    public void appPointcutAround() {
        System.out.println(" 执行 App PointcutAround");
    }


    public void appPointcutAfter() {
        System.out.println(" 执行 App PointcutAfter");
    }

    @AppAnnotation("dfc5e814-4b1d-11ef-92f6-0242ac110002")
    public void appPointcutAnnotation() {
        System.out.println(" 执行 App appPointcutAnnotation");
    }
}
