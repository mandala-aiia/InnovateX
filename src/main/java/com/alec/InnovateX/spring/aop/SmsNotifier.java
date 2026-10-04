package com.alec.InnovateX.spring.aop;

/**
 * 没有任何接口的通知类——CGLIB 代理路线的靶子。
 * JDK 代理只能"代表接口"，无接口可代表时 Spring 自动退回 CGLIB：
 * 运行期生成 SmsNotifier 的子类（类名带 $$SpringCGLIB$$），把拦截器链织进子类方法里。
 * 代价：依赖继承——所以 final 类 / final 方法 / private 方法都无法被 CGLIB 增强。
 */
public class SmsNotifier {

    public String send(String orderNo) {
        System.out.println("[SmsNotifier] send: 订单 " + orderNo + " 已短信通知");
        return "sms:" + orderNo;
    }
}
