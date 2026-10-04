package com.alec.InnovateX.spring;

import com.alec.InnovateX.spring.spel.SpelValueConfig;
import com.alec.InnovateX.spring.spel.SpelValueHolder;
import com.alec.InnovateX.spring.spel.SpelWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 主题③SpEL：表达式解析器（运算/三元/Elvis/正则/根对象/T() 静态/集合投影筛选）
 * + @Value 中 #{} 与 ${} 的混用。每个 @Test 聚焦一个知识点
 */
public class SpelTest {

    /** 算术、字符串拼接、关系与逻辑运算 */
    @Test
    public void arithmeticAndStringOperators() {
        System.out.println("运算符: " + SpelWorkbook.arithmetic());
        assertEquals("7, hello spel, true, true", SpelWorkbook.arithmetic());
    }

    /** 三元运算符、Elvis(?:) 与正则 matches */
    @Test
    public void ternaryElvisAndRegex() {
        System.out.println("三元/Elvis/正则: " + SpelWorkbook.ternaryElvisRegex());
        assertEquals("长名字, Elvis 兜底值, true, false", SpelWorkbook.ternaryElvisRegex());
    }

    /** 根对象属性/方法访问 + T() 静态类型访问 */
    @Test
    public void rootObjectAndStaticTypeAccess() {
        System.out.println("根对象: " + SpelWorkbook.rootPropertyAndMethod());
        System.out.println("T() 静态: " + SpelWorkbook.staticTypeAccess());
        assertEquals("InnovateX, true, 1.0.0", SpelWorkbook.rootPropertyAndMethod());
        assertEquals("9, true, 42", SpelWorkbook.staticTypeAccess());
    }

    /** 集合投影 .![]、筛选 .?[] 与链式组合 */
    @Test
    public void collectionProjectionAndSelection() {
        System.out.println(SpelWorkbook.collectionProjectionSelection());
        assertEquals("投影=[2, 4, 6, 8, 10], 筛选=[2, 4], 链式=[103, 104, 105]",
                SpelWorkbook.collectionProjectionSelection());
    }

    /** @Value 中 #{} 与 ${} 混用（含 Bean 引用 @bean 名 与占位符嵌套进 SpEL） */
    @Test
    public void valueSpelAndPlaceholderMixing() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(SpelValueConfig.class)) {
            SpelValueHolder holder = ctx.getBean(SpelValueHolder.class);
            assertEquals(42, holder.getArithmetic());        // #{6 * 7}
            assertEquals(99, holder.getStaticMax());         // #{T(Math).max}
            assertEquals(200, holder.getBeanReference());    // #{@seedGenerator.base * 2}
            assertEquals(42, holder.getFactorMix());         // #{ ${缺失key:2} * 21 }
            assertEquals("InnovateX", holder.getPlaceholderName());        // 纯 ${}
            assertEquals("INNOVATEX", holder.getPlaceholderUpper());       // #{ '${}'.toUpperCase() }
            assertEquals("v1 系列", holder.getPlaceholderTernary());       // #{ '${}'.startsWith(...) ? : }
            System.out.println("@Value: " + holder.getArithmetic() + ", " + holder.getStaticMax() + ", "
                    + holder.getBeanReference() + ", " + holder.getFactorMix() + ", " + holder.getPlaceholderName()
                    + ", " + holder.getPlaceholderUpper() + ", " + holder.getPlaceholderTernary());
        }
    }
}
