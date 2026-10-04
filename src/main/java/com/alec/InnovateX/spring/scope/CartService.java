package com.alec.InnovateX.spring.scope;

/** 单例服务：构造器拿到的其实是 SessionCart 的 CGLIB 代理（scoped proxy）。 */
public class CartService {

    private final SessionCart cart;

    public CartService(SessionCart cart) {
        this.cart = cart;
    }

    public void add(String item) {
        cart.add(item);
    }

    public int size() {
        return cart.size();
    }
}
