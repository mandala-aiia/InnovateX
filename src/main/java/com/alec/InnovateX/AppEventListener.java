package com.alec.InnovateX;

import org.springframework.context.ApplicationListener;

public class AppEventListener implements ApplicationListener<AppEvent> {
    @Override
    public void onApplicationEvent(AppEvent event) {
        String msg = event.getMessage();
        System.out.println("接收到的信息："+msg);
    }
}
