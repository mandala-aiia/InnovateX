package com.alec.InnovateX.spring;


public abstract class AppAbsService implements AppInterface {

    @Override
    public void display() {
        System.out.println("AppAbsService...............display");
    }

    @Override
    public void submit() {
        System.out.println("AppAbsService...............submit");
    }
}
