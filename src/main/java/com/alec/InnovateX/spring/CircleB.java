package com.alec.InnovateX.spring;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CircleB {
    private CircleC circleC;

    public void b() {
        circleC.c();
    }
}
