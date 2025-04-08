package com.alec.InnovateX.spring;

import org.springframework.context.LifecycleProcessor;

public class AppLifeCycleProcessor implements LifecycleProcessor{
    @Override
    public void onRefresh() {
        System.out.println("AppLifeCycleProcessor.......................onRefresh");
    }

    @Override
    public void onClose() {
        System.out.println("AppLifeCycleProcessor.......................onClose");
    }

    @Override
    public void start() {
        System.out.println("AppLifeCycleProcessor.......................start");
    }

    @Override
    public void stop() {
        System.out.println("AppLifeCycleProcessor.......................stop");
    }

    @Override
    public boolean isRunning() {
        return false;
    }
}
