package com.alec.InnovateX.spring.scope;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * setter/字段注入循环依赖：默认可解。
 * <p>
 * 三级缓存工作过程（menuService → stockService 环）：
 * <ol>
 *   <li>创建 menuService：实例化后先把自己（的 ObjectFactory）放进第三级缓存 singletonFactories；</li>
 *   <li>填充属性需要 stockService → 创建 stockService → 它又需要 menuService →
 *       容器发现"正在创建中"，从三级缓存取出工厂，调用 getEarlyBeanReference 拿到早期引用，
 *       升级存进第二级缓存 earlySingletonObjects，注入给 stockService；</li>
 *   <li>stockService 完成 → menuService 继续填充 → 两个半成品互相补全，各自进入一级缓存。</li>
 * </ol>
 * 若容器关闭 allowCircularReferences（见 ScopeTest 对应用例），第 2 步会直接抛
 * BeanCurrentlyInCreationException——Spring 官方立场（Boot 2.6+ 默认）即不鼓励循环依赖。
 */
@Configuration
public class SetterCircleConfig {

    @Bean
    public MenuService menuService() {
        return new MenuService();
    }

    @Bean
    public StockService stockService() {
        return new StockService();
    }
}
