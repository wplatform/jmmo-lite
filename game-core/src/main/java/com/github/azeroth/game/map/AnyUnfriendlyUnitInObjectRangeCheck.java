package com.github.azeroth.game.map;

import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.entity.unit.Unit;

public class AnyUnfriendlyUnitInObjectRangeCheck implements ICheck<unit> {
    private final WorldObject obj;
    private final Unit funit;
    private final float range;
    private final tangible.Func1Param<unit, Boolean> additionalCheck;


    public AnyUnfriendlyUnitInObjectRangeCheck(WorldObject obj, Unit funit, float range) {
        this(obj, funit, range, null);
    }

    public AnyUnfriendlyUnitInObjectRangeCheck(WorldObject obj, Unit funit, float range, tangible.Func1Param<unit, Boolean> additionalCheck) {
        obj = obj;
        funit = funit;
        range = range;
        additionalCheck = additionalCheck;
    }

    public final boolean invoke(Unit u) {
        if (u.isAlive() && obj.isWithinDist(u, range) && !funit.isFriendlyTo(u) && (additionalCheck == null || additionalCheck.invoke(u))) {
            return true;
        } else {
            return false;
        }
    }
}
