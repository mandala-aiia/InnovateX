package com.alec.InnovateX.spring.spel;

import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.List;

/**
 * SpEL 直接用法：SpelExpressionParser 把"表达式字符串"解析成可在任意上下文求值的表达式。
 * 与 @Value 的关系：@Value("#{...}") 的内部就是这套解析器 + 以容器为根上下文。
 * 覆盖：算术/关系/逻辑运算、三元、Elvis(?:)、正则(matches)、根对象属性与方法、
 * T() 静态类型访问、集合投影 .![] 与筛选 .?[]（及链式组合）
 */
public class SpelWorkbook {

    /** SpelExpressionParser 线程安全，可全局复用 */
    public static final ExpressionParser PARSER = new SpelExpressionParser();

    /** 算术、字符串拼接、关系与逻辑运算：优先级与 Java 一致（先乘除后加减，and 高于 or） */
    public static String arithmetic() {
        String result = PARSER.parseExpression("1 + 2 * 3").getValue().toString();        // 7
        result += ", " + PARSER.parseExpression("'hello' + ' ' + 'spel'").getValue();      // 字符串拼接
        result += ", " + PARSER.parseExpression("(1 + 2) * 3 == 9").getValue();            // 关系运算
        result += ", " + PARSER.parseExpression("3 > 2 and 2 > 1 or 5 < 1").getValue();    // 逻辑运算
        return result;                                                                     // 7, hello spel, true, true
    }

    /** 三元运算符、Elvis(?:)、正则 matches：Elvis 是"为 null 才取默认值"的三元简写 */
    public static String ternaryElvisRegex() {
        String result = PARSER.parseExpression("'jack'.length() > 3 ? '长名字' : '短名字'").getValue(String.class);
        result += ", " + PARSER.parseExpression("null ?: 'Elvis 兜底值'").getValue(String.class);
        result += ", " + PARSER.parseExpression("'13800001111' matches '1[0-9]{10}'").getValue(Boolean.class);
        result += ", " + PARSER.parseExpression("'abc' matches '1[0-9]{10}'").getValue(Boolean.class);
        return result;                                                    // 长名字, Elvis 兜底值, true, false
    }

    /** 根对象属性/方法访问：StandardEvaluationContext 指定 root，表达式里的 name/version 即其属性 */
    public static String rootPropertyAndMethod() {
        StandardEvaluationContext context = new StandardEvaluationContext(new AppMeta(" InnovateX ", "1.0.0"));
        String result = PARSER.parseExpression("name.trim()").getValue(context, String.class);          // 方法调用
        result += ", " + PARSER.parseExpression("name.trim().length() > 5").getValue(context, Boolean.class);
        result += ", " + PARSER.parseExpression("version").getValue(context, String.class);             // 属性(getter)
        return result;                                                                                  // InnovateX, true, 1.0.0
    }

    /** T() 静态类型访问：T(全限定类名) 拿到 Class 对象后可读静态字段、调静态方法 */
    public static String staticTypeAccess() {
        String result = PARSER.parseExpression("T(java.lang.Math).max(3, 9)").getValue(Integer.class).toString();
        result += ", " + PARSER.parseExpression("T(java.lang.Math).PI > 3.14").getValue(Boolean.class).toString();
        result += ", " + PARSER.parseExpression("T(java.lang.String).valueOf(42)").getValue(String.class);
        return result;                                                                                  // 9, true, 42
    }

    /** 集合投影 .![] 与筛选 .?[]：#this 代表正在遍历的当前元素，两者可链式组合 */
    public static String collectionProjectionSelection() {
        StandardEvaluationContext context = new StandardEvaluationContext(new Telemetry(List.of(1, 2, 3, 4, 5)));
        List<?> doubled = PARSER.parseExpression("readings.![#this * 2]").getValue(context, List.class);
        List<?> evens = PARSER.parseExpression("readings.?[#this % 2 == 0]").getValue(context, List.class);
        List<?> chained = PARSER.parseExpression("readings.?[#this > 2].![#this + 100]").getValue(context, List.class);
        return "投影=" + doubled + ", 筛选=" + evens + ", 链式=" + chained;
        // 投影=[2, 4, 6, 8, 10], 筛选=[2, 4], 链式=[103, 104, 105]（先筛出 [3,4,5] 再各自加 100）
    }

    /** 演示根对象①：属性访问走 getter（getName/getVersion） */
    public static class AppMeta {

        private final String name;
        private final String version;

        public AppMeta(String name, String version) {
            this.name = name;
            this.version = version;
        }

        public String getName() {
            return name;
        }

        public String getVersion() {
            return version;
        }
    }

    /** 演示根对象②：承载集合，供投影/筛选表达式遍历 */
    public static class Telemetry {

        private final List<Integer> readings;

        public Telemetry(List<Integer> readings) {
            this.readings = readings;
        }

        public List<Integer> getReadings() {
            return readings;
        }
    }
}
