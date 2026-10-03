package com.alec.InnovateX.spring.javaconfig;

/** 持有数据源引用的 DAO POJO：观察它拿到的是容器里的单例还是新 new 出来的实例 */
public class OrderDao {

    private final OrderDataSource dataSource;

    public OrderDao(OrderDataSource dataSource) {
        this.dataSource = dataSource;
        System.out.println("[OrderDao] 创建实例，持有 dataSource @" + Integer.toHexString(System.identityHashCode(dataSource)));
    }

    public OrderDataSource getDataSource() {
        return dataSource;
    }
}
