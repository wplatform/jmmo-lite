package com.github.azeroth.game.movement;


import java.util.ArrayList;
import java.util.Arrays;

public class SplineChainLink {
    public ArrayList<Vector3> points = new ArrayList<>();
    public int expectedDuration;
    public int timeToNext;
    public float velocity;

    public SplineChainLink(Vector3[] points, int expectedDuration, int msToNext, float velocity) {
        points.addAll(Arrays.asList(points));
        expectedDuration = expectedDuration;
        timeToNext = msToNext;
        velocity = velocity;
    }

    public SplineChainLink(int expectedDuration, int msToNext, float velocity) {
        expectedDuration = expectedDuration;
        timeToNext = msToNext;
        velocity = velocity;
    }
}
