package com.alec.InnovateX.spring.spel;

import org.springframework.beans.factory.annotation.Value;

/**
 * @Value 注入七连（#{} 是 SpEL、${} 是占位符，二者可嵌套混用；求值顺序：先解析全部 ${}，再求值 #{}）：
 * 1. #{6 * 7}                                  —— 纯 SpEL 字面量运算
 * 2. #{T(java.lang.Math).max(10, 99)}          —— SpEL 调静态方法
 * 3. #{@seedGenerator.base * 2}                —— SpEL 引用容器内其他 Bean（@bean 名）
 * 4. #{ ${annotation.app.factor:2} * 21 }      —— 占位符在 SpEL 内部"吐出"一个数值再参与运算（key 缺失走占位符默认值）
 * 5. ${annotation.app.name}                    —— 纯占位符（值来自 @PropertySource 引入的 properties）
 * 6. #{ '${annotation.app.name}'.toUpperCase() }        —— 占位符在 SpEL 内部吐出字符串字面量
 * 7. #{ '${annotation.app.version}'.startsWith('1.') ? 'v1 系列' : '新版' } —— 占位符 + SpEL 三元
 */
public class SpelValueHolder {

    @Value("#{6 * 7}")
    private int arithmetic;

    @Value("#{T(java.lang.Math).max(10, 99)}")
    private int staticMax;

    @Value("#{@seedGenerator.base * 2}")
    private int beanReference;

    /** properties 里没有 annotation.app.factor，${} 默认值 2 先就位，再被 SpEL 拿去乘 21 */
    @Value("#{ ${annotation.app.factor:2} * 21 }")
    private int factorMix;

    @Value("${annotation.app.name}")
    private String placeholderName;

    @Value("#{ '${annotation.app.name}'.toUpperCase() }")
    private String placeholderUpper;

    @Value("#{ '${annotation.app.version}'.startsWith('1.') ? 'v1 系列' : '新版' }")
    private String placeholderTernary;

    public int getArithmetic() {
        return arithmetic;
    }

    public int getStaticMax() {
        return staticMax;
    }

    public int getBeanReference() {
        return beanReference;
    }

    public int getFactorMix() {
        return factorMix;
    }

    public String getPlaceholderName() {
        return placeholderName;
    }

    public String getPlaceholderUpper() {
        return placeholderUpper;
    }

    public String getPlaceholderTernary() {
        return placeholderTernary;
    }
}
