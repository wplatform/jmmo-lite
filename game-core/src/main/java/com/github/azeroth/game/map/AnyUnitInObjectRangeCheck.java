package com.github.azeroth.game.map;

import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.entity.unit.Unit;

public class AnyUnitInObjectRangeCheck implements ICheck<unit> {
    private final WorldObject obj;
    private final float range;
    private final boolean check3D;


    public AnyUnitInObjectRangeCheck(WorldObject obj, float range) {
        this(obj, range, true);
    }

    public AnyUnitInObjectRangeCheck(WorldObject obj, float range, boolean check3D) {
        obj = obj;
        range = range;
        check3D = check3D;
    }

    public final boolean invoke(Unit u) {
        if (u.isAlive() && obj.isWithinDist(u, range, check3D)) {
            return true;
        }

        return false;
    }
}
