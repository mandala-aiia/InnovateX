package com.alec.InnovateX.spring.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

/**
 * 自调用实验的观测切面：只盯 TransferTicketService.record(..)。
 * 它是否被触发，直接证明那次调用"有没有经过代理"。
 */
@Aspect
public class BookingAspect {

    @Before("execution(* com.alec.InnovateX.spring.aop.TransferTicketService.record(..))")
    public void beforeRecord(JoinPoint jp) {
        TransferTicketService.BOOKINGS.add("record-advised");
        System.out.println("[BookingAspect] 拦截到经过代理的 record()，实参 via=" + jp.getArgs()[0]);
    }
}
