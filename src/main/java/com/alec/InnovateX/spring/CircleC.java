package com.alec.InnovateX.spring;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CircleC {
    private CircleA circleA;

    public void c() {
        circleA.a();
    }
}
