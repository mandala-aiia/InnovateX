package com.alec.InnovateX;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CircleA {
    private CircleB circleB;

    public void a(){
        circleB.b();
    }
}
