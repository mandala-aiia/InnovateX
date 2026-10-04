package com.alec.InnovateX.spring.scope;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * @Lazy 修饰"注入点"打破构造器循环依赖：
 * <p>
 * 引擎(Engine) 构造时需要控制台(Console)，控制台构造时又需要引擎——构造器循环本无解；
 * 但给参数标注 @Lazy 后，容器注入的不是 Console 本尊，而是一个 CGLIB 懒代理，
 * 代理内部持有"按需 getBean"的逻辑：环在"注入时刻"被代理切断，推迟到"首次调用时刻"才闭合，
 * 那时引擎早已构造完毕。
 * <p>
 * 双保险：console 的 @Bean 也标了 @Lazy（懒实例化），refresh 阶段完全不创建它，
 * 让"首次调用才解析目标"在实例计数上清晰可见。
 */
@Configuration
public class LazyCircleConfig {

    /** 引擎：正常构造，依赖"控制台的懒代理" */
    @Bean
    public Engine engine(@Lazy Console console) {
        return new Engine(console);
    }

    /** 控制台：@Lazy 注入点 + @Lazy Bean 定义，环的另一端被彻底推迟 */
    @Bean
    @Lazy
    public Console console(Engine engine) {
        return new Console(engine);
    }
}
