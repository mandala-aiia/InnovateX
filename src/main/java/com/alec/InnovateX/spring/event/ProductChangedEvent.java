package com.alec.InnovateX.spring.event;

/** 固化泛型的具体子类：ResolvableType 能解析出 T = ProductPayload */
public class ProductChangedEvent extends EntityChangedEvent<ProductPayload> {

    public ProductChangedEvent(Object source, ProductPayload payload) {
        super(source, payload);
    }
}
