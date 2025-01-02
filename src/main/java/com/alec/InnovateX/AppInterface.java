package com.alec.InnovateX;

public interface AppInterface {
    void display();

    void submit();

    default void defaultMethod() {
        System.out.println("This is a default method.");
    }

    static void staticMethod() {
        System.out.println("This is a static method.");
    }
}
