package com.alec.InnovateX.spring.aop;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.DeclareParents;

/**
 * 引介增强（Introduction）：AOP 五种通知之外的第六种增强。
 * @DeclareParents 声明"给匹配类型的代理额外实现 Lockable 接口，默认实现用 LockMixin"——
 * 目标类 DocumentService 不需要做任何修改，其代理就能被转型为 Lockable。
 * 这就是 Spring AOP 能在不改类的前提下"混入"新能力的机制（对应底层概念：Introduction Advisor）
 */
@Aspect
public class IntroductionAspect {

    /** value 匹配目标类型，+ 表示含子类；defaultImpl 是混入的实现类 */
    @DeclareParents(value = "com.alec.InnovateX.spring.aop.DocumentService+", defaultImpl = LockMixin.class)
    public static Lockable lockable;

    /**
     * 配套切面：this(lockableObj) 把"代理对象本身"绑定为参数（代理实现了 Lockable），
     * 锁定状态下拒绝写入——目标类的 write() 对锁一无所知
     */
    @Before("execution(* com.alec.InnovateX.spring.aop.DocumentService.write(..)) && this(lockableObj)")
    public void checkNotLocked(Lockable lockableObj) {
        if (lockableObj.isLocked()) {
            throw new IllegalStateException("文档已锁定，禁止写入");
        }
    }
}
