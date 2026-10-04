package com.alec.InnovateX.spring.scope;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 三级缓存 + AOP 联合演示的装配：
 * <p>
 * 创建顺序（alarmCenter 声明在前，决定预实例化顺序）：
 * <ol>
 *   <li>创建 alarmCenter → 实例化后把"早期引用工厂"放入第三级缓存；</li>
 *   <li>属性填充需要 monitorStation → 创建 monitorStation → 它又需要 alarmCenter →
 *       容器调用 getEarlyBeanReference → {@link EarlyProxyProcessor} 提前生成 CGLIB 代理，
 *       monitorStation 注入的就是这个代理；</li>
 *   <li>alarmCenter 初始化完成 → 后置处理器按约定<b>返回原始对象</b> → 容器在 doCreateBean 收尾时
 *       用早期单例引用替换 → 最终放进一级缓存的也是这个代理——
 *       所以"注入的代理"与"容器最终暴露的 Bean"是同一身份，且目标类只构造一次。</li>
 * </ol>
 * BeanPostProcessor 声明为 static @Bean：让它在普通 Bean 之前完成注册，
 * 避免配置类被提前实例化。
 */
@Configuration
public class EarlyProxyConfig {

    @Bean
    public static EarlyProxyProcessor earlyProxyProcessor() {
        return new EarlyProxyProcessor();
    }

    @Bean
    public AlarmCenter alarmCenter() {
        return new AlarmCenter();
    }

    @Bean
    public MonitorStation monitorStation() {
        return new MonitorStation();
    }
}
