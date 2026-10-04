package com.alec.InnovateX.spring.aot;

/** JDK 动态代理 hint 的靶子接口 */
public interface RemoteCallback {

    void onComplete(String result);
}
