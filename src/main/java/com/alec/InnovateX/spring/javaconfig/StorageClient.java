package com.alec.InnovateX.spring.javaconfig;

/** 持有 StorageEngine 的客户端 POJO：观察它拿到的是容器里的单例，还是方法直调 new 出来的新实例 */
public class StorageClient {

    private final StorageEngine engine;

    public StorageClient(StorageEngine engine) {
        this.engine = engine;
        System.out.println("[StorageClient] 创建，持有 engine @" + Integer.toHexString(System.identityHashCode(engine)));
    }

    public StorageEngine getEngine() {
        return engine;
    }
}
