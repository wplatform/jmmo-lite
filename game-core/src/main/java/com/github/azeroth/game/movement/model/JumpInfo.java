package com.github.azeroth.game.movement.model;

import lombok.Data;

// jumping
@Data
public class JumpInfo {
    public int fallTime;
    public float zSpeed;
    public float sinAngle;
    public float cosAngle;
    public float xySpeed;

    public void reset() {
        fallTime = 0;
        zSpeed = sinAngle = cosAngle = xySpeed = 0.0f;
    }
}
