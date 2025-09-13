package com.github.azeroth.game.entity.unit;


import com.github.azeroth.game.entity.object.WorldObject;

import java.util.Comparator;


public class PowerPctOrderPred implements Comparator<WorldObject> {
    private final Power power;
    private final boolean ascending;


    public PowerPctOrderPred(Power power) {
        this(power, true);
    }

    public PowerPctOrderPred(Power power, boolean ascending) {
        power = power;
        ascending = ascending;
    }

    public final int compare(WorldObject objA, WorldObject objB) {
        var a = objA.toUnit();
        var b = objB.toUnit();
        var rA = a != null ? a.getPowerPct(power) : 0.0f;
        var rB = b != null ? b.getPowerPct(power) : 0.0f;

        return (int) (_ascending ? rA < rB : rA > rB);
    }
}
