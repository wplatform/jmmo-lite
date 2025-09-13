package com.github.azeroth.game.entity.unit;

import com.github.azeroth.game.entity.object.WorldObject;

import java.util.Comparator;


public class HealthPctOrderPred implements Comparator<WorldObject> {
    private final boolean ascending;


    public HealthPctOrderPred() {
        this(true);
    }

    public HealthPctOrderPred(boolean ascending) {
        ascending = ascending;
    }

    public final int compare(WorldObject objA, WorldObject objB) {
        var a = objA.toUnit();
        var b = objB.toUnit();
        var rA = a.getMaxHealth() != 0 ? a.getHealth() / (float) a.getMaxHealth() : 0.0f;
        var rB = b.getMaxHealth() != 0 ? b.getHealth() / (float) b.getMaxHealth() : 0.0f;

        return (int) (_ascending ? rA < rB : rA > rB);
    }
}
