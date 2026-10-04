package com.alec.InnovateX.spring.di;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * 泛型注入：容器里同时有 BaseRepo<User> 和 BaseRepo<Order> 两个 bean，
 * 声明成泛型参数化类型即可精确命中，不需要 @Qualifier。
 */
public class RepoFacade {

    private final BaseRepo<User> userRepo;

    private final BaseRepo<Order> orderRepo;

    @Autowired
    public RepoFacade(BaseRepo<User> userRepo, BaseRepo<Order> orderRepo) {
        this.userRepo = userRepo;
        this.orderRepo = orderRepo;
    }

    public String userRepoName() {
        return userRepo.typeName();
    }

    public String orderRepoName() {
        return orderRepo.typeName();
    }
}
