package com.github.azeroth.game.movement.model;


import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;
import java.util.Arrays;

public class SplineChainLink {
    public ArrayList<Vector3> points = new ArrayList<>();
    public int expectedDuration;
    public int timeToNext;
    public float velocity;

    public SplineChainLink(Vector3[] points, int expectedDuration, int msToNext, float velocity) {
        this.points.addAll(Arrays.asList(points));
        this.expectedDuration = expectedDuration;
        this.timeToNext = msToNext;
        this.velocity = velocity;
    }

    public SplineChainLink(int expectedDuration, int msToNext, float velocity) {
        this.expectedDuration = expectedDuration;
        this.timeToNext = msToNext;
        this.velocity = velocity;
    }
}
