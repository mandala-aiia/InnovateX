package com.alec.InnovateX.spring.spel;

import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.List;

/**
 * SpEL（Spring Expression Language）直接用法：SpelExpressionParser 解析表达式字符串。
 * 支持：字面量与运算、三元/Elvis、正则、方法与属性访问、类型 T()、集合投影 .! 与筛选 .?
 */
public class SpelDemo {

    public static final ExpressionParser PARSER = new SpelExpressionParser();

    /** 运算符与字面量 */
    public static String operators() {
        String result = PARSER.parseExpression("1 + 2 * 3").getValue().toString();          // 7
        result += ", " + PARSER.parseExpression("'hello' + ' ' + 'spel'").getValue();       // hello spel
        result += ", " + PARSER.parseExpression("7 % 3 == 1 ? '奇' : '偶'").getValue();     // 三元
        result += ", " + PARSER.parseExpression("null ?: '默认值'").getValue();             // Elvis
        result += ", " + PARSER.parseExpression("'13800001111' matches '1[0-9]{10}'").getValue(); // 正则
        return result;
    }

    /** 属性与方法访问、静态类型 T() */
    public static String propertyAndMethod() {
        StandardEvaluationContext context = new StandardEvaluationContext(new Root(" InnovateX "));
        String result = PARSER.parseExpression("name.trim()").getValue(context, String.class);
        result += ", " + PARSER.parseExpression("name.length() > 5").getValue(context, Boolean.class);
        result += ", " + PARSER.parseExpression("T(java.lang.Math).max(3, 9)").getValue(Integer.class).toString();
        return result;
    }

    /** 集合投影（.!）与筛选（.?） */
    public static String collectionProjectionAndSelection() {
        StandardEvaluationContext context = new StandardEvaluationContext(new ListRoot(List.of(1, 2, 3, 4, 5)));
        List<Integer> doubled = PARSER.parseExpression("numbers.![#this * 2]").getValue(context, List.class);
        List<Integer> even = PARSER.parseExpression("numbers.?[#this % 2 == 0]").getValue(context, List.class);
        return "投影翻倍=" + doubled + ", 筛选偶数=" + even;
    }

    /** 演示用根对象 */
    public static class Root {
        private final String name;

        public Root(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public static class ListRoot {
        private final List<Integer> numbers;

        public ListRoot(List<Integer> numbers) {
            this.numbers = numbers;
        }

        public List<Integer> getNumbers() {
            return numbers;
        }
    }
}
