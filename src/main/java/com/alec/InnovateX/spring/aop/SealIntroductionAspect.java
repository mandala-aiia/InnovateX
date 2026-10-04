package com.alec.InnovateX.spring.aop;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.DeclareParents;

/**
 * 引介增强切面：@DeclareParents 声明"给匹配类型的代理额外实现 Sealable 接口，默认实现用 SealMixin"。
 * 目标类零改动，它从容器里拿出来的代理却能被转型为 Sealable——
 * 这就是 Spring AOP 在不改类的前提下"混入"新能力的机制（底层对应 Introduction Advisor / DeclareParentsAdvisor）。
 *
 * 配套的 @Before 用 this(sealedProxy) 把"代理对象本身"绑定为参数（代理实现了 Sealable）：
 * 密封状态下拒绝追加条款，目标类的 append() 对密封逻辑毫无感知。
 */
@Aspect
public class SealIntroductionAspect {

    /** value 匹配目标类型，+ 表示含子类；defaultImpl 是混入的实现类 */
    @DeclareParents(value = "com.alec.InnovateX.spring.aop.ContractPaperService+", defaultImpl = SealMixin.class)
    public static Sealable sealable;

    @Before("execution(* com.alec.InnovateX.spring.aop.ContractPaperService.append(..)) && this(sealedProxy)")
    public void refuseWhenSealed(Sealable sealedProxy) {
        if (sealedProxy.isSealed()) {
            throw new IllegalStateException("合同已密封，禁止追加条款");
        }
    }
}
