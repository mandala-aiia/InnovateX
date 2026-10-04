package com.alec.InnovateX.spring.scope;

/**
 * 持有懒代理的一端：字段类型是 Console，实际注入的是 Spring 生成的 CGLIB 懒代理。
 * 代理把"解析目标"推迟到第一次方法调用：调用点才去容器 getBean 真正的 Console。
 */
public class Engine {

    private final Object console;

    public Engine(Object console) {
        this.console = console;
        System.out.println("[Engine] 构造完成，持有 console 的类型: " + console.getClass().getName());
    }

    /** 反射调用：字段用 Object 接住，避免主代码依赖"它是代理"这一事实 */
    public String renderViaConsole() {
        try {
            return (String) console.getClass().getMethod("render").invoke(console);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("调用懒代理失败", e);
        }
    }

    public Object getConsole() {
        return console;
    }

    public String signature() {
        return "Engine@" + Integer.toHexString(System.identityHashCode(this));
    }
}
